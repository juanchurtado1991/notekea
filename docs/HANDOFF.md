# Handoff — where Notekea stands

The state of the work for the next agent, whichever it is (`AGENTS.md` has the rules). Update this file whenever a
milestone, a release candidate or the open work changes, so it never needs a conversation's memory to be understood.

Last updated: 2026-11-08, with the answer quality spike in review.

## Milestones

- **M0 — Foundation:** ✅
- **M1 — Vault and editor:** ✅ 18 of 18 items. Its QA run (`qa/runs/M1.md`) closed on 2026-11-01 with the macOS run
  of `m1-rc1`, as the developer decided: the Windows tester is not available until M2, and Linux was not run.
- **M2 — Indexing, links and related notes:** ✅ 13 of 13 items. 2.12 Color themes is done (#107), 2.1 Markdown
  chunking (#108), 2.2 Embedding engine (#109), 2.3 Hybrid index (#110), 2.4 Related notes panel (#111) and **2.5
  Backlinks** (#112) are done; Workspace Blue is the default theme (#113). `notes` was split into features (#118), and
  **2.6 Hybrid search** (#120), the eight hand-tuned themes (#123) and **2.7 Graph view** (#122) are done. **2.10
  Vault queries** (#124) are done. **2.11 Vault tasks** (#125, a grouped list, as Reminders) is done. **2.8 Retrieval
  evaluation set** (#126) is done: recall@5 measures 0.94 and 0.90 against baselines of 0.92 and 0.86. **2.13 Packaged
  check** (#127) is done: the nightly installs and starts the app on the three systems. **2.9 M2 performance pass**
  (#128) is done: the app moved to `net.ladenthin:llama`, because java-llama.cpp 4.2.0 leaked each model it loaded;
  measures in `docs/performance.md`, Linux only.
- **M3 — Chat with citations:** started. **3.0 Parallel inference spike** (#129) is done
  (`docs/spikes/2026-11-07-llm-parallel.md`, Linux only): Qwen3.5 2B, 4B and 9B as Small, Medium and Large,
  bge-reranker-v2-m3, Vulkan or Metal for the GPU, no mmap on the CPU, slots 1 / 2 / 4 by memory. Preliminary until
  3.4. The **answer quality spike** (`docs/spikes/2026-11-08-answer-quality.md`) is in review: answering in one call
  beats the extraction pipeline on every model; Small is right 53 % and wrong 13 %, Medium right 71 % and wrong 3 %;
  the reranker gate moves from −5 to −8; parallel calls and judges did not improve Small, and voting over 3 drafts
  helped only Medium (3.6's starting point). A **model tournament** on 504 questions
  (`docs/spikes/2026-11-08-model-tournament.md`) kept Qwen3.5 2B, 4B and 9B against five other open models. On 8 GB,
  Small answers with its sources first and a line asking to check them (decided 2026-11-08, in 3.3). Next: 3.1 Model
  manager and 3.2 Chat engine, which must fit chatting with the Small model under 2.5 GB (the spike's estimate is
  2.7–3.0 GB).

## Open work, in order

1. **No `m1-rc2`:** the developer decided to go straight to M2. The macOS run's fixes (#104: 1.3-09, 1.4-13, 1.8-05,
   the case text of 1.4-08, the cut-off buttons of #101, the report's last steps and the cases not run) are on
   `main` and assumed to work; the release run checks them again with every M1 case.
2. **Issue #103** (1.11-16, dragging a tab is unreliable with several tabs) is the only open issue, deferred to M2:
   reproduce it on macOS at 200% before fixing, as the issue says.
3. **`qa/README.md` piece 5** is 🟡: `MVP_SMOKE_TEST.md` is still edited by hand; making it generated from the
   Self-Test's cases (`./gradlew :selftest:smokeCases`, `qa/SELF_TEST.md`) is still to do. Until then, every case
   change goes both in `:selftest` and in `MVP_SMOKE_TEST.md`.
4. **Audit fixes:** a project-wide audit (2026-11-04) was fixed in #114 (merged). Tree drag and multi-selection were
   left as a product decision, and the editor toolbar's submenus (Callout, Code block, More) are still mouse-only:
   `ROADMAP.md` decided the toolbar takes no focus.
5. **After 2.9:** check on the nightly that the fork loads on Windows and macOS, then measure memory there.
6. **Intermittent Self-Test failures on CI:** Windows failed once each in 1.1-07 and 1.4-05, and Ubuntu in 1.14-09 and 1.13-04; none is
   explained yet. 1.12-01 (a real bug) and 1.17-04 are fixed.
7. **The 3.0 bench** (a Java server and Python scripts, outside the repository) is in the developer's
   `~/.cache/notekea-spike`, with every model and result; 3.4 can reuse its 30 actions and its unanswerable questions,
   the answer quality spike's expected facts and trap questions (`bench/facts.json`) and manual grades
   (`bench/mine.py`), and the tournament's 504 questions (`bench/qset.json`) with their manual grades (`bench/mineS.py`,
   `bench/mineS2.py`).

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
