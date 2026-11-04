package app.notekea.notes.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.EmbeddingError
import app.notekea.core.embedding.domain.TextEmbedder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.cancellation.CancellationException

/**
 * Brings a vault's search index up to date with its [notes] (`ROADMAP.md` 2.3), as [PlanIndexUseCase] decides: each
 * note read is chunked ([ChunkNoteUseCase]), embedded ([TextEmbedder]) and put in the index ([VectorIndex]) only when
 * its text changed (by its [ContentHash]), then marked done ([IndexStateStore]), so a run stopped anywhere (the app closed,
 * the vault switched) resumes with the notes left. Gone notes leave the index. The [Flow] tells the progress after
 * every note, over all the vault's notes (those already indexed count as done); a model or index failure stops the run, the rest waiting for the next one,
 * while an unexpected failure on one note only skips it.
 */
class IndexVaultUseCase(
    private val planIndex: PlanIndexUseCase,
    private val chunkNote: ChunkNoteUseCase,
    private val noteContentStore: NoteContentStore,
    private val textEmbedder: TextEmbedder,
    private val vectorIndex: VectorIndex,
    private val indexStateStore: IndexStateStore
) {
    /** The index version: a change of model or of chunking indexes every note again. */
    val version: String get() = "${textEmbedder.modelId}/chunks-$CHUNKING_VERSION"

    operator fun invoke(
        vaultPath: String,
        notes: Collection<NoteFile>
    ): Flow<IndexProgress> = flow {
        val held = indexStateStore.read(vaultPath = vaultPath)
        val plan = planIndex(notes = notes, indexed = held, version = version)
        plan.remove.forEach { path ->
            if (vectorIndex.remove(vaultPath = vaultPath, note = path) is Outcome.Failure) {
                return@flow emit(value = IndexProgress(total = notes.size, error = IndexRunError.Index))
            }
            indexStateStore.delete(vaultPath = vaultPath, path = path)
        }
        val hashes = held.associate { it.file.path to it.contentHash.takeIf { _ -> it.version == version } }
        // Counted over the whole vault: a run that resumes shows how far the index is, not a new count from zero.
        var progress = IndexProgress(total = plan.read.size + plan.unchanged, done = plan.unchanged)
        emit(value = progress)
        for (file in plan.read) {
            val step = indexGuarded(vaultPath = vaultPath, file = file, heldHash = hashes[file.path])
            progress = progress.after(step = step, path = file.path)
            emit(value = progress)
            if (progress.error != null) return@flow
        }
        emit(value = progress.copy(isFinished = true))
    }

    /** [indexNote], where what nobody expected from one note skips it instead of ending the run for every other. */
    private suspend fun indexGuarded(
        vaultPath: String,
        file: NoteFile,
        heldHash: String?
    ): NoteStep = try {
        indexNote(vaultPath = vaultPath, file = file, heldHash = heldHash)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (exception: Exception) {
        NoteStep.Skipped(reason = SkipReason.FAILED, cause = exception)
    }

    /** What indexing [file] did: indexed it again, found its text as indexed ([heldHash]), skipped it, or failed. */
    private suspend fun indexNote(
        vaultPath: String,
        file: NoteFile,
        heldHash: String?
    ): NoteStep {
        if ((file.size ?: 0) > MAX_NOTE_BYTES) return NoteStep.Skipped(reason = SkipReason.TOO_LARGE)
        val text = noteContentStore.read(vaultPath = vaultPath, path = file.path)
            ?: return NoteStep.Skipped(reason = SkipReason.UNREADABLE)
        val hash = ContentHash.of(text = text)
        val isUnchanged = heldHash == hash
        val step = if (isUnchanged) NoteStep.Unchanged else embedAndPut(vaultPath = vaultPath, file = file, text = text)
        if (step is NoteStep.Indexed || step is NoteStep.Unchanged) {
            indexStateStore.save(
                vaultPath = vaultPath,
                note = IndexedNote(file = file, contentHash = hash, version = version)
            )
        }
        return step
    }

    private suspend fun embedAndPut(
        vaultPath: String,
        file: NoteFile,
        text: String
    ): NoteStep {
        val chunks = chunkNote(note = file.path, text = text)
        val texts = chunks.map { embeddingText(title = file.path.nameWithoutExtension, chunk = it) }
        val vectors = when (val embedded = textEmbedder.embed(texts = texts)) {
            is Outcome.Success -> embedded.value
            is Outcome.Failure -> return failedEmbedding(error = embedded.error)
        }
        return when (vectorIndex.put(vaultPath = vaultPath, note = file.path, chunks = chunks, vectors = vectors)) {
            is Outcome.Success -> NoteStep.Indexed
            is Outcome.Failure -> NoteStep.Failed(error = IndexRunError.Index)
        }
    }

    /** The text the model embeds for [chunk]: the note's title and headings above it give it its subject. */
    private fun embeddingText(
        title: String,
        chunk: NoteChunk
    ): String = (listOf(title) + chunk.headingPath).joinToString(separator = HEADING_JOIN) + "\n" + chunk.text

    private fun failedEmbedding(
        error: EmbeddingError
    ): NoteStep = when (error) {
        is EmbeddingError.TextTooLong -> NoteStep.Skipped(reason = SkipReason.TOO_LONG)
        else -> NoteStep.Failed(error = IndexRunError.Model)
    }

    private fun IndexProgress.after(
        step: NoteStep,
        path: RelativePath
    ): IndexProgress = when (step) {
        NoteStep.Indexed -> copy(done = done + 1, indexed = indexed + 1)

        NoteStep.Unchanged -> copy(done = done + 1)

        is NoteStep.Skipped -> copy(
            done = done + 1,
            skipped = skipped + SkippedNote(path = path, reason = step.reason, cause = step.cause)
        )

        is NoteStep.Failed -> copy(error = step.error)
    }

    /** What one note's turn did. */
    private sealed interface NoteStep {
        data object Indexed : NoteStep

        data object Unchanged : NoteStep

        data class Skipped(
            val reason: SkipReason,
            val cause: Throwable? = null
        ) : NoteStep

        data class Failed(val error: IndexRunError) : NoteStep
    }

    companion object {
        /** A note larger than this (5 MB) is left out: an export or a log, not a note to search. */
        const val MAX_NOTE_BYTES = 5L * 1024 * 1024

        private const val HEADING_JOIN = " › "

        // Raise it when ChunkNoteUseCase cuts notes differently, so every note is chunked again.
        private const val CHUNKING_VERSION = 1
    }
}
