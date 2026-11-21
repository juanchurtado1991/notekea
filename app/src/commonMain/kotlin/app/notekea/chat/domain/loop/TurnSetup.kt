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
 * How a turn of the loop starts from the person's memory and the task's frame (`docs/agent-memory.md`,
 * `docs/agent-frame.md`, `docs/agent-web.md`): what its tools work with (the country a search is made in, the sites
 * trusted, the notes the task means) and the memory's lines the model is given.
 */
internal object TurnSetup {
    fun contextOf(
        task: String,
        vaultPath: String,
        notes: Collection<NoteEntry>,
        today: String,
        remembered: List<MemoryLine>,
        openNote: RelativePath?,
        frame: TaskFrame? = null,
        sources: List<String> = emptyList()
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
            sources = sources,
            memory = remembered
        ),
        frame = frame
    )

    /**
     * Which notes, and which entries in them, the task means. Writing is framed too, as a change: "add a misc line
     * to the budget" means one of two budgets (8 of the 4B's 36 misses had no frame, the frame alone, 2026-11-18).
     * A note written from nothing is counted "none" and frames nothing.
     */
    suspend fun frameOf(
        task: String,
        vaultPath: String,
        notes: Collection<NoteEntry>,
        memory: List<MemoryLine>,
        kind: WorkKind,
        frames: FrameSource?,
        openNote: RelativePath?
    ): TaskFrame? = frames
        ?.of(
            task = task,
            vaultPath = vaultPath,
            notes = notes,
            memory = memory,
            openNote = openNote,
            isChange = kind == WorkKind.ORGANIZE || kind == WorkKind.WRITE
        )

    /** The memory's lines that bear on [task]: those the frame's model and the loop's model are given. */
    fun relevantOf(
        task: String,
        remembered: List<MemoryLine>,
        kind: WorkKind
    ): List<MemoryLine> = MemoryLines.relevantTo(
        lines = remembered,
        task = task,
        isWeb = kind == WorkKind.WEB
    )

    /**
     * The lines the loop's model is given: the [relevant] ones, but not a line naming a note the frame shows and says
     * the task does not mean. The request wins over the memory: "the household budget" is not the budget the memory
     * means when the frame says the task's words mean the other one.
     */
    fun memoryOf(
        relevant: List<MemoryLine>,
        frame: TaskFrame?
    ): List<MemoryLine> {
        val others = frame
            ?.others
            ?.map {
                it
                    .value
                    .removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)
                    .lowercase()
            }
            .orEmpty()

        if (others.isEmpty()) return relevant

        return relevant.filterNot { line ->
            line.names.any { named ->
                val link = named
                    .note
                    .removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)
                    .lowercase()

                others.any {
                    if ('/' in link) it == link else it.substringAfterLast(delimiter = '/') == link
                }
            }
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
