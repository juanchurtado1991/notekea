package app.notekea.chat.domain.language

/**
 * The language the chat replies in: the request's. Spanish is told by its marks (¿, ¡, ñ, accents) or its most common
 * words; anything else is English, the language of the app's strings.
 */
enum class ReplyLanguage {
    ENGLISH,
    SPANISH;

    companion object {
        private val WORD = Regex(pattern = "[\\p{L}]+")
        private val MARKS = Regex(pattern = "[¿¡ñáéíóú]")
        private val SPANISH_WORDS = setOf(
            "que", "qué", "de", "la", "el", "los", "las", "mis", "mi", "una", "un", "por", "para", "con", "hay",
            "esta", "este", "notas", "nota", "tareas", "crea", "créame", "borra", "añade", "agrega", "abre", "muestra",
            "cuál", "cuándo", "cómo", "dónde", "quién", "en", "y", "del", "lo", "se", "mueve", "cambia", "pon"
        )
        private val ENGLISH_WORDS = setOf(
            "the", "what", "my", "is", "are", "of", "and", "to", "in", "a", "an", "how", "when", "where", "who",
            "create", "add", "open", "show", "move", "delete", "tag", "this", "with", "for", "notes", "note", "tasks"
        )

        fun of(text: String): ReplyLanguage {
            val lower = text.lowercase()

            val words = WORD
                .findAll(input = lower)
                .map { it.value }
                .toList()

            val spanish = words.count {
                it in SPANISH_WORDS
            } + MARKS.findAll(input = lower).count()

            val english = words.count {
                it in ENGLISH_WORDS
            }

            return if (spanish > english) SPANISH else ENGLISH
        }
    }
}
