package app.notekea.notes.presentation.tree

import app.notekea.core.domain.FakeClock
import app.notekea.core.domain.files.ValidateFileNameUseCase
import app.notekea.core.domain.time.FakeLocalCalendar
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.notes.domain.ApplyTemplateUseCase
import app.notekea.notes.domain.CreateFolderUseCase
import app.notekea.notes.domain.CreateNoteUseCase
import app.notekea.notes.domain.FakeBookmarkStore
import app.notekea.notes.domain.FakeFileOpener
import app.notekea.notes.domain.FakeFileRevealer
import app.notekea.notes.domain.FakeFileWatcher
import app.notekea.notes.domain.FakeNoteContentStore
import app.notekea.notes.domain.FakeNoteFileOperations
import app.notekea.notes.domain.FakeNoteMetadataCache
import app.notekea.notes.domain.FakeNoteMetadataReader
import app.notekea.notes.domain.FakeNoteScanner
import app.notekea.notes.domain.FakeTrash
import app.notekea.notes.domain.FakeTreePreferencesStore
import app.notekea.notes.domain.FakeVaultSettingsReader
import app.notekea.notes.domain.FillTemplateUseCase
import app.notekea.notes.domain.FlattenTreeUseCase
import app.notekea.notes.domain.FollowLinkUseCase
import app.notekea.notes.domain.GetDailyNoteSettingsUseCase
import app.notekea.notes.domain.NaturalNameComparator
import app.notekea.notes.domain.NoteFixtures.VAULT
import app.notekea.notes.domain.NoteFixtures.file
import app.notekea.notes.domain.ObserveVaultTreeUseCase
import app.notekea.notes.domain.OpenDailyNoteUseCase
import app.notekea.notes.domain.PlanLinkUpdatesUseCase
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.RenameEntryUseCase
import app.notekea.notes.domain.UpdateLinksAfterRenameUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
abstract class VaultTreeViewModelTestBase {
    protected val testDispatcher = StandardTestDispatcher()
    protected val scanner = FakeNoteScanner()
    protected val operations = FakeNoteFileOperations()
    protected val trash = FakeTrash()
    protected val revealer = FakeFileRevealer()
    protected val preferences = FakeTreePreferencesStore()
    protected val contents = FakeNoteContentStore()
    protected val opener = FakeFileOpener()
    protected val vaultSettings = FakeVaultSettingsReader()
    protected val bookmarkStore = FakeBookmarkStore()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        listOf(WELCOME, ROADMAP).forEach { scanner.addNote(file = file(path = it)) }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    protected fun path(
        value: String
    ): RelativePath = RelativePath(value = value)

    /** Daily notes over the fakes, at [NOW] for a user two hours east of UTC, where it is already the next day. */
    protected fun newNoteTools(): NewNoteTools = NewNoteTools(
        createNote = CreateNoteUseCase(noteFileOperations = operations),
        applyTemplate = ApplyTemplateUseCase(
            fillTemplate = FillTemplateUseCase(
                vaultSettingsReader = vaultSettings,
                noteContentStore = contents,
                localCalendar = FakeLocalCalendar(offsetHours = 2),
                clock = FakeClock(currentTime = Instant.parse(input = NOW))
            ),
            noteContentStore = contents
        )
    )

    protected fun dailyNoteTools(): DailyNoteTools = DailyNoteTools(
        getDailyNoteSettings = GetDailyNoteSettingsUseCase(vaultSettingsReader = vaultSettings),
        openDailyNote = OpenDailyNoteUseCase(
            vaultSettingsReader = vaultSettings,
            noteFileOperations = operations,
            noteContentStore = contents,
            localCalendar = FakeLocalCalendar(offsetHours = 2),
            clock = FakeClock(currentTime = Instant.parse(input = NOW))
        )
    )

    protected fun fromTemplate(): VaultTreeIntent = VaultTreeIntent.OnNoteFromTemplate(
        folder = path(PROJECTS),
        template = path(MEETING_TEMPLATE),
        baseName = UNTITLED
    )

    protected fun TestScope.rename(
        viewModel: VaultTreeViewModel,
        from: String,
        to: String
    ) {
        contents.texts.getOrPut(key = path("$to.md")) { "" }
        viewModel.onIntent(intent = VaultTreeIntent.OnRenameRequest(path = path(from)))
        viewModel.onIntent(intent = VaultTreeIntent.OnRenameTextChange(text = to))
        viewModel.onIntent(intent = VaultTreeIntent.OnRenameConfirm)
        advanceUntilIdle()
    }

    protected fun runTreeTest(
        block: suspend TestScope.() -> Unit
    ) = runTest(context = testDispatcher) { block() }

    protected fun TestScope.startedViewModel(): VaultTreeViewModel {
        val viewModel = VaultTreeViewModel(
            vaultPath = VAULT,
            platformConventions = PreviewPlatformConventions.linux,
            observeVaultTree = ObserveVaultTreeUseCase(
                noteScanner = scanner,
                noteMetadataReader = FakeNoteMetadataReader(),
                fileWatcher = FakeFileWatcher(),
                noteMetadataCache = FakeNoteMetadataCache()
            ),
            flattenTree = FlattenTreeUseCase(naturalNameComparator = NaturalNameComparator()),
            newNotes = newNoteTools(),
            createFolder = CreateFolderUseCase(noteFileOperations = operations),
            renameEntry = RenameEntryUseCase(
                noteFileOperations = operations,
                validateFileName = ValidateFileNameUseCase()
            ),
            trash = trash,
            fileRevealer = revealer,
            fileOpener = opener,
            days = dailyNoteTools(),
            bookmarkStore = bookmarkStore,
            updateLinksAfterRename = UpdateLinksAfterRenameUseCase(
                planLinkUpdates = PlanLinkUpdatesUseCase(
                    noteContentStore = contents,
                    linkFinder = JetBrainsMarkdownLinkFinder()
                ),
                applyLinkUpdates = ApplyLinkUpdatesUseCase(noteContentStore = contents)
            ),
            followLink = FollowLinkUseCase(createNote = CreateNoteUseCase(noteFileOperations = operations)),
            treePreferencesStore = preferences
        )
        advanceUntilIdle()
        return viewModel
    }

    companion object {
        const val BUG = "bug"
        const val NOW = "2026-10-26T23:30:00Z"
        const val TODAY = "Journal/2026-10-27.md"
        const val MEETING_TEMPLATE = "Templates/Meeting.md"
        const val MENU = "attachments/Weekly menu.pdf"
        const val PLATE = "attachments/Arepas plate.png"
        const val PROJECTS = "Projects"
        const val ROADMAP = "Projects/Roadmap.md"
        const val UNTITLED = "Untitled"
        const val WELCOME = "Welcome.md"
    }
}
