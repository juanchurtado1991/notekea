# Notekea — Architecture Decisions

`GUIDELINES.md` says *what* principles apply (SOLID, Clean Architecture, Koin, MVI). This document fixes *how*:
the concrete patterns every feature uses, so that every contributor — human or AI — writes features the same way.
When a pattern here and a third-party skill disagree, this document wins.

Decided on 2026-10-23. Changing a decision requires updating this file in the same pull request.

## 1. Package layout

```
app.notekea
├── core
│   ├── domain          Outcome, AppDispatchers, shared domain types
│   ├── presentation    BaseViewModel, design system (theme, tokens, components)
│   └── platform        Platform abstraction (PLATFORM_GUIDELINES.md)
├── di                  App-level Koin module that includes every feature module
└── <feature>           vault, editor, index, llm, chat, sync, …
    ├── domain          Entities, <Feature>Error, repository interfaces, use cases
    ├── data            Implementations of the domain interfaces, DTOs and mappers
    ├── presentation    <Screen>State, <Screen>Intent, <Screen>Effect, <Screen>ViewModel, <Screen>Screen
    └── di              <Feature>Module (Koin)
```

- `domain` depends on nothing but `core.domain` and the Kotlin standard library / coroutines.
- `data` depends on `domain`; platform implementations live in `jvmMain` under the same package.
- `presentation` depends on `domain` (use cases), never on `data`.
- One top-level class, interface or object per file, named after it; DTOs, interfaces and utility functions each in
  their own file (`GUIDELINES.md` §2, "File organization").

## 2. Naming

| Element | Convention | Example |
|---|---|---|
| Use case | Verb + noun + `UseCase`, single `operator fun invoke` | `OpenVaultUseCase`, `GetRelatedNotesUseCase` |
| Domain interface | Noun, no prefix or suffix | `NoteRepository`, `VectorIndex`, `GitSync` |
| Implementation | Named after the technology or source, never `Impl` | `FileSystemNoteRepository`, `LuceneVectorIndex`, `JGitSync` |
| Domain error | `<Feature>Error` sealed interface | `VaultError`, `ChatError` |
| DTO | `<Name>Dto`, only in `data` | `ModelCatalogDto` |
| Mapper | Extension function `toDomain()` / `toDto()` in `data` | `ModelCatalogDto.toDomain()` |
| MVI contract | `<Screen>State`, `<Screen>Intent`, `<Screen>Effect`, one file each | `WelcomeState.kt` |
| ViewModel | `<Screen>ViewModel` | `WelcomeViewModel` |
| Composables | `<Screen>Screen` (stateful, takes the ViewModel), `<Screen>Content` (stateless) | `WelcomeScreen`, `WelcomeContent` |
| Koin module | `<feature>Module` value | `vaultModule` |
| Test double | `Fake<Interface>` in `commonTest` under the feature's package | `FakeNoteRepository`, `FakeLlmEngine` |

## 3. Error handling

Expected failures are **values**, not exceptions.

```kotlin
sealed interface Outcome<out T, out E> {
    data class Success<T>(val value: T) : Outcome<T, Nothing>
    data class Failure<E>(val error: E) : Outcome<Nothing, E>
}

sealed interface VaultError {
    data class FolderMissing(val path: String) : VaultError
    data class PermissionDenied(val path: String) : VaultError
}

suspend fun openVault(
    path: String
): Outcome<Vault, VaultError>
```

- Every feature defines its own `<Feature>Error` sealed interface with the failures the UI must handle.
- Use cases and repository interfaces return `Outcome<T, <Feature>Error>` for operations that can fail
  in expected ways, and plain values for operations that cannot.
- Exceptions thrown by libraries (Okio, JGit, Lucene, llama.cpp) are caught **in `data`** and mapped to the
  feature's error type. They never cross into `domain` or `presentation`.
- `CancellationException` is always rethrown, never mapped.
- Truly unexpected exceptions (bugs) are not caught in `data`; `BaseViewModel.launchGuarded` catches them at the
  edge, logs them (without note content) and moves the screen to its generic error state.
- Every `<Feature>Error` case has a user-facing message resource and is covered by a reducer test.

## 4. MVI

All ViewModels extend one base class, `core.presentation.BaseViewModel`:

```kotlin
abstract class BaseViewModel<S, I, E>(
    initialState: S
) : ViewModel() {
    val state: StateFlow<S>                    // single source of truth for the screen
    val effects: Flow<E>                       // one-shot events, delivered once
    abstract fun onIntent(intent: I)           // the only public entry point
    protected fun reduce(transform: (S) -> S)  // the only way to change state
    protected fun emit(effect: E)
    protected fun launchGuarded(
        onError: (Throwable) -> Unit,
        block: suspend () -> Unit
    )
}
```

- Each screen has `<Screen>State.kt` (immutable `data class` with defaults for the initial state),
  `<Screen>Intent.kt` (`sealed interface`) and `<Screen>Effect.kt` (`sealed interface`), one file each.
- `State` models the UI states explicitly (`isLoading`, `error: <Feature>Error?`, content), so empty, loading,
  error and content are all representable and testable.
- `reduce` lambdas are pure; complex transitions are extracted into pure functions and unit tested directly.
- A screen without one-shot events uses `Nothing` as its effect type instead of an empty `Effect` interface.
  Native dialogs that return a value (the folder picker) are suspend calls behind a domain port, not effects, so
  the ViewModel handles the result and tests use a fake.
- Composables: `<Screen>Screen` collects `state` and `effects` and passes them to `<Screen>Content`, which is
  stateless (`state` + `onIntent`). `<Screen>Content` has one `@Preview` per UI state (empty, loading, error,
  content…) built from sample `<Screen>State` values (`GUIDELINES.md` §6).

## 5. Coroutines and time

- Dispatchers are injected through `core.domain.AppDispatchers` (`main`, `default`, `io`); no code references
  `Dispatchers.IO` / `Dispatchers.Default` directly. Tests inject a `StandardTestDispatcher`.
- Long-running work (indexing, downloads, generation) is exposed as `Flow` and is cancellable.
- Current time comes from an injected `kotlin.time.Clock` (daily notes, sync timestamps), never from
  `Clock.System` inside business code, so dates are testable.
- Observable data (vault contents, index progress, sync status) is exposed as `Flow`/`StateFlow` from `domain`
  interfaces; one-off operations are `suspend` functions.

## 6. Dependency injection (Koin)

- Each feature exposes one `<feature>Module` with its bindings; `di.appModule` includes them all.
- Platform implementations are bound in a `jvmMain` platform module per feature
  (`single<GitSync> { JGitSync(...) }`), selected at startup.
- Bindings are to interfaces: `single<NoteRepository> { FileSystemNoteRepository(...) }`.
- ViewModels are declared with `viewModelOf(::WelcomeViewModel)`; use cases with `factoryOf(::OpenVaultUseCase)`.
- `desktopModules` (jvmMain) lists every module of the desktop app; `main` starts Koin with it and
  `DesktopModulesTest` builds the same graph with `koinApplication` (no global Koin) and resolves its root bindings.
- Modules use the Koin DSL (`module { }`, `factoryOf`, `single<Interface> { }`), not Koin annotations.

## 7. UI text and resources

- Every user-visible string lives in Compose resources (`composeResources/values/strings.xml`), English only for
  the MVP; translations are added later as `values-<lang>/strings.xml` without code changes.
- Composables use `stringResource(Res.string.<key>)`; keys are `snake_case`, grouped by screen prefix
  (`welcome_open_folder`).
- Brand names and file-format literals are constants, not resources.
- Exception: the developer-only design system gallery (`core.presentation.gallery`), never reachable from a
  packaged build, uses constants for its sample texts.
- Icons and fonts live in `composeResources` as well (fonts: see `LIBRARIES.md` §8).

## 8. Testing strategy

| Layer | What is tested | How |
|---|---|---|
| `domain` | Use cases, pure logic (chunking, link resolution, date formats) | `commonTest`, fakes for every interface |
| `data` | Mapping, file system behavior, error mapping | `commonTest` with Okio `FakeFileSystem`; `jvmTest` only for JVM libraries (Lucene, JGit) using temporary directories |
| `presentation` | ViewModels and reducers: every intent, every state transition, every error case | `commonTest`, `kotlinx-coroutines-test` + Turbine, fake use cases |
| Architecture | Layer boundaries and conventions in this document | Konsist tests in `jvmTest` |
| AI quality | Retrieval recall and citation accuracy | Evaluation sets (`ROADMAP.md` 2.8, 3.4), run on demand |

- **Architecture tests** (`composeApp/src/jvmTest/.../architecture/ArchitectureTest.kt`) enforce: domain imports
  no other layer and no framework (Compose, Koin, Okio, Ktor, JGit, Lucene, llama.cpp, `java.io`/`java.nio`);
  presentation does not import data; `<Feature>Error` types are sealed and live in domain; DTOs live in data;
  no `Impl` classes; use cases live in domain with a single `operator fun invoke`; ViewModels extend
  `BaseViewModel` and live in presentation; no `KoinComponent`; no direct `Dispatchers.*` outside
  `JvmAppDispatchers`; no `Clock.System` outside `di`; no `Color(0x…)` outside the theme package; no Material 3 component
  outside the design system (§11); every UI composable has a `@Preview` in its file; one top-level
  class, interface or object per file; no public top-level functions in files that declare a type; constants in
  the class's `companion object`, which is the last element of the class (these file-structure rules also cover
  tests and the `:ktlint-rules` module).
  A new convention in this document comes with its Konsist rule in the same pull request.
- **Unit tests never load a real model.** `FakeLlmEngine` returns deterministic embeddings and scripted
  generations. Tests that need real inference are integration tests, excluded from `check` and run with
  `./gradlew integrationTest`, using the smallest available GGUF model downloaded on demand.
- Unit tests never touch the real user home, network or clock.

## 9. Logging

- Kermit, with one tag per class.
- Never log note content, titles, chunks, prompts, answers or embeddings (`GUIDELINES.md` §13). Log ids,
  counts, durations, error types and paths relative to the vault.

## 10. Reference implementations

New code copies the structure of these features instead of inventing its own. Read them before writing a feature.

| Concern | Reference | Files |
|---|---|---|
| Domain model, port and use case | `core.platform.domain` | `PlatformConventions`, `SystemEnvironment` (port), `ResolvePlatformConventionsUseCase` |
| Platform implementation of a port | `core.platform.data` (jvmMain) | `JvmSystemEnvironment` |
| Koin wiring | `core.platform.di`, `di` | `platformModule`, `jvmPlatformModule`, `appModule`, `desktopModules`, `main.kt` |
| Unit tests with fakes | `commonTest` `core.platform.domain` | `FakeSystemEnvironment`, `ResolvePlatformConventionsUseCaseTest` |
| Dependency graph test | `jvmTest` `di` | `DesktopModulesTest` |
| Theme tokens and shared UI components (§11) | `core.presentation.theme`, `core.presentation.components` | `NotekeaTheme`, `NotekeaColorSchemes`, `NotekeaDimensions`, `NotekeaButton`, `NotekeaTabRow`, `EmptyState`, `ErrorBanner`… |
| MVI screen (State, Intent, ViewModel, Screen, Content) | `vault.presentation.welcome` | `WelcomeState`, `WelcomeIntent`, `WelcomeViewModel`, `WelcomeReducers`, `WelcomeScreen`, `WelcomeContent` (one preview per state) |
| Data adapters on KMP libraries, tested with in-memory fakes | `vault.data` | `OkioVaultFileSystem` (Okio `FakeFileSystem`), `SettingsRecentVaultsStore` (`MapSettings`) |
| ViewModel tests over real use cases and fake ports | `commonTest` `vault` | `VaultFixture`, `WelcomeViewModelTest` |

The pattern in short: a **port** (interface) in `domain` for anything the platform provides, its **adapter** in `data`
of the platform source set, **all decisions in a use case** in `commonMain` tested with a hand-written **fake**, and
**Koin modules** that bind the adapter to the port.

## 11. Design system

Every reusable UI element comes from the design system in `core.presentation.components`, styled only with the
theme tokens of `core.presentation.theme`. Screens compose these components; they never style Material components
themselves.

- **Use before you write.** Buttons, text fields, list items, tabs and tab rows, badges, status items, empty states
  and error banners already exist (see the gallery: `./gradlew :composeApp:run -Pgallery`).
- **Missing piece? Add it to the design system**, not to the feature: a new file in `core.presentation.components`
  named `Notekea<Element>` (or a descriptive name such as `EmptyState`), colors and sizes from the theme, optional
  parts as nullable slots, a `@Preview` of its variants in the same file, and a sample in the gallery in the same
  pull request. Examples still to come: dialogs,
  chips, cards, menus, checkboxes, snackbars, the citation chip.
- **Enforced by Konsist:** outside `core.presentation.components` and `core.presentation.theme`, importing a
  Material 3 component (`Button`, `OutlinedTextField`, `Tab`, `TabRow`, `Badge`, `AlertDialog`, `Card`, chips,
  `Checkbox`, `Switch`, `DropdownMenu`, `Snackbar`… or `androidx.compose.material3.*`) fails the build.
- Layout primitives and plain content stay allowed everywhere: `Text`, `Surface`, `Icon`, `MaterialTheme`, and the
  foundation layouts (`Row`, `Column`, `Box`, lazy lists).
