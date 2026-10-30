# Notekea Self-Test

How Notekea's smoke cases are tested on the testers' machines, and how every feature keeps them up to date. Read it
before adding or changing a case of `MVP_SMOKE_TEST.md` (`GUIDELINES.md` §8). Status of each piece: `qa/README.md`,
"Before the first run".

## The goal

A tester installs one app from the milestone's release, **Notekea QA**: Notekea itself, with its tests and the **Test
Buddy manager**, a small window at the bottom left of the screen. Notekea QA keeps its notes and settings in a folder
of its own, so it never touches a Notekea the tester uses every day.

1. **Automatic part:** one click on **Start the automatic tests** runs every case a program can check on the tester's
   own system, files and screen settings, out of sight, while the tester can keep using the computer. The manager
   says **Running tests** and shows at every moment the time, the progress and what is left, the passed and failed
   counts, the case running now with its steps as they happen, and the last ones done.
2. **Manual part:** the manager walks the tester through the rest, one case and one step at a time, in Notekea
   itself: the steps as a line (✓ done, the current one in bold, the rest to come), "Done, next step", then what
   should have been seen and the case's two answers (a failure takes a note and a screenshot, and the tester's last
   steps in Notekea are added for them). Cases that need it restart Notekea for the tester (a crash test).
3. **Copy report** puts both parts, the machine and the steps behind each failure in one plain text the tester sends
   back (by chat or email), with the Report folder's screenshots and `output.txt`.

Testers are not developers: they never clone the repository, open a terminal, install Java or prepare data by hand.
A manual run takes a fraction of the 5 to 7 hours per system the old step-by-step guide took.

## Three modules: Test Buddy, Test Buddy Crash and Notekea's QA build

| Module | What it holds | Depends on | In Notekea's release |
|---|---|---|---|
| `:test-buddy` | **Test Buddy**, generic: the case types and verbs, the runner, the JUnit bridge, the report, `testBuddyMain` and the manager window. It knows nothing about Notekea, so it could test any Compose Desktop app ([README](../test-buddy/README.md)) | Compose, Compose UI Test, `:test-buddy-crash` | No |
| `:test-buddy-crash` | **Test Buddy Crash**, generic: the crash notice, the crash report with the last steps (`Breadcrumbs`) and the machine (`AppEnvironment`), configured by the app: its name, texts, folder and issue form ([README](../test-buddy-crash/README.md)) | Compose | Yes |
| `:selftest` | **Notekea QA**: every automated and manual case, the smoke data bundled in, `QaMain` (Notekea with the manager), its data folder (`QaAppData`) and its installers; also the memory runs of the performance passes (`smokeRun`) | `:composeApp`, `:test-buddy` | No |

Notekea's release ships no test code: `:composeApp` depends only on `:test-buddy-crash`.

**Test Buddy is written to become a standalone SDK** if it proves useful beyond Notekea: its own package
(`com.testbuddy`), no dependency on any Notekea module (checked by a Konsist test), a public API kept small and
documented in its modules' READMEs, and no Notekea name in its texts (the host app gives its own name). Moving it to
its own repository and publishing it, as Ghost Serializer is, would then need no change to its code.

### How Notekea QA runs

- `QaMain` calls `testBuddyMain(args, suite) { manager -> runNotekea(modules = desktopModules + qaModule(),
  extraWindows = manager) }`: Notekea as always, its app data folder replaced by Notekea QA's own, and the manager as
  one more window.
- **Automated cases run in a process of their own:** Start launches Notekea QA again with a flag, and there
  `testBuddyMain` runs the cases instead of the app, each on a fresh `CaseApp` and fresh copies of the data, and
  tells the manager each case's start, steps and end, one line each. So a case never touches the tester's Notekea,
  never clashes with its Koin, and a crash in a case never closes the app.
- **Crashes:** Notekea's notice and reports come from Test Buddy Crash, configured in `main.kt` with Notekea's texts,
  its `crashes` folder and its GitHub form. Every ViewModel intent's name goes to the last steps
  (`BaseViewModel.onIntent` → `IntentTrail` → `Breadcrumbs`), which crash reports and failed manual cases list.

### Three ways to run one suite

| Where | How | What for |
|---|---|---|
| CI | `class NotekeaSuitePart1Test : TestBuddyJUnit(notekeaSuite.shard(index = 0, count = 4))`: one JUnit test per case ID, the parts at the same time | Every pull request (steps on Linux only) and every night, on Windows, macOS and Linux |
| The developer's machine | `./gradlew :selftest:run -Pcases=1.6` | A feature's smoke test: Notekea QA with only that feature's cases |
| Testers | `./gradlew :selftest:packageQa` (the milestone build does it on a tag) | The "Notekea QA" installers, with Notekea's Java runtime and modules |

### What Notekea's suite adds (`app.notekea.selftest`)

- Cases grouped like the roadmap: `cases/m1/QuickSwitcherCases.kt` holds the cases of 1.6, and so on (the 350-line
  limit applies). Verbs that know Notekea (`openVault`, `openNote`, `waitForResult`) are in `NotekeaVerbs.kt`.
- `CaseApp`: the real Notekea for one automated case, every Koin module with its data in the case's folder and the
  system's dialogs and apps replaced (the pickers answer what the case sets, opening a file is recorded, the trash is
  the vault's `.trash`). The smoke runs of `app.notekea.smokerun`, in this module's tests, use it too.
- The `smoke-data/` folders, bundled as resources at build time with an index of their files (`SmokeData`). The
  scenarios of `smoke.sh` (`prepare`, `outside`, `move`, `edit-note`, `delete-note`) are written in Kotlin
  (`SmokeScenarios`), and `SmokeScenariosTest` checks on macOS and Linux that they build the same files, byte for
  byte and with the same dates, so the developer's smoke data and the testers' are the same.
- The report ends with the last lines of Notekea QA's log (`diagnostics`).
- The last manual case, G-09, is 15 minutes of free use on the test data, answered "Nothing odd" or "I found
  something": it catches what no written case thought of, and every bug it finds becomes a case.
- `pending-cases.txt`: the cases of `MVP_SMOKE_TEST.md` not written yet; the consistency test
  (`SmokeCasesConsistencyTest`) fails while a case is neither written as its **Checked by** says nor listed there.

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

**Automate what is reasonable, not a quota.** Because the manager guides every manual case step by step, a manual
case is cheap for the tester. So there is no percentage to reach: automate the cases a few clear verbs can check, and
leave the rest manual instead of forcing them. The consistency test prints the share of `Self-Test` cases as
information only.

## Every feature keeps it up to date

The Self-Test is never written at the end of a milestone. Every `feature/` and `fix/` pull request, in the step that
adds the behavior:

1. Adds or revises its cases, with **Checked by** (in `MVP_SMOKE_TEST.md` by hand until piece 5; after it, in the
   cases themselves, and regenerates the file).
2. Writes each `Self-Test` case as an `AutomatedCase` named after its ID, in `:selftest`; it runs in CI from then on.
3. Writes each manual case as a `ManualCase`, its numbered steps (the manager shows one at a time) and expected
   result in plain English: the button and menu names as the tester sees them, the shortcut of each system, never a
   terminal command or a file path the tester must type.
4. Adds the data the cases need to `smoke-data/` (bundled automatically).
5. Keeps the consistency test green: it fails when a case of `MVP_SMOKE_TEST.md` has no `AutomatedCase` or
   `ManualCase` with its ID, when one exists for a removed case, and (after
   piece 5) when the generated file is out of date.

A pull request that adds UI also brings the screen's UI tests (`GUIDELINES.md` §10). The developer's feature smoke
test uses the same app: `./gradlew :selftest:run -Pcases=1.19` opens Notekea QA with only that feature's cases.

## `MVP_SMOKE_TEST.md` becomes generated

Until piece 5 of `qa/README.md` lands, `MVP_SMOKE_TEST.md` is the master list, written by hand: the triage and the
M1 cases are written from it. Then the Self-Test's cases become the **only source** and the file is **generated**
from them (`./gradlew :selftest:smokeCases`), so no case is written twice:

- Each case in code carries what the file shows: its ID, title, steps, expected result, **Checked by** and
  "Testable from".
- The file keeps its readable layout, by milestone and feature, so a reviewer reads in a pull request's diff what is
  tested without reading Kotlin. Its header says it is generated and must not be edited.
- The consistency test fails when the committed file differs from what the cases generate.
- The Win, macOS and Linux columns go: results live in the Self-Test's reports and in `qa/runs/M<N>.md`.
- `qa/M1_SMOKE_TEST.md`, the old step-by-step guide, was deleted in piece 8: its steps live in the manual cases.

## Language

The manager and every manual case are in English only, like the rest of the repository (the developer's decision):
plain words a tester who is not a developer can follow.

## Never touching the tester's notes

- Notekea QA keeps its settings, recent vaults and cache in a folder of its own, next to Notekea's (`QaAppData`):
  a Notekea the tester uses every day is never read or changed, so there is nothing to back up or clean up.
- The automatic part runs in another process, in temporary folders (app data and vault copies) it deletes as it goes.
- Each manual case's test vaults are prepared fresh in `Test data` of `<Documents>/Notekea QA`; nothing writes
  outside that folder and Notekea QA's own.

## The report

Plain text, so it pastes into any chat; also saved as `report.txt` in the Report folder:

```text
Notekea QA report
Notekea 0.1.0-alpha (m1-rc1)
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
     Last steps in Notekea:
       10:15:01 Workspace: OnSidePanelToggle
Diagnostics:
Notekea's recent log: …
```

The Report folder, which **Open the Report folder** shows, also holds the screenshots and `output.txt`, everything
Notekea printed (G-02 reads it).

## Releases

The milestone build workflow (`qa/README.md`) packages Notekea and Notekea QA for the three systems with the same Java
runtime and modules (`checkRuntimeModules`), so the QA build also catches what only breaks once packaged. Testers
install Notekea QA only; Notekea's own installer is the release's.
