package app.notekea.selftest.cases.m2

import androidx.compose.ui.input.key.Key
import app.notekea.selftest.openNote
import app.notekea.selftest.openVault
import app.notekea.selftest.pressInWindow
import com.testbuddy.ManualReason
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.SuiteBuilder

private const val SAMPLE = "Sample Vault"
private const val OPEN_GRAPH =
    "1. In Notekea, open the Sample Vault (see TEST DATA above) and wait until the bar at the bottom says " +
        "\"40 notes ready to search\".\n2. Open Recipes/Arepas and press Ctrl+G (Cmd+G on macOS)."

/** The cases of 2.7, the graph (`MVP_SMOKE_TEST.md`): its tab, opened around a note or on the whole vault. */
fun SuiteBuilder.graphCases() {
    graphFromNote()
    aroundANote()
    wholeVault()
    keyboard()
    depthAndMotion()
    findANote()
    linkFromTheGraph()
}

private fun SuiteBuilder.graphFromNote() {
    automated(id = "2.7-01", title = "The graph opens around a note, and a note chosen on it opens") {
        openVault(name = SAMPLE, shownText = "Home")
        openNote(name = "Arepas", shownText = "pre-cooked corn flour")
        pressInWindow(shortcut = Mod + Key.G)
        expectText(text = "This note")
        clickIcon(label = "Weekly menu")
        expectText(text = "Connected to")
        click(text = "Open note")
        expectNoText(text = "Connected to")
    }
}

private fun SuiteBuilder.aroundANote() {
    manual(id = "2.7-02", reason = ManualReason.EYES) {
        prepare(SAMPLE)
        title(text = "Around a note the graph is readable, and choosing a note moves it to the center")
        steps(text = "$OPEN_GRAPH\n3. Hover a circle.\n4. Click another circle.")
        expected(
            text = "Arepas is in the center with its notes around it and a name under each. Links are solid lines, " +
                "related notes dashed. Hovering lights up a note and its neighbors. The clicked note slides to the " +
                "center, and the panel on the right shows it."
        )
    }
}

private fun SuiteBuilder.wholeVault() {
    manual(id = "2.7-03", reason = ManualReason.EYES) {
        prepare(SAMPLE)
        title(text = "The whole vault shows its links, zooms, and leaves a folder out")
        steps(
            text = "$OPEN_GRAPH\n3. Click Whole vault.\n4. Scroll over the map to zoom in, and drag it.\n5. Click " +
                "Show orphans.\n6. Click a folder in the legend at the bottom, then close and open the vault again."
        )
        expected(
            text = "Colored circles spread over the map, one color per folder. Names appear as you zoom in. Notes " +
                "with no links appear with Show orphans. The folder's notes leave the map and stay out after " +
                "opening the vault again; clicking it brings them back."
        )
    }
}

private fun SuiteBuilder.keyboard() {
    manual(id = "2.7-04", reason = ManualReason.EYES) {
        prepare(SAMPLE)
        title(text = "The graph works from the keyboard")
        steps(
            text = "$OPEN_GRAPH\n3. Press the arrow keys, then Space twice.\n4. Press Enter.\n5. Go back to the " +
                "graph's tab and press Esc."
        )
        expected(
            text = "Arrows move between connected notes. Space hides and shows the panel. Enter opens the note. " +
                "Esc goes back to Arepas."
        )
    }
}

private fun SuiteBuilder.depthAndMotion() {
    manual(id = "2.7-05", reason = ManualReason.EYES) {
        prepare(SAMPLE)
        title(text = "Depth adds a farther ring, and reduced motion stops the slide")
        steps(
            text = "$OPEN_GRAPH\n3. Click 2 in the toolbar.\n4. Drag the map.\n5. Turn on Reduce motion in Settings, " +
                "Appearance, and click a circle."
        )
        expected(
            text = "A second ring appears, its notes smaller and fainter, and it moves a little less as you drag. " +
                "With Reduce motion the note jumps to the center at once."
        )
    }
}

private fun SuiteBuilder.findANote() {
    manual(id = "2.7-06", reason = ManualReason.EYES) {
        prepare(SAMPLE)
        title(text = "Search finds a note on the map and brings it into view")
        steps(
            text = "$OPEN_GRAPH\n3. Click Whole vault and drag the map to one side.\n4. Click the magnifying glass at " +
                "the top right, or press Ctrl+F (Cmd+F on macOS), and type soup.\n5. Press Enter twice, then Esc."
        )
        expected(
            text = "The matching notes keep their color and name while the rest fade, and the bar counts them. Enter " +
                "selects a match and slides it to the middle, the next Enter goes to the next one. Esc closes the bar, " +
                "every note comes back, and the arrow keys walk the map again."
        )
    }
}

private fun SuiteBuilder.linkFromTheGraph() {
    manual(id = "2.7-07", reason = ManualReason.EYES) {
        prepare(SAMPLE)
        title(text = "Link joins two notes from the graph, and Undo takes it back")
        steps(
            text = "$OPEN_GRAPH\n3. Click circles until the panel shows Related, not linked.\n4. Click Link on a row." +
                "\n5. Open that note from the panel and look at its end, then go back to the graph's tab.\n6. Click " +
                "Link on another row, then Undo."
        )
        expected(
            text = "The dashed line turns solid and the panel says Linked to that note. The note ends with the new " +
                "[[link]], and the rest of it is unchanged. Undo removes the link from the note and from the map."
        )
    }
}
