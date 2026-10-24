# Notekea — Code Guidelines

These rules apply to every change in this repository. Code, identifiers, comments and documentation are written in English.
UI behavior per operating system (window frame, shortcuts, menus, dialogs) is defined in `PLATFORM_GUIDELINES.md`.
The concrete patterns that implement these rules (naming, errors, MVI base class, DI, tests) are in `ARCHITECTURE.md`.

## Product principle: friendly first, powerful underneath

Notekea is built for everyday users, without giving up the power it offers. Every user-facing decision follows it:

- **Plain language.** The UI speaks in tasks ("Create a vault", "Find the folder"), never in implementation terms;
  errors say what happened and what to do next, in one sentence, and show the path or name involved.
- **The common path needs no knowledge.** Sensible defaults (a suggested name and location, the last vault reopened
  on launch) so a first-time user succeeds without reading anything.
- **Power is discoverable, not required.** Shortcuts, advanced options and file paths are visible as secondary hints
  (a shortcut next to its button, a muted path under a name), never as the only way to do something.
- **Nothing is a dead end.** Every error or missing state offers an action (locate, remove, retry).
- **Polished by default.** Screens are composed from the design system (§2, `ARCHITECTURE.md` §11) with generous
  spacing and a clear hierarchy; a screen is not done until its previews look right in light and dark.

## 1. SOLID

- **Single responsibility:** one reason to change per class. Split a class when its name needs "and".
- **Open/closed:** extend behavior by adding implementations of an interface, not by editing `when` chains over types.
- **Liskov substitution:** every implementation of a contract must be usable wherever the contract is expected, with no special cases in callers.
- **Interface segregation:** small, focused interfaces (`GitSync`, `VectorIndex`), never a single "god" service.
- **Dependency inversion:** high-level code depends on interfaces declared in `domain`; concrete libraries (JGit, Lucene, llama.cpp) only appear in `data` implementations.
- **Interfaces are 100% abstract**: no method or property with a default body, not even a trivial one (`= false`,
  `{}`), and no extension functions or properties whose receiver is an interface of this project. An interface is
  only a contract; behavior lives in its implementations. When implementers must share default behavior, it goes in
  an abstract class that implements the interface (the `AbstractList` pattern): implement the interface for full
  control, extend the abstract class for the convenience. Enforced by Konsist.

## 2. Clean Architecture

Code is organized **by feature first, then by layer**:

```
app.notekea.<feature>
├── domain        Entities, use cases and interfaces (ports). Pure Kotlin, no framework or platform imports.
├── data          Implementations of the domain interfaces (Lucene, JGit, llama.cpp, SQLDelight, Okio…).
└── presentation  MVI ViewModels and Compose UI.
```

- **Dependency rule:** `presentation → domain ← data`. `domain` never imports from `data` or `presentation`.
- `domain` lives in `commonMain`. `data` lives in `commonMain` when it only uses KMP libraries, and in `jvmMain` (or another platform source set) when it needs a platform library.
- No library type crosses a layer boundary: map library objects to domain models inside `data`.
- **Every reusable UI element comes from the design system** (`core.presentation.components`): screens use
  `NotekeaButton`, `NotekeaTextField`, `NotekeaTab`…, and a missing element is added there, with a gallery sample,
  instead of styling a Material component inside a feature (`ARCHITECTURE.md` §11, enforced by Konsist).

**File organization**

- **One top-level class, interface or object per file**, and the file is named after it. This applies to DTOs
  too: every DTO lives in its own file (`ModelEntryDto.kt`, `ModelCatalogDto.kt`), never grouped.
- **Interfaces always live in their own file**, never next to their implementation.
- **Utilities live in their own file**: extension functions, mappers and helper functions go into a file without
  classes (`<Topic>Extensions.kt`, `<Topic>Mappers.kt`), never as public top-level functions next to a class.
  Private top-level constants and private helpers used only by that file's class are allowed.
- Nested types stay nested: the subtypes of a sealed interface are declared inside it, in the same file.
- **Constants live in a `companion object`** of the file's class (`private const val` inside it), never as
  top-level declarations next to a class. Only files without a class (top-level composables, `main.kt`, Koin
  module files) keep their private top-level constants.
- **The `companion object` is always the last element of its class.**
- Enforced by the Konsist architecture tests (`ARCHITECTURE.md` §8).

## 3. Dependency injection with Koin

- Koin is the only dependency injection mechanism. Do not create dependencies manually inside classes.
- Constructor injection only: no `KoinComponent`, `inject()` or `get()` inside business classes.
- One Koin module per feature (`<feature>Module`), plus platform modules that bind the platform implementations (e.g. the JGit implementation of `GitSync` in a `jvmMain` module).

## 4. MVI in the presentation layer

Each screen has:

- `State`: an immutable `data class` describing everything the UI renders.
- `Intent`: a `sealed interface` with every user action.
- `Effect`: a `sealed interface` for one-shot events (navigation, snackbars), exposed as a `Flow`.
- A `ViewModel` exposing `val state: StateFlow<State>`, `val effects: Flow<Effect>` and a single `fun onIntent(intent: Intent)`.

State changes go through a pure reducer (`(State, Result) -> State`) so they can be unit tested without coroutines or UI.
Composables are stateless: they receive `state` and `onIntent` and never talk to `domain` or `data` directly.

## 5. DRY

- Do not duplicate logic. Extract shared behavior into a function, extension or `domain` use case.
- Constants instead of magic strings and numbers.
- Duplication is only acceptable when it is intentional and documented with the reason (e.g. a measured performance trade-off).

## 6. Granular, self-descriptive functions

- A function does one thing, and its name says what that thing is. If a function needs a comment to explain
  what it does, rename it or split it.
- **Function bodies may not exceed 40 lines.** This is enforced by the `notekea:max-function-length` ktlint rule
  (the signature is not counted). When a function grows, extract private helpers whose names describe each step.
- Long or multi-line conditions go into a well-named `val` (e.g. `val isExpressionOnNextLine = ...`) instead of
  being inlined in the `if`.
- Prefer names over comments: `tryAddKspDependency()` instead of `addKspDependency() // returns false if...`.
- **Every UI element and every screen has a `@Preview`** in its own file, at the end, `private`, named
  `<Composable>…Preview`, and wrapped in `NotekeaPreviewThemes` so it shows the light and dark themes side by side.
  Components get one preview per meaningful variant (e.g. primary, secondary, destructive, disabled). Screens get
  **one preview per UI state**: the stateless `<Screen>Content` receives a sample `<Screen>State` for each case
  (empty, loading, error, content, and any feature-specific state). Enforced by Konsist; theme functions, the
  preview helper and the developer gallery are exempt.
- **Parameters go one per line**, in functions and constructors. Exception: a class whose primary constructor has
  a **single parameter** keeps it on the class line when the line fits in 120 columns:
  `class ResolvePlatformConventionsUseCase(private val systemEnvironment: SystemEnvironment) {`.

## 7. No warnings

- The build fails on any Kotlin warning (`allWarningsAsErrors`). Fix the cause; do not silence it.
- `@Suppress` is only allowed with a comment explaining why the warning cannot be fixed.
- Deprecated APIs are migrated, not suppressed.

## 8. Branches and commits

- **Never commit directly to `main`.** Every feature or fix is developed in its own branch created from `main`:
  - `feature/<short-description>` for new features
  - `fix/<short-description>` for bug fixes
  - `chore/<short-description>` for tooling, build or documentation changes
- Changes reach `main` through a pull request. CI runs on every pull request and every merge to `main`.
- **One branch and one pull request per feature, with granular commits inside.** Prefer several small commits,
  each doing one logical step (e.g. `feat: add Platform interface`, `test: cover shortcut mapping`,
  `feat: bind JVM platform in Koin`), over one large commit. Every commit must leave the build green (the
  pre-commit hook enforces it), so history stays readable, reviewable and bisectable.
- The feature's tests and its `CHANGELOG.md` entry are part of the same pull request (in any of its commits).
- Pull requests are merged with **rebase merging**, so the granular commits land on `main` as they are and history
  stays linear.
- Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/): `feat:`, `fix:`, `test:`, `refactor:`, `docs:`, `chore:`.
- **Manual smoke test before every pull request of a `feature/` or `fix/` branch.** Automated tests do not prove the
  app works as a whole, so the developer runs the app (`./gradlew :composeApp:run`) on **at least one OS** and checks:
  1. The app starts and the main window renders without errors in the log.
  2. The new or fixed behavior works end to end, including its empty, loading and error states
     (use the "Done when" criteria of the feature in `ROADMAP.md` as the script).
  3. Nothing nearby broke: open a sample vault from `smoke-data/vaults`, open and edit a note, and use the features the change touches.
  4. UI changes follow `PLATFORM_GUIDELINES.md` on the OS tested (shortcuts, menus, dialogs).

  **Smoke tests always have data ready.** A feature that needs data to be smoke tested (vaults, notes, conflicts,
  models, a moved or locked folder…) adds it to `smoke-data/` in the same pull request, inside the repository
  whenever Git can hold it (`smoke-data/vaults/`), and otherwise as a step of `smoke-data/smoke.sh` and
  `smoke.ps1`; `smoke-data/README.md` lists what each feature uses. The developer never has to invent test data.

  The pull request records the OS and version used and what was checked. Changes that are platform-specific
  (file system, shortcuts, menus, packaging, Git credentials) must be smoke tested on **each affected OS**.
  `chore/` branches that do not change runtime behavior (docs, CI, tooling) are exempt.
- **Every `feature/` and `fix/` pull request adds its cases to `MVP_SMOKE_TEST.md`**: what a person must check on
  each OS before the release, including cases that only become testable with later features. That file is run in
  full on Windows, macOS and Linux before v0.1.0 and is the main source of bugs before release.
- **The same pull request revises the existing cases its change makes outdated.** The file describes the app as it
  is after the change, not as it was: when a feature replaces or extends behavior an earlier case checks (the note
  tree replacing the "12 notes found" summary), that case is rewritten in place, keeping its ID, so the release
  run never tests something that no longer exists. Cases that a later feature will change carry a note naming
  that feature, and that feature's pull request removes the note when it revises them.
- **Dependabot pull requests** are exempt from the `CHANGELOG.md` entry and from the manual smoke test when they
  only update GitHub Actions, Gradle plugins or build/test tooling; green CI on the three OSes is enough. When they
  update a library used at runtime (Compose, Koin, Ktor, Okio, Lucene, java-llama.cpp, JGit, SQLDelight, Ghost…),
  a smoke test on at least one OS is required before merging, and notable upgrades get a `Changed` entry in the
  changelog.

## 9. Format before every commit

The versioned pre-commit hook (`.githooks/pre-commit`) does this automatically; it is installed by the first build.
It runs, in this order:

```bash
./gradlew ktlintFormat
./gradlew check koverVerify
```

`ktlintFormat` applies the standard rules plus Notekea's custom rules (`:ktlint-rules`) and the reformatted files
are re-staged; `check` runs lint and every test; `koverVerify` enforces the minimum line coverage (80% in every module, UI excluded).
The commit is blocked if any step fails. `git commit --no-verify` is for emergencies only and CI will still fail.

## 10. Unit tests for every feature

- Every new feature ships with unit tests in the same commit.
- Tests must be **useful**: they verify behavior and edge cases (empty input, errors, conflicts, cancellation), not getters or trivial mappings.
- Test `domain` use cases and MVI reducers first; they hold the logic.
- Prefer hand-written fakes over mocks. Use Turbine for `Flow` and `kotlinx-coroutines-test` for coroutines.
- Test names describe the behavior: ``fun `sync keeps both versions when the remote has a conflicting change`()``.

## 11. Changelog

- Every change is recorded in `CHANGELOG.md`, following [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
- New entries go under `## [Unreleased]`, in the matching section: `Added`, `Changed`, `Deprecated`, `Removed`, `Fixed`, `Security`.
- On release, `Unreleased` is renamed to the version and date.

## 12. Cross-platform tests (Windows, macOS, Linux)

- CI runs every test on Windows, macOS and Linux.
- Prefer `commonTest`: if a test can live there, it must. Use `jvmTest` only for code that exists only in `jvmMain`.
- Never hard-code path separators or line endings: build paths with Okio's `Path` and normalize `\r\n` when comparing text.
- Do not depend on the machine: no absolute paths, no user home, no network. Use temporary directories created by the test.

## 13. Privacy

Notekea's core promise is that notes never leave the user's computer. Every change must keep it:

- **Zero telemetry.** No analytics, crash reporting, usage pings or "anonymous" statistics, not even opt-out ones.
- **No network access except for explicit user actions:** downloading a model the user chose, and Git sync to the
  remote the user configured. Each network call is visible in the UI. Any new kind of network access needs an
  issue and an update to this section first.
- **Logs never contain note content**: no note text, titles, chunks, prompts, answers or embeddings. Log identifiers,
  counts, durations and error types instead; file paths only relative to the vault.
- **Credentials** (Git tokens, SSH passphrases) live only in the OS keychain, never in files, settings or logs.
- **Model downloads are verified** with a checksum before they are loaded.
- **Everything stays in user-visible places:** the vault folder and the platform's application data directory
  (see `PLATFORM_GUIDELINES.md`). Deleting the vault and that directory removes every trace of the user's data.

## 14. Dependency updates

Updating too often breaks things and steals time from features; updating too rarely creates risky big jumps and
leaves vulnerabilities unpatched. The policy:

- **Security updates are applied as soon as they arrive** (Dependabot security updates and alerts are enabled).
- **Routine updates are monthly and grouped:** Dependabot opens at most one pull request per ecosystem per month
  with all minor and patch updates (`.github/dependabot.yml`).
- **Major versions are upgraded by hand**, one per branch, after reading the release notes, because they change APIs.
- **Kotlin, KSP, Compose Multiplatform and Ghost are upgraded together and by hand.** They are tightly coupled:
  check that Compose and KSP support the new Kotlin version and that Ghost is tested against it before bumping.
  Dependabot ignores them; security alerts for them still appear in the Security tab.
- Every upgrade goes through a `chore/` branch and a pull request with green CI on the three OSes. Upgrades of
  runtime libraries also need the manual smoke test (§8) and a `Changed` entry in `CHANGELOG.md`.
