package app.notekea.chat.presentation

import app.notekea.chat.domain.ActOnRequestUseCase
import app.notekea.chat.domain.ActionContext
import app.notekea.chat.domain.ActionKind
import app.notekea.chat.domain.ActionRunner
import app.notekea.chat.domain.ActionTurnStep
import app.notekea.chat.domain.ChatExchange
import app.notekea.chat.domain.TurnOutcome
import app.notekea.core.appearance.domain.ColorTheme
import app.notekea.core.domain.Outcome
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
    private val update: (id: Long, change: (ChatEntry) -> ChatEntry) -> Unit,
    private val emit: (ChatEffect) -> Unit
) {
    /** Acts on [request] for the entry [id]; returns what is left to do: answer it, talk, or nothing. */
    suspend fun act(
        id: Long,
        request: String,
        notes: Collection<NoteEntry>,
        history: List<ChatExchange>,
        openNote: RelativePath?
    ): ChatReply {
        var reply: ChatReply = ChatReply.Acted

        actOnRequest(
            request = request,
            vaultPath = vaultPath,
            notes = notes,
            context = contextOf(
                request = request,
                notes = notes,
                openNote = openNote
            ).copy(history = history),
            openNote = openNote
        ).collect { step ->
            when (step) {
                is ActionTurnStep.Answer -> reply = ChatReply.Question(question = step.question)

                is ActionTurnStep.Chat -> reply = ChatReply.Talk(note = step.note)

                is ActionTurnStep.Help -> reply = ChatReply.Help(request = step.request)

                ActionTurnStep.Overview -> reply = ChatReply.Overview

                else -> follow(
                    id = id,
                    step = step
                )
            }
        }

        return reply
    }

    private fun follow(
        id: Long,
        step: ActionTurnStep
    ) {
        // The window acts outside the state's update, which may run more than once.
        if (step is ActionTurnStep.Shown) step.steps.forEach { emit(ChatEffect.Show(step = it)) }

        update(id) { entry ->
            val action = entry.action ?: ChatAction(status = ActionStatus.FINISHED)

            when (step) {
                ActionTurnStep.Planning,
                is ActionTurnStep.Answer,
                is ActionTurnStep.Chat,
                is ActionTurnStep.Help,
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

                is ActionTurnStep.Problem -> acted(
                    entry,
                    action.copy(
                        status = ActionStatus.FAILED,
                        problem = step.problem
                    )
                )

                ActionTurnStep.Finished -> acted(entry, action.copy(status = ActionStatus.FINISHED))
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

        update(entry.id) { it.copy(action = action.copy(status = ActionStatus.RUNNING)) }

        when (
            val done = actionRunner.run(vaultPath = vaultPath, steps = action.steps, notes = notes)
        ) {
            is Outcome.Success -> {
                update(entry.id) {
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
                    ?.let { emit(ChatEffect.Open(note = it)) }

                action
                    .steps
                    .filter {
                        !it.kind.isConfirmed && it.kind != ActionKind.SET_THEME
                    }
                    .forEach { emit(ChatEffect.Show(step = it)) }
            }

            is Outcome.Failure -> update(entry.id) {
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

        update(entry.id) { it.copy(action = action.copy(status = ActionStatus.CANCELLED)) }
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

        update(entry.id) { it.copy(action = action.copy(status = ActionStatus.UNDONE)) }
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

        val today = localCalendar.momentOf(instant = clock.now()).date

        return ActionContext(
            today = "${today.year}-${twoDigits(today.month)}-${twoDigits(today.day)}",
            openNote = openNote?.value?.removeSuffix(suffix = MARKDOWN),
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
            }
        )
    }

    private fun twoDigits(value: Int): String = value.toString().padStart(
        length = 2,
        padChar = '0'
    )

    private companion object {
        const val MARKDOWN = ".md"
        val WORD_BREAK = Regex(pattern = "[^\\p{L}\\p{N}]+")
    }
}
