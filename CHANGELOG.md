# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- **Notekea QA** replaces the separate Self-Test app: testers install one app, Notekea with its tests and the Test
  Buddy **manager**, a small window always on top. **Automatic** says "Running tests" and shows the time, the
  progress and what is left, the counts, the case running now with each step as it happens, and the last ones done;
  the cases run out of sight in a process of their own, so the tester's Notekea is never touched and can be used
  meanwhile. **Manual** walks through each case one step at a time, then asks for the result (a note, a screenshot,
  and the tester's last steps in Notekea added for them); Restart brings Notekea back on the case that needs it.
  **Report** adds the machine and the steps behind each failure. Notekea QA keeps its own data folder, so nothing
  needs backing up. Test Buddy works for any Compose Desktop app (`test-buddy/README.md`).
- **Test Buddy Crash** (`test-buddy-crash/README.md`), in Notekea's release: the crash notice and reports, configured
  by the app (name, texts, folder, GitHub form), now listing the last steps (each screen action's name, never what was
  written) and the machine (system, Java, language, screen and scale).
- The app's log is kept in `logs/notekea.log` in the app data folder (rotated at 1 MB, three files kept, never note
  text), and About has **Copy diagnostics**: the app, system and Java versions and the last 100 log lines, to paste
  in a tester's report (`qa/README.md`, piece 6).
- Milestone builds (`qa/README.md`, piece 7): a tag `m<N>-rc<k>` packages Notekea and Notekea Self-Test for
  Windows, macOS and Linux and publishes them as a GitHub pre-release with the testers' guides. Each candidate
  installs over the last (version `1.<N>.<k>`), and About shows which candidate it is.
- Testers' guides for the Self-Test (`qa/GUIDE_*.md`, piece 8): install Notekea and Notekea Self-Test, run the
  automatic part, follow the manual cases, send the report and finish and clean up, on Windows, macOS and Linux. The
  step-by-step M1 guide and the report template are gone: their steps live in the Self-Test's manual cases.
- Faster CI: the Self-Test runs in four parts at the same time (`Suite.shard` in Test Buddy), a step's pull request
  into its feature branch runs on Linux only, a pull request of documents only skips the build, `smokeRun` no longer
  repeats the smoke runs inside `check`, and a nightly workflow runs everything with the smoke runs on the three
  systems.

- AI hardware plan (`ROADMAP.md` M3): a dedicated GPU is used whenever the machine has one and the CPU is the route
  for modest PCs; the 3.0 spike checks the GPU backends, measures the time to the first token and picks the models
  with the evaluation sets; Lucene stays the desktop index; the repositories' code graph comes from a parser only.
- Actions from chat (`ROADMAP.md` 3.9): one action per request from a fixed list (open a note at the exact place,
  search, tasks, create notes, properties, tasks, links), writing ones always confirmed and undoable, measured with
  their own evaluation set. Repositories (post-MVP 5) are now cloned as they are and indexed by a parser, so the
  chat answers about the code with `file:line` and opens it there; Notekea writes no notes about the code and no
  code, and says so in plain words.

- Self-Test cases of 1.14 to 1.18 and General, so every M1 case is written: 31 automatic ones (1.11-10, the
  properties form, its source and broken YAML, note info and the Chat panel, the status bar, the zoom, embeds and
  their notices, the formatting toolbar, More and list editing, a word saved when switching vaults, pictures read
  again, names with numbers in order) and 14 manual ones, the General cases and, last, 15 minutes of free use (G-09).
  Manual cases get an Open Notekea button that starts the installed app (G-07 with its crash test on) and keeps its
  output in the Report folder; free use answers "Nothing odd" or "I found something". 1.17-05 now expects the
  vault's tags as the smoke data has them (#recipe first, #weekend already a tag).
- Notekea Self-Test plan (`qa/SELF_TEST.md`): testers install Notekea and a Self-Test app that runs the automatic
  smoke cases on their system and walks them through the manual ones, in one report; every
  feature keeps it up to date from now on (`GUIDELINES.md` §8, `qa/README.md`).
- Self-Test cases of 1.10 to 1.13: 31 automatic ones (today's note and the day arrows, Obsidian's daily note
  settings, the tool rail and the sidebar's header, tabs opened, opened behind, closed and switched, two panes and
  dragging tabs between them, a tab's place kept, folders as cards, quick folder clicks, reordering tabs, templates
  with and without a folder, Obsidian's template settings, a template for a new note and over a selection, the
  panels' first width, bookmarks from the star and the menu, opening, folding, reordering and following them) and 5
  manual ones. 1.9-05 became a manual case; dropping a tab in the first place moved to 1.11-16; 1.11-10 stays
  pending: a tab of a note deleted while Notekea was closed came back after the relaunch, with "This note couldn't
  be opened".
- Self-Test cases of 1.6 to 1.9: 23 automatic ones (search by text and tag, the palette's keyboard, a tag link, the
  large vault, tasks as checkboxes and Mod+Enter, folding callouts, embeds in Edit, the attachments folder, notes
  shown in a note, the outline and the side panel, in a narrow window too) and 13 manual ones (callout colors,
  pictures, PDFs, file dialogs, drag and drop, pasting screenshots, Obsidian). 1.9-05 stays pending: Mod+Shift+O
  did not give the outline the keyboard in the automated case. Test Buddy now clicks what is on screen, scrolls to
  it first, and lets a case have a window size of its own.
- Self-Test cases of 1.1 to 1.5 (`qa/README.md` piece 5): the 33 automatic ones drive the real Notekea (shortcuts,
  dialogs, the app data folder, opening, creating and locating vaults, the tree, renaming with links, deleting,
  changes made by other apps, the editor's saving, conflicts, find, Preview and its links) and the 14 manual ones
  walk the tester through what needs eyes or the system. Notekea's verbs gained `relaunch`, the tree's rows and the
  note's text; Test Buddy gained `clickIcon`, `rightClick`, `clickWords`, `clearText`, a hidden text counting as gone,
  and copies that keep files' dates.
- Notekea's Self-Test suite (`:selftest`, `qa/README.md` piece 4): the real Notekea for each case (`CaseApp`), its
  verbs (`openVault`, `openNote`), the smoke data bundled in and its scenarios in Kotlin (checked byte for byte
  against `smoke.sh`), the first cases (1.6-01 to 1.6-03), a consistency test with `MVP_SMOKE_TEST.md` and the
  cases still pending, `./gradlew :selftest:run -Pcases=1.6` for a feature's smoke test and `packageSelfTest`. The
  automated smoke runs and `smokeRun` moved into it (`./gradlew :selftest:smokeRun`), and the Java modules of the
  installers are shared in `gradle.properties`, checked for both apps.
- Test Buddy app (`qa/README.md` piece 3b): `testBuddyApp(suite)` opens the Self-Test window, a wizard in three
  steps: the automatic cases run in the background with their progress; the manual cases one per screen, with fresh
  test data, Passed, Failed (a note and a screenshot) or Skip, saved so the tester can go on later; then the report
  to copy and Finish and clean up, which puts the installed app's folders back as they were and deletes the test
  data. Everything stays in `<Documents>/<app> Self-Test`.
- Test Buddy core (`:test-buddy`, `qa/README.md` piece 3a): the generic Self-Test SDK, ready to become a standalone
  library. A suite declares how to show the app, its test data and its automated and manual cases; a case's body uses
  plain verbs (`click`, `type`, `press(Mod + Key.P)`, `expectText`, `expectFile`…) over Compose UI Test and fails with
  a message a tester can read. The runner gives each case fresh folders and keeps a screenshot of a failure; the
  JUnit bridge runs the same cases in CI; `formatReport` writes the tester's plain-text report.
- **Checked by** in `MVP_SMOKE_TEST.md`: each General and M1 case says whether the Self-Test runs it (122 of 167) or
  guides the tester through it, and why (eyes, system, packaged, costly); four cases that mixed both were split
  (1.4-13, 1.5-12, 1.8-14, 1.11-16).

- Ghost Serializer vault (`smoke-data/vaults/Ghost Serializer`, ROADMAP 1.18): the production code of Ghost
  Serializer as 371 notes, one per Kotlin file explaining what it is about, how it works and its code, linked to the
  files it uses; the large vault of the performance passes and the timing cases, in place of the 5,000 generated
  near-empty notes of `smoke.sh large`, which is gone.
- Memory runs (ROADMAP 1.18): Personal Notes with a 2,000-line note and the Ghost Serializer vault, driven through the
  app (notes opened and closed, a text search, Edit and Preview scrolled, idle, the vault left and opened again), each
  in a JVM of its own; samples record the heap in use and committed, the resident memory and Skia's caches. M1's
  numbers and where the memory goes are in `docs/performance.md`.

- Automated smoke runs (ROADMAP 1.16): the real app driven through its UI on copies of the smoke vaults, a run per M1
  feature and an idle run, in every test run and in CI; `./gradlew :composeApp:smokeRun -PidleSeconds=600` records
  their memory for the performance passes.
- Sample Vault (`smoke-data/vaults/Sample Vault`, ROADMAP 1.16): 40 notes covering every M1 feature, with
  non-default Obsidian settings, for tests, QA, the automated smoke runs and demos.
- Embeds (ROADMAP 1.15): an embedded note shows its own embeds inside it, three notes deep; `![[Note#^id]]` shows
  one block; a note that does not exist offers Create note, a missing heading or block offers Open note, and an
  embed that comes back around or goes too deep says so instead of looping.

### Changed

- Plan (`ROADMAP.md`): vault queries (2.10, what Dataview and Bases do, with no query language), vault tasks (2.11)
  and questions in plain words (3.8, the local model writes the query, the index answers it exactly) join the MVP;
  M2 and M3 grow to 4.5 weeks each. Study cards are the first item after the MVP, and the Repositories item records
  how its background work is estimated, shown and resumed.
- Memory (ROADMAP 1.18): a vault's ViewModels (tree, watcher, caches, editors) are cleared when it closes, after its
  notes are saved; folder cards decode pictures at card size and keep at most 60; the editor keeps only the pictures of
  the note on screen; searches sort only the results they show; names compare without a pattern, in the same order;
  list keys read only the cursor's line.
- Logo: the designed kea and N on a note with a tangerine folded corner replaces the placeholder in the window, About,
  the welcome screen and the rail, in the theme's tonal blues: the note in the primary container color and the mark in
  the primary color, for the light or dark theme.
- Performance (ROADMAP 1.16, from an audit of needless allocations): the editor no longer rescans the note on a caret
  move, skips the embed and link work of notes without embeds or links, and checks a line's characters before its
  regexes; the sidebar keeps its rows when only the selection changes and applies watcher changes without copying
  the whole tree; the tag finder, the metadata cache and rescans do less work; Compose inputs stay stable so their
  composables skip; and a file's preview is released once no tab shows it.
- Code (ROADMAP 1.16): use cases that only forwarded a call are gone, the editor's and the sidebar's ViewModels are
  split into parts they route intents to, and no file is longer than 350 lines (`notekea:max-file-length`).
- Plan (`ROADMAP.md`, `GUIDELINES.md` §6 and §8, `ARCHITECTURE.md` §10, `qa/README.md`, `CLAUDE.md`): every milestone
  ends with a performance pass (1.18, 2.9, 3.7, 4.3, 5.4) that profiles memory, idle RAM and timing on the automated
  smoke runs, which come with every new smoke case from 1.16 on; 1.16 also trims the code (no use case that only
  forwards a call, no file over 350 lines).
- Docs: `ROADMAP.md` records every decision of 1.14 (the base editor's UI finished there, what was decided against,
  the new layout); `GUIDELINES.md` adds choosing designs from rendered options, calm UI, readable width and zoom, no
  dead code and the one-pull-request exception; `ARCHITECTURE.md` adds `core.appearance`, the new design-system
  pieces and the layout rules; `PLATFORM_GUIDELINES.md` adds the sidebar, Edit / Preview and zoom shortcuts.
- Second polish (ROADMAP 1.14): the editor toolbar always fits on one line, what does not fit moving into More; unsaved
  tabs show a dot; the status bar's path and the folder above a note's title show it in Files; the status bar shows
  the zoom while it is not 100 % and resets it on a click; an editor without tabs offers four start actions with their
  shortcuts; sections fold with an animation; file kinds have icon colors; a section header without a shortcut no
  longer shows a tooltip over its first row; Chat's future question field is drawn dashed.
- Wide screens (ROADMAP 1.14): the note's page takes 70 % of the editor pane, between 760 dp and 1100 dp, and
  `Mod+=` / `Mod+-` / `Mod+0` zoom the whole window from 80 % to 160 %, remembered.
- Polish (ROADMAP 1.14): one surface for the panes with quiet dividers, BOOKMARKS and FILES section headers, a quiet
  selection while a list lacks the keyboard, tabs with a minimum width, a fade and a list of every open tab when they
  overflow, roomier reading lines and header, brighter secondary text, the save state in the status bar.
- Tabs (ROADMAP 1.14) look like a browser's: the open one rises into the note with rounded corners, the others stay
  quiet and show their close button on hover.
- The tool rail (ROADMAP 1.14) keeps Files, Search notes, Today and a Side panel button at its top and Open another
  vault, About and Settings at its bottom; New note and New folder leave it, Open another vault leaves the sidebar and
  Outline leaves the editor's toolbar. The app shows its version as 0.1.0-alpha until its first public release.
- The side panel is an inspector (ROADMAP 1.14): its Outline and Note info sections fold on their own, remembered, in
  place of tabs; `Mod+Shift+O` opens a folded outline. An Inspector | Chat control at its top shows Chat as "Coming
  soon" until the chat is built (ROADMAP 3.3).
  place of tabs; `Mod+Shift+O` opens a folded outline.
- Plan for the editor's memory (`ROADMAP.md` 1.18): measure the editor alone on the three systems with a 5,000-note
  vault, check that opening and closing notes gives the memory back, and cut it to under 400 MB before M2.
- Plan for memory (`ROADMAP.md` 3.0, 3.1, 3.2, 5.4): the inference spike measures memory per model and context and
  whether a 1–2B model is good enough; the model manager recommends a model from the machine's RAM; the chat model is
  loaded only while used; a new M5 feature sets memory budgets (editor < 400 MB, chatting on an 8 GB laptop < 2.5 GB)
  and tunes the JVM. Using an installed Ollama is post-MVP.
- The sidebar opens as wide as the outline panel (340 dp), so the note sits between two panels of the same width; a
  width the user set by dragging its edge is kept.
- The editor's toolbar is a rounded capsule of groups with the most used buttons (Heading, Bold, Italic, Link to a
  note, Tag, Checklist, Bulleted list, Insert template); Web link, Numbered list, Callout, Code block, Image or file,
  Quote, Divider and Table move to More (⋯), Callout and Code block opening their types beside it.
- Plan for the release (`ROADMAP.md` 5.3): the MVP's builds are not code-signed; the README explains how to open
  them the first time on Windows and macOS, and signing comes later, when there are users to justify it.
- Code: Markdown marks and shared text pieces are named once (`MarkdownSyntax`, `StringPatterns`) instead of
  being written as literals, and the fence and tag patterns are no longer duplicated (`GUIDELINES.md` §5).
- The open vault shows its notes in a sidebar tree instead of the "N notes found" summary, and opening a vault no
  longer walks it twice.
- Features never depend on each other: the app composes them through slots (`ARCHITECTURE.md` §1, enforced by
  Konsist).

- Plan for the graph view (`ROADMAP.md` 2.7): it opens as a local graph of the current note, colors notes by
  folder without setup, focuses a note's links on hover, has no physics settings and can be used with the keyboard.
- Plan for M3: answers from small local models (~2B, ~4B, ~8B) are made precise by a pipeline that reranks
  retrieval, extracts evidence in parallel and verifies every sentence against the notes, plus an opt-in Deep
  answer mode that writes several drafts in parallel and keeps the best verified one (`ROADMAP.md` 3.0, 3.5, 3.6;
  `ARCHITECTURE.md` §12). The sample vault (1.16) and evaluation sets move to `smoke-data/`.
- Post-MVP plan for repositories (`ROADMAP.md`): a code repository's docs in the vault first, then a module graph
  from a parser, then summaries and chat over the code once an evaluation shows small local models can do it.
- Testing rules learned from the editor's smoke test (`GUIDELINES.md` §8, §10): fakes behave like the disk or the
  network, every step adds an integration test on real files and a screen test for its UI, a new library is tested
  the way Notekea uses it, every smoke bug gets a regression test, and the agent runs a pre-smoke before the
  developer's smoke test, committing the cases it can run headless as tests named after them (the new
  **Automated** column of `MVP_SMOKE_TEST.md`; the 1.x cases are backfilled before the M1 run). `smoke.sh` and
  `smoke.ps1` accept `SMOKE_SCRATCH=scratch-<name>` so the agent's checks never touch the developer's scratch folder.
- Contribution workflow: a feature is one branch and one pull request made of granular commits (each keeping the
  build green), merged with rebase merging, instead of a single commit per feature.
- Dependabot pull requests that only touch CI or build tooling are exempt from changelog entries and smoke tests;
  runtime library upgrades still require a smoke test.
- Dependabot runs monthly with one grouped minor/patch PR per ecosystem; major versions and Kotlin, KSP,
  Compose and Ghost are upgraded by hand (`GUIDELINES.md` §14). Security updates stay immediate.
- Dependencies: Kotlin 2.4.21, ktlint 1.8.0 (Gradle plugin 14.2.0), Gradle 9.8.2, JUnit 6.1.3, slf4j 2.0.20;
  CI actions `checkout` v7, `setup-java` v6, `setup-gradle` v6 and `upload-artifact` v7.

### Fixed

- Quick edits (a few properties in a row) no longer bring back an older version of the note or say it "changed
  outside Notekea": the check for outside changes could report a read of Notekea's own earlier save after a later one;
  it now reads the file again and ignores a file that holds the last save (found by the Self-Test's 1.14-02 on macOS).
- Settings saved by two Notekeas at once (opened twice, or the Self-Test relaunching it) no longer fail: each save
  writes a temporary file of its own and saves run one at a time in the whole process (found by the Self-Test's
  1.3-03).
- A tab shown again goes back to the same part of its note on a slow computer too: the scroll came before the page
  was laid out, gave up after 30 frames while Preview was still drawing a long note, or was pulled up when Preview
  laid the note out again shorter for a moment, and the note stayed at the top.
- Saving a note on Windows no longer fails when another reader has it open for a moment (Notekea checking it for
  outside changes, an antivirus, a sync app): the save tries again a few times before it gives up.
- A note deleted while Notekea was closed no longer comes back as a tab that says "This note couldn't be opened":
  the last run's tabs are restored against the scanned vault, not against the note cache, which can still list it
  (found by the Self-Test's 1.11-10).
- On macOS the selected entry of Files opens with Cmd+↓, as in Finder: Enter renames there, so a note could not be
  opened from the tree with the keyboard (found by the Self-Test's 1.4-02 on macOS). About lists Open the selected
  file with the key of each system.
- The installed app no longer crashes when it opens a vault (issue #68): its Java runtime now includes the JDK modules
  the app needs (`java.sql` for the database among them) and is built from JDK 21, the one the app is tested on, and
  `./gradlew check` fails when the code needs a JDK module the installers leave out.
- The installers add Notekea to the system's launcher with the app's logo (the Start menu and a desktop shortcut on
  Windows, Applications on macOS, the applications menu on Linux), and a newer Windows installer updates the installed
  app instead of adding a second one.
- Saving the app settings from two places at once (opening a folder saves the tree's open folders and the tabs) no
  longer fails with "Something went wrong": saves now run one at a time.
- The app follows the system's dark mode on Linux (read from the desktop portal: GNOME, KDE, COSMIC); it always
  showed the light theme there.
- A space typed at the end of a line in a code block now moves the cursor, and the block is shaded as a whole.
- The editor no longer scrolls the note when the text is clicked, and shows typed characters without ligatures
  (`->` stayed an arrow in the editor and in code). Palettes opened with text (a `#tag` search) put the cursor after it.
- Status bar items and tab labels stay on one line with an ellipsis in narrow windows instead of wrapping and being
  clipped; tab badge numbers never split across lines.
- Component previews stack the light and dark themes vertically, so Android Studio's limited preview width shows both.

### Added

- Properties view (ROADMAP 1.14): a note's frontmatter shows as a foldable form under its title, each property typed
  (text, list, number, checkbox, date picked on a compact calendar) and edited in place on a soft card, with Add
  property (also in the toolbar's More), rename and remove; Show source edits the YAML as written, and YAML the form
  cannot read is shown that way with an explanation. Every edit rewrites only its property's lines.
- About Notekea (ROADMAP 1.14): the logo, the version, a link to GitHub and every keyboard shortcut as keycaps; and a
  Settings dialog that says what is coming. A placeholder logo shows in the window, About and the welcome screen.
- Status bar (ROADMAP 1.14): the vault and the open file's folders on the left; the vault's scan with a progress bar,
  and the note's line:column, line endings, encoding and words on the right.
- Note info (ROADMAP 1.14): a side panel section with the note's words, reading time, links, tags, tasks done and when
  its file was created and modified.
- Bookmarks (ROADMAP 1.13): the star beside a note's title, "Bookmark" in a tree row's menu or Mod+Shift+B
  bookmark a note, folder or file; the Bookmarks section above the tree opens them, folds, reorders them by dragging
  and removes those gone from the vault. They follow renames and deletions made in Notekea and are kept per vault in
  the app's settings, never in the vault.
- Templates (ROADMAP 1.12): Mod+T or the toolbar's Insert template opens a picker of the notes in the templates
  folder (Obsidian's `templates.json`, or `Templates`), and the chosen one is written at the cursor, or as the whole
  of a note without text yet, with `{{title}}`, `{{date}}` and `{{time}}` filled in and its properties merged into
  the note's. "New note from template" in a folder's menu makes a note filled once it is named. Without a templates
  folder, Create makes one with an example template.
- Tabs and split panes (ROADMAP 1.11): every note, file or folder opened gets its own tab (an open one goes to its
  tab); Mod+click or the middle button opens it behind the current tab. Tabs close with ✕, the middle button or Mod+W,
  cycle with Ctrl+Tab / Ctrl+Shift+Tab and jump with Mod+1…9. Split right (Mod+Shift+D or the tab menu) shows two
  panes side by side; tabs can be dragged to reorder them or between the panes. A folder opens as a grid of cards, a note's card showing the
  first picture it embeds. Each note keeps its scroll and cursor while the app runs, and the tabs and split come
  back when the vault is opened again.
- A tool rail on the left edge, as in Android Studio, stays when the sidebar is hidden: Files, Search notes, New note,
  New folder and Today. The sidebar's header gets Expand all, Collapse all and Minimize beside Sort. A hidden sidebar
  no longer leaves a button over the tabs.
- New note and New folder open a hidden sidebar, so the new entry is seen where it went with its name ready to type.
- The window opens large enough for the sidebar, the note and the outline, and keeps that size from the welcome
  screen to the open vault.
- Daily notes (ROADMAP 1.10): "Today" in the sidebar and Mod+D open today's note, creating it if needed, in the folder,
  name format and template of Obsidian's Daily notes settings (`.obsidian/daily-notes.json`), or in `Journal/` as
  `YYYY-MM-DD` without them. Today is the date in the computer's time zone. A daily note's header has arrows
  (Mod+Alt+← / Mod+Alt+→) to the daily notes before and after it.
- Outline (ROADMAP 1.9): a side panel on the right of the editor lists the note's headings as an indented tree. A
  click (or Enter) goes to the heading, in Edit mode with the cursor on it, and the heading the view is in is
  highlighted while scrolling, in Edit and Preview. A note without headings says how to add one. Mod+Shift+O moves
  the keyboard to the outline (arrows move, Enter goes, Esc returns to the note); Mod+Shift+\ or the toolbar's
  Outline button shows or hides the panel, whose width and visibility are remembered. When the window is too narrow
  for both, the panel floats over the note while it is used.
- Images and attachments (ROADMAP 1.8): images embedded with `![[image.png]]` or `![alt](path.png)` show in Preview
  and under their line in Edit, at their own size or the width the note asks for (`|300`), never wider than the note.
  Other files show as a chip that opens them with the system's app, and a missing file offers Locate…, which rewrites
  the link to the chosen file. Pasting an image (Mod+V), dropping files or choosing Image or file in the toolbar saves
  them into the attachments folder (Obsidian's `attachmentFolderPath`, otherwise `attachments/`) and embeds them; a new
  image gets a width to see and change (`|400`, or its own when narrower).
  Images also show inside table cells (`![[image.png\|80]]`) and sentences in Preview, and a file inserted on a
  table row goes into its cell.
  On Linux Wayland desktops a pasted image is read with `wl-paste` (wl-clipboard), since Java only sees text there;
  without wl-clipboard the editor says how to install it instead of waiting seconds and pasting nothing.
- Embedded notes: `![[Note]]` shows the note's text in place and `![[Note#Heading]]` one section of it, in Preview
  and under its line in Edit, as Obsidian does; the title opens the note.
- The sidebar lists the vault's other files (images, PDFs…) with their extension and an icon; selecting one shows the
  picture beside the tree, with Open with its app and Show in Files. Folder numbers count every file, not only notes.
- Design system: `NotekeaAttachmentImage`, `NotekeaFileChip` and `NotekeaMissingFile`.
- Formatting toolbar (ROADMAP 1.17): under the note's title, Edit | Preview tabs (Mod+E; Preview is Read mode) and
  buttons with plain-language tooltips for Heading (Large, Medium, Small), Bold (Mod+B), Italic (Mod+I), Link to a
  note (Mod+K, chosen in the quick switcher, the selection becoming its label), Web link (Mod+Shift+K), Tag (a picker
  that searches the vault's tags, in the frontmatter or the text, and creates new ones), Bulleted list, Numbered
  list, Checklist, Callout (every type, optionally started folded, explained in the menu), Code block (plain text or
  one of ten languages) and More (Quote, Divider, and Table with its size picked on a grid). A button
  formats the selection (which stays selected) or the word at the cursor, and pressing it again removes the
  formatting; elsewhere it inserts an example to type over. Lists continue with Enter, nest with Tab and Shift+Tab,
  and lose their marker with Backspace. The toolbar stays in view while scrolling, Find in note sits at its end, and
  Preview keeps only the tabs and Find.
- The vault index also reads the `#tags` written in each note's text (the note cache is rebuilt once).
- Design system: `NotekeaCompactTabs`, `NotekeaGridSizePicker`, menu items that keep their menu open
  (`keepsMenuOpen`), explain themselves (`supportingText`) or open a submenu (`submenu`), and buttons, menus and tabs
  that leave the keyboard in the text (`takesFocus`).
- Tasks and callouts (ROADMAP 1.7): tasks show a clickable checkbox over their `[ ]` in Edit mode, and Mod+Enter
  toggles the task on the cursor line; the change is written to the file like any edit. In Read mode, Obsidian callouts
  (every type and alias, unknown types as notes) are cards with an icon and a color by urgency, fold with `-` / `+`
  and a click on the title, and nest at any depth; tasks checked with a capital `[X]` show as checked.
- Design system: `NotekeaCheckbox` and `NotekeaCallout`.
- Crash reports: when Notekea fails, it saves a report in the `crashes` folder of the app data directory (the
  last ten are kept; versions and the error, never note content) and shows "Notekea needs to close" in its own
  dialog window, in the app's theme, with Copy details and Report on GitHub, which opens a new Crash report issue form to paste it into, instead of Compose's raw
  error message. The open note is saved before the app closes. `NOTEKEA_CRASH_TEST=1` triggers a crash for testing.
- Quick switcher and search (ROADMAP 1.6): a "Search notes" button at the top of the sidebar (showing its shortcut) or
  Mod+P opens a palette over the window that finds notes by name, alias, frontmatter title or path (`gard/tom`) as you
  type, ignoring letter case and accents, with the matched letters in bold and the most recently changed notes before
  you type. Mod+Shift+F (or Tab in the palette) searches the text of the notes, showing the matching line as plain text; a `#tag`
  finds notes tagged in their frontmatter or text, nested tags included, and clicking a `#tag` in Read mode searches
  it. Arrows, Enter and Esc work without leaving the field, and the chosen note opens and is selected in the tree,
  with the keyboard in its text (closing the palette gives the keyboard back to the open note). Note texts stay in
  memory between searches, so only changed notes are read again.
- Design system: `NotekeaPalette`, `NotekeaPaletteItem` and `NotekeaSearchButton`.
- Smoke data for 1.6: aliases for Arepas and a nested `#recipe/drinks` tag in the sample vault.
- Editor (ROADMAP 1.5): selecting a note opens it beside the tree with its folder, an editable title that renames it
  and updates the links to it, and its tags and aliases. The body is a text field with Markdown highlighting
  (headings, emphasis, code, links, wiki links, tags) whose frontmatter is kept byte for byte, Windows line breaks
  included. Edits save a second after the last keystroke, at once with Mod+S, and before switching notes, leaving the
  text or closing the window. An outside change reloads the note, or asks Keep mine / Load theirs when there are
  unsaved edits; a note deleted while open offers Restore or Close. Mod+F (or the magnifier) finds in the note; Mod+E
  switches to Read mode, rendered with code blocks colored by language and clickable links (a missing wiki link's
  note is created), and the chosen mode is remembered. After a rename, a notice names the notes whose links changed
  and leaves by itself after 10 seconds.
- Design system: `NotekeaTitleField`, `NotekeaFindBar`, `NotekeaMarkdown` and `Modifier.focusBorder`, the one focus
  style of every clickable component; the window waits for screens to save before it closes.
- Libraries: `multiplatform-markdown-renderer-code` with `dev.snipme:highlights`, for code colors in Read mode.
- Smoke data for 1.5: an Editor folder in the Playground Vault and `edit-note`, `delete-note` and `check-note` in
  `smoke.sh` / `smoke.ps1`.
- Vault tree and note list (ROADMAP 1.4): a resizable (200–480 dp) and hideable (`Mod+\`) sidebar with the vault's
  folders and Markdown notes, note counts, natural sorting by name or date, keyboard navigation, a context menu and
  shortcuts (Mod+N new note, F2 / Enter on macOS rename, Delete / Cmd+Backspace on macOS delete). Notes and folders
  are created, renamed in place and deleted to the system trash (`gio trash` on Linux), falling back to the vault's
  `.trash` folder; renaming updates the wiki and Markdown links of the notes that point to the entry and says how many changed. The
  tree follows outside changes through a file watcher, reads frontmatter titles, tags and aliases with Ghost, and
  opens 5,000 notes in under a second from a SQLite cache (`notekea.db` in the app data directory). Expanded
  folders are remembered per vault; sort, sidebar width and visibility for all vaults.
- Design system: `NotekeaIconButton`, `NotekeaTreeItem`, `NotekeaInlineTextField`, `NotekeaSkeletonRow`,
  `NotekeaContextMenuArea`, `NotekeaConfirmDialog` (platform button order) and `NotekeaSplitPane`; error banners
  stack their buttons when narrow; icons from Lucide through `NotekeaIcons` (a Konsist rule keeps the library there).
- Smoke data for 1.4: a Playground Vault to change freely, simulated outside changes, a generated 5,000-note vault
  and a cache reset in `smoke.sh` / `smoke.ps1`.

- Smoke test data in `smoke-data/`: sample vaults (`Personal Notes`, `Work Obsidian`, `Empty Vault`) and
  `smoke.sh` / `smoke.ps1` for scenarios Git cannot hold (moved and unreadable folders, first launch); a rule in
  `GUIDELINES.md` §8 requires every feature to ship the data its smoke test needs.
- Open a vault (ROADMAP 1.3): a welcome screen to open a folder with the native picker (Ctrl+O / ⌘O anywhere) or
  create a new vault with a suggested name and location; the last vault reopens on launch; recent vaults show
  their folder and when they were opened, and a moved folder can be located or removed. Settings live in
  `settings.properties` in the app data directory. It is the reference MVI screen (`ARCHITECTURE.md` §10).
- Code rule: interfaces are fully abstract, with no default bodies and no extension functions or properties on them
  (enforced by Konsist).
- Product principle in `GUIDELINES.md`: friendly first, powerful underneath (plain language, sensible defaults,
  shortcuts as visible hints, no dead ends).
- Design system: `NotekeaCard`, `NotekeaWordmark`, a low-emphasis `TEXT` button style, shortcut hints on buttons,
  and disabled rows with trailing content in `NotekeaListItem`.

- Android target in `composeApp` (AGP 9.4.1 KMP library plugin, no Android app yet) so Android Studio renders the
  common `@Preview`s; all common code now also compiles for Android, an early check for the mobile phase.

- Theme and design system (ROADMAP 1.2): "cobalt and tangerine" palette with light and dark schemes, bundled Inter and JetBrains Mono,
  type scale, 6 dp / 10 dp shapes, spacing and motion tokens; components `NotekeaButton`, `NotekeaTextField`,
  `NotekeaListItem`, `NotekeaTab`, `StatusBarItem`, `EmptyState` and `ErrorBanner`; a WCAG AA contrast test for the
  palette; and a developer gallery (`./gradlew :composeApp:run -Pgallery`).
- Design system rule: every reusable UI element comes from `core.presentation.components` (`NotekeaTabRow` added);
  a Konsist test fails the build if a screen imports a Material 3 component directly.
- IDE previews: every design system component and `App` have `@Preview`s in the light and dark themes
  (`NotekeaPreviewThemes`); a Konsist test requires a preview for every UI composable, and screens preview each
  UI state.

- Platform conventions (ROADMAP 1.1): `PlatformConventions` resolved at startup with the primary shortcut modifier,
  quit shortcut, dialog button order and per-user application data directory for Windows, macOS and Linux;
  Koin is started on launch. It is the reference implementation for new features (`ARCHITECTURE.md` §10).
- Architecture rule: only `JvmSystemEnvironment` may read the operating system name.
- Status tracking in `ROADMAP.md` (⬜ / 🚧 / ✅ per feature and a count per milestone).
- `MVP_SMOKE_TEST.md`: the manual test run for Windows, macOS and Linux before v0.1.0; every feature adds its cases.
- Code rules: single-parameter class constructors stay on one line; constants live in the class's
  `companion object`, which is the last element of the class (enforced by Konsist).
- The desktop JVM runs with `--enable-native-access=ALL-UNNAMED`, removing the Skiko native-access warning.

- Kotlin Multiplatform project skeleton with a Compose Desktop window (`composeApp`, JVM target).
- Domain interfaces for the core features: `LlmEngine`, `VectorIndex`, `GitSync` and `FileWatcher`.
- Core building blocks: `Outcome` for typed errors, `AppDispatchers` (`JvmAppDispatchers` on desktop) and
  `BaseViewModel` for MVI screens, with unit tests.
- Konsist architecture tests enforcing the layer boundaries and conventions of `ARCHITECTURE.md`, including one
  top-level type per file (DTOs and interfaces each in their own file) and utilities in separate files.
- Version catalog with the MVP libraries (Ghost, Koog, Lucene, java-llama.cpp, JGit, SQLDelight, Koin, among others).
- ktlint configuration (`.editorconfig`) and the `:ktlint-rules` module with the custom rules
  `expression-on-assignment-line`, `max-function-length` (40-line function bodies) and `parameter-order`.
- GitHub Actions CI that runs lint, every test and the coverage check on Windows, macOS and Linux on each pull
  request and merge to `main`.
- Kover coverage with an 80% minimum line coverage enforced per module (Composable UI excluded), so a
  well-tested module cannot hide an untested one.
- Versioned pre-commit hook (`ktlintFormat`, `check`, `koverVerify`), installed automatically by the first build.
- Dependabot for the version catalog and GitHub Actions.
- `.gitattributes` normalizing line endings to LF on every OS.
- Kotlin warnings are treated as errors.
- Apache License 2.0 (`LICENSE`, `NOTICE`).
- Community files: `README.md`, `CONTRIBUTING.md`, `SECURITY.md`, pull request template and issue templates.
- Privacy rules in `GUIDELINES.md`: zero telemetry, no note content in logs, credentials only in the OS keychain.
- Mandatory manual smoke test on at least one OS for every `feature/` and `fix/` pull request.
- Technical spike report `docs/spikes/2026-10-23-ghost-llama.md`: app launch, Ghost JSON/YAML and java-llama.cpp
  embeddings and chat verified on Linux, with the gotchas that shape the roadmap.
- Documentation: `LIBRARIES.md`, `GUIDELINES.md`, `ARCHITECTURE.md` (naming, `Outcome` with sealed errors,
  `BaseViewModel`, injected dispatchers and clock, string resources, testing strategy),
  `PLATFORM_GUIDELINES.md`, `ROADMAP.md` (MVP with
  Obsidian-level essentials: backlinks, attachments, tasks, callouts, outline, daily notes, tabs and splits,
  templates, bookmarks, properties, embeds and graph view), `CLAUDE.md` and this changelog.
- Agent skills in `.claude/skills`: Compose Multiplatform skills (Meet-Miyani/compose-skill) and macOS design
  guidelines (ehmo/platform-design-skills), with their licenses and `THIRD_PARTY_NOTICES.md`.
