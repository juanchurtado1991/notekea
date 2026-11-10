package app.notekea.search.eval

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.embedding.domain.TextEmbedder
import app.notekea.core.notes.data.LuceneVectorIndex
import app.notekea.core.notes.domain.ChunkNoteUseCase
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteFile
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.NoteSections
import app.notekea.core.notes.domain.OutlineFinder
import app.notekea.core.notes.domain.RelativePath
import app.notekea.search.domain.FakeIndexStateStore
import app.notekea.search.domain.IndexVaultUseCase
import app.notekea.search.domain.PlanIndexUseCase
import kotlinx.coroutines.flow.last
import java.io.File
import java.nio.file.Files

/**
 * A sample vault of `smoke-data` indexed for an evaluation, as the app indexes it: its [notes], their texts in
 * [store] and the real [index]. The notes are the ones committed, so local edits to `smoke-data` change no number.
 */
class EvalVault(
    val path: String,
    val notes: List<NoteEntry>,
    val store: FakeNoteContentStore,
    val index: LuceneVectorIndex
) {
    companion object {
        private const val MARKDOWN_SUFFIX = ".md"
        private const val VAULTS_FOLDER = "smoke-data/vaults"

        /** [vault] as committed, indexed with [embedder]. */
        suspend fun of(
            vault: String,
            embedder: TextEmbedder,
            dispatchers: AppDispatchers
        ): EvalVault {
            val store = FakeNoteContentStore()
            val root = "$VAULTS_FOLDER/$vault/"

            val notes = git("ls-files", "-z", "--", root)
                .split('\u0000')
                .filter { it.endsWith(suffix = MARKDOWN_SUFFIX) }
                .map { file ->
                    val path = RelativePath(value = file.removePrefix(prefix = root))
                    val text = git("show", "HEAD:$file")
                    store.texts[path] = text
                    val file = NoteFile(path, text.length.toLong(), modifiedAt = null)

                    NoteEntry(file, NoteMetadata.NONE)
                }

            val index =
                LuceneVectorIndex(Files.createTempDirectory("notekea-eval").toString(), dispatchers)

            val path = File(root).canonicalPath

            IndexVaultUseCase(
                planIndex = PlanIndexUseCase(),
                chunkNote = ChunkNoteUseCase(
                    noteSections = NoteSections(outlineFinder = OutlineFinder())
                ),
                noteContentStore = store,
                textEmbedder = embedder,
                vectorIndex = index,
                indexStateStore = FakeIndexStateStore()
            )(
                vaultPath = path,
                notes = notes.map { it.file }
            ).last()

            return EvalVault(
                path = path,
                notes = notes,
                store = store,
                index = index
            )
        }

        /** The output of `git [arguments]`, run at the repository's root (the tests run in `app`). */
        private fun git(vararg arguments: String): String {
            val process = ProcessBuilder(listOf("git") + arguments)
                .directory(File("..").canonicalFile)
                .redirectErrorStream(false)
                .start()

            val output = process
                .inputStream
                .readBytes()
                .decodeToString()

            check(process.waitFor() == 0) { "git ${arguments.first()} failed" }

            return output
        }
    }
}
