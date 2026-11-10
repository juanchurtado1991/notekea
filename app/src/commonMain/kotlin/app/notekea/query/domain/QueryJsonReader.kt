package app.notekea.query.domain

/** Reads the query the model wrote as JSON ([QueryPrompt.SCHEMA]), as written: names not checked yet. */
fun interface QueryJsonReader {
    fun read(json: String): VaultQuery?
}
