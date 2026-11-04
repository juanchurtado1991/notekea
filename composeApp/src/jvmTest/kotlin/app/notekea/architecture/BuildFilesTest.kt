package app.notekea.architecture

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/** What a module's build file leaves to the `notekea.jvm` convention plugin, so no two modules can drift apart. */
class BuildFilesTest {
    @Test
    fun `module build files leave the shared setup to the convention plugin`() {
        val modules = File(ROOT).listFiles().orEmpty()
            .filterNot { it.name == BUILD_SRC }
            .map { File(it, BUILD_FILE) }
            .filter { it.isFile }
        val repeated = modules.flatMap { file ->
            val text = file.readText()
            SHARED_SETUP.filter { it in text }.map { "${file.parentFile.name}: $it" }
        }
        assertEquals(expected = emptyList(), actual = repeated)
        assertEquals(expected = MODULES, actual = modules.count { PLUGIN in it.readText() })
    }

    private companion object {
        const val BUILD_FILE = "build.gradle.kts"
        const val BUILD_SRC = "buildSrc"
        const val MODULES = 5
        const val PLUGIN = "id(\"notekea.jvm\")"
        const val ROOT = ".."
        val SHARED_SETUP = listOf(
            "jvmToolchain",
            "allWarningsAsErrors",
            "ktlintRuleset",
            "ignoreFailures",
            "--enable-native-access",
            "checkRuntimeModules",
            "0.1.0-alpha"
        )
    }
}
