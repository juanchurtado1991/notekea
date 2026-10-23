# Contributing to Notekea

Thanks for helping! Notekea is a local-first notes app with on-device AI, built with Kotlin Multiplatform.

## Before you start

Read these documents; pull requests are reviewed against them:

- [`GUIDELINES.md`](GUIDELINES.md) — code rules: SOLID, Clean Architecture, Koin, MVI, tests, privacy.
- [`PLATFORM_GUIDELINES.md`](PLATFORM_GUIDELINES.md) — UI behavior on Windows, macOS and Linux.
- [`LIBRARIES.md`](LIBRARIES.md) — approved libraries. Open an issue before proposing a new one.

For anything larger than a small fix, open an issue first so we can agree on the approach.

## Setup

Requirements: JDK 21 and Git. Everything else is downloaded by Gradle.

```bash
./gradlew :composeApp:run
```

The first build also installs the repository's git hooks (`git config core.hooksPath .githooks`).

## Workflow

1. Create a branch from `main`: `feature/<name>`, `fix/<name>` or `chore/<name>`.
2. Build the feature in **small, granular commits**, one logical step each, using
   [Conventional Commits](https://www.conventionalcommits.org/) (`feat:`, `fix:`, `test:`, `refactor:`, `docs:`,
   `chore:`). Include unit tests and a `CHANGELOG.md` entry under `[Unreleased]` before opening the pull request.
3. Every commit must keep the build green: the pre-commit hook runs `ktlintFormat`, then `check` and
   `koverVerify`, and blocks the commit if they fail.
4. **Add the feature's cases to `MVP_SMOKE_TEST.md`** (what to check on each OS before the release), then
   **smoke test the app manually** on at least one OS (every affected OS for platform-specific changes):
   run `./gradlew :composeApp:run`, follow the feature's "Done when" criteria in `ROADMAP.md` and check that
   nearby features still work (see `GUIDELINES.md` §8).
5. Open a pull request against `main`, record the smoke test (OS and what you checked) and fill in the checklist.
   CI runs on Windows, macOS and Linux.

## Reporting bugs and ideas

Use the issue templates. For security problems, do **not** open a public issue: follow [`SECURITY.md`](SECURITY.md).

## License

By contributing, you agree that your contributions are licensed under the [Apache License 2.0](LICENSE).
