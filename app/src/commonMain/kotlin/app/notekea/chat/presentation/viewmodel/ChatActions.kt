package app.notekea.chat.presentation.viewmodel

import app.notekea.chat.domain.ChatExchange
import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.domain.agent.AgentUse
import app.notekea.chat.domain.answer.AskedWords
import app.notekea.chat.domain.language.SpokenLanguage
import app.notekea.chat.domain.plan.ActOnRequestUseCase
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionProblem
import app.notekea.chat.domain.plan.ActionRunner
import app.notekea.chat.domain.plan.ActionTurnStep
import app.notekea.chat.domain.plan.FoundNotes
import app.notekea.chat.presentation.model.ActionStatus
import app.notekea.chat.presentation.model.ChatAction
import app.notekea.chat.presentation.model.ChatEffect
import app.notekea.chat.presentation.model.ChatEntry
import app.notekea.chat.presentation.model.ChatStep
import app.notekea.core.appearance.domain.ColorTheme
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.domain.time.LocalCalendar
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath
import kotlin.time.Clock

/**
 * The chat's side of actions (3.8, 3.9), for [ChatViewModel]: a request is planned and acted on, a question handed
 * back to be answered; the card's Do it, Cancel and Undo. [update] changes the entry of an id, [emit]
 * sends the window what to show.
 */
internal class ChatActions(
    private val vaultPath: String,
    private val actOnRequest: ActOnRequestUseCase,
    private val actionRunner: ActionRunner,
    private val localCalendar: LocalCalendar,
    private val clock: Clock,
    private val host: ChatHost
) {
    /** Acts on [request] for the entry [id]; returns what is left to do: answer it, talk, or nothing. */
    suspend fun act(
        id: Long,
        request: String,
        notes: Collection<NoteEntry>,
        history: List<ChatExchange>,
        agentUse: AgentUse,
        openNote: RelativePath?
    ): ChatReply {
        var reply: ChatReply = ChatReply.Acted
        val found = mutableListOf<FoundNotes>()

        actOnRequest(
            request = request,
            vaultPath = vaultPath,
            notes = notes,
            context = contextOf(
                request = request,
                notes = notes,
                openNote = openNote
            ).copy(
                history = history,
                language = SpokenLanguage.inChat(
                    text = request,
                    history = history
                )
            ),
            openNote = openNote,
            agentUse = agentUse
        ).collect { step ->
            when (step) {
                is ActionTurnStep.Answer -> reply = ChatReply.Question(question = step.question)

                is ActionTurnStep.Chat -> reply = ChatReply.Talk(note = step.note)

                is ActionTurnStep.Help -> reply = ChatReply.Help(request = step.request)

                ActionTurnStep.Overview -> reply = ChatReply.Overview

                ActionTurnStep.ToAgent -> reply = ChatReply.Agent

                else -> {
                    if (step is ActionTurnStep.Found) found += step.found

                    follow(
                        id = id,
                        step = step
                    )
                }
            }
        }

        // A question the plan answered with tables is also answered in words, above them.
        val isAskedOfTables =
            reply == ChatReply.Acted &&
                found.isNotEmpty() &&
                AskedWords.asksAbout(request)

        return if (isAskedOfTables) {
            ChatReply.FromFound(
                question = request,
                tables = found
            )
        } else {
            reply
        }
    }

    private fun follow(
        id: Long,
        step: ActionTurnStep
    ) {
        // The window acts outside the state's update, which may run more than once.
        if (step is ActionTurnStep.Shown) {
            step.steps.forEach { host.emit(ChatEffect.Show(step = it)) }
        }

        host
            .update(id) { entry ->
                val action = entry.action ?: ChatAction(status = ActionStatus.FINISHED)

                when (step) {
                    ActionTurnStep.Planning,
                    is ActionTurnStep.Answer,
                    is ActionTurnStep.Chat,
                    is ActionTurnStep.Help,
                    ActionTurnStep.ToAgent,
                    ActionTurnStep.Overview -> entry.copy(step = ChatStep.PLANNING)

                    ActionTurnStep.Writing -> entry.copy(
                        step = ChatStep.COMPOSING,
                        action = action
                    )

                    is ActionTurnStep.Found ->
                        entry.copy(action = action.copy(found = action.found + step.found))

                    is ActionTurnStep.Shown ->
                        entry.copy(action = action.copy(shown = action.shown + step.steps))

                    is ActionTurnStep.Proposed -> acted(
                        entry,
                        action.copy(
                            status = ActionStatus.PROPOSED,
                            steps = step.steps
                        )
                    )

                    // The question takes the turn's place: an action card there was drawn instead.
                    is ActionTurnStep.Problem ->
                        (step.problem as? ActionProblem.SectionAsked)?.let {
                            entry.copy(
                                step = ChatStep.DONE,
                                outcome = TurnOutcome.ANSWERED,
                                action = null,
                                sectionAsked = it.section
                            )
                        } ?: acted(
                            entry,
                            action.copy(
                                status = ActionStatus.FAILED,
                                problem = step.problem
                            )
                        )

                    ActionTurnStep.Finished -> acted(
                        entry,
                        action.copy(status = ActionStatus.FINISHED)
                    )
                }
            }
    }

    private fun acted(
        entry: ChatEntry,
        action: ChatAction
    ): ChatEntry = entry.copy(
        step = ChatStep.DONE,
        outcome = TurnOutcome.ACTED,
        action = action
    )

    /** Do it: the card's steps are carried out; the first note made opens. */
    suspend fun confirm(
        entry: ChatEntry,
        notes: Collection<NoteEntry>
    ) {
        val action = entry.action?.takeIf {
            it.status == ActionStatus.PROPOSED
        } ?: return

        host.update(entry.id) { it.copy(action = action.copy(status = ActionStatus.RUNNING)) }

        when (
            val done = actionRunner.run(vaultPath = vaultPath, steps = action.steps, notes = notes)
        ) {
            is Outcome.Success -> {
                host
                    .update(entry.id) {
                        it.copy(
                            action = action.copy(
                                status = ActionStatus.DONE,
                                done = done.value
                            )
                        )
                    }

                done
                    .value
                    .opened
                    ?.let { host.emit(ChatEffect.Open(note = it)) }

                action
                    .steps
                    .filter {
                        !it.kind.isConfirmed && it.kind != ActionKind.SET_THEME
                    }
                    .forEach { host.emit(ChatEffect.Show(step = it)) }
            }

            is Outcome.Failure ->
                host
                    .update(entry.id) {
                        it.copy(
                            action = action.copy(
                                status = ActionStatus.FAILED,
                                problem = done.error
                            )
                        )
                    }
        }
    }

    fun cancel(entry: ChatEntry) {
        val action = entry.action?.takeIf {
            it.status == ActionStatus.PROPOSED
        } ?: return

        host.update(entry.id) { it.copy(action = action.copy(status = ActionStatus.CANCELLED)) }
    }

    suspend fun undo(entry: ChatEntry) {
        val action = entry.action ?: return

        val done = action.done?.takeIf {
            action.status == ActionStatus.DONE
        } ?: return

        actionRunner.undo(
            vaultPath = vaultPath,
            done = done
        )

        host.update(entry.id) { it.copy(action = action.copy(status = ActionStatus.UNDONE)) }
    }

    /** Today's date as the model reads it, YYYY-MM-DD. */
    fun today(): String = dateOf(date = localCalendar.momentOf(instant = clock.now()).date)

    private fun dateOf(date: CalendarDate): String =
        "${date.year}-${twoDigits(date.month)}-${twoDigits(date.day)}"

    /** Today for the talk, with what changed in [notes]: "2026-11-11. Notes changed today: none. Last changed: …". */
    fun todayIn(notes: Collection<NoteEntry>): String {
        val context = contextOf(
            request = "",
            notes = notes,
            openNote = null
        )

        val changed = context
            .changedToday
            .joinToString()
            .ifEmpty { "none" }

        val lastAt = notes
            .mapNotNull { it.file.modifiedAt }
            .maxOrNull()
            ?.let { dateOf(date = localCalendar.momentOf(instant = it).date) }

        val last = context
            .latestNote
            ?.let { " The last changed note is $it, changed on $lastAt." }
            .orEmpty()

        return "${context.today}. Notes changed today: $changed.$last"
    }

    /** The note changed last in [notes] and those changed today, by their paths without the extension. */
    private fun changesIn(notes: Collection<NoteEntry>): Pair<String?, List<String>> {
        val latest = notes
            .filter {
                it.file.modifiedAt != null
            }
            .maxByOrNull {
                it.file.modifiedAt!!
            }
            ?.file
            ?.path
            ?.value
            ?.removeSuffix(suffix = MARKDOWN)

        val day = localCalendar.momentOf(instant = clock.now()).date

        val changedToday = notes
            .filter { note ->
                note
                    .file
                    .modifiedAt
                    ?.let {
                        localCalendar.momentOf(instant = it).date == day
                    } == true
            }
            .map {
                it
                    .file
                    .path
                    .value
                    .removeSuffix(suffix = MARKDOWN)
            }

        return latest to changedToday
    }

    /** What the plan, or agent mode, may name in the vault: its names, the closest to [request] first. */
    fun contextOf(
        request: String,
        notes: Collection<NoteEntry>,
        openNote: RelativePath?
    ): ActionContext {
        val words = request
            .lowercase()
            .split(WORD_BREAK)
            .filter {
                it.length > 2
            }
            .toSet()

        val paths = notes.map { it.file.path }

        val names = paths
            .sortedByDescending { path ->
                words.count {
                    it in path.value.lowercase()
                }
            }
            .map { it.value.removeSuffix(suffix = MARKDOWN) }

        val (latest, changedToday) = changesIn(notes = notes)

        return ActionContext(
            today = today(),
            changedToday = changedToday,
            openNote = openNote?.value?.removeSuffix(suffix = MARKDOWN),
            latestNote = latest,
            folders = ActionContext.foldersOf(paths = paths),
            notes = names,
            properties = notes
                .flatMap { note ->
                    note
                        .metadata
                        .properties
                        .map { it.key }
                }
                .distinct()
                .sorted(),
            tags = notes
                .flatMap {
                    it.metadata.tags + it.metadata.textTags
                }
                .distinct()
                .sorted(),
            themes = ColorTheme.entries.map {
                it.id.replace(
                    oldChar = '_',
                    newChar = ' '
                )
            } + MODE_THEMES
        )
    }

    private fun twoDigits(value: Int): String = value.toString().padStart(
        length = 2,
        padChar = '0'
    )

    private companion object {
        const val MARKDOWN = ".md"
        val WORD_BREAK = Regex(pattern = "[^\\p{L}\\p{N}]+")

        // The app's light or dark mode, which set_theme also takes.
        val MODE_THEMES = listOf("dark", "light", "system")
    }
}
