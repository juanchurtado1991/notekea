# Notekea Self-Test

How Notekea's smoke cases are tested on the testers' machines, and how every feature keeps them up to date. Read it
before adding or changing a case of `MVP_SMOKE_TEST.md` (`GUIDELINES.md` §8). Status of each piece: `qa/README.md`,
"Before the first run".

## The goal

A tester installs two apps from the milestone's release, **Notekea** and **Notekea Self-Test**, and opens the second:

1. **Automatic part:** one click on **Start** runs every case a program can check (the target is at least 80% of the
   cases) on the tester's own system, files and screen settings, and shows each case's ID with ✅ or ❌. The tester
   can keep using the computer meanwhile.
2. **Manual part:** **Manual tests** walks the tester through the rest, one case per screen: what to open, what to
   do, what they should see, and **Passed** / **Failed** (with a note and a screenshot when it failed). The tester uses
   the installed Notekea for these.
3. **Copy report** puts both parts in one plain text the tester sends back (by chat or email).
4. **Finish and clean up** leaves the installed Notekea as it was before the test, ready for everyday use; then the
   Self-Test can be uninstalled.

Testers are not developers: they never clone the repository, open a terminal, install Java or prepare data by hand.
A manual run takes a fraction of the 5 to 7 hours per system the step-by-step guide takes.

## Two modules: an SDK and Notekea's suite

| Module | What it holds | Depends on |
|---|---|---|
| `:selftest-sdk` | The generic Self-Test: the case types, the runner, the report, the Self-Test window (Start, results, Manual tests, Copy report, Finish and clean up) and its own texts. It knows nothing about Notekea, so it could test any Compose Desktop app | Compose, Compose UI Test |
| `:selftest` | **Notekea's suite**: every automated and manual case, the smoke data bundled in, how to prepare it and clean up after, the "Notekea Self-Test" app and its installers; also the memory runs of the performance passes (`smokeRun`) | `:composeApp`, `:selftest-sdk` |

Notekea itself ships no test code: the app depends on neither module.

### What the SDK offers (`app.notekea.selftest.sdk`)

- `AutomatedCase`: an ID (`1.5-04`) and a body that drives the app with Compose UI Test (find by text, click, type,
  shortcuts) and checks what the case expects, down to the files on disk. Each case runs in its own temporary app
  data folder, on fresh copies of the data it names.
- `ManualCase`: an ID, the data it needs prepared, its steps and its expected result as string resources.
- `SelfTestSuite`: what a host app gives the SDK: its name and version, its cases, how to prepare data (`prepare`)
  and how to put the installed app back as it was (`backUp`, `restore`).
- The runner: runs the automated cases one at a time with a timeout, records `Passed`, `Failed` (with the message) or
  `Skipped`, and never stops at the first failure.
- A JUnit bridge: the same suite runs as one test per case ID in `jvmTest`, so CI runs every automated case on
  Windows, macOS and Linux on every pull request. **A case is written once and runs in both places.**
- The window and the report format (below).

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
- **packaged:** it only exists in the installed app (its shortcuts, the crash report window of the packaged build).

Everything else is `Self-Test`. When part of a case is automatic and part needs eyes, it is split into two cases.
The share of `Self-Test` cases is printed by the consistency test and should stay at 80% or above.

## Every feature keeps it up to date

The Self-Test is never written at the end of a milestone. Every `feature/` and `fix/` pull request, in the step that
adds the behavior:

1. Adds or revises its cases in `MVP_SMOKE_TEST.md`, with **Checked by**.
2. Writes each `Self-Test` case as an `AutomatedCase` named after its ID, in `:selftest`; it runs in CI from then on.
3. Writes each manual case as a `ManualCase`, its steps and expected result in **English and Spanish**
   (`values/` and `values-es/` of `:selftest`): plain words, the button and menu names as the tester sees them, the
   shortcut of each system, never a terminal command or a file path the tester must type.
4. Adds the data the cases need to `smoke-data/` (bundled automatically).
5. Keeps the consistency test green: it fails when a case of `MVP_SMOKE_TEST.md` has no `AutomatedCase` or
   `ManualCase` with its ID, when one exists for a removed case, or when a manual case lacks a language.

A pull request that adds UI also brings the screen's UI tests (`GUIDELINES.md` §10). The developer's feature smoke
test uses the same app: `./gradlew :selftest:run -Pcases=1.19` opens the Self-Test with only that feature's cases.

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
