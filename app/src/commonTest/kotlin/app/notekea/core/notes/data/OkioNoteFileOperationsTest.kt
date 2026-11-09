package app.notekea.core.notes.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.ForwardingFileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import okio.Sink
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals

class OkioNoteFileOperationsTest {
    private val testDispatcher = StandardTestDispatcher()
    private val fakeFileSystem = FakeFileSystem().apply {
        createDirectories(
            dir =
            VAULT.toPath() / IDEAS
        )
    }

    // Audit: a path with `..` wrote outside the vault, Okio taking `\\` for a separator as well.
    @Test
    fun `nothing is created or moved out of the vault`() = runOperationsTest {
        val note = path(value = "$IDEAS/Note.md")

        operations().createFile(
            vaultPath = VAULT,
            path = note
        )

        val outcomes = listOf(
            operations().createFile(
                vaultPath = VAULT,
                path = path(value = "..\\..\\evil.md")
            ),
            operations().createFile(
                vaultPath = VAULT,
                path = path(value = "../evil.md")
            ),
            operations().createFolder(
                vaultPath = VAULT,
                path = path(value = "$IDEAS/../../evil")
            ),
            operations().move(
                vaultPath = VAULT,
                from = note,
                to = path(value = "../evil.md")
            )
        )

        assertEquals(
            expected = List(size = 4) { Outcome.Failure(error = NotesError.InvalidName) },
            actual = outcomes
        )

        assertEquals(
            expected = true,
            actual = fakeFileSystem.exists(path = vaultPath(path = "$IDEAS/Note.md"))
        )
    }

    @Test
    fun `an empty note and an empty folder are created where they were asked`() =
        runOperationsTest {
            val outcomes = listOf(
                operations().createFile(
                    vaultPath = VAULT,
                    path = path(value = "$IDEAS/New note.md")
                ),
                operations().createFolder(
                    vaultPath = VAULT,
                    path = path(value = "Drafts")
                )
            )

            assertEquals(
                expected = listOf(Outcome.Success(value = Unit), Outcome.Success(value = Unit)),
                actual = outcomes
            )

            assertEquals(
                expected = listOf("", "true"),
                actual = listOf(
                    fakeFileSystem
                        .read(file = vaultPath(path = "$IDEAS/New note.md")) { readUtf8() },
                    fakeFileSystem
                        .metadata(path = vaultPath(path = "Drafts"))
                        .isDirectory
                        .toString()
                )
            )
        }

    @Test
    fun `creating never overwrites and needs an existing parent folder`() = runOperationsTest {
        writeNote(path = "$IDEAS/Taken.md")

        assertEquals(
            expected = listOf(
                Outcome.Failure(error = NotesError.NameTaken(name = "Taken.md")),
                Outcome.Failure(error = NotesError.NameTaken(name = IDEAS)),
                Outcome.Failure(error = NotesError.EntryMissing(path = "Gone"))
            ),
            actual = listOf(
                operations().createFile(
                    vaultPath = VAULT,
                    path = path(value = "$IDEAS/Taken.md")
                ),
                operations().createFolder(
                    vaultPath = VAULT,
                    path = path(value = IDEAS)
                ),
                operations().createFile(
                    vaultPath = VAULT,
                    path = path(value = "Gone/New note.md")
                )
            )
        )

        assertEquals(
            expected = CONTENT,
            actual = fakeFileSystem.read(file = vaultPath(path = "$IDEAS/Taken.md")) { readUtf8() }
        )
    }

    @Test
    fun `a disk that refuses the change fails the operation`() = runOperationsTest {
        val operations = operations(fileSystem = refusing())

        assertEquals(
            expected = listOf(
                Outcome.Failure(error = NotesError.OperationFailed(path = "New note.md")),
                Outcome.Failure(error = NotesError.OperationFailed(path = "Drafts"))
            ),
            actual = listOf(
                operations.createFile(
                    vaultPath = VAULT,
                    path = path(value = "New note.md")
                ),
                operations.createFolder(
                    vaultPath = VAULT,
                    path = path(value = "Drafts")
                )
            )
        )
    }

    @Test
    fun `a folder moves with everything inside it`() = runOperationsTest {
        writeNote(path = "$IDEAS/Reading list.md")

        val outcome = operations().move(
            vaultPath = VAULT,
            from = path(value = IDEAS),
            to = path(value = "Someday")
        )

        assertEquals(
            expected = listOf(Outcome.Success(value = Unit), false, CONTENT),
            actual = listOf(
                outcome,
                fakeFileSystem.exists(path = vaultPath(path = IDEAS)),
                fakeFileSystem
                    .read(file = vaultPath(path = "Someday/Reading list.md")) { readUtf8() }
            )
        )
    }

    @Test
    fun `moving fails for a missing source, a name in use or a refusing disk, but allows a case change`() =
        runOperationsTest {
            writeNote(path = "$IDEAS/a.md")
            writeNote(path = "$IDEAS/b.md")

            assertEquals(
                expected = listOf(
                    Outcome.Failure(error = NotesError.EntryMissing(path = "Gone.md")),
                    Outcome.Failure(error = NotesError.NameTaken(name = "b.md")),
                    Outcome.Failure(error = NotesError.OperationFailed(path = "$IDEAS/a.md")),
                    Outcome.Success(value = Unit)
                ),
                actual = listOf(
                    operations().move(
                        vaultPath = VAULT,
                        from = path(value = "Gone.md"),
                        to = path(value = "New.md")
                    ),
                    operations().move(
                        vaultPath = VAULT,
                        from = path(value = "$IDEAS/a.md"),
                        to = path(value = "$IDEAS/b.md")
                    ),
                    operations(fileSystem = refusing()).move(
                        vaultPath = VAULT,
                        from = path(value = "$IDEAS/a.md"),
                        to = path(value = "$IDEAS/c.md")
                    ),
                    operations().move(
                        vaultPath = VAULT,
                        from = path(value = "$IDEAS/a.md"),
                        to = path(value = "$IDEAS/A.md")
                    )
                )
            )
        }

    @Test
    fun `a case change never replaces another note on a disk where both names exist`() =
        runOperationsTest {
            fakeFileSystem.write(file = vaultPath(path = "$IDEAS/foo.md")) { writeUtf8("lower") }
            fakeFileSystem.write(file = vaultPath(path = "$IDEAS/Foo.md")) { writeUtf8("upper") }

            val outcome = operations().move(
                vaultPath = VAULT,
                from = path(value = "$IDEAS/foo.md"),
                to = path(value = "$IDEAS/Foo.md")
            )

            assertEquals(
                expected =
                Outcome.Failure(error = NotesError.NameTaken(name = "Foo.md")) to
                    listOf("lower", "upper"),
                actual = outcome to listOf("foo.md", "Foo.md").map { name ->
                    fakeFileSystem.read(file = vaultPath(path = "$IDEAS/$name")) { readUtf8() }
                }
            )
        }

    @Test
    fun `only an empty file is removed`() = runOperationsTest {
        fakeFileSystem
            .write(file = vaultPath(path = "$IDEAS/Full.md")) { writeUtf8(string = "text") }

        fakeFileSystem.write(file = vaultPath(path = "$IDEAS/Empty.md")) {}

        operations().deleteEmptyFile(
            vaultPath = VAULT,
            path = path(value = "$IDEAS/Full.md")
        )

        operations().deleteEmptyFile(
            vaultPath = VAULT,
            path = path(value = "$IDEAS/Empty.md")
        )

        assertEquals(
            expected = true to false,
            actual = fakeFileSystem.exists(path = vaultPath(path = "$IDEAS/Full.md")) to
                fakeFileSystem.exists(path = vaultPath(path = "$IDEAS/Empty.md"))
        )
    }

    @Test
    fun `anything on disk exists, hidden entries included`() = runOperationsTest {
        writeNote(path = ".obsidian/app.json")

        assertEquals(
            expected = listOf(true, true, true, false),
            actual = listOf(".obsidian/app.json", IDEAS, "", "Gone.md").map {
                operations().exists(
                    vaultPath = VAULT,
                    path = path(value = it)
                )
            }
        )
    }

    private fun operations(fileSystem: FileSystem = fakeFileSystem): OkioNoteFileOperations =
        OkioNoteFileOperations(
            fileSystem = fileSystem,
            dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
        )

    private fun path(value: String): RelativePath = RelativePath(value = value)

    private fun refusing(): FileSystem = object : ForwardingFileSystem(delegate = fakeFileSystem) {
        override fun atomicMove(
            source: Path,
            target: Path
        ) = throw IOException(DENIED)

        override fun createDirectory(
            dir: Path,
            mustCreate: Boolean
        ) = throw IOException(DENIED)

        override fun sink(
            file: Path,
            mustCreate: Boolean
        ): Sink = throw IOException(DENIED)
    }

    private fun runOperationsTest(block: suspend TestScope.() -> Unit) = runTest(
        context = testDispatcher
    ) { block() }

    private fun vaultPath(path: String): Path = VAULT.toPath() / path

    private fun writeNote(path: String) {
        val file = vaultPath(path = path)

        file.parent?.let { fakeFileSystem.createDirectories(dir = it) }
        fakeFileSystem.write(file = file) { writeUtf8(string = CONTENT) }
    }

    companion object {
        private const val CONTENT = "# Note\n"
        private const val DENIED = "Read-only file system"
        private const val IDEAS = "Ideas"
        private const val VAULT = "/home/ana/Notes"
    }
}
