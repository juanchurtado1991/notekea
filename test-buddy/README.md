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
        title(text = "Dark mode follows the system")
        steps(text = "Switch the system to dark mode.")
        expected(text = "The app turns dark at once and every text reads well.")
    }

    backUp { listOf(NotesPaths.appDataDir) }
}
```

- `launch` shows the app on a case's `CaseFolders`: `appData` stands for its data folder (never the real one) and
  `data` holds a fresh copy of the suite's `data` folders. Both are deleted after the case.
- An automated case can have a `window` size of its own (a narrow window) instead of the suite's.
- A case ID is used once and has no spaces. A manual case needs a title, steps and an expected result, in plain
  English for a tester who is not a developer.
- `backUp` names what Finish and clean up puts back as it was after the manual cases.
- `installedApp { system -> path }` says where the installed app's program is on each system: every manual case gets
  an "Open <app>" button that starts it, its output written to `<case id> output.txt` in the Report folder. A case's
  `launchWith(name, value)` adds a variable for that start (a crash test), and `answers(passed, failed)` renames its
  two answers ("Nothing odd" / "I found something" for free use).
- `diagnostics { text }` is read when the report is made and ends it: the installed app's recent log, for one.

## Verbs (`CaseScope`)

| Verb | What it does |
|---|---|
| `click(text, substring)` | Clicks the first element showing the text, once it shows |
| `rightClick(text)` | Right-clicks the first element showing the text, for its context menu |
| `clickWords(words)` | Clicks some words inside a longer text, where they are drawn: a link in a paragraph |
| `clickIcon(label)` | Clicks the first icon button with that label for screen readers |
| `type(text)`, `clearText()` | Types into the focused text field, or empties it |
| `press(shortcut)`, `press(key)` | Presses `Mod + Key.P` (Cmd on macOS, Ctrl elsewhere), `Mod + Shift + Key.F`, `Ctrl + Key.Tab`, or a single key |
| `scrollTo(text)` | Scrolls the list holding the text into view, also a lazy list that has not made the row yet |
| `expectText(text, substring)`, `expectNoText(text, substring)` | Waits until the text shows, or is gone or hidden |
| `file(path)` | The file at a path of the case's data, to read or change |
| `expectFile(path, content, containing)`, `expectNoFile(path)` | Waits until the file exists with that content, or is gone |

Every expectation waits for the app (up to 10 seconds by default) and fails with a `CaseFailure` a tester can read:
`expected to see "Lisbon trip", it did not appear`. `ui` is Compose UI Test itself, for anything the verbs do not
cover, and `system` is the `HostSystem` the case runs on.

## Running a suite

- **CI:** `class NotesSuiteTest : TestBuddyJUnit(notesSuite)` turns every automated case into a JUnit 5 test named
  after its ID (`1.6-03 Open a note from the quick switcher`). The test module needs `useJUnitPlatform()`; the system
  property `testbuddy.workFolder` keeps the screenshots of failures in a folder of your choice (where CI uploads).
  A long suite runs in parts at the same time: `suite.shard(index, count)` keeps every `count`-th automated case
  (the manual ones stay in part 0), one `TestBuddyJUnit` class per part, with Gradle's `maxParallelForks`.
- **Code:** `SuiteRunner(suite, workFolder).run()` runs the cases one at a time and never stops at the first failure;
  each result is `Passed`, `Failed` (with its message and a screenshot in `workFolder/screenshots`) or `Skipped`
  (after `shouldStop` said so).
- **Report:** `formatReport(suite, ReportEnvironment.current(), automatic, manual, diagnostics)` writes the plain text
  a tester sends back.

## The Self-Test app

`fun main() = testBuddyApp(suite)` opens the "<app> Self-Test" window, a wizard in three steps:

1. **Automatic:** Start runs every automated case in the background (the tester can keep using the computer), each
   with its state and, when it failed, why; Stop skips the rest.
2. **Manual:** before the first manual case the installed app's `backUp` folders are copied aside. Then one case per
   screen: its fresh test data in the Test data folder, Do this, You should see, and Passed, Failed (what they saw and
   a screenshot) or Skip for now. Answers are saved, so the tester can close the window and go on later.
3. **Send report:** the report to copy, the Report folder with the screenshots, and Finish and clean up, which puts
   the app's folders back as they were and deletes the test data.

Everything the tester does is kept in `<Documents>/<app> Self-Test` (`SelfTestFolders`): `Test data`, `Report` and
the Self-Test's own `.self-test`.
