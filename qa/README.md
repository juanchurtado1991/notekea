# Milestone QA run

How a milestone is tested on Windows, macOS and Linux before the next one starts (`GUIDELINES.md` §8, testing
level 2). This file is for whoever coordinates the run: the developer and the agent (Claude). Testers only need the
guide of their system: [Windows](GUIDE_WINDOWS.md), [macOS](GUIDE_MACOS.md), [Linux](GUIDE_LINUX.md).

## When

- When the last item of a milestone (M1, M2, M3, M4), its performance pass (`ROADMAP.md`, "Closing a milestone"), is
  merged into `main`, before any work on the next milestone.
- The M5 run is the release run (`ROADMAP.md` 5.5), with the release builds, which are not signed (`ROADMAP.md` 5.3).
- **The M1 run is the first one and is retroactive**: it covers everything built since M0 (the "General" cases and
  all M1 cases of `MVP_SMOKE_TEST.md`), each as a Self-Test case, automatic or manual.

## Who

| System | Tester | Builds the app? |
|---|---|---|
| Windows | A tester who is not a developer | No: installs the `.msi` from the release |
| macOS | The developer | No: installs the `.dmg` from the release |
| Linux | The developer, with the agent | No: installs the `.deb` from the release |

Testers never build anything, open a terminal or prepare data: they install Notekea and **Notekea Self-Test**,
which runs the automatic cases and walks them through the manual ones ([SELF_TEST.md](SELF_TEST.md)). They never
measure memory or time with tools either: the milestone's performance pass does, on the automated smoke runs
(`docs/performance.md`).

## Before the first run (status)

The process below needs these pieces, built in this order before the M1 run, one pull request each (the agent checks
this list and builds what is missing). The design is in [SELF_TEST.md](SELF_TEST.md).

| # | Piece | What it is | Status |
|---|---|---|---|
| 1 | Rules and design | `SELF_TEST.md`, this table, `GUIDELINES.md` §8, `CLAUDE.md`, `MVP_SMOKE_TEST.md`'s **Checked by** column | ✅ [#72](https://github.com/juanchurtado1991/notekea/pull/72) |
| 2 | Triage | **Checked by** filled in for every General and M1 case, splitting mixed ones; the share that is `Self-Test` | ✅ [#74](https://github.com/juanchurtado1991/notekea/pull/74): 122 of 167 cases `Self-Test` |
| 3a | Test Buddy core | `:test-buddy`, the Self-Test SDK: case types and DSL, verbs, runner, JUnit bridge, report. The spike before it showed Compose UI Test driving an app inside a packaged app on Windows, macOS and Linux, also with a window open | ✅ [#75](https://github.com/juanchurtado1991/notekea/pull/75) |
| 3b | Test Buddy app | The Self-Test window (design A, a wizard: Automatic, Manual, Send report), the manual walk-through with Passed/Failed, a note and a screenshot, pausing, back up and restore, `testBuddyApp(suite)` | ✅ [#76](https://github.com/juanchurtado1991/notekea/pull/76) |
| 4 | Notekea's suite | `:selftest`: `CaseApp` (the real Notekea on a case's folders), the smoke runs of `app.notekea.smokerun` moved in, the bundled smoke data, the `smoke.sh` scenarios in Kotlin (checked against the script), back up, the first cases (1.6-01 to 1.6-03), `pending-cases.txt` and the consistency test, `run -Pcases=…`, `packageSelfTest` | ✅ [#77](https://github.com/juanchurtado1991/notekea/pull/77) |
| 5 | M1 cases | Every General and M1 case as an `AutomatedCase` or a `ManualCase`, in four pull requests: 1.1–1.5, 1.6–1.9, 1.10–1.13, 1.14–1.18 and General. The last one makes `MVP_SMOKE_TEST.md` generated from the cases (`SELF_TEST.md`) | 🟡 1.1–1.5 in [#78](https://github.com/juanchurtado1991/notekea/pull/78), 1.6–1.9 in [#80](https://github.com/juanchurtado1991/notekea/pull/80), 1.10–1.13 in [#81](https://github.com/juanchurtado1991/notekea/pull/81) (1.11-10 pending); 1.14–1.18 and General to do |
| 6 | File logs and Copy diagnostics | Logs written to a rotating file in the app data folder (never note content, `ARCHITECTURE.md` §9), **Help → Copy diagnostics** (version, OS, last log lines), read by the Self-Test's report | 🟡 Crash reports with Copy details and Report on GitHub done; rotating logs and Copy diagnostics still to do |
| 7 | Milestone build workflow | `.github/workflows/milestone-build.yml`: on a tag `m<N>-rc<k>`, packages Notekea and Notekea Self-Test (`.msi`, `.dmg`, `.deb`) on the three runners and publishes a GitHub **pre-release** with them and the testers' guides. Unsigned until `ROADMAP.md` 5.3 | ⬜ |
| 8 | Testers' guides | `GUIDE_*.md` rewritten as: install both apps, run the Self-Test, send the report; `M1_SMOKE_TEST.md` and `REPORT_TEMPLATE.txt` deleted, their steps now living in the manual cases | ⬜ |

Change ⬜ to ✅ with the pull request link as each one lands. Already done: the automated smoke runs (1.16,
`app.notekea.smokerun`), which piece 4 moves into `:selftest`.

## Steps

1. **Check** that every feature of the milestone is ✅ in `ROADMAP.md` and that `MVP_SMOKE_TEST.md` has their cases.
2. **Build the release candidate**: tag `main` as `m<N>-rc<k>` (`m1-rc1`, then `m1-rc2` after fixes) and push the
   tag. The workflow publishes the pre-release `Notekea m<N>-rc<k>` on the repository's Releases page. Installers are
   never committed to Git: each is 50–100 MB and would stay in the history forever.
3. **Open the run record**: copy `runs/TEMPLATE.md` to `runs/M<N>.md` and list every case to run (the "General"
   section plus every milestone up to this one; skip cases whose "Testable from" is a later feature).
4. **Send each tester** the link to the pre-release and to the guide of their system.
5. **Testers run the Self-Test's automatic part.** If a case fails they send the report and wait: the manual part is
   not run on a build whose automatic cases fail, unless the developer says the failure does not block it.
6. **Testers run the manual part** in the Self-Test, then **Copy report** and **Finish and clean up**, and send the
   report with the screenshots of failed cases.
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
- `SELF_TEST.md`: the Self-Test's design and how every feature keeps it up to date.
- `M1_SMOKE_TEST.md`: every M1 case written out click by click for the three systems; the source of the M1 manual
  cases' texts, deleted by piece 8.
- `REPORT_TEMPLATE.txt`: what a tester filled in before the Self-Test's report; deleted by piece 8.
- `runs/TEMPLATE.md`: the record of one run; `runs/M<N>.md` once it starts.
