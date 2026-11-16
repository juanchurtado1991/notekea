package app.notekea.core.web.domain

/**
 * The country a web search is made in (`docs/agent-web.md`, "The region"): by its name in English or Spanish, as a
 * request ("in Mexico", "en Colombia") or the memory ("I live in Bogotá, Colombia") says it, to its ISO code.
 */
object Countries {
    /** The ISO code of the first country [text] names; null when it names none. */
    fun codeIn(text: String): String? {
        val words = folded(text = text)

        return NAMES
            .entries
            .firstOrNull { (name, _) ->
                Regex(pattern = "\\b$name\\b").containsMatchIn(input = words)
            }
            ?.value
    }

    private fun folded(text: String): String = text
        .lowercase()
        .map {
            ACCENTS[it] ?: it
        }
        .joinToString(separator = "")

    private val ACCENTS =
        mapOf('á' to 'a', 'é' to 'e', 'í' to 'i', 'ó' to 'o', 'ú' to 'u', 'ñ' to 'n')

    private val NAMES = mapOf(
        "colombia" to "CO", "mexico" to "MX", "argentina" to "AR", "chile" to "CL", "peru" to "PE",
        "venezuela" to "VE", "ecuador" to "EC", "bolivia" to "BO", "uruguay" to "UY", "paraguay" to "PY",
        "costa rica" to "CR", "panama" to "PA", "guatemala" to "GT", "honduras" to "HN",
        "el salvador" to "SV", "nicaragua" to "NI", "cuba" to "CU", "republica dominicana" to "DO",
        "dominican republic" to "DO",
        "puerto rico" to "PR", "spain" to "ES", "espana" to "ES", "united states" to "US",
        "estados unidos" to "US", "usa" to "US", "canada" to "CA", "brazil" to "BR", "brasil" to "BR",
        "united kingdom" to "GB", "reino unido" to "GB", "france" to "FR", "francia" to "FR", "germany" to "DE", "alemania" to "DE",
        "italy" to "IT", "italia" to "IT", "portugal" to "PT", "japan" to "JP", "japon" to "JP"
    )
}
