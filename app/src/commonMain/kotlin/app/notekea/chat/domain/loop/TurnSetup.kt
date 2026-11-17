package app.notekea.chat.domain.loop

import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.memory.domain.MemoryLine
import app.notekea.core.memory.domain.MemoryLines
import app.notekea.core.memory.domain.MemorySection
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.web.domain.Countries
import app.notekea.core.web.domain.SourceTrust

/**
 * How a turn of the loop starts from the person's memory (`docs/agent-memory.md`, `docs/agent-web.md`): what its
 * tools work with (the country a search is made in, the sites trusted, the notes the memory says the task means) and
 * the memory's lines the model is given.
 */
internal object TurnSetup {
    fun contextOf(
        task: String,
        vaultPath: String,
        notes: Collection<NoteEntry>,
        today: String,
        remembered: List<MemoryLine>,
        openNote: RelativePath?,
        told: List<AmbiguousTask.Settled> = emptyList()
    ): ToolContext = ToolContext(
        vaultPath = vaultPath,
        notes = notes,
        task = task,
        openNote = openNote,
        today = dateOf(written = today),
        country = countryOf(
            task = task,
            remembered = remembered
        ),
        trust = SourceTrust(
            sources = emptyList(),
            memory = remembered
        ),
        remembered = settledOf(
            task = task,
            notes = notes,
            remembered = remembered,
            told = told
        )
            .map { it.meant }
            .toSet()
    )

    /**
     * The sets of notes settled for [task]: the ones its own words tell apart by what the notes say ([told]), then
     * the memory's, but not for a set the task already told: the request wins over the memory.
     */
    fun settledOf(
        task: String,
        notes: Collection<NoteEntry>,
        remembered: List<MemoryLine>,
        told: List<AmbiguousTask.Settled>
    ): List<AmbiguousTask.Settled> = told + AmbiguousTask
        .settledOf(task, notes, remembered.flatMap { it.names })
        .filterNot { settled ->
            told.any {
                it.meant == settled.meant || it.meant in settled.others
            }
        }

    /** The sets of notes [task]'s name fits that a word of it tells apart, by what the notes say ([ToldApart]). */
    suspend fun apartOf(
        task: String,
        vaultPath: String,
        notes: Collection<NoteEntry>,
        vaultGuide: VaultGuide
    ): List<AmbiguousTask.Settled> {
        val byPath = notes.associateBy { it.file.path }

        val groups = AmbiguousTask
            .openOf(
                task = task,
                notes = notes
            )
            .map { group ->
                group.mapNotNull {
                    byPath[it]
                }
            }

        if (groups.isEmpty()) return emptyList()

        return ToldApart.settledOf(
            task = task,
            groups = groups,
            texts = vaultGuide.textsOf(vaultPath, groups.flatten().distinct())
        )
    }

    /** The lines the model is given: the relevant ones, but a name the task says which one of is the request's. */
    fun memoryOf(
        task: String,
        remembered: List<MemoryLine>,
        kind: WorkKind,
        apart: List<AmbiguousTask.Settled> = emptyList()
    ): List<MemoryLine> = MemoryLines
        .relevantTo(
            lines = remembered,
            task = task,
            isWeb = kind == WorkKind.WEB
        )
        .filterNot { line ->
            line.names.any { AmbiguousTask.isQualified(task, it.said) } || isOverruled(line, apart)
        }

    // A line that names another note of a set the task's own words told apart: "my Sunday weekly review".
    private fun isOverruled(
        line: MemoryLine,
        apart: List<AmbiguousTask.Settled>
    ): Boolean {
        val note = line
            .names
            .firstOrNull()
            ?.note
            ?.removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)
            ?.lowercase() ?: return false

        return apart
            .flatMap { it.others }
            .map {
                it
                    .value
                    .removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)
                    .lowercase()
            }
            .any {
                it == note || it.substringAfterLast(delimiter = '/') == note
            }
    }

    // The country a search is made in: the one the task names, else the one the memory says the person lives in.
    private fun countryOf(
        task: String,
        remembered: List<MemoryLine>
    ): String? = Countries.codeIn(text = task) ?: remembered
        .filter {
            it.section == MemorySection.ABOUT
        }
        .firstNotNullOfOrNull { Countries.codeIn(text = it.text) }

    private fun dateOf(written: String): CalendarDate? {
        val parts = written
            .split("-")
            .mapNotNull { it.toIntOrNull() }

        if (parts.size != DATE_PARTS) return null

        return CalendarDate(
            year = parts[0],
            month = parts[1],
            day = parts[2]
        ).takeIf { it.isValid }
    }

    private const val DATE_PARTS = 3
}
