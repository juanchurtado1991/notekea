package app.notekea.core.notes.domain.links

import app.notekea.core.notes.domain.RelativePath

/*
 * Path arithmetic for Markdown links: decoding and encoding their targets, resolving them from a folder ("../" and
 * "./" included) and writing a path relative to a folder. Obsidian encodes spaces in Markdown links (`%20`); what
 * would break a link's target (`#`, parentheses, `%`) is encoded too.
 */

private const val CURRENT = "."
private const val ENCODED_BYTE_LENGTH = 3
private const val ENCODED_SPACE = "%20"

// What would end a Markdown link's target early or be read as part of it: "%" first, so the others' own "%"
// is not encoded again. A note renamed "C# (draft)" broke every link to it (the code review of 2026-11-13).
private val ENCODED = linkedMapOf(
    "%" to "%25",
    " " to ENCODED_SPACE,
    "#" to "%23",
    "(" to "%28",
    ")" to "%29"
)
private const val HEX_RADIX = 16
private const val PARENT = ".."
private const val SPACE = " "
private val PERCENT_RUN = Regex(pattern = "(?:%[0-9A-Fa-f]{2})+")

/** Decodes each run of `%XX` sequences as UTF-8 and leaves everything else, malformed sequences included, as it is. */
fun percentDecoded(target: String): String = PERCENT_RUN
    .replace(input = target) { run ->
        run
            .value
            .chunked(size = ENCODED_BYTE_LENGTH)
            .map {
                it
                    .drop(n = 1)
                    .toInt(radix = HEX_RADIX)
                    .toByte()
            }
            .toByteArray()
            .decodeToString()
    }

fun spacesEncoded(target: String): String = ENCODED
    .entries
    .fold(initial = target) { text, (plain, encoded) ->
        text.replace(
            oldValue = plain,
            newValue = encoded
        )
    }

/** [target] read from [folder], or null when it climbs above the vault. */
fun resolvedFrom(
    folder: RelativePath,
    target: String
): RelativePath? {
    val segments = mutableListOf<String>()
    val parts = folder.value.split(RelativePath.SEPARATOR) + target.split(RelativePath.SEPARATOR)

    parts.filter { it.isNotEmpty() }.forEach { segment ->
        when (segment) {
            CURRENT -> Unit
            PARENT -> segments.removeLastOrNull() ?: return null
            else -> segments += segment
        }
    }

    return RelativePath(value = segments.joinToString(separator = RelativePath.SEPARATOR))
}

/**
 * Where a wiki link's [target] points in the vault, without `.md`: from [source]'s folder when it starts with `./` or
 * `../`, otherwise from the root, a leading `/` or not; null when it climbs above the vault.
 */
fun wikiPath(
    target: String,
    source: RelativePath
): RelativePath? {
    val written = LinkResolver.withoutMarkdownExtension(target = target.trim())
    val first = written.substringBefore(delimiter = RelativePath.SEPARATOR)
    val isRelative = first == CURRENT || first == PARENT

    return resolvedFrom(
        folder = if (isRelative) source.parent ?: RelativePath.ROOT else RelativePath.ROOT,
        target = written
    )
}

/** How to reach [target] from [folder], with `../` for every folder to climb. */
fun relativeTo(
    folder: RelativePath,
    target: RelativePath
): String {
    val from = folder
        .value
        .split(RelativePath.SEPARATOR)
        .filter { it.isNotEmpty() }

    val to = target.value.split(RelativePath.SEPARATOR)

    val shared = from
        .zip(to)
        .takeWhile { (a, b) ->
            a == b
        }
        .size

    val climbs = List(size = from.size - shared) { PARENT }

    return (climbs + to.drop(n = shared)).joinToString(separator = RelativePath.SEPARATOR)
}
