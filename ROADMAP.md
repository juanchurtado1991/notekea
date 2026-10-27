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

**Closing a milestone:** a milestone is done when all its features are ✅ **and** its QA run passes on Windows,
macOS and Linux (`qa/README.md`); only then does the next milestone start. The M1 run also covers M0, which never
had one. The M5 run is the release run.

**Status tracking:** features are never removed from this file. Each feature title carries its status —
⬜ not started, 🚧 in progress (branch open), ✅ done (with its pull request) — and the table below counts them.
`CHANGELOG.md` records what changed; this file records what is planned and what is left.

| Milestone | Goal | Estimate | Status |
|---|---|---|---|
| M0 — Foundation | Project setup, tooling, guidelines | Done | ✅ |
| M1 — Vault and editor | Open a folder, browse, read and edit notes with Obsidian-level essentials | 3.5 weeks | 14 / 18 |
| M2 — Indexing, links and related notes | Local embeddings, hybrid search, related notes, backlinks, graph | 2.5 weeks | 0 / 8 |
| M3 — Chat with citations | Local LLM, model download, RAG chat with citations, verified answers, deep answer mode | 3 weeks | 0 / 7 |
| M4 — Git sync | Optional commit / pull / push of the vault | 1 week | 0 / 2 |
| M5 — Release | Onboarding polish, performance and memory, packaging, first public release | 1.5 weeks | 0 / 5 |
| Post-MVP | Extensions (Koog + MCP), mobile, more | — |

---

## UI overview

### Layout

One main window with three resizable panes, a status bar, and the native window frame and menu bar
(`PLATFORM_GUIDELINES.md`).

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│ (native title bar / menu bar)                                                    │
├──────────────┬──────────────────────────────────────────┬────────────────────────┤
│ SIDEBAR      │ [Pricing ✕] [2026-10-23 ✕] [+]          │ RIGHT PANEL            │
│ 🔍 Search    │──────────────────────────────────────────│ [Inspector][Chat]      │
│ 📅 Today     │ Pricing ideas                 Edit|Read  │ ▾ RELATED              │
│              │ tags: product · status: draft  (props)   │                        │
│ ★ Bookmarks  │ ─────────────────────────────────────    │ Related notes          │
│   Roadmap    │ > [!tip] Start simple                    │ ┌────────────────────┐ │
│              │ - [x] Compare competitors                │ │ Meeting w/ Ana     │ │
│ ▾ Projects   │ - [ ] Decide price                       │ │ "…start at 9€"     │ │
│   Notekea    │ ![[chart.png]]                           │ │ [Insert link]      │ │
│ ▾ Journal    │                                          │ └────────────────────┘ │
│   2026-10-23 │                                          │                        │
├──────────────┴──────────────────────────────────────────┴────────────────────────┤
│ STATUS BAR   ● Indexed 1,204 notes   ● Model: ready   ● Sync: up to date          │
└──────────────────────────────────────────────────────────────────────────────────┘
```

- **Sidebar** (default 340 dp, as wide as the outline panel, min 200, collapsible with `Mod+\`): the vault's name with Sort, Expand all, Collapse
  all and Minimize, bookmarks, folder tree. A tool rail on its left (Files, Search notes, Today, Side panel; Open another vault, About and Settings at its bottom; 1.14)
  stays when it is hidden (1.11).
- **Editor** (flexible, min 480 dp): tab bar, title, properties, Edit / Read toggle, Markdown content.
  Can be split into two panes side by side.
- **Right panel** (default 340 dp, min 280, collapsible with `Mod+Shift+\`): an **Inspector | Chat** control at its
  top (remembered). The inspector has sections that fold on their own (remembered): **Outline** and **Note info**
  (1.14), then **Related** and **Backlinks** (M2). **Chat** (3.3) fills the panel, since a conversation needs its full
  height; until it is built it says "Coming soon" (1.14).
- **Status bar** (24 dp): indexing progress, model state and sync state. Each item is clickable and opens its
  detail (indexing log, model manager, sync panel).
- Below 1000 dp of window width the right panel becomes an overlay; below 760 dp the sidebar does too.

`Mod` = Cmd on macOS, Ctrl on Windows and Linux.

### Design tokens

Notekea has its own visual identity, identical on every platform: a vivid **cobalt blue** for actions and selection
and a **tangerine** accent, its complementary color, that keeps a nod to the kea's orange underwings. All values live
in the theme; composables never hard-code colors. Typefaces approved on 2026-10-23; the palette was chosen the same
day after two earlier versions (saturated olive and orange, then sage and terracotta) were judged too dull.

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
- **Motion:** 150–200 ms for panel open/close; no animation longer than 250 ms; respect "reduce motion".
- **Themes:** light and dark, following the system by default.
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
- Implemented in `core.presentation.theme` (internal `palette` + semantic `NotekeaColorSchemes`, typography,
  dimensions) and `core.presentation.components`; a test gates WCAG AA contrast for every text pair. The gallery
  opens with `./gradlew :composeApp:run -Pgallery`.

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

- States: no recents (only the two buttons), folder missing (recent item greyed with "Locate…"),
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
  (`Mod+E`).
- Unsaved indicator in the header while the debounce is pending; "Saved" fades after 1 s. `Mod+S` saves at once;
  leaving the text, opening another note and closing the window save first.
- Conflict banner if the file changed on disk while editing: "This note changed outside Notekea —
  Keep mine / Load theirs"; a note without unsaved edits reloads by itself. A note deleted while open offers
  Restore / Close; a save never recreates a note that was moved or deleted.
- Find in note: `Mod+F` opens an inline bar with match count and next/previous (`Enter` / `Shift+Enter`).
- Read mode: code blocks colored by language (Edit mode keeps code plain); `[[links]]` and links to notes open the
  note, a missing wiki link's note is created at the vault root, a name several notes share opens the closest one
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
  or an embed inside a sentence stay links.
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
  beside it (revised by 1.14: Files, Search notes, Today and Side panel, then Open another vault, About and Settings
  at the bottom; new notes and folders come from Mod+N and the tree's menu). The sidebar's header holds the vault's name, then Sort, Expand all, Collapse all and Minimize; the
  Search notes and Today rows and the New note / New folder buttons move to the rail.
- **Window size:** the window opens large enough (1440 × 900 dp at most, never larger than the screen's usable area)
  to show the tool rail, the sidebar, a note and the outline beside it, which are open by default; the welcome screen
  and a vault's workspace share that size, so the window never resizes itself when a vault opens or closes.
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

### ⬜ 1.14 Properties view — `feature/properties`

Frontmatter shown as a compact form above the note content.

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
  "Coming soon", above its question field shown disabled; `Mod+Shift+O` always comes back to the inspector.
- **The workspace's frame** (decided with the developer while 1.14 was reviewed):
  - **Tool rail:** Files, Search notes, Today and Side panel at the top; Open another vault, About and Settings at the
    bottom, as in Obsidian. New note and New folder leave the rail (Mod+N and the tree's menu stay), "Open another
    vault" leaves the sidebar's bottom, and the editor toolbar's Outline button leaves for the rail's Side panel.
  - **About:** a dialog with the logo, the name, the version and a link to GitHub, and every keyboard shortcut of the
    platform as keycaps, in groups from the most used (Notes, Writing, Workspace).
  - **Settings:** a dialog with the sections to come, dimmed, and "Coming soon" until settings are built.
  - **Logo:** a placeholder (a cobalt square with an "N") in the window, About and the welcome screen, until the
    designed logo (an SVG of paths on a 1024 square, with and without its background) replaces it and the packaged
    icons are drawn from it.
  - **Version:** people see `0.1.0-alpha` (About, crash reports) until the first public release; the installers keep
    a plain numeric version, which DMG and MSI require.
- "Show source" toggles the raw YAML, editable and saved once typing pauses, like the text; YAML the form cannot read
  shows an error banner with its line and falls back to the raw view, never losing data.
- **Done when:** round-tripping every sample-vault note through the properties view produces byte-identical
  files when nothing was edited.

### ⬜ 1.15 Embeds — `feature/embeds`

- `![[Other note]]` renders the target note inline in Read mode inside a bordered card with its title as a link;
  `![[Other note#Heading]]` embeds only that section.
- Recursion guard: an embed that includes itself (directly or indirectly) shows "Circular embed" instead of
  looping; maximum depth 3.
- Missing targets show a placeholder with "Create note".

### ⬜ 1.16 Sample vault — `chore/sample-vault`

`smoke-data/vaults/Sample Vault/`, next to the smoke test vaults (one dataset for smoke tests, unit tests,
evaluation and demos; `SmokeDataTest` and `smoke-data/README.md` are updated with it), with 40 Markdown notes on
varied topics (projects, journal, books, recipes, meeting notes), with frontmatter, wiki links and some deliberately
related notes without links. It also covers every feature above: tasks, all callout types, images and attachments,
daily notes, templates, embeds (including a circular one) and a `.obsidian/` folder with non-default settings. Used
by tests, manual QA and demos.

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

### ⬜ 1.18 Editor memory — `chore/editor-memory`

The editor alone, before any AI, should stay light: measure how much memory it uses and cut it, so M2 and M3 start
from a small base (budget of 5.4: under 400 MB).

- **Measure:** the memory of the whole process (resident memory, not only the Java heap) on Linux, then Windows and
  macOS, with `smoke-data/vaults/Personal Notes` and the Large Vault (`smoke.sh large`, 5,000 notes): right after
  opening, after opening and closing 20 notes in tabs, after scrolling a long note in Edit and Preview, and after
  ten minutes idle. The numbers go in `docs/performance.md`.
- **No growth:** opening and closing notes, tabs and folders, and switching vaults, gives back what it took: memory
  after closing everything returns near the level after opening (no leak in the editor's caches, the tabs' scroll
  memory, the decoded images or the file watcher).
- **Cuts the measurements point at:** a heap limit and a garbage collector suited to a desktop app (set in the
  packaged app's JVM options), bounded caches (decoded images, note texts, metadata), note texts never held twice,
  and Skia's caches sized for a text app.
- **Done when:** the editor alone stays under 400 MB with the Large Vault open on Linux, the measurements are in
  `docs/performance.md`, and a test keeps the bounded caches bounded. 5.4 measures the whole app again, AI included.

---

## M2 — Indexing, links and related notes

### ⬜ 2.1 Markdown chunking — `feature/chunking`

Split each note by headings into chunks of ~200–400 tokens with overlap; keep note path, heading path and
character range of every chunk (needed for citations). Pure `commonMain` logic, exhaustively unit tested.

### ⬜ 2.2 Embedding engine — `feature/embeddings`

`LlmEngine.embed` implemented with java-llama.cpp and a small bundled GGUF embedding model
(~100 MB, e.g. nomic-embed or bge-small). Batched, off the UI thread, cancellable.

### ⬜ 2.3 Hybrid index — `feature/hybrid-index`

`VectorIndex` with Lucene: HNSW vectors + BM25 keywords, merged with reciprocal rank fusion. Incremental
re-indexing using the SQLDelight index state (path, content hash, modified date). Index stored in the app data
directory, never inside the vault.

**UI — Indexing progress:**
- Status bar: "Indexing 340 / 1,204" with a thin progress bar; "Indexed 1,204 notes" when done.
- Click opens a popover with the last run, duration, skipped files and a "Rebuild index" button.
- First run on a large vault shows a non-blocking banner in the editor: "Notekea is reading your notes.
  Related notes and chat will be ready in about 2 minutes." The app stays usable.
- **Done when:** editing one note re-indexes only that note within 2 s; closing the app mid-indexing resumes
  correctly.

### ⬜ 2.4 Related notes panel — `feature/related-notes`

For the open note, the top 8 most similar notes (excluding notes already linked from it), updated when the note
changes.

**UI — Right panel, Related section:**
- Cards with note title, folder and the most relevant snippet (matching passage highlighted with `accent`).
- Actions on hover: Open, Open to the side, **Insert link** (adds `[[Note]]` at the cursor). Relations are never
  written to the note automatically; "Insert link" is the only way a suggestion becomes a real link.
- Section "Already linked" collapsed at the bottom.
- States: index not ready ("Related notes appear after indexing"), no results, error.

### ⬜ 2.5 Backlinks — `feature/backlinks`

A link graph built from `[[wiki links]]`, Markdown links, embeds and frontmatter aliases, kept up to date by the
`FileWatcher` (independent of the vector index, so it works even before indexing finishes).

**UI — Right panel, Backlinks section:**
- **Linked mentions:** notes that link to the current note, each with the paragraph containing the link.
- **Unlinked mentions:** notes that contain the current note's title or an alias as plain text, with a
  **Link** button that turns that mention into `[[Note]]` (explicit user action).
- Count badge on the tab; click a mention to open the note scrolled to it.
- Next to **Related**, this shows the difference between links the user made and connections the AI found.
- Renaming a note offers to update every link to it ("Update 12 links in 7 notes?"), never silently.
- **Done when:** link resolution matches Obsidian's (shortest path, aliases, headings `[[Note#Heading]]`,
  block references are kept as-is) on the sample vault.

### ⬜ 2.6 Hybrid search — `feature/hybrid-search`

`Mod+Shift+F` switches to hybrid search: results ranked by meaning and keywords, each with its snippet.

### ⬜ 2.7 Graph view — `feature/graph-view`

A map of how notes connect, made to be understood at a glance by someone who is not technical: it opens small
(the current note and its neighbors), colors itself, and has no physics settings. Only notes are nodes; tags and
attachments never are, so the graph never turns into a hairball of everything in the vault.

**UI — Graph** (opened with `Mod+G` or from the sidebar, as an editor tab):
- **Local first:** opened from a note, the graph is local: that note and its direct neighbors (depth 1), with a
  1 / 2 depth selector. The global graph is an explicit choice ("Whole vault"), and the default when no note is open.
  With more than 2,000 nodes the global graph suggests local mode.
- **Nodes and edges:** nodes sized by number of links; `[[links]]` as solid edges; semantic relations (above a
  similarity threshold) as dashed edges in `accent`, labeled "Related by meaning" and toggleable: on by default in
  local mode, off in global mode.
- **Automatic colors:** nodes colored by top-level folder from the design system palette, with a legend, and no
  setup needed. Custom color groups by tag are a later option.
- **Focus on hover:** hovering a note highlights it, its edges and its neighbors and dims the rest.
- **Labels:** always shown in local mode; in global mode only once zoomed in enough to read them.
- **Filters:** exclude folders (for example templates or an archive), remembered per vault; orphans are hidden by
  default in global mode ("Show orphans").
- **Layout:** force-directed, computed off the UI thread, with tuned defaults (checked on the `smoke-data` vaults)
  and no physics sliders; at most one "Closer / More spread out" control.
- **Interaction:** pan, zoom and drag; clicking a node opens the note. Keyboard: arrow keys move between connected
  nodes, `Enter` opens the focused note, `Esc` returns to the note.
- **States:**
  - Empty vault, or a note with no links: a hint that teaches ("Link notes with `[[ ]]` to see them connected
    here").
  - Index not ready: only link edges, with a hint.
  - Too large: local mode suggestion.
- **Out of scope:** Canvas-style whiteboards (see "Out of scope for the MVP").
- **Done when:** opening the graph from a note shows a readable local graph without any setup, the global graph
  stays responsive on a 2,000-note vault, and every node can be reached and opened with the keyboard.

### ⬜ 2.8 Retrieval evaluation set — `chore/rag-eval`

`smoke-data/eval/retrieval.json` (over the 1.16 sample vault): 50 questions over the sample vault with the expected
note and heading. A test reports recall@5 and fails below the agreed baseline, so changes to chunking, models or
ranking are measured.

---

## M3 — Chat with citations

Local models are small (about 2B, 4B or 8B parameters), so answer quality comes from **what reaches the model and
from checking the answer against the notes**, not from model size: retrieval is reranked, evidence is extracted
before answering, and every sentence of the answer must be backed by a quote that exists in a note. Parallel
generation (several llama.cpp slots sharing one loaded model) runs the independent steps at the same time. Models
critiquing or debating their own answers without evidence from the notes are not used: small models judge no
better than they answer, and each round adds waiting time. Every step is measured with the evaluation set (3.4)
and kept only if it improves citation accuracy within the latency budget (`ARCHITECTURE.md` §12).

### ⬜ 3.0 Parallel inference spike — `chore/llm-parallel-spike`

Before 3.2, measure with real 2B, 4B and 8B chat models on the reference machines (Windows, macOS, Linux):
- java-llama.cpp `setParallel(n)` with continuous batching, called concurrently from coroutines: does each call get
  its own slot, and is the output identical to a single call with the same seed?
- RAM per extra slot (KV cache) and tokens/s with 1, 2 and 4 slots, on CPU only and with GPU offload.
- `rerank()` with a small GGUF reranker (bge-reranker class): quality on the sample vault and time per 20 chunks.
- Grammar-constrained output (`jsonSchemaToGrammar`): valid JSON every time, cost in tokens/s.

- Memory of the whole process (editor included) with each model and a context of 2k, 4k and 8k tokens, and with the
  KV cache in 8 bits instead of 16: what it saves and what it costs in quality on the 3.4 evaluation set.
- Whether a 1–2B model with the pipeline of `ARCHITECTURE.md` §12 reaches the minimum quality of 3.4: if it does, it
  is the default model, so chatting fits the memory budget of 5.4 on an 8 GB laptop.

Result: `docs/spikes/<date>-llm-parallel.md` with the numbers, the default slot count per RAM size (likely one on
8 GB, since every slot adds its own KV cache), the context size and the latency budget used by 3.5 and 3.6.

### ⬜ 3.1 Model manager — `feature/model-manager`

Catalog of tested chat models (JSON parsed with Ghost): **Small (~2B)**, **Medium (~4B)** and **Large (~8B)** GGUF,
plus the reranker model (3.0), which is downloaded with whichever chat model is chosen. Download with Ktor
(progress, pause, resume), SHA-256 verification, delete. Hardware check: warns when RAM is below the model's
requirement, including the KV cache of its parallel slots (3.0) and the editor itself. The model is recommended from
the machine's RAM (Small on 8 GB, Medium on 16 GB), with nothing technical to choose. Sizes below are placeholders
until 3.0 measures them.

**UI — Model manager** (opened from Settings or the status bar):

```
┌─────────────────────────────────────────────────────────┐
│ AI model                                                 │
│ ○ Small  (~2B, 1.5 GB) · fast, basic answers             │
│ ● Medium (~4B, 2.5 GB) · recommended for 16 GB  [Active] │
│ ○ Large  (~8B, 5 GB) · best answers, needs 16 GB or more │
│                                                          │
│ Downloading Medium ▓▓▓▓▓▓░░░░ 61% · 1.4 / 2.3 GB  [Pause]│
│ Models are stored in ~/Library/Application Support/...   │
└─────────────────────────────────────────────────────────┘
```

- States: not downloaded, downloading (pause/resume/cancel), verifying, ready, failed checksum (retry),
  not enough disk space.

### ⬜ 3.2 Chat engine — `feature/chat-engine`

`LlmEngine.generate` with java-llama.cpp, streaming tokens as a `Flow`, cancellable. The engine loads the model once
with the slot count chosen by 3.0 and accepts concurrent generations (a `Semaphore` caps them at the slot count);
structured steps use grammar-constrained JSON output. Prompts are built with
`LlamaModel.applyTemplate` (passing messages with an empty prompt returns nothing), and integration tests exit the
process explicitly because llama.cpp keeps native threads alive (spike of 2026-10-23). RAG pipeline: retrieve top
chunks (hybrid index), build a prompt that requires citing chunk ids, parse citations from the answer, drop
citations that do not match a retrieved chunk.

- **Memory:** the model is loaded when the chat opens and unloaded after a few minutes without use (5 by default,
  in Settings), so writing notes never pays for it; its weights are read from disk with mmap, not locked in memory,
  and the context size is the one 3.0 measured, not the model's maximum (budgets in 5.4).

### ⬜ 3.3 Chat panel — `feature/chat-panel`

**UI — Right panel, Chat mode** (its "Coming soon" screen and the Inspector | Chat control exist since 1.14)**:**
- Message list; the answer streams token by token with a "Stop" button (`Esc`).
- Citations rendered as `accent` chips `[1]`, `[2]`; hover shows the snippet, click opens the note scrolled to
  and highlighting the cited paragraph.
- "Sources" list under each answer with note titles.
- Scope selector above the input: **Whole vault** / **This note** / **This folder**.
- Input: multiline, `Enter` sends, `Shift+Enter` new line; `Mod+L` focuses the chat from anywhere.
- When nothing relevant is found the assistant says so instead of guessing ("I couldn't find this in your notes").
- While an answer is prepared, one line shows the current step in plain language ("Searching your notes…",
  "Reading 6 notes…", "Checking sources…"), never a bare spinner.
- **Deep answer** toggle next to the input (3.6), off by default, with a one-line hint: "Takes longer, checks more
  drafts against your notes."
- States: model not downloaded (inline card with "Download model"), index not ready, generating, error with retry.
- Chat history per vault, stored as JSON (Ghost) in the app data directory, with "Clear history".

### ⬜ 3.4 Answer evaluation — `chore/answer-eval`

Extends the eval set with expected facts per question; a manual-run task reports citation accuracy
(cited paragraph actually contains the fact) for the active model. It also reports latency per step, so 3.5
and 3.6 compare quality and speed for each model size.

### ⬜ 3.5 Answer pipeline — `feature/answer-pipeline`

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
- **Done when:** on the evaluation set, citation accuracy beats 3.2 for Small, Medium and Large, and the median
  answer time stays within the budget set by 3.0.

### ⬜ 3.6 Deep answer mode — `feature/deep-answer`

An opt-in mode for when precision matters more than speed. Several drafts are written in parallel (different
seeds, same evidence), each goes through the verification of 3.5, and the draft with the most supported sentences
wins; ties go to the shorter draft. It is chosen by verified support, not by majority vote.
- **UI:** the **Deep answer** toggle of the chat panel (3.3); the step line shows "Writing 3 drafts…" and
  "Comparing drafts…"; Stop (`Esc`) keeps the best verified draft so far.
- Draft count follows the slot count measured in 3.0 for the machine's RAM.
- **Done when:** on the evaluation set it beats the normal mode's citation accuracy for every model size, and it is
  cancellable at any moment.

---

## M4 — Git sync

### ⬜ 4.1 Git sync engine — `feature/git-sync`

`GitSync` with JGit: connect (clone or init + remote), auto-commit after 10 s of inactivity, `pull --rebase` on
open and every 5 minutes, push after commit. Conflicts keep both versions (`note.md` and `note (conflict).md`).
The index folder is never committed. Token stored in the OS keychain (Java Keyring).

### ⬜ 4.2 Sync UI — `feature/sync-ui`

- **Settings → Sync:** remote URL, authentication (HTTPS token or SSH key), auto-sync on/off, interval,
  "Sync now".
- **Status bar:** "Up to date", "Syncing…", "3 changes pending", "Conflict" (`error` color).
- **Conflicts:** banner listing conflicted notes; opening one shows both versions side by side with
  "Keep mine", "Keep theirs" and "Keep both".
- States: not configured (status item hidden), offline (retries silently, shows "Offline"), auth failed
  (opens Settings → Sync with the error).

---

## M5 — Release

### ⬜ 5.1 Onboarding — `feature/onboarding`

First run flow, three steps, skippable at any point:

1. **Welcome:** what Notekea does in one sentence and the privacy promise ("Your notes never leave this computer").
2. **Choose your notes:** open folder / create vault / use the sample vault.
3. **Get the AI:** recommended model for this machine's RAM, download in the background while indexing starts.

### ⬜ 5.2 Settings — `feature/settings`

`Mod+,` opens Settings with sections: General (theme, language, open last vault), Editor (font size, line width,
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

Notekea should run well on the laptop a non-technical person has (8 GB of RAM). With the whole app built, measure it
on the reference machines (Windows, macOS, Linux) and cut what the numbers point at, starting from the editor's
measurements of 1.18.

- **Memory budgets** (whole process, the Sample Vault and a 1,000-note vault):

  | Situation | Budget |
  |---|---|
  | Editor only | < 400 MB |
  | Indexing or searching (embedding model loaded) | < 700 MB |
  | Chatting with the Small model on an 8 GB laptop | < 2.5 GB |
  | Chat closed for a few minutes | back to the editor-only level |

- **Measure:** profile the editor with the Large Vault (`smoke.sh large`), the indexing and a chat session; record
  memory, startup time and typing latency per OS in `docs/performance.md`.
- **JVM:** a heap limit and a garbage collector suited to a desktop app, and a runtime trimmed with jlink to the
  modules the app uses (5.3 packages it).
- **Cuts the measurements point at:** caches that grow without bound, note texts held twice, the embedding model
  kept loaded after indexing ends.
- **Done when:** the release run (5.5) meets every budget on Windows, macOS and Linux.

### ⬜ 5.5 Release v0.1.0

Website `notekea.app` with download links, README screenshots, announcement (Hacker News, r/ObsidianMD,
r/LocalLLaMA, Kotlin Slack). Measure: stars in the first month, issues from real users, retention feedback.

---

## Post-MVP (ordered by expected value)

1. **Extensions via MCP** — Koog agent runtime with `ai.koog:agents-mcp` once stable; extensions are MCP servers
   the user enables explicitly. Agent features only when local models handle tool calling reliably.
2. **Mobile (Android first, then iOS)** — `LlmEngine` via Llamatik, `VectorIndex` via SQLite + sqlite-vec,
   `GitSync` via libgit2 on iOS (see "Mobile risks" in `LIBRARIES.md`).
3. **More sources** — PDFs and web clippings inside the vault, indexed like notes.
4. **Repositories** — a code repository becomes part of the vault, so the graph and the chat cover it. Separate from
   Git sync (4.1), which only versions the vault itself; it reuses its JGit clone and pull. In three steps, each
   worth shipping on its own:
   1. **Repository docs:** clone a repository into a read-only vault folder with only its Markdown (README,
      `docs/`, ADRs, CHANGELOG), and update it with "Update" (pull). Indexing, links, graph and chat then work
      unchanged.
   2. **Module graph from a parser:** a parser (for example tree-sitter, to be approved in `LIBRARIES.md`) writes
      one note per module or package, with its imports as links. The graph is exact and repeatable, and takes
      seconds, with no model involved.
   3. **Summaries and chat over the code:** optional summaries per module from the local model, marked as
      generated, and chat over code chunked by function or class, with citations to `file:line` and never to a
      generated note, so verified answers (3.5) check against the code itself. Only if an evaluation set like 2.8
      shows small local models (and a code embedding model) are good enough: a medium repository can take hours
      on a laptop.
5. **More than two split panes** and detachable windows.
6. **Custom title bar** with the tabs inside it (JetBrains Jewel `DecoratedWindow`).
7. **Screenshot tests** for UI regressions (Roborazzi).
8. **Use an installed Ollama** — when the user already runs Ollama, Notekea can use its model instead of loading its
   own, so the memory is not spent twice. For technical users; the bundled model stays the default.

## Out of scope for the MVP

Cloud models (OpenAI, Anthropic…), accounts, real-time collaboration, a plugin API of our own, mobile apps,
importers for other note apps (Notion, Evernote), Canvas-style whiteboards, automatic link insertion.
