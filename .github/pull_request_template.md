## What

<!-- What does this PR change, and why? Link the issue if there is one. -->

## How it was tested

<!-- Unit tests added and what they cover. -->

## Manual smoke test

<!-- Required for feature/ and fix/ branches (GUIDELINES.md §8). Platform-specific changes: every affected OS. -->

- **OS and version:** <!-- e.g. Ubuntu 24.04, macOS 15.4, Windows 11 24H2 -->
- [ ] The app starts and the window renders without errors in the log
- [ ] The new or fixed behavior works end to end, including empty, loading and error states
- [ ] Nearby features still work (sample vault: open and edit a note, plus the features this change touches)
- [ ] Shortcuts, menus and dialogs follow `PLATFORM_GUIDELINES.md` on this OS

**What I checked:**

<!-- Short list of the steps you followed, ideally the feature's "Done when" criteria from ROADMAP.md. -->

## Checklist

- [ ] One feature per PR, on a `feature/`, `fix/` or `chore/` branch
- [ ] Unit tests cover the new behavior (useful tests, not getters)
- [ ] Tests live in `commonTest` whenever possible
- [ ] `./gradlew ktlintFormat` and `./gradlew check koverVerify` pass locally
- [ ] No new warnings and no unexplained `@Suppress`
- [ ] `CHANGELOG.md` updated under `[Unreleased]`
- [ ] UI changes follow `PLATFORM_GUIDELINES.md` (shortcuts, menus and dialogs per OS)
- [ ] New libraries, if any, are added to `LIBRARIES.md` with the reason
