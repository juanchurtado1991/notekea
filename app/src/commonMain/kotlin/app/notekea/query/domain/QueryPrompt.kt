package app.notekea.query.domain

import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole

/**
 * What the model is given to turn a request in plain words into a query (3.8): the vault's properties with what they
 * hold, its folders and tags, today's date and the request; the reply is JSON shaped by [SCHEMA], conditions only those
 * the query tab offers.
 */
object QueryPrompt {
    private val CONDITIONS =
        QueryCondition.entries.joinToString(separator = ",") { "\"${it.written}\"" }

    val SCHEMA =
        "{\"type\":\"object\",\"properties\":{\"filters\":{\"type\":\"array\",\"maxItems\":5,\"items\":" +
            "{\"type\":\"object\",\"properties\":{\"property\":{\"type\":\"string\"},\"condition\":{\"type\":" +
            "\"string\",\"enum\":[$CONDITIONS]},\"value\":{\"type\":\"string\"}},\"required\":[\"property\"," +
            "\"condition\",\"value\"]}},\"sort\":{\"type\":\"string\"},\"descending\":{\"type\":\"boolean\"}}," +
            "\"required\":[\"filters\"]}"

    private const val INSTRUCTIONS =
        """You turn a request for notes into a query of their properties, as JSON.
Each filter has a property (one of the vault's, or "" for "in folder" and "has tag"), a condition and a value.
Conditions: is, contains, more than, less than (numbers), before, after (dates YYYY-MM-DD), is empty (value ""),
in folder (value: a folder), has tag (value: a tag without #). "more than" and "less than" are strict: "4 or more" is
"more than 3", "in 2026" is after 2025-12-31 and before 2027-01-01. A checkbox is "true" or "false".
"sort" names a property to order by ("" for none), "descending" true for the highest first. Use only the properties,
folders and tags listed, never others (there is no property "title" or "name" unless listed); a word that names a
note or a topic is not a filter. The request may be in another language than the vault: match its words to them by meaning
("libros" is a folder Books or a tag book, "calificación", "nota" or "estrellas" is rating, "terminado" is finished).
Example: "libros con más de 3" -> {"filters":[{"property":"","condition":"in folder","value":"Books"},
{"property":"rating","condition":"more than","value":"3"}]}"""

    fun messages(
        request: String,
        catalog: QueryCatalog,
        today: String
    ): List<ChatMessage> = listOf(
        ChatMessage(
            role = ChatRole.SYSTEM,
            text = INSTRUCTIONS
        ),
        ChatMessage(
            role = ChatRole.USER,
            text = "Today: $today\nProperties: " +
                catalog.keys.joinToString { "${it.name} (${it.kind.name.lowercase()})" } +
                "\nFolders: ${catalog.folders.joinToString()}" +
                "\nTags: ${catalog.tags.joinToString()}\n\nRequest: $request"
        )
    )
}
