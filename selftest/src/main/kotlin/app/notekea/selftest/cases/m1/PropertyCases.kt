package app.notekea.selftest.cases.m1

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import app.notekea.selftest.noteText
import app.notekea.selftest.openNote
import app.notekea.selftest.pressInWindow
import app.notekea.selftest.relaunch
import com.testbuddy.CaseScope
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.SuiteBuilder
import java.nio.file.Files

private const val PRACTICE = "Properties practice"
private const val PRACTICE_TEXT = "Edit the properties above"
private const val PRACTICE_FILE = "$PLAYGROUND/Editor/Properties practice.md"
private const val BROKEN_FILE = "$PLAYGROUND/Editor/Broken properties.md"
private const val BROKEN_LINE = "this line is not a property"
private const val WELCOME_FILE = "$PLAYGROUND/Welcome.md"
private const val REMOVE_PROPERTY = "Remove property"

/** The cases of 1.14 about a note's properties (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.propertyCases() {
    propertiesShown()
    propertiesEdited()
    propertiesAddedAndRenamed()
    firstProperty()
    propertiesSource()
    brokenProperties()
    propertiesInPreview()
}

private fun SuiteBuilder.propertiesShown() {
    automated(id = "1.14-01", title = "The properties show as a form that folds, and opening never writes") {
        openPractice()
        val original = Files.readString(file(path = PRACTICE_FILE))
        listOf("status", "draft", "rating", "4", "done", "Nov 2, 2026", "#recipe", "#weekend", "Practice", "Form test")
            .forEach { expectText(text = it) }
        expectText(text = "book: Abuela's notebook", substring = true)
        click(text = "Properties")
        expectNoText(text = "draft")
        relaunch()
        expectText(text = PRACTICE_TEXT, substring = true)
        expectNoText(text = "draft")
        click(text = "Properties")
        expectText(text = "draft")
        expectUnchanged(path = PRACTICE_FILE, content = original)
    }
}

private fun SuiteBuilder.propertiesEdited() {
    automated(id = "1.14-02", title = "Each property edited in the form is saved at once, only its lines") {
        openPractice()
        editField(current = "draft", text = "ready")
        expectFile(path = PRACTICE_FILE, containing = "status: ready\n")
        editField(current = "4", text = "five")
        expectUnchanged(path = PRACTICE_FILE, content = Files.readString(file(path = PRACTICE_FILE)))
        editField(current = "five", text = "5")
        expectFile(path = PRACTICE_FILE, containing = "rating: 5\n")
        clickIcon(label = "done")
        expectFile(path = PRACTICE_FILE, containing = "done: true\n")
        editField(current = "Nov 2, 2026", text = "2026-11-09")
        expectFile(path = PRACTICE_FILE, containing = "due: 2026-11-09\n")
        expectText(text = "Nov 9, 2026")
        addToList(property = "tags", item = "brunch")
        expectFile(path = PRACTICE_FILE, containing = "  - brunch\n")
        clickOnRowOf(rowText = "Practice", label = "Remove")
        addToList(property = "aliases", item = "Notekea")
        expectFile(path = PRACTICE_FILE, containing = "aliases: ['Form test', Notekea]\n")
        expectFile(path = PRACTICE_FILE, containing = "  book: Abuela's notebook\n  page: 12\n")
    }
}

private fun SuiteBuilder.propertiesAddedAndRenamed() {
    automated(id = "1.14-03", title = "A property is added, renamed (never to a taken or broken name) and removed") {
        openPractice()
        click(text = "Add property")
        click(text = "Checkbox")
        expectFile(path = PRACTICE_FILE, containing = "property: false\n")
        editField(current = "property", text = "favorite")
        expectFile(path = PRACTICE_FILE, containing = "favorite: false\n")
        editField(current = "favorite", text = "status")
        editField(current = "status", text = "bad: name")
        expectFile(path = PRACTICE_FILE, containing = "favorite: false\n")
        expectFile(path = PRACTICE_FILE, containing = "status: draft\n")
        clickOnRowOf(rowText = "rating", label = REMOVE_PROPERTY)
        expectNoText(text = "rating")
        expectFile(path = PRACTICE_FILE, containing = "done: false\n")
    }
}

private fun SuiteBuilder.firstProperty() {
    automated(id = "1.14-04", title = "The first property adds a frontmatter, and removing the last removes it") {
        openPlaygroundNote(name = WELCOME, shownText = "This is a sample vault")
        expectNoText(text = "Properties")
        clickIcon(label = "More")
        click(text = "Add property")
        click(text = "Text")
        expectText(text = "Properties")
        expectFile(path = WELCOME_FILE, containing = "---\nproperty:\n---\n")
        clickOnRowOf(rowText = "property", label = REMOVE_PROPERTY)
        expectNoText(text = "Properties")
        expectFile(path = WELCOME_FILE, containing = "# Welcome")
        if (Files.readString(file(path = WELCOME_FILE)).startsWith("---")) {
            throw com.testbuddy.CaseFailure(message = "expected Welcome without a frontmatter")
        }
    }
}

private fun SuiteBuilder.propertiesSource() {
    automated(id = "1.14-05", title = "The properties' source is edited as written and saved like the text") {
        openPractice()
        clickIcon(label = "Show source")
        expectText(text = "# Properties for the smoke test", substring = true)
        val source = sourceField(part = "page: 12")
        val yaml = source.fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.EditableText]
        source.performTextReplacement(text = yaml.text.replace("page: 12", "page: 13"))
        expectFile(path = PRACTICE_FILE, containing = "  page: 13\n")
        noteText(part = PRACTICE_TEXT).performClick()
        clickIcon(label = "Show properties")
        expectText(text = "page: 13", substring = true)
        expectText(text = "book: Abuela's notebook", substring = true)
    }
}

private fun SuiteBuilder.brokenProperties() {
    automated(id = "1.14-06", title = "Properties that are not valid YAML show as source, with why") {
        openPlaygroundNote(name = "Broken properties", shownText = "Its frontmatter is not valid YAML")
        expectText(text = "not valid YAML (line 2)", substring = true)
        val source = sourceField(part = BROKEN_LINE)
        val yaml = source.fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.EditableText]
        source.performTextReplacement(text = yaml.text.lines().filterNot { it == BROKEN_LINE }.joinToString("\n"))
        expectNoText(text = "not valid YAML (line 2)", substring = true)
        expectFile(path = BROKEN_FILE, content = "---\ntitle: Broken on purpose\n---\n" + brokenBody())
        noteText(part = "Its frontmatter is not valid YAML").performClick()
        expectText(text = "Broken on purpose")
    }
}

private fun SuiteBuilder.propertiesInPreview() {
    automated(id = "1.14-07", title = "Properties are edited in Preview too, and every note shows its own") {
        openPractice()
        pressInWindow(shortcut = Mod + Key.E)
        editField(current = "draft", text = "cooked")
        expectFile(path = PRACTICE_FILE, containing = "status: cooked\n")
        openNote(name = "Arepas", shownText = AREPAS_FLOUR)
        listOf("servings", "6", "#recipe", "#venezuelan", "Arepa", "Corn cakes").forEach { expectText(text = it) }
    }
}

private fun CaseScope.openPractice() {
    openPlaygroundNote(name = PRACTICE, shownText = PRACTICE_TEXT)
}

/** Types [item] in the empty field at the end of the list [property] and presses Enter. */
private fun CaseScope.addToList(
    property: String,
    item: String
) {
    onRowOf(rowText = property, matcher = hasSetTextAction() and hasText(text = "")).performClick()
    type(text = item)
    press(key = Key.Enter)
}

private fun CaseScope.sourceField(part: String) = ui.onAllNodes(
    matcher = hasSetTextAction() and hasText(text = part, substring = true)
)[0]

private fun brokenBody(): String =
    "# Broken properties\n\nIts frontmatter is not valid YAML: the properties show as source, with an explanation.\n"
