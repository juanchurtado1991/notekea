package app.notekea.chat.domain.loop

import app.notekea.core.memory.domain.MemoryLine
import app.notekea.core.memory.domain.MemoryLines
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath

/**
 * The notes a task's words fit equally and that it does not tell apart (`docs/agent-loop.md`, "Why the loop fails",
 * item 3): two notes of one name, two people of one first name, "the X" where several notes are an X.
 */
internal object AmbiguousTask {
    /**
     * The first set of two notes or more [task] can mean and does not say which of, by their paths; else empty. A set
     * the memory [settled] (`"Laura" is [[Laura Castro]]`, said in the task) is not open (`docs/agent-memory.md`).
     */
    fun notesOf(
        task: String,
        notes: Collection<NoteEntry>,
        settled: List<MemoryLine.Named> = emptyList()
    ): List<RelativePath> {
        val said = wordsOf(text = task)
        if (said.any { it in PLURAL }) return emptyList()

        val named = notes
            .map { it.file.path }
            .filter { it.isMarkdown }
            .map { Named(path = it) }

        return candidatesOf(
            task = task,
            said = said,
            named = named
        )
            .firstOrNull { group ->
                val isOpen =
                    !isToldApart(group = group, said = said) && !isSettled(group, task, settled)

                group.size in 2..MOST_OPTIONS && isOpen
            }
            ?.map { it.path }
            .orEmpty()
    }

    /**
     * The sets of notes [task] could mean that the memory settles (`docs/agent-memory.md`): the note it means and the
     * others, so a turn that picks another is reminded once, and a tool's "several notes share this name" is not said.
     */
    fun settledOf(
        task: String,
        notes: Collection<NoteEntry>,
        settled: List<MemoryLine.Named>
    ): List<Settled> {
        val said = wordsOf(text = task)

        val isPlural = said.any {
            it in PLURAL
        }

        if (settled.isEmpty() || isPlural) return emptyList()

        val named = notes
            .map { it.file.path }
            .filter { it.isMarkdown }
            .map { Named(path = it) }

        return candidatesOf(
            task = task,
            said = said,
            named = named
        )
            .filter { group ->
                group.size in 2..MOST_OPTIONS && !isToldApart(group = group, said = said)
            }
            .mapNotNull { group ->
                val meant = settled.firstNotNullOfOrNull { line ->
                    group.firstOrNull {
                        isMeant(
                            named = it,
                            line = line,
                            task = task
                        )
                    }
                } ?: return@mapNotNull null

                Settled(
                    meant = meant.path,
                    others = group.filter {
                        it != meant
                    }.map { it.path }
                )
            }
            .toList()
    }

    /** A set of notes the memory settled: the one it [meant], and the [others] the task's words could also mean. */
    data class Settled(
        val meant: RelativePath,
        val others: List<RelativePath>
    )

    // The note a line of the memory links, when the task says that line's words and does not qualify them.
    private fun isMeant(
        named: Named,
        line: MemoryLine.Named,
        task: String
    ): Boolean {
        val link = folded(text = line.note.removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION))

        val path = folded(
            text = named
                .path
                .value
                .removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)
        )

        val isLink = if ('/' in link) path == link else named.title == link

        return isLink &&
            MemoryLines.says(text = task, said = line.said) &&
            !isQualified(task = task, said = line.said)
    }

    /**
     * Whether [task] says which one it means right after [said]: "Tomás from the chess club", "Laura Méndez", "la
     * revisión semanal personal". The request wins over the memory (`docs/agent-memory.md`).
     */
    fun isQualified(
        task: String,
        said: String
    ): Boolean {
        val last = wordsOf(text = said).lastOrNull() ?: return false
        val words = task.split(NOT_WORD).filter { it.isNotEmpty() }

        val at = words.indexOfFirst {
            folded(text = it) == last
        }

        val next = words.getOrNull(index = at + 1) ?: return false

        return at >= 0 && (folded(text = next) in QUALIFIERS || next.first().isUpperCase())
    }

    // The memory says which of them the words of the task mean: the note it links is one of the group.
    private fun isSettled(
        group: List<Named>,
        task: String,
        settled: List<MemoryLine.Named>
    ): Boolean = settled.any { named ->
        // A link with its folder ([[Work/Budget 2026]]) means that path; a bare title, the note of that title.
        val link = folded(text = named.note.removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION))

        val isSaid = MemoryLines.says(
            text = task,
            said = named.said
        )

        val isOne = group.any {
            val path = folded(
                text = it
                    .path
                    .value
                    .removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)
            )

            if ('/' in link) path == link else it.title == link
        }

        isSaid &&
            isOne &&
            !isQualified(task = task, said = named.said)
    }

    /**
     * The words of [task] that meant any of [paths] ("Laura", "the budget"), as the task writes them: what a line of
     * the memory says once the person picks one (`docs/agent-memory.md`, "How a line is written"); null when none.
     */
    fun saidOf(
        task: String,
        paths: List<RelativePath>
    ): String? {
        val common = paths
            .map { Named(path = it).words }
            .reduceOrNull { shared, words ->
                shared intersect words
            }
            .orEmpty()

        if (common.isEmpty()) return null

        val the = THE
            .findAll(input = task)
            .firstOrNull {
                folded(text = it.groupValues[THING]) in common
            }

        if (the != null) return the.value

        return task
            .split(NOT_WORD)
            .filter {
                it.isNotEmpty() && folded(text = it) in common
            }
            .distinctBy { folded(text = it) }
            .joinToString(separator = " ")
            .ifEmpty { null }
    }

    /** Whether [text] names each of [paths] by a word only it has: an answer or a change that covers them all. */
    fun coversAll(
        paths: List<RelativePath>,
        text: String
    ): Boolean {
        val group = paths.map { Named(path = it) }
        val said = wordsOf(text = text)

        return group.all { named ->
            val own = named.words - group
                .filter {
                    it != named
                }
                .flatMap { it.words }
                .toSet()

            val whole = named
                .path
                .value
                .removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)

            own.any { it in said } || whole in text
        }
    }

    // Two notes of one name the task says whole; a first name two people have; "the X" several notes are.
    private fun candidatesOf(
        task: String,
        said: List<String>,
        named: List<Named>
    ): Sequence<List<Named>> = sequence {
        named
            .groupBy { it.title }
            .values
            .filter { group ->
                group.size > 1 && group.first().titleWords.all { it in said }
            }
            .forEach { yield(it) }

        // A first name alone: another name in the task ("Tomás … the Japan trip") or "la de…" may settle it.
        val names = FIRST_NAME
            .findAll(input = task)
            .filter {
                it.range.first > 0
            }
            .toList()

        val isAlone = names.size == 1 && !DESCRIBED.containsMatchIn(input = task)

        if (isAlone) {
            val first = folded(text = names.single().value)

            yield(
                named.filter {
                    it.titleWords.firstOrNull() == first
                }
            )
        }

        THE
            .findAll(input = task)
            .filter { isBare(after = task.substring(startIndex = it.range.last + 1)) }
            .map { folded(text = it.groupValues[THING]) }
            .filterNot {
                it in NOT_THINGS || it.endsWith(suffix = "s")
            }
            .forEach { thing ->
                yield(
                    named.filter { note ->
                        val isFolder = note.folderWords.any {
                            it == thing || it == thing + "s"
                        }

                        thing in note.titleWords || isFolder
                    }
                )
            }
    }

    // "the X" said bare: the task ends after it, or after one more word ("the supplier meeting"), or goes on with
    // where it goes ("the project to Archive"); any other word says which X it is ("the project it talks about").
    private fun isBare(after: String): Boolean {
        val next = after
            .split(NOT_WORD)
            .filter { it.isNotEmpty() }

        return next.isEmpty() ||
            next.first().lowercase() in GOES ||
            next.size == 1
    }

    // Told apart when the task says a word of one of them the others do not have: "Laura Castro", "work goals".
    private fun isToldApart(
        group: List<Named>,
        said: List<String>
    ): Boolean = group.any { named ->
        val others = group
            .filter {
                it != named
            }
            .flatMap { it.words }
            .toSet()

        (named.words - others).any {
            it.length >= MIN_WORD && it in said
        }
    }

    private class Named(val path: RelativePath) {
        val title = folded(text = path.nameWithoutExtension)
        val titleWords = wordsOf(text = path.nameWithoutExtension)

        val folderWords = path
            .value
            .substringBeforeLast(
                delimiter = '/',
                missingDelimiterValue = ""
            )
            .let { wordsOf(text = it) }

        val words = (titleWords + folderWords).toSet()
    }

    private fun wordsOf(text: String): List<String> = folded(text = text)
        .split(NOT_WORD)
        .filter { it.isNotEmpty() }

    private fun folded(text: String): String = text
        .lowercase()
        .map {
            ACCENTS[it] ?: it
        }
        .joinToString(separator = "")

    private const val MOST_OPTIONS = 6
    private const val MIN_WORD = 3
    private const val THING = 2

    private val NOT_WORD = Regex(pattern = "[^\\p{L}\\p{N}]+")

    // Bounded by letters, not a word boundary, which the JVM reads as ASCII: "Ángela" and "José" are names too.
    private val FIRST_NAME = Regex(pattern = "(?<![\\p{L}\\p{N}])\\p{Lu}\\p{Ll}+(?![\\p{L}\\p{N}])")

    private val THE = Regex(
        pattern = "\\b(the|el|la|my|mi)\\s+([\\p{L}\\p{N}-]+)",
        option = RegexOption.IGNORE_CASE
    )

    // "Laura, la de estadística", "Tomás, the one from work".
    private val DESCRIBED = Regex(
        pattern = "\\b(la de|el de|la del|el del|la que|el que|the one)\\b",
        option = RegexOption.IGNORE_CASE
    )

    // Words that start saying which one a name means: "from", "de", "who", "at", "personal", "work".
    private val QUALIFIERS = setOf(
        "from", "of", "at", "in", "who", "that", "de", "del", "que", "en", "personal", "work", "trabajo", "home",
        "casa"
    )

    private val GOES = setOf("to", "into", "as", "a", "al", "en", "como")

    private val PLURAL = setOf(
        "all", "every", "each", "both", "todas", "todos", "cada", "ambos", "ambas", "two", "dos"
    )

    private val NOT_THINGS = setOf(
        "note", "nota", "vault", "folder", "carpeta", "task", "tarea", "first", "primera", "primer", "last",
        "ultima", "next", "proxima", "proximo", "same", "other", "otra", "otro", "new", "nueva", "nuevo", "word",
        "palabra", "one", "open", "current"
    )

    private val ACCENTS = mapOf(
        'á' to 'a', 'é' to 'e', 'í' to 'i', 'ó' to 'o', 'ú' to 'u', 'ü' to 'u', 'ñ' to 'n',
        'à' to 'a', 'è' to 'e', 'ç' to 'c', 'ã' to 'a', 'õ' to 'o', 'â' to 'a', 'ê' to 'e',
        'ô' to 'o'
    )
}
