package app.notekea.chat.presentation

import app.notekea.chat.domain.ChatScope

/**
 * The chat panel: its [entries] (oldest first), the [question] being written, the [scope] it will be asked of (the
 * open note is [noteName], its folder [folderName]), whether the model and the index can answer, and whether Deep
 * answer and agent mode (4.7) are on, where the model offers them.
 */
data class ChatState(
    val entries: List<ChatEntry> = emptyList(),
    val question: String = "",
    val scope: ChatScope = ChatScope.VAULT,
    val model: ModelStatus = ModelStatus.CHECKING,
    val isSmall: Boolean = false,
    val isDeepOffered: Boolean = false,
    val isDeep: Boolean = false,
    val isAgentOffered: Boolean = false,
    val isAgent: Boolean = false,
    val isIndexReady: Boolean = false,
    val noteName: String? = null,
    val folderName: String? = null
) {
    val isAnswering: Boolean get() = entries.lastOrNull()?.isAnswering == true

    val canSend: Boolean
        get() =
            question.isNotBlank() &&
                !isAnswering &&
                model == ModelStatus.READY &&
                isIndexReady
}
