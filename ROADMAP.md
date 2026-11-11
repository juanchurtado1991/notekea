# Notekea — Roadmap

The MVP is a desktop app (Windows, macOS, Linux) that answers one promise:

> Open your Markdown folder and, without configuring anything, Notekea shows which notes are related and answers
> questions about them citing the exact paragraph. Everything on your computer.

Milestones are delivered in order. Each feature is one branch and one pull request, with granular commits
(see `GUIDELINES.md`); the suggested
branch name is given next to it. Estimates assume one developer and are meant for planning, not as promises.

## Product principles

- **Notekea never modifies a note without an explicit user action.** Semantic relations are shown, not written:
  the related notes panel suggests connections and the user turns one into a real `[[link]]` with one click.
- **Obsidian compatibility:** a vault edited in Notekea stays valid in Obsidian and vice versa. Obsidian-flavored
  Markdown (wiki links, embeds, callouts, tasks, frontmatter properties) is supported. When `.obsidian/` exists,
  its settings for attachments, daily notes and templates folders are honored (read-only).
- **Local only:** see the privacy rules in `GUIDELINES.md`.

**Closing a milestone:** every milestone ends with its **performance pass** (its last item: 1.18, 2.9, 3.7, 4.3, 4.8,
5.4), and it is done when all its items are ✅; the next one then starts.

**One QA run before the release (decided with the developer, 2026-11-04):** after M1's, the only QA run is the release
run, between 5.4 and 5.5, on Windows, macOS and Linux (`qa/README.md`). It covers every case of `MVP_SMOKE_TEST.md`, so
it needs one or two weeks for the run, its fixes and a new build. Until then each feature keeps its local smoke test
and its Auto-Test cases, which CI runs on the three systems, and two checks cover what those cannot see: a packaged
app that installs and starts on each system every night (2.13), and the developer installing the app on Windows and
macOS for half an hour at the end of M3, to see the chat answer there (no QA run, no release candidate).

**Performance pass** (`GUIDELINES.md` §8, `docs/performance.md`): the milestone's **automated smoke runs** (below)
drive the real app through its cases on the smoke data while memory is sampled, so each pass profiles where the
memory goes (resident memory, Java heap, native and Skia memory, per scenario), how much the app holds when idle
(after one and ten minutes with a vault open), how memory returns after notes, tabs and vaults are closed, startup
time and typing latency. The numbers go in `docs/performance.md`, next to the previous milestone's, and are checked
against the budgets of 5.4; what grew or leaks is cut in the same pass.

**Automated smoke runs:** whenever smoke cases are added to `MVP_SMOKE_TEST.md`, the cases a script can drive are
also added as automated runs of the real app (the infrastructure is built in 1.16 for every M1 case). They catch
regressions on every run and are the workload each performance pass measures; the manual smoke test keeps what only
a person can judge (looks, feel, the operating system's own dialogs).

**Status tracking:** features are never removed from this file. Each feature title carries its status —
⬜ not started, 🚧 in progress (branch open), ✅ done (with its pull request) — and the table below counts them.
`CHANGELOG.md` records what changed; this file records what is planned and what is left.

| Milestone | Goal | Estimate | Status |
|---|---|---|---|
| M0 — Foundation | Project setup, tooling, guidelines | Done | ✅ |
| M1 — Vault and editor | Open a folder, browse, read and edit notes with Obsidian-level essentials | 3.5 weeks | 18 / 18 |
| M2 — Indexing, links and related notes | Color themes, local embeddings, hybrid search, related notes, backlinks, graph, vault queries and tasks, packaged check | 5 weeks | 13 / 13 |
| M3 — Chat with citations | Local LLM, model download, RAG chat with citations, verified answers, deep answer mode, questions in plain words, actions from chat | 7 weeks | 2 / 10 |
| M4 — Git sync | Optional commit / pull / push of the vault | 1 week | 0 / 3 |
| M4b — Sources, study cards and agent | Web clips, documents, study cards, agent mode | 10 weeks | 0 / 5 |
| M5 — Release | Onboarding polish, performance and memory, packaging, first public release | 1.5 weeks | 0 / 5 |
| Post-MVP | Extensions (Koog + MCP), mobile, repositories, more | — |

---

## UI overview

### Layout

One main window with three resizable panes, a status bar and the native window frame, the same on every system, with no
menu bar (`PLATFORM_GUIDELINES.md`).

```
┌────────────────────────────────────────────────────────────────────────────────────┐
│ (native title bar)                                                                 │
├──┬─────────────┬──────────────────────────────────────────┬────────────────────────┤
│N │ Vault ⇅ ⌄ – │ ╭Pricing ✕╮ 2026-10-23 •│ plate.png  ⌄ + │ [Inspector][Chat]    – │
│▤ │ BOOKMARKS   │──────────────────────────────────────────│ ▾ OUTLINE              │
│▢ │   Roadmap   │    🗀 Projects                            │   Pricing ideas        │
│🔍│ FILES       │    Pricing ideas                     ☆   │     Competitors        │
│📅│ ▾ Projects  │    ╭ Properties 2 ──────────────╮        │ ▾ NOTE INFO            │
│  │   Notekea   │    │ tags  product  status draft│        │   236 words · 2 min    │
│  │ ▾ Journal   │    ╰────────────────────────────╯        │ ▾ RELATED (M2)         │
│  │   2026-10-23│    Edit Preview   [H B I│🔗 #│☑ •│▦│⋯] 🔍│ ▾ BACKLINKS (M2)       │
│  │             │    > [!tip] Start simple                 │                        │
│⇄ │             │    - [x] Compare competitors             │                        │
│ⓘ │             │    - [ ] Decide price                    │                        │
│⚙ │             │    ![[chart.png]]                        │                        │
├──┴─────────────┴──────────────────────────────────────────┴────────────────────────┤
│ ⇄ Vault › Projects › 🗎 Pricing ideas 8:7 │ LF │ UTF-8 │ 236 words │ ✓ Saved │ 120 %│
└────────────────────────────────────────────────────────────────────────────────────┘
```

- **Sidebar** (default 340 dp, as wide as the outline panel, min 200, collapsible with `Mod+\`): the vault's name with Sort, Expand all, Collapse
  all and Minimize, bookmarks, folder tree. A tool rail on its left (Files, Side panel, Search notes, Today; Open another vault, About and Settings at its bottom; 1.14)
  stays when it is hidden (1.11).
- **Editor** (flexible, min 480 dp): tab bar (browser-style tabs, an unsaved note's tab marked with a dot), the
  note's folder (a click shows it in Files), its title and star, properties, the toolbar (Edit | Preview and the
  formatting capsule, always one line), Markdown content. The page takes 70 % of the pane, between 760 and 1100 dp,
  and the whole window zooms with `Mod+=` / `Mod+-` / `Mod+0` (1.14). Without tabs it offers Find a note, New note,
  Today's note and Search in notes. Can be split into two panes side by side.
- **Right panel** (default 340 dp, min 280, collapsible with `Mod+Shift+\`): an **Inspector | Chat** control at its
  top (remembered). The inspector has sections that fold on their own (remembered): **Outline** and **Note info**
  (1.14), then **Related** and **Backlinks** (M2). **Chat** (3.3) fills the panel, since a conversation needs its full
  height; until it is built it says "Coming soon" (1.14).
- **Status bar** (24 dp, since 1.14): where the user is (the vault, the folders and the open file, each shown in Files
  on a click) on the left; on the right the work in progress with a thin progress bar (the vault's first scan, then
  indexing), and for a note the cursor's line:column in the file, its line endings, its encoding, its words and
  whether it is saved; last, the zoom while it is not 100 % (a click resets it). Then indexing progress, model state
  and sync state join it (M2–M4). Each of those is clickable and opens its
  detail (indexing log, model manager, sync panel).
- Below 1000 dp of window width the right panel becomes an overlay; below 760 dp the sidebar does too.

`Mod` = Cmd on macOS, Ctrl on Windows and Linux.

### Design tokens

Notekea has its own visual identity, identical on every platform: a vivid **cobalt blue** for actions and selection
and a **tangerine** accent, its complementary color, that keeps a nod to the kea's orange underwings. All values live
in the theme; composables never hard-code colors. Typefaces approved on 2026-10-23; the palette was chosen the same
day after two earlier versions (saturated olive and orange, then sage and terracotta) were judged too dull. The table
below is that first palette, which the logo keeps; the app's themes are in 2.12.

| Token | Light | Dark | Use |
|---|---|---|---|
| `primary` | Cobalt `#1F5BD8` | `#7FB0FF` (dark text on it) | Primary buttons, selection, focus ring, links |
| `primaryContainer` | `#DCE7FD` | `#16294D` | Selected list items |
| `accent` | Tangerine `#FF7A45` | `#FF9A6B` | Citations, highlights, "new" badges (dark text on it) |
| `background` | `#F7F9FC` | `#0C111A` | Window background |
| `surface` | `#FFFFFF` | `#141B26` | Panes, cards |
| `onSurface` | `#141A24` | `#E6EBF3` | Body text |
| `muted` | `#5D6676` | `#94A0B3` | Secondary text, placeholders |
| `outline` | `#D6DDE8` | `#273142` | Borders, dividers |
| `error` | `#B3261E` | `#F2B8B5` | Errors, conflicts |

- **Typography:** one bundled UI typeface (Inter, SIL OFL) for UI and editor body; a bundled monospaced typeface
  (JetBrains Mono, SIL OFL) for code blocks. Scale: 12 / 14 (body) / 16 / 20 / 28.
- **Spacing:** 4 dp grid (4, 8, 12, 16, 24, 32). **Corner radius:** 6 dp controls, 10 dp cards.
- **No menu bar:** the UI is the same on the three systems. Commands live in the tool rail, the toolbars and their
  shortcuts, listed in About. macOS's own app menu keeps About, Settings and Quit (`PLATFORM_GUIDELINES.md` §4).
- **Motion:** 150–200 ms for panel open/close; no animation longer than 250 ms; respect "reduce motion": the setting
  in Settings → Appearance or the system's turns animations off.
- **Themes:** light and dark, following the system by default; since 2.12 also chosen in Settings → Appearance,
  with eight color themes, Workspace Blue the one it starts with.
- **Accessibility:** WCAG AA contrast for text, every action reachable by keyboard, visible focus ring,
  semantics for screen readers on all interactive elements.

### UI states

Every screen and panel defines its **empty**, **loading**, **error** and **content** states. They are part of the
MVI `State` and covered by reducer tests.

---

## M0 — Foundation ✅

Project skeleton, version catalog, ktlint with custom rules, Kover, pre-commit hook, CI on three OSes, guidelines,
platform guidelines, community files. See `CHANGELOG.md`.

---

## M1 — Vault and editor

M1 is closed (2026-11-01): its QA run (`qa/runs/M1.md`) passed on macOS with m1-rc1. Its fixes are on `main` without a
release candidate of their own: the release run checks them, with Windows and Linux; 1.11-16 (tab drag, #103) is
deferred to M2.

### ✅ 1.1 Platform conventions — `feature/platform-conventions` — [notekea#15](https://github.com/juanchurtado1991/notekea/pull/15)

The platform conventions from `PLATFORM_GUIDELINES.md`: current OS, primary shortcut modifier, quit shortcut,
dialog button order, application data directory. Implemented as the `SystemEnvironment` port (read-only facts,
`JvmSystemEnvironment` on desktop) plus `ResolvePlatformConventionsUseCase` (all mapping logic, tested for every OS
from `commonTest`), exposed through Koin as a single `PlatformConventions`. This is the reference implementation
for domain, data, dependency injection and tests (`ARCHITECTURE.md` §10).

- **Done when:** every mapping is unit tested; nothing else checks the OS name.

### ✅ 1.2 Theme and design system — `feature/design-system` — [notekea#16](https://github.com/juanchurtado1991/notekea/pull/16)

Notekea theme (tokens above), bundled fonts, light/dark, base components: buttons, text field, list item, tab,
status bar item, empty state, error banner.

- **Done when:** the window renders with the theme in both modes; components have a preview gallery screen
  available in debug builds.
- Implemented in `core.presentation.designsystem.theme` (internal `palette` + semantic `ColorThemeSchemes`,
  typography, dimensions) and `core.presentation.designsystem.components`; a test gates WCAG AA contrast for every
  text pair. The gallery opens with `./gradlew :app:run -Pgallery`.

### ✅ 1.3 Open a vault — `feature/vault-open` — [notekea#19](https://github.com/juanchurtado1991/notekea/pull/19)

Pick a folder with the native picker (FileKit); remember the last vault (Multiplatform Settings); recent vaults list.

**UI — Welcome screen** (shown when no vault is open):

```
┌───────────────────────────────────────────┐
│                 🦜 Notekea                 │
│   Your notes, connected by local AI.       │
│                                            │
│   [ Open folder… ]   [ Create new vault ]  │
│                                            │
│   Recent                                   │
│   ~/Notes                    2 days ago    │
│   ~/Work/Obsidian            last week     │
└───────────────────────────────────────────┘
```

- States: no recents (only the two buttons; since 1.14 beside a card of what Notekea is), folder missing (recent item greyed with "Locate…"),
  permission error (error banner with the path).
- Shortcut: `Mod+O` opens a folder.
- **Done when:** a vault opens, survives restart, and missing folders are handled.

### ✅ 1.4 Vault tree and note list — `feature/vault-tree` — [notekea#21](https://github.com/juanchurtado1991/notekea/pull/21)

Read the folder recursively (Okio), only `.md` files, ignoring `.git`, `.obsidian`, `.trash` and hidden folders.
Parse frontmatter (title, tags, aliases) with Ghost YAML. Live updates through `FileWatcher`. Metadata is cached in
SQLite (SQLDelight, `notekea.db` in the app data directory) so a known vault opens from the cache and only changed
notes are read again.

**UI — Sidebar:**
- Folder tree with expand/collapse, note count per folder.
- Sort by name or modified date; selection highlighted with `primary`.
- Context menu: New note, New folder, Rename, Reveal in Finder/Explorer/Files, Delete (confirmation dialog
  with "Delete note" button, platform button order).
- States: empty vault ("This folder has no notes yet" + "New note" button), loading skeleton, error banner.
- Shortcuts: `Mod+N` new note, `F2` / `Enter` (macOS) rename, `Delete` / `Mod+Backspace` (macOS) delete.
- Sidebar resizable (200–480 dp, 260 by default) and hideable with `Mod+\`; expanded folders remembered per vault,
  sort, width and visibility for all vaults.
- Delete moves to the system trash (`gio trash` on Linux), falling back to the vault's `.trash` folder.
- Rename updates the `[[wiki links]]`, embeds and Markdown links that point to the entry, as Obsidian does by
  default, so no link breaks; a notice says how many notes changed. A note that changes while it is rewritten is
  left as it was and counted in the notice. Keeping the old links is a Settings option (5.2).
- Until the editor (1.5), the main area shows the selected note's title, folder, tags and aliases.
- **Done when:** a 5,000-note vault lists in under 1 s after the first scan, external changes appear within 1 s.
- Smoke test: cases 1.4-01 to 1.4-12; 1.3-02, 1.3-04 and 1.3-07 rewritten for the tree.

### ✅ 1.5 Editor — `feature/editor` — [notekea#39](https://github.com/juanchurtado1991/notekea/pull/39)

`BasicTextField` with Markdown highlighting (headings, bold, italic, code, links, `[[wiki links]]`, `#tags`),
autosave (debounced 1 s), Read mode with the Markdown renderer and clickable `[[links]]`. Only the body is edited:
the frontmatter is kept byte for byte, and its tags and aliases show read-only in the header. `#tags` are
highlighted; clicking one searches it in 1.6. Notes open in the mode last chosen (Edit or Read), remembered
between runs.

**UI — Editor pane:**
- Header: note title (editable, renames the file and updates the links to it), path breadcrumb, Edit / Read toggle
  (`Mod+E`) (revised by 1.17 and 1.14: Edit | Preview in the toolbar, the folder above the title).
- Unsaved indicator in the header while the debounce is pending; "Saved" fades after 1 s (revised by 1.14: the save
  state lives in the status bar, and the unsaved note's tab shows a dot). `Mod+S` saves at once;
  leaving the text, opening another note and closing the window save first.
- Conflict banner if the file changed on disk while editing: "This note changed outside Notekea —
  Keep mine / Load theirs"; a note without unsaved edits reloads by itself. A note deleted while open offers
  Restore / Close; a save never recreates a note that was moved or deleted.
- Find in note: `Mod+F` opens an inline bar with match count and next/previous (`Enter` / `Shift+Enter`).
- Read mode: code blocks colored by language (Edit mode keeps code plain); `[[links]]` and links to notes open the
  note, a missing wiki link's note is created where the link says (a bare name at the vault root), a name several notes share opens the closest one
  (same folder, then nearest the vault root), web links open the browser.
- **Done when:** typing never loses text, external changes are detected, frontmatter is preserved untouched.
- Smoke test: cases 1.5-01 to 1.5-11; 1.4-01 and 1.4-06 rewritten for the editor.

### ✅ 1.6 Quick switcher and search — `feature/quick-switcher` — [notekea#52](https://github.com/juanchurtado1991/notekea/pull/52)

`Mod+P` opens a palette with fuzzy search over note names, aliases, frontmatter titles and paths (a search with `/`
matches the path), ignoring letter case and accents; `Mod+Shift+F` searches note content (plain text in M1, upgraded
to hybrid search in M2). A `#tag` in the search finds notes tagged in their frontmatter or text, nested tags included,
and clicking a `#tag` in Read mode searches that tag. Note texts stay in memory between searches, so only notes that
changed are read again.

**UI — Sidebar:** a "Search notes" button drawn like a search field under the vault name, with the `Mod+P` shortcut
shown on it, opens the palette, so search is in sight without knowing the shortcut.

**UI — Palette:** centered overlay 560 dp wide over a dimmed window, with Notes / Content tabs (`Tab` switches,
keeping the text), the input, and up to 10 results with title, folder and a matching line (the alias or path that
matched, or the line of content as plain text, without Markdown marks), matches in bold. Before typing it lists the
most recently changed notes. Arrows move the selection (wrapping around), `Enter` opens the note and selects it in the
tree with the keyboard in its text, `Esc` or a click outside closes and gives the keyboard back to the open note.
While content search runs the previous results stay with a small indicator. Empty states: "No notes match", a hint for
an empty content search, and an empty vault.
- **Done when:** a note is found by name, alias, path or content with the keyboard alone, a clicked tag lists its
  notes, and typing stays fluid on a 5,000-note vault.
- Smoke test: cases 1.6-01 to 1.6-12; 1.4-01 and 1.5-01 revised for the aliases added to the sample vault.

### ✅ 1.7 Tasks and callouts — `feature/markdown-extensions` — [notekea#54](https://github.com/juanchurtado1991/notekea/pull/54)

Obsidian-flavored Markdown blocks that most vaults use.

- **Tasks:** `- [ ]` / `- [x]` (or `[X]`, any list marker, also inside quotes and callouts, never in code) rendered as
  checkboxes in Read mode and as clickable checkboxes over the brackets in Edit mode; clicking toggles the character
  in the file (explicit user action), saved and undone like any edit. `Mod+Enter` toggles the task on the cursor line.
- **Callouts:** `> [!note]`, `[!tip]`, `[!warning]`, `[!danger]`, `[!info]`, `[!quote]`, every other Obsidian type
  and their aliases, rendered in Read mode as cards with an icon per type and a theme color by urgency (warning in the
  accent color; danger, failure and bug in the error color; quote muted; the rest primary); foldable callouts
  (`[!note]-` / `[!note]+`) start closed or open and fold with a click on the title. A callout without a title shows
  its type. Code blocks inside callouts show without `>` marks.
- Unknown callout types render as `note`, never as broken text.
- **Done when:** every callout type and nesting level in the sample vault renders correctly in Read mode.
- Smoke test: cases 1.7-01 to 1.7-06; 1.5-08 revised for the checkboxes in Edit mode.

### ✅ 1.8 Images and attachments — `feature/attachments` — [notekea#56](https://github.com/juanchurtado1991/notekea/pull/56)

- Render images (`![[image.png]]`, `![alt](path.png)`), scaled to the editor width; `![[image.png|300]]` sets the
  width. In Preview they take their place; in Edit the link stays as written and the image shows under its line, so
  what was pasted is seen without switching modes. Links resolve as in Obsidian: a path, or a file name found in the
  note's folder, the attachments folder or anywhere in the vault.
- **Images in tables and paragraphs:** an embed inside a table cell (its pipe escaped, `![[image.png\|80]]`) or a
  sentence shows inline in Preview at its width; inserting a file with the cursor on a table row puts it in the cell.
- **Paste or drop an image** (`Mod+V`, drag and drop) saves it into the attachments folder (`Pasted image
  <date and time>.png` for a pasted one) and inserts the link at the cursor; a dropped file of any kind is copied the
  same way. **Image or file** in the toolbar does it with the system's file picker. A new image is embedded with a
  width the user sees and can change (`![[photo.png|400]]`: its own width, at most 400). Attachments folder: `.obsidian/
  app.json` `attachmentFolderPath` when present (the vault root, the note's folder, a subfolder of it or a fixed
  folder, as Obsidian), otherwise `attachments/`; a Notekea setting comes with the Settings screen.
- Non-image attachments (PDF, audio, other files) show as a file chip that opens with the system default app.
- **Embedded notes:** `![[Note]]` on a line of its own shows the note's text in place (without its frontmatter), in a
  frame whose title opens it; `![[Note#Heading]]` shows only that section, down to the next heading of its level. In
  Preview and under its line in Edit, as Obsidian does. A missing note or heading, a block (`#^id`), the note itself
  or an embed inside a sentence stay links (revised by 1.15: only an embed inside a sentence stays a link; the others
  show, nest or say why).
- **Files in the sidebar:** the tree lists files that are not notes too, with their extension and an image or file
  icon, among the notes by name; a folder's number counts all its files, notes and others. Selecting one shows it beside the tree: the
  picture with its size, or its name; with **Open with its app** and **Show in Files/Finder/File Explorer**. Rename
  keeps the extension; delete asks first ("Delete this file?").
- States: missing file (placeholder with the broken path and **Locate…**, which picks the file, copies it into the
  attachments folder when it is outside the vault, and rewrites the link to it), unsupported format (file chip).
- **Done when:** pasting a screenshot works on the three OSes and the result opens unchanged in Obsidian.

### ✅ 1.9 Outline — `feature/outline` — [notekea#57](https://github.com/juanchurtado1991/notekea/pull/57)

**UI — Right panel, Outline section:** the headings of the current note as an indented tree; click scrolls the editor
to that heading; the heading under the cursor is highlighted while scrolling. Empty state: "This note has no
headings". Shortcut: `Mod+Shift+O` focuses the outline.

- **Headings:** lines starting with one to six `#` and a space, as the editor highlights them, outside code blocks;
  shown as they read (no `#`, emphasis or link brackets). A heading is indented one step under the nearest heading of
  a lower level above it, as in Obsidian.
- **Going to a heading:** a click, or Enter in the outline, scrolls the heading to the top of the editor's view in
  Edit and Preview; in Edit mode the cursor goes to the end of its line and the text takes the keyboard.
- **Current heading:** the last heading at or above the top of the view (the first one while it is in view below
  it) is highlighted, and the outline keeps it in view; a heading just chosen stays highlighted even when the note
  ends too soon to bring it to the top.
- **Keyboard:** `Mod+Shift+O` shows the panel if hidden and focuses the outline at the current heading; arrows,
  Home and End move, Enter goes, Esc returns to the note.
- **The panel:** 340 dp by default (280–560, resized by dragging its edge), shown or hidden with `Mod+Shift+\`, the
  tool rail's Side panel button (the editor toolbar's Outline button until 1.14) or its own minimize button; width and visibility are remembered for every vault. It never
  leaves the editor narrower than 480 dp; when not even its narrowest width fits, it floats over the note instead and
  closes after a heading is chosen or with Esc.

### ✅ 1.10 Daily notes — `feature/daily-notes` — [notekea#58](https://github.com/juanchurtado1991/notekea/pull/58)

- "Today" in the sidebar and `Mod+D` open today's note, creating it if needed.
- Folder and date format from `.obsidian/daily-notes.json` when present, otherwise `Journal/` and `YYYY-MM-DD`;
  optional template (see 1.12).
- Previous / next day navigation in the note header (`Mod+Alt+←` / `Mod+Alt+→`).
- **Done when:** the date format, folder and template are honored and covered by tests, including timezones.
- **Today:** a row under Search notes, with its shortcut beside it. It selects today's note in the tree (opening
  its folders) and creates it first when it is missing, folders included; "today" is the date of the computer's time
  zone at that moment. An existing note opens as it is.
- **Obsidian's settings:** `folder`, `format` (Moment.js, English names, `/` makes subfolders, as in
  `YYYY/MMMM/dddd, MMMM Do YYYY`) and `template` of `.obsidian/daily-notes.json`, read again at every Today; blank
  fields mean the vault root, `YYYY-MM-DD` and no template, as in Obsidian. The template fills `{{date}}`,
  `{{time}}`, `{{title}}` and `{{date:FORMAT}}` / `{{time:FORMAT}}`; a template that cannot be read leaves the note
  empty. Templates in general come with 1.12.
- **Previous / next:** a note whose path is the folder plus a name in the format is a daily note; its header shows
  ‹ and › to the closest daily notes before and after it that exist (days without a note are skipped, nothing is
  created), greyed out when there is none on that side.

### ✅ 1.11 Tabs and split panes — `feature/editor-tabs` — [notekea#59](https://github.com/juanchurtado1991/notekea/pull/59)

- Each opened note becomes a tab; `Mod+click` (or middle click) opens a link in a new tab.
- `Mod+W` closes the tab, `Ctrl+Tab` / `Ctrl+Shift+Tab` cycle tabs (on every OS), `Mod+1…9` jump to a tab.
- Split the editor into two panes side by side (`Mod+Shift+D` or the tab context menu "Split right");
  drag a tab between panes. Maximum two panes in the MVP.
- Open tabs and splits are restored when the vault is reopened.
- Tabs live inside the window content; the window keeps its native title bar (`PLATFORM_GUIDELINES.md`).
- **Opening:** every note, file or folder opened (a click or Enter in the sidebar, a link, the quick switcher, Today,
  the day arrows, a new note) gets a tab after the active one; one that is already open goes to its tab, in either
  pane, so two editors never write the same note. `Mod+click` and the middle button open it in a tab behind the
  current one. The arrow keys in the sidebar only move the selection. The tree selects the active tab's file.
- **What a tab shows:** a note in its pane's editor (which remembers each note's scroll and cursor while the app runs),
  another file's preview (Open with its app, Show in Files), or a folder as a grid of cards for what is directly in
  it, subfolders first: a note's card shows the first picture it embeds, an image's card the image itself. A tab of a
  file that is gone says so.
- **Closing:** the tab's ✕, the middle button or `Mod+W`; the tab to the right becomes active. The tab menu also has
  Close other tabs, and Split right (or Move to the other pane). The "+" after the tabs finds a note to open.
- **Order:** a tab dragged along its row keeps to the row's line while the other tabs slide aside to open the place
  it would take; let go, it glides into that place.
- **Two panes:** Split right moves the active tab to a new pane on the right; a click in a pane makes it the active
  one (its tabs' line is blue, the editor shortcuts and the outline follow it); a pane left without tabs closes. One
  side panel serves both panes.
- **Tool rail:** a narrow strip of icons on the window's left edge, there whether the sidebar is shown or not, as in
  Android Studio: Files (shows or hides the sidebar), Search notes, New note, New folder and Today, each with a tooltip
  beside it (revised by 1.14: Files and Side panel, then Search notes and Today, and Open another vault, About and
  Settings at the bottom; new notes and folders come from Mod+N and the tree's menu). The sidebar's header holds the vault's name, then Sort, Expand all, Collapse all and Minimize; the
  Search notes and Today rows and the New note / New folder buttons move to the rail.
- **Window size:** the window opens large enough (1440 × 900 dp at most, never larger than the screen's usable area)
  to show the tool rail, the sidebar, a note and the outline beside it, which are open by default; the welcome screen
  and a vault's workspace share that size, so the window never resizes itself when a vault opens or closes. Its size,
  position and maximized state come back on the next launch, on a screen that still shows it; it never goes below
  560 × 400 dp, and its title names the active tab and the vault.
- **Restore:** each vault's tabs, active tabs and split are saved on every change in the app settings and come back
  when it opens again, without the tabs of files deleted meanwhile.

### ✅ 1.12 Templates — `feature/templates` — [notekea#61](https://github.com/juanchurtado1991/notekea/pull/61)

- **Templates folder:** the folder of `.obsidian/templates.json` when present (with its date and time formats),
  otherwise `Templates/` with `YYYY-MM-DD` and `HH:mm`, read again at every opening of the picker. Every note in it
  and its subfolders is a template, named by its path inside it (`Work/Weekly review`).
- **Insert:** `Mod+T` or the toolbar's Insert template opens a picker of templates, found by name as in the quick
  switcher; the chosen one is filled in and written at the cursor (in place of the selection), as one edit that one
  Undo removes. A note without text yet becomes the template whole. Read mode switches to Edit first.
- **New note from a template:** a folder's menu in the tree has "New note from template"; `Mod+T` with no note in
  the active tab does the same in the folder it shows (the vault root without tabs). The note is made with its name
  ready to type and filled once it is named, so `{{title}}` is its name.
- **Variables:** `{{title}}`, `{{date}}`, `{{time}}`, with optional format (`{{date:YYYY-MM-DD}}`), as in Obsidian.
- **Properties:** the template's frontmatter merges into the note's, as Obsidian does: missing properties are added,
  existing ones keep their value, and tags, aliases and CSS classes gain the items they lack.
- **States:** no templates folder ("Create a Templates folder to add templates" with a "Create" button, which makes
  it with an example template, "Meeting notes"); an empty folder and a search that matches nothing say what to do.

### ✅ 1.13 Bookmarks — `feature/bookmarks` — [notekea#63](https://github.com/juanchurtado1991/notekea/pull/63)

- **Bookmark:** the star beside the note's title, "Bookmark" in a tree row's menu, or `Mod+Shift+B` (the
  active tab's entry, or the selected one) bookmarks a note, a folder or another file; the same action removes it.
  The star is filled in yellow while the note is bookmarked, and a small yellow star follows a bookmarked entry's
  name in the tree.
- **Bookmarks section:** above the tree, under a "Bookmarks" header with their number that folds them, hidden while
  there are none. A bookmark opens its entry in a tab and selects it in the tree with its folders open; dragged up or
  down, it moves among the others; its menu removes it.
- **Changes:** a bookmark follows its entry when it is renamed or moved in Notekea (a folder takes the bookmarks
  inside it along) and goes when the entry is deleted there. One whose entry is gone outside the app is greyed out
  with a ✕ that removes it.
- **Storage:** in the app's settings, per vault, in order: the vault's files are never touched, and Obsidian's own
  bookmarks (`.obsidian/bookmarks.json`) are left alone.

### ✅ 1.14 Properties view — `feature/properties` — [notekea#65](https://github.com/juanchurtado1991/notekea/pull/65)

Frontmatter shown as a compact form above the note content. While it was reviewed the feature grew, by the
developer's decision, to **finish the base editor's UI** before it reaches `main`: the side panel as an inspector,
the workspace's frame (tool rail, tabs, status bar, welcome screen), About, Settings as "Coming soon", wide screens
and zoom, and two rounds of polish (below). The graph view (2.7) and onboarding (5.1) wait for their milestones.

- **The form:** a "Properties" card under the title (a soft background) with their number, open by default and folded
  with its header (the fold is remembered); one row per property with its type's icon (`#` for tags, like the
  toolbar's Tag), its muted name (renamed in place) and its value edited in place, highlighted while hovered, with a ✕
  that removes it shown on hover. A note without properties shows nothing.
- **Types** detected from the values, as Obsidian does: text, list (tags, aliases and CSS classes always; as chips
  with a field that adds one), number, checkbox, date (shown as `Oct 27, 2026`, typed as `2026-10-27` or picked on a compact month calendar
  whose weeks start on Monday, with Today). Nested and multi-line values
  are shown as written and edited in the source.
- **Add property:** in the block and in the toolbar's More, its menu picks the type; the new property gets a free name
  (`property`) to rename.
- **Editing** a property patches only its lines of the raw frontmatter text, in the style it had (block or inline
  list, quotes), preserving key order, comments and unknown keys; the frontmatter is never encoded again as a whole
  (Ghost would drop unknown keys and re-quote strings, see `docs/spikes/2026-10-23-ghost-llama.md`). Each edit is saved
  at once, never over an outside change.
- **Note info** (a section of the right panel, under Outline): the note's words, reading time, links, tags (from
  the text and the properties), tasks done of all, and when its file was created and last modified.
- **Chat mode** (the right panel's Inspector | Chat control): until 3.3 builds it, Chat says what it will do, marked
  "Coming soon", above where its question field will be, drawn dashed and dimmed; `Mod+Shift+O` always comes back to the inspector.
- **The workspace's frame** (decided with the developer while 1.14 was reviewed):
  - **Tabs:** as a browser's: the open tab rises with rounded top corners into the note's surface, its icon in the
    primary color; the others stay quiet, a faint fill under the pointer and a thin line between them, their close
    button shown on hover.
  - **Tool rail:** Notekea's logo on top, then the two panels (Files, Side panel), Search notes and Today; Open another vault,
    About and Settings at the bottom, as in Obsidian. New note and New folder leave the rail (Mod+N and the tree's menu stay), "Open another
    vault" leaves the sidebar's bottom, and the editor toolbar's Outline button leaves for the rail's Side panel.
  - **About:** a dialog with the logo, the name, the version and a link to GitHub, and every keyboard shortcut of the
    platform as keycaps, in groups from the most used (Notes, Writing, Workspace).
  - **Settings:** a dialog with the sections to come, dimmed, and "Coming soon" until settings are built.
  - **Welcome screen:** two columns (stacked in a narrow window): the logo, the actions and any error, and beside them
    the recent vaults, at most five shown and the rest scrolling inside, or what Notekea is while there are none.
  - **Status bar:** under the whole window, as Android Studio's: the vault › folders › file on the left; the vault's
    scan with a thin progress bar while it runs, then the note's line:column (in Edit mode, counted in the file),
    line endings (LF or CRLF), encoding and words on the right.
  - **Polish:** one surface for the panes, divided by quiet hairlines (no gap down to the window's background);
    BOOKMARKS and FILES headers in the sidebar like the inspector's; the selected row blue while its list has the
    keyboard and quiet grey otherwise (the focus ring only for a keyboard row that is not the selected one); tabs at
    least 96 dp wide, fading out at the strip's end when more are hidden, with a ⌄ listing every open tab; more room
    between a note's header and its toolbar; reading line height 1.6; brighter secondary text; the save state
    ("Saved" / "Unsaved") always in the status bar instead of beside the title; the vault's folder in the vault
    name's tooltip, not under it.
  - **Scrollbars:** a thin scrollbar shows on what scrolls (the tree, the outline, About's shortcuts, the recent
    vaults, the welcome screen), only while there is more to see.
  - **Logo:** a placeholder (a cobalt square with an "N") in the window, About and the welcome screen, until the
    designed logo (an SVG of paths on a 1024 square, with and without its background) replaces it and the packaged
    icons are drawn from it.
  - **Page width:** the note's page takes 70 % of the editor pane, never narrower than 760 dp nor wider than 1100 dp
    (nor wider than the pane), so a wide screen is used without lines too long to read.
  - **Zoom:** `Mod+=` / `Mod+-` scale the whole window by 10 % steps between 80 % and 160 %, `Mod+0` goes back to
    100 %; the scale is remembered and listed in About's Workspace shortcuts. While it is not 100 %, the status bar
    ends with it ("120 %"), and a click on it goes back to 100 %.
  - **Second polish:** a section header has a tooltip only when it has a shortcut; the editor toolbar is always one
    line, the groups that do not fit (lists and template, then links) moving to the top of More; a tab with unsaved
    changes shows a dot in place of its ✕; a folder or file of the status bar's path, or the folder above a note's
    title, shows it in Files (opening the sidebar); an editor without tabs offers Find a note, New note, Today's note
    and Search in notes with their shortcuts as keycaps; folding sections slide open and shut, Inspector and Chat
    fade into each other and a folder's rows fade in; pictures, PDFs and sounds or videos have their own icon colors
    in the tree, the bookmarks and the tabs.
  - **Version:** people see `0.1.0-alpha` (About, crash reports) until the first public release; the installers keep
    a plain numeric version, which DMG and MSI require.
- "Show source" toggles the raw YAML, editable and saved once typing pauses, like the text; YAML the form cannot read
  shows an error banner with its line and falls back to the raw view, never losing data.
- **Decided against** while reviewing, so later features do not bring them back:
  - Properties in a tab of the side panel: they stay only in the note, under the title.
  - Tabs in the side panel (Outline | Info): sections that fold on their own read better in a narrow panel.
  - A Tag button that only edits the properties: `#tag` in the text and `tags:` in the properties are both valid in
    Obsidian, so the toolbar keeps `#` and the properties keep their own tags row.
  - Undo after deleting: entries go to the system trash, which cannot be restored reliably on the three systems; the
    confirmation dialog is enough.
  - A 24 dp padding at the rail's top and bottom; the rail keeps 8 dp.
  - Numbers limited to positive integers: a Number property takes decimals and negatives, as in Obsidian, since
    the type cannot know what a property means (`price: 9.5`, `temperature: -2`); only what is not a number is refused.
- **Done when:** round-tripping every sample-vault note through the properties view produces byte-identical
  files when nothing was edited, and the cases 1.14-01 to 1.14-15 of `MVP_SMOKE_TEST.md` pass.

### ✅ 1.15 Embeds — `feature/embeds` — [notekea#66](https://github.com/juanchurtado1991/notekea/pull/66)

1.8 already shows `![[Other note]]` and `![[Other note#Heading]]` in a frame whose title opens the note, in Preview
and under its line in Edit. 1.15 completes them as Obsidian does (decided with the developer):

- **Nested embeds:** an embedded note's own embeds show inside its frame as in the note itself (notes in their frame,
  pictures at their width, file chips), three notes deep at most.
- **Blocks:** `![[Note#^id]]` shows the block marked `^id`: the list item or paragraph whose last line ends with
  ` ^id`, or the block (a table, a quote) just above a line holding only `^id`, without the marker.
- **When a note cannot be shown,** its place is the same frame, muted, saying why in plain words:
  - a note that does not exist yet: "Create note" creates it and opens it in a tab, as a click on a link to it does;
  - a heading or block the note does not have: "Open note";
  - an embed that comes back around (two notes embedding each other, or a note embedding itself): "Circular embed",
    with a yellow bar, and it stops there;
  - a fourth note deep: "Too deep to show", with a yellow bar and "Open note".
- An embed inside a sentence and `![[#Heading]]` (a section of the same note) stay links.
- **Done when:** `Editor/Embeds/Embeds practice` of the smoke data shows every case above in Preview and Edit, and
  the cases 1.15-01 to 1.15-05 of `MVP_SMOKE_TEST.md` pass.

### ✅ 1.16 Sample vault, automated smoke runs and code trim — `chore/sample-vault` — [notekea#67](https://github.com/juanchurtado1991/notekea/pull/67)

Before M1's performance pass (1.18), the M1 code gets its test data, its automated smoke runs and a trim, decided
with the developer on 2026-10-28. The smoke test of 1.18 then also proves the trim broke nothing.

- **Sample vault:** `smoke-data/vaults/Sample Vault/`, next to the smoke test vaults (one dataset for smoke tests,
  unit tests, evaluation and demos; `SmokeDataTest` and `smoke-data/README.md` are updated with it), with 40 Markdown
  notes on varied topics (projects, journal, books, recipes, meeting notes), with frontmatter, wiki links and some
  deliberately related notes without links. It also covers every feature above: tasks, all callout types, images and
  attachments, daily notes, templates, bookmarks, properties of every type, embeds (nested, by block, circular) and a
  `.obsidian/` folder with non-default settings. Used by tests, manual QA, the automated smoke runs and demos.
- **Automated smoke runs** (`auto-test/src/test/.../smokerun` since the Auto-Test, Compose UI Test): the real app (every Koin
  module, its view models, data layer and file system, drawn headless) with its data in a temporary folder, never the
  user's, and the system's dialogs and apps replaced (folder and file pickers, opening a file, the trash), driven
  through its UI on copies of the smoke vaults like a person would: find by text, click, type, press shortcuts. One
  run per M1 feature chains its main cases and checks their results, down to what is written on disk (the vault and
  its tree, Recent, tabs, bookmarks, typing saved, tasks, the outline, toolbar formats, search, attachments, daily
  notes, templates, properties, embeds), plus an idle run. Each run samples the Java heap and the process's resident
  memory when it starts and ends. They run in every `jvmTest` (so in `check` and in CI on the three systems), and
  `./gradlew :auto-test:smokeRun -PidleSeconds=600` runs them alone and writes
  `build/reports/smoke-runs/memory.csv`, the workload of 1.18 and every later performance pass. The cases they cover
  say so in the **Automated** column of `MVP_SMOKE_TEST.md` ("smoke run"); every later feature adds its own runs.
- **Code trim** (`ARCHITECTURE.md` §4 and §10, `GUIDELINES.md` §6), so the code stays small as M2 starts:
  - **Pass-through use cases go:** a use case exists only when it holds a decision or combines ports; one that only
    calls a port (`DeleteEntryUseCase` calling `Trash.moveToTrash`) is removed, with its Koin binding and trivial
    test, and the ViewModel calls the port. The Konsist rules are updated in the same change.
  - **No file over 350 lines** (300 is the aim), production and tests alike, enforced by a new ktlint rule,
    `notekea:max-file-length`. The files over it are split by sub-feature: `EditorViewModel` (reducers and handlers
    for properties, attachments, outline, find; the ViewModel only routes), `EditorContent`, `NotekeaTabStrip`,
    `EditorToolbar`, `VaultTreeViewModel`, `VaultTreeScreen`, `PropertiesView`, `SidePanel`, `WorkspaceContent`,
    `WelcomeContent`, `MarkdownFormatter`, the design system gallery (one section per file) and the large tests
    (`VaultTreeViewModelTest`, `EditorViewModelTest`, `ArchitectureTest`, `EditorToolbarTest`, split by behavior).
  - Behavior does not change: every existing test keeps passing unchanged in what it asserts.
- **Done when:** the Sample Vault and its tests are in, every automatable M1 case runs in the automated smoke runs
  on the three systems, no use case only forwards a call, no file is over 350 lines and the rule enforces it.

### ✅ 1.17 Formatting toolbar — `feature/formatting-toolbar` — [notekea#55](https://github.com/juanchurtado1991/notekea/pull/55)

A toolbar above the note, so people who do not know Markdown can format, link and tag in plain words; it writes the
same Markdown a person would type, so notes stay readable in any app.

**UI — Editor toolbar** (under the title, replacing the Edit / Read icon):
- **Edit | Preview** tabs, like GitHub's, switched with `Mod+E` (Preview is Read mode).
- Buttons with an icon, a plain-language tooltip and its shortcut, in one rounded capsule of groups: Heading, Bold,
  Italic · Link to a note, Tag · Checklist, Bulleted list · Insert template (1.12) · More (⋯), which holds the rest
  (Web link, Numbered list, Callout and Code block with their types beside the menu, Image or file, Quote, Divider,
  Table), so the bar stays short. The buttons: **Heading** (menu: Large, Medium, Small heading),
  **Bold** (`Mod+B`), **Italic** (`Mod+I`), **Link to a note** (`Mod+K`, picks the note in the quick switcher and writes
  `[[Note]]`, the selection becoming its label), **Web link** (`Mod+Shift+K`), **Tag** (the tag picker: the vault's
  tags, in the frontmatter or written in the text, the most used first; typing finds any of them or offers it as a new
  tag, which is found like the others once the note is saved; the selected text is the first search), **Bulleted
  list**, **Numbered list**, **Checklist**, **Callout** (menu of types with their icons in plain words, and "Start
  folded", explained in the menu), **Code block** (menu: Plain text and the ten most used languages Preview colors:
  Python, JavaScript, TypeScript, Java, Kotlin, C#, C++, Go, Rust, Shell) and **More** (Quote, Divider and Table, its
  size picked on a grid of up to 8 × 6 cells, with sample text in every cell).
- With a selection, a button formats it (bold wraps it, a list prefixes its lines); bold and italic format the word
  the cursor is in; elsewhere a button inserts an example with the placeholder text selected, ready to type over.
  Pressing a toggle again on formatted text removes it (bold, lists, quote, heading). The toolbar never takes the
  keyboard from the text, so the selection stays for the next button.
- Lists work as in other editors: Enter continues a bulleted, numbered or checklist item (an empty one ends the list)
  and a quote, Tab and Shift+Tab nest an item under the one above and take it back, and Backspace right after a
  marker removes it.
- The text shows what is typed: no ligatures (`->` stays `->`, in code too), a code block is shaded as a block and
  the cursor moves past the spaces typed in it, and clicking or typing never scrolls the note unless the cursor would
  leave the view; the toolbar stays at the top while scrolling.
- **Find in note** stays at the end of the toolbar; the formatting buttons hide in Preview.
- Aliases and frontmatter tags are edited in 1.14 Properties view (the frontmatter stays read-only in the editor).
- **Done when:** a person who never wrote Markdown can make a heading, a checklist, a callout, a table and a link to
  another note with the mouse alone, and every result reads the same in Preview and in Obsidian.

### ✅ 1.18 M1 performance pass: editor memory — `chore/editor-memory` — [notekea#69](https://github.com/juanchurtado1991/notekea/pull/69)

M1's performance pass (see "Closing a milestone"). The editor alone, before any AI, should stay light: measure how
much memory it uses and cut it, so M2 and M3 start from a small base (budget of 5.4: under 400 MB). The workload is
the automated smoke runs of 1.16, and its smoke test also checks the code trim of 1.16.

- **Measure:** the memory of the whole process (resident memory, not only the Java heap) on Linux, then Windows and
  macOS, with `smoke-data/vaults/Personal Notes` and `smoke-data/vaults/Ghost Serializer` (a code repository as 371
  notes, one per Kotlin file explaining it, the kind of vault the Repositories feature will write; it replaced a
  generated vault of 5,000 near-empty notes, which no one would have): right after opening, after opening and closing
  20 notes in tabs, after scrolling a long note in Edit and Preview, and after ten minutes idle. The numbers go in `docs/performance.md`.
- **No growth:** opening and closing notes, tabs and folders, and switching vaults, gives back what it took: memory
  after closing everything returns near the level after opening (no leak in the editor's caches, the tabs' scroll
  memory, the decoded images or the file watcher).
- **Cuts the measurements point at,** only those without a drawback (decided with the developer on 2026-10-28):
  bounded caches (decoded images, note texts, metadata), pictures decoded at the size they are shown, note texts
  never held twice, leaks fixed. Settings that trade speed or smoothness for memory stay out: measured on Linux the
  packaged app idles at 291 MB on the welcome screen, 228 MB with only the C1 compiler (slower heavy Java code) and
  161 MB with Skia also drawing in software (less smooth, more CPU), and neither is worth it; the AI's models, loaded
  only while used (3.1, 3.2), are what decide how light the app feels.
- **Audit of 1.16:** an audit of needless allocations ranked them by ease and risk; 1.16 already applied the easy,
  low-risk ones (editor scans per keystroke and caret move, tree copies per watcher change, stable Compose inputs,
  file previews released with their tab). Left for this pass: the old vault's ViewModels kept alive after switching
  vaults, the folder grid's covers and the editor's images held at full size, `ListEditing` splitting the whole note
  on Enter, the sort of the quick switcher's candidates and the natural name comparator's regex.
- **Done when:** the editor alone stays under 400 MB with the Ghost Serializer vault open on Linux, the measurements are in
  `docs/performance.md`, and a test keeps the bounded caches bounded. 5.4 measures the whole app again, AI included.
- **Result (2026-10-28):** the Ghost Serializer vault opens at 334 MB, idles at 451 MB after 20 of its notes were
  opened and closed and stays there once left (the generated Large Vault it replaced: 368 and 516 MB); Personal Notes
  reaches 761 MB after a 2,000-line note is scrolled in Edit and Preview, so the 400 MB budget holds while opening a
  vault but not after working in it. The
  Java heap stays under 40 MB (112 MB in Preview): the rest is heap the JVM keeps committed, native memory glibc keeps
  after Skia and text layout free it, and the layout of a long note (about 170 MB while it is open). Two JVM options
  would bring the idle Large Vault to 388 MB (`-XX:TrimNativeHeapInterval`, `-XX:G1PeriodicGCInterval`); the
  developer chose code cuts only, so the packaged app runs with none. Left for later: an editor that lays out only the
  visible part of a long note.

---

## M2 — Indexing, links and related notes

### ✅ 2.12 Color themes — `feature/color-themes` — [notekea#107](https://github.com/juanchurtado1991/notekea/pull/107)

Settings → Appearance, the first section of Settings to be built: the light and dark mode, and a color theme. Themes
were replaced on 2026-11-05: tones computed from two brand colors painted the whole window, so the eleven first themes
became eight made by hand, all in the image of Workspace Blue, the theme the app starts with.

| Theme | Character | Lead, dark | Lead, light |
|---|---|---|---|
| Workspace Blue | Quiet grays, a clear blue, an orange accent | `#669DF1` | `#1868DB` |
| Graphite | Almost monochrome | `#B9C0CC` | `#4A5160` |
| Sage | A calm green | `#7FC29C` | `#2C7A55` |
| Lagoon | A clean teal | `#5CC2C7` | `#0B7682` |
| Iris | A soft violet | `#A998F4` | `#6A4ED6` |
| Rose | An elegant pink | `#F08CA8` | `#BE3468` |
| Clay | Warm terracotta, like paper | `#E3966C` | `#AD4F28` |
| Honey | A quiet amber | `#E5B567` | `#8F6108` |

Whoever kept a theme that is gone gets the closest one (`ColorTheme.fromId`), and the first palette's `default` id now
leads to Workspace Blue.

- **Colors:** every theme has gray surfaces with a touch of its hue and one lead color for actions, selection and links,
  in a light and a dark version. Every pair of text and background meets WCAG AA (4.5:1), checked by a test for every
  theme and mode. The logo takes the theme's colors. The colors of meaning (bookmarks, file types, warnings, errors)
  stay the same in every theme.
- **UI — Settings → Appearance** (design A, picked 2026-11-01):
  - **Mode:** System, Light or Dark (a segmented control); System follows the operating system, as before.
  - **Theme:** a grid of cards, three per row, Workspace Blue first, each showing the theme's light and dark versions side by side with
    its name; the chosen one is outlined and checked. Clicking or Enter on a card applies it at once.
  - The other sections stay "Coming soon" (5.2).
- **Behavior:** the choice applies at once to every window, the crash notice included, and is kept for the next run
  (app settings, the same for every vault).
- **Done when:** each theme can be chosen with the mouse and the keyboard and shows in light and dark, the choice
  survives a restart, and the contrast test passes for all twenty-two color schemes.

### ✅ 2.1 Markdown chunking — `feature/chunking` — [notekea#108](https://github.com/juanchurtado1991/notekea/pull/108)

Split each note by headings into chunks of ~200–400 tokens with overlap; keep note path, heading path and
character range of every chunk (needed for citations). Pure `commonMain` logic, exhaustively unit tested.

- Implemented in `notes.domain` (features never depend on each other): `ChunkNoteUseCase` over the note's body
  (frontmatter left out, ranges in the body as the editor shows it), `NoteSections` by the outline's headings,
  `ChunkBlocks` and `ChunkPacker`. Sizes (`ChunkSize`, estimated tokens, `TokenEstimate`): a section under 200 joins
  the next ones under the same parent heading up to 400; one that fits 400 is a chunk; a longer one is split between
  its paragraphs, lists, tables and whole code blocks, packed to 300, each chunk starting with the previous one's last
  sentence or line (at most 60). 2.2 checks the estimate against the embedding model's tokenizer.
- Checked on all 431 notes of `smoke-data` (`ChunkSmokeDataTest`); no Auto-Test case, since nothing shows yet: the
  cases of 2.3, 2.4 and 2.6 exercise it.

### ✅ 2.2 Embedding engine — `feature/embeddings` — [notekea#109](https://github.com/juanchurtado1991/notekea/pull/109)

`LlmEngine.embed` implemented with java-llama.cpp and a small bundled GGUF embedding model
(~100 MB, e.g. nomic-embed or bge-small). Batched, off the UI thread, cancellable.

- **Multilingual** (the developer's requirement: notes and questions mix Spanish and English): the model is
  `granite-embedding-107m-multilingual` (Q8_0, 121 MB, 384 dimensions, 512 tokens, Apache 2.0), chosen over
  multilingual-e5-small and bge-m3 on Spanish questions about English notes (`docs/spikes/2026-11-01-embedding-models.md`).
- Implemented as the `TextEmbedder` port in `core.embedding` (shared, so the index can use it; `LlmEngine` keeps
  only generation) and `LlamaTextEmbedder`: loaded on first use, let go after three idle minutes, one call at a time
  off the UI thread, cancellable between texts, a text over 510 tokens refused rather than cut, vectors of length 1;
  java-llama.cpp's native library extracted to the app data, not `/tmp`. The build downloads the model from a pinned
  commit, checks its SHA-256 and bundles it in Notekea and Notekea QA; CI caches it.
- `TokenEstimate` (2.1) counts a token every two characters: the model's tokenizer splits code finely, and
  `ChunkTokensRealModelTest` checks every chunk of `smoke-data` fits it and is never over its estimate. The tests
  that load the model run in `modelTest`, in the nightly build, not in every pull request's `check`.

### ✅ 2.3 Hybrid index — `feature/hybrid-index` — [notekea#110](https://github.com/juanchurtado1991/notekea/pull/110)

`VectorIndex` with Lucene: HNSW vectors + BM25 keywords, merged with reciprocal rank fusion. Incremental
re-indexing using the SQLDelight index state (path, content hash, modified date). Index stored in the app data
directory, never inside the vault.

Lucene stays the desktop index (weighed against SQLite FTS5 + sqlite-vec on 2026-10-29): it is plain Java in the JVM
Notekea already runs, reads its index from disk instead of holding it in memory, and analyzes each language (Spanish
stems and accents) where FTS5 does not; sqlite-vec is a native extension to ship and load on every system. sqlite-vec
remains the plan for mobile (post-MVP 2).

**UI — Indexing progress:**
- Status bar: "Indexing 340 / 1,204" with a thin progress bar; "Indexed 1,204 notes" when done.
- Click opens a popover with the last run, duration, skipped files and a "Rebuild index" button.
- First run on a large vault shows a non-blocking banner in the editor: "Notekea is reading your notes.
  Related notes and chat will be ready in about 2 minutes." The app stays usable.
- **Done when:** editing one note re-indexes only that note within 2 s; closing the app mid-indexing resumes
  correctly.
- Implemented in `notes` (features never depend on each other): `note_index_state` (SQLDelight) keeps each note's
  text fingerprint (`ContentHash`), size, date and index version; `PlanIndexUseCase` picks the notes to read;
  `IndexVaultUseCase` chunks, embeds (title and headings in front) and puts only notes whose text changed, marking
  each done, so a run resumes; notes over 5 MB, unreadable or too long are skipped and named.
  `LuceneVectorIndex` keeps one index per vault in the app data's `index` folder (BM25 with `NoteAnalyzer`, no
  accents and Spanish stems, `lucene-analysis-common`; HNSW on unit vectors), merged by `RankFusion` (RRF).
  `SearchIndexViewModel` follows the tree: a run 300 ms after a change, a busy index tried again after 30 s,
  Rebuild index. UI design B (picked 2026-11-03): `SearchIndexStatus`, `SearchIndexDetails`, `SearchIndexNotice`,
  on the design system's `NotekeaStatusRing`, `NotekeaPopover` and `NotekeaNotice`.

### ✅ 2.4 Related notes panel — `feature/related-notes` — [notekea#111](https://github.com/juanchurtado1991/notekea/pull/111)

For the open note, the top 8 most similar notes (excluding notes already linked from it), updated when the note
changes.

**UI — Right panel, Related section:**
- Cards with note title, folder and the most relevant snippet (matching passage underlined).
- Actions on hover: Open, Open to the side, **Insert link** (adds `[[Note]]` at the cursor). Relations are never
  written to the note automatically; "Insert link" is the only way a suggestion becomes a real link.
- Section "Already linked" collapsed at the bottom.
- States: index not ready ("Related notes appear after indexing"), no results, error.
- Implemented in `notes`: `VectorIndex.nearest` (vectors only; `LuceneVectorIndex` by HNSW) and
  `FindRelatedNotesUseCase`, which embeds the note's chunks (title and headings in front, as indexed), keeps each
  other note's best chunk, leaves out the note and the notes it links (`LinkFinder`, `LinkResolver`) and keeps 8;
  `RelatedSnippet` highlights the passage's sentence that shares the most words with the note.
  `RelatedNotesViewModel` follows the open note (`FollowRelatedNotes`): 200 ms after a note opens, 1.5 s after typing
  stops, again when the index is ready. `Workspace.openBeside` opens a note in the other pane, splitting the editor if
  needed. UI design A, cards (picked 2026-11-03): `RelatedSection` in the inspector on the design system's
  `NotekeaPassageCard`; Outline and Related share the height Note info leaves.

### ✅ 2.5 Backlinks — `feature/backlinks` — [notekea#112](https://github.com/juanchurtado1991/notekea/pull/112)

A link graph built from `[[wiki links]]`, Markdown links, embeds and frontmatter aliases, kept up to date by the
`FileWatcher` (independent of the vector index, so it works even before indexing finishes).

**UI — Right panel, Backlinks section:**
- **Linked mentions:** notes that link to the current note, each with the paragraph containing the link.
- **Unlinked mentions:** notes that contain the current note's title or an alias as plain text, with a
  **Link** button that turns that mention into `[[Note]]` (explicit user action).
- Count badge on the tab; click a mention to open the note scrolled to it.
- Next to **Related**, this shows the difference between links the user made and connections the AI found.
- Renaming a note updates every link to it without asking, as Obsidian does by default (1.4); decided with the
  developer, who does not want a question on every rename.
- **Done when:** link resolution matches Obsidian's (shortest path, aliases, headings `[[Note#Heading]]`,
  block references are kept as-is) on the sample vault.
- Implemented in `notes`: `FindMentionsUseCase` reads the vault's texts (`NoteTextCache`, so only changed notes are
  read again) and lists, per note, its links that resolve to the open one (`LinkFinder`, `LinkResolver`: wiki links,
  embeds, Markdown links, headings and blocks) or, when it has none, the plain words that name it or an alias
  (`MentionFinder`: whole words, never in code, links, addresses or tags), each with its line as a reader sees it
  (`MentionPassage` on `MarkdownLineStripper`). Only notes holding the name are parsed. Obsidian does not resolve a
  bare `[[alias]]`, and neither does Notekea: aliases count for unlinked mentions. `LinkMentionUseCase` writes
  `[[Note]]` (or `[[Note|words]]`) only if the words are still there (`ApplyLinkUpdatesUseCase`).
  `NoteMentionsViewModel` follows the open note and the tree (the file watcher). Renaming keeps updating links
  without a question (1.4).
  UI design A (picked 2026-11-03), following the macOS inspector: `ConnectionsSection` with the design system's
  `NotekeaCountTabs` and `NotekeaPassageRow` (rows, not cards; 2.4's Related moved to them), Note info as a compact
  table; a mention's click selects it in its note (`EditorIntent.OnRevealRequest`).

### ✅ 2.6 Hybrid search — `feature/hybrid-search` — [notekea#120](https://github.com/juanchurtado1991/notekea/pull/120)

`Mod+Shift+F` searches by meaning and keywords together (design A, chosen 2026-11-04): each note once, with the passage
that matched, its heading beside the folder, and a click or `Enter` that opens the note with that passage selected, in
Edit for that note, since Preview shows no selection, without changing the mode remembered; a note already open goes to
the passage.

- **Order:** the exact words of 1.6 show at once, and the hybrid answer replaces them: its passages first, then what
  only the exact words found. A note with every word always stays, and the row that was selected stays selected.
- **What counts:** the index always has a closest chunk, so one stays only if it holds every searched word or is close
  enough in meaning (`SearchPassagesUseCase`, measured on the Sample Vault).
- **`#tag`** limits the search to the notes that have it; a search of tags alone stays as in 1.6 and opens the note.
- **While the index is not ready,** or the model cannot answer, the exact words stay and a line below them says so.
- **Done when:** a note is found without its words, in another language too; a search with nothing close finds nothing;
  and search never stops working because of the index.

### ✅ 2.7 Graph view — `feature/graph-view` — [notekea#122](https://github.com/juanchurtado1991/notekea/pull/122)

A map of how notes connect, made to be understood at a glance by someone who is not technical: it opens small
(the current note and its neighbors), colors itself, and has no physics settings. Only notes are nodes; tags and
attachments never are, so the graph never turns into a hairball of everything in the vault.

**UI — Graph** (opened with `Mod+G` or from the sidebar, as an editor tab):
- **Design 1, "Finder inspector" (picked 2026-11-05):** a compact toolbar, a map of circles with their names below, a
  halo on the selected note, its details in the side panel's inspector, shown and hidden with `Space`, and a legend whose
  folders leave the map when clicked. 2D, with depth instead of 3D: notes farther from the center are smaller, fainter and move a
  little less, zooming reveals names, and a chosen note slides to the center, unless motion is reduced.
- **Local first:** opened from a note, the graph is local, on rings: that note and its direct neighbors (depth 1), with
  a 1 / 2 depth selector. The global graph is an explicit choice ("Whole vault"). With no note open the tab asks to find
  one ("No note selected").
  With more than 2,000 nodes the global graph suggests local mode.
- **Nodes and edges:** nodes sized by number of links; `[[links]]` as solid edges; semantic relations (above a
  similarity threshold) as dashed edges in `accent`, on the ring outside the last, labeled "Related by meaning" and
  toggleable, in local mode only.
- **Automatic colors:** nodes colored by top-level folder from the design system palette, with a legend, and no
  setup needed. Custom color groups by tag are a later option.
- **Focus on hover:** hovering a note highlights it, its edges and its neighbors and dims the rest.
- **Labels:** always shown in local mode; in global mode only once zoomed in enough to read them.
- **Filters:** exclude folders (for example templates or an archive), remembered per vault; orphans are hidden by
  default in global mode ("Show orphans").
- **Layout:** rings around a note, and force-directed for the whole vault, computed off the UI thread, with tuned
  defaults and no physics sliders.
- **Interaction:** pan and zoom; a click selects a note, and around a note moves it to the center; a double click or
  Open opens it. Keyboard: arrow keys move between connected nodes, `Enter` opens the focused note, `Esc` returns to the
  note.
- **Search:** a button and `Mod+F` open a bar that finds any note by name: matches keep their color and name, the rest
  fade, and `Enter` goes through them. Each brings its own graph with a move: the note reaches the center, its neighbors
  come out around it and the other notes fade, unless motion is reduced.
- **Link from the graph:** the inspector lists notes close in meaning that no link joins yet, each with Link, which adds
  `[[note]]` at the end of the selected note and can be undone there. A note edited meanwhile is never overwritten.
- **States:**
  - Empty vault, or a note with no links: a hint that teaches ("Link notes with `[[ ]]` to see them connected
    here").
  - Index not ready: only link edges, with a hint.
  - Too large: local mode suggestion.
- **Out of scope:** Canvas-style whiteboards (see "Out of scope for the MVP").
- **Done when:** opening the graph from a note shows a readable local graph without any setup, the global graph
  stays responsive on a 2,000-note vault, and every node can be reached and opened with the keyboard.

### ✅ 2.10 Vault queries — `feature/vault-queries` — [notekea#124](https://github.com/juanchurtado1991/notekea/pull/124)

What Obsidian users get from the Dataview plugin and Bases, without a query language to learn: lists and tables of
notes chosen by their properties, computed exactly from the index. The AI only writes the query, in 3.8; the result
never comes from a model.

- **Index:** every property of every note, with the types of 1.14 (text, number, checkbox, date, list), in the
  metadata cache next to tags, aliases and title, kept up to date by the file watcher.
- **Query:** filters (is, contains, more or less than, before or after a date, is empty, has a tag, in a folder),
  sort and the columns to show. No scripting.
- **UI — Query tab** (opened from the tool rail, as an editor tab):
  - Built with controls: Add filter (property, condition, value), Sort, Columns. The results are a table whose rows
    open their note and whose cells edit the property as the properties view does (1.14).
  - Saved inside a note as a fenced ```` ```query ```` block holding the query as YAML: Preview shows the live table,
    Edit shows the YAML, and Obsidian shows it as a code block, so nothing breaks there.
  - States: no note matches ("No notes match these filters"), a property no note has, a block that is not a valid
    query (explained above its YAML, like broken properties).
- **Done when:** on the Sample Vault, books by rating, projects by status and meetings by date list the same notes as
  a count by hand, the table follows a property edit within a second, and a note with a query block opens unchanged
  in Obsidian.

### ✅ 2.11 Vault tasks — `feature/vault-tasks` — [notekea#125](https://github.com/juanchurtado1991/notekea/pull/125)

Every task of the vault in one view, as Obsidian users get from the Tasks plugin, with no special syntax to learn.

- **Index:** every `- [ ]` and `- [x]` of every note (the `TaskFinder` of 1.7) with its note, line, heading and its
  date when it has one: a date written in the task (`2026-11-02`) or the Tasks plugin's `📅 2026-11-02`, so Obsidian
  users keep theirs.
- **UI — Tasks tab** (opened from the tool rail, as an editor tab): open tasks grouped by note, or by date (overdue,
  today, this week, later, no date); filters by folder, tag and done; checking a task writes it to its note as 1.7
  does; clicking one opens its note at its line.
- **Done when:** on the Sample Vault the tab lists every task of every note, checking one in the tab or in its note
  shows in the other at once, and a task changed outside Notekea updates within a second.

### ✅ 2.8 Retrieval evaluation set — `chore/rag-eval` — [notekea#126](https://github.com/juanchurtado1991/notekea/pull/126)

`smoke-data/eval/retrieval.json` (over the 1.16 sample vault): 50 questions over the sample vault with the expected
note and heading, plus 30 over the Ghost Serializer vault of 1.18, a code repository as notes: where something is
("where are field names matched?"), what to read to understand a part, and how a change would be made, each with the
notes that should be found. A test reports recall@5 and fails below the agreed baseline, so changes to chunking,
models or ranking are measured.

Done: the first measure is 0.94 on the Sample Vault and 0.90 on Ghost Serializer. The agreed baselines are 0.92 and 0.86,
one miss of margin per vault.

### ✅ 2.13 Packaged check — `chore/packaged-check` — [notekea#127](https://github.com/juanchurtado1991/notekea/pull/127)

With one QA run before the release, a problem only a packaged app shows (a Java module left out, a native library that
does not load, as in #68) must not wait for it. Every night `nightly.yml` packages Notekea on Windows, macOS and Linux,
installs it, starts it, checks that its window opens with no crash report, and uninstalls it.

- **Done when:** a package that cannot start fails the nightly on the system where it happens.

### ✅ 2.9 M2 performance pass — `chore/m2-performance` — [notekea#128](https://github.com/juanchurtado1991/notekea/pull/128)

The performance pass of M2 (see "Closing a milestone"), on the automated smoke runs of M1 and M2: memory and time of
the first indexing and of an incremental one on the Sample Vault and the Ghost Serializer vault, memory with the embedding model
loaded and after it is let go, search latency, and idle memory after indexing ends. Compared with 1.18 in
`docs/performance.md`; budgets of 5.4 (indexing or searching under 700 MB, back to the editor's level after).

Done: java-llama.cpp 4.2.0 never freed a model, about 275 MB lost on each reload, so the app moved to the maintained fork
`net.ladenthin:llama`, with the model on one thread of its own, two compiler threads, a collection when idle and, on
Linux, a native trim. Linux, Ghost Serializer: index peak 894 to 662 MB, idle 893–1172 to about 390 MB, recall unchanged.

---

## M3 — Chat with citations

Local models are small (about 2B, 4B or 8B parameters), so answer quality comes from **what reaches the model and
from checking the answer against the notes**, not from model size: retrieval is reranked, evidence is extracted
before answering, and every sentence of the answer must be backed by a quote that exists in a note. Parallel
generation (several llama.cpp slots sharing one loaded model) runs the independent steps at the same time. Models
critiquing or debating their own answers without evidence from the notes are not used: small models judge no
better than they answer, and each round adds waiting time. Every step is measured with the evaluation set (3.4)
and kept only if it improves citation accuracy within the latency budget (`ARCHITECTURE.md` §12).

**Hardware (decided with the developer, 2026-10-29): a dedicated GPU is always used when the machine has one, and
the CPU is the route for modest PCs.** Models load with their layers on the GPU (Metal on Apple Silicon; CUDA or
Vulkan on Windows and Linux, as far as the llama.cpp build allows, see 3.0), fall back to the CPU when there is no
GPU or not enough video memory, and nobody has to choose. On CPU only, parallel slots still save time (3.0 measured
it: writing is limited by memory bandwidth and a batch of slots reads the weights once), but every step is slower,
so the long jobs (Deep answer, the repositories' notes) are offered as background work instead of something to wait
for.

### ✅ 3.0 Parallel inference spike — `chore/llm-parallel-spike` — [notekea#129](https://github.com/juanchurtado1991/notekea/pull/129)

Before 3.2, measure with real 2B, 4B and 8B chat models on Linux (decided with the developer on 2026-11-07: only a
Linux machine was available; Windows and macOS are measured in 3.7 and the release run):
- java-llama.cpp `setParallel(n)` with continuous batching, called concurrently from coroutines: does each call get
  its own slot, and is the output identical to a single call with the same seed?
- RAM per extra slot (KV cache) and tokens/s with 1, 2 and 4 slots, on CPU only and with GPU offload.
- The GPU route: which GPU backends java-llama.cpp ships for each system (Metal on Apple Silicon; CUDA or Vulkan on
  Windows and Linux, or a build of our own if it ships none), how Notekea detects the GPU and its video memory, how
  many layers it offloads, and the fall back to the CPU when the GPU is missing, too small or fails.
- Time to the first token with 1k, 2k and 4k tokens of retrieved notes, on CPU and on GPU: on a CPU reading the
  prompt can take longer than writing the answer, so the latency budget counts both.
- The candidates are the open models current at the time, chosen for Spanish and English and for following a JSON
  grammar; no family is fixed in advance (Qwen 2.5 and Gemma were weighed on 2026-10-29). The 3.4 evaluation set and
  the Ghost Serializer questions of 2.8 pick the Small, Medium and Large defaults.
- `rerank()` with a small GGUF reranker (bge-reranker class): quality on the sample vault and time per 20 chunks.
- Grammar-constrained output (`jsonSchemaToGrammar`): valid JSON every time, cost in tokens/s.
- Plans and tools per model size: how well each model picks from about 30 actions, in one step or by kind first and
  action second, whether it can plan up to 5 steps at once (3.9), and whether it can choose its next step from what
  the last one found (4.7): the model sizes that can use each one.

- Memory of the whole process (editor included) with each model and a context of 2k, 4k and 8k tokens, and with the
  KV cache in 8 bits instead of 16: what it saves and what it costs in quality on the 3.4 evaluation set.
- Whether a 1–2B model with the pipeline of `ARCHITECTURE.md` §12 reaches the minimum quality of 3.4: if it does, it
  is the default model, so chatting fits the memory budget of 5.4 on an 8 GB laptop.

Result: `docs/spikes/<date>-llm-parallel.md` with the numbers, the default slot count per RAM size (likely one on
8 GB, since every slot adds its own KV cache), the context size and the latency budget used by 3.5 and 3.6.

Done (`docs/spikes/2026-11-07-llm-parallel.md`, preliminary until 3.4): Small Qwen3.5-2B, Medium Qwen3.5-4B, Large
Qwen3.5-9B, reranker bge-reranker-v2-m3, whose score also turns away questions the notes cannot answer. GPU by
Vulkan (needs only the driver) and Metal; no mmap on the CPU (24–31 % less memory). Slots: 1 on 8 GB, 2 on 16 GB, 4
on a GPU; context 4k a slot; a whole answer in 4 s on GPU and 12 s on CPU (median). Actions in one step, plans from
Medium up. On an approximated 8 GB laptop chatting with the Small model goes over 2.5 GB; 3.2 and 3.7 fit it.

### ✅ 3.1 Model manager — `feature/model-manager` — [notekea#132](https://github.com/juanchurtado1991/notekea/pull/132)

Catalog of tested chat models (JSON parsed with Ghost): **Small (~2B)**, **Medium (~4B)** and **Large (~8B)** GGUF,
plus the reranker model (3.0), which is downloaded with whichever chat model is chosen. Download with Ktor
(progress, pause, resume), SHA-256 verification, delete. Hardware check: warns when RAM is below the model's
requirement, including the KV cache of its parallel slots (3.0) and the editor itself. The model is recommended from
the machine's RAM and, when it has a dedicated GPU, its video memory (Small on 8 GB, Medium on 16 GB, a larger size
when the GPU holds it), with nothing technical to choose. The models and sizes are those of 3.0.

**UI — Model manager** (Settings → AI model; design C, chosen by the developer on 2026-11-08):

```
┌─────────────────────────────────────────────────────────┐
│ AI model                                                 │
│ Chat answers from your notes with a model that runs on   │
│ this computer. Medium fits it, with 16 GB of memory.     │
│ ┌──────────────────────────────────────────────────────┐ │
│ │ Medium · Qwen3.5 4B  [Recommended]                   │ │
│ │ Good answers on most computers.                      │ │
│ │ ▓▓▓▓▓▓▓▓▓▓▓░░░░░░░ 1.9 GB of 3.2 GB  [Pause] Cancel  │ │
│ └──────────────────────────────────────────────────────┘ │
│ › OTHER SIZES                                         2  │
│ Models are kept in ~/.local/share/notekea/models         │
└─────────────────────────────────────────────────────────┘
```

- States: not downloaded, downloading (pause/resume/cancel), verifying, in use or downloaded (use, delete with a
  question), failed (network, checksum, disk space or folder) with Try again; while one size installs, the others
  wait.
- **Decided while building it (2026-11-08):** the catalog ships with the app (`files/model-catalog.json`), each file
  pinned to a Hugging Face commit and checked by its SHA-256 (a mirror of our own comes later if a link ever moves).
  A file keeps its name only once verified (`.part` until then), so a paused or dropped download resumes with a
  `Range`. The installer outlives Settings and the window; a model that finishes installing becomes the one in use.
  Each model names the memory it needs (8 / 16 / 32 GB on the CPU, or 2 / 4 / 8 GB of video memory with 8 / 8 / 16
  GB of RAM), and the largest that fits is recommended. Video memory is read from `nvidia-smi` and, on Linux, AMD's
  `/sys/class/drm`; other GPUs are recommended by RAM until 3.7 measures them. The status bar entry moves to the
  chat panel (3.3), which links to Settings → AI model when no model is in use.

### ✅ 3.2 Chat engine — `feature/m3-chat` — [notekea#139](https://github.com/juanchurtado1991/notekea/pull/139)

`LlmEngine.generate` with java-llama.cpp, streaming tokens as a `Flow`, cancellable. The engine loads the model once
with the slot count chosen by 3.0 and accepts concurrent generations (a `Semaphore` caps them at the slot count);
structured steps use grammar-constrained JSON output. Prompts are built with
`LlamaModel.applyTemplate` (passing messages with an empty prompt returns nothing), and integration tests exit the
process explicitly because llama.cpp keeps native threads alive (spike of 2026-10-23). RAG pipeline: retrieve top
chunks (hybrid index), build a prompt that requires citing chunk ids, parse citations from the answer, drop
citations that do not match a retrieved chunk.

- **Memory:** the model is loaded when the chat opens and unloaded after a few minutes without use (5 by default,
  in Settings), so writing notes never pays for it; on the CPU its weights are read without mmap (with mmap the
  CPU keeps both the mapped file and its own copy, 3.0), never locked in memory, and the context size is the one
  3.0 measured, not the model's maximum (budgets in 5.4).

### ✅ 3.3 Chat panel — `feature/m3-chat` — [notekea#139](https://github.com/juanchurtado1991/notekea/pull/139)

**UI — Right panel, Chat mode** (its "Coming soon" screen and the Inspector | Chat control exist since 1.14)**:**
- Message list; the answer streams token by token with a "Stop" button (`Esc`).
- Citations rendered as `accent` chips `[1]`, `[2]`; hover shows the snippet, click opens the note scrolled to
  and highlighting the cited paragraph.
- "Sources" list under each answer with note titles.
- With the Small model (8 GB machines), the sources come first, above the answer, with one line saying answers
  from the small model should be checked against them (decided with the developer, 2026-11-08: Small is right about
  half the time and wrong one time in eight, `docs/spikes/2026-11-08-answer-quality.md`).
- Scope selector above the input: **Whole vault** / **This note** / **This folder**.
- Input: multiline, `Enter` sends, `Shift+Enter` new line; `Mod+L` focuses the chat from anywhere.
- When nothing relevant is found the assistant says so instead of guessing ("I couldn't find this in your notes").
- While an answer is prepared, one line shows the current step in plain language ("Searching your notes…",
  "Reading 6 notes…", "Checking sources…"), never a bare spinner.
- **Deep answer** toggle next to the input (3.6), off by default, with a one-line hint: "Takes longer, checks more
  drafts against your notes."
- States: model not downloaded (inline card with "Download model", which opens Settings → AI model, 3.1), index not
  ready, generating, error with retry. While a model downloads, the status bar shows its progress and opens Settings
  → AI model (moved here from 3.1, whose Settings section runs the download).
- Chat history per vault, stored as JSON (Ghost) in the app data directory, with "Clear history".
- **Decided while building it (2026-11-10):** design A · Conversation (the question in a bubble on the right, the
  answer on the left with its chips in the text). The step line has no "Checking sources…": citations are checked
  in code, at once. The card without a model says "Choose a model", since Settings → AI model may also offer a
  model already downloaded. The history keeps the last 100 questions of each vault, and each question is answered
  alone (a follow-up does not see the earlier ones yet).

### ✅ 3.4 Answer evaluation — `feature/m3-chat` — [notekea#139](https://github.com/juanchurtado1991/notekea/pull/139)

Extends the eval set with expected facts per question; a manual-run task reports citation accuracy
(cited paragraph actually contains the fact) for the active model. It also reports latency per step, so 3.5
and 3.6 compare quality and speed for each model size.

Spike (`docs/spikes/2026-11-08-answer-quality.md`, 2026-11-08): expected facts agree with a manual grade 0.90–0.97 of
the time and a 9B judge 0.76–0.88, so the facts are the measure and a judge only a second opinion; the spike's facts
and trap questions start the set, which also needs harder questions (two notes, dates, comparisons).
Tournament (`docs/spikes/2026-11-08-model-tournament.md`): on 504 questions, Qwen3.5 2B, 4B and 9B beat SmolLM3,
Ministral 3B, Phi-4-mini and Gemma 4 E2B and E4B at every size, so the 3.0 defaults stay. Plain facts can be fooled
by a model that pastes its chunks, so 3.4 grades strictly (facts, the question's language, no pasted excerpt); the
504 questions start its set.

### ✅ 3.5 Answer pipeline — `feature/m3-chat` — [notekea#139](https://github.com/juanchurtado1991/notekea/pull/139)

Replaces the single "retrieve and answer" call of 3.2 with steps that make small models precise
(`ARCHITECTURE.md` §12):
1. **Rewrite:** the question becomes 2–3 search queries (grammar-constrained JSON), searched in parallel and merged.
2. **Rerank:** the reranker keeps the most relevant chunks for the question.
3. **Extract evidence (parallel):** one slot per chunk copies the sentences that answer the question, or "none";
   irrelevant chunks never reach the answer.
4. **Answer:** written only from the extracted quotes, citing each one.
5. **Verify (parallel):** every cited quote must exist word for word in its note (deterministic check), and the
   model confirms, sentence by sentence, that the quote supports it. Unsupported sentences are removed; when nothing
   is left the assistant says "I couldn't find this in your notes".

Each step sits behind an option so 3.4 can measure it alone and in combination.

Measured before it is built (`docs/spikes/2026-11-08-answer-quality.md`): on every model, one call over the five
reranked chunks (cited, `NOT_FOUND` when they do not answer) beat extraction and removing unsupported sentences, and
was four to eight times faster; extraction loses answers in tables and lists. So 3.2's single call is the baseline
to beat, and a step is kept only if 3.4 shows it helps. The reranker gate is −8, not −5: the models decline on
their own and −5 turned away answerable questions.
- **Done when:** on the evaluation set, citation accuracy beats 3.2 for Small, Medium and Large, and the median
  answer time stays within the budget set by 3.0.
- **Measured (2026-11-10):** no step beats 3.2's single call (`docs/answer-quality.md`, 3.5): giving each excerpt
  its note and headings left Medium at 389 right against 388, extraction and checking lost in the spike, and a
  rewrite could win at most the 2 % of answerable questions declined. None is built; the single call is the pipeline.

### ✅ 3.6 Deep answer mode — `feature/m3-chat` — [notekea#139](https://github.com/juanchurtado1991/notekea/pull/139)

An opt-in mode for when precision matters more than speed. Several drafts are written in parallel (different
seeds, same evidence), each goes through the verification of 3.5, and the draft with the most supported sentences
wins; ties go to the shorter draft. It is chosen by verified support, not by majority vote.
- **UI:** the **Deep answer** toggle of the chat panel (3.3); the step line shows "Writing 3 drafts…" and
  "Comparing drafts…"; Stop (`Esc`) keeps the best verified draft so far.
- Draft count follows the slot count measured in 3.0 for the machine's RAM; on CPU only, where drafts run one after
  another, Deep answer says how long it will take and can keep working in the background.
- **Done when:** on the evaluation set it beats the normal mode's citation accuracy for every model size, and it is
  cancellable at any moment.

Measured before it is built (`docs/spikes/2026-11-08-answer-quality.md`, "Parallel calls and judges"): choosing the
draft by support did not beat one answer on any model; a majority of 3 drafts helped Medium (4 more right answers,
none wrong) and not Small or Large, and a model judge did not help. 3.6 starts from the majority vote and offers
the mode only for the sizes the 3.4 set shows it helps.
- **Measured (2026-11-10):** three drafts and a vote (`DraftVote`): Medium 91 % right against 89 %, Small 79 against
  80, Large 85 against 85; Deep answer is offered with Medium only. The drafts are written at once, so Stop ends them
  all and keeps nothing; there is no "best draft so far" to keep.

### ✅ 3.8 Questions in plain words — `feature/m3-chat` — [notekea#139](https://github.com/juanchurtado1991/notekea/pull/139)

Notes and tasks asked for in plain words ("books I rated 4 or more in 2026", "what is due this week in Garden?") and
answered exactly by the queries of 2.10 and 2.11: the model only turns the question into a query.

- **Translate:** the local model writes the query as grammar-constrained JSON (3.0) from the question and the vault's
  property names and types; the query is checked before it runs, and a property the vault does not have makes it ask
  which one was meant.
- **UI:**
  - In the query tab, a "Describe what you want" field fills the controls, so the user sees the query, and can
    correct it, before trusting the table.
  - In the chat (3.3), a question about properties or tasks is answered with the table or the task list, each row
    citing its note (and its line for a task), instead of a written paragraph.
- **Evaluation:** 30 questions over the Sample Vault with the notes or tasks they should return, in the set of 3.4,
  reported per model size.
- **Done when:** the Small model returns the expected notes for most of them (the threshold set in 3.4), and a wrong
  translation always shows as a query the user can see and correct, never as a silently wrong answer.
- **Decided while building it (2026-11-10):** the chat and the actions share one planner (3.9): a request about notes
  by their properties is a `find_notes` step, translated by the query feature's `TranslateQueryUseCase`, the same
  that fills the Query tab from "Describe what you want"; tasks are a `show_tasks` step. The 30 questions are part of
  the 106 requests of `smoke-data/eval/actions.json`.

### ✅ 3.9 Actions from chat — `feature/m3-chat` — [notekea#139](https://github.com/juanchurtado1991/notekea/pull/139)

The chat is a way to use Notekea, not only to ask about notes (decided with the developer, 2026-10-29, widened on
2026-11-04): it can do almost anything a person can do with their notes. A request becomes **a plan of up to 5 steps**,
written at once as JSON constrained by a grammar (3.0), from a fixed list of actions, or an answer (3.5). Notekea
checks every step before doing anything (the note and the folder exist, the property name is valid) and carries it
out with the use cases the app already has; the model only chooses. Asking first for the kind of action and then for
the action is used if 3.0 shows it picks better. A step that needs text writes it with 3.5, so a summary can be added
to a note in the same plan. Work that must decide as it goes belongs to agent mode (4.7).

- **Done at once, nothing changes in the notes:**
  - Read and find: open a note at a quote or a heading, search by text, tag or property (2.10), show tasks (2.11),
    open today's note, show related notes and backlinks, list notes without tags and broken links.
  - The app: open a panel, split the pane, close tabs, switch between Edit and Preview, change the color theme.
- **Written, always confirmed first:** create a note (title, folder, text, optionally from a template), add text to a
  note or to today's note, edit a section or rewrite a note (shown as a difference), set or change a property, add a
  tag, add or complete a task, link two notes, bookmark a note or folder, make a new note from the open one (its
  summary, its tasks), clip a web page (4.4).
- **Organizing, always confirmed first, listing every note:** create a folder, rename and move notes and folders (links
  follow, without asking, as in 1.4), send to the system's trash.
- **Several notes:** a step can cover up to 50 notes ("tag every note in Recipes"); the card lists them.
- **Never:** deleting for good, anything outside the open vault, Settings that hold credentials, the remote or the
  models, and writing code. Asked for one of these, the chat says in plain words what it can do instead.
- **Safety:** text inside notes or pages can look like an instruction ("delete all notes"). The step that chooses
  actions reads only the request and the names of notes and folders, never their text; a URL is only followed if the
  user wrote it (`GUIDELINES.md` §13); writing is always confirmed, on one card for the whole plan, which is never
  confirmed by itself. Every action done is listed in the chat, and the plan can be undone at once, a trashed note
  included where the system allows it (checked on the three systems in this item; where it does not, the card says so).

**UI — Action card** (in the chat panel of 3.3):

```
You:  create a note "Meeting with Ana" in Work from the Meeting template, status draft

┌ Notekea will: ──────────────────────────────────┐
│  ＋ Create note   Work/Meeting with Ana          │
│     from the template Meeting                    │
│  ✎ Property      status: draft                   │
│                         [ Do it ]  [ Cancel ]    │
└──────────────────────────────────────────────────┘
→ "Done" with Undo (Mod+Z), and the note opens
```

- An evaluation set of about 100 requests with the expected plan and its fields (`create_task(note, text)`), run on
  Small, Medium and Large like 3.4.
- **Done when:** the recommended model writes the right plan with the right fields for at least 90% of the set;
  where a model size falls short, its writing actions are offered as suggestions the user completes instead.
- **Decided while building it (2026-11-10):** one call plans the request as JSON under a grammar (`ActionPrompt`,
  with examples), and code fixes what small models do on their way (a question is only answered, a plan that writes
  does not first open what it names, a folder or a template written as a note is moved to its field). The text to
  write is written before the card, so the card shows it: 120 words plus 20 per note read, at most 600; notes too long
  to read at once are read as digests first; written notes use the formatting toolbar's Markdown and keep only links
  to notes that exist. A pasted text becomes a folder of up to 15 notes with an index note (asked by the developer).
  Plans: Small 75 %, Medium 92 %, Large 91 % of 106 requests; every change still waits for the card, which is the
  suggestion Small's plans need. The end to end run of 13 requests on the Sample Vault (`ActionRunChatModelTest`)
  passes with Medium, Undo included. Not built: rewriting a note as a difference, and clipping a web page (4.4).

### ✅ 3.7 M3 performance pass — `feature/m3-chat` — [notekea#139](https://github.com/juanchurtado1991/notekea/pull/139)

The performance pass of M3 (see "Closing a milestone"), on the automated smoke runs of M1 to M3: memory while a model
loads, chats and answers deeply on an 8 GB machine, how fast it returns to the editor's level after the chat is
closed, time to the first token, and idle memory with the chat open but unused. Compared with 2.9 in
`docs/performance.md`; budgets of 5.4 (chatting with the Small model under 2.5 GB).

---

## M4 — Git sync

### ⬜ 4.1 Git sync engine — `feature/git-sync`

`GitSync` with JGit: connect (clone or init + remote), auto-commit after 10 s of inactivity, `pull --rebase` on
open and every 5 minutes, push after commit. Conflicts keep both versions (`note.md` and `note (conflict).md`).
The index folder is never committed. Token stored in the OS keychain (Java Keyring).

### ⬜ 4.2 Sync UI — `feature/sync-ui`

- **Settings → Sync:** remote URL, authentication (an HTTPS access token; SSH keys after the MVP, decided with the
  developer on 2026-11-11), auto-sync on/off, interval, "Sync now".
- **Status bar:** "Up to date", "Syncing…", "3 changes pending", "Conflict" (`error` color).
- **Conflicts:** banner listing conflicted notes; opening one shows both versions side by side with
  "Keep mine", "Keep theirs" and "Keep both".
- States: not configured (status item hidden), offline (retries silently, shows "Offline"), auth failed
  (opens Settings → Sync with the error).

### ⬜ 4.3 M4 performance pass — `chore/m4-performance`

The performance pass of M4 (see "Closing a milestone"), on the automated smoke runs of M1 to M4: memory and time of
a sync of the Sample Vault and the Ghost Serializer vault, of the auto-commit while typing, and idle memory with sync on.
Compared with 3.7 in `docs/performance.md`.

---

## M4b — Sources, study cards and agent

Added to the MVP with the developer on 2026-11-04. It comes after M4 so Git sync is done, and before M5 so the
performance work of 5.4 and the release run cover all of it. Repositories stays after the MVP.

### ⬜ 4.4 Web clips — `feature/web-clips`

Notes from a web page: give Notekea a URL and it writes notes about the article into a folder.

- **Flow:** the URL is downloaded (`GUIDELINES.md` §13), the article is taken out of the page (a library to approve in
  `LIBRARIES.md`), the local model writes the notes with 3.5, each one checked against the article, and Notekea creates
  `Web sources/<title>.md` with the properties `source` and `clipped`, the notes, and the article's text under
  `## Original text`, so search and the chat keep working if the page disappears.
- **Where:** "Clip a web page" in the start actions shown where no note is open, and in the chat as an action of 3.9
  ("clip https://…"). Not in the editor's More menu: the start actions keep the editor's toolbar as it is.
- **With no model installed,** it saves the article alone and says the notes need a model.
- **What fails, in plain words:** a page that needs JavaScript or a login, a paywall, a page that is not an article. No
  browser is built into the app.
- **Done when:** the articles of an evaluation set of 20 saved pages make notes whose every sentence is backed by the
  article, and a page that cannot be read leaves no note behind.

### ⬜ 4.5 Documents — `feature/document-sources`

Documents inside the vault are indexed like notes, whatever their format (decided with the developer, 2026-11-04):
search, Related and the chat find them. Notekea reads them and never edits them, and they do not become notes.

- **Plain text:** `.txt`, `.rst`, `.org`, `.csv`, `.json` and `.html`, with almost no new code.
- **Office and books:** PDF, Word (`.docx`), LibreOffice (`.odt`), EPUB, PowerPoint (`.pptx`) and Excel (`.xlsx`). The item
  starts by measuring what Apache Tika adds to the app's size against one small library per format; the choice goes in
  `LIBRARIES.md`.
- **One reader per format,** so another format is cheap to add later.
- **Citations:** a click opens the file in a read-only tab (decided with the developer, 2026-11-11): a PDF as its real
  pages, drawn with PDFBox, at the cited page with the passage marked; the other formats as their text. "Open in the
  system's app" is always there. Apache Tika reads every format (`LIBRARIES.md` §5b).
- **Left out:** scanned PDFs and images with no text, listed as not readable (OCR comes after the MVP), other binary
  files, and source code, which Repositories indexes properly.
- **Done when:** a sentence of each format can be found and cited, a change to a file indexes it again, and a damaged
  file is only skipped and reported.

### ⬜ 4.6 Study cards — `feature/study-cards`

Flashcards made by the local model from a note: each question cites the sentence it comes from and passes the
verification of 3.5; reviewed with spaced repetition in a review tab, and stored in the note in the format of Obsidian's
Spaced Repetition plugin, so they work in both. Chosen with the developer on 2026-10-28, when the answer pipeline is
measured.

### ⬜ 4.7 Agent mode — `feature/agent-mode`

Work that must decide as it goes (decided with the developer, 2026-11-04): "read these 10 articles, compare them and
reorganize my notes about it". Local models are small and choosing tools over several steps is the hardest thing asked
of them, so it is built to be safe when they get it wrong, and 3.0 and 3.4 decide which model sizes offer it.

- **The loop:** the agent chooses one step at a time, from the read-only actions of 3.9 and the URLs in the request,
  as JSON constrained by a grammar, and sees what each one found. Notekea stops it at 30 steps, at a time limit, or on
  Stop, and it runs in the background with its progress in the chat.
- **Nothing is written while it works:** every change goes into a plan, shown at the end on the card of 3.9, with all
  its notes listed, and nothing happens until the user confirms it. Undoing it undoes the whole plan.
- **Safety:** here the agent does read notes and pages, so their text can steer what it proposes. It cannot fetch a URL
  the user did not write, cannot leave the vault, and every change still passes the user's confirmation.
- **Tools:** plain coroutines like the rest of the pipeline: search, read a note, find notes by their properties, the
  open tasks, and fetch a page whose address the request writes.
- **Evaluation:** 30 tasks over the Sample Vault with the plan or answer each should reach (`AgentEvalChatModelTest`);
  a size offers agent mode at 80 % right.
- **Done when:** the model sizes that offer it reach the threshold set in 3.4 on those tasks, and a failed run ends with
  no change to the vault. A model size that falls short does not offer it; the rest of the chat is unaffected.

### ⬜ 4.8 M4b performance pass — `chore/m4b-performance`

The performance pass of M4b (see "Closing a milestone"): memory and time of a web clip, of indexing large documents and of a
long agent run. Compared with 4.3 in `docs/performance.md`.

---

## M5 — Release

### ⬜ 5.1 Onboarding — `feature/onboarding`

First run flow, three steps, skippable at any point:

1. **Welcome:** what Notekea does in one sentence and the privacy promise ("Your notes never leave this computer").
2. **Choose your notes:** open folder / create vault / use the sample vault.
3. **Get the AI:** recommended model for this machine's RAM, download in the background while indexing starts.

### ⬜ 5.2 Settings — `feature/settings`

`Mod+,` opens Settings (the dialog the tool rail's Settings opens since 1.14, with its sections marked "Coming soon")
with sections: General (language, open last vault), Appearance (mode and color theme, built in 2.12), Editor (font size, line width,
spell check), Files (update links when renaming: always or ask, default always; attachments folder, daily notes folder and date format, templates folder — prefilled from
`.obsidian/` when present), AI (model manager, chat scope default), Sync (M4), Shortcuts (read-only list),
About (version, licenses, privacy statement).

### ⬜ 5.3 Packaging — `chore/release-pipeline`

GitHub Actions on tag `v*`: build `.dmg` (macOS), `.msi` (Windows) and `.deb` (Linux), attach them to a GitHub
Release with checksums.

- **Unsigned builds:** the MVP is not code-signed, to keep it free of paid certificates (an Apple Developer account
  and a Windows code-signing certificate cost about 300 USD a year together). Linux needs no signature.
- **First launch:** the README has a "First launch" section per OS, with what the system shows and how to open the
  app anyway: Windows SmartScreen ("More info" → "Run anyway") and macOS Gatekeeper (System Settings → Privacy &
  Security → "Open Anyway"), as the QA guides (`qa/GUIDE_*.md`) explain it.
- **Later, when there are users to justify it:** free signing for open source projects (SignPath Foundation) for
  Windows, and an Apple Developer ID with notarization for macOS. Neither blocks the release.

### ⬜ 5.4 Performance and memory — `chore/performance`

M5's performance pass, the last one (see "Closing a milestone"). Notekea should run well on the laptop a
non-technical person has (8 GB of RAM). With the whole app built, measure it on the reference machines (Windows,
macOS, Linux) with every automated smoke run and cut what the numbers point at, starting from the passes of 1.18,
2.9, 3.7 and 4.3.

- **Memory budgets** (whole process, the Sample Vault and a 1,000-note vault):

  | Situation | Budget |
  |---|---|
  | Editor only | < 400 MB |
  | Indexing or searching (embedding model loaded) | < 700 MB |
  | Chatting with the Small model on an 8 GB laptop | < 2.5 GB |
  | Chat closed for a few minutes | back to the editor-only level |

- **Measure:** profile the editor with the Ghost Serializer vault (`smoke-data/vaults/Ghost Serializer`), the indexing and a chat session; record
  memory, startup time and typing latency per OS in `docs/performance.md`.
- **JVM:** a heap limit and a garbage collector suited to a desktop app, and a runtime trimmed with jlink to the
  modules the app uses (5.3 packages it).
- **Cuts the measurements point at:** caches that grow without bound, note texts held twice, the embedding model
  kept loaded after indexing ends.
- **Preview of a long note** (found in 1.18, decided with the developer for this pass): Read mode composes every
  section of the note at once (one Markdown renderer per heading, in a `Column` inside the page's scroll), so a
  2,000-line note reaches 761 MB. Its sections become the items of a `LazyColumn`, with the title, properties and
  toolbar as its first items: the sticky toolbar follows the list's state, the outline scrolls to a section by its
  index and highlights from the first visible item, and each tab remembers the list's position. Embedded notes keep
  their `Column`. Measured with the Personal Notes memory run, before and after.
- **Done when:** the release run (5.5) meets every budget on Windows, macOS and Linux.

### ⬜ 5.5 Release v0.1.0

The release run (`qa/README.md`) comes first, on the three systems, and closes M2 to M5. Then: website `notekea.app`
with download links, README screenshots, announcement (Hacker News, r/ObsidianMD, r/LocalLLaMA, Kotlin Slack). Measure: stars in the first month, issues from real users, retention feedback.

---

## Post-MVP (ordered by expected value)

1. **Extensions via MCP** — Koog agent runtime with `ai.koog:agents-mcp` once stable; extensions are MCP servers
   the user enables explicitly.
2. **Mobile (Android first, then iOS)** — `LlmEngine` via Llamatik, `VectorIndex` via SQLite + sqlite-vec,
   `GitSync` via libgit2 on iOS (see "Mobile risks" in `LIBRARIES.md`).
3. **Repositories** — a code repository becomes something the chat can answer about, with the exact file and line.
   Separate from Git sync (4.1), which only versions the vault itself; it reuses its JGit clone and pull.
   **Decided with the developer (2026-10-29), replacing the plan of 2026-10-28 that wrote notes about the code:**
   Notekea writes no notes for a repository. It clones it as it is into a read-only folder of the app data (never
   inside the vault), indexes its code, and the chat and its actions (3.9) answer about it:
   1. **Clone and update:** add a repository by its URL or folder, "Update" pulls it; after a pull only the changed
      files are indexed again. Its Markdown (README, `docs/`, ADRs, CHANGELOG) is indexed like notes.
   2. **Code index from a parser (AST):** a parser (for example tree-sitter, to be approved in `LIBRARIES.md`) reads
      every source file into its symbols (classes, interfaces, functions, properties) with their exact line ranges,
      and the relations between them (imports, extends, implements, injects, calls). Code is chunked by function or
      class for the hybrid index (with a code embedding model, if 2.8-style evaluation shows it helps). The index and
      the graph of classes and modules are exact and repeatable, take seconds to minutes, and involve no model.
      tree-sitter's Java bindings use the foreign-function API of Java 22 and Notekea runs on 21, so the item starts
      with a spike: those bindings after a JDK update, a native build, or Kotlin's own parser for Kotlin code.
   3. **Questions about the code:** "which line opens the database?", "what calls `syncCache`?", "where is the
      network error handled?", "which classes should I read to understand the parser?". Answers cite `file:line`
      and are verified against the code itself (3.5); a click opens the file in a read-only code tab, scrolled to
      the line and highlighted. The actions of 3.9 gain read-only code actions: find a symbol, open a file at a
      line, list what calls or implements something.
   4. **No code is written:** Notekea reads code, it does not edit it. When asked to create a class, change a
      function or fix a bug, the chat answers in plain words, for example: "Notekea reads your code to answer
      questions about it, but it doesn't write code. For that, try a coding assistant such as Claude Code or
      Codex." The request is recognized by the action step of 3.9 (an action that is not in its list), not by the
      model improvising.
   The Ghost Serializer vault (`smoke-data/vaults/Ghost Serializer`) stays as the large vault of the tests and the
   performance passes, and its questions (2.8) as an evaluation set; the repository itself becomes the test data of
   this item.
4. **One editing mode** — a note looks finished and is edited right there, so Edit and Preview become one. Decided with
   the developer on 2026-11-05, as a feature of its own. It is built in steps: first Edit hides the Markdown marks except
   on the cursor's line, then tables, embeds and images show and are edited in place. Preview stays until the new mode
   matches it on long notes, within the memory targets of 1.18 and 5.4, and the raw Markdown stays one action away for
   what does not render well. Design options are shown first.
5. **More than two split panes** and detachable windows.
6. **Custom title bar** with the tabs inside it (JetBrains Jewel `DecoratedWindow`).
7. **Screenshot tests** for UI regressions (Roborazzi).
8. **Web search for agent mode** — the agent finds its own sources instead of using the URLs it is given. Decided with
   the developer on 2026-11-04 to come after the MVP. It needs an outside search service, so searches leave the computer
   and the privacy promise changes: off by default, each search shown and approved first, with the user's own provider,
   its own issue and an update to `GUIDELINES.md` §13.
9. **OCR** — text from scanned PDFs and pictures, on the computer. Tesseract first, as an optional download.
10. **Use an installed Ollama** — when the user already runs Ollama, Notekea can use its model instead of loading its
   own, so the memory is not spent twice. For technical users; the bundled model stays the default.

## Out of scope for the MVP

Cloud models (OpenAI, Anthropic…), accounts, real-time collaboration, a plugin API of our own, mobile apps,
importers for other note apps (Notion, Evernote), Canvas-style whiteboards, automatic link insertion.
