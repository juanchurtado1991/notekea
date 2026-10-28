# Performance

What each milestone's performance pass measured (`ROADMAP.md`, "Closing a milestone"; `GUIDELINES.md` §8), next to
the passes before it. Budgets are those of `ROADMAP.md` 5.4: the editor alone under 400 MB.

## How the numbers are taken

```bash
./gradlew :composeApp:smokeRun -PidleSeconds=600
```

runs the automated smoke runs (`app.notekea.smokerun`), each test class in a JVM of its own, and writes
`composeApp/build/reports/smoke-runs/memory.csv`: one line per sample, with the Java heap in use after a collection
and the resident memory of the whole process (heap, native, Skia, compiled code), in megabytes. `-PidleSeconds` is how
long the idle steps wait (0, no waiting, in a plain test run); `-PsmokeJvmArgs` passes JVM options to compare.

The memory runs (`ROADMAP.md` 1.18) are the workload of a pass:

- **Personal Notes** (`smoke-data/vaults/Personal Notes`, plus a generated 2,000-line `Long note`): opened, 12 notes
  opened in tabs and closed, a text search, the long note scrolled to its end in Edit and then in Preview, idle one
  minute and the whole idle time, the vault left for the welcome screen, and opened again.
- **Large Vault** (5,000 notes in 50 folders, as `smoke.sh large` writes it): opened, 20 notes opened in tabs and
  closed, a text search over every note, idle, left and opened again.
- **Idle** (Sample Vault): five notes opened and closed, then idle.

The runs drive the real app through Compose's test framework, which adds its own memory to the process: the numbers
compare passes with each other, and the packaged app is measured apart where a budget is checked.

## M1 — 1.18, editor memory

Linux (Pop!_OS 24.04, kernel 7.2, OpenJDK 21.0.11, 24 cores, 62 GB), test JVM, in megabytes (heap / resident).

### Before (main after 1.16)

| Run | Moment | Heap | Resident |
|---|---|---|---|
| Personal Notes | start (welcome screen) | 19 | 242 |
| | opened | 23 | 287 |
| | 12 notes open | 28 | 343 |
| | notes closed | 27 | 350 |
| | text searched | 27 | 350 |
| | long note scrolled in Edit | 40 | 634 |
| | long note scrolled in Preview | 112 | 768 |
| | idle one minute | 39 | 774 |
| | vault closed | 27 | 777 |
| | opened again | 29 | 741 |
| Large Vault | opened | 27 | 359 |
| | 20 notes open | 36 | 489 |
| | notes closed | 32 | 506 |
| | text searched (5,000 notes) | 33 | 507 |
| | idle one minute | 33 | 495 |
| | vault closed | 29 | 496 |
| | opened again | 32 | 511 |
| Idle (Sample Vault) | five notes open | 29 | 372 |
| | notes closed | 30 | 374 |
| | idle one minute | 30 | 358 |
| | idle ten minutes | 30 | 359 |

What it says:

- The Java heap stays small (under 40 MB) except while Preview shows a long note; the process's memory is native:
  Skia's surfaces and caches, text layout and compiled code.
- **A long note is the cost:** scrolling 2,000 lines adds about 280 MB in Edit and 130 MB more in Preview, and none
  of it comes back when the note closes or the vault does.
- Notes opened and closed, a text search over 5,000 notes (the note text cache) and ten idle minutes cost little and
  do not grow.
- Leaving a vault gives nothing back: its ViewModels (tree, watcher, editors, caches) stay alive in the window.

The packaged app idles at 291 MB on the welcome screen (measured in 1.16); settings that trade speed or smoothness for
memory (only the C1 compiler: 228 MB; Skia drawing in software: 161 MB) are not used.
