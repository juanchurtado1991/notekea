package app.notekea.chat.data

import app.notekea.chat.domain.ChatScope
import app.notekea.chat.domain.ChatTurn
import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.domain.answer.AnswerSource
import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.notes.domain.RelativePath
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals

/** The chat history on the disk: one file per vault, read back as written, and an empty chat for a damaged file. */
class FileChatHistoryStoreTest {
    private val folder = Files.createTempDirectory("notekea-chat")
    private val store = FileChatHistoryStore(
        folder = folder,
        dispatchers = JvmAppDispatchers()
    )

    private val turn = ChatTurn(
        question = "¿Cuánta agua lleva la masa?",
        scope = ChatScope.FOLDER,
        outcome = TurnOutcome.ANSWERED,
        answer = "Dos tazas y media [1].",
        sources = listOf(
            AnswerSource(
                number = 1,
                note = RelativePath(value = "Recipes/Arepas.md"),
                headingPath = listOf("Arepas", "Ingredients"),
                range = 10..42,
                text = "2½ cups of warm water"
            )
        ),
        scopeName = "Recipes"
    )

    @Test
    fun `each vault keeps its own turns, read back as written, and clearing one leaves the other`() =
        runBlocking {
            store.save(
                vaultPath = VAULT,
                turns = listOf(turn)
            )

            store.save(
                vaultPath = OTHER,
                turns = listOf(turn.copy(outcome = TurnOutcome.NOT_FOUND))
            )

            val saved =
                store.load(vaultPath = VAULT) to store.load(vaultPath = OTHER).map { it.outcome }

            store.clear(vaultPath = VAULT)
            val files = folder.listDirectoryEntries().size

            assertEquals(
                expected = Triple(listOf(turn) to listOf(TurnOutcome.NOT_FOUND), emptyList(), 1),
                actual = Triple(saved, store.load(vaultPath = VAULT), files)
            )
        }

    @Test
    fun `a damaged file is an empty chat, and the next save replaces it`() = runBlocking {
        store.save(
            vaultPath = VAULT,
            turns = listOf(turn)
        )

        folder
            .listDirectoryEntries()
            .single()
            .writeText(text = "{ not json")

        val damaged = store.load(vaultPath = VAULT)

        store.save(
            vaultPath = VAULT,
            turns = listOf(turn)
        )

        assertEquals(
            expected = emptyList<ChatTurn>() to listOf(turn),
            actual = damaged to store.load(vaultPath = VAULT)
        )
    }

    // The macOS smoke of 2026-11-12: two saves at once shared the partial file, and the second found it moved away.
    @Test
    fun `saves at the same time each land, and none fails on the other's partial file`() =
        runBlocking {
            val failures = mutableListOf<String>()

            Logger.setLogWriters(
                object : LogWriter() {
                    override fun log(
                        severity: Severity,
                        message: String,
                        tag: String,
                        throwable: Throwable?
                    ) {
                        synchronized(failures) { failures += message }
                    }
                }
            )

            try {
                (1..SAVES)
                    .map { count ->
                        async {
                            store.save(
                                vaultPath = VAULT,
                                turns = List(size = count) { turn }
                            )
                        }
                    }
                    .awaitAll()
            } finally {
                Logger.setLogWriters(platformLogWriter())
            }

            assertEquals(
                expected = emptyList(),
                actual = failures
            )
        }

    private companion object {
        const val VAULT = "/home/someone/Notes"
        const val OTHER = "/home/someone/Work"
        const val SAVES = 40
    }
}
