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
        told: List<AmbiguousTask.Settled> = emptyList(),
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
        vaultGuide: VaultGuide,
        remembered: List<MemoryLine> = emptyList()
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

        val qualified = qualifiedOf(
            task = task,
            notes = notes,
            remembered = remembered
        )

        val all = groups + qualified.map { it.second }
        if (all.isEmpty()) return emptyList()

        val texts = vaultGuide.textsOf(vaultPath, all.flatten().distinct())

        val told = ToldApart.settledOf(
            task = task,
            groups = groups,
            texts = texts
        )

        // Only the word written inside the remembered name tells its notes apart: any other is about the work.
        return told + qualified.flatMap { (word, group) ->
            ToldApart.settledOf(
                task = word,
                groups = listOf(group),
                texts = texts
            )
        }
    }

    // The notes that share a name with one the memory names, where the task writes a word inside that name: "the
    // household budget" is not "the budget" the memory settled when only the other Budget 2026 writes "household"
    // (the loop's seventh run held back the right answer for the memory's note).
    private fun qualifiedOf(
        task: String,
        notes: Collection<NoteEntry>,
        remembered: List<MemoryLine>
    ): List<Pair<String, List<NoteEntry>>> = remembered
        .flatMap { it.names }
        .mapNotNull { named ->
            val word = insideOf(
                task = task,
                said = named.said
            ) ?: return@mapNotNull null

            val title = named
                .note
                .removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)
                .substringAfterLast(delimiter = '/')

            val sharing = notes.filter {
                it.file.path.isMarkdown &&
                    it.file.path.nameWithoutExtension.equals(
                        other = title,
                        ignoreCase = true
                    )
            }

            (word to sharing).takeIf {
                sharing.size > 1
            }
        }

    private fun insideOf(
        task: String,
        said: String
    ): String? {
        val words = said
            .lowercase()
            .split(' ')
            .filter { it.isNotEmpty() }

        if (words.size < 2) return null

        val before = Regex.escape(words.dropLast(n = 1).joinToString(separator = " "))
        val last = Regex.escape(words.last())

        // Bounded by letters both sides: `\b` is ASCII on the JVM, and a name may start with "Á" (bug hunt, L6).
        val inside = "(?<![\\p{L}\\p{N}])$before\\s+(\\p{L}{4,})\\s+$last(?![\\p{L}\\p{N}])"

        return Regex(pattern = inside)
            .find(input = task.lowercase())
            ?.groupValues
            ?.get(1)
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
