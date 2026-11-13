package app.notekea.chat.presentation.model

import app.notekea.chat.domain.ChatScope
import app.notekea.chat.domain.answer.DeepQuestion
import app.notekea.chat.presentation.viewmodel.ChatReply
import app.notekea.core.llm.domain.ModelTier

/**
 * The chat panel: its [entries] (oldest first), the [question] being written, the [scope] it will be asked of (the
 * open note is [noteName], its folder [folderName]), the model in use ([tier]) and the [mode] it answers in.
 */
data class ChatState(
    val entries: List<ChatEntry> = emptyList(),
    val question: String = "",
    val scope: ChatScope = ChatScope.VAULT,
    val model: ModelStatus = ModelStatus.CHECKING,
    val isSmall: Boolean = false,
    val tier: ModelTier? = null,
    val isDeepOffered: Boolean = false,
    val isAgentOffered: Boolean = false,
    val mode: ChatMode = ChatMode.AUTO,
    val contextUsage: Float = 0f,
    val isCompacting: Boolean = false,
    val isIndexReady: Boolean = false,
    val noteName: String? = null,
    val folderName: String? = null
) {
    val isAnswering: Boolean get() = entries.lastOrNull()?.isAnswering == true

    /** The modes the model in use offers: Auto and Fast always, Deep thinking and the agent where they help. */
    val modes: List<ChatMode>
        get() = listOfNotNull(
            ChatMode.AUTO,
            ChatMode.FAST,
            ChatMode.DEEP.takeIf { isDeepOffered },
            ChatMode.AGENT.takeIf { isAgentOffered }
        )

    /** Whether [reply] is answered with Deep thinking: chosen, or picked by Auto for a question that asks to reason. */
    internal fun isDeepFor(reply: ChatReply): Boolean = when (mode) {
        ChatMode.DEEP -> isDeepOffered

        ChatMode.AUTO ->
            isDeepOffered &&
                reply is ChatReply.Question &&
                DeepQuestion.isAsked(reply.question)

        else -> false
    }

    val canSend: Boolean
        get() =
            question.isNotBlank() &&
                !isAnswering &&
                model == ModelStatus.READY &&
                isIndexReady
}
