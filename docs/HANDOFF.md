# Handoff — where Notekea stands

The state of the work for the next agent, whichever it is (`AGENTS.md` has the rules). Update this file whenever a
milestone, a release candidate or the open work changes, so it never needs a conversation's memory to be understood.

Last updated: 2026-11-05, with 2.7 (#122) and the eight new themes (#123) in review.

## Milestones

- **M0 — Foundation:** ✅
- **M1 — Vault and editor:** ✅ 18 of 18 items. Its QA run (`qa/runs/M1.md`) closed on 2026-11-01 with the macOS run
  of `m1-rc1`, as the developer decided: the Windows tester is not available until M2, and Linux was not run.
- **M2 — Indexing, links and related notes:** started. 2.12 Color themes is done (#107), 2.1 Markdown chunking
  (#108), 2.2 Embedding engine (#109), 2.3 Hybrid index (#110), 2.4 Related notes panel (#111) and **2.5
  Backlinks** (#112) are done; Workspace Blue is the default theme (#113). `notes` was split into features (#118), and
  **2.6 Hybrid search** (#120) is done. **2.7 Graph view** (#122) and the eight hand-tuned themes (#123) are in review. Next
  is **2.10 Vault queries** (`feature/vault-queries`, `ROADMAP.md`).

## Open work, in order

1. **No `m1-rc2`:** the developer decided to go straight to M2. The macOS run's fixes (#104: 1.3-09, 1.4-13, 1.8-05,
   the case text of 1.4-08, the cut-off buttons of #101, the report's last steps and the cases not run) are on
   `main` and assumed to work; the release run checks them again with every M1 case.
2. **Issue #103** (1.11-16, dragging a tab is unreliable with several tabs) is the only open issue, deferred to M2:
   reproduce it on macOS at 200% before fixing, as the issue says.
3. **`qa/README.md` piece 5** is 🟡: `MVP_SMOKE_TEST.md` is still edited by hand; making it generated from the
   Self-Test's cases (`./gradlew :selftest:smokeCases`, `qa/SELF_TEST.md`) is still to do. Until then, every case
   change goes both in `:selftest` and in `MVP_SMOKE_TEST.md`.
4. **Audit fixes:** a project-wide audit (2026-11-04) is fixed on `fix/audit-findings`, one commit per finding, waiting
   for the developer's manual review. Tree drag and multi-selection were left as a product decision, and the editor
   toolbar's submenus (Callout, Code block, More) are still mouse-only: `ROADMAP.md` decided the toolbar takes no focus.
5. **M2** goes on with 2.10, 2.11, 2.8, 2.13 and 2.9, in that order.
6. **Intermittent Self-Test failures on CI:** Windows failed once each in 1.1-07 and 1.4-05, and Ubuntu in 1.14-09; none is
   explained yet. 1.12-01 (a real bug) and 1.17-04 are fixed.

`fix/audit-findings` is #114, waiting for the developer's review.

## The release run

Decided on 2026-11-04: there is no M2, M3 or M4 QA run. The only run left comes after 5.4 and before 5.5, on Windows,
macOS and Linux, with every case (`ROADMAP.md`, `qa/README.md`). The developer also installs the app on Windows and
macOS at the end of M3 to see the chat answer there. Study cards, documents, web clips and agent mode joined the MVP as M4b;
Repositories stays after it. The testers: the developer (macOS and Linux) and a tester who is not a
developer (Windows), each with Notekea QA and their guide (`qa/GUIDE_*.md`).

## Decisions to keep

- **Default theme:** Workspace Blue (`ColorTheme.DEFAULT`, decided 2026-11-03). Themes are hand-tuned in
  `ThemeColorSets` (2026-11-05); `ColorTheme.fromId` keeps the ids of the themes that are gone. Keep the theme's generic name: no third-party brand
  names or marks in the app.
- **Architecture:** Clean Architecture + MVI + Koin + ports with fakes stays (decided 2026-10-28). A use case only
  when it decides something; files at most 350 lines, functions at most 40.
- **Test Buddy** (`:test-buddy`, `:test-buddy-crash`) is kept generic, ready to become a library of its own: nothing
  Notekea-specific in it. Notekea's cases live in `:selftest`. The Self-Test is in English only.
- **Every feature keeps the Self-Test current** (its cases, automatic or manual), so the release run only needs
  a tag. There is no quota of automatic cases: automate what is reasonable.
- **Post-MVP repositories** (`ROADMAP.md`, post-MVP): clone the repository as it is, index it with a parser, answer
  with file and line; Notekea never writes notes about the code nor writes code.
