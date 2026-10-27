# Smoke test data

Ready-made data for the manual smoke tests (`GUIDELINES.md` §8, `MVP_SMOKE_TEST.md`). Every feature that needs
data to be smoke tested adds it here, in the same pull request, and lists it below. All content is fictional.

## Vaults (`vaults/`)

Open them with **Open folder…**. Notekea never writes inside a vault, so opening them leaves the repository clean.

| Vault | Notes counted | What it covers |
|---|---|---|
| `Personal Notes` | 12 | Nested folders (three levels in `Projects/Garden`), wiki links, tags (one nested), aliases, YAML front matter, tasks, a table, a non-ASCII file name (`Café con leche.md`), a folder name with a space; files that must **not** count as notes: `.obsidian/` and `.trash/` (hidden) and `attachments/shopping.txt` (shown in the tree as a file) |
| `Work Obsidian` | 6 | A second vault in Obsidian format, to switch between vaults and fill the recent list |
| `Empty Vault` | 0 | Shows "This folder has no notes yet" (it only holds a hidden `.gitkeep` so Git keeps the folder) |

`SmokeDataTest` (composeApp `jvmTest`) fails if these counts change, so this table stays true; `SmokeVaultTreeTest`
checks the tags the tree reads from `Personal Notes`.

## Scenarios Git cannot hold (`smoke.sh`, `smoke.ps1`)

Some cases cannot live in Git: a folder without read permission, a vault that moves, or a first launch with no
saved settings. The scripts build them in `smoke-data/scratch/` (ignored by Git), next to the sample vaults, so the
folder picker reaches them the same way. Not in `/tmp`: a sandboxed terminal and the native picker may each see a
different `/tmp`.

```bash
smoke-data/smoke.sh prepare           # Movable Vault, Locked Vault, New Vault Location, Playground Vault (all readable; its Recipes have distinct dates, its Editor folder has the editor's notes), Daily Vault, Templates Vault
smoke-data/smoke.sh move              # renames Movable Vault to Moved Vault, to test "Folder not found"
smoke-data/smoke.sh lock              # removes read permission from Locked Vault, to test the permission error
smoke-data/smoke.sh outside           # creates, edits and deletes notes in Playground Vault, as another app would
smoke-data/smoke.sh edit-note         # adds a line to Playground Vault/Editor/Shared note.md, as another app would
smoke-data/smoke.sh edit-note-soon    # the same after 3 seconds, so the change lands while you type in Notekea
smoke-data/smoke.sh delete-note       # deletes Playground Vault/Editor/Shared note.md, as another app would
smoke-data/smoke.sh check-note        # says whether Editor/Windows line endings.md kept its CR LF and frontmatter
smoke-data/smoke.sh large             # creates Large Vault: 5,000 notes in 50 folders, to time the tree
smoke-data/smoke.sh clear-cache       # deletes Notekea's note cache, so the next open is a full first scan
smoke-data/smoke.sh first-launch      # backs up Notekea's settings so the next start is a first launch
smoke-data/smoke.sh restore-settings  # puts the backed-up settings back
smoke-data/smoke.sh clean             # removes the scratch folder
```

On Windows: `powershell -ExecutionPolicy Bypass -File smoke-data\smoke.ps1 prepare` (same commands).
`SMOKE_SCRATCH=scratch-<name>` (`$env:SMOKE_SCRATCH` on Windows) builds the scenarios in `smoke-data/scratch-<name>/`
instead, also ignored by Git: the agent's pre-smoke uses `scratch-agent`, so it never changes the developer's
`scratch`. It accepts only that form of name, never a path, and does not change `clear-cache`, `first-launch` or
`restore-settings`, which always use the app's real data folder.
Native folder pickers refuse to select a folder they cannot read, so `Locked Vault` is opened first and locked
afterwards (`lock`), like a known vault whose permissions change. Run the scripts as a normal user: as root (or an
administrator) the locked folder stays readable.

## Data per feature

| Feature | Smoke cases | Data |
|---|---|---|
| 1.3 Open a vault | 1.3-01 … 1.3-10, 1.1-04, 1.2-04 | All three vaults; `prepare`, `move`, `lock`, `first-launch` |
| 1.4 Vault tree | 1.4-01 … 1.4-12, 1.3-02, 1.3-04, 1.3-07, 1.1-03 | `Personal Notes`, `Empty Vault`; `prepare` (Playground Vault, Movable Vault), `outside`, `large`, `clear-cache`, `move` |
| 1.5 Editor | 1.5-01 … 1.5-11, 1.4-01 | `prepare` (Playground Vault and its `Editor` folder), `edit-note`, `edit-note-soon`, `delete-note`, `check-note` |
| 1.6 Quick switcher and search | 1.6-01 … 1.6-12, 1.4-01, 1.5-01 | `prepare` (Playground Vault: Arepas' aliases `Arepa` and `Corn cakes`, `#recipe/drinks` in Café con leche, `#travel` in the journal and in Lisbon trip's frontmatter), `edit-note`, `large`; `SmokeQuickSwitcherTest` checks the expected results on `Personal Notes` |
| 1.7 Tasks and callouts | 1.7-01 … 1.7-06, 1.5-08 | `prepare` (Playground Vault: `Editor/Tasks and callouts`, copied from `smoke-data/notes/Tasks and callouts.md`, which `SmokeTasksAndCalloutsTest` also reads) |
| 1.17 Formatting toolbar | 1.17-01 … 1.17-11, 1.5-10 | `prepare` (Playground Vault: `Editor/Formatting practice`, plain text copied from `smoke-data/notes/Formatting practice.md`, which `SmokeFormattingToolbarTest` also reads; the tag picker lists the Playground Vault's tags, in the frontmatter and in the text, the Editor notes' included) |
| 1.8 Images and attachments | 1.8-01 … 1.8-13 | `prepare` (Playground Vault: `Editor/Images and files` from `smoke-data/notes/Images and files.md`, which embeds `Editor/Shopping basics` (from `smoke-data/notes/Shopping basics.md`: frontmatter, a Vegetables and a Dairy section), its files copied from `smoke-data/attachments/` into the vault's `attachments` folder: a 480 × 240 plate, a PDF and a `broken.png` that holds text; `smoke-data/outside/` copied to `scratch/Outside files` to locate, pick and drop). `SmokeAttachmentsTest` reads the same files |
| 1.9 Outline | 1.9-01 … 1.9-08 | `prepare` (Playground Vault: `Editor/Outline practice` from `smoke-data/notes/Outline practice.md`: frontmatter, long sections under headings of levels 1 to 4 (one skips a level, one has bold and a link, one has closing `###`), and a `#tag` line and a code block with `# a comment` that are not headings). `SmokeOutlineTest` reads the same file |
| 1.10 Daily notes | 1.10-01 … 1.10-06 | `prepare` (Playground Vault: `Journal` holds 2026-10-20, -21 and -22 and has no Obsidian settings, so the defaults apply; `Daily Vault` copied from `smoke-data/daily/`: `.obsidian/daily-notes.json` (folder `Diary`, format `YYYY/MMMM/dddd, MMMM Do YYYY`, template `Templates/Daily`), the template, two earlier days (Monday, October 19th and Thursday, October 22nd 2026) and `Diary/Ideas`, which is not a daily note). `SmokeDailyNotesTest` reads the same files |
| 1.11 Tabs and split panes | 1.11-01 … 1.11-15, 1.4-01, 1.4-02, 1.4-04, 1.4-11, 1.5-07, 1.6-01, 1.6-12, 1.8-12, 1.10-01 | `prepare` (Playground Vault: its `Editor` folder, whose `Images and files` note gives its card a picture, and its `attachments` folder for the grid; `Editor/Outline practice` to scroll; `Editor/Read mode` for a Mod+click on a link; `delete-note` removes a note while the app is closed). `SmokeTabsTest` reads the same files |
| 1.12 Templates | 1.12-01 … 1.12-07, 1.4-04, 1.17-01 | `prepare` (Playground Vault, which has no templates folder; `Templates Vault` copied from `smoke-data/templates/`: `.obsidian/templates.json` (folder `Snippets`, date `DD/MM/YYYY`, time `h:mm A`), the templates `Snippets/Meeting` (properties `tags: [meeting]`, `type: meeting`) and `Snippets/Work/Weekly review`, and `Projects/Kickoff notes` (properties `tags: [project]`, `status: active`) and the empty `Projects/Empty note` to insert them in). `SmokeTemplatesTest` reads the same files |
