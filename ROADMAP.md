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
  its settings for attachments, daily notes and templates folders are honored, and Settings → Files writes them
  there (5.2).
- **Local only:** see the privacy rules in `GUIDELINES.md`.

**Closing a milestone:** every milestone ends with its **performance pass** (its last item: 1.18, 2.9, 3.7, 4.3, 4.8,
5.4), and it is done when all its items are ✅; the next one then starts. 4.3 and 4.8 were moved into 5.4 by the
developer on 2026-11-11, with videos and all of M5 in one pull request before the single release run.

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
⬜ not started, 🚧 in progress (branch open), ✅ done (with its pull request), ↪ moved into a later item — and the table
below counts them.
`CHANGELOG.md` records what changed; this file records what is planned and what is left.

| Milestone | Goal | Estimate | Status |
|---|---|---|---|
| M0 — Foundation | Project setup, tooling, guidelines | Done | ✅ |
| M1 — Vault and editor | Open a folder, browse, read and edit notes with Obsidian-level essentials | 3.5 weeks | 18 / 18 |
| M2 — Indexing, links and related notes | Color themes, local embeddings, hybrid search, related notes, backlinks, graph, vault queries and tasks, packaged check | 5 weeks | 13 / 13 |
| M3 — Chat with citations | Local LLM, model download, RAG chat with citations, verified answers, deep answer mode, questions in plain words, actions from chat | 7 weeks | 10 / 10 |
| M4 — Git sync | Optional commit / pull / push of the vault | 1 week | 2 / 2 (4.3 moved into 5.4) |
| M4b — Sources, study cards and agent | Web clips, documents, study cards, agent mode, videos | 10 weeks | 4 / 5 (4.8 moved into 5.4) |
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
  with seven color themes, Workspace Blue the one it starts with.
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

## Done milestones: M1 to M4b

Each done milestone's items, specs and "Done when" are in `docs/roadmap/`, under their numbers, which keep their
meaning everywhere they are cited (`ROADMAP.md` 2.12 is in `docs/roadmap/M2.md`):

- [M1 — Vault and editor](docs/roadmap/M1.md) ✅
- [M2 — Indexing, links and related notes](docs/roadmap/M2.md) ✅
- [M3 — Chat with citations](docs/roadmap/M3.md) ✅
- [M4 — Git sync](docs/roadmap/M4.md) ✅
- [M4b — Sources, study cards and agent](docs/roadmap/M4b.md) ✅

## M5 — Release

### 🚧 5.1 Onboarding — `feature/videos` (built, in [notekea#142](https://github.com/juanchurtado1991/notekea/pull/142): an Apple-style welcome sheet over the welcome screen, the sample vault shipped)

First run flow, three steps, skippable at any point:

1. **Welcome:** what Notekea does in one sentence and the privacy promise ("The AI runs on this computer. Notes leave
   it only if you turn on sync.", reworded on 2026-11-11 because sync sends them to the repository).
2. **Choose your notes:** open folder / create vault / use the sample vault.
3. **Get the AI:** recommended model for this machine's RAM, download in the background while indexing starts.

### 🚧 5.2 Settings — `feature/videos` (built, in [notekea#142](https://github.com/juanchurtado1991/notekea/pull/142): macOS System Settings style, General, Editor, Files, Shortcuts, About, and Spanish)

`Mod+,` opens Settings (the dialog the tool rail's Settings opens since 1.14, with its sections marked "Coming soon")
with sections: General (language, open last vault), Appearance (mode and color theme, built in 2.12), Editor (font size, line width),
Files (attachments folder, daily notes folder and date format, templates folder), AI (model manager, chat scope default),
Sync (M4), Shortcuts (read-only list), About (version, licenses, privacy statement).

Decided with the developer on 2026-11-11:

- **Language:** the whole interface also in Spanish, with English, Español or the system's language in General.
- **Files:** settings are written to the vault's `.obsidian/` files, the ones Notekea already reads, so Obsidian and
  Notekea agree.
- **Renames:** they update links without asking, as decided in 1.x; there is no "ask" setting.
- **Spell check:** moved after the MVP.
- **Designs:** 2–3 rendered designs per new section, picked by the developer before they are built.
- **Already in Settings since #140:** Ask about Settings beside the dialog. Every setting has a `SettingsPlace` the chat
  opens and makes blink, so each new setting here gets one (GUIDELINES §15).

Built in #142:

- **Spanish:** every string of the interface has a Spanish version (`values-es`), switched live, without losing what the
  screens hold. The chat's replies keep following the question's language. The tests run in English whatever the
  computer's language.
- **Notekea QA in Spanish (the developer's decision):** the manager (Test Buddy's `TesterLanguage`), the 95 manual
  cases and the testers' guides. Notekea QA starts in Spanish, and the automated cases run in English.
- **macOS redesign (the developer's approval of the HIG review, 2026-11-11):** the picks and the dependency audit are in
  `docs/macos-redesign.md`. It covers macOS's type scale, the sidebar's and chrome's colors, the source list, push
  buttons, a unified title bar and a menu bar on macOS, the chat as a document, macOS's alert, springs and the system's
  accent color. The window buttons stay the system's own on every platform.

### 🚧 5.3 Packaging — `feature/videos` (built, in [notekea#142](https://github.com/juanchurtado1991/notekea/pull/142): `.github/workflows/release.yml` and the README's Install and First launch)

GitHub Actions on tag `v*`: build `.dmg` (macOS), `.msi` (Windows) and `.deb` (Linux), attach them to a GitHub
Release with checksums.

- **Unsigned builds:** the MVP is not code-signed, to keep it free of paid certificates (an Apple Developer account
  and a Windows code-signing certificate cost about 300 USD a year together). Linux needs no signature.
- **First launch:** the README has a "First launch" section per OS, with what the system shows and how to open the
  app anyway: Windows SmartScreen ("More info" → "Run anyway") and macOS Gatekeeper (System Settings → Privacy &
  Security → "Open Anyway"), as the QA guides (`qa/GUIDE_*.md`) explain it.
- **Later, when there are users to justify it:** free signing for open source projects (SignPath Foundation) for
  Windows, and an Apple Developer ID with notarization for macOS. Neither blocks the release.

### 🚧 5.4 Performance and memory — `feature/videos` (measured on Linux in [notekea#142](https://github.com/juanchurtado1991/notekea/pull/142); Windows and macOS in the release run)

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

Done in #142, on Linux (`docs/performance.md`, M5):

- **Measured** every smoke run with all of M5 built. The heap stays at 80–110 MB, and the rest of the process is
  native (Skia, compiled code).
- **JVM:** the packaged app gives back the heap it does not use (`-XX:MaxHeapFreeRatio=20 -XX:MinHeapFreeRatio=5`),
  so the committed heap is 40 % smaller. String deduplication and a smaller code cache were tried and left out.
- **Leaks fixed:**
  - a document tab's drawn pages and its open PDF;
  - YouTube pictures;
  - the chat's undo journals;
  - a cancelled FFmpeg.
- **The long note's preview** was already done by One editing mode: the note is a lazy list of blocks.

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
11. **Spell check** — in English and Spanish, in Settings → Editor. Moved out of 5.2 by the developer on 2026-11-11:
   Compose Desktop has none, and its dictionaries are a new download.

12. **One agent loop** — asked for by the developer on 2026-11-14: the chat's work as solid as Claude Code's. Today a
   planner (3.9) and a loop of moves (4.7) are two engines joined at the plan. The design to move to is one loop:
   the model calls a tool (search, read, find, write a draft, check it), sees its real result, and goes on until the
   work is checked; the tools' results are kept as a transcript across the turns of a chat, compacted like its
   memory, instead of a record written afterwards. The local model's context (4,096 tokens) is what bounds it, so
   the design comes first, with an evaluation set of tasks over documents, and it is shown to the developer before
   it is built.

## Out of scope for the MVP

Cloud models (OpenAI, Anthropic…), accounts, real-time collaboration, a plugin API of our own, mobile apps,
importers for other note apps (Notion, Evernote), Canvas-style whiteboards, automatic link insertion.
