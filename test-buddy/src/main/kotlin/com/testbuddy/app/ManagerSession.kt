package com.testbuddy.app

import com.testbuddy.CaseResult
import com.testbuddy.ManualAnswer
import com.testbuddy.Suite
import com.testbuddy.crash.AppEnvironment
import com.testbuddy.crash.Breadcrumbs
import com.testbuddy.formatReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.extension

/**
 * The manager's work for one [suite]: runs the automatic cases in their own process ([startRun]), walks the tester
 * through the manual ones a step at a time, preparing each case's data, and writes the report. What the tester did is
 * kept in [folders], so the app can close or restart and the manager goes on where it was. [startRun] is how a run
 * starts, given what to tell about it and when it ends ([AutomaticRun] in the app).
 */
internal class ManagerSession(
    private val suite: Suite,
    private val folders: SelfTestFolders,
    private val environment: () -> AppEnvironment = AppEnvironment::current,
    private val startRun: (onEvent: (RunnerEvent) -> Unit, onEnd: () -> Unit) -> AutomaticRun = { onEvent, onEnd ->
        AutomaticRun(log = folders.report.resolve(RUN_LOG), onEvent = onEvent, onEnd = onEnd).also { it.start() }
    }
) {
    private val resultStore = ResultStore(file = folders.work.resolve(RESULTS))
    private val answerStore = AnswerStore(file = folders.work.resolve(ANSWERS))
    private val position = PositionStore(file = folders.work.resolve(POSITION))
    private val manualData = ManualData(sources = suite.dataSources, target = folders.data)
    private val mutableState = MutableStateFlow(value = restoredState())
    private var run: AutomaticRun? = null

    val state: StateFlow<ManagerState> = mutableState.asStateFlow()

    fun startAutomatic() {
        if (mutableState.value.isRunning) return
        mutableState.update {
            it.copy(results = emptyMap(), isRunning = true, runningId = null, startedAt = System.currentTimeMillis())
        }
        run = startRun(::onEvent) { mutableState.update { it.copy(isRunning = false, runningId = null) } }
    }

    fun stopAutomatic() {
        run?.stop()
    }

    fun select(tab: ManagerTab) {
        if (tab == ManagerTab.REPORT) writeReport()
        if (tab ==
            ManagerTab.MANUAL
        ) {
            showManual(index = mutableState.value.manualIndex, step = mutableState.value.manualStep)
        }
        mutableState.update { it.copy(tab = tab) }
        savePosition()
    }

    fun nextStep() = moveStep(by = 1)

    fun previousStep() = moveStep(by = -1)

    fun pass() = answer(answer = ManualAnswer.Passed(id = currentManual().id))

    fun fail(
        note: String,
        screenshot: Path?
    ) {
        val id = currentManual().id
        val name = screenshot?.let { copyIntoReport(file = it, name = "$id.${it.extension}") }
        answer(
            answer = ManualAnswer.Failed(id = id, note = note.trim(), screenshot = name, steps = Breadcrumbs.recent())
        )
    }

    fun skip() = showManual(index = mutableState.value.manualIndex + 1, step = 0)

    fun previousCase() = showManual(index = mutableState.value.manualIndex - 1, step = 0)

    /** The current case's own variables for a restart (a crash test), none for the others. */
    fun launchEnvironment(): Map<String, String> =
        suite.manual.getOrNull(mutableState.value.manualIndex)?.launchEnvironment.orEmpty()

    private fun onEvent(event: RunnerEvent) {
        when (event) {
            is RunnerEvent.Started -> mutableState.update { it.copy(runningId = event.id, runningSteps = emptyList()) }

            is RunnerEvent.Step -> mutableState.update {
                it.copy(runningSteps = (it.runningSteps + event.text).takeLast(n = SHOWN_STEPS))
            }

            is RunnerEvent.Ended -> {
                mutableState.update { it.copy(results = it.results + (event.result.id to event.result)) }
                resultStore.save(results = mutableState.value.results.values)
            }

            RunnerEvent.Done -> Unit
        }
    }

    private fun moveStep(by: Int) {
        val steps = currentManual().stepList.size
        mutableState.update {
            it.copy(manualStep = (it.manualStep + by).coerceIn(minimumValue = 0, maximumValue = steps))
        }
        savePosition()
    }

    private fun answer(answer: ManualAnswer) {
        mutableState.update { it.copy(answers = it.answers + (answer.id to answer)) }
        answerStore.save(answers = mutableState.value.answers.values)
        val next = mutableState.value.manualIndex + 1
        if (next < suite.manual.size) showManual(index = next, step = 0) else select(tab = ManagerTab.REPORT)
    }

    private fun showManual(
        index: Int,
        step: Int
    ) {
        val case = suite.manual.getOrNull(index) ?: return
        manualData.prepare(folders = case.data)
        mutableState.update { it.copy(manualIndex = index, manualStep = step) }
        savePosition()
    }

    private fun currentManual() = suite.manual[mutableState.value.manualIndex]

    private fun writeReport() {
        val current = mutableState.value
        val automatic = suite.automated.map { current.results[it.id] ?: CaseResult.Skipped(id = it.id) }
        val manual = suite.manual.map { current.answers[it.id] ?: ManualAnswer.NotRun(id = it.id) }
        automatic.filterIsInstance<CaseResult.Failed>().forEach { failed ->
            failed.screenshot?.takeIf(Files::isRegularFile)?.let {
                copyIntoReport(file = it, name = it.fileName.toString())
            }
        }
        val diagnostics = runCatching { suite.diagnostics() }.getOrNull()
        val report = formatReport(suite, environment(), automatic, manual, diagnostics)
        Files.writeString(folders.report.resolve(REPORT), report)
        mutableState.update { it.copy(report = report) }
    }

    private fun copyIntoReport(
        file: Path,
        name: String
    ): String {
        Files.copy(file, folders.report.resolve(name), StandardCopyOption.REPLACE_EXISTING)
        return name
    }

    private fun savePosition() = mutableState.value.let {
        position.save(tab = it.tab, index = it.manualIndex, step = it.manualStep)
    }

    private fun restoredState(): ManagerState {
        folders.create()
        val saved = position.load()
        val state = ManagerState(
            tab = saved?.tab ?: ManagerTab.AUTOMATIC,
            results = resultStore.load(),
            manualIndex = saved?.index ?: 0,
            manualStep = saved?.step ?: 0,
            answers = answerStore.load()
        )
        return if (state.tab == ManagerTab.REPORT) state.copy(tab = ManagerTab.MANUAL) else state
    }

    private companion object {
        const val RESULTS = "results.txt"
        const val ANSWERS = "answers.txt"
        const val POSITION = "position.txt"
        const val REPORT = "report.txt"
        const val RUN_LOG = "automatic run output.txt"

        // The steps of the running case the manager lists: enough to follow it, few enough to read.
        const val SHOWN_STEPS = 6
    }
}
