package app.notekea.search.eval

import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.data.LlamaTextEmbedder
import app.notekea.core.notes.data.LuceneVectorIndex
import app.notekea.core.notes.domain.ChunkNoteUseCase
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.MarkdownLineStripper
import app.notekea.core.notes.domain.NaturalNameComparator
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteFile
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.NoteSections
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.OutlineFinder
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.TextFolder
import app.notekea.search.domain.FakeIndexStateStore
import app.notekea.search.domain.IndexVaultUseCase
import app.notekea.search.domain.PlanIndexUseCase
import app.notekea.search.domain.SearchNoteContentUseCase
import app.notekea.search.domain.SearchPassagesUseCase
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Recall@5 of hybrid search on the evaluation set (`ROADMAP.md` 2.8), on the bundled model and the real index, as
 * [SearchPassagesUseCase] answers a person: a question is answered when one of its notes is among the first five
 * results. The report, with the questions missed, is written to [REPORT]. Below the agreed baseline the test fails, so a
 * change to chunking, models or ranking is measured.
 */
class RetrievalEvalRealModelTest {
    private val dispatchers = JvmAppDispatchers()
    private val embedder = LlamaTextEmbedder(
        modelPath = System.getProperty(MODEL_PROPERTY),
        nativeFolder = Files.createTempDirectory("notekea-natives").toString(),
        dispatchers = dispatchers
    )

    @AfterTest
    fun closeEmbedder() = embedder.close()

    @Test
    fun `search answers the evaluation set at or above the baseline`() = runBlocking {
        val questions = RetrievalEvalSet.read().questions
        val measured = questions.groupBy { it.vault }.map { (vault, asked) -> measure(vault = vault, asked = asked) }
        File(REPORT).run {
            parentFile.mkdirs()
            writeText(text = measured.joinToString(separator = "\n\n") { it.report() } + "\n")
        }
        println(File(REPORT).readText())
        measured.forEach { result ->
            val baseline = BASELINES.getValue(result.vault)
            assertTrue(actual = result.recall >= baseline, message = "${result.vault}: ${result.recall} < $baseline")
        }
    }

    private suspend fun measure(
        vault: String,
        asked: List<RetrievalQuestion>
    ): RetrievalMeasure {
        val folder = RetrievalEvalSet.folderOf(vault = vault)
        val store = FakeNoteContentStore()
        val notes = folder.walkTopDown().filter { it.isFile && it.extension == MARKDOWN }.map { file ->
            val path = RelativePath(value = file.relativeTo(base = folder).invariantSeparatorsPath)
            store.texts[path] = file.readText()
            NoteEntry(NoteFile(path, file.length(), modifiedAt = null), NoteMetadata.NONE)
        }.toList()
        val index = LuceneVectorIndex(Files.createTempDirectory("notekea-eval").toString(), dispatchers)
        IndexVaultUseCase(
            planIndex = PlanIndexUseCase(),
            chunkNote = ChunkNoteUseCase(noteSections = NoteSections(outlineFinder = OutlineFinder())),
            noteContentStore = store,
            textEmbedder = embedder,
            vectorIndex = index,
            indexStateStore = FakeIndexStateStore()
        )(vaultPath = folder.path, notes = notes.map { it.file }).last()
        val textFolder = TextFolder()
        val cache = NoteTextCache(contentStore = store, textFolder = textFolder)
        val stripper = MarkdownLineStripper()
        val search = SearchPassagesUseCase(
            textEmbedder = embedder,
            vectorIndex = index,
            noteTextCache = cache,
            textFolder = textFolder,
            markdownLineStripper = stripper,
            searchNoteContent = SearchNoteContentUseCase(
                noteTextCache = cache,
                textFolder = textFolder,
                naturalNameComparator = NaturalNameComparator(),
                markdownLineStripper = stripper,
                dispatchers = dispatchers
            )
        )
        val missed = asked.filterNot { question ->
            val found = (search(folder.path, notes, question.question) as Outcome.Success).value
            found.take(n = RECALL_AT).any { it.note.file.path.value in question.notes }
        }
        return RetrievalMeasure(vault = vault, asked = asked.size, missed = missed)
    }

    private companion object {
        const val MARKDOWN = "md"
        const val MODEL_PROPERTY = "notekea.embedding.model"
        const val RECALL_AT = 5
        const val REPORT = "build/reports/retrieval-eval.txt"
        val BASELINES = mapOf(RetrievalEvalSet.SAMPLE_VAULT to 0.92, RetrievalEvalSet.GHOST_VAULT to 0.86)
    }
}
