# Notekea — MVP Smoke Test

The full manual test run before releasing v0.1.0 (`ROADMAP.md` 5.4). Unit tests and CI prove the parts work; this
list proves the **app** works for a real person on a real machine. It is the main source of bugs before release.

## How this file works

- **Every feature pull request adds its cases here**, under its milestone, in the same pull request
  (`GUIDELINES.md` §8). A case is something a person can do and see, written so that someone who never saw the
  feature can follow it.
- **Every feature pull request also revises the cases it makes outdated**, in place and keeping their IDs, so the
  file always describes the current app. A case that a later feature will change says so in a note under its table
  ("Revised by 1.4"); that feature's pull request rewrites the case and removes the note.
- Cases that depend on a feature not built yet say so in **Testable from**; they still go in now, so nothing is
  forgotten.
- Before the release, the whole list is run on **Windows, macOS and Linux** with a packaged build (not
  `./gradlew run`). Each OS column gets ✅, ❌ (with a link to the bug issue) or — (not applicable).
- Test data lives in `smoke-data/` (sample vaults plus `smoke.sh` / `smoke.ps1` for scenarios Git cannot hold);
  every case that needs data names it there.
- Bugs found are filed as issues, fixed on `fix/` branches, and the failing case is run again.

## Test environment (fill in for the release run)

| OS | Version | Desktop environment | Hardware (CPU / RAM / GPU) | Build | Tester | Date |
|---|---|---|---|---|---|---|
| Windows | | — | | | | |
| macOS | | — | | | | |
| Linux | | | | | | |

On Linux the folder picker is drawn by the desktop through the XDG portal (GNOME, KDE, COSMIC…), so its look,
shortcuts and rules differ between desktops; record the desktop environment. Pop!_OS with COSMIC was used while
building 1.3. Test data comes from `smoke-data/` (see its README); run its scripts from a normal user account.

## General

| ID | Steps | Expected | Testable from | Win | macOS | Linux |
|---|---|---|---|---|---|---|
| G-01 | Install the packaged app (`.msi` / `.dmg` / `.deb`) and launch it from the system launcher | The app opens without security prompts beyond the OS's normal first-launch confirmation | 5.3 | | | |
| G-02 | Launch the app from a terminal and read the output | No exceptions, no `WARNING: A restricted method` native-access warning | 1.1 | | | |
| G-03 | Close the main window | The process exits completely (no Java process left running) | 1.1 | | | |
| G-04 | Disconnect from the network and use the app (open vault, edit, related notes, chat with a downloaded model) | Everything works offline; only model download and Git sync report "offline" | 3.3 | | | |
| G-05 | Use the app for 10 minutes while watching outgoing connections (e.g. Little Snitch, Wireshark, `ss -tp`) | No network traffic except model download and Git sync started by the user | 3.3 | | | |
| G-06 | Switch the OS between light and dark mode while the app is open | The app follows the system theme (Linux: record whether the desktop environment's setting is detected; Compose support varies) | 1.2 | | | |

## M1 — Vault and editor

### 1.1 Platform conventions

| ID | Steps | Expected | Testable from | Win | macOS | Linux |
|---|---|---|---|---|---|---|
| 1.1-01 | Press the quit shortcut: Cmd+Q on macOS, Alt+F4 on Windows, Ctrl+Q on Linux | The app quits; on macOS the app menu shows "Quit Notekea  ⌘Q" | 5.2 (menus) | | | |
| 1.1-02 | Use any app shortcut (e.g. new note) | macOS uses Cmd, Windows and Linux use Ctrl; no shortcut needs the other OS's modifier | 1.3 | | | |
| 1.1-03 | Open any confirmation dialog (e.g. delete a note) | Windows: primary button first, then Cancel. macOS and Linux: Cancel first, then the primary button | 1.4 | | | |
| 1.1-04 | After the app has saved any setting, open the app data folder | It exists at `~/Library/Application Support/Notekea` (macOS), `%APPDATA%\Notekea` (Windows), `$XDG_DATA_HOME/notekea` or `~/.local/share/notekea` (Linux); nothing is written next to the executable or inside the vault | 1.3 | | | |
| 1.1-05 | Linux only: start the app with `XDG_DATA_HOME=/tmp/xdg-test` set | App data goes to `/tmp/xdg-test/notekea` | 1.3 | — | — | |

### 1.2 Theme and design system

| ID | Steps | Expected | Testable from | Win | macOS | Linux |
|---|---|---|---|---|---|---|
| 1.2-01 | Look at any screen text and at a code block | UI text uses Inter and code uses JetBrains Mono on every OS (not the system font) | 1.2 | | | |
| 1.2-02 | Compare the app in light and dark mode | Cobalt and tangerine palette: cobalt primary, tangerine accents (citations, badges); all text is readable, nothing low-contrast | 1.2 | | | |
| 1.2-03 | Check buttons, text fields and list items | Controls have small rounded corners (6 dp), not pill shapes; cards 10 dp | 1.2 | | | |
| 1.2-04 | Navigate the main window with Tab / Shift+Tab only | Every interactive element gets a visible focus ring and can be activated from the keyboard | 1.3 | | | |
| 1.2-05 | With a screen reader (Narrator, VoiceOver, Orca), move through a screen | Buttons, tabs and list items are announced with their text and role | 1.3 | | | |
| 1.2-06 | Run the packaged app | There is no way to open the design system gallery | 5.3 | | | |
| 1.2-07 | Make the window as narrow as possible | Status bar items and tab labels stay on one line and end with "…"; badge numbers never split; nothing is clipped | 1.2 | | | |

### 1.3 Open a vault

| ID | Steps | Expected | Testable from | Win | macOS | Linux |
|---|---|---|---|---|---|---|
| 1.3-01 | Run `smoke.sh first-launch` (`smoke.ps1 first-launch` on Windows), then start the app | Welcome screen: "Notekea" wordmark, tagline, "Open folder…" (with the Ctrl+O / ⌘O hint) and "Create new vault"; no recent list | 1.3 | | | |
| 1.3-02 | Click "Open folder…" and choose `smoke-data/vaults/Personal Notes` | The native folder picker opens; the vault opens as "Personal Notes" in the window title and the sidebar lists its 12 notes: Ideas (2), Journal (3), Projects (1), Recipes (3), Travel (2) and Welcome. `.obsidian`, `.trash` and `attachments/shopping.txt` are not shown (`attachments` appears as an empty folder) | 1.4 | | | |
| 1.3-03 | Quit and relaunch the app | The last vault reopens directly, without flashing the welcome screen | 1.3 | | | |
| 1.3-04 | Click "Open another vault" at the bottom of the sidebar, open `Work Obsidian` and `Empty Vault` ("This folder has no notes yet"), then check the recent list | Vaults appear most recent first with their folder (`~/…` on macOS and Linux) and when they were opened ("Just now", "2 days ago"…) | 1.3 | | | |
| 1.3-05 | Press Ctrl+O (⌘O on macOS) on the welcome screen and inside an open vault | The folder picker opens in both places, also right after clicking elsewhere or opening a vault; cancelling it changes nothing | 1.3 | | | |
| 1.3-06 | Run `smoke.sh prepare` (`smoke.ps1 prepare` on Windows), open `smoke-data/scratch/Movable Vault`, quit, run `smoke.sh move` (`smoke.ps1 move`) and relaunch | The vault is muted with "Folder not found", "Remove" and "Locate…"; "Locate…" lets you pick its new place and opens it; "Remove" only removes it from the list | 1.3 | | | |
| 1.3-07 | Click "Create new vault", keep the suggested name and location, and create it | A "My Notes" folder is created in Documents (or the home folder) and opens empty ("This folder has no notes yet" with a "New note" button); nothing else is written inside it | 1.4 | | | |
| 1.3-08 | Create "My Notes" in `New Vault Location` (from `prepare`), then a vault named `a/b` | Each problem is explained in plain language under the name field, keeping what you typed; typing again clears the message | 1.3 | | | |
| 1.3-09 | Open `smoke-data/scratch/Locked Vault` (from `prepare`), click "Open another vault", run `smoke.sh lock` (`smoke.ps1 lock` on Windows), then click "Locked Vault" in Recent | An error banner names the folder and says to check its permissions; "Dismiss" hides it. (Native pickers do not let you select an unreadable folder, so it is locked after it is known.) | 1.3 | | | |
| 1.3-10 | Click "Create new vault" and use only the keyboard | The name field has focus; Enter creates the vault, Esc closes the panel; while a vault opens, "Opening vault…" shows a spinner without moving the buttons | 1.3 | | | |

### 1.4 Vault tree and note list

Run `smoke.sh prepare` (`smoke.ps1 prepare` on Windows) first: cases that change notes use
`smoke-data/scratch/Playground Vault`, a copy of `Personal Notes`, so the repository stays clean. "Mod" is Cmd on macOS
and Ctrl on Windows and Linux.

| ID | Steps | Expected | Testable from | Win | macOS | Linux |
|---|---|---|---|---|---|---|
| 1.4-01 | Open `Personal Notes`, expand Projects and Garden, select "Tomato plan", then "Arepas" and the Recipes folder | Folders come first, each with its note count; the selected row is highlighted; a note opens in the editor with its folder, title, tags (`#recipe #venezuelan` for Arepas) and aliases above its text; a folder shows how many notes it holds | 1.4 | | | |
| 1.4-02 | Click the tree, then use only the keyboard: ↓ ↑, → on a folder (twice), ← (twice), Enter on a folder | ↓ ↑ move the selection; → opens a folder, then enters it; ← closes it, then goes to the parent; Enter opens or closes the selected folder; the selected row shows a focus ring and the list scrolls to keep it visible | 1.4 | | | |
| 1.4-03 | In `Playground Vault`, open Recipes, click the sort button in the sidebar header and choose "Date modified, newest first"; quit and relaunch; choose "Name" again | The button's tooltip names the current sort and the menu checks it; by date Recipes shows Pasta e fagioli, Arepas, Café con leche (`prepare` dates them), by name Arepas, Café con leche, Pasta e fagioli, folders always first by name; after relaunch the vault reopens with the same sort and the same folders open | 1.4 | | | |
| 1.4-04 | In `Playground Vault`, select Recipes and press Mod+N, then the "New folder" button, then right-click Travel and choose "New note"; then click the empty space below the last row and press Mod+N, and right-click that space and choose "New folder" | "Untitled", "Untitled folder" and "Untitled" are created in the selected folder (numbered when the name is taken), revealed and selected, with their name ready to type; a click on the empty space clears the selection, so the last two are created at the vault root | 1.4 | | | |
| 1.4-05 | Rename a note with F2 (Enter on macOS) and the context menu: type a new name with spaces (also press Space while the whole name is selected) and press Enter; rename another and press Esc; try `a/b`, `.hidden` and the name of a sibling | Spaces are typed like any letter and the field keeps focus; the file is renamed on disk without doubling `.md`; Esc keeps the old name; each refused name explains why under the field and keeps what was typed | 1.4 | | | |
| 1.4-06 | In `Playground Vault`, rename Recipes/Arepas to "Arepas reina" (confirm with Enter); then rename Journal/2026-10-22, which nothing links to | No question is asked: a notice says links were updated in 2 notes, and `Welcome.md` and `Journal/2026-10-20.md` contain `[[Arepas reina]]` (open them in a text editor) while their other links stay as they were; renaming the journal note shows no notice and changes no other file | 1.4 | | | |
| 1.4-07 | Delete a note with Delete (Mod+Backspace on macOS), cancel, delete again and confirm; then delete the Travel folder | The dialog names the note (a folder also says how many notes go with it), with Cancel and "Delete note" in the platform order (1.1-03); focus starts on Cancel, shown by a focus ring; the note goes to the system trash and can be restored from there (if the system trash is unavailable, a notice says it went to the vault's `.trash` folder) | 1.4 | | | |
| 1.4-08 | Right-click a note and choose "Show in Files" ("Show in Finder" on macOS, "Show in File Explorer" on Windows); also open the menu with Shift+F10 or the Menu key | The file manager opens with the note selected (on Linux at least its folder opens); the menu shows each action's shortcut; Shift+F10 / Menu opens the menu on the selected row | 1.4 | | | |
| 1.4-09 | With `Playground Vault` open, run `smoke.sh outside` (`smoke.ps1 outside`) | Within a second, without clicking anything: Inbox/Created outside appears, Ideas/Reading list disappears, and Lisbon trip shows the new `#edited` tag when selected | 1.4 | | | |
| 1.4-10 | Run `smoke.sh large` and `smoke.sh clear-cache` (`smoke.ps1 …`), open `smoke-data/scratch/Large Vault`, then quit and relaunch | The first open lists 5,000 notes in about a second (the sidebar shows a skeleton meanwhile); after relaunch the tree appears at once (under 1 s) | 1.4 | | | |
| 1.4-11 | Drag the sidebar edge to its narrowest and widest, press Mod+\ twice, use the "Show sidebar" button, then relaunch | The pointer becomes a resize cursor; the sidebar stays between 200 and 480 dp; Mod+\ slides it closed and open, and the button brings it back; width and visibility survive the relaunch | 1.4 | | | |
| 1.4-12 | Open `smoke-data/scratch/Movable Vault`, run `smoke.sh move` (`smoke.ps1 move`) while it is open and wait a few seconds without touching the app, then click "Try again" | Within about 2 seconds the sidebar explains that the vault folder cannot be found, with "Try again" and "Open another vault" ("Try again" keeps explaining it); "Show in Files" on a note from before the move says it cannot be found instead of opening the file manager; nothing crashes | 1.4 | | | |

### 1.5 Editor

Run `smoke.sh prepare` (`smoke.ps1 prepare` on Windows) and open `smoke-data/scratch/Playground Vault`: its `Editor`
folder holds the notes these cases use. "Mod" is Cmd on macOS and Ctrl on Windows and Linux. To look at a file as
stored, open it in a plain text editor (TextEdit, Notepad, gedit).

| ID | Steps | Expected | Testable from | Win | macOS | Linux |
|---|---|---|---|---|---|---|
| 1.5-01 | Open Recipes/Arepas, type a sentence at the end, stop typing and watch the header; then open the file in a text editor | "Unsaved" shows while typing and turns into "Saved", which fades after a second; the file has the sentence and its frontmatter (`tags: [recipe, venezuelan]`, `servings: 6`) exactly as before | 1.5 | | | |
| 1.5-02 | Type in a note and press Mod+S at once; type again and click another note in the tree; type again and close the window within a second | Mod+S shows "Saved" at once; the note clicked away from and the note open when the window closed both have every typed character after relaunching | 1.5 | | | |
| 1.5-03 | Open Editor/Windows line endings, add a line and wait for "Saved"; run `smoke.sh check-note` (`smoke.ps1 check-note`) | It prints "OK: every line ends with CR LF and the frontmatter is unchanged" | 1.5 | | | |
| 1.5-04 | Click the title of Recipes/Arepas, change it to "Arepas reina" and press Enter; then try a name with `/` and the name of Pasta e fagioli; press Esc | The file is renamed, the tree selects it, and a notice says links were updated in 2 notes; a refused name is explained under the title until it is edited; Esc puts the current name back | 1.5 | | | |
| 1.5-05 | Open Editor/Shared note without typing in it, then run `smoke.sh edit-note` (`smoke.ps1 edit-note`) | Within about a second the note shows the new last line, with no question | 1.5 | | | |
| 1.5-06 | Open Editor/Shared note, type a word, and before "Saved" appears run `smoke.sh edit-note`; choose "Keep mine"; repeat and choose "Load theirs" | A banner says the note changed outside Notekea; "Keep mine" writes your text over the change; "Load theirs" shows the file's text and drops your word; nothing is saved while the banner is shown | 1.5 | | | |
| 1.5-07 | Open Editor/Shared note, run `smoke.sh delete-note`, then click "Restore"; delete it again and click "Close" | A banner says the note was moved or deleted outside Notekea; "Restore" writes it back with what the editor shows; "Close" empties the editor and the tree no longer lists it | 1.5 | | | |
| 1.5-08 | Open Editor/Read mode in the editor | Headings are larger, bold and italic show as such, code is in the monospaced font on a shaded background (code blocks without colors), links and wiki links are blue, tags are orange, and Markdown symbols (`#`, `**`, `[[`) are muted | 1.5 | | | |
| 1.5-09 | In Editor/Read mode press Mod+F, type "arepas", press Enter, Shift+Enter and Esc; type in the note with the bar open | The bar shows "1 of N" and every match is highlighted, the current one stronger; Enter and Shift+Enter move and wrap around; Esc closes the bar with the match selected; typing updates the count | 1.5 | | | |
| 1.5-10 | In Editor/Read mode press Mod+E (or the book button), then Mod+E again | The note is shown rendered: headings, lists, checkboxes, the quote, and both code blocks with colors by language (Kotlin and Python); the button and Mod+E go back to the Markdown; another note opens in Edit mode | 1.5 | | | |
| 1.5-11 | In Read mode, click "Arepas", go back to Editor/Read mode, click "the coffee", then "A note that does not exist yet", then "Pasta e fagioli" and "the web" | Each wiki link opens its note and the tree selects it; the missing one creates "A note that does not exist yet" at the vault root and opens it; the Markdown link opens Pasta e fagioli; the web link opens the browser; `[[not a link inside code]]` is not a link | 1.5 | | | |
