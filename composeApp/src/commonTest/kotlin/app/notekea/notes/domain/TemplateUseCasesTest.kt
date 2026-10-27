package app.notekea.notes.domain

import app.notekea.core.domain.FakeClock
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.FakeLocalCalendar
import app.notekea.notes.domain.NoteFixtures.VAULT
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/** Filling a template, applying it to a new note, and making the templates folder (`ROADMAP.md` 1.12). */
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
    fun `a template is filled in with the title, and now where the user is in the vault's formats`() = runTest {
        store.texts[meeting] = "---\ntags: meeting\n---\n# {{title}}\n{{date}} at {{time}}\n"
        val defaults = fillTemplate(vaultPath = VAULT, template = meeting, title = "Team sync")
        settings.templates = TemplateSettings(folder = "Templates", dateFormat = "DD/MM/YYYY", timeFormat = "h:mm A")
        val vaults = fillTemplate(vaultPath = VAULT, template = meeting, title = "Team sync")

        assertEquals(
            expected = listOf(
                NoteDocument.fromText(text = "---\ntags: meeting\n---\n# Team sync\n2026-10-27 at 09:05\n"),
                NoteDocument.fromText(text = "---\ntags: meeting\n---\n# Team sync\n27/10/2026 at 9:05 AM\n")
            ),
            actual = listOf(defaults, vaults).map { (it as Outcome.Success).value }
        )
    }

    @Test
    fun `a template that cannot be read fails, and applying it leaves the note alone`() = runTest {
        val note = RelativePath(value = "Untitled.md")
        store.texts[note] = ""

        val applied = ApplyTemplateUseCase(fillTemplate = fillTemplate, noteContentStore = store)(
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
    fun `a new note made from a template becomes the template, titled with the note's name`() = runTest {
        val note = RelativePath(value = "Projects/Kickoff.md")
        store.texts[meeting] = "---\ntags: meeting\n---\n# {{title}}\n"
        store.texts[note] = ""

        val applied = ApplyTemplateUseCase(fillTemplate = fillTemplate, noteContentStore = store)(
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
    fun `the templates folder is made with its example once, in the folder the vault's settings name`() = runTest {
        settings.templates = TemplateSettings.DEFAULT.copy(folder = "Meta/Templates")
        val createFolder = CreateTemplatesFolderUseCase(
            vaultSettingsReader = settings,
            noteFileOperations = operations,
            noteContentStore = store
        )

        val first = createFolder(vaultPath = VAULT, exampleName = "Meeting notes", exampleText = "# {{title}}\n")
        store.texts[RelativePath(value = "Meta/Templates/Meeting notes.md")] = "edited"
        val second = createFolder(vaultPath = VAULT, exampleName = "Meeting notes", exampleText = "# {{title}}\n")

        val example = RelativePath(value = "Meta/Templates/Meeting notes.md")
        assertEquals(
            expected = listOf(Outcome.Success(value = example), Outcome.Success(value = example)) to
                listOf("Meta", "Meta/Templates", example.value) to "edited",
            actual = listOf(first, second) to operations.existing.map { it.value } to store.texts[example]
        )
    }
}
