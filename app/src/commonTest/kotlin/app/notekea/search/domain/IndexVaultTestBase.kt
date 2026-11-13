package app.notekea.search.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.EmbeddingError
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import app.notekea.core.notes.domain.NoteFile
import app.notekea.core.notes.domain.NoteSections
import app.notekea.core.notes.domain.OutlineFinder
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.index.ChunkNoteUseCase
import app.notekea.core.notes.domain.index.IndexError
import app.notekea.core.notes.domain.index.IndexRunError
import app.notekea.core.notes.domain.index.NoteChunk
import app.notekea.core.notes.domain.index.VectorIndex
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/** A search index over in-memory stores, and the notes its tests write. */
abstract class IndexVaultTestBase {
    protected val contents = FakeNoteContentStore()
    protected val embedder = FakeTextEmbedder()
    protected val index = FakeVectorIndex()
    protected val states = FakeIndexStateStore()
    protected val shortEmbedder = FakeTextEmbedder(maxTokens = SHORT_LIMIT)
    protected val indexVault = indexVault(embedder = embedder)

    protected fun indexVault(
        embedder: FakeTextEmbedder,
        vectorIndex: VectorIndex = index
    ) = IndexVaultUseCase(
        planIndex = PlanIndexUseCase(),
        chunkNote = ChunkNoteUseCase(noteSections = NoteSections(outlineFinder = OutlineFinder())),
        noteContentStore = contents,
        textEmbedder = embedder,
        vectorIndex = vectorIndex,
        indexStateStore = states
    )

    /** Writes the notes and gives their files, of [size] and dated [DATE]. */
    protected fun write(
        vararg notes: Pair<RelativePath, String>,
        size: Long = 1
    ): List<NoteFile> = notes.map { (path, text) ->
        contents.texts[path] = text

        NoteFile(
            path = path,
            size = size,
            modifiedAt = DATE
        )
    }

    /** [file] saved again unchanged: a new date, the same text. */
    protected fun touched(file: NoteFile) = file.copy(
        modifiedAt = Instant.parse(input = "2026-11-03T11:00:00Z")
    )

    protected companion object {
        const val VAULT = "/home/ana/Notes"

        // Words a text may have in the test of a chunk longer than the model reads.
        const val SHORT_LIMIT = 10
        val DATE = Instant.parse(input = "2026-11-03T10:00:00Z")
        val AREPAS = RelativePath(value = "Recipes/Arepas.md")
        val DUNE = RelativePath(value = "Books/Dune.md")
        val LISBON = RelativePath(value = "Trips/Lisbon.md")
        val LONG = RelativePath(value = "Long.md")
        val HUGE = RelativePath(value = "Huge export.md")
        val LOCKED = RelativePath(value = "Locked.md")
    }
}
