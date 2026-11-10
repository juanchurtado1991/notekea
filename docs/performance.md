# Performance

What each milestone's performance pass measured (`ROADMAP.md`, "Closing a milestone"; `GUIDELINES.md` §8), next to
the passes before it. Budgets are those of `ROADMAP.md` 5.4: the editor alone under 400 MB.

## How the numbers are taken

```bash
./gradlew :auto-test:smokeRun -PidleSeconds=600
```

runs the automated smoke runs (`app.notekea.smokerun`, in `:auto-test` since the Auto-Test), each test class in a JVM of its own, and writes
`auto-test/build/reports/smoke-runs/memory.csv` (it appends: delete it before a run): one line per sample, with the Java
heap in use after a collection, the resident memory of the whole process (heap, native, Skia, compiled code), the peak
since the last sample and the seconds the step took, in megabytes. `-PidleSeconds` is how
long the idle steps wait (0, no waiting, in a plain test run); `-PsmokeJvmArgs` passes JVM options to compare;
`-PrealModel` runs on the real embedding model instead of the stand-in.

The memory runs (`ROADMAP.md` 1.18) are the workload of a pass:

- **Personal Notes** (`smoke-data/vaults/Personal Notes`, plus a generated 2,000-line `Long note`): opened, 12 notes
  opened in tabs and closed, a text search, the long note scrolled to its end as it reads and then in Source (in
  Edit and then in Preview until One editing mode), idle one
  minute and the whole idle time, the vault left for the welcome screen, and opened again.
- **Ghost Serializer** (`smoke-data/vaults/Ghost Serializer`: a code repository as 371 notes of 250–550 words, one per
  Kotlin file, with their links): opened, 20 of its larger notes opened in tabs and closed, a text search over every
  note, idle, left and opened again. It replaced, during 1.18, a generated "Large Vault" of 5,000 near-empty notes: the
  tables below that name it were measured on that vault.
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

### After the code cuts (1.18)

Each open vault has its own ViewModels, cleared when it closes; folder card pictures are decoded at card size (at most
60 kept); the editor keeps only the pictures of the note on screen; searches sort only the results shown; names compare
without a pattern; list keys read only the cursor's line.

`-PidleSeconds=600`:

| Run | Moment | Heap | Committed heap | Resident |
|---|---|---|---|---|
| Personal Notes | opened | 23 | 64 | 306 |
| | 12 notes open | 28 | 64 | 354 |
| | notes closed | 27 | 64 | 362 |
| | text searched | 27 | 64 | 362 |
| | long note scrolled in Edit | 40 | 147 | 616 |
| | long note scrolled in Preview | 112 | 234 | 761 |
| | idle one minute | 39 | 147 | 766 |
| | idle ten minutes | 36 | 137 | 678 |
| | vault closed | 26 | 100 | 685 |
| | opened again | 27 | 94 | 690 |
| Large Vault | opened | 27 | 64 | 368 |
| | 20 notes open | 36 | 137 | 557 |
| | notes closed | 32 | 117 | 554 |
| | text searched (5,000 notes) | 33 | 117 | 535 |
| | idle one minute | 33 | 117 | 516 |
| | idle ten minutes | 33 | 117 | 516 |
| | vault closed | 25 | 94 | 518 |
| | opened again | 25 | 90 | 524 |
| Idle (Sample Vault) | notes closed | 30 | 64 | 368 |
| | idle ten minutes | 30 | 64 | 357 |

Ghost Serializer, after the cuts (`-PidleSeconds=600`):

| Moment | Heap | Committed heap | Resident |
|---|---|---|---|
| opened | 24 | 64 | 334 |
| 20 notes open | 35 | 80 | 468 |
| notes closed | 29 | 80 | 477 |
| text searched (371 notes) | 31 | 80 | 477 |
| idle one minute | 31 | 80 | 451 |
| idle ten minutes | 31 | 80 | 451 |
| vault closed | 25 | 80 | 454 |
| opened again | 25 | 80 | 456 |

The heap a closed vault leaves drops (29–33 MB to 25–27 MB), but the process does not shrink: what it freed is
native memory the C library keeps, and heap the JVM keeps committed.

### Where the resident memory goes

Measured with the columns added in 1.18 (`heap_committed_mb`, Skia's caches):

- **Committed heap:** the JVM grows its heap to 120–230 MB while a long note is laid out or Preview renders it, for
  30–110 MB in use, and keeps much of it.
- **Freed native memory not returned:** glibc keeps what Skia and text layout freed in its arenas (up to eight per core:
  192 on the 24-core machine measured). Trimming it gives 130–150 MB back after a minute idle.
- **Skia's own caches** (fonts, resources) stay under 1 MB: not a cost.
- **A long note in the editor** still costs about 170 MB while it is open, in text layout for the whole note; cutting
  it means laying out only the visible part of the note, an editor change beyond this pass.

Runtime settings compared (resident MB, Linux, `-PidleSeconds=60`):

| Moment | Default | `-XX:TrimNativeHeapInterval=10000 -XX:G1PeriodicGCInterval=15000` | Those and `MALLOC_ARENA_MAX=2` |
|---|---|---|---|
| Large Vault, 20 notes open | 540 | 571 | 479 |
| Large Vault, idle one minute | 515 | 388 | 380 |
| Large Vault, vault closed | 522 | 396 | 397 |
| Personal Notes, long note in Preview | 775 | 704 | 669 |
| Personal Notes, idle one minute | 779 | 618 | 577 |

The two JVM options only act while the app is idle (a trim every ten seconds, a collection when idle for fifteen); the
arena limit needs an environment variable, which the packaged app's launcher cannot set.

## M2 — 2.9, indexing and search

Same machine and JVM. Budgets of `ROADMAP.md` 5.4: indexing or searching under 700 MB and back to the editor's level
after; the editor alone under 400 MB. The tables down to the fork describe `main` before this pass's fix; "The fix" at the end is what shipped.

### The packaged app

Ghost Serializer, 371 notes, real window, real model, resident megabytes:

| Moment | Result |
|---|---|
| Welcome screen, idle | 330 (291 in 1.16) |
| Startup to the window | 0.87–0.92 s |
| First index, peak | 894 |
| After indexing, idle | 893 |
| After the model is let go and loaded again | 1172 |

**The model leaks.** Each load and unload keeps about 275 MB (110 of file mapping, 165 anonymous, 11 threads): ten cycles
take a JVM from 425 MB to 2.9 GB. `Java_de_kherud_llama_LlamaModel_delete` in `de.kherud:llama:4.2.0` only stops the task
queue, and the line that deletes the server is commented out; 4.2.0 (June 2025) is the last release. So letting the model
go after three idle minutes frees nothing, and the next search or edit loads another copy.

Loading the model costs about 290 MB (121 of file, 170 anonymous). Threads, `parallel` and batch size do not change that,
only the speed; the vectors stay the same.

Where 915 MB went while indexing (`-XX:NativeMemoryTracking=summary`, which adds its own cost): JVM 344 (heap 98,
metaspace 67, GC 75, code 53, symbols 27), model about 290, the rest (Skia, text layout, what glibc keeps) about 280. Live
objects are small: 20 MB of heap after indexing. The heap's committed peak of 1 GB is not resident: capping it barely
moves the resident peak.

### The test runs

The stand-in embedder, `-PidleSeconds=0`, median of three; the Idle run at `-PidleSeconds=600`. Resident:

| Run | Moment | 1.18 | 2.9 |
|---|---|---|---|
| Personal Notes | opened | 306 | 522 |
| | 12 notes open | 354 | 723 |
| | long note scrolled in Edit | 616 | 890 |
| | long note scrolled in Preview | 761 | 1057 |
| | vault closed | 685 | 1061 |
| Ghost Serializer | opened | 334 | 544 |
| | 20 notes open | 468 | 787 |
| | vault closed | 454 | 795 |
| Idle (Sample Vault) | idle ten minutes | 357 | 598 |

Committed heap went from 64–80 to about 225, and heap in use from about 30 to about 75: a vault now indexes while it
opens. The growth in the test JVM is far larger than in the packaged app (welcome screen +39 MB); this pass did not
attribute all of it. On the real model the runs that open several apps in one JVM climb by about 280 MB per app, up to
2.2 GB, which is the same leak: use the stand-in to compare them.

Idle does not grow over ten minutes, and closing a vault returns nothing. First index: Sample Vault (40 notes) 4.4 s,
Ghost Serializer 48 s (26 s on the stand-in). Incremental index of one note: 0.5–0.7 s. One embedding: 8.9 ms. A search
shows its first results in 0.2–0.4 s (0.7 s when the model has to load); keyword results can come first, so this is
not the latency of the meaning search.

### Options tested on the packaged app

The same vault, resident, including the child process where there is one. Child means the model in a process of its own
that stops after 30 idle seconds. This was a prototype whose worker used the system's `java`, not the packaged runtime.

| Option | Index peak | Idle after | After a reload | Settled |
|---|---|---|---|---|
| Today | 894 | 893 | 1172 | 1172, and growing |
| Model always loaded | 821 | 816 | 818 | 818 |
| Child | 934 | 532 | 917 | 535 |
| Child, trim | 871 | 414–421 | 866 | 432 |
| Child, trim, two compiler threads | 806 | 379–384 | 775 | 377 |
| The same, `-Xmx512m` | 791 | 361 | 752 | 356 |
| The same, `-XX:MaxNewSize=64m` | 809 | 383 | 787 | 382 |
| Child, trim, C1 only | 768 | 339–347 | 726 | 342 |
| Child, trim, C1 only, software drawing | 723 | 283 | 670 | 285 |
| Model always loaded, trim, C1 only | 661 | 615–631 | 625 | 621 |

Trim is `-XX:+UnlockExperimentalVMOptions -XX:TrimNativeHeapInterval=10000 -XX:G1PeriodicGCInterval=15000`, where the
native trim needs glibc, so Linux only; the periodic collection is not tied to a system. Two compiler threads is `-XX:CICompilerCount=2`. No option reached both 700 MB at the peak and 400 MB after.

Speed in the test JVM, median of three, seconds:

| Step | Default | Two compiler threads | C1 only |
|---|---|---|---|
| Long note scrolled in Edit | 27.3 | 27.8 | 69.7 |
| Long note scrolled in Preview | 9.0 | 9.1 | 17.9 |
| 20 notes opened | 13.7 | 13.9 | 14.0 |
| Editor typing case | 1.8 | 2.0 | 2.3 |

The typing case times a whole scripted case, not a key press, and its rounds spread 1.7–3.3 s, so it shows no difference.
Startup: 0.94 s with two compiler threads, 0.87 s with C1, against 0.87–0.92 s. The child's first vector takes 0.7 s
against 0.5 s for a load in the process. First index stays at 45–50 s in every configuration. `-XX:MaxHeapFreeRatio=30`
raised the peak to 1067 MB and did not help after. The Serial collector raised it too (1000 against 883, test JVM). The
compiler saving depends on the core count: a machine with fewer cores already runs fewer compiler threads.

### The maintained fork of java-llama.cpp

`net.ladenthin:llama:6.0.0` (MIT, on Maven Central, llama.cpp b11968) frees the model on close, waiting for its worker
thread first. Tested in place of `de.kherud:llama:4.2.0`, as a temporary change, Linux only.

Ten load and unload cycles in a bare JVM, resident: 352 to 2769 MB today; 352 to 357 MB on the fork. With the model
closed: 248 MB, and 122 MB with trim. A hundred cycles, a third of them closed during a call, gave no crash: the call
gets a `LlamaException`. Vectors are not identical (lowest cosine 0.99987 over 601 texts), 3.7 ms a text against 4.4.

The packaged app, Ghost Serializer, trim and two compiler threads, resident:

| Option | Index peak, three runs | Idle, model let go | Model loaded, idle |
|---|---|---|---|
| Fork | 669–674 | 375–387 (three reloads, no growth) | 615–646 |
| Child | 801–803 | 374–384 | 771–780 |

One longer fork run peaked at 945 MB in its first 75 seconds, and that was not explained or repeated. The model is let
go after three idle minutes on the fork and 30 seconds on the child.

Retrieval on the fork: recall@5 0.94 on Sample Vault and 0.90 on Ghost Serializer, the same as 2.8 measured (baselines
0.92 and 0.86). All 10 `*RealModelTest` cases pass. The Linux package grows from 225 to 244 MB and the extracted natives to 74 MB. The default
log level writes the text of every embedded note to stderr: `setLogVerbosity(1)` stops it. There is no Intel macOS native.

### The fix

Shipped: the fork, the model on one thread of its own, two compiler threads and the idle collection on the three systems,
and the native trim on Linux. The packaged app as built, Ghost Serializer, twelve minutes with two reloads:

| Measure | Before | After | Budget |
|---|---|---|---|
| Index peak | 894 | 662 | 700 |
| Idle, model let go | 893 | 383–392 | 400 |
| After a reload, let go | 1172, and growing | 390–392 | 400 |
| Model loaded | 893 | 620–636 | 700 |
| Startup to the window | 0.87–0.92 s | 0.91–0.92 s | |

**Loading on one thread matters as much as the fork.** Thirty loads across a pool of threads, on the fork with no trim, took a
bare JVM to 3 GB: the model is freed, but glibc keeps what each thread freed in that thread's arena, and the next load, on
another thread, cannot reuse it. On one thread the same memory is reused and stays flat, which also holds where trim does
not act. `LlamaTextEmbedderRealModelTest` covers it on Linux, and the log level too.

The idle collection and the trim wake the app: idle, it uses 0.26% of one core with them and 0.08% without.

Not measured: Windows and macOS. Not cut: the growth of the test runs against 1.18, which the packaged app does not show,
and the cost of a long note open while indexing.

## Post-MVP — One editing mode

Linux, test JVM, the stand-in embedder, `-PidleSeconds=0`, one run of the Personal Notes memory run
(`./gradlew :auto-test:smokeRun --tests "*PersonalNotesMemoryRunTest*"`). Edit and Preview are gone: the note is a
lazy list of blocks drawn as they read, with one text field over the cursor's blocks, and Source is the whole note in
one text field. In megabytes; the 2.9 column is the median of three of that pass.

| Moment | Heap | Committed heap | Resident | Resident in 2.9 |
|---|---|---|---|---|
| opened | 56 | 113 | 538 | 522 |
| 12 notes open | 77 | 140 | 657 | 723 |
| notes closed | 76 | 140 | 666 | |
| long note scrolled as it reads | 81 | 144 | 703 | 890 (Edit) |
| long note scrolled in Source | 92 | 170 | 855 | 1057 (Preview) |
| vault closed | 76 | 170 | 855 | 1061 |

What it says:

- **The long note stopped being the cost.** Scrolling its 2,000 lines adds 37 MB to the process; it added about 170 MB
  in Edit and 170 MB more in Preview. Only the blocks on screen are composed, and they leave when they scroll away.
- Source is the old editor: the whole note laid out in one text field, which adds about 150 MB and types as slowly
  as Edit did on a long note (`docs/spikes/2026-11-09-one-editing-mode.md`: 1,380 ms per key against 6 ms).
- As before, nothing comes back when the note or the vault closes.

Not measured: Windows and macOS, the packaged app, and a median of several runs.
