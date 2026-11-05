package app.notekea.core.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.IndexError
import app.notekea.core.notes.domain.NoteChunk
import app.notekea.core.notes.domain.RankFusion
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.SearchHit
import app.notekea.core.notes.domain.VectorIndex
import co.touchlab.kermit.Logger
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.KnnFloatVectorField
import org.apache.lucene.document.StoredField
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.Term
import org.apache.lucene.index.VectorSimilarityFunction
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.KnnFloatVectorQuery
import org.apache.lucene.search.Query
import org.apache.lucene.search.TermInSetQuery
import org.apache.lucene.search.TermQuery
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.store.LockObtainFailedException
import org.apache.lucene.util.BytesRef
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

/**
 * [VectorIndex] on Lucene, one index per vault in [indexRoot] (the app's data), named by a hash of the vault's path.
 * Each chunk is a document: its note, place and headings stored, its headings and text indexed for BM25 by
 * [NoteAnalyzer], and its vector for nearest-neighbour search (HNSW, dot product of unit vectors). A search takes the
 * best of each as the search asks and merges them ([RankFusion]). Every change opens the index, commits and closes it, so
 * it is on disk when the call returns and another Notekea on the same vault only waits ([IndexError.Busy]).
 */
class LuceneVectorIndex(
    private val indexRoot: String,
    private val dispatchers: AppDispatchers
) : VectorIndex {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private val analyzer = NoteAnalyzer()
    private val mutex = Mutex()

    override suspend fun put(
        vaultPath: String,
        note: RelativePath,
        chunks: List<NoteChunk>,
        vectors: List<FloatArray>
    ): Outcome<Unit, IndexError> = write(vaultPath = vaultPath) { writer ->
        writer.deleteDocuments(Term(NOTE, note.value))
        chunks.zip(other = vectors).forEach { (chunk, vector) -> writer.addDocument(documentOf(chunk, vector)) }
    }

    override suspend fun remove(
        vaultPath: String,
        note: RelativePath
    ): Outcome<Unit, IndexError> = write(vaultPath = vaultPath) { it.deleteDocuments(Term(NOTE, note.value)) }

    override suspend fun clear(
        vaultPath: String
    ): Outcome<Unit, IndexError> = write(vaultPath = vaultPath) { it.deleteAll() }

    override suspend fun nearest(
        vaultPath: String,
        vector: FloatArray,
        limit: Int
    ): Outcome<List<SearchHit>, IndexError> = read(vaultPath = vaultPath) { searcher ->
        val fields = searcher.storedFields()
        searcher.search(KnnFloatVectorQuery(VECTOR, vector, limit), limit).scoreDocs.map {
            hitOf(document = fields.document(it.doc), score = it.score)
        }
    }

    override suspend fun search(
        vaultPath: String,
        text: String,
        vector: FloatArray,
        limit: Int,
        only: Set<RelativePath>?
    ): Outcome<List<SearchHit>, IndexError> = read(vaultPath = vaultPath) { hitsOf(it, text, vector, limit, only) }

    /** [query] on the vault's index as it is on disk; nothing found when it has none yet. */
    private suspend fun read(
        vaultPath: String,
        query: (IndexSearcher) -> List<SearchHit>
    ): Outcome<List<SearchHit>, IndexError> = withContext(context = dispatchers.io) {
        val folder = folderOf(vaultPath = vaultPath)
        if (!Files.exists(folder)) return@withContext Outcome.Success(value = emptyList())
        guarded {
            FSDirectory.open(folder).use { directory ->
                if (DirectoryReader.indexExists(directory)) {
                    DirectoryReader.open(directory).use { reader -> query(IndexSearcher(reader)) }
                } else {
                    emptyList()
                }
            }
        }
    }

    private fun hitsOf(
        searcher: IndexSearcher,
        text: String,
        vector: FloatArray,
        limit: Int,
        only: Set<RelativePath>?
    ): List<SearchHit> {
        val notes = only?.let { paths -> TermInSetQuery(NOTE, paths.map { BytesRef(it.value) }) }
        val byMeaning = searcher.search(KnnFloatVectorQuery(VECTOR, vector, limit, notes), limit).scoreDocs
        val byWords = keywordQuery(text, BooleanClause.Occur.SHOULD, notes)?.let {
            searcher.search(it, limit).scoreDocs
        }
            .orEmpty()
        val withEveryWord = keywordQuery(text, BooleanClause.Occur.MUST, notes)?.let {
            searcher.search(it, limit).scoreDocs
        }
            .orEmpty()
            .mapTo(destination = HashSet()) { it.doc }
        val fields = searcher.storedFields()
        val closeness = byMeaning.associate { it.doc to it.score }
        return RankFusion.merge(rankings = listOf(byMeaning.map { it.doc }, byWords.map { it.doc }))
            .take(n = limit)
            .map { (document, score) ->
                hitOf(document = fields.document(document), score = score).copy(
                    hasWords = document in withEveryWord,
                    closeness = closeness[document] ?: 0f
                )
            }
    }

    /** Any of the first [MAX_QUERY_TERMS] words of the question, as the index reads them; null when it has none. */
    private fun keywordQuery(
        text: String,
        occur: BooleanClause.Occur,
        notes: Query?
    ): Query? {
        val terms = mutableListOf<String>()
        analyzer.tokenStream(TEXT, text).use { stream ->
            val term = stream.addAttribute(CharTermAttribute::class.java)
            stream.reset()
            while (stream.incrementToken()) terms += term.toString()
            stream.end()
        }
        if (terms.isEmpty()) return null
        val query = BooleanQuery.Builder()
        terms.distinct().take(n = MAX_QUERY_TERMS).forEach {
            query.add(TermQuery(Term(TEXT, it)), occur)
        }
        notes?.let { query.add(it, BooleanClause.Occur.FILTER) }
        return query.build()
    }

    private fun documentOf(
        chunk: NoteChunk,
        vector: FloatArray
    ): Document = Document().apply {
        add(StringField(NOTE, chunk.note.value, Field.Store.YES))
        add(StoredField(CHUNK, chunk.index))
        add(StoredField(HEADINGS, chunk.headingPath.joinToString(separator = HEADING_SEPARATOR)))
        add(StoredField(START, chunk.range.first))
        add(StoredField(END, chunk.range.last))
        add(TextField(TEXT, (chunk.headingPath + chunk.text).joinToString(separator = "\n"), Field.Store.NO))
        add(KnnFloatVectorField(VECTOR, vector, VectorSimilarityFunction.DOT_PRODUCT))
    }

    private fun hitOf(
        document: Document,
        score: Float
    ): SearchHit = SearchHit(
        note = RelativePath(value = document.get(NOTE)),
        index = document.getField(CHUNK).numericValue().toInt(),
        headingPath = document.get(HEADINGS).split(HEADING_SEPARATOR).filter { it.isNotEmpty() },
        range = document.getField(START).numericValue().toInt()..document.getField(END).numericValue().toInt(),
        score = score
    )

    private suspend fun write(
        vaultPath: String,
        change: (IndexWriter) -> Unit
    ): Outcome<Unit, IndexError> = withContext(context = dispatchers.io) {
        mutex.withLock {
            guarded<Unit> {
                val folder = Files.createDirectories(folderOf(vaultPath = vaultPath))
                FSDirectory.open(folder).use { directory ->
                    IndexWriter(directory, IndexWriterConfig(analyzer)).use { writer ->
                        change(writer)
                        writer.commit()
                    }
                }
            }
        }
    }

    private fun <T> guarded(
        block: () -> T
    ): Outcome<T, IndexError> = try {
        Outcome.Success(value = block())
    } catch (_: LockObtainFailedException) {
        Outcome.Failure(error = IndexError.Busy)
    } catch (exception: IOException) {
        logger.w(throwable = exception) { UNAVAILABLE_MESSAGE }
        Outcome.Failure(error = IndexError.Unavailable)
    }

    /** The vault's index folder: a hash of its path, so no path is written in the folder's name. */
    private fun folderOf(
        vaultPath: String
    ): Path {
        val hash = MessageDigest.getInstance(SHA_256).digest(vaultPath.toByteArray(charset = Charsets.UTF_8))
        return Path.of(indexRoot, hash.take(n = FOLDER_BYTES).joinToString(separator = "") { "%02x".format(it) })
    }

    companion object {
        private const val NOTE = "note"
        private const val CHUNK = "chunk"
        private const val HEADINGS = "headings"
        private const val START = "start"
        private const val END = "end"
        private const val TEXT = "text"
        private const val VECTOR = "vector"
        private const val HEADING_SEPARATOR = "\u001f"

        // The best chunks of each ranking merged: enough for the related notes and the chat to pick from.
        private const val FOLDER_BYTES = 8

        // Lucene refuses a query of over 1,024 clauses, and a pasted page is no sharper a question for its last words.
        private const val MAX_QUERY_TERMS = 256
        private const val SHA_256 = "SHA-256"
        private const val LOG_TAG = "LuceneVectorIndex"
        private const val UNAVAILABLE_MESSAGE = "The search index is unavailable"
    }
}
