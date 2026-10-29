package com.testbuddy.app

import com.testbuddy.CaseResult
import com.testbuddy.ManualAnswer
import com.testbuddy.ReportEnvironment
import com.testbuddy.Suite
import com.testbuddy.SuiteRunner
import com.testbuddy.formatReport
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.extension

/**
 * The Self-Test's three steps for one [suite]: runs the automatic cases in the [background], walks the tester through
 * the manual ones (backing up the installed app first and preparing each case's data) and writes the report. What
 * the tester did is kept in [folders], so closing the window and opening it again goes on where they were.
 */
internal class SelfTestSession(
    private val suite: Suite,
    private val folders: SelfTestFolders,
    private val scope: CoroutineScope,
    private val background: CoroutineDispatcher,
    private val environment: () -> ReportEnvironment = ReportEnvironment::current,
    private val runner: SuiteRunner = SuiteRunner(suite = suite, workFolder = folders.work.resolve(RUNS))
) {
    private val resultStore = ResultStore(file = folders.work.resolve(RESULTS))
    private val answerStore = AnswerStore(file = folders.work.resolve(ANSWERS))
    private val backup = AppBackup(folder = folders.work.resolve(BACKUP), paths = suite.backUp)
    private val manualData = ManualData(sources = suite.dataSources, target = folders.data)
    private val mutableState = MutableStateFlow(value = restoredState())

    @Volatile private var stopRequested = false

    val state: StateFlow<SelfTestState> = mutableState.asStateFlow()

    fun startAutomatic() {
        if (mutableState.value.isRunning) return
        stopRequested = false
        mutableState.update {
            it.copy(results = emptyMap(), isRunning = true, runningId = suite.automated.firstOrNull()?.id)
        }
        scope.launch {
            withContext(background) { runner.run(shouldStop = { stopRequested }, onResult = ::record) }
            mutableState.update { it.copy(isRunning = false, runningId = null) }
        }
    }

    fun stopAutomatic() {
        stopRequested = true
    }

    fun goToManual() {
        backup.make()
        val firstOpen = suite.manual.indexOfFirst { it.id !in mutableState.value.answers }.coerceAtLeast(0)
        mutableState.update { it.copy(step = SelfTestStep.MANUAL) }
        showManual(index = firstOpen)
    }

    fun pass() = answer(answer = ManualAnswer.Passed(id = currentManual().id))

    fun fail(
        note: String,
        screenshot: Path?
    ) {
        val id = currentManual().id
        val name = screenshot?.let { copyIntoReport(file = it, name = "$id.${it.extension}") }
        answer(answer = ManualAnswer.Failed(id = id, note = note.trim(), screenshot = name))
    }

    fun skip() = advance()

    fun previous() = showManual(index = mutableState.value.manualIndex - 1)

    fun goToReport() {
        val current = mutableState.value
        val automatic = suite.automated.map { current.results[it.id] ?: CaseResult.Skipped(id = it.id) }
        val manual = suite.manual.map { current.answers[it.id] ?: ManualAnswer.NotRun(id = it.id) }
        automatic.filterIsInstance<CaseResult.Failed>().forEach { failed ->
            failed.screenshot?.takeIf(Files::isRegularFile)?.let {
                copyIntoReport(file = it, name = it.fileName.toString())
            }
        }
        val report = formatReport(suite = suite, environment = environment(), automatic = automatic, manual = manual)
        mutableState.update { it.copy(step = SelfTestStep.REPORT, report = report) }
    }

    fun backToManual() = mutableState.update { it.copy(step = SelfTestStep.MANUAL) }

    /** Puts the installed app back as it was before the manual cases and deletes the test data. */
    fun finish() {
        backup.restore()
        manualData.clear()
        mutableState.update { it.copy(isClean = true) }
    }

    private fun record(result: CaseResult) {
        val next = suite.automated.getOrNull(suite.automated.indexOfFirst { it.id == result.id } + 1)?.id
        mutableState.update {
            it.copy(results = it.results + (result.id to result), runningId = next.takeIf { _ -> it.isRunning })
        }
        resultStore.save(results = mutableState.value.results.values)
    }

    private fun answer(answer: ManualAnswer) {
        mutableState.update { it.copy(answers = it.answers + (answer.id to answer)) }
        answerStore.save(answers = mutableState.value.answers.values)
        advance()
    }

    private fun advance() {
        val next = mutableState.value.manualIndex + 1
        if (next < suite.manual.size) showManual(index = next) else goToReport()
    }

    private fun showManual(index: Int) {
        val case = suite.manual.getOrNull(index) ?: return
        manualData.prepare(folders = case.data)
        mutableState.update { it.copy(manualIndex = index) }
    }

    private fun currentManual() = suite.manual[mutableState.value.manualIndex]

    private fun copyIntoReport(
        file: Path,
        name: String
    ): String {
        Files.copy(file, folders.report.resolve(name), StandardCopyOption.REPLACE_EXISTING)
        return name
    }

    private fun restoredState(): SelfTestState {
        folders.create()
        val results = resultStore.load()
        val answers = answerStore.load()
        val step = if (answers.isNotEmpty()) SelfTestStep.MANUAL else SelfTestStep.AUTOMATIC
        val index = suite.manual.indexOfFirst { it.id !in answers }.takeIf { it >= 0 } ?: 0
        return SelfTestState(step = step, results = results, manualIndex = index, answers = answers)
    }

    private companion object {
        const val RUNS = "runs"
        const val RESULTS = "results.txt"
        const val ANSWERS = "answers.txt"
        const val BACKUP = "backup"
    }
}
