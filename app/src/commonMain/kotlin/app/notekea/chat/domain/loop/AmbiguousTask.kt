package app.notekea.chat.domain.loop

import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath

/**
 * The notes a task's words fit equally and that it does not tell apart (`docs/agent-loop.md`, "Why the loop fails",
 * item 3): two notes of one name, two people of one first name, "the X" where several notes are an X.
 */
internal object AmbiguousTask {
    /** The first set of two notes or more [task] can mean and does not say which of, by their paths; else empty. */
    fun notesOf(
        task: String,
        notes: Collection<NoteEntry>
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
                group.size in 2..MOST_OPTIONS && !isToldApart(group = group, said = said)
            }
            ?.map { it.path }
            .orEmpty()
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
    private val FIRST_NAME = Regex(pattern = "\\b\\p{Lu}\\p{Ll}+\\b")

    private val THE = Regex(
        pattern = "\\b(the|el|la|my|mi)\\s+([\\p{L}\\p{N}-]+)",
        option = RegexOption.IGNORE_CASE
    )

    // "Laura, la de estadística", "Tomás, the one from work".
    private val DESCRIBED = Regex(
        pattern = "\\b(la de|el de|la del|el del|la que|el que|the one)\\b",
        option = RegexOption.IGNORE_CASE
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
