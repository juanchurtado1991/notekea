package app.notekea.core.web.domain

/**
 * The country a web search is made in (`docs/agent-web.md`, "The region"): by its name in English or Spanish, as a
 * request ("in Mexico", "en Colombia") or the memory ("I live in Bogotá, Colombia") says it, to its ISO code.
 */
object Countries {
    /** The ISO code of the first country [text] names, by where it is in the text; null when it names none. */
    fun codeIn(text: String): String? {
        val words = folded(text = text)

        return NAMES
            .entries
            .mapNotNull { (name, code) ->
                positionOf(
                    name = name,
                    words = words
                )?.let {
                    it to code
                }
            }
            .minByOrNull { it.first }
            ?.second
    }

    // Where [name] starts in [words]. A name that is also a common word ("chile relleno", "una cuba de vino") only
    // counts after a preposition, as a place is said: "en Chile", "in the USA"; not after "de", which
    // a food takes as much ("salsa de chile").
    private fun positionOf(
        name: String,
        words: String
    ): Int? {
        val escaped = Regex.escape(literal = name)
        val before = if (name in AMBIGUOUS) "(?:en|in|desde|from|a|to) (?:the )?" else ""
        val pattern = "(?<![a-z])$before$escaped(?![a-z])"

        return Regex(pattern = pattern)
            .find(input = words)
            ?.let {
                it.range.last + 1 - name.length
            }
    }

    /** The name of the country of [code] as a search writes it ("Costa Rica"); null for a code it does not know. */
    fun nameOf(code: String): String? = NAMES
        .entries
        .firstOrNull {
            it.value == code.uppercase()
        }
        ?.key
        ?.split(" ")
        ?.joinToString(separator = " ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }

    private fun folded(text: String): String = text
        .lowercase()
        .map {
            ACCENTS[it] ?: it
        }
        .joinToString(separator = "")

    private val ACCENTS =
        mapOf('á' to 'a', 'é' to 'e', 'í' to 'i', 'ó' to 'o', 'ú' to 'u', 'ñ' to 'n')

    // Bare "usa" is the Spanish verb ("usa la web"), "chile" a food, "cuba" a barrel, "el salvador" a title.
    private val AMBIGUOUS = setOf("usa", "chile", "cuba", "panama", "el salvador")

    private val NAMES = mapOf(
        "colombia" to "CO", "mexico" to "MX", "argentina" to "AR", "chile" to "CL", "peru" to "PE",
        "venezuela" to "VE", "ecuador" to "EC", "bolivia" to "BO", "uruguay" to "UY", "paraguay" to "PY",
        "costa rica" to "CR", "panama" to "PA", "guatemala" to "GT", "honduras" to "HN",
        "el salvador" to "SV", "nicaragua" to "NI", "cuba" to "CU", "republica dominicana" to "DO",
        "dominican republic" to "DO",
        "puerto rico" to "PR", "spain" to "ES", "espana" to "ES", "united states" to "US",
        "estados unidos" to "US", "ee. uu." to "US", "ee.uu." to "US", "eeuu" to "US",
        "usa" to "US", "canada" to "CA", "brazil" to "BR", "brasil" to "BR",
        "united kingdom" to "GB", "reino unido" to "GB", "france" to "FR", "francia" to "FR",
        "germany" to "DE", "alemania" to "DE",
        "italy" to "IT", "italia" to "IT", "portugal" to "PT", "japan" to "JP", "japon" to "JP"
    )
}
