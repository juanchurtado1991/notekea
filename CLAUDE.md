# Notekea — Agent instructions

Before changing anything, read and follow:

- `GUIDELINES.md` — code rules (SOLID, Clean Architecture, Koin, MVI, branches, commits, tests, changelog).
- `ARCHITECTURE.md` — the concrete patterns: package layout, naming, `Outcome` + sealed errors, `BaseViewModel`,
  injected dispatchers and clock, string resources, testing strategy (fakes, never a real model in unit tests).
- `PLATFORM_GUIDELINES.md` — UI behavior on Windows, macOS and Linux.
- `LIBRARIES.md` — the approved libraries and why. Do not add a library that is not listed there without asking.
- `ROADMAP.md` — milestones, features (with branch names, status and the UI specification of each screen).
  When a feature's pull request is ready, mark it ✅ with the PR link and update the milestone count.

**Before writing a feature, read the reference implementations in `ARCHITECTURE.md` §10 and mirror their
structure**. Build every screen from the design system components (`ARCHITECTURE.md` §11); add missing ones there.
The rest of the pattern: port in domain, adapter in data, logic in a use case tested with a fake, Koin modules.

Everything written into this repository (code, comments, docs) is in English.

## Skills

Third-party skills live in `.claude/skills` (see `.claude/skills/THIRD_PARTY_NOTICES.md`).
When a skill disagrees with the project documents above, **the project documents win**.

## Verification

```bash
./gradlew ktlintFormat
./gradlew check koverVerify
```

## Smoke tests (`GUIDELINES.md` §8)

A `feature/` or `fix/` branch is not done until the developer has run the manual smoke test. Agents cannot replace
it: when the automated checks pass, hand over a short smoke test script based on the feature's "Done when" criteria
in `ROADMAP.md`, and wait for the developer's result before opening the pull request. In the same pull request:

1. **Data:** add what the smoke test needs to `smoke-data/` (inside the repository when Git can hold it, otherwise
   as a step of `smoke.sh` and `smoke.ps1`) and list it in `smoke-data/README.md`. The developer never invents data.
2. **New cases:** add the feature's release cases to `MVP_SMOKE_TEST.md`, written so they can be reproduced on
   Windows, macOS and Linux (name both `smoke.sh` and `smoke.ps1`, and the shortcut on each platform).
3. **Outdated cases:** rewrite in place, keeping their IDs, the existing cases the change makes outdated, including
   those marked "Revised by <feature>"; mark the cases a later feature will change with a "Revised by" note.
4. **Results:** the developer's results go in the pull request description (OS, version, what was checked); the OS
   columns of `MVP_SMOKE_TEST.md` are only filled in the release run with a packaged build.

## Skills from another working directory

When a session works on Notekea from another folder, `.claude/skills` is not loaded automatically: read the
relevant `SKILL.md` files and references from disk before the work they cover, including `compose-ui` before any UI.
