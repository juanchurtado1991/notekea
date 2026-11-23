package app.notekea.chat.domain.tool

/**
 * The JSON the loop exchanges with the model under a schema: its replies are one flat object, read as [fields], and
 * a text of the vault's own that a schema names (a skill) is written as [quoted].
 */
interface ModelJson {
    /**
     * The fields of the flat object [json] is, each as a text: a list one item a line, a number or `true` as
     * written. Null when it is no object, or one cut short.
     */
    fun fields(json: String): Map<String, String>?

    /** [text] as a JSON string, with its quotes and its escapes. */
    fun quoted(text: String): String
}
