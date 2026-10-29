package app.notekea.selftest

import java.io.File

/** The cases of `MVP_SMOKE_TEST.md`: each row's ID and its **Checked by**, the sixth cell from the end. */
object SmokeCaseList {
    private val row = Regex(pattern = """^\| (G-\d+|\d+\.\d+-\d+) \|""")
    private val cellSeparator = Regex(pattern = """(?<!\\)\|""")
    private const val CHECKED_BY_FROM_END = 6

    fun read(file: File): List<SmokeCase> = file.readLines().filter { row.containsMatchIn(it) }.map { line ->
        val cells = line.split(cellSeparator)
        SmokeCase(id = cells[1].trim(), checkedBy = cells[cells.size - CHECKED_BY_FROM_END].trim())
    }
}
