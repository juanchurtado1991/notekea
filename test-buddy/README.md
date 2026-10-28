# Test Buddy

Write an app's smoke cases once, in a few lines of plain verbs, and run them in CI, on a developer's machine and as a
Self-Test app that testers install. Built on the official Compose UI Test, for Compose Desktop apps.

Test Buddy lives in the Notekea repository for now (`qa/SELF_TEST.md`) but knows nothing about Notekea: its package is
`com.testbuddy`, it depends on no Notekea module (a Konsist test checks it), and the host app gives it its name.

## A suite

```kotlin
val notesSuite = testBuddy(appName = "Notes", version = "1.0") {
    launch { folders -> NotesApp(appDataDir = folders.appData, vaults = folders.data) }
    data(from = Path.of("smoke-data/vaults"))
    window(width = 1280, height = 800)

    automated(id = "1.6-03", title = "Open a note from the quick switcher") {
        click(text = "Personal Notes")
        press(shortcut = Mod + Key.P)
        type(text = "lisbon")
        press(key = Key.Enter)
        expectText(text = "Lisbon trip")
        expectFile(path = "Personal Notes/Travel/Lisbon trip.md", containing = "# Lisbon")
    }

    manual(id = "1.17-02", reason = ManualReason.EYES) {
        prepare("Personal Notes")
        title(text = plainText(english = "Dark mode follows the system", "es" to "El modo oscuro sigue al sistema"))
        steps(text = plainText(english = "Switch the system to dark mode."))
        expected(text = plainText(english = "The app turns dark at once and every text reads well."))
    }

    backUp { listOf(NotesPaths.appDataDir) }
}
```

- `launch` shows the app on a case's `CaseFolders`: `appData` stands for its data folder (never the real one) and
  `data` holds a fresh copy of the suite's `data` folders. Both are deleted after the case.
- A case ID is used once and has no spaces. A manual case needs a title, steps and an expected result; `CaseText`
  resolves them in the tester's language (`plainText`, or the host's string resources) and falls back to English.
- `backUp` names what Finish and clean up puts back as it was after the manual cases.

## Verbs (`CaseScope`)

| Verb | What it does |
|---|---|
| `click(text, substring)` | Clicks the first element showing the text, once it shows |
| `type(text)` | Types into the focused text field |
| `press(shortcut)`, `press(key)` | Presses `Mod + Key.P` (Cmd on macOS, Ctrl elsewhere), `Mod + Shift + Key.F`, `Ctrl + Key.Tab`, or a single key |
| `scrollTo(text)` | Scrolls the list holding the text into view, also a lazy list that has not made the row yet |
| `expectText(text, substring)`, `expectNoText(text)` | Waits until the text shows, or goes away |
| `file(path)` | The file at a path of the case's data, to read or change |
| `expectFile(path, content, containing)`, `expectNoFile(path)` | Waits until the file exists with that content, or is gone |

Every expectation waits for the app (up to 10 seconds by default) and fails with a `CaseFailure` a tester can read:
`expected to see "Lisbon trip", it did not appear`. `ui` is Compose UI Test itself, for anything the verbs do not
cover, and `system` is the `HostSystem` the case runs on.

## Running a suite

- **CI:** `class NotesSuiteTest : TestBuddyJUnit(notesSuite)` turns every automated case into a JUnit 5 test named
  after its ID (`1.6-03 Open a note from the quick switcher`). The test module needs `useJUnitPlatform()`.
- **Code:** `SuiteRunner(suite, workFolder).run()` runs the cases one at a time and never stops at the first failure;
  each result is `Passed`, `Failed` (with its message and a screenshot in `workFolder/screenshots`) or `Skipped`
  (after `shouldStop` said so).
- **Report:** `formatReport(suite, ReportEnvironment.current(), automatic, manual, diagnostics)` writes the plain text
  a tester sends back.

The Self-Test window, the manual walk-through and Finish and clean up come next (`qa/README.md`, piece 3b).
