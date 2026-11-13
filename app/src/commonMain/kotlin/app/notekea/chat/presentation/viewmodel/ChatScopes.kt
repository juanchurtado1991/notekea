package app.notekea.chat.presentation.viewmodel

import app.notekea.chat.domain.ChatScope
import app.notekea.chat.presentation.model.ChatState
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath

/** The scope chosen, or the whole vault when there is no open note (or folder) to ask of. */
internal fun ChatState.askedScope(): ChatScope = when {
    scope == ChatScope.NOTE && noteName == null -> ChatScope.VAULT
    scope == ChatScope.FOLDER && folderName == null -> ChatScope.VAULT
    else -> scope
}

/** The name the entry shows for [scope]: the open note's, its folder's, or none for the vault. */
internal fun ChatState.nameOf(scope: ChatScope): String? = when (scope) {
    ChatScope.VAULT -> null
    ChatScope.NOTE -> noteName
    ChatScope.FOLDER -> folderName
}

/** The notes a question of [scope] reads, around [activeNote]; null for the whole vault. */
internal fun notesOf(
    scope: ChatScope,
    notes: Collection<NoteEntry>,
    activeNote: RelativePath?
): Set<RelativePath>? {
    val note = activeNote ?: return null

    return when (scope) {
        ChatScope.VAULT -> null

        ChatScope.NOTE -> setOf(note)

        ChatScope.FOLDER -> {
            val prefix = note
                .parent
                ?.value
                .orEmpty() + "/"

            notes
                .map { it.file.path }
                .filter { it.value.startsWith(prefix = prefix) }
                .toSet()
        }
    }
}
