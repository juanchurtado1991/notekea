package app.notekea.chat.domain

/**
 * The language a person wrote in, named for the model ("Spanish"), so every reply is in it however the notes or the
 * plan's rewording of the request are written; null when a message is too short or mixed to tell.
 */
object SpokenLanguage {
    private val WORD = Regex(pattern = "[\\p{L}']+")

    private val COMMON = mapOf(
        "English" to setOf(
            "the", "thanks", "hello", "what", "my", "is", "are", "of", "and", "to", "in", "how", "when", "where",
            "who", "which", "do", "does", "did", "i", "you", "can", "this", "that", "with", "for", "please", "about",
            "notes", "note", "tell", "me", "show", "write", "make", "add", "it", "was", "have", "much", "many"
        ),
        "Spanish" to setOf(
            "que", "qué", "de", "la", "el", "los", "las", "mis", "mi", "una", "un", "por", "para", "con", "hay",
            "es", "esta", "está", "este", "notas", "nota", "tareas", "cuál", "cuáles", "cuanto", "cuánto", "cuántos",
            "cuantos", "cómo", "como", "dónde", "quién", "quien", "y", "del", "lo", "se", "me", "podés", "puedes",
            "decime", "dime", "escribe", "escribí", "hola", "gracias", "sobre", "tengo", "en", "más", "mas"
        ),
        "French" to setOf(
            "le", "les", "des", "est", "je", "j'ai", "que", "qui", "quoi", "sur", "mes", "mon", "ma", "une", "et",
            "dans", "pour", "avec", "voudrais", "savoir", "écrit", "ce", "c'est", "bonjour", "merci", "notes"
        ),
        "Portuguese" to setOf(
            "o", "os", "as", "da", "do", "das", "dos", "um", "uma", "é", "você", "não", "meu", "minha", "sobre",
            "obrigado", "obrigada", "olá", "quais", "qual", "escrevi", "notas", "em", "com", "para"
        ),
        "German" to setOf(
            "der", "die", "das", "ist", "und", "ich", "was", "wie", "mein", "meine", "über", "nicht", "mit", "für",
            "ein", "eine", "habe", "notizen", "danke", "hallo"
        ),
        "Italian" to setOf(
            "il", "lo", "gli", "della", "che", "è", "sono", "mio", "mia", "miei", "le", "una", "per", "con", "cosa",
            "ciao", "grazie", "note", "ho", "scritto"
        )
    )

    // A word only one language says, enough alone: "gracias!", "merci".
    private val OWN = setOf(
        "gracias", "hola", "thanks", "hello", "merci", "bonjour", "obrigado", "obrigada", "olá", "danke", "hallo",
        "grazie", "ciao"
    )

    private val MARKS = mapOf(
        "Spanish" to Regex(pattern = "[¿¡ñ]"),
        "French" to Regex(pattern = "[çœàèêëîïôûù]"),
        "Portuguese" to Regex(pattern = "[ãõ]"),
        "German" to Regex(pattern = "[äöüß]")
    )

    // A language asked for by name ("in Spanish please", "en inglés") wins over the one the message is written in.
    private val ASKED = mapOf(
        "Spanish" to listOf("spanish", "español", "espanol", "castellano", "espagnol", "spanisch"),
        "English" to listOf("english", "inglés", "ingles", "anglais", "englisch"),
        "French" to listOf("french", "francés", "frances", "français", "francais", "französisch"),
        "Portuguese" to listOf("portuguese", "portugués", "portugues", "português", "portugais"),
        "German" to listOf("german", "alemán", "aleman", "deutsch", "allemand"),
        "Italian" to listOf("italian", "italiano", "italien")
    )
    private val ASKING = Regex(pattern = "\\b(in|en|em|auf|na)\\s+(\\p{L}+)")

    /**
     * The language of [text], or when it is too short to tell ("gracias!"), the one of the latest question of
     * [history] that tells (the chat probe, 2026-11-11: a Spanish chat thanked was answered in English).
     */
    fun inChat(
        text: String,
        history: List<ChatExchange>
    ): String? = of(text = text) ?: history
        .asReversed()
        .firstNotNullOfOrNull { of(text = it.question) }

    fun of(text: String): String? {
        val lower = text.lowercase()

        ASKING.findAll(input = lower).forEach { match ->
            val name = match.groupValues[2]

            ASKED
                .entries
                .firstOrNull {
                    name in it.value
                }
                ?.let {
                    return it.key
                }
        }

        val words = WORD
            .findAll(input = lower)
            .map { it.value }
            .toList()

        val scores = COMMON.mapValues { (language, common) ->
            val marks = MARKS[language]?.findAll(input = lower)?.count() ?: 0

            words.count {
                it in common
            } + words.count {
                it in OWN && it in common
            } + marks * MARK_WEIGHT
        }

        val best = scores.maxBy { it.value }
        val isClear = best.value >= MIN_SCORE && scores.count { it.value == best.value } == 1

        return best.key.takeIf { isClear }
    }

    private const val MARK_WEIGHT = 2
    private const val MIN_SCORE = 2
}
