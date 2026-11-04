# Handoff — where Notekea stands

The state of the work for the next agent, whichever it is (`AGENTS.md` has the rules). Update this file whenever a
milestone, a release candidate or the open work changes, so it never needs a conversation's memory to be understood.

Last updated: 2026-11-03, with 2.5 in review.

## Milestones

- **M0 — Foundation:** ✅
- **M1 — Vault and editor:** ✅ 18 of 18 items. Its QA run (`qa/runs/M1.md`) closed on 2026-11-01 with the macOS run
  of `m1-rc1`, as the developer decided: the Windows tester is not available until M2, and Linux was not run.
- **M2 — Indexing, links and related notes:** started. 2.12 Color themes is done (#107), 2.1 Markdown chunking
  (#108), 2.2 Embedding engine (#109), 2.3 Hybrid index (#110), 2.4 Related notes panel (#111) and **2.5
  Backlinks** (#112, open, smoke passed on Linux) are done; next is **2.6 Hybrid search** (`feature/hybrid-search`,
  `ROADMAP.md`).

## Open work, in order

1. **No `m1-rc2`:** the developer decided to go straight to M2. The macOS run's fixes (#104: 1.3-09, 1.4-13, 1.8-05,
   the case text of 1.4-08, the cut-off buttons of #101, the report's last steps and the cases not run) are on
   `main` and assumed to work; M2's QA run, from `m2-rc1`, checks them again with every M1 case.
2. **Issue #103** (1.11-16, dragging a tab is unreliable with several tabs) is the only open issue, deferred to M2:
   reproduce it on macOS at 200% before fixing, as the issue says.
3. **`qa/README.md` piece 5** is 🟡: `MVP_SMOKE_TEST.md` is still edited by hand; making it generated from the
   Self-Test's cases (`./gradlew :selftest:smokeCases`, `qa/SELF_TEST.md`) is still to do. Until then, every case
   change goes both in `:selftest` and in `MVP_SMOKE_TEST.md`.
4. **M2** goes on with 2.6 Hybrid search once #112 is merged. Main's CI has failed since #111: Self-Test cases matched note names
   anywhere in the window and found them in Related's rows. `feature/backlinks` fixes it (a vault opens in a case once
   its notes are ready to search; tree and palette results are found by their test tags), so main is green again once
   #112 is merged; its CI checks 1.12-02 and 1.4-02, which did not fail locally.

#112 (2.5) is open, waiting for the developer's review.

## The M2 QA run

When M2's last item (2.9, its performance pass) is merged, its QA run covers Windows and Linux again, with every
General, M1 and M2 case, and macOS for M2's. The testers: the developer (macOS and Linux) and a tester who is not a
developer (Windows), each with Notekea QA and their guide (`qa/GUIDE_*.md`).

## Decisions to keep

- **Default theme:** Workspace Blue (`ColorTheme.DEFAULT`, decided 2026-11-03). The first palette is the theme Cobalt
  Tangerine, still stored as `default`; its id must not change. Keep the theme's generic name: no third-party brand
  names or marks in the app.
- **Architecture:** Clean Architecture + MVI + Koin + ports with fakes stays (decided 2026-10-28). A use case only
  when it decides something; files at most 350 lines, functions at most 40.
- **Test Buddy** (`:test-buddy`, `:test-buddy-crash`) is kept generic, ready to become a library of its own: nothing
  Notekea-specific in it. Notekea's cases live in `:selftest`. The Self-Test is in English only.
- **Every feature keeps the Self-Test current** (its cases, automatic or manual), so a milestone's QA run only needs
  a tag. There is no quota of automatic cases: automate what is reasonable.
- **Post-MVP repositories** (`ROADMAP.md`, post-MVP): clone the repository as it is, index it with a parser, answer
  with file and line; Notekea never writes notes about the code nor writes code.
