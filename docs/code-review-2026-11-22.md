# Code review of the whole project, 2026-11-22

Nine read-only reviewers went over the app (about 120k lines), its tests (110k) and the other modules (28k). The
bugs they found are fixed in `dba8eb15`, each with its test. This file is what is left: one bug that became a small
feature, the measurements under way, and the refactor, in the order to do it.

Rules for all of it: `AGENTS.md`; one Gradle build or measurement at a time; commit with `--no-verify`; never commit
`BundledModelCatalogSource.kt` nor `smoke-data/vaults/Sample Vault` or `Ghost Serializer` (the developer's local
edits: they make `SampleVaultTest`, `SmokeDataTest` and six Auto-Test index cases fail locally, and only those).

## 1. A synced file that grows past 100 MB (decided by the developer)

It leaves the repository (it stays in the vault) **and a dialog tells the person**. The first half is written, not
committed: `git apply ~/.cache/notekea-loop-runs/large-tracked-files.patch` (`LargeFiles.leaveOut(changed = …)`
untracks and excludes; its test is in `JGitCommitTest`; `SyncSafetyTest` line 102 then passes `changed =`).

What is missing: `LargeFiles.leaveOut` returns the paths and `JGitCommit.all` drops them. Carry them out
(`GitRepository.commitAll` → `SyncEngine` → a `StateFlow` on `VaultSync` of the files left out and not yet
acknowledged → `SyncStatusState` → a dialog over the workspace, as `RepairDialog` is shown). The dialog names the
files and says: over 100 MB, no Git host takes them, they stay on this computer and are no longer synced; one that
was synced before leaves the other devices at their next sync. One button. Strings in `values` and `values-es`, a
smoke case in `MVP_SMOKE_TEST.md` and its Auto-Test case (`SmokeCasesConsistencyTest` checks both), a ViewModel
test, `CHANGELOG.md`.

## 2. Measurements under way

All on pool round 10 (`scripts/loop-eval/measure.sh <label> pool <small|medium|large> 10`), read by cause only
(`compare.py`, `buckets.py`), logged in `docs/loop-goal-plan.md` section 7.

- **Baselines at the old code, Q4_K_M** (`r10-base`): 2B 178 of 439, 4B 321, 9B 328.
- **The review's fixes** (`s68-review`, the code of `dba8eb15`, Q4): the 4B first, then the 9B and the 2B. The
  figures cause (17 on the 4B) is what `FigureValues` and the tally's key aim at. Several chat fixes went in
  together, so a fall is read by cause before anything is reverted.
- **Q5_K_M against Q4_K_M** on that same code: `NOTEKEA_MODELS=~/.cache/notekea-spike/models-q5` (the folder holds
  the Q5 files under the names the tests expect) with labels `s68-q5`. One round moves 2 to 3 points by itself: under
  +4, measure a second round (seed 9, baselines `r9-base`) before saying Q5 is better. If Q5 wins, the catalog
  (`model-catalog.json`: file names, urls, sizes, sha256, minimum memory) is the developer's decision.

## 3. Refactor, one block a commit, tests green after each

Lowest risk first. Each line: what repeats → what replaces it (lines saved).

1. **Core helpers.** `(x as? Outcome.Success)?.value` at 43 sites → `Outcome.valueOrNull()`, `errorOrNull()`,
   `map` (120). Hand-padded ISO dates at 10 sites → `CalendarDate.iso`, `CalendarDate.ofIso`, and
   `LocalCalendar.today(clock)` for 13 sites (110). `.value.removeSuffix(MARKDOWN_EXTENSION)` at 30 sites →
   `RelativePath.bare()` moved from `TaskFrame.kt` to core (40). Line start and end by hand at 17 sites, and the
   walk through a `FenceTracker` at about 17 → `CharSequence.lineStartAt`, `lineEndAt`, `forEachLine`,
   `forEachLineOutsideCode` (150). Six private `ACCENTS` maps → `TextFolder` as an object (45; `FrameWords` is
   measured by the evals: move it last and measure).
2. **`BaseViewModel`.** `logger` + `LOG_TAG` + a one-line `logError` in 34 view models → `protected val logger` and
   `launchLogged(message)` (120). Four `*Host` objects that forward `state`/`reduce`/`emit`/`launch` → `hostOf` (40).
   `launchDebounced(key, pause)` for `FilesSettingsViewModel`, `MemorySettingsViewModel` and the editor's three.
3. **Tests.** `LoopHarness` for the 12 loop tests (500). `ViewModelTestBase` for 47 files' dispatcher set-up and
   25 `collectEffects` (400). `noteTextCache(store)` for 32 constructions (90). `EvalModels`, `EvalReports`,
   `EvalProperties` for the `*ChatModelTest` (400; slots, default size and GPU stay explicit: they change scores).
   `themedScene` and `SceneClock` for the scene tests (300). `TwoComputers` for the JGit tests (100).
4. **Auto-Test, Test Buddy, ktlint.** `openPlayground()` and `openSample()` (185). Public `await`, `shown`,
   `awaitShown` in `CaseScope` (170). One result codec (`RunnerEvent` and `ResultStore`, 75). Shared constants in
   `CaseVaults.kt` (50). `NotekeaRule` base and an `Emit` typealias for the 16 rules (150).
5. **Design system.** `NotekeaSettingsChoiceRow` and `NotekeaSettingsSwitchRow` with the spotlight inside (150).
   `NotekeaFormDialog` for four dialogs and `NotekeaDialogButtons` in platform order for three (130; it also puts
   the buttons of `ClipDialog` and `ConflictDialog` in Windows' order). `NotekeaMediaCard` (80). `NotekeaMutedText`
   for nine private copies (60). `FieldKeys.kt` (50). `NotekeaMenuAnchor` (90, compare the looks).
6. **Domain.** `RewriteNoteUseCase` for the five "edit a note only if unchanged" use cases (70). `LlmEngine.textOf`
   for about 12 collect-into-a-`StringBuilder` sites (100). Okio path helpers: `RelativePath.isHidden`, one `stat`
   (100). `GhostFrontmatterParser` replaced by `NoteMetadata.of(properties)` (130; run its tests against it).
   Editor: `TextFieldState.apply(edit)`, `TextEdit.replacing`, embed target parsing in one place, one
   `ToolbarEntry` list for the toolbar and the More menu (160). `CachedSettingsStore<T>` for three stores (80).
7. **Blind exams.** `answer`, `change`, `ask`, `trap` builders in `blind/BlindDsl.kt` and one `BlindExam` built by
   `exam(id, vault, …)`, the 59 `Blind*Cases.kt` gone (3,500). **First** write a golden file from today's objects
   (every exam's id, vault, sealed and each case's fields, in order) and a test that the new source reproduces it:
   `BlindPool.sampleOf(seed)` shuffles the list, so order and content must not move or the logged runs stop being
   comparable.

Leave alone: the loop's `heldOf` objects and the tools' declarations (a base saves a line a site and hides the
order of the checks); the per-feature effects and error types; the wikilink and inline regexes (different groups).

## 4. For a stronger model, with the developer

- **`NoteTextCache` is a Koin `factory`**: each consumer keeps its own copy of every note's text. One shared cache
  needs keys by vault and another eviction than `retainAll(notes)`, which drops everything when a caller passes a
  subset.
- **`WorkspaceViewModels` is built anew on every recomposition** of `VaultWindow`. A `remember` there passes the
  file and function limits only by moving code out; measure the recompositions first.
- **Dead tools**: `NoteTool`, `PlanTool`, `NotedFact`, `PlanStep` and `TurnWork`'s plan and notes are registered in
  no toolset, so `LoopPrompt`'s `work` is always empty, while `BuiltInSkills` still tells the model to "Note".
  Removing them changes a prompt: measure.
- **The blind cases as data files** (2,573 cases, about 19,000 lines of Kotlin to one JSONL an exam): after block 7
  and the measuring, with the same golden file as the guard.
- Smaller bugs reported and not fixed: `SyncEngines.engines` is an unsynchronised map; `LlamaLlmEngine.withModels`
  checks `isClosing` outside its lock; `FileChatHistoryStore` takes its lock after the dispatcher hop;
  `TreeRenaming.withPending` keeps a stale rename; `embedChunks` embeds every chunk again after `TextTooLong`;
  `awaitReply` in the Auto-Test's chat verbs can return before the reply.
