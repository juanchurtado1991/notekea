# Test Buddy Crash

What a Compose Desktop app shows and keeps when it fails, for its release and its QA build alike: a crash notice in a
dialog window of its own, a report with **the person's last steps** and **the machine**, kept in the app's folder,
and the actions to send it. Sister library of [Test Buddy](../test-buddy/README.md); it depends on Compose only.

## Use it

```kotlin
fun main() {
    val crashBuddy = CrashBuddy(
        config = CrashConfig(
            appName = "Notes",
            version = "1.0",
            reportFolder = appDataDir.resolve("crashes"),        // the 10 newest reports stay
            issueFormUrl = "https://github.com/me/notes/issues/new?template=crash.yml", // none hides Report
            texts = CrashTexts.english(appName = "Notes"),       // or the app's own, translated
            isCloseLast = !isMacOs                               // the platform's button order
        ),
        onError = { logger.e(it) { "Unexpected error" } }
    )
    crashBuddy.installThreadHandler()                            // any thread: kept, no notice
    application {
        CompositionLocalProvider(LocalWindowExceptionHandlerFactory provides crashBuddy.windowExceptionHandlers()) {
            NotesWindow()
            CrashNotice(crashBuddy = crashBuddy, theme = { NotesTheme { it() } })
        }
    }
}
```

- **A window fails** (composition, layout, drawing): the report is written and [CrashNotice] opens a dialog window, a
  separate scene that still works when the main window is broken, in the app's theme. It says the app must close and
  where the report is, with **Copy details**, **Report on GitHub** (copies, then opens the form) and **Close**, which
  closes the failed window the usual way, so the app saves its work. If the notice itself fails, a plain Swing dialog
  says the same.
- **Another thread fails:** the report is written, without a notice, since the app goes on.

## Last steps

```kotlin
Breadcrumbs.record(step = "Editor: OnSaveShortcut")
```

The app names each action, never what the person wrote: the screen and the action is enough. The same step right
after itself is counted on one line (`Editor: OnTextChange (×37)`), and the 50 newest stay. A good place is the one
entry point of a screen's actions; Notekea records every ViewModel intent's name in `BaseViewModel.onIntent`. Test
Buddy adds the same steps to a failed manual case in the QA report.

## The report

```text
Notes crash report
Version: 1.0 (m1-rc1)
Time: 2026-10-26T10:15:30Z
OS: Windows 11 10.0 (amd64)
Java: 21.0.5
Language: es-CO
Screen: 1920×1080 at 125%
Thread: AWT-EventQueue-0

Last steps:
10:15:01 Welcome: OnOpenFolder
10:15:12 Editor: OnTextChange (×12)

java.lang.IllegalStateException: …
```

Every line is cut at 300 characters and the report at 400 lines: an error's message could carry some of the person's
text. Nothing is ever sent: a report only leaves the computer if the person pastes and submits it.
