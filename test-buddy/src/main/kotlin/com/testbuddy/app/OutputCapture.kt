package com.testbuddy.app

import java.io.FileOutputStream
import java.io.OutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path

/**
 * Copies what the app prints (its output and errors) into `file` as well, so a tester sends it from the Report folder
 * without opening a terminal; the console still gets it.
 */
internal object OutputCapture {
    fun start(file: Path) {
        Files.createDirectories(file.parent)
        val copy = FileOutputStream(file.toFile(), true)
        System.setOut(PrintStream(Both(first = System.out, second = copy), true))
        System.setErr(PrintStream(Both(first = System.err, second = copy), true))
    }

    private class Both(
        private val first: OutputStream,
        private val second: OutputStream
    ) : OutputStream() {
        override fun write(byte: Int) {
            first.write(byte)
            second.write(byte)
        }

        override fun write(
            bytes: ByteArray,
            offset: Int,
            length: Int
        ) {
            first.write(bytes, offset, length)
            second.write(bytes, offset, length)
        }

        override fun flush() {
            first.flush()
            second.flush()
        }
    }
}
