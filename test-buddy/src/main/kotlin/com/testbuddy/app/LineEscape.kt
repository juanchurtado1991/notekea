package com.testbuddy.app

/**
 * A text kept on one line, of a file or of a process's output, and read back: a backslash, a tab and both line
 * breaks are written as `\\`, `\t`, `\n` and `\r`. The code review of 2026-11-22: each writer had its own copy, and
 * neither escaped `\r`, which every reader of lines splits on, so a message quoting a Windows file was cut.
 */
internal object LineEscape {
    fun escape(text: String): String = text
        .replace("\\", "\\\\")
        .replace("\t", "\\t")
        .replace("\n", "\\n")
        .replace("\r", "\\r")

    fun unescape(text: String): String = buildString {
        var index = 0

        while (index < text.length) {
            val char = text[index]

            if (char == '\\' && index + 1 < text.length) {
                append(
                    when (text[index + 1]) {
                        't' -> '\t'
                        'n' -> '\n'
                        'r' -> '\r'
                        else -> text[index + 1]
                    }
                )

                index += 2
            } else {
                append(char)
                index++
            }
        }
    }
}
