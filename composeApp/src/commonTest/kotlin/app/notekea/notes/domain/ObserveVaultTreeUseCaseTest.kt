package app.notekea.notes.domain

import app.cash.turbine.test
import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.NoteFixtures.VAULT
import app.notekea.notes.domain.NoteFixtures.entry
import app.notekea.notes.domain.NoteFixtures.file
import app.notekea.notes.domain.NoteFixtures.tags
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveVaultTreeUseCaseTest {
    private val scanner = FakeNoteScanner()
    private val reader = FakeNoteMetadataReader()
    private val watcher = FakeFileWatcher()
    private val cache = FakeNoteMetadataCache()
    private val observeVaultTree = ObserveVaultTreeUseCase(
        noteScanner = scanner,
        noteMetadataReader = reader,
        fileWatcher = watcher,
        noteMetadataCache = cache
    )

    @Test
    fun `the first tree has every note with its frontmatter`() = runTest {
        scanner.addNote(file = file(path = AREPAS))
        scanner.folders += RelativePath(value = RECIPES)
        reader.metadata[RelativePath(value = AREPAS)] = tags("recipe")

        observeVaultTree(vaultPath = VAULT).test {
            assertEquals(
                expected = success(
                    tree = VaultTree(
                        folders = setOf(RelativePath(value = RECIPES)),
                        notes = mapOf(RelativePath(value = AREPAS) to entry(path = AREPAS, metadata = tags("recipe")))
                    )
                ),
                actual = awaitItem()
            )
        }
    }

    @Test
    fun `a note that cannot be read is listed without metadata`() = runTest {
        scanner.addNote(file = file(path = AREPAS))
        reader.metadata[RelativePath(value = AREPAS)] = null

        observeVaultTree(vaultPath = VAULT).test {
            assertEquals(
                expected = NoteMetadata.NONE,
                actual = (awaitItem() as Outcome.Success)
                    .value.notes
                    .getValue(RelativePath(value = AREPAS))
                    .metadata
            )
        }
    }

    @Test
    fun `a failing scan reports the error and ends`() = runTest {
        scanner.failure = NotesError.VaultMissing(path = VAULT)

        observeVaultTree(vaultPath = VAULT).test {
            assertEquals(
                expected = Outcome.Failure(error = NotesError.VaultMissing(path = VAULT)),
                actual = awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun `changes that arrive together are applied in one update`() = runTest {
        scanner.addNote(file = file(path = AREPAS))
        scanner.addNote(file = file(path = LISBON))

        observeVaultTree(vaultPath = VAULT).test {
            awaitItem()
            scanner.files.remove(RelativePath(value = LISBON))
            scanner.addNote(file = file(path = AREPAS, modifiedOffsetMillis = 1L))
            scanner.addNote(file = file(path = NEW_NOTE))
            watcher.emit(change = VaultChange.Deleted(path = RelativePath(value = LISBON)))
            watcher.emit(change = VaultChange.Modified(path = RelativePath(value = AREPAS)))
            watcher.emit(change = VaultChange.Created(path = RelativePath(value = NEW_NOTE), isFolder = false))

            assertEquals(
                expected = setOf(RelativePath(value = AREPAS), RelativePath(value = NEW_NOTE)),
                actual = (awaitItem() as Outcome.Success).value.notes.keys
            )
            assertEquals(
                expected = file(path = AREPAS, modifiedOffsetMillis = 1L),
                actual = scanner.files.getValue(RelativePath(value = AREPAS))
            )
            expectNoEvents()
        }
    }

    @Test
    fun `a deleted folder takes its notes with it`() = runTest {
        scanner.addNote(file = file(path = AREPAS))
        scanner.folders += RelativePath(value = RECIPES)

        observeVaultTree(vaultPath = VAULT).test {
            awaitItem()
            scanner.files.clear()
            watcher.emit(change = VaultChange.Deleted(path = RelativePath(value = RECIPES)))

            assertEquals(
                expected = success(tree = VaultTree.EMPTY),
                actual = awaitItem()
            )
        }
    }

    @Test
    fun `a new folder or lost events rescan, reading only notes that changed`() = runTest {
        scanner.addNote(file = file(path = AREPAS))
        scanner.addNote(file = file(path = LISBON))

        observeVaultTree(vaultPath = VAULT).test {
            awaitItem()
            reader.reads.clear()
            scanner.addNote(file = file(path = LISBON, modifiedOffsetMillis = 1L))
            scanner.addNote(file = file(path = "Moved in/Packing checklist.md"))
            scanner.folders += RelativePath(value = "Moved in")
            watcher.emit(change = VaultChange.Created(path = RelativePath(value = "Moved in"), isFolder = true))
            awaitItem()
            watcher.emit(change = VaultChange.Overflow)
            awaitItem()

            assertEquals(
                expected = listOf(RelativePath(value = "Moved in/Packing checklist.md"), RelativePath(value = LISBON)),
                actual = reader.reads.sortedBy { it.value }
            )
            assertEquals(
                expected = 3,
                actual = scanner.scanCount
            )
        }
    }

    @Test
    fun `a note without size or date is always read again on a rescan`() = runTest {
        scanner.addNote(file = file(path = AREPAS, size = null))

        observeVaultTree(vaultPath = VAULT).test {
            awaitItem()
            watcher.emit(change = VaultChange.Overflow)
            awaitItem()

            assertEquals(
                expected = listOf(RelativePath(value = AREPAS), RelativePath(value = AREPAS)),
                actual = reader.reads
            )
        }
    }

    @Test
    fun `a rescan that fails reports the error and ends`() = runTest {
        scanner.addNote(file = file(path = AREPAS))

        observeVaultTree(vaultPath = VAULT).test {
            awaitItem()
            scanner.failure = NotesError.PermissionDenied(path = VAULT)
            watcher.emit(change = VaultChange.Overflow)

            assertEquals(
                expected = Outcome.Failure(error = NotesError.PermissionDenied(path = VAULT)),
                actual = awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun `the cached tree shows first and only notes that changed are read again`() = runTest {
        cache.entries[RelativePath(value = AREPAS)] = entry(path = AREPAS, metadata = tags("recipe"))
        cache.entries[RelativePath(value = LISBON)] = entry(path = LISBON)
        scanner.addNote(file = file(path = AREPAS))
        scanner.addNote(file = file(path = LISBON, modifiedOffsetMillis = 1L))

        observeVaultTree(vaultPath = VAULT).test {
            val cached = (awaitItem() as Outcome.Success).value
            val scanned = (awaitItem() as Outcome.Success).value

            assertEquals(
                expected = listOf(RelativePath(value = LISBON)),
                actual = reader.reads
            )
            assertEquals(
                expected = tags("recipe") to file(path = LISBON, modifiedOffsetMillis = 1L),
                actual =
                    scanned.notes.getValue(RelativePath(value = AREPAS)).metadata to
                        scanned.notes.getValue(RelativePath(value = LISBON)).file
            )
            assertEquals(
                expected = setOf(RelativePath(value = "Recipes"), RelativePath(value = "Travel")),
                actual = cached.folders
            )
        }
    }

    @Test
    fun `only what changed is saved to the cache`() = runTest {
        scanner.addNote(file = file(path = AREPAS))
        scanner.addNote(file = file(path = LISBON))

        observeVaultTree(vaultPath = VAULT).test {
            awaitItem()
            scanner.files.remove(RelativePath(value = LISBON))
            watcher.emit(change = VaultChange.Deleted(path = RelativePath(value = LISBON)))
            awaitItem()

            assertEquals(
                expected = listOf(
                    listOf(RelativePath(value = AREPAS), RelativePath(value = LISBON)) to emptyList(),
                    emptyList<RelativePath>() to listOf(RelativePath(value = LISBON))
                ),
                actual = cache.updates.map { (upserted, removed) -> upserted.sortedBy { it.value } to removed }
            )
        }
    }

    @Test
    fun `a note that cannot be read is cached without its size, so it is read again next time`() = runTest {
        scanner.addNote(file = file(path = AREPAS))
        reader.metadata[RelativePath(value = AREPAS)] = null

        observeVaultTree(vaultPath = VAULT).test {
            awaitItem()

            assertEquals(
                expected = null,
                actual = cache.entries
                    .getValue(RelativePath(value = AREPAS))
                    .file.size
            )
        }
    }

    private fun success(
        tree: VaultTree
    ): Outcome<VaultTree, NotesError> = Outcome.Success(value = tree)

    companion object {
        private const val AREPAS = "Recipes/Arepas.md"
        private const val LISBON = "Travel/Lisbon trip.md"
        private const val NEW_NOTE = "New note.md"
        private const val RECIPES = "Recipes"
    }
}
