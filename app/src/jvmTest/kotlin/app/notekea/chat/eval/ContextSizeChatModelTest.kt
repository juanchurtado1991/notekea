package app.notekea.chat.eval

import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import java.io.File
import kotlin.test.Test

/**
 * How much the chat model reads in one call (`docs/agent-loop.md`, step 0): a document of numbered clauses is asked
 * about its second and last one, with the same context split among 4, 2 or 1 slots, or shared by 4. A tool to
 * read, not a pass or fail: it writes `build/reports/context-size.md` and `context-shared.md`.
 */
class ContextSizeChatModelTest {
    @Test
    fun `the model is asked about documents of growing length, with the context split in fewer slots`() {
        assumeTrue(RawChatModel.isInstalled)

        val report = StringBuilder("# Context size (medium)\n\n| slots | shared | clauses | ")

        report.append("characters | prompt tokens | seconds | resident MB | reply |\n")
        report.append("|---|---|---|---|---|---|---|---|\n")

        SETUPS.forEach { (slots, isShared) ->
            RawChatModel(
                slots = slots,
                isShared = isShared
            ).use { model ->
                CLAUSES.forEach { clauses ->
                    val reply = model.asked(clauses = clauses)

                    report.append("| $slots | $isShared | $clauses | ${document(clauses).length} | ")
                    report.append("${reply.promptTokens} | ${reply.millis / MILLIS} | ")
                    report.append("${residentMegabytes()} | ${reply.text} |\n")
                }
            }
        }

        written(
            name = "context-size.md",
            text = report.toString()
        )
    }

    @Test
    fun `four calls side by side read their documents, with a context of their own or a shared one`() {
        assumeTrue(RawChatModel.isInstalled)

        val report = StringBuilder("# Four slots side by side (medium)\n\n| shared | clauses of ")

        report.append("each call | prompt tokens | seconds each | replies |\n")
        report.append("|---|---|---|---|---|\n")

        listOf(false, true).forEach { isShared ->
            RawChatModel(
                slots = SIDE_BY_SIDE,
                isShared = isShared
            ).use { model ->
                TOGETHER.forEach { clauses ->
                    val replies = runBlocking(context = Dispatchers.IO) {
                        clauses.map {
                            async { model.asked(clauses = it) }
                        }.awaitAll()
                    }

                    report.append("| $isShared | $clauses | ${replies.map { it.promptTokens }} | ")
                    report.append("${replies.map { it.millis / MILLIS }} | ")
                    report.append("${replies.joinToString(separator = " / ") { it.text }} |\n")
                }
            }
        }

        written(
            name = "context-shared.md",
            text = report.toString()
        )
    }

    private fun RawChatModel.asked(clauses: Int): RawReply {
        val reply = reply(
            messages = listOf(
                ChatMessage(
                    role = ChatRole.SYSTEM,
                    text = "Answer from the document only, in one short sentence."
                ),
                ChatMessage(
                    role = ChatRole.USER,
                    text = "${document(clauses)}\n\nWhat is the fee for service 2 and for service $clauses?"
                )
            ),
            maxTokens = REPLY_TOKENS
        )

        return reply.copy(
            text = reply.text.replace(
                oldChar = '\n',
                newChar = ' '
            )
        )
    }

    private fun document(clauses: Int): String = (1..clauses)
        .joinToString(separator = "\n") {
            "Clause $it: the fee for service $it is ${FEE + it} dollars a month."
        }

    private fun written(
        name: String,
        text: String
    ) {
        File("build/reports/$name").run {
            parentFile.mkdirs()
            writeText(text = text)
        }
    }

    private fun residentMegabytes(): Long = ProcessBuilder(
        "ps",
        "-o",
        "rss=",
        "-p",
        ProcessHandle
            .current()
            .pid()
            .toString()
    )
        .start()
        .inputStream
        .bufferedReader()
        .readText()
        .trim()
        .toLongOrNull()
        ?.div(other = KILOBYTES) ?: 0

    private companion object {
        // The same memory for the context each time (16,384 tokens): split among fewer slots, or shared by four.
        val SETUPS = listOf(4 to false, 2 to false, 1 to false, 4 to true)
        val CLAUSES = listOf(100, 220, 450, 900)

        // Four short calls, a long one alone once every slot holds a call's cache, two long ones that fit
        // together, four that do not, and a long one beside short ones that do not fit with it.
        val TOGETHER = listOf(
            listOf(100, 100, 100, 100),
            listOf(450),
            listOf(220, 220),
            listOf(220, 220, 220, 220),
            listOf(450, 100, 100, 100)
        )

        const val SIDE_BY_SIDE = 4
        const val FEE = 1_000
        const val REPLY_TOKENS = 60
        const val MILLIS = 1_000.0
        const val KILOBYTES = 1_024L
    }
}
