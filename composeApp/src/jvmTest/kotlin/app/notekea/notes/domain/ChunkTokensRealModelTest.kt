package app.notekea.notes.domain

import de.kherud.llama.LlamaModel
import de.kherud.llama.ModelParameters
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `ROADMAP.md` 2.1 and 2.2 together: every chunk of every `smoke-data` note fits what the bundled embedding model
 * reads, counted by its own tokenizer, and the estimate the chunks are sized by never counts fewer tokens than it.
 */
class ChunkTokensRealModelTest {
    private val chunkNote = ChunkNoteUseCase(noteSections = NoteSections(outlineFinder = OutlineFinder()))

    @Test
    fun `every smoke-data chunk fits the model, never more tokens than estimated`() {
        val parameters = ModelParameters().setModel(System.getProperty(MODEL_PROPERTY)).enableEmbedding()
        val overs = LlamaModel(parameters.setCtxSize(CONTEXT_TOKENS)).use { model ->
            notes().flatMap { file ->
                chunkNote(note = NOTE, text = file.readText()).mapNotNull { chunk ->
                    val tokens = model.encode(chunk.text).size
                    "${file.path} ${chunk.index}: $tokens, estimated ${chunk.tokens}".takeIf {
                        tokens > chunk.tokens || tokens > CONTEXT_TOKENS - SPECIAL_TOKENS
                    }
                }
            }
        }

        assertEquals(expected = emptyList(), actual = overs)
    }

    private fun notes(): List<File> = File(SMOKE_DATA).walkTopDown()
        .filter { it.isFile && it.name.endsWith(suffix = RelativePath.MARKDOWN_EXTENSION) }
        .filterNot { file -> file.path.split(File.separator).any { it.startsWith(prefix = SCRATCH) } }
        .toList()

    private companion object {
        val NOTE = RelativePath(value = "Note.md")
        const val MODEL_PROPERTY = "notekea.embedding.model"
        const val SMOKE_DATA = "../smoke-data"
        const val SCRATCH = "scratch"
        const val CONTEXT_TOKENS = 512
        const val SPECIAL_TOKENS = 2
    }
}
