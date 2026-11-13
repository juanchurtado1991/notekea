package app.notekea.clips.data

import org.jsoup.nodes.Element

/** A page's table as a Markdown one: its first row the header, every row as wide as the widest. */
internal object HtmlTable {
    fun of(table: Element): String {
        val rows = table
            .select("tr")
            .map { row ->
                row
                    .children()
                    .filter {
                        it.normalName() == "th" || it.normalName() == "td"
                    }
                    .map { cellOf(cell = it) }
            }
            .filter { cells ->
                cells.any { it.isNotEmpty() }
            }

        val width = rows.maxOfOrNull { it.size } ?: 0
        val isTable = rows.size >= 2 && width >= 2

        // A table of one column or one row lays a page out: its text is read as paragraphs.
        if (!isTable) {
            return rows
                .flatten()
                .filter { it.isNotEmpty() }
                .joinToString(separator = "\n\n")
        }

        val lines = rows.map { cells ->
            val padded = cells + List(size = width - cells.size) { "" }

            padded.joinToString(
                separator = " | ",
                prefix = "| ",
                postfix = " |"
            )
        }

        val rule = List(size = width) { "---" }.joinToString(
            separator = " | ",
            prefix = "| ",
            postfix = " |"
        )

        return (listOf(lines.first(), rule) + lines.drop(n = 1)).joinToString(separator = "\n")
    }

    private fun cellOf(cell: Element): String = HtmlInline
        .of(node = cell)
        .replace(
            oldValue = "|",
            newValue = "\\|"
        )
}
