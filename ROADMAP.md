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

**Status tracking:** features are never removed from this file. Each feature title carries its status —
⬜ not started, 🚧 in progress (branch open), ✅ done (with its pull request) — and the table below counts them.
`CHANGELOG.md` records what changed; this file records what is planned and what is left.

| Milestone | Goal | Estimate | Status |
|---|---|---|---|
| M0 — Foundation | Project setup, tooling, guidelines | Done | ✅ |
| M1 — Vault and editor | Open a folder, browse, read and edit notes with Obsidian-level essentials | 3.5 weeks | 3 / 16 |
| M2 — Indexing, links and related notes | Local embeddings, hybrid search, related notes, backlinks, graph | 2.5 weeks | 0 / 8 |
| M3 — Chat with citations | Local LLM, model download, RAG chat with citations | 2 weeks | 0 / 4 |
| M4 — Git sync | Optional commit / pull / push of the vault | 1 week | 0 / 2 |
| M5 — Release | Onboarding polish, packaging, signing, first public release | 1 week | 0 / 4 |
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
│ 🔍 Search    │──────────────────────────────────────────│ [Related][Backlinks]   │
│ 📅 Today     │ Pricing ideas                 Edit|Read  │ [Outline][Chat]        │
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

- **Sidebar** (default 260 dp, min 200, collapsible with `Mod+\`): search field, "Today" (daily note),
  bookmarks, folder tree.
- **Editor** (flexible, min 480 dp): tab bar, title, properties, Edit / Read toggle, Markdown content.
  Can be split into two panes side by side.
- **Right panel** (default 340 dp, min 280, collapsible with `Mod+Shift+\`): tabs **Related**, **Backlinks**,
  **Outline** and **Chat**.
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

### ⬜ 1.4 Vault tree and note list — `feature/vault-tree`

Read the folder recursively (Okio), only `.md` files, ignoring `.git`, `.obsidian`, `.trash` and hidden folders.
Parse frontmatter (title, tags, aliases) with Ghost YAML. Live updates through `FileWatcher`.

**UI — Sidebar:**
- Folder tree with expand/collapse, note count per folder.
- Sort by name or modified date; selection highlighted with `primary`.
- Context menu: New note, New folder, Rename, Reveal in Finder/Explorer/Files, Delete (confirmation dialog
  with "Delete note" button, platform button order).
- States: empty vault ("This folder has no notes yet" + "New note" button), loading skeleton, error banner.
- Shortcuts: `Mod+N` new note, `F2` / `Enter` (macOS) rename, `Delete` / `Mod+Backspace` (macOS) delete.
- **Done when:** a 5,000-note vault lists in under 1 s after the first scan, external changes appear within 1 s.
- Smoke test: rewrite 1.3-02, 1.3-04 and 1.3-07 (marked "Revised by 1.4" in `MVP_SMOKE_TEST.md`).

### ⬜ 1.5 Editor — `feature/editor`

`BasicTextField` with Markdown highlighting (headings, bold, italic, code, links, `[[wiki links]]`), autosave
(debounced 1 s), Read mode with the Markdown renderer, clickable `[[links]]` and `#tags`.

**UI — Editor pane:**
- Header: note title (editable, renames the file), path breadcrumb, Edit / Read toggle (`Mod+E`).
- Unsaved indicator in the header while the debounce is pending; "Saved" fades after 1 s.
- Conflict banner if the file changed on disk while editing: "This note changed outside Notekea —
  Keep mine / Load theirs".
- Find in note: `Mod+F` opens an inline bar with match count and next/previous (`Enter` / `Shift+Enter`).
- **Done when:** typing never loses text, external changes are detected, frontmatter is preserved untouched.

### ⬜ 1.6 Quick switcher and search — `feature/quick-switcher`

`Mod+P` opens a palette with fuzzy search over note titles and aliases; `Mod+Shift+F` searches note content
(plain text in M1, upgraded to hybrid search in M2).

**UI — Palette:** centered overlay 560 dp wide, input on top, up to 10 results with title, folder and matching
snippet; arrows to move, `Enter` to open, `Esc` to close. Empty state: "No notes match".

### ⬜ 1.7 Tasks and callouts — `feature/markdown-extensions`

Obsidian-flavored Markdown blocks that most vaults use.

- **Tasks:** `- [ ]` / `- [x]` rendered as checkboxes in Read mode and as clickable checkboxes in Edit mode;
  clicking toggles the character in the file (explicit user action). `Mod+Enter` toggles the task on the cursor line.
- **Callouts:** `> [!note]`, `[!tip]`, `[!warning]`, `[!danger]`, `[!info]`, `[!quote]` and their aliases, rendered
  as cards with icon and theme color; foldable callouts (`[!note]-` / `[!note]+`) collapse in Read mode.
- Unknown callout types render as `note`, never as broken text.
- **Done when:** every callout type and nesting level in the sample vault renders correctly in Read mode.

### ⬜ 1.8 Images and attachments — `feature/attachments`

- Render images (`![[image.png]]`, `![alt](path.png)`) inline, scaled to the editor width; `![[image.png|300]]`
  sets the width.
- **Paste or drop an image** (`Mod+V`, drag and drop) saves it into the attachments folder and inserts the link.
  Attachments folder: `.obsidian/app.json` `attachmentFolderPath` when present, otherwise `attachments/`
  (configurable in Settings).
- Non-image attachments (PDF, audio, other files) show as a file chip that opens with the system default app.
- States: missing file (placeholder with the broken path and "Locate…"), unsupported format (file chip).
- **Done when:** pasting a screenshot works on the three OSes and the result opens unchanged in Obsidian.

### ⬜ 1.9 Outline — `feature/outline`

**UI — Right panel, Outline tab:** the headings of the current note as an indented tree; click scrolls the editor
to that heading; the heading under the cursor is highlighted while scrolling. Empty state: "This note has no
headings". Shortcut: `Mod+Shift+O` focuses the outline.

### ⬜ 1.10 Daily notes — `feature/daily-notes`

- "Today" in the sidebar and `Mod+D` open today's note, creating it if needed.
- Folder and date format from `.obsidian/daily-notes.json` when present, otherwise `Journal/` and `YYYY-MM-DD`;
  optional template (see 1.12).
- Previous / next day navigation in the note header (`Mod+Alt+←` / `Mod+Alt+→`).
- **Done when:** the date format, folder and template are honored and covered by tests, including timezones.

### ⬜ 1.11 Tabs and split panes — `feature/editor-tabs`

- Each opened note becomes a tab; `Mod+click` (or middle click) opens a link in a new tab.
- `Mod+W` closes the tab, `Ctrl+Tab` / `Ctrl+Shift+Tab` cycle tabs (on every OS), `Mod+1…9` jump to a tab.
- Split the editor into two panes side by side (`Mod+Shift+D` or the tab context menu "Split right");
  drag a tab between panes. Maximum two panes in the MVP.
- Open tabs and splits are restored when the vault is reopened.
- Tabs live inside the window content; the window keeps its native title bar (`PLATFORM_GUIDELINES.md`).

### ⬜ 1.12 Templates — `feature/templates`

- Templates folder: `.obsidian/templates.json` when present, otherwise `Templates/`.
- `Mod+T` opens a picker of templates and inserts the chosen one at the cursor (or into a new note).
- Variables: `{{title}}`, `{{date}}`, `{{time}}`, with optional format (`{{date:YYYY-MM-DD}}`).
- States: no templates folder ("Create a Templates folder to add templates" with a "Create" button).

### ⬜ 1.13 Bookmarks — `feature/bookmarks`

- Star a note from the header or the context menu (`Mod+Shift+B`); bookmarks section at the top of the sidebar,
  reorderable by drag and drop.
- Stored in the app data directory per vault, so the vault files are never touched.

### ⬜ 1.14 Properties view — `feature/properties`

Frontmatter shown as a compact form above the note content.

- Types detected from the values: text, list (tags, aliases as chips), number, checkbox, date.
- Editing a property patches only the edited lines of the raw frontmatter text, preserving key order, quoting and
  unknown keys. Ghost YAML is used only to read typed values: it drops unknown keys and re-quotes strings, so the
  frontmatter is never re-encoded as a whole (see `docs/spikes/2026-10-23-ghost-llama.md`).
- "Show source" toggles the raw YAML; invalid YAML shows an error banner and falls back to the raw view, never
  losing data.
- **Done when:** round-tripping every sample-vault note through the properties view produces byte-identical
  files when nothing was edited.

### ⬜ 1.15 Embeds — `feature/embeds`

- `![[Other note]]` renders the target note inline in Read mode inside a bordered card with its title as a link;
  `![[Other note#Heading]]` embeds only that section.
- Recursion guard: an embed that includes itself (directly or indirectly) shows "Circular embed" instead of
  looping; maximum depth 3.
- Missing targets show a placeholder with "Create note".

### ⬜ 1.16 Sample vault — `chore/sample-vault`

`samples/vault/` with 40 Markdown notes on varied topics (projects, journal, books, recipes, meeting notes), with
frontmatter, wiki links and some deliberately related notes without links. It also covers every feature above:
tasks, all callout types, images and attachments, daily notes, templates, embeds (including a circular one) and a
`.obsidian/` folder with non-default settings. Used by tests, manual QA and demos.

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

**UI — Right panel, Related tab:**
- Cards with note title, folder and the most relevant snippet (matching passage highlighted with `accent`).
- Actions on hover: Open, Open to the side, **Insert link** (adds `[[Note]]` at the cursor). Relations are never
  written to the note automatically; "Insert link" is the only way a suggestion becomes a real link.
- Section "Already linked" collapsed at the bottom.
- States: index not ready ("Related notes appear after indexing"), no results, error.

### ⬜ 2.5 Backlinks — `feature/backlinks`

A link graph built from `[[wiki links]]`, Markdown links, embeds and frontmatter aliases, kept up to date by the
`FileWatcher` (independent of the vector index, so it works even before indexing finishes).

**UI — Right panel, Backlinks tab:**
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

**UI — Graph** (opened with `Mod+G` or from the sidebar, as an editor tab):
- Notes as nodes, sized by number of links; `[[links]]` as solid edges and semantic relations (above a similarity
  threshold) as dashed edges in `accent`, toggleable.
- Local graph mode (the current note and its neighbors up to depth 2) and global mode.
- Filters: folder, tag, show orphans. Pan, zoom and drag; click opens the note.
- Force-directed layout computed off the UI thread; with more than 2,000 nodes it starts in local mode.
- States: empty vault, index not ready (only link edges, with a hint), too large (local mode suggestion).

### ⬜ 2.8 Retrieval evaluation set — `chore/rag-eval`

`samples/eval/retrieval.json` (fixtures from 1.16): 50 questions over the sample vault with the expected note and heading. A test
reports recall@5 and fails below the agreed baseline, so changes to chunking, models or ranking are measured.

---

## M3 — Chat with citations

### ⬜ 3.1 Model manager — `feature/model-manager`

Catalog of tested chat models (JSON parsed with Ghost; 3B–4B GGUF, ~2 GB), download with Ktor (progress, pause,
resume), SHA-256 verification, delete. Hardware check: warns when RAM is below the model's requirement.

**UI — Model manager** (opened from Settings or the status bar):

```
┌─────────────────────────────────────────────────────────┐
│ AI model                                                 │
│ ○ Small  (1.2 GB) · fast, basic answers                  │
│ ● Medium (2.3 GB) · recommended for 16 GB RAM  [Active]  │
│ ○ Large  (4.9 GB) · best answers, needs 32 GB RAM        │
│                                                          │
│ Downloading Medium ▓▓▓▓▓▓░░░░ 61% · 1.4 / 2.3 GB  [Pause]│
│ Models are stored in ~/Library/Application Support/...   │
└─────────────────────────────────────────────────────────┘
```

- States: not downloaded, downloading (pause/resume/cancel), verifying, ready, failed checksum (retry),
  not enough disk space.

### ⬜ 3.2 Chat engine — `feature/chat-engine`

`LlmEngine.generate` with java-llama.cpp, streaming tokens as a `Flow`, cancellable. Prompts are built with
`LlamaModel.applyTemplate` (passing messages with an empty prompt returns nothing), and integration tests exit the
process explicitly because llama.cpp keeps native threads alive (spike of 2026-10-23). RAG pipeline: retrieve top
chunks (hybrid index), build a prompt that requires citing chunk ids, parse citations from the answer, drop
citations that do not match a retrieved chunk.

### ⬜ 3.3 Chat panel — `feature/chat-panel`

**UI — Right panel, Chat tab:**
- Message list; the answer streams token by token with a "Stop" button (`Esc`).
- Citations rendered as `accent` chips `[1]`, `[2]`; hover shows the snippet, click opens the note scrolled to
  and highlighting the cited paragraph.
- "Sources" list under each answer with note titles.
- Scope selector above the input: **Whole vault** / **This note** / **This folder**.
- Input: multiline, `Enter` sends, `Shift+Enter` new line; `Mod+L` focuses the chat from anywhere.
- When nothing relevant is found the assistant says so instead of guessing ("I couldn't find this in your notes").
- States: model not downloaded (inline card with "Download model"), index not ready, generating, error with retry.
- Chat history per vault, stored as JSON (Ghost) in the app data directory, with "Clear history".

### ⬜ 3.4 Answer evaluation — `chore/answer-eval`

Extends the eval set with expected facts per question; a manual-run task reports citation accuracy
(cited paragraph actually contains the fact) for the active model.

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
spell check), Files (attachments folder, daily notes folder and date format, templates folder — prefilled from
`.obsidian/` when present), AI (model manager, chat scope default), Sync (M4), Shortcuts (read-only list),
About (version, licenses, privacy statement).

### ⬜ 5.3 Packaging and signing — `chore/release-pipeline`

GitHub Actions on tag `v*`: build `.dmg` (macOS, signed and notarized), `.msi` (Windows, signed) and `.deb`
(Linux), attach them to a GitHub Release with checksums. Requires an Apple Developer account and a Windows
code-signing certificate.

### ⬜ 5.4 Release v0.1.0

Website `notekea.app` with download links, README screenshots, announcement (Hacker News, r/ObsidianMD,
r/LocalLLaMA, Kotlin Slack). Measure: stars in the first month, issues from real users, retention feedback.

---

## Post-MVP (ordered by expected value)

1. **Extensions via MCP** — Koog agent runtime with `ai.koog:agents-mcp` once stable; extensions are MCP servers
   the user enables explicitly. Agent features only when local models handle tool calling reliably.
2. **Mobile (Android first, then iOS)** — `LlmEngine` via Llamatik, `VectorIndex` via SQLite + sqlite-vec,
   `GitSync` via libgit2 on iOS (see "Mobile risks" in `LIBRARIES.md`).
3. **More sources** — PDFs and web clippings inside the vault, indexed like notes.
4. **More than two split panes** and detachable windows.
5. **Custom title bar** with the tabs inside it (JetBrains Jewel `DecoratedWindow`).
6. **Screenshot tests** for UI regressions (Roborazzi).

## Out of scope for the MVP

Cloud models (OpenAI, Anthropic…), accounts, real-time collaboration, a plugin API of our own, mobile apps,
importers for other note apps (Notion, Evernote), Canvas-style whiteboards, automatic link insertion.
