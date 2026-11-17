package app.notekea.chat.domain.loop

import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath

/**
 * The note a task means among several its name fits, told by a word of the task only that note writes: "Tomás from
 * the chess club" is the Tomás tagged club, "my Sunday weekly review" the one held on Sundays. Worked out before the
 * turn, so a small model neither asks which nor answers from the first it reads (the loop's fifth run, 2026-11-16).
 */
internal object ToldApart {
    /** The sets among [groups] that one word of [task] settles, by each note's tags, folder and text ([texts]). */
    fun settledOf(
        task: String,
        groups: List<List<NoteEntry>>,
        texts: Map<RelativePath, String>
    ): List<AmbiguousTask.Settled> = groups.mapNotNull { group ->
        val titles = group
            .flatMap {
                wordsOf(text = it.file.path.nameWithoutExtension)
            }
            .toSet()

        val said = wordsOf(
            text = task.replace(
                regex = QUOTED,
                replacement = " "
            )
        )
            .filter {
                it.length >= MIN_WORD &&
                    it !in COMMON &&
                    it !in titles
            }
            .distinct()

        val written = group.associateWith { note ->
            val holds = note.allTags + note.folders + texts[note.file.path].orEmpty()
            val words = wordsOf(text = holds.joinToString(separator = " ")).toSet()

            said.filter { word ->
                formsOf(word = word).any {
                    it in words
                }
            }
        }

        // Words only one note writes; the set is settled when one note alone has any.
        val own = written.mapValues { (note, words) ->
            words - written
                .filterKeys {
                    it != note
                }
                .values
                .flatten()
                .toSet()
        }

        own
            .entries
            .singleOrNull { it.value.isNotEmpty() }
            ?.let { (meant, words) ->
                AmbiguousTask.Settled(
                    meant = meant.file.path,
                    others = group
                        .filter {
                            it != meant
                        }
                        .map { it.file.path },
                    said = words.first()
                )
            }
    }

    // A word as written, and as its singular or plural: "clubs" is also "club".
    private fun formsOf(word: String): List<String> =
        listOf(word, word + "s", word.removeSuffix(suffix = "s"))

    private fun wordsOf(text: String): List<String> = text
        .lowercase()
        .map {
            ACCENTS[it] ?: it
        }
        .joinToString(separator = "")
        .split(NOT_WORD)
        .filter { it.isNotEmpty() }

    private const val MIN_WORD = 4

    private val NOT_WORD = Regex(pattern = "[^\\p{L}\\p{N}]+")

    // What the task asks to write is no word about which note: 'water the plants twice', "call the bank".
    private val QUOTED = Regex(pattern = "'[^']{2,}'|\"[^\"]+\"|“[^”]+”|«[^»]+»")

    private val ACCENTS = mapOf(
        'á' to 'a',
        'é' to 'e',
        'í' to 'i',
        'ó' to 'o',
        'ú' to 'u',
        'ü' to 'u',
        'ñ' to 'n'
    )

    // Words a task says about the work, not about which note: they tell no note apart.
    private val COMMON = setOf(
        "what", "which", "when", "where", "does", "have", "from", "with", "that", "this", "they", "them", "their",
        "about", "into", "much", "many", "still", "will", "would", "could", "should", "please", "there", "then",
        "than", "also", "only", "just", "over", "under", "after", "before", "between", "each", "every", "some",
        "note", "notes", "task", "tasks", "text", "line", "list", "page", "folder", "file", "vault", "property",
        "tag", "tags", "link", "links", "name", "named", "title", "number", "phone", "email", "address", "date",
        "today", "tomorrow", "week", "month", "year", "time", "make", "made", "write", "written", "create", "move",
        "rename", "delete", "trash", "read", "find", "search", "show", "tell", "give", "know", "need", "want",
        "summarize", "summary", "change", "update", "edit", "append", "remove", "archive", "open", "done", "last",
        "next", "first", "new", "old", "more", "less", "other", "same", "says", "said", "mean", "means", "is",
        "total", "totals", "amount", "count", "suma", "sumar", "cuenta", "cantidad",
        "como", "cual", "cuales", "cuando", "donde", "para", "pero", "este", "esta", "esto", "esos", "esas",
        "todo", "toda", "todos", "todas", "cada", "sobre", "desde", "hasta", "entre", "tiene", "tienen", "hace",
        "hacer", "nota", "notas", "tarea", "tareas", "texto", "linea", "lista", "pagina", "carpeta", "archivo",
        "etiqueta", "etiquetas", "enlace", "nombre", "titulo", "numero", "telefono", "correo", "direccion",
        "fecha", "hoy", "manana", "semana", "mes", "ano", "anade", "anadir", "agrega", "agregar", "escribe",
        "escribir", "crea", "crear", "mueve", "mover", "borra", "borrar", "lee", "leer", "busca", "buscar",
        "muestra", "dime", "cuanto", "cuanta", "cuantos", "cuantas", "quiero", "puedes", "favor", "resumen",
        "resume", "cambia", "cambiar", "quita", "quitar", "nuevo", "nueva", "otro", "otra", "mismo", "misma"
    )
}
