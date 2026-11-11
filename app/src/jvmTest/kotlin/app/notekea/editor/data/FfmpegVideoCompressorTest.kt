package app.notekea.editor.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.editor.domain.CompressStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The compressor with a stand-in for FFmpeg, a shell script (bug hunt, 2026-11-11): a copy that does not fit or is
 * cancelled is deleted, and a cancelled FFmpeg stops instead of writing on its own. Skipped on Windows, with no shell.
 */
class FfmpegVideoCompressorTest {
    private val root = Files.createTempDirectory("ffmpeg-compressor")
    private val work = root.resolve("work")
    private val source = root.resolve("clip.mov").also { Files.writeString(it, "a video") }

    @Test
    fun `a copy that never fits is deleted, and the compression says it failed`() = whereShellRuns {
        val compressor = compressorWith(script = TOO_BIG)

        val steps = runBlocking {
            compressor
                .compress(
                    source = source.toString(),
                    maxBytes = MAX_BYTES
                )
                .toList()
        }

        assertEquals(
            expected = listOf<Any>(true, emptyList<Path>()),
            actual = listOf(steps.last() is CompressStep.Failed, copiesLeft())
        )
    }

    @Test
    fun `a cancelled compression stops FFmpeg and leaves no copy`() = whereShellRuns {
        val compressor = compressorWith(script = SLOW_THEN_WRITES)

        runBlocking {
            compressor
                .compress(
                    source = source.toString(),
                    maxBytes = MAX_BYTES
                )
                .first {
                    it is CompressStep.Compressing
                }
        }

        // The stand-in writes a second line, then its copy a second later: left running, the copy would appear.
        Thread.sleep(LATER_MILLIS)

        assertEquals(
            expected = emptyList(),
            actual = copiesLeft()
        )
    }

    private fun whereShellRuns(test: () -> Unit) {
        if (File.separatorChar == '\\') return
        test()
    }

    private fun compressorWith(script: String): FfmpegVideoCompressor {
        val tools = root.resolve("tools")
        val installed = Files.createDirectories(tools.resolve(FfmpegTool.VERSION))
        val program = installed.resolve(PROGRAM)

        Files.writeString(program, script)
        assertTrue(actual = program.toFile().setExecutable(true))
        Files.writeString(installed.resolve(INSTALLED_MARK), "stand-in")

        return FfmpegVideoCompressor(
            installer = FfmpegInstaller(
                folder = tools,
                tool = FfmpegTool(
                    folder = "stand-in",
                    sha256 = "",
                    size = 0,
                    program = PROGRAM
                )
            ),
            workFolder = work,
            dispatchers = FakeAppDispatchers(dispatcher = Dispatchers.IO)
        )
    }

    private fun copiesLeft(): List<Path> = if (Files.isDirectory(work)) {
        work.listDirectoryEntries()
    } else {
        emptyList()
    }

    private companion object {
        const val LATER_MILLIS = 2_500L
        const val MAX_BYTES = 1_000L
        const val PROGRAM = "ffmpeg"

        // The file FfmpegInstaller writes once FFmpeg is unpacked.
        const val INSTALLED_MARK = "installed"

        // The last argument is the copy; "-i" alone asks for the length.
        const val TOO_BIG = """#!/bin/sh
for last; do :; done
case "$*" in
  *-progress*) head -c 5000 /dev/zero > "${'$'}last"; echo "out_time_us=5000000" ;;
  *) echo "  Duration: 00:00:10.00, start: 0.000000" ;;
esac
"""

        const val SLOW_THEN_WRITES = """#!/bin/sh
for last; do :; done
case "$*" in
  *-progress*)
    echo "out_time_us=1000000"
    sleep 0.3
    echo "progress=continue"
    sleep 1
    mkdir -p "${'$'}(dirname "${'$'}last")"
    head -c 500 /dev/zero > "${'$'}last"
    ;;
  *) echo "  Duration: 00:00:10.00, start: 0.000000" ;;
esac
"""
    }
}
