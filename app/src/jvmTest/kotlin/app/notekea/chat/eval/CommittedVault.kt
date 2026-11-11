package app.notekea.chat.eval

import app.notekea.chat.domain.ChatScope
import app.notekea.chat.domain.ChatTurn
import app.notekea.chat.domain.FakeChatHistoryStore
import app.notekea.chat.domain.TurnOutcome
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

                // When the note was last committed, so "the last note I wrote" is the vault's, not the copy's.
                git("log", "-1", "--format=%ct", "--", file)
                    .decodeToString()
                    .trim()
                    .toLongOrNull()
                    ?.let { target.setLastModified(it * MILLIS_PER_SECOND) }
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

    /** A chat of [earlier] turns from an earlier session, as the app finds one when [vault] opens again. */
    fun earlierChat(
        vault: File,
        earlier: Int
    ): FakeChatHistoryStore = FakeChatHistoryStore().apply {
        turns[vault.path] = (1..earlier).map {
            ChatTurn(
                question = "How much water do the arepas need, take $it?",
                scope = ChatScope.VAULT,
                outcome = TurnOutcome.ANSWERED,
                answer = EARLIER_ANSWER.repeat(n = EARLIER_REPEATS),
                sources = emptyList(),
                scopeName = null
            )
        }
    }

    private const val EARLIER_ANSWER =
        "You wrote that the arepas need 2½ cups of warm water and rest five minutes. "
    private const val EARLIER_REPEATS = 6
    private const val MILLIS_PER_SECOND = 1_000L
}
