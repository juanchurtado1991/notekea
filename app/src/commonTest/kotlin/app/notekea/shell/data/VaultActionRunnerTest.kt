package app.notekea.shell.data

import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionProblem
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.chat.domain.plan.DonePlan
import app.notekea.chat.domain.plan.NoteDraft
import app.notekea.core.appearance.domain.ColorTheme
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
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/** The chat's actions carried out on fakes of the vault: each change written as planned and taken back by Undo. */
class VaultActionRunnerTest {
    private val store = FakeNoteContentStore()
    private val operations = FakeNoteFileOperations()
    private val trash = FakeTrash()
    private val bookmarks = FakeBookmarkStore()
    private val appearance = FakeAppearanceStore()
    private val settings = FakeVaultSettingsReader()
    private val calendar = FakeLocalCalendar()
    private val clock = FakeClock(currentTime = Instant.parse(input = "2026-11-10T09:00:00Z"))
    private val apply = ApplyLinkUpdatesUseCase(noteContentStore = store)
    private val notes = listOf(entry(path = DUNE.value), entry(path = HOME.value))

    private val runner = VaultActionRunner(
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
        bookmarkStore = bookmarks,
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
        appearanceStore = appearance
    )

    private suspend fun run(vararg steps: ActionStep): DonePlan = (
        runner.run(
            vaultPath = VAULT,
            steps = steps.toList(),
            notes = notes
        ) as Outcome.Success
        ).value

    @Test
    fun `a written note, an added line and a property are done, then Undo takes them all back`() =
        runTest {
            store.texts[DUNE] = "# Dune\n\nDesert politics.\n"
            store.texts[HOME] = "# Home\n"

            val done = run(
                ActionStep(
                    kind = ActionKind.WRITE_FROM_NOTES,
                    content = listOf(
                        NoteDraft(
                            path = SUMMARY,
                            text = "# Summary\n\n- Desert [[Dune]]\n"
                        )
                    )
                ),
                ActionStep(
                    kind = ActionKind.APPEND_TEXT,
                    notes = listOf(HOME),
                    text = "Read [[Summary]]"
                ),
                ActionStep(
                    kind = ActionKind.SET_PROPERTY,
                    notes = listOf(DUNE),
                    property = "rating",
                    value = "5"
                )
            )

            val written = listOf(store.texts[SUMMARY], store.texts[HOME], store.texts[DUNE])

            runner.undo(
                vaultPath = VAULT,
                done = done
            )

            assertEquals(
                expected = listOf(
                    listOf("# Summary\n\n- Desert [[Dune]]\n", "# Home\n\nRead [[Summary]]\n", "---\nrating: 5\n---\n# Dune\n\nDesert politics.\n"),
                    listOf(SUMMARY),
                    listOf("# Home\n", "# Dune\n\nDesert politics.\n"),
                    done.created to done.changed
                ),
                actual = listOf(
                    written,
                    trash.trashed.toList(),
                    listOf(store.texts[HOME], store.texts[DUNE]),
                    listOf(SUMMARY) to listOf(HOME, DUNE)
                )
            )
        }

    @Test
    fun `only the latest twenty plans can be undone, so the journals do not grow with the session`() =
        runTest {
            store.texts[HOME] = "# Home\n"

            val plans = (1..21).map {
                run(
                    ActionStep(
                        kind = ActionKind.APPEND_TEXT,
                        notes = listOf(HOME),
                        text = "Line $it"
                    )
                )
            }

            val oldest = runner.undo(
                vaultPath = VAULT,
                done = plans.first()
            )

            val afterOldest = store.texts[HOME]

            val newest = runner.undo(
                vaultPath = VAULT,
                done = plans.last()
            )

            // The oldest journal was let go (memory leak hunt): undoing it does nothing, and says it cannot (the
            // code review of 2026-11-13: it said "undone"); the newest still undoes.
            assertEquals(
                expected = listOf<Any>(
                    Outcome.Failure(error = ActionProblem.CannotUndo),
                    Outcome.Success(value = Unit)
                ),
                actual = listOf<Any>(oldest, newest)
            )

            assertEquals(
                expected = listOf(true, false),
                actual = listOf(
                    afterOldest.orEmpty().contains(other = "Line 1\n"),
                    store
                        .texts[HOME]
                        .orEmpty()
                        .contains(other = "Line 21")
                )
            )
        }

    @Test
    fun `a note in the way is never overwritten, and the theme changes by its name`() = runTest {
        store.texts[SUMMARY] = "Mine"
        operations.existing += SUMMARY

        val failed = runner.run(
            vaultPath = VAULT,
            steps = listOf(
                ActionStep(
                    kind = ActionKind.CREATE_NOTE,
                    title = "Summary",
                    content = listOf(
                        NoteDraft(
                            path = SUMMARY,
                            text = "Theirs"
                        )
                    )
                )
            ),
            notes = notes
        )

        val isKnown = runner.setTheme(theme = "Forest")

        assertEquals(
            expected = Triple(true, "Mine", ColorTheme.FOREST),
            actual = Triple(
                failed is Outcome.Failure && isKnown,
                store.texts[SUMMARY],
                appearance
                    .observe()
                    .first()
                    .theme
            )
        )
    }

    @Test
    fun `a plan that fails halfway takes back what it did, and an append with no note fails`() =
        runTest {
            store.texts[HOME] = "# Home\n"
            store.texts[SUMMARY] = "Mine"
            operations.existing += SUMMARY

            val halfway = runner.run(
                vaultPath = VAULT,
                steps = listOf(
                    ActionStep(
                        kind = ActionKind.APPEND_TEXT,
                        notes = listOf(HOME),
                        text = "Added"
                    ),
                    ActionStep(
                        kind = ActionKind.CREATE_NOTE,
                        title = "Summary",
                        content = listOf(
                            NoteDraft(
                                path = SUMMARY,
                                text = "Theirs"
                            )
                        )
                    )
                ),
                notes = notes
            )

            val nowhere = runner.run(
                vaultPath = VAULT,
                steps = listOf(
                    ActionStep(
                        kind = ActionKind.APPEND_TEXT,
                        notes = listOf(MISSING),
                        text = "Lost"
                    )
                ),
                notes = notes
            )

            assertEquals(
                expected = Triple(true, "# Home\n", true),
                actual = Triple(
                    halfway is Outcome.Failure,
                    store.texts[HOME],
                    nowhere is Outcome.Failure
                )
            )
        }

    private companion object {
        const val VAULT = "/vault"
        val DUNE = RelativePath(value = "Books/Dune.md")
        val HOME = RelativePath(value = "Home.md")
        val SUMMARY = RelativePath(value = "Books/Summary.md")
        val MISSING = RelativePath(value = "Gone.md")
    }
}
