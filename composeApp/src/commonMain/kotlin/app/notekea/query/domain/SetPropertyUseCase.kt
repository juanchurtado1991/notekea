package app.notekea.query.domain

import app.notekea.core.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.core.notes.domain.FrontmatterPatch
import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.NoteDocument
import app.notekea.core.notes.domain.NoteProperty
import app.notekea.core.notes.domain.NoteRewrite
import app.notekea.core.notes.domain.RelativePath

/**
 * Sets a property of a note from a query's table (`ROADMAP.md` 2.10), adding it when the note has none, with the same
 * patch as the properties view, so the rest of the note stays byte for byte. The note is written only if it does not
 * change meanwhile. Returns what was written, or null if nothing was.
 */
class SetPropertyUseCase(
    private val noteContentStore: NoteContentStore,
    private val applyLinkUpdates: ApplyLinkUpdatesUseCase
) {
    private val patch = FrontmatterPatch()

    suspend operator fun invoke(
        vaultPath: String,
        path: RelativePath,
        property: NoteProperty
    ): NoteRewrite? {
        val original = noteContentStore.read(vaultPath = vaultPath, path = path) ?: return null
        val document = NoteDocument.fromText(text = original)
        val patched = patch.set(header = document.header, key = property.key, value = property.value)
        val header = if (patched != document.header) {
            patched
        } else {
            patch.add(header = document.header, key = property.key, value = property.value)
        }
        val rewrite = NoteRewrite(
            path = path,
            original = original,
            updated = document.copy(header = header).toText(),
            linkCount = 0
        )
        return rewrite.takeIf { applyLinkUpdates(vaultPath, listOf(it)).updated.isNotEmpty() }
    }
}
