# Notekea Self-Test

How Notekea's smoke cases are tested on the testers' machines, and how every feature keeps them up to date. Read it
before adding or changing a case of `MVP_SMOKE_TEST.md` (`GUIDELINES.md` §8). Status of each piece: `qa/README.md`,
"Before the first run".

## The goal

A tester installs two apps from the milestone's release, **Notekea** and **Notekea Self-Test**, and opens the second:

1. **Automatic part:** one click on **Start** runs every case a program can check on the tester's own system, files and screen settings, and shows each case's ID with ✅ or ❌. The tester
   can keep using the computer meanwhile.
2. **Manual part:** **Manual tests** walks the tester through the rest, one case per screen: what to open, what to
   do, what they should see, and **Passed** / **Failed** (with a note and a screenshot when it failed). The tester uses
   the installed Notekea for these.
3. **Copy report** puts both parts in one plain text the tester sends back (by chat or email).
4. **Finish and clean up** leaves the installed Notekea as it was before the test, ready for everyday use; then the
   Self-Test can be uninstalled.

Testers are not developers: they never clone the repository, open a terminal, install Java or prepare data by hand.
A manual run takes a fraction of the 5 to 7 hours per system the step-by-step guide takes.

## Two modules: Test Buddy and Notekea's suite

| Module | What it holds | Depends on |
|---|---|---|
| `:test-buddy` | **Test Buddy**, the generic Self-Test: the case types, the runner, the report, the Self-Test window (Start, results, Manual tests, Copy report, Finish and clean up) and its own texts. It knows nothing about Notekea, so it could test any Compose Desktop app | Compose, Compose UI Test |
| `:selftest` | **Notekea's suite**: every automated and manual case, the smoke data bundled in, how to prepare it and clean up after, the "Notekea Self-Test" app and its installers; also the memory runs of the performance passes (`smokeRun`) | `:composeApp`, `:test-buddy` |

Notekea itself ships no test code: the app depends on neither module.

**Test Buddy is written to become a standalone SDK** if it proves useful beyond Notekea: its own package
(`com.testbuddy`), no dependency on any Notekea module (checked by a Konsist test), a public API kept small and
documented in its module's `README.md`, and no Notekea name in its texts (the host app gives its own name). Moving it
to its own repository and publishing it, as Ghost Serializer is, would then need no change to its code.

### What Test Buddy offers (`com.testbuddy`)

Its promise: **a case is a few lines of plain verbs, written once, and the same suite becomes a CI test, a window for
the developer and an installable app for testers.** A host app declares its suite with a small DSL (a sketch; piece
3 settles the names):

```kotlin
val notekeaSuite = testBuddy(appName = "Notekea", version = AppVersion.current) {
    launch { appData, data -> NotekeaApp(appDataDir = appData, vaults = data) }
    bundledData("smoke-data/vaults")

    automated("1.6-03", "Open a note from the quick switcher") {
        openVault("Personal Notes")
        press(Mod + P) // Ctrl on Windows and Linux, Cmd on macOS
        type("lisbon")
        press(Enter)
        expectTab("Lisbon trip")
        expectFile("Personal Notes/Travel/Lisbon trip.md", unchanged = true)
    }

    manual("1.17-02", reason = Eyes) {
        prepare("Personal Notes")
        title(Res.string.case_1_17_02_title)
        steps(Res.string.case_1_17_02_steps) // English and Spanish
        expected(Res.string.case_1_17_02_expected)
    }

    backUp { NotekeaPaths.appDataDir } // what Finish and clean up restores
}

fun main() = testBuddyApp(notekeaSuite) // the "Notekea Self-Test" app
```

- **Verbs, not finders:** `click`, `type`, `press(Mod + Key.P)`, `scrollTo`, `expectText`, `expectNoText`, `file`,
  `expectFile`, `expectNoFile` (the full list is in `test-buddy/README.md`). They wrap Compose UI Test, which stays
  reachable as `ui` for what the verbs do not cover, and map `Mod` to each system's key. Verbs that know the app
  (`openVault`, `expectTab`) are written by the host, in `:selftest`.
- **Each automated case starts clean:** a temporary app data folder, fresh copies of its data, a timeout; on failure
  it keeps a screenshot and a plain message ("expected the tab 'Lisbon trip', it did not appear").
- **The host only says how to launch itself** on those folders (`launch`) and what to back up (`backUp`); Test Buddy
  does the rest and never writes anywhere else.
- **The runner** runs the automated cases one at a time, records `Passed`, `Failed` (with the message) or `Skipped`,
  and never stops at the first failure.
- **Three ways to run one suite:**

| Where | How | What for |
|---|---|---|
| CI | `class NotekeaSuiteTest : TestBuddyJUnit(notekeaSuite)`: one JUnit test per case ID | Every pull request, on Windows, macOS and Linux |
| The developer's machine | `./gradlew :selftest:run -Pcases=1.6` | A feature's smoke test: its automated cases run, its manual cases in the same window testers use |
| Testers | `./gradlew :selftest:packageSelfTest` | The "Notekea Self-Test" installers, with Notekea's Java runtime and modules |

- **Built in:** the window (Start, results, Manual tests, Copy report, Finish and clean up) and its texts, the report
  (version, system, language, screen and scale added on their own, plus the diagnostics the host passes), the
  consistency test and the generation of the case list (`MVP_SMOKE_TEST.md`).
- **Packaging** is a short block in `selftest/build.gradle.kts` at first; if Test Buddy becomes a standalone SDK,
  it becomes a Gradle plugin (`id("com.testbuddy")`) that copies the host's runtime modules and builds the
  installers in one line.
- **Built on the official Compose UI Test** (`LIBRARIES.md`), stable on desktop. Existing Compose Multiplatform
  end-to-end libraries (Parikshan, cmp-bridge) are early alphas when this was written: Test Buddy borrows their verb
  names where they fit but does not depend on them. None of them, nor case managers such as TestRail, combines
  automatic and guided manual cases in one installable app for testers; that is Test Buddy's reason to exist.
- **Later, not now:** a recorder that writes an `automated` case from the clicks of a person using the app.

### What Notekea's suite adds (`app.notekea.selftest`)

- Cases grouped like the roadmap: `cases/m1/EditorCases.kt` holds the cases of 1.5, and so on (the 350-line limit
  applies).
- The vaults of `smoke-data/vaults` and the extra data `smoke.sh` / `smoke.ps1` build, bundled as resources at build
  time. Data preparation is written once, in Kotlin, in this module; the scripts call it, so the developer's smoke
  data and the testers' are the same.
- `backUp` / `restore` of the installed Notekea's app data folder (settings, recent vaults, cache), found through
  `PlatformConventions`.

## Which cases are automatic

Every case of `MVP_SMOKE_TEST.md` says in its **Checked by** column `Self-Test` or `Manual (<reason>)`. A case is
manual only for one of these reasons:

- **eyes:** a person must judge how it looks or feels (spacing, colors, smooth scrolling);
- **system:** it needs something outside the app: the installer, the system's launcher, a native file dialog, the
  system's settings (dark mode), another app (the clipboard in a text editor, Obsidian);
- **packaged:** it only exists in the installed app (its shortcuts, the crash report window of the packaged build);
- **costly:** automating it would cost far more than the case is worth (fragile timing, heavy setup, a scenario that
  only a long script could reach).

Everything else is `Self-Test`. When part of a case is automatic and part needs eyes, it is split into two cases.

**Automate what is reasonable, not a quota.** Because the Self-Test guides every manual case step by step, a manual
case is cheap for the tester. So there is no percentage to reach: automate the cases a few clear verbs can check, and
leave the rest manual instead of forcing them. The consistency test prints the share of `Self-Test` cases as
information only.

## Every feature keeps it up to date

The Self-Test is never written at the end of a milestone. Every `feature/` and `fix/` pull request, in the step that
adds the behavior:

1. Adds or revises its cases, with **Checked by** (in `MVP_SMOKE_TEST.md` by hand until piece 5; after it, in the
   cases themselves, and regenerates the file).
2. Writes each `Self-Test` case as an `AutomatedCase` named after its ID, in `:selftest`; it runs in CI from then on.
3. Writes each manual case as a `ManualCase`, its steps and expected result in **English and Spanish**
   (`values/` and `values-es/` of `:selftest`): plain words, the button and menu names as the tester sees them, the
   shortcut of each system, never a terminal command or a file path the tester must type.
4. Adds the data the cases need to `smoke-data/` (bundled automatically).
5. Keeps the consistency test green: it fails when a case of `MVP_SMOKE_TEST.md` has no `AutomatedCase` or
   `ManualCase` with its ID, when one exists for a removed case, when a manual case lacks a language, and (after
   piece 5) when the generated file is out of date.

A pull request that adds UI also brings the screen's UI tests (`GUIDELINES.md` §10). The developer's feature smoke
test uses the same app: `./gradlew :selftest:run -Pcases=1.19` opens the Self-Test with only that feature's cases.

## `MVP_SMOKE_TEST.md` becomes generated

Until piece 5 of `qa/README.md` lands, `MVP_SMOKE_TEST.md` is the master list, written by hand: the triage and the
M1 cases are written from it. Then the Self-Test's cases become the **only source** and the file is **generated**
from them (`./gradlew :selftest:smokeCases`), so no case is written twice:

- Each case in code carries what the file shows: its ID, title, steps, expected result, **Checked by** and
  "Testable from". Manual cases take them from their English string resources.
- The file keeps its readable layout, by milestone and feature, so a reviewer reads in a pull request's diff what is
  tested without reading Kotlin. Its header says it is generated and must not be edited.
- The consistency test fails when the committed file differs from what the cases generate.
- The Win, macOS and Linux columns go: results live in the Self-Test's reports and in `qa/runs/M<N>.md`.
- `qa/M1_SMOKE_TEST.md`, the step-by-step guide, is deleted in piece 8: its steps live in the manual cases.

## Languages

The Self-Test's window and every manual case are shown in Spanish when the system is in Spanish and in English
otherwise. English is the source and the language of everything else in the repository; Spanish exists only as
`values-es` resources of the two modules. Case IDs, the report's keys and the bug issues stay in English.

## Never touching the tester's notes

- The automatic part runs in temporary folders (app data and vault copies) that it deletes when it ends.
- Before the manual part, the Self-Test asks the tester to close Notekea, backs up Notekea's app data folder into its
  own folder and prepares the test vaults in its own folder (`Notekea Self-Test` in the tester's documents).
- **Finish and clean up** asks to close Notekea, restores the backup and deletes the test vaults. It never deletes or
  changes a vault the test did not create.

## The report

Plain text, so it pastes into any chat:

```text
Notekea Self-Test report
Notekea 0.1.0-alpha (m1-rc1) · Windows 11 24H2 · es-CO · 1920×1080 at 125%
Automatic: 131 passed, 2 failed, 0 skipped (17 min)
  ❌ 1.6-03 Opening a note from the quick switcher: expected "Lisbon trip" in the tab strip, found nothing
Manual: 28 passed, 1 failed, 2 not run
  ❌ 1.17-02 Dark mode follows the system: "the sidebar stayed white" (screenshot: 1.17-02.png)
Diagnostics: <the last lines of Notekea's log>
```

Screenshots are saved in the Self-Test's folder, which **Open report folder** shows, to send next to the text.

## Releases

The milestone build workflow (`qa/README.md`) packages Notekea and Notekea Self-Test for the three systems with the
same Java runtime and modules (`checkRuntimeModules`), so the Self-Test also catches what only breaks once packaged.
