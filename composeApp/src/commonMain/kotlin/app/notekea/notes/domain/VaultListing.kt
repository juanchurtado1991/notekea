package app.notekea.notes.domain

/**
 * Everything a scan of the vault found: every visible folder (the vault itself excluded), every Markdown file and
 * every other file ([attachments]: images, PDFs…), without reading any file's content.
 */
data class VaultListing(
    val folders: Set<RelativePath>,
    val files: List<NoteFile>,
    val attachments: List<NoteFile> = emptyList()
)
