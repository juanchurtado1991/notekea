# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

- The open vault shows its notes in a sidebar tree instead of the "N notes found" summary, and opening a vault no
  longer walks it twice.
- Features never depend on each other: the app composes them through slots (`ARCHITECTURE.md` §1, enforced by
  Konsist).

- Plan for M3: answers from small local models (~2B, ~4B, ~8B) are made precise by a pipeline that reranks
  retrieval, extracts evidence in parallel and verifies every sentence against the notes, plus an opt-in Deep
  answer mode that writes several drafts in parallel and keeps the best verified one (`ROADMAP.md` 3.0, 3.5, 3.6;
  `ARCHITECTURE.md` §12). The sample vault (1.16) and evaluation sets move to `smoke-data/`.
- Contribution workflow: a feature is one branch and one pull request made of granular commits (each keeping the
  build green), merged with rebase merging, instead of a single commit per feature.
- Dependabot pull requests that only touch CI or build tooling are exempt from changelog entries and smoke tests;
  runtime library upgrades still require a smoke test.
- Dependabot runs monthly with one grouped minor/patch PR per ecosystem; major versions and Kotlin, KSP,
  Compose and Ghost are upgraded by hand (`GUIDELINES.md` §14). Security updates stay immediate.
- Dependencies: Kotlin 2.4.21, ktlint 1.8.0 (Gradle plugin 14.2.0), Gradle 9.8.2, JUnit 6.1.3, slf4j 2.0.20;
  CI actions `checkout` v7, `setup-java` v6, `setup-gradle` v6 and `upload-artifact` v7.

### Fixed

- Status bar items and tab labels stay on one line with an ellipsis in narrow windows instead of wrapping and being
  clipped; tab badge numbers never split across lines.
- Component previews stack the light and dark themes vertically, so Android Studio's limited preview width shows both.

### Added

- Vault tree and note list (ROADMAP 1.4): a resizable (200–480 dp) and hideable (`Mod+\`) sidebar with the vault's
  folders and Markdown notes, note counts, natural sorting by name or date, keyboard navigation, a context menu and
  shortcuts (Mod+N new note, F2 / Enter on macOS rename, Delete / Cmd+Backspace on macOS delete). Notes and folders
  are created, renamed in place and deleted to the system trash (`gio trash` on Linux), falling back to the vault's
  `.trash` folder; renaming updates the wiki and Markdown links of the notes that point to the entry and says how many changed. The
  tree follows outside changes through a file watcher, reads frontmatter titles, tags and aliases with Ghost, and
  opens 5,000 notes in under a second from a SQLite cache (`notekea.db` in the app data directory). Expanded
  folders are remembered per vault; sort, sidebar width and visibility for all vaults.
- Design system: `NotekeaIconButton`, `NotekeaTreeItem`, `NotekeaInlineTextField`, `NotekeaSkeletonRow`,
  `NotekeaContextMenuArea`, `NotekeaConfirmDialog` (platform button order) and `NotekeaSplitPane`; error banners
  stack their buttons when narrow; icons from Lucide through `NotekeaIcons` (a Konsist rule keeps the library there).
- Smoke data for 1.4: a Playground Vault to change freely, simulated outside changes, a generated 5,000-note vault
  and a cache reset in `smoke.sh` / `smoke.ps1`.

- Smoke test data in `smoke-data/`: sample vaults (`Personal Notes`, `Work Obsidian`, `Empty Vault`) and
  `smoke.sh` / `smoke.ps1` for scenarios Git cannot hold (moved and unreadable folders, first launch); a rule in
  `GUIDELINES.md` §8 requires every feature to ship the data its smoke test needs.
- Open a vault (ROADMAP 1.3): a welcome screen to open a folder with the native picker (Ctrl+O / ⌘O anywhere) or
  create a new vault with a suggested name and location; the last vault reopens on launch; recent vaults show
  their folder and when they were opened, and a moved folder can be located or removed. Settings live in
  `settings.properties` in the app data directory. It is the reference MVI screen (`ARCHITECTURE.md` §10).
- Code rule: interfaces are fully abstract, with no default bodies and no extension functions or properties on them
  (enforced by Konsist).
- Product principle in `GUIDELINES.md`: friendly first, powerful underneath (plain language, sensible defaults,
  shortcuts as visible hints, no dead ends).
- Design system: `NotekeaCard`, `NotekeaWordmark`, a low-emphasis `TEXT` button style, shortcut hints on buttons,
  and disabled rows with trailing content in `NotekeaListItem`.

- Android target in `composeApp` (AGP 9.4.1 KMP library plugin, no Android app yet) so Android Studio renders the
  common `@Preview`s; all common code now also compiles for Android, an early check for the mobile phase.

- Theme and design system (ROADMAP 1.2): "cobalt and tangerine" palette with light and dark schemes, bundled Inter and JetBrains Mono,
  type scale, 6 dp / 10 dp shapes, spacing and motion tokens; components `NotekeaButton`, `NotekeaTextField`,
  `NotekeaListItem`, `NotekeaTab`, `StatusBarItem`, `EmptyState` and `ErrorBanner`; a WCAG AA contrast test for the
  palette; and a developer gallery (`./gradlew :composeApp:run -Pgallery`).
- Design system rule: every reusable UI element comes from `core.presentation.components` (`NotekeaTabRow` added);
  a Konsist test fails the build if a screen imports a Material 3 component directly.
- IDE previews: every design system component and `App` have `@Preview`s in the light and dark themes
  (`NotekeaPreviewThemes`); a Konsist test requires a preview for every UI composable, and screens preview each
  UI state.

- Platform conventions (ROADMAP 1.1): `PlatformConventions` resolved at startup with the primary shortcut modifier,
  quit shortcut, dialog button order and per-user application data directory for Windows, macOS and Linux;
  Koin is started on launch. It is the reference implementation for new features (`ARCHITECTURE.md` §10).
- Architecture rule: only `JvmSystemEnvironment` may read the operating system name.
- Status tracking in `ROADMAP.md` (⬜ / 🚧 / ✅ per feature and a count per milestone).
- `MVP_SMOKE_TEST.md`: the manual test run for Windows, macOS and Linux before v0.1.0; every feature adds its cases.
- Code rules: single-parameter class constructors stay on one line; constants live in the class's
  `companion object`, which is the last element of the class (enforced by Konsist).
- The desktop JVM runs with `--enable-native-access=ALL-UNNAMED`, removing the Skiko native-access warning.

- Kotlin Multiplatform project skeleton with a Compose Desktop window (`composeApp`, JVM target).
- Domain interfaces for the core features: `LlmEngine`, `VectorIndex`, `GitSync` and `FileWatcher`.
- Core building blocks: `Outcome` for typed errors, `AppDispatchers` (`JvmAppDispatchers` on desktop) and
  `BaseViewModel` for MVI screens, with unit tests.
- Konsist architecture tests enforcing the layer boundaries and conventions of `ARCHITECTURE.md`, including one
  top-level type per file (DTOs and interfaces each in their own file) and utilities in separate files.
- Version catalog with the MVP libraries (Ghost, Koog, Lucene, java-llama.cpp, JGit, SQLDelight, Koin, among others).
- ktlint configuration (`.editorconfig`) and the `:ktlint-rules` module with the custom rules
  `expression-on-assignment-line`, `max-function-length` (40-line function bodies) and `parameter-order`.
- GitHub Actions CI that runs lint, every test and the coverage check on Windows, macOS and Linux on each pull
  request and merge to `main`.
- Kover coverage with an 80% minimum line coverage enforced per module (Composable UI excluded), so a
  well-tested module cannot hide an untested one.
- Versioned pre-commit hook (`ktlintFormat`, `check`, `koverVerify`), installed automatically by the first build.
- Dependabot for the version catalog and GitHub Actions.
- `.gitattributes` normalizing line endings to LF on every OS.
- Kotlin warnings are treated as errors.
- Apache License 2.0 (`LICENSE`, `NOTICE`).
- Community files: `README.md`, `CONTRIBUTING.md`, `SECURITY.md`, pull request template and issue templates.
- Privacy rules in `GUIDELINES.md`: zero telemetry, no note content in logs, credentials only in the OS keychain.
- Mandatory manual smoke test on at least one OS for every `feature/` and `fix/` pull request.
- Technical spike report `docs/spikes/2026-10-23-ghost-llama.md`: app launch, Ghost JSON/YAML and java-llama.cpp
  embeddings and chat verified on Linux, with the gotchas that shape the roadmap.
- Documentation: `LIBRARIES.md`, `GUIDELINES.md`, `ARCHITECTURE.md` (naming, `Outcome` with sealed errors,
  `BaseViewModel`, injected dispatchers and clock, string resources, testing strategy),
  `PLATFORM_GUIDELINES.md`, `ROADMAP.md` (MVP with
  Obsidian-level essentials: backlinks, attachments, tasks, callouts, outline, daily notes, tabs and splits,
  templates, bookmarks, properties, embeds and graph view), `CLAUDE.md` and this changelog.
- Agent skills in `.claude/skills`: Compose Multiplatform skills (Meet-Miyani/compose-skill) and macOS design
  guidelines (ehmo/platform-design-skills), with their licenses and `THIRD_PARTY_NOTICES.md`.
