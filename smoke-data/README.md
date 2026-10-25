# Smoke test data

Ready-made data for the manual smoke tests (`GUIDELINES.md` §8, `MVP_SMOKE_TEST.md`). Every feature that needs
data to be smoke tested adds it here, in the same pull request, and lists it below. All content is fictional.

## Vaults (`vaults/`)

Open them with **Open folder…**. Notekea never writes inside a vault, so opening them leaves the repository clean.

| Vault | Notes counted | What it covers |
|---|---|---|
| `Personal Notes` | 12 | Nested folders (three levels in `Projects/Garden`), wiki links, tags, YAML front matter, tasks, a table, a non-ASCII file name (`Café con leche.md`), a folder name with a space; files that must **not** count: `.obsidian/`, `.trash/` and `attachments/shopping.txt` |
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
smoke-data/smoke.sh prepare           # Movable Vault, Locked Vault, New Vault Location, Playground Vault (all readable; its Recipes have distinct dates, its Editor folder has the editor's notes)
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
Native folder pickers refuse to select a folder they cannot read, so `Locked Vault` is opened first and locked
afterwards (`lock`), like a known vault whose permissions change. Run the scripts as a normal user: as root (or an
administrator) the locked folder stays readable.

## Data per feature

| Feature | Smoke cases | Data |
|---|---|---|
| 1.3 Open a vault | 1.3-01 … 1.3-10, 1.1-04, 1.2-04 | All three vaults; `prepare`, `move`, `lock`, `first-launch` |
| 1.4 Vault tree | 1.4-01 … 1.4-12, 1.3-02, 1.3-04, 1.3-07, 1.1-03 | `Personal Notes`, `Empty Vault`; `prepare` (Playground Vault, Movable Vault), `outside`, `large`, `clear-cache`, `move` |
| 1.5 Editor | 1.5-01 … 1.5-11, 1.4-01 | `prepare` (Playground Vault and its `Editor` folder), `edit-note`, `edit-note-soon`, `delete-note`, `check-note` |
