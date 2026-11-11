package app.notekea.core.settings.domain

/** The setting a text names ("¿dónde pongo el token?" → [SettingsPlace.SYNC_TOKEN]), by the longest of its words. */
object SettingsPlaces {
    private val ACCENTS =
        mapOf('á' to 'a', 'é' to 'e', 'í' to 'i', 'ó' to 'o', 'ú' to 'u', 'ñ' to 'n')
    private val MARKS = Regex(pattern = "[¿?¡!.,:;\"'()]")
    private val SPACES = Regex(pattern = "\\s+")

    /** The place [text] names, or null; on a tie, one of [topic], the section open. */
    fun named(
        text: String,
        topic: SettingsTopic? = null
    ): SettingsPlace? {
        val plain = plain(text = text)

        return SettingsPlace
            .entries
            .map { place ->
                val score = place.words.maxOf { word ->
                    val wanted = plain(text = word)
                    if (wanted in plain) wanted.length else 0
                }

                place to score
            }
            .filter { (_, score) ->
                score > 0
            }
            .maxWithOrNull(
                compareBy<Pair<SettingsPlace, Int>> { (_, score) -> score }
                    .thenBy { (place, _) ->
                        place.topic == topic
                    }
            )
            ?.first
    }

    // " lowercase words without accents or marks ", so a word matches only whole.
    private fun plain(text: String): String = text
        .lowercase()
        .map {
            ACCENTS[it] ?: it
        }
        .joinToString(separator = "")
        .replace(MARKS, " ")
        .replace(SPACES, " ")
        .trim()
        .let { " $it " }
}
