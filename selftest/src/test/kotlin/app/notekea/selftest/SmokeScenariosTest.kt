package app.notekea.selftest

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readBytes
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The Kotlin scenarios build what `smoke-data/smoke.sh` builds, byte for byte and with the same dates, so the
 * developer's smoke data and the testers' are the same. The script needs bash, so the check runs on macOS and Linux.
 */
class SmokeScenariosTest {
    private val smokeData = File("../smoke-data").canonicalFile.toPath()
    private val isWindows = System.getProperty("os.name").startsWith("Windows")

    @Test
    fun `prepare builds what smoke sh prepare builds`() {
        if (isWindows) return
        val scratchName = "scratch-selftest"
        val script = runScript(scratchName = scratchName, command = "prepare")
        val kotlin = Files.createTempDirectory("scenarios").resolve("scratch")
        SmokeScenarios(smokeData = smokeData, scratch = kotlin).prepare()
        assertEquals(tree(root = script), tree(root = kotlin))
        runScript(scratchName = scratchName, command = "clean")
    }

    @Test
    fun `outside changes the Playground Vault as smoke sh outside does`() {
        if (isWindows) return
        val scratchName = "scratch-selftestout"
        runScript(scratchName = scratchName, command = "prepare")
        val script = runScript(scratchName = scratchName, command = "outside")
        val kotlin = Files.createTempDirectory("scenarios").resolve("scratch")
        SmokeScenarios(smokeData = smokeData, scratch = kotlin).apply {
            prepare()
            outside()
        }
        assertEquals(tree(root = script), tree(root = kotlin))
        runScript(scratchName = scratchName, command = "clean")
    }

    @Test
    fun `move, edit and delete change the scratch as another app would`() {
        val scratch = Files.createTempDirectory("scenarios").resolve("scratch")
        val scenarios = SmokeScenarios(smokeData = smokeData, scratch = scratch).apply { prepare() }
        scenarios.editNote(time = "10:00:00")
        val shared = scratch.resolve("Playground Vault/Editor/Shared note.md")
        assertEquals(true, Files.readString(shared).endsWith("\nEdited outside Notekea at 10:00:00.\n"))
        scenarios.deleteNote()
        assertEquals(false, Files.exists(shared))
        scenarios.move()
        assertEquals(
            listOf(false, true),
            listOf("Movable Vault", "Moved Vault").map {
                Files.isDirectory(scratch.resolve(it))
            }
        )
    }

    private fun runScript(
        scratchName: String,
        command: String
    ): Path {
        val process = ProcessBuilder("bash", smokeData.resolve("smoke.sh").toString(), command)
            .apply { environment()["SMOKE_SCRATCH"] = scratchName }
            .redirectErrorStream(true)
            .start()
        process.inputStream.readAllBytes()
        assertEquals(0, process.waitFor(), "smoke.sh $command failed")
        return smokeData.resolve(scratchName)
    }

    // Every file under [root] by its path, with its bytes and, for the dated recipes, its date to the second.
    private fun tree(root: Path): Map<String, String> = Files.walk(root).use { paths ->
        paths.filter { Files.isRegularFile(it) || Files.isDirectory(it) }.toList().associate { path ->
            val relative = root.relativize(path).toString().replace(File.separatorChar, '/')
            val isDated = relative.startsWith("Playground Vault/Recipes/")
            val content = if (Files.isDirectory(path)) "<folder>" else path.readBytes().contentHashCode().toString()
            val date = if (isDated) " " + Files.getLastModifiedTime(path).toMillis() / MILLIS_PER_SECOND else ""
            relative to content + date
        }
    }

    private companion object {
        const val MILLIS_PER_SECOND = 1_000
    }
}
