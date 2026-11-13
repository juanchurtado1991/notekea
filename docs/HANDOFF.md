# Handoff — where Notekea stands

The state of the work for the next agent, whichever it is (`AGENTS.md` has the rules). Update this file whenever a
milestone, a release candidate or the open work changes, so it never needs a conversation's memory to be understood.

Last updated: 2026-11-13, with videos and all of M5 on `feature/videos`, one pull request
([notekea#142](https://github.com/juanchurtado1991/notekea/pull/142), draft), the developer's macOS smoke test under
way, its fixes and the code review's refactors in it.

## Milestones

M0 to M4b are done; M5 and videos are on `feature/videos` (#142). How each milestone went, its pull requests and
what each left open are in `docs/history.md`; the plan and every feature's spec are in `ROADMAP.md`.

## Videos and M5, on `feature/videos` (one pull request, #142)

The developer decided on 2026-11-11 to skip M4's separate performance pass and build every M5 item in one pull
request, with one QA run at the end, in Notekea QA, for the whole MVP.

- **4.9 Videos:** done. It plays local videos and YouTube (a Notekea window of its own), and compresses videos over
  100 MB with FFmpeg, downloaded once and verified.
- **5.1 Onboarding:** an Apple-style sheet in three steps, and the sample vault shipped with the app.
- **5.2 Settings:** like macOS's System Settings.
  - **Spanish:** the whole interface, switched live; the chat follows each question's language. Notekea QA's manager,
    manual cases and guides are in Spanish too.
- **The macOS redesign:** the developer approved every pick of the HIG review. The audit, the order and what was left
  out are in `docs/macos-redesign.md`, and the rules in `PLATFORM_GUIDELINES.md`. The window buttons stay the
  system's own everywhere.
- **5.3 Packaging:** `.github/workflows/release.yml` on tag `v*`, and the README's First launch.
- **5.4 Performance:** measured on Linux with everything built (`docs/performance.md`, M5). Windows and macOS come
  from the release run.
- **After it:** a code review and a bug hunt of the whole project. Then a test audit, which the developer reviews
  before the QA build is made.

### Next: the developer's smoke test on macOS (2026-11-12)

Everything of #142 is built and pushed. `check koverVerify` passed in a clean worktree on Linux. Nothing of it has run
on macOS or Windows yet. The developer continues on their Mac:

1. **Check out `feature/videos`** in a clean checkout. The bundled Sample Vault is copied from `smoke-data`, so local
   edits there would ship in the build.
2. **Run Notekea QA locally** with `./gradlew :auto-test:run`. Add `-Pcases=5.2,4.9` for one feature's cases and
   `-PchatModels=<folder>` for the cases that ask a chat model.
   - The manager, its manual cases and the guides are in Spanish, and Notekea QA starts in Spanish.
3. **macOS-only work never seen running** (written on Linux; check each first):
   - the unified title bar (`MacTitleBar`): the traffic lights over the sidebar, the band and the tabs row moving the
     window, a double click zooming, full screen;
   - the menu bar (`MacMenuBar`, screen menu bar): each item does its shortcut once, not twice (case 1.1-10);
   - the system's accent (`CommandSystemAccent`, `defaults read -g AppleAccentColor`; case 2.12-06);
   - the stacked alert (case 5.2-05);
   - the YouTube player window and the video player on macOS (cases 4.9-02, 4.9-04).
4. **What the developer finds** goes into #142 as fixes, each with a regression test (`AGENTS.md`). Every fix keeps
   the Spanish strings (`values-es`) and both `MVP_SMOKE_TEST.md` and the Auto-Test case in step.
5. **Then the QA build**, only when the developer asks for it: a tag `m5-rc1` publishes Notekea and Notekea QA
   (`.github/workflows/milestone-build.yml`). `./gradlew :auto-test:packageQa` builds this system's installer locally.
   The release run (5.5) covers every case on the three systems and closes M2 to M5.

**From the macOS smoke (2026-11-12):** the title bar moves the window again; seven themes, Workspace Blue the default
(`ROADMAP.md` 2.12); the rail in groups (Search, Files, Side panel; Today, Tasks, Graph, Query, Study; Open another
vault and Settings), with no About button, About being in Settings; Settings' icons drawn as the rail's. Notekea QA's
automatic run starts with App Nap off (`-NSAppSleepDisabled YES`, Test Buddy's `AppProcess`): behind another window
macOS slowed it tenfold and cases waiting for the index failed at random. The YouTube window is made on Cocoa's main
thread (`CocoaMainThread`, through JNA); a local video's native library goes with the app (`VideoPlayerNatives`); a
pasted documentation page asks its question in the chat again.

**Later on 2026-11-13, also in #142:**

- **The code review's suggestions:** each one is its own commit. These include the ViewModels' hosts
  (`ViewModelHost`), the folders of chat, design system, notes domain, shell and graph, and one JUnit version.
- **Study cards are multiple choice:** the developer's design A. The card sits in the middle with its deck, n of m and a
  progress bar, then four options A to D (keys 1 to 4). The chat model writes the wrong options (`MakeChoicesUseCase`)
  and makes the next card's ahead. A right choice is Good, a wrong one Hard, then Next (Enter). Without a model, or
  while the options are written, Show answer works as before (4.6-03).
- **The chat always knows where you are,** shown as chips over its field (the developer's choice 1). The chips name
  the open note with the section at the cursor and the selected text, or a file, a folder, a view or Settings. The
  plan, agent mode and the talk read it as one line (`WorkContext`), and a chip's × keeps its part out (3.3-12).
- **The chat-model cases ran here for the first time** (models downloaded on 2026-11-13):
  - Four Auto-Test parts loading the model at once ran the GPU out of memory, so the cases that ask a model now run
    one after another in part 1 (`NotekeaSuiteParts.asksModel`).
  - A reply or an agent's summary that only says nothing was found is now the not-found reply (3.3-05).
- **Files in notes open in Notekea** (the developer's smoke, 2026-11-13):
  - A PDF's card shows its first page and opens the PDF's tab (1.8-03). Any file Notekea does not show still opens
    with its app (`FilesShownHere`).
  - A video's card shows a frame that is not black (`FilePosters`, with FFmpeg, downloaded the first time). A click
    opens the video's tab, which plays it with volume and full screen (`VideoTab`, `NotekeaVideoControls`).
  - On macOS a YouTube video plays in a tab inside Notekea's window (`MacWebView`: WKWebView through JNA, in this
    process). Windows and Linux still open the window of their own (`YouTubePlayerWindow`); doing the same there is
    open work, and that window was never seen playing since YouTube began asking players for their origin.
- **Web sources, deeper and kept up to date** (the developer's requests, 2026-11-13):
  - The chat and the clip dialog ask how deep to follow a section's links (2 or 3 levels, up to 150 pages) when the
    site has no index of its pages, and the chat says how far the clip is (4.4-06).
  - Update from the web, in the menu of the Web sources folder, of a section's folder and of a clipped note: each page
    is read again, a changed one replaces the note's original text and keeps what was written above it, and a
    section's new pages become notes (`UpdateWebSourcesUseCase`, 4.4-05). Settings → General can do it on its own
    every day, week or month (`AutomaticWebUpdate`, 5.2-06). The status bar shows it, and a ring turns while Git syncs.
- **Smaller changes of the same day:** the rail's Open another vault asks first (`SwitchVaultDialog`), and Settings →
  Files chooses its folders and date format from lists instead of typing them (5.2-02).
- **Not looked into yet:** a ggml Metal abort when the app quits after using the model, and the onboarding card that
  shows before the app finishes loading. The developer asked to wait until the smoke test ends.

**Decisions of this pull request** (also in `ROADMAP.md` 5.1–5.4, `docs/macos-redesign.md`, `PLATFORM_GUIDELINES.md`,
`qa/AUTO_TEST.md` *Language*):

- **Spanish:** the whole interface switches live. The chat's `_es` reply strings stay in English inside `values-es`,
  because replies follow the question's language. Tests run with `user.language=en`.
- **The macOS redesign** was approved whole. The window buttons stay the system's own everywhere. 8C (popovers), the
  high-contrast mode and the page's extra top margin were left out, with reasons in `docs/macos-redesign.md`.
- **Test audit:** the developer asked to automate every manual case a text, an order or a count can check. There are
  191 automatic and 91 manual cases now.


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

- **Default theme:** Workspace Blue (`ColorTheme.DEFAULT`, decided 2026-11-03, kept on 2026-11-12). The other six
  themes are 60/30/10 palettes in `ThemeColorSets` (2026-11-12, `ROADMAP.md` 2.12); `ColorTheme.fromId` keeps the ids of the themes that are gone. Keep the theme's generic name: no third-party brand
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
  both spikes are done (`docs/spikes/2026-11-09-one-editing-mode.md`). Steps 2 to 6 of the plan below are written on
  `feature/one-editing-mode`; what is left is under the steps. Design: the cursor's line shows
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
  5. The table editor by cells (done; design chosen by the developer on 2026-11-10): a block that is one table
     (`TableText`: every line a row between pipes with the header's number of cells) stays drawn while the cursor
     is in it, and the note's text field is laid over the cursor's cell, showing that cell's range of the text
     only (`EditorPageState.cell` and `fieldRange`, `EditorWindowItem`, `CellPlace`). Tab, Enter and the arrows
     move between cells and out of the table (`CellKeys`), a pipe or a line break typed in a cell cannot break the
     row (`CellInput`), and a bar above the table (`TableBar`), also on the right click, inserts a row above or
     below and a column left or right, aligns the column and deletes the row or the column (`TableEditing`,
     `EditorIntent.OnTableEdit`). Drawn tables line their columns up as the delimiter row says
     (`NotekeaMarkdownTable`). Known limits:
     - A table that is not strict (rows of different lengths, no outer pipes, indented, a wiki-link alias with a
       bare pipe, an embed in a cell) shows as written while the cursor is in it, as before.
     - An action on the structure writes the whole table again with one space around each cell.
     - A line format (heading, list) or a paste of several lines with the cursor in a cell can break the table;
       undo brings it back. Find's marks are not drawn in tables.
     - The cell's field shows the cell as written, left-aligned; a click lands on the exact character only in a
       left-aligned cell. A selection that leaves the cell (Shift+arrows, a drag) shows the table as written.
     - A right click on the cell being edited opens the text's menu, not the table's; on a table the cursor is not
       in, it only puts the cursor in the cell.
     - The room for the bar stays above every table in Edit, so nothing moves when the cursor comes in. A note
       that starts with a table gets text above it only in Source.
     - The table is parsed again on every key typed in a cell.
  6. Edit and Preview are gone (done): `EditorState.isReading`, its preference, the tabs and `OnReadModeToggle`
     went; `Mod+E` (`toggleSourceShortcut`) and the toolbar's button turn Source on and off, which a note forgets
     when it closes. `ReadingView` stays: it draws the blocks and the embedded notes. The memory run scrolls the
     long note as it reads and in Source (`docs/performance.md`, "Post-MVP — One editing mode": 37 MB for the long
     note, against 170 in Edit and 170 more in Preview). The Auto-Test cases that went to Preview put the cursor
     at the note's start instead (`cursorToNoteStart`), since the cursor's block shows as written; a text of the
     note is always in the field's semantics, so a case cannot wait for Markdown to "go away": it looks for what
     is drawn. Cases 1.5-08, 1.5-10, 1.5-12 and 1.9-04 were rewritten, 1.5-14 and 1.5-15 (tables) are new.
     Known limit: a link, a tag or a callout's title in the cursor's own block is text, not a click target.
     From the developer's smoke (2026-11-10): with the cursor under its block's text, past a blank line, the block
     is drawn again and the field holds only the lines under it (`EditorPageState.underStart`, `WindowBlock`),
     which is also how the lines under a table are written; an embedded file or a divider put in by the toolbar
     leaves the cursor there (`MarkdownBlocks.edit`, `isCursorUnder`), so the picture shows at once; an embed's
     line never hides its marks (`ShownTextBuilder`); a task's box shows a hand; and the table bar's inserts have
     icons that say the side (`PanelTop` and the like).
  7. The toolbar (done; of three options the developer chose B on 2026-11-10, then C, without the capsule): the
     groups start at the left, on the page (`NotekeaToolbarGroups`), Source and Find stay at the right end, a
     button takes the theme's color under the pointer and is marked while the cursor is in its format (`ActiveFormats`, read from the cursor's line alone, so a line of a code block is read as text;
     `EditorPageState.activeFormats`; `NotekeaIconButton.isMarked`). `ToolbarFit`'s widths no longer count the tabs.
  8. The tree's icons (done): the note `Home.md` at the vault's root has a house and the notes of the templates'
     folder (the vault's Obsidian setting, `Templates` without it) the template icon in their folder's color
     (`NoteLooks.iconOf`, `VaultTreeState.templatesFolder`); mentions and related notes use the same icons.
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
