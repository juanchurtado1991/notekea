# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

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
