# Notekea — MVP Smoke Test

The manual cases every milestone QA run executes on Windows, macOS and Linux (`qa/README.md`), and the full run before
releasing v0.1.0 (`ROADMAP.md` 5.4). Unit tests, UI tests and CI prove the parts work; this list proves the **app**
works for a real person on a real machine. It is the main source of bugs before release.

## How this file works

- **Every feature pull request adds its cases here**, under its milestone, in the same pull request
  (`GUIDELINES.md` §8). A case is something a person can do and see, written so that someone who never saw the
  feature can follow it.
- **Every feature pull request also revises the cases it makes outdated**, in place and keeping their IDs, so the
  file always describes the current app. A case that a later feature will change says so in a note under its table
  ("Revised by 1.4"); that feature's pull request rewrites the case and removes the note.
- **Automated** names the test that runs a case on every build (`EditorScreenTest`), so a case checked once keeps
  being checked as the app grows; "manual" marks what only a person can check (native pickers, real permissions,
  installers, the system clipboard), and empty means not automated yet. A feature that changes a case updates its
  test in the same pull request. Automated cases still run by hand in the QA runs, on packaged builds.
- Cases that depend on a feature not built yet say so in **Testable from**; they still go in now, so nothing is
  forgotten.
- At the end of every milestone, the cases testable so far (the "General" section and every milestone up to that
  one) are run on **Windows, macOS and Linux** with packaged builds, and the results go to `qa/runs/M<N>.md`. The
  M1 run is the first and covers everything since M0. Before the release, the whole list is run the same way and
  each OS column below gets ✅, ❌ (with a link to the bug issue) or — (not applicable).
- Test data lives in `smoke-data/` (sample vaults plus `smoke.sh` / `smoke.ps1` for scenarios Git cannot hold);
  every case that needs data names it there.
- Bugs found are filed as issues, fixed on `fix/` branches, and the failing case is run again.

## Test environment (fill in for the release run; milestone runs record theirs in `qa/runs/`)

| OS | Version | Desktop environment | Hardware (CPU / RAM / GPU) | Build | Tester | Date |
|---|---|---|---|---|---|---|
| Windows | | — | | | | |
| macOS | | — | | | | |
| Linux | | | | | | |

On Linux the folder picker is drawn by the desktop through the XDG portal (GNOME, KDE, COSMIC…), so its look,
shortcuts and rules differ between desktops; record the desktop environment. Pop!_OS with COSMIC was used while
building 1.3. Test data comes from `smoke-data/` (see its README); run its scripts from a normal user account.

## General

| ID | Steps | Expected | Testable from | Automated | Win | macOS | Linux |
|---|---|---|---|---|---|---|---|
| G-01 | Install the packaged app (`.msi` / `.dmg` / `.deb`) and launch it from the system launcher | The app opens without security prompts beyond the OS's normal first-launch confirmation | 5.3 | | | | |
| G-02 | Launch the app from a terminal and read the output | No exceptions, no `WARNING: A restricted method` native-access warning | 1.1 | | | | |
| G-03 | Close the main window | The process exits completely (no Java process left running) | 1.1 | | | | |
| G-04 | Disconnect from the network and use the app (open vault, edit, related notes, chat with a downloaded model) | Everything works offline; only model download and Git sync report "offline" | 3.3 | | | | |
| G-05 | Use the app for 10 minutes while watching outgoing connections (e.g. Little Snitch, Wireshark, `ss -tp`) | No network traffic except model download and Git sync started by the user | 3.3 | | | | |
| G-06 | Switch the OS between light and dark mode while the app is open | The app follows the system theme at once, on Linux too (GNOME, KDE and COSMIC tell it through the desktop portal) | 1.17 | `PortalSystemAppearanceTest` | | | |
| G-07 | Start the app with `NOTEKEA_CRASH_TEST=1` (macOS and Linux: `NOTEKEA_CRASH_TEST=1 ./gradlew :composeApp:run`; Windows PowerShell: `$env:NOTEKEA_CRASH_TEST="1"; .\gradlew.bat :composeApp:run`), open a note and type a word, then wait | About 3 seconds after the window opens, a dialog window "Notekea needs to close" in the app's style and light or dark theme, with crisp text, names the report file in the app data folder (`crashes/crash-<date>.txt`); Copy details puts the report on the clipboard and says "Copied to the clipboard" (it starts with "Notekea crash report" and has no note text); Report on GitHub opens the Crash report form; Close Notekea closes the app and the word typed is saved | 1.6 | `CrashReporterTest`, `CrashNoticeContentTest`, `RecordCrashUseCaseTest`, `CrashReportOnDiskTest` | | | |

## M1 — Vault and editor

### 1.1 Platform conventions

| ID | Steps | Expected | Testable from | Automated | Win | macOS | Linux |
|---|---|---|---|---|---|---|---|
| 1.1-01 | Press the quit shortcut: Cmd+Q on macOS, Alt+F4 on Windows, Ctrl+Q on Linux | The app quits; on macOS the app menu shows "Quit Notekea  ⌘Q" | 5.2 (menus) | | | | |
| 1.1-02 | Use any app shortcut (e.g. new note) | macOS uses Cmd, Windows and Linux use Ctrl; no shortcut needs the other OS's modifier | 1.3 | | | | |
| 1.1-03 | Open any confirmation dialog (e.g. delete a note) | Windows: primary button first, then Cancel. macOS and Linux: Cancel first, then the primary button | 1.4 | | | | |
| 1.1-04 | After the app has saved any setting, open the app data folder | It exists at `~/Library/Application Support/Notekea` (macOS), `%APPDATA%\Notekea` (Windows), `$XDG_DATA_HOME/notekea` or `~/.local/share/notekea` (Linux); nothing is written next to the executable or inside the vault | 1.3 | | | | |
| 1.1-05 | Linux only: start the app with `XDG_DATA_HOME=/tmp/xdg-test` set | App data goes to `/tmp/xdg-test/notekea` | 1.3 | | — | — | |

### 1.2 Theme and design system

| ID | Steps | Expected | Testable from | Automated | Win | macOS | Linux |
|---|---|---|---|---|---|---|---|
| 1.2-01 | Look at any screen text and at a code block | UI text uses Inter and code uses JetBrains Mono on every OS (not the system font) | 1.2 | | | | |
| 1.2-02 | Compare the app in light and dark mode | Cobalt and tangerine palette: cobalt primary, tangerine accents (citations, badges); all text is readable, nothing low-contrast | 1.2 | | | | |
| 1.2-03 | Check buttons, text fields and list items | Controls have small rounded corners (6 dp), not pill shapes; cards 10 dp | 1.2 | | | | |
| 1.2-04 | Navigate the main window with Tab / Shift+Tab only | Every interactive element gets a visible focus ring and can be activated from the keyboard | 1.3 | | | | |
| 1.2-05 | With a screen reader (Narrator, VoiceOver, Orca), move through a screen | Buttons, tabs and list items are announced with their text and role | 1.3 | | | | |
| 1.2-06 | Run the packaged app | There is no way to open the design system gallery | 5.3 | | | | |
| 1.2-07 | Make the window as narrow as possible | Status bar items and tab labels stay on one line and end with "…"; badge numbers never split; nothing is clipped | 1.2 | | | | |

### 1.3 Open a vault

| ID | Steps | Expected | Testable from | Automated | Win | macOS | Linux |
|---|---|---|---|---|---|---|---|
| 1.3-01 | Run `smoke.sh first-launch` (`smoke.ps1 first-launch` on Windows), then start the app | Welcome screen: "Notekea" wordmark, tagline, "Open folder…" (with the Ctrl+O / ⌘O hint) and "Create new vault"; no recent list | 1.3 | | | | |
| 1.3-02 | Click "Open folder…" and choose `smoke-data/vaults/Personal Notes` | The native folder picker opens; the vault opens as "Personal Notes" in the window title and the sidebar lists its 12 notes: Ideas (2), Journal (3), Projects (1), Recipes (3), Travel (2) and Welcome. `.obsidian`, `.trash` and `attachments/shopping.txt` are not shown (`attachments` appears as an empty folder) | 1.4 | | | | |
| 1.3-03 | Quit and relaunch the app | The last vault reopens directly, without flashing the welcome screen | 1.3 | | | | |
| 1.3-04 | Click "Open another vault" at the bottom of the sidebar, open `Work Obsidian` and `Empty Vault` ("This folder has no notes yet"), then check the recent list | Vaults appear most recent first with their folder (`~/…` on macOS and Linux) and when they were opened ("Just now", "2 days ago"…) | 1.3 | | | | |
| 1.3-05 | Press Ctrl+O (⌘O on macOS) on the welcome screen and inside an open vault | The folder picker opens in both places, also right after clicking elsewhere or opening a vault; cancelling it changes nothing | 1.3 | | | | |
| 1.3-06 | Run `smoke.sh prepare` (`smoke.ps1 prepare` on Windows), open `smoke-data/scratch/Movable Vault`, quit, run `smoke.sh move` (`smoke.ps1 move`) and relaunch | The vault is muted with "Folder not found", "Remove" and "Locate…"; "Locate…" lets you pick its new place and opens it; "Remove" only removes it from the list | 1.3 | | | | |
| 1.3-07 | Click "Create new vault", keep the suggested name and location, and create it | A "My Notes" folder is created in Documents (or the home folder) and opens empty ("This folder has no notes yet" with a "New note" button); nothing else is written inside it | 1.4 | | | | |
| 1.3-08 | Create "My Notes" in `New Vault Location` (from `prepare`), then a vault named `a/b` | Each problem is explained in plain language under the name field, keeping what you typed; typing again clears the message | 1.3 | | | | |
| 1.3-09 | Open `smoke-data/scratch/Locked Vault` (from `prepare`), click "Open another vault", run `smoke.sh lock` (`smoke.ps1 lock` on Windows), then click "Locked Vault" in Recent | An error banner names the folder and says to check its permissions; "Dismiss" hides it. (Native pickers do not let you select an unreadable folder, so it is locked after it is known.) | 1.3 | | | | |
| 1.3-10 | Click "Create new vault" and use only the keyboard | The name field has focus; Enter creates the vault, Esc closes the panel; while a vault opens, "Opening vault…" shows a spinner without moving the buttons | 1.3 | | | | |

### 1.4 Vault tree and note list

Run `smoke.sh prepare` (`smoke.ps1 prepare` on Windows) first: cases that change notes use
`smoke-data/scratch/Playground Vault`, a copy of `Personal Notes`, so the repository stays clean. "Mod" is Cmd on macOS
and Ctrl on Windows and Linux.

| ID | Steps | Expected | Testable from | Automated | Win | macOS | Linux |
|---|---|---|---|---|---|---|---|
| 1.4-01 | Open `Personal Notes`, expand Projects and Garden, select "Tomato plan", then "Arepas" and the Recipes folder | Folders come first, each with its note count; the selected row is highlighted; a note opens in the editor with its folder, title, tags (`#recipe #venezuelan` for Arepas) and aliases (`Arepa`, `Corn cakes`) above its text; a folder shows how many notes it holds | 1.4 | | | | |
| 1.4-02 | Click the tree, then use only the keyboard: ↓ ↑, → on a folder (twice), ← (twice), Enter on a folder | ↓ ↑ move the selection; → opens a folder, then enters it; ← closes it, then goes to the parent; Enter opens or closes the selected folder; the selected row shows a focus ring and the list scrolls to keep it visible | 1.4 | | | | |
| 1.4-03 | In `Playground Vault`, open Recipes, click the sort button in the sidebar header and choose "Date modified, newest first"; quit and relaunch; choose "Name" again | The button's tooltip names the current sort and the menu checks it; by date Recipes shows Pasta e fagioli, Arepas, Café con leche (`prepare` dates them), by name Arepas, Café con leche, Pasta e fagioli, folders always first by name; after relaunch the vault reopens with the same sort and the same folders open | 1.4 | | | | |
| 1.4-04 | In `Playground Vault`, select Recipes and press Mod+N, then the "New folder" button, then right-click Travel and choose "New note"; then click the empty space below the last row and press Mod+N, and right-click that space and choose "New folder" | "Untitled", "Untitled folder" and "Untitled" are created in the selected folder (numbered when the name is taken), revealed and selected, with their name ready to type; a click on the empty space clears the selection, so the last two are created at the vault root | 1.4 | | | | |
| 1.4-05 | Rename a note with F2 (Enter on macOS) and the context menu: type a new name with spaces (also press Space while the whole name is selected) and press Enter; rename another and press Esc; try `a/b`, `.hidden` and the name of a sibling | Spaces are typed like any letter and the field keeps focus; the file is renamed on disk without doubling `.md`; Esc keeps the old name; each refused name explains why under the field and keeps what was typed | 1.4 | | | | |
| 1.4-06 | In `Playground Vault`, rename Recipes/Arepas to "Arepas reina" (confirm with Enter); then rename Journal/2026-10-22, which nothing links to | No question is asked: a notice says links were updated in 3 notes, and `Welcome.md`, `Journal/2026-10-20.md` and `Editor/Read mode.md` contain `[[Arepas reina]]` (open them in a text editor) while their other links stay as they were; renaming the journal note shows no notice and changes no other file | 1.4 | | | | |
| 1.4-07 | Delete a note with Delete (Mod+Backspace on macOS), cancel, delete again and confirm; then delete the Travel folder | The dialog names the note (a folder also says how many notes go with it), with Cancel and "Delete note" in the platform order (1.1-03); focus starts on Cancel, shown by a focus ring; the note goes to the system trash and can be restored from there (if the system trash is unavailable, a notice says it went to the vault's `.trash` folder) | 1.4 | | | | |
| 1.4-08 | Right-click a note and choose "Show in Files" ("Show in Finder" on macOS, "Show in File Explorer" on Windows); also open the menu with Shift+F10 or the Menu key | The file manager opens with the note selected (on Linux at least its folder opens); the menu shows each action's shortcut; Shift+F10 / Menu opens the menu on the selected row | 1.4 | | | | |
| 1.4-09 | With `Playground Vault` open, run `smoke.sh outside` (`smoke.ps1 outside`) | Within a second, without clicking anything: Inbox/Created outside appears, Ideas/Reading list disappears, and Lisbon trip shows the new `#edited` tag when selected | 1.4 | | | | |
| 1.4-10 | Run `smoke.sh large` and `smoke.sh clear-cache` (`smoke.ps1 …`), open `smoke-data/scratch/Large Vault`, then quit and relaunch | The first open lists 5,000 notes in about a second (the sidebar shows a skeleton meanwhile); after relaunch the tree appears at once (under 1 s) | 1.4 | | | | |
| 1.4-11 | Drag the sidebar edge to its narrowest and widest, press Mod+\ twice, use the "Show sidebar" button, then relaunch | The pointer becomes a resize cursor; the sidebar stays between 200 and 480 dp; Mod+\ slides it closed and open, and the button brings it back; width and visibility survive the relaunch | 1.4 | | | | |
| 1.4-12 | Open `smoke-data/scratch/Movable Vault`, run `smoke.sh move` (`smoke.ps1 move`) while it is open and wait a few seconds without touching the app, then click "Try again" | Within about 2 seconds the sidebar explains that the vault folder cannot be found, with "Try again" and "Open another vault" ("Try again" keeps explaining it); "Show in Files" on a note from before the move says it cannot be found instead of opening the file manager; nothing crashes | 1.4 | | | | |

### 1.5 Editor

Run `smoke.sh prepare` (`smoke.ps1 prepare` on Windows) and open `smoke-data/scratch/Playground Vault`: its `Editor`
folder holds the notes these cases use. "Mod" is Cmd on macOS and Ctrl on Windows and Linux. To look at a file as
stored, open it in a plain text editor (TextEdit, Notepad, gedit).

| ID | Steps | Expected | Testable from | Automated | Win | macOS | Linux |
|---|---|---|---|---|---|---|---|
| 1.5-01 | Open Recipes/Arepas, type a sentence at the end, stop typing and watch the header; then open the file in a text editor | "Unsaved" shows while typing and turns into "Saved", which fades after a second; the file has the sentence and its frontmatter (`tags: [recipe, venezuelan]`, `aliases: [Arepa, Corn cakes]`, `servings: 6`) exactly as before | 1.5 | | | | |
| 1.5-02 | Type in a note and press Mod+S at once; type again and click another note in the tree; type again and close the window within a second | Mod+S shows "Saved" at once; the note clicked away from and the note open when the window closed both have every typed character after relaunching | 1.5 | | | | |
| 1.5-03 | Open Editor/Windows line endings, add a line and wait for "Saved"; run `smoke.sh check-note` (`smoke.ps1 check-note`) | It prints "OK: every line ends with CR LF and the frontmatter is unchanged" | 1.5 | | | | |
| 1.5-04 | Click the title of Recipes/Arepas, change it to "Arepas reina" and press Enter; then try a name with `/` and the name of Pasta e fagioli; press Esc | The file is renamed, the tree selects it, and a notice says "Links updated in 3 notes: 2026-10-20, Read mode, Welcome." and leaves by itself after about 10 seconds; a refused name is explained under the title until it is edited; Esc puts the current name back | 1.5 | | | | |
| 1.5-05 | Open Editor/Shared note without typing in it, then run `smoke.sh edit-note` (`smoke.ps1 edit-note`) | Within about a second the note shows the new last line, with no question | 1.5 | | | | |
| 1.5-06 | Open Editor/Shared note; run `smoke.sh edit-note-soon` (`smoke.ps1 edit-note-soon`), go back to Notekea within 3 seconds and keep typing in the note until a banner appears; choose "Keep mine"; repeat and choose "Load theirs" | A banner says the note changed outside Notekea; "Keep mine" writes your text over the change; "Load theirs" shows the file's text and drops what you typed; nothing is saved while the banner is shown. (Switching to the terminal makes the editor save, which is why the change must land while you type.) | 1.5 | | | | |
| 1.5-07 | Open Editor/Shared note, run `smoke.sh delete-note`, then click "Restore"; delete it again and click "Close" | A banner says the note was moved or deleted outside Notekea; "Restore" writes it back with what the editor shows; "Close" empties the editor and the tree no longer lists it | 1.5 | | | | |
| 1.5-08 | Open Editor/Read mode in the editor | Headings are larger, bold and italic show as such, code is in the monospaced font on a shaded background (code blocks without colors), links and wiki links are blue, tags are orange, Markdown symbols (`#`, `**`, `[[`) are muted, and the two tasks show a checkbox over their brackets (1.7) | 1.5 | | | | |
| 1.5-09 | In Editor/Read mode press Mod+F, type "arepas", press Enter, Shift+Enter and Esc; type in the note with the bar open | The bar shows "1 of N" and every match is highlighted, the current one stronger; Enter and Shift+Enter move and wrap around; Esc closes the bar with the match selected; typing updates the count | 1.5 | | | | |
| 1.5-10 | In Editor/Read mode press Mod+E (or click the Preview tab under the title), open another note, quit and relaunch; then press Mod+E again | The note is shown rendered: headings, lists, checkboxes, the quote, and both code blocks with colors by language (Kotlin and Python); the other note and the relaunched app open in Read mode too, since the last chosen mode is remembered; Mod+E goes back to the Markdown, which is then remembered | 1.5 | | | | |
| 1.5-11 | In Read mode, click "Arepas", go back to Editor/Read mode, click "the coffee", then "A note that does not exist yet", then "Pasta e fagioli" and "the web" | Each wiki link opens its note and the tree selects it; the missing one creates "A note that does not exist yet" at the vault root and opens it; the Markdown link opens Pasta e fagioli; the web link opens the browser; `[[not a link inside code]]` is not a link | 1.5 | | | | |

### 1.6 Quick switcher and search

Run `smoke.sh prepare` (`smoke.ps1 prepare` on Windows) and open `smoke-data/scratch/Playground Vault`, a copy of
`Personal Notes` plus the editor's notes. "Mod" is Cmd on macOS and Ctrl on Windows and Linux; Mod+P is ⌘P or Ctrl+P,
Mod+Shift+F is ⇧⌘F or Ctrl+Shift+F. The names in **Automated** are the tests that check the same results on every
build (`SmokeQuickSwitcherTest` runs on the smoke vault itself).

| ID | Steps | Expected | Testable from | Automated | Win | macOS | Linux |
|---|---|---|---|---|---|---|---|
| 1.6-01 | Press Mod+P without typing, then type `are` and press Enter | The palette opens over the window with the most recently changed notes; typing puts "Arepas" first with "Are" in bold; Enter closes the palette, opens Arepas in the editor and the tree selects it with Recipes expanded | 1.6 | `SmokeQuickSwitcherTest` 1_6-01 | | | |
| 1.6-02 | Press Mod+P and type `corn cakes`; clear the field and type `cafe` | "Arepas" is first with a second line "Corn cakes" (its alias) highlighted; `cafe` finds "Café con leche" although it has an accent | 1.6 | `SmokeQuickSwitcherTest` 1_6-02 | | | |
| 1.6-03 | Press Mod+P and type `gard/tom` | "Tomato plan" is first, with the line "Projects/Garden/Tomato plan" under it | 1.6 | `SmokeQuickSwitcherTest` 1_6-03 | | | |
| 1.6-04 | Press Mod+Shift+F and type `tomato` | The Content tab is selected; after a short pause it lists "2026-10-20" first (two matches, line "Planted the last tomato seedlings…"), then "Pasta e fagioli" ("Add tomatoes…") and "Tomato plan", each line with the word in bold and read as plain text ("notes in Tomato plan", no `[[`, `**`, `#` or `-` marks) | 1.6 | `SmokeQuickSwitcherTest` 1_6-04 | | | |
| 1.6-05 | In the Content tab type `#recipe` | "Arepas" (tagged in its frontmatter, no line under it), "Café con leche" (line `#recipe/drinks`) and "Pasta e fagioli" (line `#recipe`); no other note | 1.6 | `SmokeQuickSwitcherTest` 1_6-05 | | | |
| 1.6-06 | Press Mod+P, type `a`, press Tab, Tab again, Down three times and Up four times; then Esc; open it again and click outside the card | Tab switches between Notes and Content keeping `a` in the field; the selection moves and wraps from the first result to the last; Esc and the click outside both close the palette and the note shown does not change | 1.6 | `QuickSwitcherViewModelTest`, `QuickSwitcherContentTest` | | | |
| 1.6-07 | Open Journal/2026-10-21, switch to Read mode (Mod+E) and click `#travel` | `#travel` is blue like a link; the click opens the Content tab with `#travel` typed, listing "2026-10-21" (line `#journal #travel`) and "Lisbon trip" (tagged in its frontmatter) | 1.6 | `ReadModeTagClickTest`, `SmokeQuickSwitcherTest` 1_6-07 | | | |
| 1.6-08 | Press Mod+P and type `zzz`; press Tab and clear the field | "No notes match" with a hint to press Tab; with Content selected and the field empty it explains that words or a `#tag` can be typed | 1.6 | `QuickSwitcherContentTest` | | | |
| 1.6-09 | Run `smoke.sh edit-note` (`smoke.ps1 edit-note`), then press Mod+Shift+F and type `edited outside` | "Shared note" is listed with the line "Edited outside Notekea at …" just added by the script | 1.6 | `ContentSearchOnDiskTest` | | | |
| 1.6-10 | Run `smoke.sh large` (`smoke.ps1 large`), open `Large Vault`, press Mod+P and type `note 07 042`; then press Mod+Shift+F and type `see` | Names filter as fast as you type and "Note 07 042" ends up first; the first content search over the 5,000 notes shows its results within about two seconds, and searching again is immediate | 1.6 | manual (timing) | | | |
| 1.6-11 | In Edit mode, open Recipes/Arepas and click in its text; press Mod+P, then Esc, and type a word; press Mod+P, type `pasta`, press Enter and type another word | Without clicking, the first word goes into Arepas and the second into Pasta e fagioli, each at its cursor | 1.6 | `EditorViewModelTest`, `QuickSwitcherViewModelTest`, `EditorTextFocusTest` | | | |
| 1.6-12 | Look at the top of the sidebar, then click "Search notes" | A field-like button under the vault name shows a magnifier, "Search notes" and the shortcut (⌘P on macOS, Ctrl+P elsewhere); clicking it opens the palette on the Notes tab, as Mod+P does | 1.6 | `SidebarSearchButtonTest` | | | |

### 1.7 Tasks and callouts

Run `smoke.sh prepare` (`smoke.ps1 prepare` on Windows) and open `smoke-data/scratch/Playground Vault`: its
`Editor/Tasks and callouts` note holds every case (the same file as `smoke-data/notes/Tasks and callouts.md`). "Mod" is
Cmd on macOS and Ctrl on Windows and Linux; Mod+Enter is ⌘↩ or Ctrl+Enter.

| ID | Steps | Expected | Testable from | Automated | Win | macOS | Linux |
|---|---|---|---|---|---|---|---|
| 1.7-01 | Open Editor/Tasks and callouts in Read mode (Mod+E) and look at the Tasks section | Nine tasks show as checkboxes, "Arepas on Monday" and "Pasta e fagioli on Tuesday" indented; four are checked: "Soak the beans", "Pasta e fagioli on Tuesday", "A numbered task done with a capital X" and, in the To do callout, "A finished task inside a callout" | 1.7 | `SmokeTasksAndCalloutsTest` 1_7-01 | | | |
| 1.7-02 | Switch to Edit mode, click the checkbox of "Buy corn flour", wait for "Saved", then open the file in a text editor; click the checkbox again | A checkbox covers every `[ ]` / `[x]` (not inside code); the click checks it at once, the header shows "Saved", and the file reads `- [x] Buy corn flour`; the second click unchecks it (`- [ ]`) | 1.7 | `EditorTaskCheckboxTest`, `EditorViewModelTest` | | | |
| 1.7-03 | In Edit mode, click in the text of "Arepas on Monday" and press Mod+Enter twice; then click in the "## Tasks" heading and press Mod+Enter | The task is checked, then unchecked again, without clicking its checkbox; on the heading nothing changes | 1.7 | `EditorViewModelTest` | | | |
| 1.7-04 | In Read mode, scroll through the Callouts and Aliases sections | Each callout is a card with its icon and title: "Note" (titled by its type), Abstract, Info, To do, Tip, Success, Question and Example in blue; Warning and "Caution is a warning" in orange; Failure, Danger and Bug in red; Quote in gray; "TL;DR is an abstract" like Abstract and "An unknown type is a note" like Note; no `[!` text is visible | 1.7 | `SmokeTasksAndCalloutsTest` 1_7-04, `NotekeaMarkdownCalloutTest` | | | |
| 1.7-05 | In the Folding section, click the title of "A folded question (click to open)" twice, then the title of "An open tip (click to fold)" | The folded question starts closed with a chevron and opens and closes with each click (the title is not selected); the open tip starts open and folds | 1.7 | `NotekeaMarkdownCalloutTest` | | | |
| 1.7-06 | Look at the Nesting section and the Example callout | "Level one" holds "Level two", which holds "Level three", each with its own color; the Example callout shows `val servings = 6` as colored code without `>` marks; the last quote is a plain quote with a bar | 1.7 | `NotekeaMarkdownCalloutTest`, `ReadingMarkdownTest` | | | |

### 1.17 Formatting toolbar

Run `smoke.sh prepare` (`smoke.ps1 prepare` on Windows) and open `smoke-data/scratch/Playground Vault`: its
`Editor/Formatting practice` note is plain text to format with the toolbar alone (the same file as
`smoke-data/notes/Formatting practice.md`). Run the cases in order, on the same note. "Mod" is Cmd on macOS and Ctrl
on Windows and Linux: Mod+B is ⌘B or Ctrl+B, Mod+Shift+K is ⇧⌘K or Ctrl+Shift+K.

| ID | Steps | Expected | Testable from | Automated | Win | macOS | Linux |
|---|---|---|---|---|---|---|---|
| 1.17-01 | Open Editor/Formatting practice in Edit mode and hover each button of the bar under the title; click Preview, then press Mod+E | Under the title: the Edit and Preview tabs (the selected one underlined with square ends), then Heading, Bold, Italic · Link to a note, Web link, Tag · Bulleted list, Numbered list, Checklist · Callout, Code block, More · Find in note; each tooltip names the action in plain words with its shortcut where it has one (Bold Mod+B, Italic Mod+I, Link to a note Mod+K, Web link Mod+Shift+K, Find in note Mod+F, the tabs Mod+E), and Callout explains itself ("a colored box for a tip, a warning…"); Preview shows the note rendered with only the tabs and Find in note; Mod+E goes back to Edit | 1.17 | `EditorToolbarTest` | | | |
| 1.17-02 | Click in "Weekend arepas" and choose Heading › Large heading, then Heading › Large heading again, then Heading › Medium heading; select "corn flour" and click Bold twice; select "fresh" and press Mod+I; click inside the word "beans" and press Mod+B twice; click at the end of the first line and, with nothing selected, click Bold and type `soft` | The line becomes `# Weekend arepas` (large), then plain again, then `## Weekend arepas`; `**corn flour**` shows bold, then plain again; `*fresh*` shows italic; with the cursor in a word, Mod+B makes `**beans**` and takes it off again; at the end of the line Bold inserts `**bold text**` with "bold text" selected, and typing replaces it: `**soft**` | 1.17 | `SmokeFormattingToolbarTest` 1_17-02, `MarkdownFormatterTest` | | | |
| 1.17-03 | Select the three lines from "Buy corn flour" to "Soak the beans" with the mouse and click Checklist, Numbered list, Bulleted list and Checklist again without selecting again; click the checkbox of "Soak the beans" | Every click changes all three lines and the three stay selected: a checklist (`- [ ] `, with checkboxes), then `1.` `2.` `3.`, then `- ` bullets, then a checklist again; the click checks "Soak the beans" (`- [x] Soak the beans`) | 1.17 | `SmokeFormattingToolbarTest` 1_17-03, `EditorToolbarTest` | | | |
| 1.17-04 | Select "coffee" and click Link to a note; type `cafe` and press Enter; then press Mod+K and Esc and type a word; switch to Preview and click "coffee" | The palette says "Choose a note to link" and has no Notes / Content tabs; Enter writes `[[Café con leche\|coffee]]` and the cursor follows it; after Esc nothing is inserted and the word goes into the note at the cursor; in Preview "coffee" is a link that opens Café con leche | 1.17 | `SmokeFormattingToolbarTest` 1_17-04, `QuickSwitcherViewModelTest`, `EditorViewModelTest` | | | |
| 1.17-05 | Back in Edit, select "hot pan", press Mod+Shift+K and type `https://example.com`; click at the end of the "## Weekend arepas" line and click Tag; type `rec`, press Enter; click Tag again, type `weekend` and press Enter; wait for "Saved", click Tag, type `week`, then press Esc and type a word | Web link writes `[hot pan](https://)` with `https://` selected, so typing completes `[hot pan](https://example.com)`; Tag opens "Find or create a tag" listing the vault's tags, the most used first: #journal, #recipe, #smoke, #travel, #crlf, #editor, #ideas, #index; `rec` leaves #recipe, #recipe/drinks and "New tag: #rec", and Enter adds #recipe; `weekend` offers only "New tag: #weekend"; the line ends up `## Weekend arepas #recipe #weekend`; once saved, `week` finds #weekend as a tag of the vault; Esc closes the picker and the word goes into the note at the cursor | 1.17 | `SmokeFormattingToolbarTest` 1_17-05, `TagPickerViewModelTest`, `TagSearchTest`, `TagRankingTest` | | | |
| 1.17-06 | Select the line "Remember that the pan stays hot for a long time.", open Callout, check Start folded and choose Warning; click in the empty last line, open Callout (Start folded is still checked: uncheck it) and choose Tip, then type `Use a cast-iron pan`; switch to Preview and click the Warning title | The menu lists every callout type with its icon, with "Start folded" first and explained under it ("Shows only the title until it is clicked"); it stays open when clicked and shows its check; the selected line stays selected and becomes `> [!warning]- Warning` / `> Remember that the pan stays hot for a long time.`; Tip inserts `> [!tip] Tip` / `> Write here` with "Write here" selected, replaced by the typed text; in Preview the Warning is an orange card that starts folded and opens on click, and the Tip is a blue card | 1.17 | `SmokeFormattingToolbarTest` 1_17-06, `EditorToolbarTest`, `NotekeaMarkdownCalloutTest` | | | |
| 1.17-07 | In Edit, click in the empty last line, open More and point at Table: move over the grid and click the cell of 3 columns × 2 rows; click in the empty last line again and choose Code block › Kotlin; then More › Divider and More › Quote, each from the empty last line; switch to Preview | Table opens a grid beside the menu that fills as the pointer moves and says "3 × 2 table"; the click inserts a table with the headers Column 1, Column 2, Column 3 and two rows of "Text", "Column 1" selected to type over; Code block lists Plain text and Python, JavaScript, TypeScript, Java, Kotlin, C#, C++, Go, Rust, Shell, and Kotlin writes ```` ```kotlin ```` with "code" selected; Divider writes `---` and Quote `> Quote` on lines of their own; Preview shows a three-column table, the Kotlin code colored, a line and a quote | 1.17 | `SmokeFormattingToolbarTest` 1_17-07, `EditorToolbarTest` | | | |
| 1.17-08 | Open Editor/Tasks and callouts, click in a line in the middle of the screen and type a word; scroll down; click Preview, then press Mod+E twice; make the window narrow until the bar no longer fits on one line | Clicking and typing never move the note (the title stays in view); the bar scrolls up with the title, then stays at the top of the editor over the text, and its buttons still work there; no focus ring is left on the Edit or Preview tab, whichever way the mode changes; in a narrow window the formatting buttons move to a line of their own above the tabs | 1.17 | `EditorToolbarTest` | | | |
| 1.17-09 | In Editor/Formatting practice press Mod+Z once, then Mod+Shift+Z; wait for "Saved" and, if Obsidian is installed, open the Playground Vault there | Mod+Z undoes the last toolbar change in one step and Mod+Shift+Z redoes it; Obsidian shows the same heading, bold and italic, checklist, links, tags, callouts (the Warning folded), table, code block and quote as Preview | 1.17 | manual (Obsidian) | | | |
| 1.17-10 | In Editor/Formatting practice click at the end of "- [x] Soak the beans" and press Enter, type `Buy salt`, press Enter twice; click in "Buy fresh cheese" and press Tab, then Shift+Tab; click right after the `- [ ] ` of "Buy salt" and press Backspace; in the Tip callout, press Enter at the end of its last line | Enter adds `- [ ] ` (unchecked) under "Soak the beans"; on the empty item the second Enter ends the list (the marker goes); Tab nests "Buy fresh cheese" under "Buy corn flour" (two spaces) and Shift+Tab takes it back; Backspace leaves "Buy salt" as plain text; in the callout Enter starts a new `> ` line | 1.17 | `SmokeFormattingToolbarTest` 1_17-10, `ListEditingTest`, `EditorToolbarTest` | | | |
| 1.17-11 | In the Kotlin code block of 1.17-07, double-click "code", type `val hello = "World" -> x // != y`, with its spaces; then type `->` in a plain line | Double-click selects "code"; the whole block is shaded from fence to fence; every space moves the cursor at once (even at the end of the line) and the text shows exactly what was typed, `->`, `//` and `!=` as separate characters; the plain line also keeps `->` (no arrow) | 1.17 | `MarkdownHighlighterTest`, `MarkdownStylesTest` | | | |
