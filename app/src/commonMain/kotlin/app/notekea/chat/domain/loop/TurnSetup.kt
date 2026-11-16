package app.notekea.chat.domain.loop

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
        openNote: RelativePath?
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
        remembered = AmbiguousTask
            .settledOf(task, notes, remembered.mapNotNull { it.named })
            .map { it.meant }
            .toSet()
    )

    /** The lines the model is given: the relevant ones, but a name the task says which one of is the request's. */
    fun memoryOf(
        task: String,
        remembered: List<MemoryLine>,
        kind: WorkKind
    ): List<MemoryLine> = MemoryLines
        .relevantTo(
            lines = remembered,
            task = task,
            isWeb = kind == WorkKind.WEB
        )
        .filterNot { line ->
            line.named?.let { AmbiguousTask.isQualified(task, it.said) } == true
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
