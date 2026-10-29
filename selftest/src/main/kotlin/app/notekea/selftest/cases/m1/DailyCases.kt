package app.notekea.selftest.cases.m1

import androidx.compose.ui.input.key.Key
import app.notekea.selftest.clickInTree
import app.notekea.selftest.openNote
import app.notekea.selftest.openVault
import app.notekea.selftest.pressInWindow
import app.notekea.selftest.titleField
import app.notekea.selftest.typeAtEnd
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.ManualReason
import com.testbuddy.ShortcutModifier.Alt
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.SuiteBuilder
import java.nio.file.Files
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val DAILY_VAULT = "Daily Vault"

/** The cases of 1.10, daily notes (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.dailyCases() {
    todayButton()
    todayFromAnywhere()
    dayArrows()
    dailyFromSettings()
    dailyGaps()
    dailyInObsidian()
}

private fun SuiteBuilder.todayButton() {
    automated(id = "1.10-01", title = "Today makes today's note once, in Journal") {
        openVault(name = PLAYGROUND, shownText = WELCOME)
        val today = "$PLAYGROUND/Journal/${LocalDate.now()}.md"
        clickIcon(label = "Today")
        expectFile(path = today)
        clickInTree(text = WELCOME)
        clickIcon(label = "Today")
        expectText(text = LocalDate.now().toString())
        val journal = Files.list(file(path = "$PLAYGROUND/Journal")).use { it.count() }
        if (journal !=
            4L
        ) {
            throw CaseFailure(message = "expected one note for today in Journal, it holds $journal notes")
        }
    }
}

private fun SuiteBuilder.todayFromAnywhere() {
    automated(id = "1.10-02", title = "Mod+D opens today's note from the middle of typing") {
        openVault(name = PLAYGROUND, shownText = WELCOME)
        clickInTree(text = WELCOME)
        typeAtEnd(part = "This is a sample vault", text = " Sunny")
        press(shortcut = Mod + Key.D)
        expectFile(path = "$PLAYGROUND/Journal/${LocalDate.now()}.md")
        expectFile(path = "$PLAYGROUND/Welcome.md", containing = "Sunny")
    }
}

private fun SuiteBuilder.dayArrows() {
    automated(id = "1.10-03", title = "The arrows and Mod+Alt+arrows step through the daily notes") {
        openVault(name = PLAYGROUND, shownText = WELCOME)
        openNote(name = "2026-10-21", shownText = "#travel")
        clickIcon(label = "Previous daily note")
        expectText(text = "Planted the last tomato seedlings", substring = true)
        clickIcon(label = "Next daily note")
        clickIcon(label = "Next daily note")
        titleShows(name = "2026-10-22")
        pressInWindow(shortcut = Mod + Alt + Key.DirectionLeft)
        titleShows(name = "2026-10-21")
        pressInWindow(shortcut = Mod + Alt + Key.DirectionRight)
        titleShows(name = "2026-10-22")
        openNote(name = WELCOME, shownText = WELCOME)
        expectNoText(text = "Previous daily note")
    }
}

private fun SuiteBuilder.dailyFromSettings() {
    automated(id = "1.10-04", title = "Obsidian's daily note settings decide the folder, the name and the text") {
        openVault(name = DAILY_VAULT, shownText = "Diary")
        pressInWindow(shortcut = Mod + Key.D)
        val today = LocalDate.now()
        val note = "$DAILY_VAULT/Diary/${today.year}/${month(date = today)}/${dailyName(date = today)}.md"
        expectFile(path = note, containing = "# ${dailyName(date = today)}")
        expectFile(path = note, containing = "Created at ")
        expectFile(
            path = note,
            containing = "## Plan for ${today.format(DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH))}"
        )
        expectFile(path = note, containing = "- [ ]")
    }
}

private fun SuiteBuilder.dailyGaps() {
    automated(id = "1.10-05", title = "Stepping back skips the days without a note") {
        openVault(name = DAILY_VAULT, shownText = "Diary")
        pressInWindow(shortcut = Mod + Key.D)
        expectText(text = dailyName(date = LocalDate.now()))
        pressInWindow(shortcut = Mod + Alt + Key.DirectionLeft)
        titleShows(name = "Thursday, October 22nd 2026")
        pressInWindow(shortcut = Mod + Alt + Key.DirectionLeft)
        titleShows(name = "Monday, October 19th 2026")
        openNote(name = "Ideas", shownText = "Ideas")
        expectNoText(text = "Previous daily note")
    }
}

private fun SuiteBuilder.dailyInObsidian() {
    manual(id = "1.10-06", reason = ManualReason.SYSTEM) {
        prepare(DAILY_VAULT)
        title(text = "Obsidian finds the daily note Notekea made")
        steps(
            text = "If Obsidian is not installed, press Skip for now.\n1. Open the Daily Vault from the Test data " +
                "folder in Notekea and press Ctrl+D (Cmd+D on macOS).\n2. Open the same Daily Vault in Obsidian, " +
                "turn on the Daily notes core plugin and run \"Open today's daily note\"."
        )
        expected(
            text = "Obsidian opens the note Notekea made, with the same name and folder, instead of making another."
        )
    }
}

private fun CaseScope.titleShows(name: String) {
    titleField(name = name)
}

private fun month(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH))

// Obsidian's "dddd, MMMM Do YYYY": Thursday, October 22nd 2026.
private fun dailyName(date: LocalDate): String {
    val day = date.dayOfMonth
    val suffix = when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }
    val weekday = date.format(DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH))
    return "$weekday, ${month(date = date)} $day$suffix ${date.year}"
}
