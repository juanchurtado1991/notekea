import org.gradle.api.artifacts.MinimalExternalModuleDependency

/**
 * The natives of `net.ladenthin:llama`, as `compose.desktop.currentOs` picks Skiko's: each installer is built on its
 * own system. Every system gets its CPU jar; Windows and Linux on x86-64 also get the Vulkan jar next to it, which
 * puts a chat model on the GPU with nothing but the GPU's driver and falls back to the CPU without one
 * (`docs/spikes/2026-11-07-llm-parallel.md`). macOS's jar is Metal, for Apple silicon; there is none for Intel macOS.
 */
object LlamaNatives {
    private val classifiers = mapOf(
        ("Linux" to "amd64") to listOf("cpu-linux-x86-64", "vulkan-linux-x86-64"),
        ("Linux" to "aarch64") to listOf("cpu-linux-aarch64"),
        ("Windows" to "amd64") to listOf("cpu-windows-x86-64", "vulkan-windows-x86-64"),
        ("Windows" to "aarch64") to listOf("cpu-windows-aarch64"),
        ("Mac" to "aarch64") to listOf("metal-macos-aarch64")
    )

    /** [library]'s natives jars for this machine. */
    fun dependencies(library: MinimalExternalModuleDependency): List<String> = classifiers().map {
        "${library.module}:${library.versionConstraint.requiredVersion}:$it"
    }

    /** The classifiers of this machine's natives; the build stops on a system without them. */
    private fun classifiers(): List<String> {
        val name = System.getProperty("os.name")
        val arch = System.getProperty("os.arch")
        val system = classifiers.keys.firstOrNull { name.startsWith(it.first) && arch == it.second }
        return checkNotNull(classifiers[system]) {
            "net.ladenthin:llama has no natives for $name on $arch"
        }
    }
}
