# Notekea — Agent instructions

Before changing anything, read and follow:

- `GUIDELINES.md` — code rules (SOLID, Clean Architecture, Koin, MVI, branches, commits, tests, changelog).
- `ARCHITECTURE.md` — the concrete patterns: package layout, naming, `Outcome` + sealed errors, `BaseViewModel`,
  injected dispatchers and clock, string resources, testing strategy (fakes, never a real model in unit tests).
- `PLATFORM_GUIDELINES.md` — UI behavior on Windows, macOS and Linux.
- `LIBRARIES.md` — the approved libraries and why. Do not add a library that is not listed there without asking.
- `qa/SELF_TEST.md` — how every smoke case is tested on the testers' machines (the Notekea Self-Test) and what each
  feature must add to it.
- `ROADMAP.md` — milestones, features (with branch names, status and the UI specification of each screen).
  In the feature's pull request to `main`, mark it ✅ with the PR link and update the milestone count.

## Step-by-step review (`GUIDELINES.md` §8)

Every feature is built and reviewed one step at a time: a feature branch from `main`, then one
`step/<feature>/<nn>-<short-description>` branch per step, created from the feature branch, each pushed with a pull
request **into the feature branch**. Propose the steps with the design, before writing code. After pushing a step,
stop and wait for the developer's review; they merge it (rebase merging), or ask for changes on the same step
branch. Only when every step is merged does the smoke test run, and then one pull request takes the feature branch
to `main`. Never merge a pull request yourself.

While building UI, render its states headless (`ImageComposeScene`, in a temporary test you delete afterwards) and
show them to the developer in the conversation; never commit screenshots or attach them to pull requests. Every
step brings the tests of `GUIDELINES.md` §10: fakes that behave like the real thing, an integration test on real
files, a screen test for UI, and a regression test for every smoke bug.

**Before writing a feature, read the reference implementations in `ARCHITECTURE.md` §10 and mirror their
structure**. Build every screen from the design system components (`ARCHITECTURE.md` §11); add missing ones there.
The rest of the pattern: port in domain, adapter in data, logic in a use case tested with a fake, Koin modules.

Everything written into this repository (code, comments, docs) is in English.

## Code size rules (`GUIDELINES.md` §6, `ARCHITECTURE.md` §10)

- No file over 350 lines (aim for 300), tests included; split by sub-feature, never into one long helper.
- No function body over 40 lines.
- A use case only when it decides something; a ViewModel that needs one port's operation as it is calls the port.

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
it: when every step is merged into the feature branch, hand over a short smoke test script based on the feature's
"Done when" criteria in `ROADMAP.md`, and wait for the developer's result before opening the pull request to
`main`. Fixes the smoke test needs are new steps. Before handing it over, run the pre-smoke of `GUIDELINES.md` §8:
every case you can run headless becomes a committed test named after the case ID and listed in the **Automated**
column of `MVP_SMOKE_TEST.md`, and every expected value of the script is checked against `smoke-data/`. Run the
scripts only as `SMOKE_SCRATCH=scratch-agent smoke-data/smoke.sh <command>`; never run them on the developer's
`smoke-data/scratch`, and never run `clear-cache`, `first-launch` or `restore-settings`, which change the app's real
data folder. Within the feature's steps:

1. **Data:** add what the smoke test needs to `smoke-data/` (inside the repository when Git can hold it, otherwise
   as a step of `smoke.sh` and `smoke.ps1`) and list it in `smoke-data/README.md`. The developer never invents data.
2. **New cases:** add the feature's release cases to `MVP_SMOKE_TEST.md` (by hand until `qa/README.md` piece 5;
   after it, in the Self-Test's cases, regenerating the file with `./gradlew :selftest:smokeCases`), written so they can be reproduced on
   Windows, macOS and Linux (name both `smoke.sh` and `smoke.ps1`, and the shortcut on each platform).
3. **Outdated cases:** rewrite in place, keeping their IDs, the existing cases the change makes outdated, including
   those marked "Revised by <feature>"; mark the cases a later feature will change with a "Revised by" note.
4. **Self-Test:** give every new or changed case its **Checked by** (`Self-Test`, or `Manual (<reason>)` for
   eyes, system, packaged or costly; automate what is reasonable, with no quota) and write it in `:selftest` in the same step: an `AutomatedCase` named after its ID, or
   a `ManualCase` with its steps and expected result in English and Spanish, in plain words for a tester who is not
   a developer (no terminal, no paths to type). Keep the consistency test green (`qa/SELF_TEST.md`).
5. **Results:** the developer's results go in the description of the pull request to `main` (OS, version, what was
   checked); the OS
   columns of `MVP_SMOKE_TEST.md` are only filled in the release run with a packaged build.

## Testing levels (`GUIDELINES.md` §8)

1. **Feature smoke test** — the section above: the developer runs it with your script before a feature reaches
   `main`. Every automatable case you add also becomes an automated case of the Self-Test (point 4 above), the
   automated smoke runs of `ROADMAP.md` 1.16, listed in its **Automated** column.
2. **Milestone performance pass** — the last item of every milestone (1.18, 2.9, 3.7, 4.3, 5.4): run the automated
   smoke runs while sampling memory, profile where it goes and idle RAM, record it in `docs/performance.md`, cut
   what grew. Its smoke test also proves nothing broke.
3. **Milestone QA run** — `qa/README.md`. When the last feature of a milestone (M1–M4) is merged into `main`, do not
   start the next milestone: check the "Before the first run" table in `qa/README.md` and build what is still ⬜
   (one `chore/` or `test/` pull request each, reviewed like any other), then follow its steps: tag `m<N>-rc<k>`,
   open `qa/runs/M<N>.md`, hand the testers the release and their guide (`qa/GUIDE_*.md`): they install Notekea and
   the Notekea Self-Test and send its report. Turn every failure in their reports into
   an issue and a `fix/` branch, publish the next release candidate and retest until Windows, macOS and Linux pass.
   The M1 run is retroactive: it covers everything since M0.
4. **Release run** — the M5 milestone run with the release builds (unsigned, `ROADMAP.md` 5.3).

**The Self-Test is never left for the end of a milestone:** every feature keeps it current, so a milestone's QA
run only needs a tag. Every step that adds or changes UI also brings its screen tests, and UI is reviewed headless
with `ImageComposeScene` while it is built.

## Skills from another working directory

When a session works on Notekea from another folder, `.claude/skills` is not loaded automatically: read the
relevant `SKILL.md` files and references from disk before the work they cover, including `compose-ui` before any UI.
