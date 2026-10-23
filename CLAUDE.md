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
structure** (port in domain, adapter in data, logic in a use case tested with a fake, Koin modules).

Everything written into this repository (code, comments, docs) is in English.

## Skills

Third-party skills live in `.claude/skills` (see `.claude/skills/THIRD_PARTY_NOTICES.md`).
When a skill disagrees with the project documents above, **the project documents win**.

## Verification

```bash
./gradlew ktlintFormat
./gradlew check koverVerify
```

A `feature/` or `fix/` branch is not done until the developer has run the manual smoke test (`GUIDELINES.md` §8).
Agents cannot replace it: when the automated checks pass, hand over a short smoke test script based on the
feature's "Done when" criteria in `ROADMAP.md`, and wait for the developer's result before opening the pull request.
