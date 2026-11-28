package app.notekea.chat.presentation.viewmodel

import app.notekea.chat.domain.ChatError
import app.notekea.chat.domain.ChatExchange
import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.domain.agent.AgentStep
import app.notekea.chat.domain.answer.AnswerStep
import app.notekea.chat.domain.language.SpokenLanguage
import app.notekea.chat.domain.loop.ChatLoop
import app.notekea.chat.domain.loop.WorkKind
import app.notekea.chat.domain.plan.ActionProblem
import app.notekea.chat.domain.prompt.AnswerPrompt
import app.notekea.chat.presentation.model.ActionStatus
import app.notekea.chat.presentation.model.AgentMoveLine
import app.notekea.chat.presentation.model.ChatAction
import app.notekea.chat.presentation.model.ChatEffect
import app.notekea.chat.presentation.model.ChatEntry
import app.notekea.chat.presentation.model.ChatStep
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.web.domain.SourceTrust

/**
 * The agent in the chat (4.7), for [ChatViewModel]: every request worked on by the one loop, move after move, each
 * move listed on its entry, and its end shown as the usual card (its plan, confirmed before anything changes) or as
 * what it found.
 */
internal class ChatAgent(
    private val vaultPath: String,
    private val actions: ChatActions,
    private val host: ChatHost,
    private val loop: ChatLoop
) {
    /**
     * The entry worked on by the loop, its end to come; asked of a note or a folder, its [scope] is those notes. The
     * reply of the chat's own that the loop hands back (talk, help, the overview), null when the turn ended here.
     */
    suspend fun takeOver(
        id: Long,
        task: String,
        notes: Collection<NoteEntry>,
        history: List<ChatExchange>,
        openNote: RelativePath?,
        scope: Set<RelativePath>?
    ): ChatReply? {
        host.update(id) { it.copy(step = ChatStep.WORKING) }

        // The model is told the names of the notes it may look in, and the folders, tags and properties of the vault.
        val looked = scope?.let { only ->
            notes.filter {
                it.file.path in only
            }
        } ?: notes

        val context = actions.contextOf(
            request = task,
            notes = notes,
            looked = looked,
            openNote = openNote
        ).copy(
            history = history,
            language = SpokenLanguage.inChat(
                text = task,
                history = history
            )
        )

        // With the memory turned off, a question back offers no Remember.
        val isRemembering = loop.isRemembering()

        val steps =
            loop.runTurn(task, vaultPath, loop.notesFor(notes = notes), context, openNote, scope)

        val replies = mutableListOf<ChatReply>()

        steps
            .collect { step ->
                // The window acts outside the state's update, which may run more than once.
                if (step is AgentStep.Shown) {
                    step.steps.forEach { host.emit(ChatEffect.Show(step = it)) }
                }

                if (step is AgentStep.Replied) {
                    replies += replyOf(
                        kind = step.kind,
                        task = task,
                        scope = scope
                    )
                }

                // A failing model ends the turn as a failing answer does: on the question and on the chat.
                if (step is AgentStep.Failed) {
                    val failed = AnswerStep.Failed(error = ChatError.Model(error = step.error))

                    return@collect host.follow(id, failed)
                }

                val isForgetful = step is AgentStep.Asked && !isRemembering
                val shown = if (isForgetful) step.copy(remembering = null) else step

                host.update(id) { entry -> entry.after(step = shown) }
            }

        return replies.firstOrNull()
    }

    // Talk asked of one note is about it ("what do you think of it?").
    private fun replyOf(
        kind: WorkKind,
        task: String,
        scope: Set<RelativePath>?
    ): ChatReply = when (kind) {
        WorkKind.HELP -> ChatReply.Help(request = task)
        WorkKind.OVERVIEW -> ChatReply.Overview
        else -> ChatReply.Talk(note = scope?.singleOrNull())
    }
}
