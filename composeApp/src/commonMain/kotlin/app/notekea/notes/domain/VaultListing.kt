package app.notekea.notes.domain

/**
 * Everything a scan of the vault found: every visible folder (the vault itself excluded) and every Markdown file,
 * without reading any file's content.
 */
data class VaultListing(
    val folders: Set<RelativePath>,
    val files: List<NoteFile>
)
