package app.notekea.selftest

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText

/**
 * The notes of the editor cases (1.5) that `smoke.sh` writes rather than copies: every Markdown element Read mode
 * shows, a note other apps change, and a note with Windows line breaks and frontmatter that must be written back
 * unchanged.
 */
object EditorNotes {
    fun write(folder: Path) {
        Files.createDirectories(folder)
        folder.resolve("Read mode.md").writeText(READ_MODE)
        folder.resolve("Shared note.md")
            .writeText("# Shared note\n\nAnother app changes or deletes this note while it is open in Notekea.\n")
        folder.resolve("Windows line endings.md").writeText(
            "---\r\ntitle: Written on Windows\r\ntags: [crlf]\r\n---\r\n# Windows line endings\r\n\r\n" +
                "Every line ends with CR LF.\r\n"
        )
    }

    private val READ_MODE = """
        ---
        tags: [editor, smoke]
        ---
        # Read mode

        ## Links

        Cook [[Arepas]], see [[Café con leche|the coffee]], open [[A note that does not exist yet]].
        A Markdown link to [Pasta e fagioli](../Recipes/Pasta%20e%20fagioli.md) and to [the web](https://example.com).

        ## Text

        Some **bold**, some *italic*, some `inline code` and a #smoke-tag.

        > A quote with [[Welcome]] inside.

        - [x] A finished task
        - [ ] A task to do

        ## Code

        ```kotlin
        fun servings(people: Int): Int {
            val perPerson = 2 // arepas per person
            return people * perPerson
        }
        ```

        ```python
        def greet(name):
            return f"Hello, {name}"  # [[not a link inside code]]
        ```

    """.trimIndent()
}
