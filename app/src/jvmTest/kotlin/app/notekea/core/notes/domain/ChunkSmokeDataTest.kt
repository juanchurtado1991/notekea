package app.notekea.core.notes.domain

import app.notekea.core.notes.domain.index.ChunkNoteUseCase
import app.notekea.core.notes.domain.index.ChunkSize
import app.notekea.core.notes.domain.index.NoteChunk
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.measureTime

/**
 * Chunking on every note of `smoke-data` (the Sample Vault, the Ghost Serializer repository as notes and the
 * rest): each chunk is exactly its part of the body and at most the maximum size, the chunks keep every word of the
 * body in order, and their headings are the outline's. Chunking the Ghost Serializer vault stays quick.
 */
class ChunkSmokeDataTest {
    private val sections = NoteSections(outlineFinder = OutlineFinder())
    private val chunkNote = ChunkNoteUseCase(noteSections = sections)

    @Test
    fun `every smoke-data note is chunked into exact parts of its body that keep all its text and its headings`() {
        val problems = notes(folder = SMOKE_DATA).flatMap { file ->
            val body = NoteDocument.fromText(text = file.readText()).body

            problemsOf(
                body = body,
                chunks = chunkNote(
                    note = NOTE,
                    text = file.readText()
                )
            ).map { "${file.path}: $it" }
        }

        assertEquals(
            expected = emptyList(),
            actual = problems
        )
    }

    @Test
    fun `the Ghost Serializer vault is chunked in well under a second`() {
        val notes = notes(folder = GHOST_VAULT).map { it.readText() }

        notes.forEach {
            chunkNote(
                note = NOTE,
                text = it
            )
        }

        // The best of a few runs: another process on a busy machine is not the chunker being slow.
        val time = List(size = TIMED_RUNS) {
            measureTime {
                notes.forEach {
                    chunkNote(
                        note = NOTE,
                        text = it
                    )
                }
            }
        }.min()

        assertTrue(
            actual = time.inWholeMilliseconds < BUDGET_MILLIS,
            message = "${notes.size} notes took $time"
        )
    }

    private fun problemsOf(
        body: String,
        chunks: List<NoteChunk>
    ): List<String> = chunks.mapNotNull { chunk ->
        val hasOutlineHeadings = hasOutlineHeadings(
            body = body,
            chunk = chunk
        )

        when {
            body.substring(range = chunk.range) != chunk.text -> "chunk ${chunk.index} is not its range"
            chunk.tokens > ChunkSize.MAX -> "chunk ${chunk.index} has ${chunk.tokens} tokens"
            !hasOutlineHeadings -> "chunk ${chunk.index} has other headings"
            else -> null
        }
    } + outOfOrder(chunks = chunks) + lostText(
        body = body,
        chunks = chunks
    )

    /** Whether [chunk]'s headings are those of the outline's section it starts in, or the first of them. */
    private fun hasOutlineHeadings(
        body: String,
        chunk: NoteChunk
    ): Boolean = sections.of(body = body).any { section ->
        chunk.range.first in section.range &&
            section.headingPath.take(n = chunk.headingPath.size) == chunk.headingPath
    }

    private fun outOfOrder(chunks: List<NoteChunk>): List<String> = chunks
        .zipWithNext()
        .filter { (first, second) ->
            second.range.first < first.range.first || second.range.last <= first.range.last
        }
        .map { (_, second) -> "chunk ${second.index} is out of order" }

    private fun lostText(
        body: String,
        chunks: List<NoteChunk>
    ): List<String> = body
        .indices
        .firstOrNull { index ->
            !body[index].isWhitespace() && chunks.none { index in it.range }
        }
        ?.let { listOf("the text at $it is in no chunk") }
        .orEmpty()

    private fun notes(folder: String): List<File> = File(folder)
        .walkTopDown()
        .filter {
            it.isFile && it.name.endsWith(suffix = RelativePath.MARKDOWN_EXTENSION)
        }
        .filterNot { file ->
            file
                .path
                .split(File.separator)
                .any { it.startsWith(prefix = SCRATCH) }
        }
        .toList()

    private companion object {
        val NOTE = RelativePath(value = "Note.md")
        const val SMOKE_DATA = "../smoke-data"
        const val GHOST_VAULT = "../smoke-data/vaults/Ghost Serializer"
        const val SCRATCH = "scratch"

        // Generous for the slowest CI runner: it takes a few tens of milliseconds on a laptop.
        private const val TIMED_RUNS = 3
        const val BUDGET_MILLIS = 1_000
    }
}
