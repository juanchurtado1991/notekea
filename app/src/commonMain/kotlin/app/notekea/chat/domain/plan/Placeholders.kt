package app.notekea.chat.domain.plan

/**
 * Whether a text written from notes holds placeholders where their content should be: "(Insert the pros here)",
 * "replace the examples with the real points", "first open the PDF". A model with nothing to write from fills a note
 * this way (the developer's smoke of 2026-11-14); such a text is never shown as what the notes say.
 */
internal object Placeholders {
    private const val FILL =
        "insertar|inserta|insert|añadir|añade|completar|completa|fill in|pendiente|tbd|to be added"

    private val PATTERNS = listOf(
        Regex(
            pattern = "[(\\[]\\s*($FILL)\\b[^)\\]]{0,80}[)\\]]",
            option = RegexOption.IGNORE_CASE
        ),
        Regex(
            pattern = "lorem ipsum",
            option = RegexOption.IGNORE_CASE
        ),
        Regex(
            pattern = "reemplaza[r]? (los|estos|cada) (ejemplos?|fila)|replace (the|these|each) (examples?|row)",
            option = RegexOption.IGNORE_CASE
        ),
        Regex(
            pattern = "(primero )?debes (abrir|leer) (el|la|tu) (pdf|documento|contrato|nota)|" +
                "you (must|should|need to) (first )?(open|read) (the|your) (pdf|document|contract|note)",
            option = RegexOption.IGNORE_CASE
        ),
        Regex(
            pattern = "se requiere leer|con los puntos reales|with the (real|actual) points",
            option = RegexOption.IGNORE_CASE
        )
    )

    fun isIn(text: String): Boolean = PATTERNS.any { it.containsMatchIn(input = text) }
}
