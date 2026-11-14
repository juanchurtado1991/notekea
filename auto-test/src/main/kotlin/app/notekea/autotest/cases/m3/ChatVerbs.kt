package app.notekea.autotest.cases.m3

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import app.notekea.autotest.cases.m1.AREPAS_FLOUR
import app.notekea.autotest.cases.m1.PLAYGROUND
import app.notekea.autotest.cases.m1.WELCOME
import app.notekea.autotest.cases.m1.openPlaygroundNote
import app.notekea.autotest.notekea
import app.notekea.autotest.openVault
import app.notekea.autotest.pressInWindow
import app.notekea.chat.domain.language.SpokenLanguage
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.ShortcutModifier.Mod
import kotlin.time.Duration.Companion.minutes

private val HEADER = Regex(pattern = "^(Scope|Outcome|Answered with): ")
internal const val QUESTION_FIELD = "Ask about your notes…"
internal const val NOT_FOUND = "I couldn't find this in your notes."
internal const val WATER = "How much water does the arepa dough take?"
internal const val REST = "How long does the dough rest?"
internal const val CHECKLIST = "What is in the packing checklist?"
internal const val SUMMARY = "Summarize my Lisbon trip"

// Arepas takes 2 1/2 cups of water and rests 5 minutes: the ways a model writes it, in English and Spanish.
internal val TWO_AND_A_HALF =
    arrayOf("2 1/2", "2½", "2.5", "two and a half", "dos tazas y media", "2 tazas y media")
internal val FIVE_MINUTES = arrayOf("5 minutes", "five minutes", "5 minutos", "cinco minutos")
private const val STOP = "Stop"
private const val MORE = "More"
private const val COPY_CHAT = "Copy chat"
private const val START_MILLIS = 10_000L

// A reply on a laptop's CPU: searching, reading and writing take minutes at worst.
private const val REPLY_MILLIS = 4L * 60_000L

/** How long a case that asks the model a few times may take. */
internal val MODEL_CASE_TIMEOUT = 15.minutes

/**
 * Puts the machine's chat model in use, opens the Playground Vault, with Recipes/Arepas when [withArepas], and shows
 * the chat (Mod+L), its question field ready.
 */
internal fun CaseScope.openChat(withArepas: Boolean = true) {
    notekea().useChatModel()

    if (withArepas) {
        openPlaygroundNote(
            name = "Arepas",
            shownText = AREPAS_FLOUR
        )
    } else {
        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )
    }

    pressInWindow(shortcut = Mod + Key.L)
    expectText(text = QUESTION_FIELD)
}

/** Chooses [mode] in the menu under the chat's field, with Medium in use (it reads "Medium · Auto" until then). */
internal fun CaseScope.chooseMode(mode: String) {
    click(text = "Medium · Auto")
    click(text = mode)
    expectText(text = "Medium · $mode")
}

/** Asks [question] in the chat's field and waits until its reply is whole (the Stop button is Send again). */
internal fun CaseScope.ask(question: String) {
    send(question = question)
    awaitReply()
}

/** Types [question] in the chat's field and sends it with Enter, without waiting for the reply. */
internal fun CaseScope.send(question: String) {
    clickIcon(label = QUESTION_FIELD)
    type(text = question)
    press(key = Key.Enter)
}

/** Waits until a reply is being written: the field's button turned into Stop. */
internal fun CaseScope.awaitReplyStart(running: String = STOP) {
    try {
        ui.waitUntil(timeoutMillis = START_MILLIS) { isRunning(running = running) }
    } catch (exception: ComposeTimeoutException) {
        throw CaseFailure(message = "expected a reply to start, none did").apply {
            initCause(exception)
        }
    }
}

/**
 * Waits for a step of the reply to be the one going on ([text], its line in the list of steps). The steps before it
 * ask the model too (understanding the request), which on a loaded laptop takes longer than a text's usual wait: the
 * model's own time is given (3.6-01 stayed on Understanding your request… past it, 2026-11-14).
 */
internal fun CaseScope.awaitStep(text: String) {
    step(description = "Wait for the step “$text”")

    try {
        ui
            .waitUntil(timeoutMillis = REPLY_MILLIS) {
                ui
                    .onAllNodes(hasText(text = text))
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
    } catch (exception: ComposeTimeoutException) {
        throw CaseFailure(message = "expected the step “$text”, it never came").apply {
            initCause(exception)
        }
    }
}

/**
 * Waits until the reply being written ends (the field's button, [running] meanwhile, is back): the model can take
 * minutes on a laptop.
 */
internal fun CaseScope.awaitReply(running: String = STOP) {
    step(description = "Wait for the reply")

    // A reply starts at once; one that ended before the first look has nothing to wait for.
    runCatching { awaitReplyStart(running = running) }

    try {
        ui
            .waitUntil(timeoutMillis = REPLY_MILLIS) {
                !isRunning(running = running)
            }
    } catch (exception: ComposeTimeoutException) {
        throw CaseFailure(message = "expected the reply to end, it was still being written").apply {
            initCause(exception)
        }
    }

    ui.waitForIdle()
}

/** Opens the chat's ⋯ menu: the rightmost More, as the editor's toolbar has one too. */
internal fun CaseScope.chatMenu() {
    step(description = "Click the chat's More button")
    val more = ui.onAllNodes(matcher = hasContentDescription(value = MORE))

    val chats = List(size = more.fetchSemanticsNodes().size) {
        more[it]
    }
        .maxByOrNull {
            it
                .fetchSemanticsNode()
                .boundsInRoot
                .left
        }
        ?: throw CaseFailure(message = "expected the chat's More button, it did not appear")

    chats.clickWhenStill()
}

private fun CaseScope.isRunning(running: String): Boolean = ui
    .onAllNodes(matcher = hasContentDescription(value = running))
    .fetchSemanticsNodes()
    .isNotEmpty()

/** Copy chat (⋯ at the chat's top right): the conversation as the clipboard gets it, to check what was replied. */
internal fun CaseScope.copiedChat(): String {
    val clipboard = notekea().clipboard
    val before = clipboard.copies
    chatMenu()
    click(text = COPY_CHAT)

    try {
        ui
            .waitUntil(timeoutMillis = START_MILLIS) {
                clipboard.copies > before
            }
    } catch (exception: ComposeTimeoutException) {
        throw CaseFailure(message = "expected Copy chat to copy the chat, it did not").apply {
            initCause(exception)
        }
    }

    return requireNotNull(clipboard.copied)
}

/** Fails unless [chat] (a [copiedChat]) holds one of [texts], in any case: the words a model picks vary. */
internal fun expectInChat(
    chat: String,
    vararg texts: String
) {
    val holds = texts.any {
        chat.contains(
            other = it,
            ignoreCase = true
        )
    }

    if (!holds) {
        throw CaseFailure(
            message = "expected the chat to say ${texts.joinToString(separator = " or ") { "\"$it\"" }}, it said:\n$chat"
        )
    }
}

/**
 * The language of [reply] (a [replyTo]): of its own words, between the transcript's header lines and its sources,
 * which are the notes' words. Looking for " el " or " la " failed a reply in Spanish with neither (2026-11-13).
 */
internal fun languageOf(reply: String): String? {
    val said = reply
        .substringBefore(delimiter = "\nSources:")
        .lines()
        .filterNot { line ->
            line.startsWith(prefix = "##") || HEADER.containsMatchIn(input = line)
        }
        .joinToString(separator = " ")

    return SpokenLanguage.of(text = said)
}

/** Fails unless [reply] is written in [language] ("Spanish"). */
internal fun expectLanguage(
    reply: String,
    language: String
) {
    val written = languageOf(reply = reply)

    if (written != language) {
        throw CaseFailure(message = "expected the chat to answer in $language, it answered in $written:\n$reply")
    }
}

/** The reply to [question] in [chat] (a [copiedChat]): its part of the transcript, up to the next question. */
internal fun replyTo(
    chat: String,
    question: String
): String {
    val start = chat.indexOf(string = "## $question")
    if (start < 0) throw CaseFailure(message = "expected the chat to hold \"$question\", it did not:\n$chat")

    val end = chat.indexOf(
        string = "\n## ",
        startIndex = start + 1
    )

    return if (end < 0) chat.substring(startIndex = start) else chat.substring(start, end)
}

/** Fails unless every source of [reply] (a [replyTo]) is a note of [folder] (`"Recipes/"`). */
internal fun expectOnlyFrom(
    reply: String,
    folder: String
) {
    val sources = reply
        .lines()
        .filter { SOURCE_LINE.matches(input = it) }

    if (sources.isEmpty()) throw CaseFailure(message = "expected the reply to cite its notes, it cited none:\n$reply")

    val outside = sources.filterNot {
        SOURCE_LINE
            .find(input = it)
            ?.groupValues
            ?.get(1)
            ?.startsWith(folder) == true
    }

    if (outside.isNotEmpty()) throw CaseFailure(message = "expected only notes of $folder, it cited $outside")
}

private val SOURCE_LINE = Regex(pattern = """^\[\d+] ([^:]+):.*""")

/** Fails unless one of [texts] is on screen, in part of a text: the words a model picks vary. */
internal fun CaseScope.expectAnyText(vararg texts: String) {
    step(description = "Expect one of ${texts.joinToString()}")

    val isShown = texts.any { text ->
        ui
            .onAllNodes(
                matcher = hasText(
                    text = text,
                    substring = true,
                    ignoreCase = true
                )
            )
            .fetchSemanticsNodes()
            .isNotEmpty()
    }

    if (!isShown) throw CaseFailure(message = "expected the reply to say ${texts.joinToString(separator = " or ")}")
}
