package app.notekea.query.domain

import app.notekea.core.domain.StringPatterns
import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.NoteDocument
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.frontmatter.FrontmatterBlocks
import app.notekea.core.notes.domain.frontmatter.FrontmatterPatch
import app.notekea.core.notes.domain.frontmatter.FrontmatterReader
import app.notekea.core.notes.domain.frontmatter.NoteProperty
import app.notekea.core.notes.domain.frontmatter.PropertyValue
import app.notekea.core.notes.domain.links.ApplyLinkUpdatesUseCase
import app.notekea.core.notes.domain.links.NoteRewrite

/**
 * Sets a property of a note from a query's table, adding it when the note has none, with the same
 * patch as the properties view, so the rest of the note stays byte for byte. The note is written only if it does not
 * change meanwhile. Returns what was written, or null if nothing was.
 */
class SetPropertyUseCase(
    private val noteContentStore: NoteContentStore,
    private val applyLinkUpdates: ApplyLinkUpdatesUseCase
) {
    private val patch = FrontmatterPatch()

    /**
     * [tag] added to the tags the note writes now, read from its text and not from an index that may be behind it:
     * two tags added to one note in a plan lost the first (the bug hunt of 2026-11-17). Null when it has the tag.
     */
    suspend fun withTag(
        vaultPath: String,
        path: RelativePath,
        tag: String
    ): NoteRewrite? {
        val original = noteContentStore.read(
            vaultPath = vaultPath,
            path = path
        ) ?: return null

        val block = FrontmatterBlocks
            .of(header = NoteDocument.fromText(text = original).header)
            ?.blockOf(key = TAGS)

        val held = when (val value = block?.let { FrontmatterReader().valueOf(block = it) }) {
            is PropertyValue.Items -> value.items
            is PropertyValue.Text -> listOf(value.text)
            else -> emptyList()
        }.filter { it.isNotBlank() }

        if (held.any {
                it.equals(
                    other = tag,
                    ignoreCase = true
                )
            }
        ) {
            return null
        }

        return invoke(
            vaultPath = vaultPath,
            path = path,
            property = NoteProperty(
                key = block?.key ?: TAGS,
                value = PropertyValue.Items(items = held + tag)
            )
        )
    }

    suspend operator fun invoke(
        vaultPath: String,
        path: RelativePath,
        property: NoteProperty
    ): NoteRewrite? {
        val original = noteContentStore.read(
            vaultPath = vaultPath,
            path = path
        ) ?: return null

        val document = NoteDocument.fromText(text = original)

        val isKnown =
            FrontmatterBlocks.of(header = document.header)?.blockOf(key = property.key) != null

        val header = if (isKnown) {
            patch.set(
                header = document.header,
                key = property.key,
                value = property.value
            )
        } else {
            patch.add(
                document.header,
                property.key,
                property.value,
                lineBreakOf(document = document)
            )
        }

        val rewrite = NoteRewrite(
            path = path,
            original = original,
            updated = document.copy(header = header).toText(),
            linkCount = 0
        )

        return rewrite.takeIf { applyLinkUpdates(vaultPath, listOf(it)).updated.isNotEmpty() }
    }

    private fun lineBreakOf(document: NoteDocument): String =
        if (document.usesCrLf) StringPatterns.WINDOWS_NEW_LINE else StringPatterns.NEW_LINE

    private companion object {
        const val TAGS = "tags"
    }
}
