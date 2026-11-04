package app.notekea.notes.domain

/**
 * Decides what the search index must do for a vault's [notes] (`ROADMAP.md` 2.3): a note it does not hold, or holds
 * from another [version], or whose size or date changed (or are unknown) is read; a note it holds that is gone is
 * removed; the rest are left as they are, the ones left out on purpose telling why. Reading does not mean indexing
 * again: the run compares the text's hash first.
 * Notes are read in path order, so a run is predictable and resumes where it stopped.
 */
class PlanIndexUseCase {
    operator fun invoke(
        notes: Collection<NoteFile>,
        indexed: List<IndexedNote>,
        version: String
    ): IndexPlan {
        val held = indexed.associateBy { it.file.path }
        val read = notes.filter { file ->
            val note = held[file.path]
            note == null || note.version != version || !isSameFile(indexed = note.file, current = file)
        }.sortedBy { it.path.value }
        val present = notes.mapTo(destination = HashSet()) { it.path }
        val toRead = read.mapTo(destination = HashSet()) { it.path }
        return IndexPlan(
            read = read,
            remove = held.keys.filterNot { it in present }.sortedBy { it.value },
            unchanged = notes.size - read.size,
            skipped = held.values.filter { it.file.path in present && it.file.path !in toRead }
                .mapNotNull { note -> note.skipReason?.let { SkippedNote(path = note.file.path, reason = it) } }
                .sortedBy { it.path.value }
        )
    }

    /** Whether the file is as it was when indexed: same size and date, both known. */
    private fun isSameFile(
        indexed: NoteFile,
        current: NoteFile
    ): Boolean = current.size != null && current.modifiedAt != null &&
        current.size == indexed.size && current.modifiedAt == indexed.modifiedAt
}
