package app.notekea.tree.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.notes.data.GhostFrontmatterParser
import app.notekea.core.notes.domain.NoteDocument
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.frontmatter.NoteProperty
import app.notekea.core.notes.domain.frontmatter.PropertyValue
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals

class OkioNoteMetadataReaderTest {
    private val testDispatcher = StandardTestDispatcher()
    private val fakeFileSystem = FakeFileSystem()
    private val reader = OkioNoteMetadataReader(
        fileSystem = fakeFileSystem,
        dispatchers = FakeAppDispatchers(dispatcher = testDispatcher),
        parser = GhostFrontmatterParser()
    )

    @Test
    fun `every property keeps its type, and frontmatter that is not properties gives none`() =
        runReaderTest {
            writeNote(
                content = "---\nrating: 4\nfinished: true\nread: 2026-03-02\nauthor: Frank\n---\nDune\n"
            )

            val typed = readNote()?.properties
            writeNote(content = "---\nrating: 4\nnot a property\n---\nDune\n")

            assertEquals(
                expected = listOf(
                    NoteProperty(
                        key = "rating",
                        value = PropertyValue.Number(text = "4")
                    ),
                    NoteProperty(
                        key = "finished",
                        value = PropertyValue.Checkbox(isChecked = true)
                    ),
                    NoteProperty(
                        key = "read",
                        value = PropertyValue.Date(date = CalendarDate(2026, 3, 2))
                    ),
                    NoteProperty(
                        key = "author",
                        value = PropertyValue.Text(text = "Frank")
                    )
                ) to emptyList(),
                actual = typed to readNote()?.properties
            )
        }

    @Test
    fun `a task's place is in the note's body, with or without frontmatter, and with Windows line breaks`() =
        runReaderTest {
            listOf(
                "---\ntags: [a]\n---\n# Plan\n- [ ] Write\n",
                "Intro\n- [ ] Write\n",
                "---\r\ntags: [a]\r\n---\r\nIntro\r\n- [ ] Write\r\n"
            ).forEach { content ->
                writeNote(content = content)

                val task = requireNotNull(readNote()).tasks.single()
                val body = NoteDocument.fromText(text = content).body

                assertEquals(
                    expected = "- [ ] Write",
                    actual = body.substring(task.stateIndex - 3, task.endIndex)
                )
            }
        }

    @Test
    fun `frontmatter at the top of a note is read, and the body only for its tags`() =
        runReaderTest {
            writeNote(
                content = "---\ntags: [travel]\n---\n\n# Lisbon trip #trip\n\n---\ntags: [body]\n---\n"
            )

            assertEquals(
                expected = NoteMetadata(
                    tags = listOf("travel"),
                    aliases = emptyList(),
                    title = null,
                    textTags = listOf("trip"),
                    properties = listOf(
                        NoteProperty(
                            key = "tags",
                            value = PropertyValue.Items(items = listOf("travel"))
                        )
                    )
                ),
                actual = readNote()
            )
        }

    @Test
    fun `a note without frontmatter keeps the tags of its first line`() = runReaderTest {
        writeNote(content = "#inbox to sort\nThen #later.\n")

        assertEquals(
            expected = listOf("inbox", "later"),
            actual = readNote()?.textTags
        )
    }

    @Test
    fun `windows line endings, a byte order mark and a dots closing fence are accepted`() =
        runReaderTest {
            writeNote(content = "\uFEFF---\r\ntitle: Arepas\r\n...\r\nBody\r\n")

            assertEquals(
                expected = "Arepas",
                actual = readNote()?.title
            )
        }

    @Test
    fun `no frontmatter, a late fence or an unclosed block mean no metadata`() = runReaderTest {
        val contents = listOf(
            "# Welcome\n\ntags: [x]\n",
            "\n---\ntags: [x]\n---\n",
            "---\ntags: [x]\n",
            ""
        )

        assertEquals(
            expected = List(size = contents.size) { NoteMetadata.NONE },
            actual = contents.map {
                writeNote(content = it)
                readNote()
            }
        )
    }

    @Test
    fun `a frontmatter block longer than the limit is not read`() = runReaderTest {
        val longLine = "x".repeat(n = OkioNoteMetadataReader.MAX_FRONTMATTER_CHARACTERS + 1)
        writeNote(content = "---\ntitle: Huge\nnote: $longLine\n---\n")

        assertEquals(
            expected = NoteMetadata.NONE,
            actual = readNote()
        )
    }

    @Test
    fun `a note that cannot be read gives no metadata at all`() = runReaderTest {
        assertEquals(
            expected = null,
            actual = reader.read(
                vaultPath = VAULT,
                path = RelativePath(value = "Gone.md")
            )
        )
    }

    private suspend fun readNote(): NoteMetadata? = reader.read(
        vaultPath = VAULT,
        path = RelativePath(value = NOTE)
    )

    private fun runReaderTest(block: suspend TestScope.() -> Unit) = runTest(
        context = testDispatcher
    ) { block() }

    private fun writeNote(content: String) {
        fakeFileSystem.createDirectories(dir = VAULT.toPath())
        fakeFileSystem.write(file = VAULT.toPath() / NOTE) { writeUtf8(string = content) }
    }

    companion object {
        private const val NOTE = "Note.md"
        private const val VAULT = "/home/ana/Notes"
    }
}
