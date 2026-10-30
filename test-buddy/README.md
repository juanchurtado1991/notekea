# Test Buddy

Write an app's smoke cases once, in a few lines of plain verbs, and get three things from them:

- **CI tests:** every automated case is a JUnit test.
- **A QA build:** the app itself with a small **manager** window next to it. Testers install it, run the automated
  cases with one click and follow the manual cases one step at a time, then send one report.
- **Crash reports with the steps that led to them**, through its sister library, [Test Buddy Crash](../test-buddy-crash/README.md),
  which ships in the app's release too.

Built on the official Compose UI Test, for **Compose Desktop** apps, including Kotlin Multiplatform projects with a
desktop target. Test Buddy lives in the Notekea repository for now (`qa/SELF_TEST.md`) but knows nothing about
Notekea: its package is `com.testbuddy`, it depends on no Notekea module (a Konsist test checks it), and the host app
gives it its name.

## 1. Add it

Two modules, `:test-buddy` and `:test-buddy-crash`. Keep the release clean: the app's own module depends only on
`:test-buddy-crash`; a **QA module** depends on the app and on `:test-buddy`, and holds the cases.

```kotlin
// settings.gradle.kts
include(":app", ":app-qa", ":test-buddy", ":test-buddy-crash")

// app/build.gradle.kts (a KMP project: in jvmMain)
dependencies { implementation(project(":test-buddy-crash")) }

// app-qa/build.gradle.kts: a Compose Desktop application of its own
dependencies {
    implementation(project(":app"))
    implementation(project(":test-buddy"))
    implementation(compose.desktop.currentOs)
    testImplementation(libs.junit.jupiter)
}
compose.desktop.application { mainClass = "com.example.qa.QaMainKt" /* + nativeDistributions, as the app's */ }
```

## 2. Write the suite (in the QA module)

```kotlin
val notesSuite = testBuddy(appName = "Notes", version = "1.0") {
    // How an automated case shows the app: on folders of its own, never the real ones.
    launch { folders -> NotesApp(appDataDir = folders.appData, vaults = folders.data) }
    data(from = Path.of("smoke-data/vaults"))       // copied fresh for every case, and for each manual case
    window(width = 1280, height = 800)
    diagnostics { lastLogLines() }                   // ends the report

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
        steps(text = "1. Open the Personal Notes vault of the Test data folder.\n2. Switch the system to dark mode.")
        expected(text = "The app turns dark at once and every text reads well.")
    }
}
```

- A case ID is used once and has no spaces. A manual case needs a title, steps and an expected result, in plain
  English for a tester who is not a developer. Number its steps (`1.`, `2.`…): the manager shows them one at a time.
- An automated case can have a `window` size and a `timeout` of its own.
- `answers(passed, failed)` renames a manual case's two answers ("Nothing odd" / "I found something" for free use);
  `launchWith(name, value)` gives it a **Restart** button that starts the app again with that variable (a crash test).

### Verbs (`CaseScope`)

| Verb | What it does |
|---|---|
| `click(text, substring)` | Clicks the first element showing the text, once it shows |
| `rightClick(text)` | Right-clicks it, for its context menu |
| `clickWords(words)` | Clicks some words inside a longer text, where they are drawn: a link in a paragraph |
| `clickIcon(label)` | Clicks the first icon button with that label for screen readers |
| `type(text)`, `clearText()` | Types into the focused text field, or empties it |
| `press(shortcut)`, `press(key)` | Presses `Mod + Key.P` (Cmd on macOS, Ctrl elsewhere), `Mod + Shift + Key.F`, or a single key |
| `scrollTo(text)` | Scrolls the list holding the text into view, also a lazy list that has not made the row yet |
| `expectText`, `expectNoText` | Waits until the text shows, or is gone or hidden |
| `file(path)`, `expectFile`, `expectNoFile` | The file at a path of the case's data; waits until it exists with that content, or is gone |
| `step(description)` | Names a step of the host's own verbs (`Open the vault Personal Notes`) |

Every verb names its step (`Click “Save”`), which the manager shows while the case runs and a failure's report lists.
Every expectation waits for the app (10 seconds by default) and fails with a message a tester can read: `expected to
see "Lisbon trip", it did not appear`. `ui` is Compose UI Test itself, for anything the verbs do not cover;
`isShown()` tells whether a node is on screen without failing when it went away meanwhile.

## 3. Run it in CI

```kotlin
class NotesSuiteTest : TestBuddyJUnit(notesSuite)    // one JUnit 5 test per case, named after its ID
```

The test task needs `useJUnitPlatform()`; `testbuddy.workFolder` keeps failures' screenshots where CI uploads. A long
suite runs in parts at the same time: `suite.shard(index, count)`, one `TestBuddyJUnit` class per part, with Gradle's
`maxParallelForks`. `suite.only(listOf("1.6"))` keeps the cases of one feature.

## 4. Build the QA app

The app's `main` takes extra windows, and the QA module's `main` gives it the manager:

```kotlin
// app: its usual main, opened up a little
fun main() = runNotesApp()
fun runNotesApp(extraWindows: @Composable ApplicationScope.() -> Unit = {}) = application {
    NotesWindow()
    extraWindows()
}

// app-qa
fun main(args: Array<String>) = testBuddyMain(args = args, suite = notesSuite) { manager ->
    runNotesApp(extraWindows = manager)   // give the QA build a data folder of its own, too
}
```

`testBuddyMain` runs the app as always, with the manager as one more window. When the tester starts the automated
cases, the app starts again in a process of its own with a flag, and there `testBuddyMain` runs the cases out of sight
instead of the app: the tester's app is never touched and can still be used. Package the QA module like the app
(`nativeDistributions`) and send testers its installer.

## The manager

A small window, always on top, at the bottom left of the screen; the tester moves and minimizes it, and closing it only
minimizes it.

1. **Automatic:** Start, then **Running tests** with the time, the progress and what is left, the passed and failed
   counts, the case running **now** with its steps as they happen, the last ones done, and Stop.
2. **Manual:** one case at a time and one step at a time: the steps as a line (✓ done, the current one in bold, the
   rest to come), Back and "Done, next step", Open the test data, and Restart for the cases that need it. After the
   last step, what should have been seen and the two answers; a failure asks what the tester saw and takes a
   screenshot, and the tester's last steps in the app are added for them (`Breadcrumbs`).
3. **Report:** the report to copy, and the Report folder with the screenshots, `output.txt` (everything the app
   printed) and a copy of the report.

Everything is kept in `<Documents>/<app> QA` (`SelfTestFolders`): `Test data`, `Report` and Test Buddy's own
`.self-test`, so the app can close or restart and the manager goes on where it was.

## The report

```text
Notes QA report
Notes 1.0 (m1-rc1)
OS: Windows 11 10.0 (amd64)
Java: 21.0.5
Language: es-CO
Screen: 1920×1080 at 125%
Automatic: 120 passed, 1 failed, 0 skipped (21 min)
  ❌ 1.6-03 Open a note from the quick switcher: expected to see "Lisbon trip", it did not appear (screenshot: 1.6-03.png)
     Last steps of the test:
       Press Ctrl+P
       Type “lisbon”
Manual: 46 passed, 1 failed, 0 not run
  ❌ 1.17-02 Dark mode follows the system: "the sidebar stayed white" (screenshot: 1.17-02.png)
     Last steps in Notes:
       10:15:01 Settings: OnThemeChange
Diagnostics:
…
```
