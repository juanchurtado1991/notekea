# Splitting `notes` into features before M3

Date: 2026-11-04. Status: proposed, waiting for the developer's decision. No code has moved.

## Result

Split `notes` into features **with packages guarded by Konsist, not Gradle modules**, in small pull requests that only
move code, before M3 starts. The work has one real difficulty: `tree` also holds the code that assembles the app's
window, so that code moves out first. Gradle modules would not pay for themselves yet: a full compile takes 20 s.

## Why now

`notes` holds 25.7k of the 41.5k lines of `commonMain`, in 379 files: the editor, the tree, indexing, search, Connections
and templates. Konsist keeps `notes`, `vault` and `core` apart (`ARCHITECTURE.md` §1), but nothing keeps the areas inside
`notes` apart. M3 and M4b add the chat, agent mode and new sources, which all use those areas, so boundaries are cheapest
to add before they arrive. Smaller areas also mean an agent reads less before changing one.

## What was measured

Sizes and dependencies are from `fix/audit-findings` at e344326e, which also holds PR #114. A script read every source
file and counted references to the types of each area. The compile times are from `main` at 1b14b07, on this 24-core
machine, with Gradle's cache and daemon as they are day to day.

### Areas of `notes`

| Area | Lines | Note |
|---|---:|---|
| `presentation/editor` | 7,782 | Depends on no other area of `presentation`; holds the side panel too (853 lines) |
| `domain` | 7,230 | 173 files in one flat package |
| `presentation/tree` | 4,641 | The hub (below) |
| `data` | 2,615 | Includes `jvmMain` and `androidMain` |
| `presentation/workspace` | 1,122 | Panes, tabs, side panel state |
| `presentation/switcher` | 625 | |
| `presentation/index` | 613 | |
| `presentation/connections` | 493 | |
| `presentation/templates` | 443 | |
| `presentation/related` | 362 | |
| `presentation/tags` | 275 | |
| `presentation/mentions` | 168 | |

### Dependencies between areas

Most areas are already independent. Only three things stand in the way:

1. **`tree` is a hub.** It reaches nine other areas (27 references to `editor`, 19 to `workspace`, 11 to `templates`, 10
   to `switcher`). They come from ten files that assemble the window rather than show a tree, about 1,450 lines:
   `VaultTreeScreen`, `VaultWorkspace`, `VaultTreeFollowers`, `VaultConnections`, `WorkspaceViewModels`, `WorkspaceStatusBar`,
   `SidebarRail`, `PaneContext`, `PaneEditors` and `PreviewNoteContent`.
2. **Two cycles.** `tree` and `workspace` depend on each other (`workspace` uses `FilePreview` and `TreeFilePreviews`), and
   so do `connections` and `related`.
3. **`domain` is flat.** About 73 of its files, some 2,900 lines, are used by more than one future feature: `RelativePath`,
   `NoteDocument`, `NoteFile`, `NoteContentStore`, `NoteFileOperations`, `LinkFinder`, `LinkResolver`, `FrontmatterReader`.

The script also reported `core`, `connections` and `related` reaching `editor`. Those were `NotekeaIcons.SidePanel` having
the name of a composable, not dependencies; the same goes for the references from `core` to `domain` and `vault`, which
Konsist already forbids.

### Compile times

| Change | Time |
|---|---:|
| Full recompile of `:composeApp:compileKotlinJvm` | 20 s |
| A body changes in a small domain class | 14 s |
| A public member added to `RelativePath`, used nearly everywhere | 9 s |

The last two are too close to read much into, but all three are short. The 3–4 minutes of `check` are not compile time,
so splitting the compile would not give them back. How much of `check` modules could save was not measured.

## Konsist packages or Gradle modules

| | Packages + Konsist | Gradle modules |
|---|---|---|
| Boundary | Fails in a test | Fails to compile, and `internal` really hides code |
| Cost to set up | Moves and a few rules | Build files, plus everything below |
| Compile time | Unchanged | Little to gain at 20 s |
| Compose resources | One `strings.xml`, as today | Each module has its own `Res`, or all share a module |
| Test fakes | `commonTest`, as today | A module of their own: there are no KMP test fixtures to share them |
| KSP, SQLDelight | Unchanged | Decided per module; one database across features needs a home |
| Coverage, Konsist, packaging | Unchanged | Kover and the Konsist paths need aggregating across modules |

**Decision proposed: packages.** They give an agent the same small area to read, and an architecture test already fails a
pull request as surely as a compile error does.

**Look at Gradle modules again when** a full compile passes about a minute, a boundary keeps being broken despite its rule,
or mobile or extensions need a part of the app built on its own.

## Target layout

```
app.notekea
├── core
│   └── notes        Shared by features: paths, note files, documents, links, frontmatter, and their adapters
├── shell            Assembles the window: panes, tabs, side panel, status bar, what follows what
├── editor           Text, properties, embeds, toolbar, outline
├── tree             Sidebar, bookmarks, daily notes, tags
├── search           Index, quick switcher, content search
├── connections      Related, Linked, Unlinked (joins `related`, `mentions`, `connections`)
├── templates
└── vault            As today
```

- **`shell`** is a new kind of package: like `main.kt`, it can depend on every feature, and nothing depends on it. This is
  the rule `ARCHITECTURE.md` §1 already gives, "it takes a composable slot and the app fills it", applied to what `tree`
  grew into.
- **`core.notes`** follows the same section: what two features share lives in `core`. It adds about 3,000 lines of
  domain and most of `data`, so it gets a rule of its own: no Compose and no feature imports.
- **New work starts as its own feature:** `chat`, `sync` and `sources`, never inside an existing one.

Which feature gets each domain file is an estimate from the script. It sees `data` only through Koin, so each pull
request confirms its own files.

## Pull requests, in order

Each only moves code and adds its rule, and no behavior changes. Each one runs `check` and the Self-Test.

| # | Pull request | Done when |
|---|---|---|
| 1 | Move the ten assembling files and `workspace` to `shell`; `FilePreview` moves to break the `tree`–`workspace` cycle | `tree` imports no other area |
| 2 | Move what features share to `core.notes` | Each remaining `domain` file has one owner |
| 3 | `connections` joins its three areas | The cycle is gone |
| 4 | `templates` | Rule added |
| 5 | `search` | Rule added |
| 6 | `editor`, with the side panel's shared parts moved to `shell` | Rule added |
| 7 | `tree`; `notes` disappears; `notesModule` becomes one Koin module per feature | `NON_FEATURE_PACKAGES` holds `shell`, and a rule keeps features from importing it |

Estimate, not measured: three to five working days, with PRs 1 and 2 taking the most care.

**Rules that change with it:**
- `ARCHITECTURE.md` §1 (the layout, `shell`).
- `ArchitectureTestBase` (`NON_FEATURE_PACKAGES`), with new rules for `shell` and `core.notes`.
- `compose-stability.conf` (audit 69), if its entries name classes that move.

## Risks

- **Conflicts:** moves conflict with anything open. Start once #114 is merged, and do not run this next to a feature that
  touches the same area. 2.6 Hybrid search can wait for PR 5 or go first.
- **A boundary only tested by Konsist** can be crossed until the test runs. The pre-commit hook and CI already run it.
- **`core` keeps growing:** at about 14k lines plus `core.notes`, the sharp edge is what enters it. A file enters only when
  a second feature needs it.
- **`NotesError` stays shared** at first. Splitting it can wait until a feature needs errors of its own, as the index does
  with `IndexError`.
- **Self-Test and tests:** 293 test files and `:selftest` have imports to change. The moves are mechanical but large, so
  review should look at renames, not at contents.

## Guides per feature

Not needed. A line per feature in `ARCHITECTURE.md` §1 is enough for an agent to find its way, and a guide per feature is
one more document to go stale: on 2026-11-03 a stale line in `ROADMAP.md` led an agent to build a question that had
already been decided against.

## Not covered

- Android: `androidMain` only moves with its features; nothing was built for it here.
- Whether `check` gets faster with modules.
- Splitting `core` itself.
