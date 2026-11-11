package app.notekea.chat.eval

import java.io.File
import java.nio.file.Files

/** The committed Sample Vault copied out of Git for a probe, so the user's local edits to it change nothing. */
internal object CommittedVault {
    private const val VAULT = "Sample Vault"

    fun copy(): File {
        val vault = Files
            .createTempDirectory("notekea-probe")
            .resolve(VAULT)
            .toFile()

        val root = "smoke-data/vaults/$VAULT/"

        git("ls-files", "-z", "--", root)
            .decodeToString()
            .split('\u0000')
            .filter { it.isNotEmpty() }
            .forEach { file ->
                val target = File(vault, file.removePrefix(prefix = root))
                target.parentFile.mkdirs()
                target.writeBytes(git("show", "HEAD:$file"))
            }

        return vault
    }

    private fun git(vararg arguments: String): ByteArray {
        val process =
            ProcessBuilder(listOf("git") + arguments).directory(File("..").canonicalFile).start()

        val output = process.inputStream.readBytes()
        check(process.waitFor() == 0) { "git ${arguments.first()} failed" }

        return output
    }
}
