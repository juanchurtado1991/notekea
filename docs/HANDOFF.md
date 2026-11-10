# Handoff — where Notekea stands

The state of the work for the next agent, whichever it is (`AGENTS.md` has the rules). Update this file whenever a
milestone, a release candidate or the open work changes, so it never needs a conversation's memory to be understood.

Last updated: 2026-11-09, with the code layout rules (`GUIDELINES.md` §6 *Layout*, the `:ktlint-rules` module) in
review: `./gradlew ktlintFormat` applies them and `ktlintCheck` enforces them.

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
  3.4. The **answer quality spike** (`docs/spikes/2026-11-08-answer-quality.md`) (#130) is done: answering in one call
  beats the extraction pipeline on every model; Small is right 53 % and wrong 13 %, Medium right 71 % and wrong 3 %;
  the reranker gate moves from −5 to −8; parallel calls and judges did not improve Small, and voting over 3 drafts
  helped only Medium (3.6's starting point). A **model tournament** on 504 questions
  (`docs/spikes/2026-11-08-model-tournament.md`) kept Qwen3.5 2B, 4B and 9B against five other open models. On 8 GB,
  Small answers with its sources first and a line asking to check them (decided 2026-11-08, in 3.3). **3.1 Model
  manager** (#132) is done: Settings → AI model downloads the recommended size and the reranker from pinned Hugging
  Face commits, checked by SHA-256, into the app data's `models` folder (`app.notekea.ai`). Next: 3.2 Chat engine,
  which reads the model in use from `ActiveModelStore` and must fit chatting with the Small model under 2.5 GB (the
  spike's estimate is 2.7–3.0 GB).

## Open work, in order

1. **No `m1-rc2`:** the developer decided to go straight to M2. The macOS run's fixes (#104: 1.3-09, 1.4-13, 1.8-05,
   the case text of 1.4-08, the cut-off buttons of #101, the report's last steps and the cases not run) are on
   `main` and assumed to work; the release run checks them again with every M1 case.
2. **Issue #103** (1.11-16, dragging a tab is unreliable with several tabs) is the only open issue, deferred to M2:
   reproduce it on macOS at 200% before fixing, as the issue says.
3. **`qa/README.md` piece 5** is 🟡: `MVP_SMOKE_TEST.md` is still edited by hand; making it generated from the
   Auto-Test's cases (`./gradlew :auto-test:smokeCases`, `qa/AUTO_TEST.md`) is still to do. Until then, every case
   change goes both in `:auto-test` and in `MVP_SMOKE_TEST.md`.
4. **Audit fixes:** a project-wide audit (2026-11-04) was fixed in #114 (merged). Tree drag and multi-selection were
   left as a product decision, and the editor toolbar's submenus (Callout, Code block, More) are still mouse-only:
   `ROADMAP.md` decided the toolbar takes no focus.
5. **After 2.9:** check on the nightly that the fork loads on Windows and macOS, then measure memory there.
6. **Intermittent Auto-Test failures on CI:** Windows failed once each in 1.1-07 and 1.4-05, and Ubuntu in 1.14-09 and 1.13-04; none is
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
  when it decides something; files at most 350 lines of code, functions at most 60, lines at most 100 characters.
- **Test Buddy** (`:test-buddy`, `:test-buddy-crash`) is kept generic, ready to become a library of its own: nothing
  Notekea-specific in it. Notekea's cases live in `:auto-test`. The Auto-Test is in English only.
- **Every feature keeps the Auto-Test current** (its cases, automatic or manual), so the release run only needs
  a tag. There is no quota of automatic cases: automate what is reasonable.
- **Post-MVP repositories** (`ROADMAP.md`, post-MVP): clone the repository as it is, index it with a parser, answer
  with file and line; Notekea never writes notes about the code nor writes code.
- **One editing mode** (`ROADMAP.md`, post-MVP item 4): the plan and the design were agreed on 2026-11-09, and
  both spikes are done (`docs/spikes/2026-11-09-one-editing-mode.md`). Steps 2 to 4 of the plan below are written on
  `feature/one-editing-mode`; the next is step 5. Design: the cursor's line shows
  all its Markdown marks, muted, and every other line looks finished (the two designs not chosen revealed only the
  element under the cursor, or the whole block). The raw Markdown is a "Source" action that takes over today's
  Edit/Preview shortcut.
  **Architecture, decided by the developer: visible blocks.** One text field for the whole note lays out the whole
  note on every change and does not hold for a 2,000-line note. So the note is cut into blocks at its blank lines
  and shown in a `LazyColumn`; Preview's renderer draws every block but the cursor's, which is a `BasicTextField`
  over the note's one `TextFieldState` whose output transformation removes the text outside the block (the window).
  One text, one selection, one undo; the window is the blocks the selection touches; the field's item keeps one key.
  Measured: 6 ms per key on the long note instead of 1,380, 51 MB of heap. Rules the spike gave:
  - A block's renderer must have its height on its first frame (`rememberMarkdownState(immediate = true)`): the
    renderer's default parses later and is empty meanwhile, so the list composes every block of the note.
  - Callouts, code blocks, images, embeds and query blocks are drawn by the renderer and show their Markdown while
    the cursor is in them: no editor of their own. Tables get an editor by cells (step 5, asked for by the developer
    on 2026-11-09); until that step they show their Markdown like the rest.
  - "Source" is the window over the whole note, which is today's editor.
  One PR from `main`, one commit per step:
  1. The spikes (done).
  2. The blocks (done): the pure cut of a note into blocks and the window (`NoteBlocks`, `BlockItems`), with
     tests; the lazy list in place of the scrolled text field in Edit mode and in Preview (`EditorList`), with the
     header, the properties and the toolbar (sticky) as its first items; a click on a block puts the cursor there
     (`EditorPageState.click`); arrows, Backspace and Delete (`EditorKeys`), drag (`EditorDrag`) and Shift+click
     cross blocks; the outline, each tab's scroll position and Find's scroll read the list (`EditorListEffects`);
     a click on a task's box in a drawn block checks it. The Edit/Preview control stays.
  3. Find and embeds outside the text field (done): Find marks its matches in the drawn blocks (`TextMarks`,
     painted behind the renderer's texts; `rememberFindMarks`), and the embedded files and query blocks of the
     cursor's block are drawn under the field (`WindowEmbeds`).
  4. Marks hidden except on the cursor's line (done): the window's output transformation also hides the marks
     of the lines the cursor is not on (`ShownText`, built by `ShownTextBuilder` from the highlighter's spans; it
     maps offsets both ways for the keys, the drag and the decorations), a list's `-` shows as a bullet, and no
     line shows its marks while the field has no focus. Struck and highlighted text (`~~`, `==`) got their kinds.
     "Source" (`EditorState.isSourceMode`, the toolbar's last button but Find) is the window over the whole note
     with every mark shown: one text field again, so a long note is as slow there as the editor was before.
  5. The table editor by cells: a table's block stays a table while the cursor is in it, each cell a text field
     over the cell's range of the note's text; Tab and Enter move between cells, and rows and columns are added
     and removed from the table. Design chosen by the developer on 2026-11-10: a small bar above the table while
     the cursor is in it (insert a row above or below, a column left or right, align, delete the row or the
     column), with the same actions on the right click.
  6. Long notes and memory against Preview with the `:auto-test` memory runs; then the Edit/Preview control,
     `EditorState.isReading` and its preference go (`ReadingView` stays for embedded notes), with the Auto-Test
     cases, the QA documents, the strings in every locale and the changelog.
  Also decided on 2026-11-10: "Source" is a button at the end of the formatting toolbar, marked while it is on
  (Notekea has no palette of commands, only the note switcher, so the button and the shortcut are its two ways
  in); the editor's colours are the "accents" option (only links, tags and code take
  colour, headings keep the text's colour); Home and Templates get their icons in the tree, in a commit of their
  own in this PR. `isBasicTextFieldStyledTextEnabled` stays on: the window keeps the styled text small.
  What steps 2 and 3 leave, to settle in the later steps or to keep as known limits:
  - The window's text field holds the note's whole text in its semantics (`EditorWindow`), so a screen reader and
    the Auto-Test's verbs read and select the note as one text.
  - The lazy list already keeps what is brought into view below its sticky toolbar: `EditorBringIntoViewSpec`
    must not add the toolbar's height again (it did, and the outline marked the heading before the chosen one;
    `EditorOutlineRevealTest`).
  - A click on a drawn block lands at the same share of the block's height in its text, since a drawn block is
    taller than its lines in the field.
  - Test Buddy's `scrollTo` and `clickWords` look for the node that draws a text, and scroll lazy lists to it: the
    window's field says the whole note but draws only its blocks.
  - Auto-Test case 1.15-05 was rewritten: in Edit an embed shows its frame, and its line as written only while the
    cursor is in its block.
  - A list cannot be scrolled while it measures: what follows state the list writes waits a frame first
    (`showCursor` in `EditorListEffects`).
  - A drawn block keeps the height of the blank lines that end it, so the page does not move when the window does;
    Preview is a little looser than it was.
  - While the cursor is in a block, its embedded files and query tables show under the whole block, not under
    their own lines, and their lines show as written.
  - Find's marks in a drawn block are searched in the drawn text, which has no Markdown marks, and are painted when
    the block is drawn: tables, code blocks and callout titles take no mark (the renderer draws them without the
    hook), and a match the marks of the note's text split shows marked though Find does not count it. The match
    Find is on is always exact: its block is the text field.
  - A reference-style link whose definition is in another block does not resolve in a drawn block.
  - A note with no blank line and no heading is one block: it is laid out whole, as before.
  - `Mod+A` makes the window the whole note. Page Up and Page Down stay inside the window, an arrow that crosses to
    another block loses its column, a selection dragged past the page's edge does not scroll it, and a click on a
    drawn block puts the cursor near the click, not always on the exact character (the drawn text has no marks).
  - Preview selects text one block at a time.
