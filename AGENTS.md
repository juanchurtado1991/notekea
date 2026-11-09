# Notekea — Agent instructions

These are the instructions for any AI coding agent working on Notekea (Claude Code, Codex, Gemini, Cursor, Copilot
or another). `CLAUDE.md` only points here. Where the project stands and what comes next: `docs/HANDOFF.md`.

## The project documents

Before changing anything, read and follow:

- `GUIDELINES.md` — code rules (SOLID, Clean Architecture, Koin, MVI, branches, commits, tests, changelog).
- `ARCHITECTURE.md` — the concrete patterns: package layout, naming, `Outcome` + sealed errors, `BaseViewModel`,
  injected dispatchers and clock, string resources, testing strategy (fakes, never a real model in unit tests).
- `PLATFORM_GUIDELINES.md` — UI behavior on Windows, macOS and Linux.
- `LIBRARIES.md` — the approved libraries and why. Do not add a library that is not listed there without asking.
- `qa/AUTO_TEST.md` — how every smoke case is tested on the testers' machines (Notekea QA and its Auto-Test) and what each
  feature must add to it.
- `ROADMAP.md` — milestones, features (with branch names, status and the UI specification of each screen).
  In the feature's pull request to `main`, mark it ✅ with the PR link and update the milestone count.

Everything written into this repository (code, comments, commits, pull requests, docs) is in English.

## Working with the developer

- **Language:** talk to the developer in Spanish; write the repository in English. They are an Android developer,
  so Android analogies help.
- **They review and merge every pull request.** Never merge one, never push to `main`, never force-push (to redo a
  branch, push a new one). Check CI is green before asking for a review.
- **Commits:** Conventional Commits, small and granular, rebase merging. No `Co-Authored-By` or other AI attribution
  trailers in commits.
- **Ask before** anything outward-facing that is not part of the task (closing issues, deleting branches or
  releases, publishing a tag).
- **UI is shown, never committed:** render screens headless (`ImageComposeScene`, in a temporary test you delete
  afterwards) and show the images in the conversation; never commit screenshots or attach them to pull requests.
  For a new screen or a visual change, first show 2–3 design options to choose from.
- **No dead code:** when changing or reverting, remove the parameters, strings, helpers and imports nothing uses.

## Step-by-step review (`GUIDELINES.md` §8)

Every feature is built and reviewed one step at a time: a feature branch from `main`, then one
`step/<feature>/<nn>-<short-description>` branch per step, created from the feature branch, each pushed with a pull
request **into the feature branch**. Propose the steps with the design, before writing code. After pushing a step,
stop and wait for the developer's review; they merge it (rebase merging), or ask for changes on the same step
branch. Only when every step is merged does the smoke test run, and then one pull request takes the feature branch
to `main`. Single-step fixes and chores (`fix/`, `chore/`, `docs/`) go straight to `main` in one pull request. The
developer sometimes allows one pull request for a whole feature; that exception is only for the feature they name.

Every step brings the tests of `GUIDELINES.md` §10: fakes that behave like the real thing, an integration test on
real files, a screen test for UI, and a regression test for every smoke bug that fails without its fix (check it by
taking the fix out for a moment).

**Before writing a feature, read the reference implementations in `ARCHITECTURE.md` §10 and mirror their
structure**. Build every screen from the design system components (`ARCHITECTURE.md` §11); add missing ones there.
The rest of the pattern: port in domain, adapter in data, logic in a use case tested with a fake, Koin modules.

## Code size rules (`GUIDELINES.md` §6, `ARCHITECTURE.md` §10)

- No file over 350 lines (aim for 300), tests included; split by sub-feature, never into one long helper.
- No function body over 40 lines.
- A use case only when it decides something; a ViewModel that needs one port's operation as it is calls the port.

The custom ktlint rules (`ktlint-rules/`) and the Konsist tests enforce these and more; the pre-commit hook runs
them.

## Verification

```bash
./gradlew ktlintFormat
./gradlew check koverVerify
```

## Skills

`.agents/skills/<name>/SKILL.md` are plain Markdown guides in the open Agent Skills format (third-party, see
`.agents/skills/THIRD_PARTY_NOTICES.md`). `.claude/skills` is a link to the same folder, from where Claude Code loads
them on its own. An agent that loads skills from another folder can link it there too; any other agent reads the
relevant `SKILL.md`, and the reference files it points to, from disk before the work it covers:

| Work | Read first |
|---|---|
| Any Compose task (entry point: picks the files to read) | `compose/SKILL.md` |
| Composables, theming, focus, keyboard, accessibility, resources | `compose-ui/SKILL.md` |
| A screen, dialog or feature slice (ViewModel, UI, DI, tests) | `compose-feature/SKILL.md` |
| MVI, `BaseViewModel`, error tiers, module graph, Koin | `compose-architecture/SKILL.md` |
| Repositories, data sources, persistence | `compose-data/SKILL.md` |
| `commonMain` vs desktop code, expect/actual | `compose-platform/SKILL.md` |
| Gradle, modules, version catalog, packaging, CI | `compose-project/SKILL.md` |
| Menus, shortcuts and windows on macOS | `macos-design-guidelines/SKILL.md` |

When a skill disagrees with the project documents above, **the project documents win** (known differences are in
`THIRD_PARTY_NOTICES.md`: no navigation library, SQLDelight instead of Room).

## Project decisions

Where Notekea differs from the skills on purpose. These are decided: a review does not flag them, and code follows
them.

- **Leaf composables take `onIntent`.** `ARCHITECTURE.md` §4 names `state` + `onIntent` for `<Screen>Content`, and
  the composables under it take the same `onIntent`: a screen has dozens of gestures, and a callback for each would
  pass through every level without adding anything. Stays this way while the leaf belongs to one screen; the design
  system's components take plain callbacks.
- **`IntentTrail` and `Breadcrumbs` are global state.** They are `object`s because a crash report reads them from the
  uncaught-exception handler, when the app may have no working Koin graph or ViewModel left to ask. They hold short
  step names only, never what the person wrote, and are the one exception to "no file-level mutable state".
- **No `SavedStateHandle`.** It exists for process death on Android, which a desktop app does not have. Typed text
  reaches the disk through autosave, and what a run leaves behind (tabs, panels) is restored from the settings.
- **First loads start in `init`.** A ViewModel lives as long as its vault or its screen, with no `ON_START` to come
  back through, so the kit's cold load on start has nothing to add.
- **Streams keep their names:** `AppearanceStore.observe`, `FileWatcher.watch` and the `Observe*UseCase` classes,
  not `get…Stream`. A port with one stream names what it does, and a use case's name is a verb.
- **One Gradle module with package-level features.** `app` holds `core` and the features as packages, and
  Konsist guards their boundaries, including that features never depend on each other and that `core` depends on
  none. Gradle modules would pay in build time and wiring for a separation the app gets without them. The other
  modules exist because they ship or run separately (`auto-test`, `test-buddy`, `test-buddy-crash`, `ktlint-rules`).
- **No `.composekit.conf`.** The kit's guards are not installed: the Konsist tests and `ktlint-rules/` enforce the
  rules that apply here, in `check`, where CI and the pre-commit hook already run them.
- **`buildSrc`, not `build-logic`.** `notekea.jvm` is the one convention plugin, for the five modules. Because
  `buildSrc` puts Kotlin, Compose, Android and ktlint on the build's classpath, modules apply those by plain `id(...)`;
  their versions stay in the catalog, read by `buildSrc/build.gradle.kts`.
- **Ghost DTOs are public.** The serializers Ghost generates are public and do not compile against an `internal`
  class, so the `*Dto` classes it reads cannot be `internal` as the skills ask.

## Smoke tests (`GUIDELINES.md` §8)

A `feature/` or `fix/` branch is not done until the developer has run the manual smoke test. Agents cannot replace
it: when every step is merged into the feature branch, hand over a short smoke test script based on the feature's
"Done when" criteria in `ROADMAP.md`, and wait for the developer's result before opening the pull request to
`main`. Fixes the smoke test needs are new steps. Before handing it over, run the pre-smoke of `GUIDELINES.md` §8:
every case you can run headless becomes a committed test named after the case ID and listed in the **Automated**
column of `MVP_SMOKE_TEST.md`, and every expected value of the script is checked against `smoke-data/`. Run the
scripts only as `SMOKE_SCRATCH=scratch-agent smoke-data/smoke.sh <command>`; never run them on the developer's
`smoke-data/scratch`, and never run `clear-cache`, `first-launch` or `restore-settings`, which change the app's real
data folder. Every case names the vault it runs in, in its own steps. Cases that check something in Obsidian are
left for the release run, not the feature's smoke list. Within the feature's steps:

1. **Data:** add what the smoke test needs to `smoke-data/` (inside the repository when Git can hold it, otherwise
   as a step of `smoke.sh` and `smoke.ps1`) and list it in `smoke-data/README.md`. The developer never invents data.
2. **New cases:** add the feature's release cases to `MVP_SMOKE_TEST.md` (by hand until `qa/README.md` piece 5 makes
   it generated from the Auto-Test's cases), written so they can be reproduced on Windows, macOS and Linux (name
   both `smoke.sh` and `smoke.ps1`, and the shortcut on each platform).
3. **Outdated cases:** rewrite in place, keeping their IDs, the existing cases the change makes outdated, including
   those marked "Revised by <feature>"; mark the cases a later feature will change with a "Revised by" note.
4. **Auto-Test:** give every new or changed case its **Checked by** (`Auto-Test`, or `Manual (<reason>)` for
   eyes, system, packaged or costly; there is no quota, but every milestone keeps as many useful automated cases as it
   can: a result shown as text, an order, a count or a file's content is automated, `Manual (eyes)` is only for
   colors, motion and drawings, and a costly case is split so its cheap part, such as starting and cancelling a
   download, is automated) and write it in `:auto-test` in the
   same step: an `AutomatedCase` named after its ID, or a `ManualCase` with its steps and expected result in plain
   English for a tester who is not a developer (no terminal, no paths to type). Keep the consistency test green
   (`qa/AUTO_TEST.md`).
5. **Results:** the developer's results go in the description of the pull request to `main` (OS, version, what was
   checked); the OS columns of `MVP_SMOKE_TEST.md` are only filled in the release run with a packaged build.

## Testing levels (`GUIDELINES.md` §8)

1. **Feature smoke test** — the section above: the developer runs it with your script before a feature reaches
   `main`. Every automatable case you add also becomes an automated case of the Auto-Test.
2. **Milestone performance pass** — the last item of every milestone (1.18, 2.9, 3.7, 4.3, 5.4): run the automated
   smoke runs while sampling memory, profile where it goes and idle RAM, record it in `docs/performance.md`, cut
   what grew. Its smoke test also proves nothing broke.
3. **Release run** — `qa/README.md`, the only QA run after M1's (decided 2026-11-04, `ROADMAP.md`). When 5.4 is merged
   into `main`, before 5.5: check the "Before the first run" table in `qa/README.md` and build what is still ⬜, then
   follow its steps: tag `m5-rc<k>`, open `qa/runs/M5.md`, hand the testers the release and their guide
   (`qa/GUIDE_*.md`): they install Notekea QA, follow its manager and send its report. Turn every failure in their
   reports into an issue and a `fix/` branch, publish the next release candidate and retest. The developer decides
   when a run closes (M1 closed on macOS alone, `qa/runs/M1.md`). A milestone before it starts the next one as soon as
   its performance pass is merged.

**The Auto-Test is never left for later:** every feature keeps it current, so the release run only needs a tag.

## The development machine

- The developer works on Pop!_OS (Linux x86_64), COSMIC on Wayland, with Android Studio installed natively. Its
  folder picker (the XDG portal) cannot select a folder without read permission.
- Window screenshots do not work there: review UI with `ImageComposeScene` as above.
- Headless screen tests run inside `UiThread.run { }` (the AWT event thread); rendering from the test thread fails
  at random on CI.
- An agent may not be able to stop processes (Gradle daemons, the app): ask the developer to run the command.
- The agent's `/tmp` may differ from the system's, which is why smoke data lives in `smoke-data/` in the repository.
