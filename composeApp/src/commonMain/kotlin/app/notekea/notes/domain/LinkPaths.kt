package app.notekea.notes.domain

/*
 * Path arithmetic for Markdown links: decoding and encoding their targets, resolving them from a folder ("../" and
 * "./" included) and writing a path relative to a folder. Obsidian encodes only spaces in Markdown links (`%20`), so
 * that is all that is encoded here.
 */

private const val CURRENT = "."
private const val ENCODED_BYTE_LENGTH = 3
private const val ENCODED_SPACE = "%20"
private const val HEX_RADIX = 16
private const val PARENT = ".."
private const val SPACE = " "
private val PERCENT_RUN = Regex(pattern = "(?:%[0-9A-Fa-f]{2})+")

/** Decodes each run of `%XX` sequences as UTF-8 and leaves everything else, malformed sequences included, as it is. */
fun percentDecoded(
    target: String
): String = PERCENT_RUN.replace(input = target) { run ->
    run.value
        .chunked(size = ENCODED_BYTE_LENGTH)
        .map {
            it.drop(n = 1).toInt(radix = HEX_RADIX).toByte()
        }.toByteArray()
        .decodeToString()
}

fun spacesEncoded(
    target: String
): String = target.replace(oldValue = SPACE, newValue = ENCODED_SPACE)

/** [target] read from [folder], or null when it climbs above the vault. */
fun resolvedFrom(
    folder: RelativePath,
    target: String
): RelativePath? {
    val segments = mutableListOf<String>()
    (
        folder.value.split(
            RelativePath.SEPARATOR
        ) + target.split(RelativePath.SEPARATOR)
    ).filter { it.isNotEmpty() }.forEach { segment ->
        when (segment) {
            CURRENT -> Unit
            PARENT -> segments.removeLastOrNull() ?: return null
            else -> segments += segment
        }
    }
    return RelativePath(value = segments.joinToString(separator = RelativePath.SEPARATOR))
}

/** How to reach [target] from [folder], with `../` for every folder to climb. */
fun relativeTo(
    folder: RelativePath,
    target: RelativePath
): String {
    val from = folder.value.split(RelativePath.SEPARATOR).filter { it.isNotEmpty() }
    val to = target.value.split(RelativePath.SEPARATOR)
    val shared = from.zip(to).takeWhile { (a, b) -> a == b }.size
    return (
        List(size = from.size - shared) {
            PARENT
        } + to.drop(n = shared)
    ).joinToString(separator = RelativePath.SEPARATOR)
}
