package app.notekea.chat.data

import app.notekea.chat.domain.tool.ModelJson
import com.ghost.serialization.Ghost
import com.ghost.serialization.exception.GhostJsonException
import com.ghost.serialization.types.RawJson
import com.ghost.serialization.types.RawJsonKind

/** [ModelJson] over Ghost. What the model writes before the object's `{` or after its `}` is left out. */
class GhostModelJson : ModelJson {
    override fun fields(json: String): Map<String, String>? {
        val from = json.indexOf(char = '{')
        val to = json.lastIndexOf(char = '}')
        if (from < 0 || to < from) return null

        return try {
            Ghost
                .deserialize<Map<String, RawJson>>(json.substring(from, to + 1).encodeToByteArray())
                .mapValues { textOf(value = it.value) }
        } catch (_: GhostJsonException) {
            null
        }
    }

    override fun quoted(text: String): String = Ghost.serialize(value = text)

    // A list is kept one item a line, as the loop reads it; anything else as the text it is or was written as.
    private fun textOf(value: RawJson): String = when (value.kind()) {
        RawJsonKind.ARRAY ->
            Ghost
                .deserialize<List<RawJson>>(value.bytes)
                .joinToString(separator = "\n") { textOf(value = it) }

        // Not RawJson's own text: with an escape in it, Ghost 1.3.5 reads each byte of a letter like "á" as a letter.
        RawJsonKind.STRING -> Ghost.deserialize<String>(value.bytes)

        else -> value.decodeToString()
    }
}
