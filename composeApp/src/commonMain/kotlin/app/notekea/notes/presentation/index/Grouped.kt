package app.notekea.notes.presentation.index

private const val GROUP = 3

/** [count] with its thousands apart: 1,204. */
internal fun grouped(
    count: Int
): String = count.toString().reversed().chunked(size = GROUP).joinToString(separator = ",").reversed()
