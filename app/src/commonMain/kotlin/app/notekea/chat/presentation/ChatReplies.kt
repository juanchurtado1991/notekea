package app.notekea.chat.presentation

import app.notekea.chat.domain.AgentAnswerUseCase
import app.notekea.chat.domain.AnswerFromFoundUseCase
import app.notekea.chat.domain.AnswerFromNotesUseCase
import app.notekea.chat.domain.AnswerHelpUseCase
import app.notekea.chat.domain.AnswerStep
import app.notekea.chat.domain.ChatExchange
import app.notekea.chat.domain.ConverseUseCase
import app.notekea.chat.domain.DescribeVaultUseCase
import app.notekea.chat.domain.SpokenLanguage
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * How the chat replies once it knows what was asked, for [ChatViewModel]: talk ([ConverseUseCase]), how to use the
 * app ([AnswerHelpUseCase], with the [shortcuts] of this system), what the vault holds ([DescribeVaultUseCase]), or an
 * answer from the notes that, when the first search finds nothing in the whole vault, the agent tries again.
 */
internal class ChatReplies(
    private val vaultPath: String,
    private val answerFromNotes: AnswerFromNotesUseCase,
    private val converse: ConverseUseCase,
    private val agentAnswer: AgentAnswerUseCase,
    private val answerHelp: AnswerHelpUseCase,
    private val describeVault: DescribeVaultUseCase,
    private val answerFromFound: AnswerFromFoundUseCase,
    private val shortcuts: Map<String, String>,
    private val today: () -> String
) {
    /** The steps of the reply to [message]; [only] limits an answer to a note or a folder, [onStep] shows progress. */
    fun stepsFor(
        reply: ChatReply,
        message: String,
        history: List<ChatExchange>,
        notes: Collection<NoteEntry>,
        isDeep: Boolean,
        onStep: (ChatStep) -> Unit,
        only: Set<RelativePath>?
    ): Flow<AnswerStep> = repliedIn(
        reply = reply,
        message = message,
        history = history,
        notes = notes,
        isDeep = isDeep,
        onStep = onStep,
        only = only,
        language = SpokenLanguage.inChat(
            text = message,
            history = history
        )
    )

    private fun repliedIn(
        reply: ChatReply,
        message: String,
        history: List<ChatExchange>,
        notes: Collection<NoteEntry>,
        isDeep: Boolean,
        onStep: (ChatStep) -> Unit,
        only: Set<RelativePath>?,
        language: String?
    ): Flow<AnswerStep> = when (reply) {
        is ChatReply.Talk -> converse(
            message = message,
            history = history,
            vaultPath = vaultPath,
            note = notes.firstOrNull {
                it.file.path == reply.note
            },
            language = language,
            today = today()
        )

        is ChatReply.Question -> answered(
            question = reply.question,
            notes = notes,
            only = only,
            isDeep = isDeep,
            onStep = onStep,
            language = language
        )

        is ChatReply.Help -> answerHelp(
            question = message,
            request = reply.request,
            history = history,
            shortcuts = shortcuts,
            language = language
        )

        ChatReply.Overview -> describeVault(
            question = message,
            notes = notes,
            language = language
        )

        is ChatReply.FromFound -> answerFromFound(
            question = reply.question,
            tables = reply.tables,
            notes = notes,
            language = language
        )

        ChatReply.Acted, ChatReply.Agent -> flow { }
    }

    private fun answered(
        question: String,
        notes: Collection<NoteEntry>,
        isDeep: Boolean,
        onStep: (ChatStep) -> Unit,
        only: Set<RelativePath>?,
        language: String?
    ): Flow<AnswerStep> = flow {
        onStep(ChatStep.SEARCHING)
        var isNotFound = false

        answerFromNotes(
            vaultPath = vaultPath,
            notes = notes,
            question = question,
            only = only,
            drafts = if (isDeep) DEEP_DRAFTS else 1,
            language = language
        ).collect { step ->
            if (step == AnswerStep.NotFound) isNotFound = true else emit(value = step)
        }

        if (!isNotFound) return@flow
        if (only != null) return@flow emit(value = AnswerStep.NotFound)

        onStep(ChatStep.LOOKING_FURTHER)

        emitAll(
            flow = agentAnswer(
                vaultPath = vaultPath,
                notes = notes,
                question = question,
                language = language
            )
        )
    }

    private companion object {
        // Three drafts and a vote (3.6, docs/answer-quality.md).
        const val DEEP_DRAFTS = 3
    }
}
