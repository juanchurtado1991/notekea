# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

- Contribution workflow: a feature is one branch and one pull request made of granular commits (each keeping the
  build green), merged with rebase merging, instead of a single commit per feature.
- Dependabot pull requests that only touch CI or build tooling are exempt from changelog entries and smoke tests;
  runtime library upgrades still require a smoke test.
- Dependabot runs monthly with one grouped minor/patch PR per ecosystem; major versions and Kotlin, KSP,
  Compose and Ghost are upgraded by hand (`GUIDELINES.md` §14). Security updates stay immediate.
- Dependencies: Kotlin 2.4.21, ktlint 1.8.0 (Gradle plugin 14.2.0), Gradle 9.8.2, JUnit 6.1.3, slf4j 2.0.20;
  CI actions `checkout` v7, `setup-java` v6, `setup-gradle` v6 and `upload-artifact` v7.

### Added

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
