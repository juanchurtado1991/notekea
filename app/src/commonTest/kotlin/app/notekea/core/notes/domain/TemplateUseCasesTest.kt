package app.notekea.core.notes.domain

import app.notekea.core.domain.FakeClock
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.FakeLocalCalendar
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.templates.FillTemplateUseCase
import app.notekea.core.notes.domain.templates.TemplateSettings
import app.notekea.core.notes.domain.templates.Templates
import app.notekea.templates.domain.CreateTemplatesFolderUseCase
import app.notekea.tree.domain.ApplyTemplateUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/** Filling a template, applying it to a new note, and making the templates folder. */
class TemplateUseCasesTest {
    private val settings = FakeVaultSettingsReader()
    private val operations = FakeNoteFileOperations()
    private val store = FakeNoteContentStore()
    private val fillTemplate = FillTemplateUseCase(
        vaultSettingsReader = settings,
        noteContentStore = store,
        localCalendar = FakeLocalCalendar(offsetHours = 2),
        clock = FakeClock(currentTime = Instant.parse(input = "2026-10-27T07:05:00Z"))
    )
    private val meeting = RelativePath(value = "Templates/Meeting notes.md")

    @Test
    fun `a template's text after a closing line that ended the file leaves the properties in place`() =
        runTest {
            val note = RelativePath(value = "Untitled.md")
            store.texts[note] = "---\na: 1\n---"
            store.texts[meeting] = "# {{title}}\n"

            ApplyTemplateUseCase(fillTemplate, store)(
                vaultPath = VAULT,
                note = note,
                template = meeting
            )

            assertEquals(
                expected = "---\na: 1\n---\n# Untitled\n",
                actual = store.texts[note]
            )
        }

    @Test
    fun `a template's properties that end its file stay properties above the note's text`() =
        runTest {
            val note = RelativePath(value = "Untitled.md")
            store.texts[note] = "Some text"
            store.texts[meeting] = "---\ntags: meeting\n---"

            ApplyTemplateUseCase(fillTemplate, store)(
                vaultPath = VAULT,
                note = note,
                template = meeting
            )

            assertEquals(
                expected = "---\ntags: meeting\n---\nSome text",
                actual = store.texts[note]
            )
        }

    @Test
    fun `a template is filled in with the title, and now where the user is in the vault's formats`() =
        runTest {
            store.texts[meeting] = "---\ntags: meeting\n---\n# {{title}}\n{{date}} at {{time}}\n"

            val defaults = fillTemplate(
                vaultPath = VAULT,
                template = meeting,
                title = "Team sync"
            )

            settings.templates = TemplateSettings(
                folder = "Templates",
                dateFormat = "DD/MM/YYYY",
                timeFormat = "h:mm A"
            )

            val vaults = fillTemplate(
                vaultPath = VAULT,
                template = meeting,
                title = "Team sync"
            )

            assertEquals(
                expected = listOf(
                    NoteDocument.fromText(
                        text = "---\ntags: meeting\n---\n# Team sync\n2026-10-27 at 09:05\n"
                    ),
                    NoteDocument.fromText(
                        text = "---\ntags: meeting\n---\n# Team sync\n27/10/2026 at 9:05 AM\n"
                    )
                ),
                actual = listOf(defaults, vaults).map { (it as Outcome.Success).value }
            )
        }

    @Test
    fun `a template that cannot be read fails, and applying it leaves the note alone`() = runTest {
        val note = RelativePath(value = "Untitled.md")
        store.texts[note] = ""

        val applied = ApplyTemplateUseCase(
            fillTemplate = fillTemplate,
            noteContentStore = store
        )(
            vaultPath = VAULT,
            note = note,
            template = meeting
        )

        assertEquals(
            expected = Outcome.Failure(error = NotesError.EntryMissing(path = meeting.value)) to "",
            actual = applied to store.texts[note]
        )
    }

    @Test
    fun `a new note made from a template becomes the template, titled with the note's name`() =
        runTest {
            val note = RelativePath(value = "Projects/Kickoff.md")
            store.texts[meeting] = "---\ntags: meeting\n---\n# {{title}}\n"
            store.texts[note] = ""

            val applied = ApplyTemplateUseCase(
                fillTemplate = fillTemplate,
                noteContentStore = store
            )(
                vaultPath = VAULT,
                note = note,
                template = meeting
            )

            assertEquals(
                expected = Outcome.Success(value = Unit) to "---\ntags: meeting\n---\n# Kickoff\n",
                actual = applied to store.texts[note]
            )
        }

    @Test
    fun `the templates folder is made with its example once, in the folder the vault's settings name`() =
        runTest {
            settings.templates = TemplateSettings.DEFAULT.copy(folder = "Meta/Templates")

            val createFolder = CreateTemplatesFolderUseCase(
                vaultSettingsReader = settings,
                noteFileOperations = operations,
                noteContentStore = store
            )

            val first = createFolder(
                vaultPath = VAULT,
                exampleName = "Meeting notes",
                exampleText = "# {{title}}\n"
            )

            val example = RelativePath(value = "Meta/Templates/Meeting notes.md")
            operations.existing += example
            store.texts[example] = "edited"

            val second = createFolder(
                vaultPath = VAULT,
                exampleName = "Meeting notes",
                exampleText = "# {{title}}\n"
            )

            assertEquals(
                expected =
                listOf(Outcome.Success(value = example), Outcome.Success(value = example)) to
                    listOf("Meta", "Meta/Templates", example.value) to "edited",
                actual =
                listOf(first, second) to operations.existing.map { it.value } to
                    store.texts[example]
            )
        }

    // A template listed while it was still empty inserted nothing (1.12-01, about 1 run in 30 under load).
    @Test
    fun `the example never exists without its text`() = runTest {
        val example = RelativePath(value = "Templates/Meeting notes.md")
        store.refused += example

        val createFolder = CreateTemplatesFolderUseCase(
            vaultSettingsReader = settings,
            noteFileOperations = operations,
            noteContentStore = store
        )

        val outcome = createFolder(
            vaultPath = VAULT,
            exampleName = "Meeting notes",
            exampleText = "# {{title}}\n"
        )

        assertEquals(
            expected =
            Outcome.Failure(error = NotesError.OperationFailed(path = example.value)) to false,
            actual = outcome to (example in operations.existing)
        )
    }
}
