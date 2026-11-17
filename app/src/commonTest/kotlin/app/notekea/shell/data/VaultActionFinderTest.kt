package app.notekea.shell.data

import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionProblem
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.core.domain.FakeClock
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.FakeLocalCalendar
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.TextFolder
import app.notekea.core.notes.domain.links.ApplyLinkUpdatesUseCase
import app.notekea.core.notes.domain.tasks.NoteTask
import app.notekea.query.data.GhostQueryJsonReader
import app.notekea.query.domain.BuildQueryCatalogUseCase
import app.notekea.query.domain.QueryYaml
import app.notekea.query.domain.RunQueryUseCase
import app.notekea.query.domain.SetPropertyUseCase
import app.notekea.query.domain.TranslateQueryUseCase
import app.notekea.shell.data.chat.VaultActionFinder
import app.notekea.tasks.domain.ListTasksUseCase
import app.notekea.tasks.domain.SetTaskDoneUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/**
 * What the chat finds and checks in the vault (the bug hunt of 2026-11-17): "Pay rent" checked the task "Pay", as
 * the first task whose words the step's held was taken; and a request no property answers came back as every note.
 */
class VaultActionFinderTest {
    private val store = FakeNoteContentStore()
    private val engine = FakeLlmEngine()
    private val apply = ApplyLinkUpdatesUseCase(noteContentStore = store)
    private val home = RelativePath(value = "Home.md")

    private val finder = VaultActionFinder(
        translateQuery = TranslateQueryUseCase(
            llmEngine = engine,
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
        localCalendar = FakeLocalCalendar(),
        clock = FakeClock(currentTime = Instant.parse(input = "2026-11-17T09:00:00Z"))
    )

    @Test
    fun `the task of the step's words is checked, not a shorter one they hold nor an empty one`() =
        runTest {
            store.texts[home] = "- [ ] \n- [ ] Pay\n- [ ] Pay rent\n"

            val note = entry(
                path = home.value,
                metadata = NoteMetadata.NONE.copy(
                    tasks = listOf(
                        task(
                            state = 3,
                            end = 6,
                            text = ""
                        ),
                        task(
                            state = 10,
                            end = 16,
                            text = "Pay"
                        ),
                        task(
                            state = 20,
                            end = 31,
                            text = "Pay rent"
                        )
                    )
                )
            )

            finder.completeTask(
                vaultPath = VAULT,
                step = ActionStep(
                    kind = ActionKind.COMPLETE_TASK,
                    notes = listOf(home),
                    text = "Pay rent"
                ),
                tasks = note.metadata.tasks
            )

            assertEquals(
                expected = "- [ ] \n- [ ] Pay\n- [x] Pay rent\n",
                actual = store.texts[home]
            )
        }

    @Test
    fun `a task copied with its day is found, and of two it holds the longer one`() = runTest {
        store.texts[home] = "- [ ] Buy milk\n- [ ] Buy milk and eggs 📅 2027-01-05\n"

        val note = entry(
            path = home.value,
            metadata = NoteMetadata.NONE.copy(
                tasks = listOf(
                    task(
                        state = 3,
                        end = 14,
                        text = "Buy milk"
                    ),
                    task(
                        state = 18,
                        end = 52,
                        text = "Buy milk and eggs"
                    )
                )
            )
        )

        finder.completeTask(
            vaultPath = VAULT,
            step = ActionStep(
                kind = ActionKind.COMPLETE_TASK,
                notes = listOf(home),
                text = "Buy milk and eggs 📅 2027-01-05"
            ),
            tasks = note.metadata.tasks
        )

        assertEquals(
            expected = "- [ ] Buy milk\n- [x] Buy milk and eggs 📅 2027-01-05\n",
            actual = store.texts[home]
        )
    }

    @Test
    fun `a second tag on a note keeps the first, read from the note and not from the index`() =
        runTest {
            store.texts[home] = "---\ntags: [x]\n---\n# Home\n"
            val notes = listOf(entry(path = home.value))

            listOf("a", "b", "A").forEach { tag ->
                finder.setProperty(
                    vaultPath = VAULT,
                    step = ActionStep(
                        kind = ActionKind.ADD_TAG,
                        notes = listOf(home),
                        tag = tag
                    ),
                    path = home,
                    notes = notes
                )
            }

            assertEquals(
                expected = "---\ntags: [x, a, b]\n---\n# Home\n",
                actual = store.texts[home]
            )
        }

    @Test
    fun `a request the model turns into no filter and no order is no choice by properties`() =
        runTest {
            engine.reply = { """{"filters":[]}""" }

            val found = finder.find(
                vaultPath = VAULT,
                step = ActionStep(
                    kind = ActionKind.FIND_NOTES,
                    request = "projects that still have open tasks"
                ),
                notes = listOf(entry(path = home.value))
            )

            assertEquals(
                expected = Outcome.Failure(error = ActionProblem.UnknownQuery(property = null)),
                actual = found
            )
        }

    @Test
    fun `no priority is every note when none writes one yet, in a folder named by its own name`() =
        runTest {
            engine.reply = { NO_PRIORITY }

            val found = finder.find(
                vaultPath = VAULT,
                step = ActionStep(
                    kind = ActionKind.FIND_NOTES,
                    request = "projects with no priority"
                ),
                notes = listOf(entry(path = "Work/Projects/Garden.md"), entry(path = home.value))
            ) as Outcome.Success

            assertEquals(
                expected = listOf("Work/Projects/Garden.md"),
                actual = found
                    .value
                    .rows
                    .map { it.note.value }
            )
        }

    // A7: "call" ticked "Call mom" of "Call mom" and "Call dentist".
    @Test
    fun `of two tasks the words fit alike neither is ticked, and the step fails`() = runTest {
        store.texts[home] = "- [ ] Call mom\n- [ ] Call dentist\n"

        val done = finder.completeTask(
            vaultPath = VAULT,
            step = ActionStep(
                kind = ActionKind.COMPLETE_TASK,
                notes = listOf(home),
                text = "call"
            ),
            tasks = listOf(
                task(
                    state = 3,
                    end = 14,
                    text = "Call mom"
                ),
                task(
                    state = 18,
                    end = 33,
                    text = "Call dentist"
                )
            )
        )

        assertEquals(
            expected = listOf<Any?>(
                Outcome.Failure(error = ActionProblem.UnknownTask(text = "call")),
                "- [ ] Call mom\n- [ ] Call dentist\n"
            ),
            actual = listOf(done, store.texts[home])
        )
    }

    // A8: a zip code lost its zero and a phone its plus in any YAML reader.
    @Test
    fun `only a plain decimal is written as a number, text that looks like one stays text`() =
        runTest {
            val written = listOf("08001", "007", "0.5", "42").map { value ->
                store.texts[home] = "# Home\n"

                finder.setProperty(
                    vaultPath = VAULT,
                    step = ActionStep(
                        kind = ActionKind.SET_PROPERTY,
                        notes = listOf(home),
                        property = "code",
                        value = value
                    ),
                    path = home,
                    notes = emptyList()
                )

                store
                    .texts[home]
                    .orEmpty()
                    .lines()[1]
            }

            assertEquals(
                expected = listOf(
                    "code: \"08001\"",
                    "code: \"007\"",
                    "code: 0.5",
                    "code: 42"
                ),
                actual = written
            )
        }

    // T14: "tomorrow" is no period here, and its tasks were every open task, shown as tomorrow's.
    @Test
    fun `a period of no name here is no period`() = runTest {
        val found = finder.find(
            vaultPath = VAULT,
            step = ActionStep(
                kind = ActionKind.SHOW_TASKS,
                period = "tomorrow"
            ),
            notes = emptyList()
        ) as Outcome.Success

        assertEquals(
            expected = null,
            actual = found.value.period
        )
    }

    private fun task(
        state: Int,
        end: Int,
        text: String
    ) = NoteTask(
        stateIndex = state,
        endIndex = end,
        text = text,
        isDone = false
    )

    private companion object {
        const val NO_PRIORITY =
            """{"filters":[{"property":"","condition":"in folder","value":"Projects"},""" +
                """{"property":"priority","condition":"is empty","value":""}]}"""
    }
}
