import org.gradle.api.artifacts.MinimalExternalModuleDependency

/**
 * The natives of `net.ladenthin:llama`, one jar per system, as `compose.desktop.currentOs` picks Skiko's: each installer
 * is built on its own system. There is none for Intel macOS, and the macOS installer is built for Apple silicon.
 */
object LlamaNatives {
    private val classifiers = mapOf(
        ("Linux" to "amd64") to "cpu-linux-x86-64",
        ("Linux" to "aarch64") to "cpu-linux-aarch64",
        ("Windows" to "amd64") to "cpu-windows-x86-64",
        ("Windows" to "aarch64") to "cpu-windows-aarch64",
        ("Mac" to "aarch64") to "metal-macos-aarch64"
    )

    /** [library]'s natives jar for this machine. */
    fun dependency(library: MinimalExternalModuleDependency): String =
        "${library.module}:${library.versionConstraint.requiredVersion}:${classifier()}"

    /** The classifier of this machine's natives; the build stops on a system without them. */
    private fun classifier(): String {
        val name = System.getProperty("os.name")
        val arch = System.getProperty("os.arch")
        val system = classifiers.keys.firstOrNull { name.startsWith(it.first) && arch == it.second }
        return checkNotNull(classifiers[system]) {
            "net.ladenthin:llama has no natives for $name on $arch"
        }
    }
}
