# Milestone QA run

How a milestone is tested on Windows, macOS and Linux before the next one starts (`GUIDELINES.md` §8, testing
level 2). This file is for whoever coordinates the run: the developer and the agent (Claude). Testers only need the
guide of their system: [Windows](GUIDE_WINDOWS.md), [macOS](GUIDE_MACOS.md), [Linux](GUIDE_LINUX.md).

## When

- When the last feature of a milestone (M1, M2, M3, M4) is merged into `main`, before any work on the next milestone.
- The M5 run is the release run (`ROADMAP.md` 5.4), with signed builds.
- **The M1 run is the first one and is retroactive**: it covers everything built since M0 (the "General" cases and
  all M1 cases of `MVP_SMOKE_TEST.md`) and the UI tests written for every screen that existed before the UI test
  infrastructure.

## Who

| System | Tester | Builds the app? |
|---|---|---|
| Windows | A tester who is not a developer | No: installs the `.msi` from the release |
| macOS | The developer | No: installs the `.dmg` from the release |
| Linux | The developer, with the agent | No: installs the `.deb` from the release |

Testers never build anything or open a terminal except to prepare the test data, and their guide gives the exact
command to copy.

## Before the first run (status)

The process below needs these pieces. They are built in `chore/` pull requests after the last feature of M1 and
before the M1 run; the agent checks this list and builds what is missing.

| Piece | What it is | Status |
|---|---|---|
| UI test infrastructure | `org.jetbrains.compose.ui:ui-test` in `LIBRARIES.md`, UI tests in CI on the three OSes, tests for every existing screen (one `test/` pull request per screen) | ⬜ |
| Backfill of the 1.x cases | The cases of 1.1 to 1.5 in `MVP_SMOKE_TEST.md` that can run headless, automated with `ui-test` (or as integration tests) and named after their IDs, in the per-screen `test/` pull requests above; their **Automated** column is filled in, and the rest marked "manual". From 1.6 on each feature automates its own cases (`GUIDELINES.md` §8) | ⬜ |
| Notekea Self-Test | A packaged app that runs the UI tests on the tester's machine against its real file system and shows ✅ or ❌, with a **Copy report** button | ⬜ |
| File logs and Copy diagnostics | Logs written to a rotating file in the app data folder (never note content, `ARCHITECTURE.md` §9) and **Help → Copy diagnostics** (version, OS, last log lines) | ⬜ |
| Milestone build workflow | `.github/workflows/milestone-build.yml`: on a tag `m<N>-rc<k>`, packages `.msi`, `.dmg` and `.deb` (and the Self-Test of each OS) on the three runners and publishes a GitHub **pre-release** with them, `smoke-data.zip` and links to the guides. Unsigned until `ROADMAP.md` 5.3 | ⬜ |

Change ⬜ to ✅ with the pull request link as each one lands.

## Steps

1. **Check** that every feature of the milestone is ✅ in `ROADMAP.md` and that `MVP_SMOKE_TEST.md` has their cases.
2. **Build the release candidate**: tag `main` as `m<N>-rc<k>` (`m1-rc1`, then `m1-rc2` after fixes) and push the
   tag. The workflow publishes the pre-release `Notekea m<N>-rc<k>` on the repository's Releases page. Installers are
   never committed to Git: each is 50–100 MB and would stay in the history forever.
3. **Open the run record**: copy `runs/TEMPLATE.md` to `runs/M<N>.md` and list every case to run (the "General"
   section plus every milestone up to this one; skip cases whose "Testable from" is a later feature).
4. **Send each tester** the link to the pre-release and to the guide of their system.
5. **Testers run the Self-Test first.** If it fails they stop and send the report it copies; nothing manual is tested
   on a build whose automated tests fail.
6. **Testers run the manual cases** and send `REPORT_TEMPLATE.txt` filled in, with **Copy diagnostics** pasted under
   every failed case.
7. **Triage**: record every result in `runs/M<N>.md`. Each failure becomes a GitHub issue labeled `qa-m<N>` and the
   system (`windows`, `macos`, `linux`), with the steps, the expected and actual result, the diagnostics and the
   screenshot.
8. **Fix** each issue on a `fix/` branch to `main` (feature smoke test, `GUIDELINES.md` §8), then publish the next
   release candidate and run again **the Self-Test and the failed cases** (plus the cases near each fix) on the
   affected systems.
9. **Close the milestone** when the three systems pass: record the final build in `runs/M<N>.md` and mark the
   milestone row in `ROADMAP.md` as closed by its QA run.

## Files

- `GUIDE_WINDOWS.md`, `GUIDE_MACOS.md`, `GUIDE_LINUX.md`: step by step for testers, written for people who are not
  developers.
- `REPORT_TEMPLATE.txt`: what a tester fills in and sends back as plain text.
- `runs/TEMPLATE.md`: the record of one run; `runs/M<N>.md` once it starts.
