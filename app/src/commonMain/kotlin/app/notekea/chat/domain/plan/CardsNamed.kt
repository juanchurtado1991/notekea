package app.notekea.chat.domain.plan

/**
 * "Make study cards of WordPress" (the developer's smoke of 2026-11-13: the model planned nothing it could do, and
 * agent mode, which makes no cards, wrote a note of its own): a request for study cards is a card step for each note
 * the request names or is about, the closest [MAX_NOTES], whatever the model planned.
 */
internal object CardsNamed {
    const val MAX_NOTES = 5

    private val CARDS = Regex(
        pattern = "(flash ?cards?|study ?cards?|tarjetas?( de (estudio|memoria|repaso))?|fichas? de estudio)",
        option = RegexOption.IGNORE_CASE
    )

    private val WORD = Regex(pattern = "[^\\p{L}\\p{N}]+")

    // What a request for cards says besides its subject.
    private val FILLER = setOf(
        "make", "create", "write", "some", "study", "sttudy", "cards", "card", "flashcards", "flashcard", "from",
        "about", "with", "this", "that", "note", "notes", "please", "them", "crea", "crear", "haz", "hazme",
        "genera", "generar", "unas", "unos", "tarjetas", "tarjeta", "estudio", "memoria", "repaso", "fichas",
        "sobre", "para", "esta", "este", "nota", "notas", "favor", "todas", "todos"
    )

    private const val SHORT_WORD = 2

    fun isAsked(request: String): Boolean = CARDS.containsMatchIn(input = request)

    fun shaped(
        steps: List<PlannedStep>,
        request: String,
        context: ActionContext
    ): List<PlannedStep> {
        if (!isAsked(request = request)) return steps

        val planned = steps.filter {
            it.kind == ActionKind.MAKE_CARDS
        }

        val names = context
            .notes
            .map { it.lowercase() }
            .toSet()

        // A note the model named that the vault has, or the open one: that is the request.
        val isNamed = planned.isNotEmpty() && planned.all { step ->
            val note = step[ActionField.NOTE]?.lowercase().orEmpty()

            note == THIS_NOTE ||
                note in names ||
                names.any { it.endsWith(suffix = "/$note") }
        }

        if (isNamed) return planned

        val about = notesAbout(
            request = request,
            context = context
        )

        if (about.isEmpty()) return planned.ifEmpty { steps }

        return about.map {
            PlannedStep(
                kind = ActionKind.MAKE_CARDS,
                fields = mapOf(ActionField.NOTE to it)
            )
        }
    }

    // The notes whose name holds a word of the request's subject, the ones named after it first.
    private fun notesAbout(
        request: String,
        context: ActionContext
    ): List<String> {
        val subject = CARDS
            .replace(
                input = request,
                replacement = " "
            )
            .lowercase()
            .split(WORD)
            .filter {
                it.length > SHORT_WORD && it !in FILLER
            }

        if (subject.isEmpty()) return emptyList()

        return context
            .notes
            .map { note ->
                val title = note.substringAfterLast(delimiter = '/').lowercase()
                val path = note.lowercase()

                val inTitle = subject.count {
                    it in title
                }

                val inPath = subject.count {
                    it in path
                }

                note to inTitle * IN_TITLE + inPath
            }
            .filter { (_, score) ->
                score > 0
            }
            .sortedWith(CLOSEST)
            .take(n = MAX_NOTES)
            .map { it.first }
    }

    private const val IN_TITLE = 10

    // The most of the subject's words first, then the shortest name: the note about it, not one that mentions it.
    private val CLOSEST =
        compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first.length }
    private const val THIS_NOTE = "this note"
}
