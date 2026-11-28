package app.notekea.chat.presentation.viewmodel

import app.notekea.chat.domain.ChatExchange
import app.notekea.chat.domain.answer.AnswerStep
import app.notekea.chat.domain.answer.ConverseUseCase
import app.notekea.chat.domain.answer.DescribeVaultUseCase
import app.notekea.chat.domain.context.WorkContext
import app.notekea.chat.domain.help.AnswerHelpUseCase
import app.notekea.chat.domain.language.SpokenLanguage
import app.notekea.core.notes.domain.NoteEntry
import kotlinx.coroutines.flow.Flow

/**
 * How the chat replies once it knows what was asked, for [ChatViewModel]: talk ([ConverseUseCase]), how to use the
 * app ([AnswerHelpUseCase], with the [shortcuts] of this system), or what the vault holds ([DescribeVaultUseCase]).
 */
internal class ChatReplies(
    private val vaultPath: String,
    private val converse: ConverseUseCase,
    private val answerHelp: AnswerHelpUseCase,
    private val describeVault: DescribeVaultUseCase,
    private val shortcuts: Map<String, String>,
    private val todayIn: (Collection<NoteEntry>) -> String,
    private val working: () -> WorkContext?
) {
    /** The steps of the reply to [message]. */
    fun stepsFor(
        reply: ChatReply,
        message: String,
        history: List<ChatExchange>,
        notes: Collection<NoteEntry>
    ): Flow<AnswerStep> = repliedIn(
        reply = reply,
        message = message,
        history = history,
        notes = notes,
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
            today = todayIn(notes),
            working = working()?.described(),
            notes = notes
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
    }
}
