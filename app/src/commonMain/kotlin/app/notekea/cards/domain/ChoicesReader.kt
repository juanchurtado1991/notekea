package app.notekea.cards.domain

/** Reads the model's JSON of wrong answers; null when the reply is not it. */
fun interface ChoicesReader {
    fun read(json: String): List<String>?
}
