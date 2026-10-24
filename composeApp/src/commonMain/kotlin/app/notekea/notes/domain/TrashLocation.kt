package app.notekea.notes.domain

/** Where a deleted note or folder went, so the user is told where to find it. */
enum class TrashLocation {
    SYSTEM_TRASH,
    VAULT_TRASH_FOLDER
}
