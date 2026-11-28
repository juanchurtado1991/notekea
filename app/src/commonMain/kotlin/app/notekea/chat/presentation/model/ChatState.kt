package app.notekea.chat.presentation.model

import app.notekea.chat.domain.ChatScope
import app.notekea.chat.domain.context.WorkContext
import app.notekea.chat.domain.plan.NewNotePlace
import app.notekea.core.llm.domain.ModelTier
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath

/**
 * The chat panel: its [entries] (oldest first), the [question] being written, the [scope] it will be asked of (the
 * open note is [noteName], its folder [folderName]) and the model in use ([tier]); where the person is ([working]),
 * shared with the model as chips unless removed ([isPlaceHidden], [isSelectedHidden]).
 */
data class ChatState(
    val entries: List<ChatEntry> = emptyList(),
    val question: String = "",
    val scope: ChatScope = ChatScope.VAULT,
    val model: ModelStatus = ModelStatus.CHECKING,
    val isSmall: Boolean = false,
    val tier: ModelTier? = null,
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
    /** The state for a vault that holds [notes]: the folders a new note can go to. */
    fun holding(notes: Collection<NoteEntry>): ChatState =
        copy(folders = NewNotePlace.foldersOf(notes = notes))

    val isAnswering: Boolean get() = entries.lastOrNull()?.isAnswering == true

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
