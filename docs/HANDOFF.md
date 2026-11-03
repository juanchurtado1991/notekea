# Handoff — where Notekea stands

The state of the work for the next agent, whichever it is (`AGENTS.md` has the rules). Update this file whenever a
milestone, a release candidate or the open work changes, so it never needs a conversation's memory to be understood.

Last updated: 2026-11-03, with 2.4 in its smoke.

## Milestones

- **M0 — Foundation:** ✅
- **M1 — Vault and editor:** ✅ 18 of 18 items. Its QA run (`qa/runs/M1.md`) closed on 2026-11-01 with the macOS run
  of `m1-rc1`, as the developer decided: the Windows tester is not available until M2, and Linux was not run.
- **M2 — Indexing, links and related notes:** started. 2.12 Color themes is done (#107), 2.1 Markdown chunking
  (#108), 2.2 Embedding engine (#109) and 2.3 Hybrid index (#110) are done; **2.4 Related notes panel**
  (`feature/related-notes`, `ROADMAP.md`) is built and waits for the developer's smoke (`MVP_SMOKE_TEST.md` 2.4)
  before its pull request.

## Open work, in order

1. **No `m1-rc2`:** the developer decided to go straight to M2. The macOS run's fixes (#104: 1.3-09, 1.4-13, 1.8-05,
   the case text of 1.4-08, the cut-off buttons of #101, the report's last steps and the cases not run) are on
   `main` and assumed to work; M2's QA run, from `m2-rc1`, checks them again with every M1 case.
2. **Issue #103** (1.11-16, dragging a tab is unreliable with several tabs) is the only open issue, deferred to M2:
   reproduce it on macOS at 200% before fixing, as the issue says.
3. **`qa/README.md` piece 5** is 🟡: `MVP_SMOKE_TEST.md` is still edited by hand; making it generated from the
   Self-Test's cases (`./gradlew :selftest:smokeCases`, `qa/SELF_TEST.md`) is still to do. Until then, every case
   change goes both in `:selftest` and in `MVP_SMOKE_TEST.md`.
4. **M2** goes on with 2.4's smoke and pull request, then 2.5 Backlinks.

No pull request is open.

## The M2 QA run

When M2's last item (2.9, its performance pass) is merged, its QA run covers Windows and Linux again, with every
General, M1 and M2 case, and macOS for M2's. The testers: the developer (macOS and Linux) and a tester who is not a
developer (Windows), each with Notekea QA and their guide (`qa/GUIDE_*.md`).

## Decisions to keep

- **Architecture:** Clean Architecture + MVI + Koin + ports with fakes stays (decided 2026-10-28). A use case only
  when it decides something; files at most 350 lines, functions at most 40.
- **Test Buddy** (`:test-buddy`, `:test-buddy-crash`) is kept generic, ready to become a library of its own: nothing
  Notekea-specific in it. Notekea's cases live in `:selftest`. The Self-Test is in English only.
- **Every feature keeps the Self-Test current** (its cases, automatic or manual), so a milestone's QA run only needs
  a tag. There is no quota of automatic cases: automate what is reasonable.
- **Post-MVP repositories** (`ROADMAP.md`, post-MVP): clone the repository as it is, index it with a parser, answer
  with file and line; Notekea never writes notes about the code nor writes code.
