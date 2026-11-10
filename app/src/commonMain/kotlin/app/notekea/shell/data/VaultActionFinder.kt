package app.notekea.shell.data

import app.notekea.chat.domain.ActionKind
import app.notekea.chat.domain.ActionProblem
import app.notekea.chat.domain.ActionStep
import app.notekea.chat.domain.FoundNotes
import app.notekea.chat.domain.FoundRow
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.domain.time.LocalCalendar
import app.notekea.core.notes.domain.LinkFinder
import app.notekea.core.notes.domain.LinkResolver
import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteProperty
import app.notekea.core.notes.domain.NoteRewrite
import app.notekea.core.notes.domain.PropertyValue
import app.notekea.core.notes.domain.RelativePath
import app.notekea.query.domain.BuildQueryCatalogUseCase
import app.notekea.query.domain.QueryTranslation
import app.notekea.query.domain.QueryYaml
import app.notekea.query.domain.RunQueryUseCase
import app.notekea.query.domain.SetPropertyUseCase
import app.notekea.query.domain.TranslateQueryUseCase
import app.notekea.tasks.domain.ListTasksUseCase
import app.notekea.tasks.domain.SetTaskDoneUseCase
import app.notekea.tasks.domain.TaskGrouping
import app.notekea.tasks.domain.TaskRow
import app.notekea.tasks.domain.TaskView
import app.notekea.tasks.domain.TaskWhen
import kotlin.time.Clock

/**
 * What the chat's actions find and the writes that go through the features' own use cases (3.8, 3.9): notes by
 * their properties through the query of 2.10, tasks through the Tasks tab's list of 2.11, notes without tags and broken
 * links from the note cache, a property or a tag set, a task checked.
 */
class VaultActionFinder(
    private val translateQuery: TranslateQueryUseCase,
    private val buildCatalog: BuildQueryCatalogUseCase,
    private val runQuery: RunQueryUseCase,
    private val queryYaml: QueryYaml,
    private val listTasks: ListTasksUseCase,
    private val setTaskDone: SetTaskDoneUseCase,
    private val setPropertyUseCase: SetPropertyUseCase,
    private val noteContentStore: NoteContentStore,
    private val linkFinder: LinkFinder,
    private val localCalendar: LocalCalendar,
    private val clock: Clock
) {
    suspend fun find(
        vaultPath: String,
        step: ActionStep,
        notes: Collection<NoteEntry>
    ): Outcome<FoundNotes, ActionProblem> {
        return when (step.kind) {
            ActionKind.FIND_NOTES -> query(
                request = step.request ?: step.text.orEmpty(),
                notes = notes
            )

            ActionKind.SHOW_TASKS -> Outcome.Success(
                value = tasks(
                    step = step,
                    notes = notes
                )
            )

            ActionKind.LIST_UNTAGGED -> Outcome.Success(value = untagged(notes = notes))

            ActionKind.LIST_BROKEN_LINKS -> Outcome.Success(
                value = brokenLinks(
                    vaultPath = vaultPath,
                    notes = notes
                )
            )

            else -> Outcome.Success(value = FoundNotes())
        }
    }

    private suspend fun query(
        request: String,
        notes: Collection<NoteEntry>
    ): Outcome<FoundNotes, ActionProblem> {
        val catalog = buildCatalog(notes = notes)

        val query = when (
            val translated =
                translateQuery(request = request, catalog = catalog, today = isoOf(date = today()))
        ) {
            is QueryTranslation.Query -> translated.query

            is QueryTranslation.UnknownProperty -> return Outcome.Failure(
                error = ActionProblem.UnknownQuery(property = translated.property)
            )

            QueryTranslation.Failed ->
                return Outcome.Failure(error = ActionProblem.UnknownQuery(property = null))
        }

        val result = runQuery(
            query = query,
            notes = notes
        )

        return Outcome.Success(
            value = FoundNotes(
                columns = result.columns,
                rows = result.rows.map { row ->
                    FoundRow(
                        note = row.path,
                        cells = row.cells.map { cellOf(it) }
                    )
                },
                query = queryYaml.write(query = query)
            )
        )
    }

    private fun tasks(
        step: ActionStep,
        notes: Collection<NoteEntry>
    ): FoundNotes {
        val period = PERIODS[step.period?.lowercase()]

        val view = TaskView(
            grouping = TaskGrouping.DATE,
            folder = step.folder?.value,
            tag = step.tag,
            showsDone = period == TaskWhen.DONE
        )

        val rows = listTasks(
            view = view,
            notes = notes.filter {
                step.notes.isEmpty() || it.file.path in step.notes
            },
            today = today()
        )
            .groups
            .flatMap { it.rows }
            .filter {
                period == null || it.due == period
            }

        return FoundNotes(
            rows = rows.map {
                FoundRow(
                    note = it.path,
                    cells = listOf(it.task.text),
                    line = it.task.stateIndex
                )
            }
        )
    }

    private fun untagged(notes: Collection<NoteEntry>): FoundNotes = FoundNotes(
        rows = notes
            .filter {
                it.metadata.tags.isEmpty() && it.metadata.textTags.isEmpty()
            }
            .sortedBy {
                it
                    .file
                    .path
                    .value
                    .lowercase()
            }
            .map { FoundRow(note = it.file.path) }
    )

    private suspend fun brokenLinks(
        vaultPath: String,
        notes: Collection<NoteEntry>
    ): FoundNotes {
        val resolver = LinkResolver(notePaths = notes.map { it.file.path })

        val rows = notes.flatMap { note ->
            val text = noteContentStore.read(
                vaultPath = vaultPath,
                path = note.file.path
            ).orEmpty()

            linkFinder
                .find(markdown = text)
                .filter {
                    resolver.resolve(
                        link = it,
                        source = note.file.path
                    ) == null
                }
                .map {
                    FoundRow(
                        note = note.file.path,
                        cells = listOf(it.target)
                    )
                }
        }

        return FoundNotes(rows = rows)
    }

    /** Sets the step's property, or adds its tag to the note's tags, and returns what was written. */
    suspend fun setProperty(
        vaultPath: String,
        step: ActionStep,
        path: RelativePath,
        notes: Collection<NoteEntry>
    ): NoteRewrite? {
        val property = if (step.kind == ActionKind.ADD_TAG) {
            val tags = notes
                .firstOrNull {
                    it.file.path == path
                }
                ?.metadata
                ?.tags
                .orEmpty()

            val tag = step.tag.orEmpty()
            if (tag in tags) return null

            NoteProperty(
                key = TAGS,
                value = PropertyValue.Items(items = tags + tag)
            )
        } else {
            NoteProperty(
                key = step.property.orEmpty(),
                value = valueOf(text = step.value.orEmpty())
            )
        }

        return setPropertyUseCase(
            vaultPath = vaultPath,
            path = path,
            property = property
        )
    }

    /** Checks the open task of the step's note whose text reads most like the step's, and returns what was written. */
    suspend fun completeTask(
        vaultPath: String,
        step: ActionStep,
        notes: Collection<NoteEntry>
    ): NoteRewrite? {
        val note = notes.firstOrNull {
            it.file.path == step.notes.firstOrNull()
        } ?: return null

        val wanted = step
            .text
            .orEmpty()
            .lowercase()

        val task = note
            .metadata
            .tasks
            .filterNot { it.isDone }
            .firstOrNull {
                wanted in it.text.lowercase() || it.text.lowercase() in wanted
            }
            ?: return null

        return setTaskDone(
            vaultPath = vaultPath,
            row = TaskRow(
                path = note.file.path,
                task = task,
                due = TaskWhen.NO_DATE
            )
        )
    }

    private fun valueOf(text: String): PropertyValue {
        val trimmed = text.trim()

        val date = ISO_DATE
            .matchEntire(input = trimmed)
            ?.destructured
            ?.let { (year, month, day) ->
                CalendarDate(
                    year = year.toInt(),
                    month = month.toInt(),
                    day = day.toInt()
                ).takeIf { it.isValid }
            }

        return when {
            trimmed.equals("true", ignoreCase = true) -> PropertyValue.Checkbox(isChecked = true)
            trimmed.equals("false", ignoreCase = true) -> PropertyValue.Checkbox(isChecked = false)
            trimmed.toDoubleOrNull() != null -> PropertyValue.Number(text = trimmed)
            date != null -> PropertyValue.Date(date = date)
            else -> PropertyValue.Text(text = trimmed)
        }
    }

    private fun cellOf(property: NoteProperty?): String = when (val value = property?.value) {
        null -> ""
        is PropertyValue.Text -> value.text
        is PropertyValue.Number -> value.text
        is PropertyValue.Checkbox -> value.isChecked.toString()
        is PropertyValue.Date -> isoOf(date = value.date)
        is PropertyValue.Items -> value.items.joinToString()
        is PropertyValue.Other -> value.source
    }

    private fun today(): CalendarDate = localCalendar.momentOf(instant = clock.now()).date

    private fun isoOf(date: CalendarDate): String =
        "${date.year}-${date.month.toString().padStart(length = 2, padChar = '0')}-" +
            date
                .day
                .toString()
                .padStart(
                    length = 2,
                    padChar = '0'
                )

    private companion object {
        const val TAGS = "tags"
        val ISO_DATE = Regex(pattern = "(\\d{4})-(\\d{2})-(\\d{2})")
        val PERIODS = mapOf(
            "overdue" to TaskWhen.OVERDUE,
            "today" to TaskWhen.TODAY,
            "this week" to TaskWhen.THIS_WEEK,
            "later" to TaskWhen.LATER,
            "no date" to TaskWhen.NO_DATE,
            "done" to TaskWhen.DONE
        )
    }
}
