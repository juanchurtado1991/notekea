package app.notekea.core.documents.data

import app.notekea.core.documents.domain.DocumentError
import app.notekea.core.documents.domain.DocumentReader
import app.notekea.core.documents.domain.DocumentText
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The vault's text for the index and the chat: notes as stored, documents as read out of them, never written. */
class DocumentAwareContentStoreTest {
    private val notes = FakeNoteContentStore()

    private val documents = object : DocumentReader {
        override suspend fun read(
            vaultPath: String,
            path: RelativePath
        ): Outcome<DocumentText, DocumentError> = if (path.name == "Guide.pdf") {
            Outcome.Success(value = DocumentText(text = "## Page 1\n\nTrams climb.\n"))
        } else {
            Outcome.Failure(error = DocumentError.NoText)
        }
    }

    private val store = DocumentAwareContentStore(
        notes = notes,
        documents = documents
    )

    @Test
    fun `a note reads as stored, a document as its text, and a document is never written`() =
        runTest {
            notes.texts[RelativePath(value = "Plan.md")] = "# Plan\n"

            val written = store.write(
                vaultPath = VAULT,
                path = RelativePath(value = "Guide.pdf"),
                content = "x"
            )

            assertEquals(
                expected = listOf<Any?>("# Plan\n", "## Page 1\n\nTrams climb.\n", null, true),
                actual = listOf(
                    store.read(
                        vaultPath = VAULT,
                        path = RelativePath(value = "Plan.md")
                    ),
                    store.read(
                        vaultPath = VAULT,
                        path = RelativePath(value = "Guide.pdf")
                    ),
                    store.read(
                        vaultPath = VAULT,
                        path = RelativePath(value = "Scan.pdf")
                    ),
                    written is Outcome.Failure
                )
            )
        }

    private companion object {
        const val VAULT = "/vaults/Notes"
    }
}
