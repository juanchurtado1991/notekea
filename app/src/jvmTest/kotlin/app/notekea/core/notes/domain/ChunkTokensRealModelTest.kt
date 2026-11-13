package app.notekea.core.notes.domain

import app.notekea.core.notes.domain.index.ChunkNoteUseCase
import app.notekea.core.notes.domain.index.TokenEstimate
import net.ladenthin.llama.LlamaModel
import net.ladenthin.llama.parameters.ModelParameters
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Chunking and the embedding model together: every chunk of every `smoke-data` note fits what the bundled embedding
 * model reads, counted by its own tokenizer, and the estimate the chunks are sized by never counts fewer tokens than
 * it.
 */
class ChunkTokensRealModelTest {
    private val chunkNote =
        ChunkNoteUseCase(noteSections = NoteSections(outlineFinder = OutlineFinder()))

    @Test
    fun `every smoke-data chunk fits the model, never more tokens than estimated`() {
        val parameters = ModelParameters().setModel(
            System.getProperty(MODEL_PROPERTY)
        ).enableEmbedding()

        val overs = LlamaModel(parameters.setCtxSize(CONTEXT_TOKENS)).use { model ->
            notes().flatMap { file ->
                chunkNote(
                    note = NOTE,
                    text = file.readText()
                ).mapNotNull { chunk ->
                    val tokens = model.encode(chunk.text).size

                    "${file.path} ${chunk.index}: $tokens, estimated ${chunk.tokens}".takeIf {
                        tokens > chunk.tokens || tokens > CONTEXT_TOKENS - SPECIAL_TOKENS
                    }
                }
            }
        }

        assertEquals(
            expected = emptyList(),
            actual = overs
        )
    }

    // Audit: without spaces a text is one word and half its characters, while the model reads about one token each.
    @Test
    fun `texts in scripts without spaces are never counted under what the model reads`() {
        val parameters = ModelParameters().setModel(
            System.getProperty(MODEL_PROPERTY)
        ).enableEmbedding()

        val overs = LlamaModel(parameters.setCtxSize(CONTEXT_TOKENS)).use { model ->
            UNSPACED_TEXTS.mapNotNull { text ->
                val tokens = model.encode(text).size

                "$text: $tokens, estimated ${TokenEstimate.of(text = text)}".takeIf {
                    tokens > TokenEstimate.of(text = text)
                }
            }
        }

        assertEquals(
            expected = emptyList(),
            actual = overs
        )
    }

    private fun notes(): List<File> = File(SMOKE_DATA)
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
        const val MODEL_PROPERTY = "notekea.embedding.model"
        const val SMOKE_DATA = "../smoke-data"
        const val SCRATCH = "scratch"
        const val CONTEXT_TOKENS = 512
        const val SPECIAL_TOKENS = 2
        val UNSPACED_TEXTS = listOf(
            "吾輩は猫である。名前はまだ無い。どこで生れたかとんと見当がつかぬ。",
            "カタカナのテキストをコンピュータでプログラムする",
            "今天天气很好，我们一起去公园散步，然后吃晚饭。",
            "ภาษาไทยเขียนติดกันโดยไม่มีการเว้นวรรคระหว่างคำ",
            "한국어문장은띄어쓰기없이도길게쓸수있습니다만보통은띄어씁니다"
        )
    }
}
