package app.notekea.shell.data

import app.notekea.chat.domain.plan.ActionProblem
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.chat.domain.plan.DonePlan
import app.notekea.core.appearance.domain.FakeAppearanceStore
import app.notekea.core.domain.FakeClock
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.files.ValidateFileNameUseCase
import app.notekea.core.domain.time.FakeLocalCalendar
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeNoteFileOperations
import app.notekea.core.notes.domain.FakeNoteScanner
import app.notekea.core.notes.domain.FakeVaultSettingsReader
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RenameEntryUseCase
import app.notekea.core.notes.domain.TextFolder
import app.notekea.core.notes.domain.links.ApplyLinkUpdatesUseCase
import app.notekea.core.notes.domain.links.PlanLinkUpdatesUseCase
import app.notekea.core.notes.domain.links.UpdateLinksAfterRenameUseCase
import app.notekea.core.notes.domain.templates.FillTemplateUseCase
import app.notekea.query.data.GhostQueryJsonReader
import app.notekea.query.domain.BuildQueryCatalogUseCase
import app.notekea.query.domain.QueryYaml
import app.notekea.query.domain.RunQueryUseCase
import app.notekea.query.domain.SetPropertyUseCase
import app.notekea.query.domain.TranslateQueryUseCase
import app.notekea.shell.data.chat.VaultActionFinder
import app.notekea.shell.data.chat.VaultActionRunner
import app.notekea.tasks.domain.ListTasksUseCase
import app.notekea.tasks.domain.SetTaskDoneUseCase
import app.notekea.tree.domain.ApplyTemplateUseCase
import app.notekea.tree.domain.FakeBookmarkStore
import app.notekea.tree.domain.FakeTrash
import app.notekea.tree.domain.OpenDailyNoteUseCase
import kotlin.time.Instant

/** The chat's runner on fakes of the vault, as [VaultActionRunnerTest] builds it, for the tests of its fixes. */
class ChatRunnerFixture {
    val store = FakeNoteContentStore()
    val operations = FakeNoteFileOperations()
    val trash = FakeTrash()
    private val settings = FakeVaultSettingsReader()
    private val calendar = FakeLocalCalendar()
    private val clock = FakeClock(currentTime = Instant.parse(input = "2026-11-10T09:00:00Z"))
    private val apply = ApplyLinkUpdatesUseCase(noteContentStore = store)

    val runner = VaultActionRunner(
        noteContentStore = store,
        noteFileOperations = operations,
        noteScanner = FakeNoteScanner(),
        renameEntry = RenameEntryUseCase(
            noteFileOperations = operations,
            validateFileName = ValidateFileNameUseCase()
        ),
        updateLinksAfterRename = UpdateLinksAfterRenameUseCase(
            planLinkUpdates = PlanLinkUpdatesUseCase(
                noteContentStore = store,
                linkFinder = JetBrainsMarkdownLinkFinder()
            ),
            applyLinkUpdates = apply
        ),
        applyLinkUpdates = apply,
        applyTemplate = ApplyTemplateUseCase(
            fillTemplate = FillTemplateUseCase(settings, store, calendar, clock),
            noteContentStore = store
        ),
        openDailyNote = OpenDailyNoteUseCase(settings, operations, store, calendar, clock),
        bookmarkStore = FakeBookmarkStore(),
        trash = trash,
        finder = VaultActionFinder(
            translateQuery = TranslateQueryUseCase(
                llmEngine = FakeLlmEngine(),
                reader = GhostQueryJsonReader()
            ),
            buildCatalog = BuildQueryCatalogUseCase(),
            runQuery = RunQueryUseCase(),
            queryYaml = QueryYaml(),
            listTasks = ListTasksUseCase(),
            setTaskDone = SetTaskDoneUseCase(
                noteContentStore = store,
                applyLinkUpdates = apply
            ),
            setPropertyUseCase = SetPropertyUseCase(
                noteContentStore = store,
                applyLinkUpdates = apply
            ),
            noteTextCache = NoteTextCache(
                contentStore = store,
                textFolder = TextFolder()
            ),
            linkFinder = JetBrainsMarkdownLinkFinder(),
            localCalendar = calendar,
            clock = clock
        ),
        appearanceStore = FakeAppearanceStore()
    )

    suspend fun run(
        notes: Collection<NoteEntry>,
        vararg steps: ActionStep
    ): Outcome<DonePlan, ActionProblem> = runner.run(
        vaultPath = VAULT,
        steps = steps.toList(),
        notes = notes
    )

    suspend fun undo(done: Outcome<DonePlan, ActionProblem>) {
        runner.undo(
            vaultPath = VAULT,
            done = (done as Outcome.Success).value
        )
    }

    companion object {
        const val VAULT = "/vault"
    }
}
