package app.notekea.autotest.cases.m4

import androidx.compose.ui.input.key.Key
import app.notekea.autotest.ChatModels
import app.notekea.autotest.cases.m1.PLAYGROUND
import app.notekea.autotest.cases.m1.WELCOME
import app.notekea.autotest.cases.m3.MODEL_CASE_TIMEOUT
import app.notekea.autotest.notekea
import app.notekea.autotest.openVault
import com.testbuddy.SuiteBuilder
import java.nio.file.Files

private const val CARDS = "Rivers.md"
private const val QUESTION = "Longest river of Portugal"
private const val ANSWER = "Tagus"
private const val RIGHT = "Right!"

/**
 * With a chat model a study card is a multiple choice question (the developer's design of 2026-11-13): its answer
 * among three the model wrote; the right one chosen and Enter schedule it as Good.
 */
internal fun SuiteBuilder.studyChoiceCases() {
    automated(
        id = "4.6-03",
        title = "With a chat model a study card offers four options, and the right one with Enter is Good",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.any
    ) {
        Files.writeString(file(path = "$PLAYGROUND/$CARDS"), "# Rivers\n\n#flashcards\n\n$QUESTION::$ANSWER\n")
        notekea().useChatModel()

        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        clickIcon(label = "Study cards")

        expectText(
            text = QUESTION,
            substring = true
        )

        expectText(text = "D")
        expectNoText(text = "Show answer")
        click(text = ANSWER)
        expectText(text = RIGHT)
        press(key = Key.Enter)

        expectText(
            text = "All done for today",
            substring = true
        )

        expectFile(
            path = "$PLAYGROUND/$CARDS",
            containing = ",3,250-->"
        )
    }
}
