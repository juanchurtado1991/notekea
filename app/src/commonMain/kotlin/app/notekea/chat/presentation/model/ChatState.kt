package app.notekea.chat.presentation.model

import app.notekea.chat.domain.ChatScope
import app.notekea.chat.domain.answer.DeepQuestion
import app.notekea.chat.domain.context.WorkContext
import app.notekea.chat.presentation.viewmodel.ChatReply
import app.notekea.core.llm.domain.ModelTier
import app.notekea.core.notes.domain.RelativePath

/**
 * The chat panel: its [entries] (oldest first), the [question] being written, the [scope] it will be asked of (the
 * open note is [noteName], its folder [folderName]), the model in use ([tier]) and the [mode] it answers in; where the
 * person is ([working]), shared with the model as chips unless removed ([isPlaceHidden], [isSelectedHidden]).
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
    val folderName: String? = null,
    val working: WorkContext? = null,
    val isPlaceHidden: Boolean = false,
    val isSelectedHidden: Boolean = false,
    // The folders a note the chat creates can go to, the vault's root first.
    val folders: List<RelativePath> = emptyList()
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

    /** What the model is told of where the person is: [working] without the chips removed. */
    val shared: WorkContext?
        get() {
            val context = working ?: return null
            if (isPlaceHidden) return null

            return if (isSelectedHidden) context.copy(selected = null) else context
        }

    val canSend: Boolean
        get() =
            question.isNotBlank() &&
                !isAnswering &&
                model == ModelStatus.READY &&
                isIndexReady
}
