# Notekea M1 smoke test, step by step

Every case of `MVP_SMOKE_TEST.md` that can be tested in M1, written out click by click for Windows, macOS and Linux, so
someone who has never seen Notekea can run it alone. It is the manual part of the M1 QA run (`qa/README.md`): the
tester installs the release candidate, prepares the sample notes, then works through the sessions below and writes one
line per case in `REPORT_TEMPLATE.txt`.

`MVP_SMOKE_TEST.md` stays the source of truth: when a case changes there, it changes here in the same pull request.
The case IDs are the same in both files.

- **Time:** about 6 hours the first time on a system, 4 to 5 on the next ones. Work in the four sessions below, about
  1½ hours each, with a break between them: a tired tester misses details.
- **Order:** run the sessions in order and the cases of a session in order. Many cases build on the one before (a note
  renamed in one case is used in the next). When a case fails, write it down and go on with the next one.
- **Result:** for each case write its ID and **PASS**, **FAIL** or **SKIP** (only when the case says it does not apply
  to your system, or needs something you do not have, such as Obsidian).

## Before you start

### The keys of your system

The cases say **Mod** for the main shortcut key, which is not the same on every system. When a case says Mod+N, press
the key of your system together with N.

| In this guide | Windows | macOS | Linux |
|---|---|---|---|
| **Mod** | Ctrl | Cmd (⌘) | Ctrl |
| **Alt** | Alt | Option (⌥) | Alt |
| **Delete a selected note** | Delete | Cmd+Backspace | Delete |
| **Rename the selected row** | F2 | Enter | F2 |
| **Open a context menu from the keyboard** | Shift+F10 or the Menu key | — | Shift+F10 or the Menu key |
| **Take a screenshot** | Windows+Print Screen (saved in Pictures\Screenshots) | Cmd+Shift+3 (saved on the Desktop) | Print Screen |
| **Copy a screenshot to the clipboard** | Windows+Shift+S | Ctrl+Shift+Cmd+4 | the desktop's screenshot tool, "copy to clipboard" |
| **The file manager** | File Explorer | Finder | Files (Nautilus, Dolphin…) |
| **A plain text editor** | Notepad | TextEdit | Text Editor (gedit) |

`Ctrl+Tab` and `Ctrl+Shift+Tab` (next and previous tab) use Control on every system, macOS included.

### Install Notekea

Install the build named in your release (`Notekea m1-rc1` or later) as your guide says: [Windows](GUIDE_WINDOWS.md),
[macOS](GUIDE_MACOS.md), [Linux](GUIDE_LINUX.md). Unsigned builds show a warning the first time; the guides explain
how to get past it.

### Get the sample notes

Unzip `smoke-data.zip` into your **Documents** folder, so you have `Documents/smoke-data`. Then open a terminal and run
`prepare` once. Keep that terminal open: some cases ask you to run another command there.

| | Open a terminal | Run once |
|---|---|---|
| Windows | Start menu, type **PowerShell**, open **Windows PowerShell** | `powershell -ExecutionPolicy Bypass -File "$HOME\Documents\smoke-data\smoke.ps1" prepare` |
| macOS | Cmd+Space, type **Terminal**, Enter | `bash ~/Documents/smoke-data/smoke.sh prepare` |
| Linux | your desktop's Terminal | `bash ~/Documents/smoke-data/smoke.sh prepare` |

It answers "Smoke scenarios ready in …". `prepare` builds `smoke-data/scratch/`: copies of the sample vaults that the
cases may change freely. Run it again whenever a session asks you to start from fresh notes.

### Running a command during a case

When a case says **run `move`** (or `lock`, `outside`, `edit-note`…), type in the terminal the same line as
`prepare` with that word instead of `prepare`:

- Windows: `powershell -ExecutionPolicy Bypass -File "$HOME\Documents\smoke-data\smoke.ps1" move`
- macOS and Linux: `bash ~/Documents/smoke-data/smoke.sh move`

Three commands change Notekea's own settings, not only the sample notes: `first-launch` (Notekea starts as if newly
installed), `clear-cache` (it forgets the notes it has read) and `restore-settings` (it gets back what `first-launch`
put aside). Run them only when a case says so.

### Where things are

- **Choosing a vault:** "open `smoke-data/scratch/Playground Vault`" means: in Notekea click **Open folder…** (or press
  Mod+O), and in the system's folder picker go to Documents › smoke-data › scratch and choose **Playground Vault**.
  `smoke-data/vaults/...` is the same under **vaults**.
- **Opening a file as it is stored:** open the file manager, go to the vault's folder (for example Documents ›
  smoke-data › scratch › Playground Vault › Recipes) and open the `.md` file with the plain text editor (right-click ›
  Open with).
- **Notekea's data folder:** `%APPDATA%\Notekea` on Windows (press Windows+R and paste it), `~/Library/Application
  Support/Notekea` on macOS (in Finder press Cmd+Shift+G and paste it), `~/.local/share/notekea` on Linux.

### The parts of the window

Once a vault is open, from left to right:

- **Tool rail:** the narrow column of icons on the left edge: the Notekea logo at the top, then **Files**, **Side
  panel**, **Search notes** (magnifier) and **Today** (calendar); at the bottom **Open another vault**, **About** and
  **Settings**. Hovering an icon shows a tooltip with its name and shortcut.
- **Sidebar:** the vault's name at the top with its buttons (Sort, Expand all, Collapse all, Minimize "—"), then the
  **BOOKMARKS** section (when there are bookmarks) and the **FILES** tree of folders and notes.
- **Tabs:** across the top of the editor, one per open note, folder or file; the active tab is joined to the page.
- **The note:** its folder, title and properties, then the **toolbar** (Edit | Preview tabs, then the formatting
  buttons and More "⋯"), then the text. **Edit** shows the Markdown you type; **Preview** shows it rendered.
- **Side panel:** on the right, with an **Inspector | Chat** control on top; Inspector holds **OUTLINE** and **NOTE
  INFO**.
- **Status bar:** the thin line along the bottom: the note's path on the left; cursor position, line endings,
  encoding, word count and "Unsaved" / "Saved" on the right.
- **Palette:** the search box that opens over the window with Mod+P (notes by name) or Mod+Shift+F (text inside the
  notes).

### When a case fails

Under the case, in the report, write what you did, what you expected and what happened instead, and attach a
screenshot. If Notekea crashed, it shows "Notekea needs to close": click **Copy details** and paste them under the
case. Then go on with the next case; if Notekea no longer opens a vault, stop and send the report.

## Sessions

| Session | Cases | Prepare first |
|---|---|---|
| 1 | General, 1.1 to 1.4 | `prepare` |
| 2 | 1.5 to 1.7, 1.17 | `prepare` |
| 3 | 1.8 to 1.11 | `prepare` |
| 4 | 1.12 to 1.15, 1.18 | `prepare` |

Not run in M1 (their "Testable from" is a later feature): G-01, G-04, G-05 (offline and installer cases, 3.3 and 5.3),
1.1-01 (menus, 5.2) and 1.2-06 (packaged gallery, 5.3). Memory and timing are never measured with tools here: the
milestone's performance pass does that on the automated smoke runs.

---

## Session 1: General, 1.1 to 1.4

Run `prepare` before starting.

### G-02 · No warnings when started from a terminal

1. Quit Notekea if it is open.
2. Start it from the terminal:
   - Windows: `& "C:\Program Files\Notekea\Notekea.exe"`
   - macOS: `/Applications/Notekea.app/Contents/MacOS/Notekea`
   - Linux: `/opt/notekea/bin/Notekea`
3. Use it for a minute (open a vault, open a note), then quit it.
4. Read what the terminal printed.

**Pass when:** no line says `Exception`, and no line says `WARNING: A restricted method`.

### G-03 · Closing the window ends Notekea

1. Open Notekea and close its window with the window's close button (✕, or the red dot on macOS).
2. Look for a process left behind: Windows, Task Manager (Ctrl+Shift+Esc) › Details, look for `Notekea.exe`;
   macOS, Activity Monitor, search "Notekea"; Linux, run `pgrep -fa notekea` in the terminal.

**Pass when:** nothing named Notekea is still running a few seconds after the window closed.

### G-06 · Notekea follows the system's light or dark mode

1. Open Notekea with any vault.
2. Switch the system's appearance while Notekea is open: Windows, Settings › Personalization › Colors › "Choose your
   mode"; macOS, System Settings › Appearance; Linux, the desktop's appearance settings (GNOME, KDE or COSMIC).
3. Switch it back.

**Pass when:** Notekea turns dark and light at once each time, without restarting; on Linux too.

### G-07 · A crash is reported and the typed text is kept

1. Quit Notekea.
2. Start it with the crash test turned on:
   - Windows (PowerShell): `$env:NOTEKEA_CRASH_TEST="1"; & "C:\Program Files\Notekea\Notekea.exe"`
   - macOS: `NOTEKEA_CRASH_TEST=1 /Applications/Notekea.app/Contents/MacOS/Notekea`
   - Linux: `NOTEKEA_CRASH_TEST=1 /opt/notekea/bin/Notekea`
3. Quickly open a note and type a word; then wait without touching anything.
4. When the dialog appears, click **Copy details** and paste into the text editor; click **Report on GitHub** and
   close the browser tab it opens; click **Close Notekea**.
5. Start Notekea normally and open the note.
6. Windows only: close the PowerShell window afterwards, so the crash test is not left on.

**Pass when:**
- About 3 seconds after the window opened, a window "Notekea needs to close" appears in Notekea's style and in the
  current light or dark theme, with crisp text.
- It names the report file in the data folder: `crashes/crash-<date>.txt`.
- Copy details says "Copied to the clipboard"; the pasted text starts with "Notekea crash report" and does not contain
  the note's text.
- Report on GitHub opens GitHub's "Crash report" form.
- After Close Notekea the app is gone, and when reopened the note has the word you typed.

### 1.1-02 · Shortcuts use your system's key

1. Open `smoke-data/scratch/Playground Vault`.
2. Click a folder in the sidebar and press Mod+N.

**Pass when:** a new "Untitled" note is created with Ctrl (Windows, Linux) or Cmd (macOS); no shortcut in this guide
needs the other system's key. Delete the new note afterwards (select it, press the delete key of your system, confirm).

### 1.1-03 · Dialog buttons in your system's order

1. In the Playground Vault, select any note and press the delete key of your system (Delete, or Cmd+Backspace on
   macOS).
2. Look at the dialog's two buttons, then click **Cancel**.

**Pass when:** Windows shows the main button ("Delete note") first and Cancel second; macOS and Linux show Cancel
first and the main button second.

### 1.1-04 · Notekea keeps its data in the right place

1. With Notekea having opened a vault at least once, open Notekea's data folder (see "Where things are").
2. Look inside the Playground Vault folder and the folder where Notekea is installed.

**Pass when:** the data folder exists at `%APPDATA%\Notekea` (Windows), `~/Library/Application Support/Notekea`
(macOS) or `~/.local/share/notekea` (Linux); nothing new was written next to the Notekea program or inside the vault.

### 1.1-05 · Linux only: the data folder follows XDG_DATA_HOME

Windows and macOS: **SKIP**.

1. Quit Notekea.
2. In the terminal run `XDG_DATA_HOME=/tmp/xdg-test /opt/notekea/bin/Notekea`, open any vault, then quit.
3. Run `ls /tmp/xdg-test`.

**Pass when:** `/tmp/xdg-test/notekea` exists.

### 1.2-01 · Notekea's own fonts

1. Open the Playground Vault and the note Editor/Read mode; press Mod+E to see it rendered (Preview).
2. Look at the menu labels and the text, then at the code blocks near the end.

**Pass when:** the interface and the text use Inter (a clean sans-serif, not the system's font) and the code uses
JetBrains Mono (a monospaced font), on every system.

### 1.2-02 · Colors in light and dark mode

1. With the note of 1.2-01 open, look at the window in light mode, then switch the system to dark mode (G-06) and look
   again; switch back.

**Pass when:** the main color is a cobalt blue (selected items, links, the active tab's line) with tangerine accents
(tags); every text is easy to read in both modes, nothing is faint or low-contrast.

### 1.2-03 · Rounded corners

1. Look at buttons (Open folder… on the welcome screen), text fields (the palette, Mod+P, then Esc) and the cards of
   a folder (click the Recipes folder).

**Pass when:** controls have small rounded corners, not pill shapes; cards are slightly rounder.

### 1.2-04 · The whole window with the keyboard

1. Click once in an empty part of the sidebar, then press Tab repeatedly, and Shift+Tab to go back.
2. When a button has the focus, press Enter or Space.

**Pass when:** every button, tab and list item gets a visible ring when it has the focus, and Enter or Space activates
it.

### 1.2-05 · A screen reader announces the controls

1. Turn on the system's screen reader: Windows, Ctrl+Windows+Enter (Narrator); macOS, Cmd+F5 (VoiceOver); Linux,
   Super+Alt+S (Orca, if installed).
2. Move through the tool rail and the tabs with Tab.
3. Turn the screen reader off the same way.

**Pass when:** buttons, tabs and list items are read with their name and what they are ("Search notes, button").

### 1.2-07 · A narrow window keeps text on one line

1. Open a note and make the window as narrow as your system allows, dragging its right edge.

**Pass when:** the status bar items and the tab names stay on one line and end with "…" when they do not fit; numbers
in badges never break in two; nothing is cut in half.

### 1.3-01 · The first launch

1. Quit Notekea.
2. Run `first-launch`.
3. Start Notekea.

**Pass when:** the welcome screen shows the Notekea logo and name with its tagline, **Open folder…** (with the hint
Ctrl+O, or ⌘O on macOS) and **Create new vault**; there is no recent list, but a card beside the actions explains what
Notekea is: Plain Markdown, Works with Obsidian, Local AI, Private.

### 1.3-02 · Open a vault with the folder picker

1. Click **Open folder…** and choose `smoke-data/vaults/Personal Notes`.
2. Look at the window title and the sidebar.
3. Click the `attachments` folder.

**Pass when:**
- The system's own folder picker opened.
- The window title says "Personal Notes".
- The sidebar lists its 12 notes: Ideas (2), Journal (3), Projects (1), Recipes (3), Travel (2) and Welcome.
- `.obsidian` and `.trash` are not shown.
- `attachments` shows 1 (its one file) and, opened, lists `shopping.txt` with its extension.
- Folder numbers count every file inside, notes and others.

### 1.3-03 · The last vault reopens

1. Quit Notekea and start it again.

**Pass when:** Personal Notes opens directly, without showing the welcome screen first.

### 1.3-04 · Recent vaults

1. Click **Open another vault** at the bottom of the tool rail; open `smoke-data/vaults/Work Obsidian`.
2. Click Open another vault again and open `smoke-data/vaults/Empty Vault`: it says "This folder has no notes yet".
3. Click Open another vault again and look at the recent list.

**Pass when:** the vaults are listed most recent first, each with its folder (shown as `~/…` on macOS and Linux) and
when it was opened ("Just now", "2 days ago"…).

### 1.3-05 · Mod+O opens the folder picker anywhere

1. On the welcome screen press Mod+O; cancel the picker.
2. Click somewhere on the welcome screen, press Mod+O again; cancel.
3. Open Personal Notes from the recent list and press Mod+O; cancel.

**Pass when:** the folder picker opens every time, and cancelling it changes nothing.

### 1.3-06 · A vault that moved

1. Run `prepare` (if not run in this session).
2. Open `smoke-data/scratch/Movable Vault`; quit Notekea.
3. Run `move`; start Notekea.
4. On the welcome screen, look at Movable Vault in the recent list; click **Locate…** and choose
   `smoke-data/scratch/Moved Vault`.
5. Click Open another vault, run `prepare` and `move` again, and click **Remove** on the vault now missing.

**Pass when:** the missing vault is greyed out with "Folder not found", "Remove" and "Locate…"; Locate… opens it from
its new place; Remove only takes it off the list (the folder stays on disk).

### 1.3-07 · Create a vault

1. On the welcome screen click **Create new vault**; keep the suggested name and place; create it.

**Pass when:** a "My Notes" folder is created in Documents (or the home folder) and opens empty: "This folder has no
notes yet" with a **New note** button; nothing else is written inside it. Afterwards delete that My Notes folder with
the file manager.

### 1.3-08 · Names Notekea refuses

1. Click Open another vault, then **Create new vault**; set the place to `smoke-data/scratch/New Vault Location`
   (`prepare` already put a "My Notes" folder there) and try to create "My Notes".
2. Change the name to `a/b` and try again.
3. Type another letter after each refusal.

**Pass when:** each problem (the folder already exists, the `/` in the name) is explained in plain words under the
name field, keeping what you typed; typing again clears the message.

### 1.3-09 · A folder Notekea cannot read

1. Open `smoke-data/scratch/Locked Vault`.
2. Click Open another vault on the tool rail.
3. Run `lock`.
4. Click "Locked Vault" in the recent list; then click **Dismiss**.

**Pass when:** an error banner names the folder and says to check its permissions; Dismiss hides it. (The folder is
locked after Notekea knows it because the system's pickers do not let you choose a folder you cannot read.)

### 1.3-10 · Create a vault with the keyboard

1. On the welcome screen click **Create new vault**; type a name without clicking the field; press Esc.
2. Click Create new vault again, type `Keyboard vault` and press Enter; watch the buttons while it opens.

**Pass when:** the name field has the focus at once; Esc closes the panel; Enter creates the vault; while it opens,
"Opening vault…" shows a spinner without moving the buttons. Delete the Keyboard vault folder afterwards.

### 1.4-01 · The tree and opening notes

1. Open `smoke-data/vaults/Personal Notes`.
2. Click Projects, then Garden, then the note "Tomato plan".
3. Click Recipes, then "Arepas"; then click the Recipes folder's name.

**Pass when:**
- Folders come before notes, each with how many files it holds.
- The clicked row is highlighted.
- Each note opens in a tab of its own, showing its folder, its title and its properties above its text; Arepas shows
  the tags `#recipe #venezuelan` and the aliases `Arepa` and `Corn cakes`.
- The Recipes folder opens in a tab as a grid of cards under its name and "3 files".

### 1.4-02 · The tree with the keyboard

1. Click a row of the tree, then use only the keyboard: ↓ and ↑; on a folder → twice; ← twice; Enter on a folder; ↓ to
   a note and Enter.

**Pass when:** ↓ ↑ move the selection without opening anything; the first → opens the folder and the second goes into
it; ← closes it, then goes to the parent; Enter on a folder opens or closes it and shows it in a tab; Enter on a note
opens it in a tab; the selected row has a focus ring and the list scrolls to keep it in view.

### 1.4-03 · Sorting

1. Open `smoke-data/scratch/Playground Vault` and open the Recipes folder in the tree.
2. Hover the sort button at the top of the sidebar, click it and choose "Date modified, newest first".
3. Quit Notekea and start it again.
4. Choose "Name" again.

**Pass when:** the tooltip names the current sort and the menu checks it; by date Recipes shows Pasta e fagioli,
Arepas, Café con leche; by name Arepas, Café con leche, Pasta e fagioli; folders always come first, by name; after the
restart the vault reopens with the same sort and the same folders open.

### 1.4-04 · New notes and folders

1. In the Playground Vault select Recipes and press Mod+N; type nothing and press Esc.
2. Right-click Recipes and choose **New folder**; press Esc.
3. Right-click Travel and choose **New note**; press Esc.
4. Click the empty space below the last row of the tree and press Mod+N; press Esc.
5. Right-click that empty space and choose **New folder**; press Esc.

**Pass when:** the right-click menu offers New note, New note from template and New folder; "Untitled", "Untitled
folder" and "Untitled" are created in the selected folder (numbered when the name is taken), shown and selected with
the name ready to type; clicking the empty space clears the selection, so the last two are created at the top of the
vault.

### 1.4-05 · Renaming

1. Select one of the Untitled notes of 1.4-04 and press the rename key (F2; Enter on macOS); type `My first note`,
   pressing Space while the whole name is still selected, and press Enter.
2. Rename another Untitled with the right-click menu's **Rename**, type something and press Esc.
3. Rename one and try `a/b`, then `.hidden`, then the name of a note in the same folder.

**Pass when:** spaces are typed like any letter and the field keeps the focus; the file is renamed on disk without
doubling `.md`; Esc keeps the old name; each refused name explains why under the field and keeps what you typed.

### 1.4-06 · Renaming updates the links

1. In the Playground Vault rename Recipes/Arepas to `Arepas reina` (press Enter).
2. Open `Welcome.md`, `Journal/2026-10-20.md` and `Editor/Read mode.md` with the text editor.
3. Rename Journal/2026-10-22, which no note links to.

**Pass when:** no question is asked; a notice says links were updated in 3 notes; the three files contain
`[[Arepas reina]]` and their other links are unchanged; renaming the journal note shows no notice and changes no other
file.

### 1.4-07 and 1.4-13 · Deleting, and restoring from the trash

1. Select a note and press the delete key of your system; click Cancel.
2. Press it again and click **Delete note**.
3. Select the Travel folder and delete it.
4. Open the system's trash (Recycle Bin, Trash) and restore the Travel folder (right-click › Restore). The next case
   needs it.

**Pass when:** the dialog names the note (a folder also says how many notes go with it), with Cancel and "Delete note"
in your system's order (1.1-03); the focus starts on Cancel, with a ring; the note and the folder are in the system's
trash, and Travel comes back to the tree once restored (if the system has no trash, a notice says they went to the
vault's `.trash` folder: move Travel back from there with the file manager).

### 1.4-08 · Show in Files and the context menu

1. Right-click a note and choose **Show in File Explorer** (Windows), **Show in Finder** (macOS) or **Show in Files**
   (Linux).
2. Select a note and press Shift+F10 or the Menu key (not on macOS).

**Pass when:** the file manager opens with the note selected (on Linux at least its folder opens); the menu shows each
action's shortcut (Bookmark Mod+Shift+B, Rename F2, Delete); Shift+F10 or the Menu key opens the menu on the selected
row.

### 1.4-09 · Changes made outside Notekea

1. With the Playground Vault open, run `outside`.
2. Watch the sidebar without clicking; then click Travel/Lisbon trip.

**Pass when:** within a second, without clicking: Inbox/Created outside appears, Ideas/Reading list disappears, and
Lisbon trip shows the new `#edited` tag when selected.

### 1.4-10 · A large vault opens quickly

1. Run `clear-cache`.
2. Open `smoke-data/vaults/Ghost Serializer` (a code repository written out as 371 notes) and watch the sidebar.
3. Quit Notekea and start it again.

**Pass when:** the first time, every module and its notes are listed in about a second (the sidebar may show grey
placeholder rows meanwhile); after the restart the tree appears at once, in under a second.

### 1.4-11 · Resizing and hiding the sidebar

1. Open the Playground Vault. Drag the sidebar's right edge to its narrowest, then its widest.
2. Press Mod+\ twice.
3. Click the sidebar header's Minimize (—), then the tool rail's **Files** button.
4. Quit and restart.

**Pass when:** the pointer becomes a resize cursor; the sidebar stays between a narrow and a wide limit (200 to 480);
Mod+\ slides it closed and open; Minimize hides it and leaves the tool rail, whose Files button brings it back; nothing
covers the tabs while it is hidden; width and visibility are the same after the restart.

### 1.4-12 · The vault folder disappears while open

1. Run `prepare`, then open `smoke-data/scratch/Movable Vault` and select a note.
2. Run `move` and wait a few seconds without touching Notekea.
3. Click **Try again**; right-click the note and choose Show in Files / Finder / File Explorer.

**Pass when:** within about 2 seconds the sidebar explains that the vault's folder cannot be found, with "Try again"
and "Open another vault"; Try again keeps explaining it; Show in Files says the note cannot be found instead of
opening the file manager; nothing crashes.

**End of session 1.**

---

## Session 2: the editor, search, tasks and formatting (1.5 to 1.7, 1.17)

Run `prepare` before starting, then open `smoke-data/scratch/Playground Vault`. Its `Editor` folder holds the notes of
these cases.

### 1.5-01 · Typing is saved

1. Open Recipes/Arepas. Click at the end of its text and type a sentence.
2. Stop typing and watch the right end of the status bar.
3. Open `Playground Vault/Recipes/Arepas.md` with the text editor.

**Pass when:** "Unsaved" shows while you type and turns into "Saved", which stays (the tab no longer shows it); the
file has your sentence and its frontmatter exactly as before: `tags: [recipe, venezuelan]`, `aliases: [Arepa, Corn
cakes]`, `servings: 6`.

### 1.5-02 · Nothing typed is lost

1. In Arepas type a word and press Mod+S at once.
2. Type another word and at once click Welcome in the tree.
3. Open Arepas again, type a third word and close the window within a second.
4. Start Notekea and open Arepas.

**Pass when:** Mod+S shows "Saved" at once; after the restart Arepas has the three words.

### 1.5-03 · Windows line endings are kept

1. Open Editor/Windows line endings; add a line at the end and wait for "Saved".
2. Run `check-note`.

**Pass when:** the terminal prints "OK: every line ends with CR LF and the frontmatter is unchanged".

### 1.5-04 · Renaming from the title

1. Open Recipes/Arepas; click its title, change it to `Arepas reina` and press Enter.
2. Click the title again; try a name with `/`, then `Pasta e fagioli`; press Esc.
3. Rename it back to `Arepas` the same way (the next cases need its name).

**Pass when:** the file is renamed and the tree selects it; a notice says "Links updated in 3 notes: 2026-10-20, Read
mode, Welcome." and goes away by itself after about 10 seconds; each refused name is explained under the title until
you edit it; Esc puts the current name back.

### 1.5-05 · A note changed by another app

1. Open Editor/Shared note and do not type in it.
2. Run `edit-note`.

**Pass when:** within about a second the note shows a new last line "Edited outside Notekea at …", with no question.

### 1.5-06 · A note changed while you type

1. Open Editor/Shared note and click at the end of its text.
2. Run `edit-note-soon`; within 3 seconds click back into the note and keep typing until a banner appears.
3. Click **Keep mine**.
4. Repeat steps 2 and 3, and click **Load theirs** this time.

**Pass when:** a banner says the note changed outside Notekea; Keep mine keeps your text and writes it over the change;
Load theirs shows the file's text and drops what you typed; nothing is saved while the banner shows. (Clicking the
terminal makes Notekea save, which is why the change must arrive while you type.)

### 1.5-07 · A note deleted by another app

1. Open Editor/Shared note.
2. Run `delete-note`; click **Restore**.
3. Run `delete-note` again; click **Close**.

**Pass when:** a banner says the note was moved or deleted outside Notekea; Restore writes it back with what the editor
shows; Close closes its tab and the tree no longer lists it.

### 1.5-08 · Markdown styled while editing

1. Open Editor/Read mode (it opens in Edit; if it shows rendered text, press Mod+E).

**Pass when:** headings are larger; bold and italic show as such; code is in the monospaced font on a shaded
background (code blocks without colors); links and wiki links are blue; tags are orange; the Markdown signs (`#`, `**`,
`[[`) are faint; the two tasks show a checkbox over their brackets.

### 1.5-09 · Find in the note

1. In Editor/Read mode press Mod+F and type `arepas`.
2. Press Enter a few times, then Shift+Enter, then Esc.
3. Press Mod+F again and, with the bar open, type in the note.

**Pass when:** the bar shows "1 of N" and every match is highlighted, the current one stronger; Enter and Shift+Enter
move to the next and previous match and wrap around; Esc closes the bar with the match selected; typing updates the
count.

### 1.5-10 and 1.5-12 · Preview, and the mode is remembered

1. In Editor/Read mode press Mod+E (or click the **Preview** tab under the title).
2. Open another note; quit and restart Notekea.
3. Press Mod+E again.

**Pass when:** the note is rendered: headings, lists, checkboxes, the quote, and both code blocks colored by language
(Kotlin and Python); the other note and the restarted app open rendered too, because the last mode is remembered;
Mod+E goes back to the Markdown, which is then remembered.

### 1.5-11 · Links in Preview

1. Open Editor/Read mode and press Mod+E (Preview).
2. Click "Arepas"; go back to Read mode (its tab); click "the coffee".
3. Back in Read mode click "A note that does not exist yet", then "Pasta e fagioli", then "the web".
4. Look for `[[not a link inside code]]`.
5. Press Mod+E to go back to Edit (the next sessions expect Edit).

**Pass when:** each wiki link opens its note and the tree selects it; the missing one creates "A note that does not
exist yet" at the top of the vault and opens it; the Markdown link opens Pasta e fagioli; the web link opens the
browser; `[[not a link inside code]]` is not a link.

### 1.6-01 · The palette finds notes by name

1. Press Mod+P without typing and look at the list.
2. Type `are` and press Enter.

**Pass when:** the palette opens over the window with the most recently changed notes; typing puts "Arepas" first with
"Are" in bold; Enter closes the palette, opens Arepas in a new tab (or goes to its tab if open) and the tree selects
it with Recipes opened.

### 1.6-02 · Aliases and accents

1. Press Mod+P and type `corn cakes`.
2. Clear the field and type `cafe`.

**Pass when:** "Arepas" is first with a second line "Corn cakes" (its alias) highlighted; `cafe` finds "Café con leche"
although it has an accent. Press Esc.

### 1.6-03 · Folders in the search

1. Press Mod+P and type `gard/tom`.

**Pass when:** "Tomato plan" is first, with the line "Projects/Garden/Tomato plan" under it. Press Esc.

### 1.6-04 · Search inside the notes

1. Press Mod+Shift+F and type `tomato`.

**Pass when:** the Content tab is selected; after a short pause it lists "2026-10-20" first (two matches, line "Planted
the last tomato seedlings…"), then "Pasta e fagioli" ("Add tomatoes…") and "Tomato plan", each line with the word in
bold and shown as plain text ("notes in Tomato plan", no `[[`, `**`, `#` or `-` signs).

### 1.6-05 · Search a tag

1. In the Content tab clear the field and type `#recipe`.

**Pass when:** it lists "Arepas" (tagged in its frontmatter, no line under it), "Café con leche" (line
`#recipe/drinks`) and "Pasta e fagioli" (line `#recipe`), and no other note. Press Esc.

### 1.6-06 · The palette with the keyboard

1. Press Mod+P, type `a`, press Tab, Tab again, ↓ three times and ↑ four times.
2. Press Esc.
3. Press Mod+P again and click outside its card.

**Pass when:** Tab switches between Notes and Content keeping `a` in the field; the selection moves and wraps from the
first result to the last; Esc and the click outside both close the palette, and the note shown does not change.

### 1.6-07 · A tag in Preview searches it

1. Open Journal/2026-10-21 and press Mod+E (Preview).
2. Click `#travel`.
3. Press Esc, then Mod+E to go back to Edit.

**Pass when:** `#travel` is blue like a link; the click opens the Content tab with `#travel` typed, listing
"2026-10-21" (line `#journal #travel`) and "Lisbon trip" (tagged in its frontmatter).

### 1.6-08 · Nothing found

1. Press Mod+P and type `zzz`.
2. Press Tab and clear the field.

**Pass when:** "No notes match" with a hint to press Tab; with Content selected and the field empty it explains that
words or a `#tag` can be typed. Press Esc.

### 1.6-09 · The search sees changes from other apps

1. Run `edit-note`.
2. Press Mod+Shift+F and type `edited outside`.

**Pass when:** "Shared note" is listed with the line "Edited outside Notekea at …" just added. Press Esc.

### 1.6-10 · Searching a large vault

1. Open `smoke-data/vaults/Ghost Serializer`.
2. Press Mod+P and type `flatreader`; press Esc.
3. Press Mod+Shift+F and type `perfect hash`; press Esc; press Mod+Shift+F and type it again.
4. Open the Playground Vault again (Open another vault › Playground Vault).

**Pass when:** names filter as fast as you type and "GhostJsonFlatReader" ends up among the first, with the other flat
readers; the first content search over the 371 notes shows its results at once, with "PerfectHashFinder" among them,
and the second is immediate.

### 1.6-11 · The keyboard comes back to the note

1. Open Recipes/Arepas in Edit and click in its text.
2. Press Mod+P, then Esc, and type a word.
3. Press Mod+P, type `pasta`, press Enter and type another word.

**Pass when:** without clicking, the first word goes into Arepas and the second into Pasta e fagioli, each at its
cursor.

### 1.6-12 · The magnifier on the tool rail

1. Hover the magnifier on the tool rail; click it; press Esc.
2. Hide the sidebar with Mod+\ and click the magnifier again; press Esc and Mod+\.

**Pass when:** its tooltip says "Search notes" and the shortcut (⌘P on macOS, Ctrl+P elsewhere); a click opens the
palette on the Notes tab, as Mod+P does, with the sidebar shown or hidden.

### 1.7-01 · Tasks in Preview

1. Open Editor/Tasks and callouts and press Mod+E (Preview); look at the Tasks section.

**Pass when:** nine tasks show as checkboxes, "Arepas on Monday" and "Pasta e fagioli on Tuesday" indented; four are
checked: "Soak the beans", "Pasta e fagioli on Tuesday", "A numbered task done with a capital X" and, in the To do
callout, "A finished task inside a callout".

### 1.7-02 · Checking a task in Edit

1. Press Mod+E (Edit). Click the checkbox of "Buy corn flour"; wait for "Saved".
2. Open `Playground Vault/Editor/Tasks and callouts.md` with the text editor.
3. Click the checkbox again.

**Pass when:** a checkbox covers every `[ ]` and `[x]` (not inside code); the click checks it at once and the status
bar says "Saved"; the file reads `- [x] Buy corn flour`; the second click unchecks it (`- [ ]`).

### 1.7-03 · Mod+Enter checks the task on the cursor's line

1. Click in the text of "Arepas on Monday" and press Mod+Enter twice.
2. Click in the "## Tasks" heading and press Mod+Enter.

**Pass when:** the task is checked, then unchecked again, without clicking its checkbox; on the heading nothing
changes.

### 1.7-04 · Callouts

1. Press Mod+E (Preview) and scroll through the Callouts and Aliases sections.

**Pass when:** each callout is a card with its icon and title: "Note" (named after its type), Abstract, Info, To do,
Tip, Success, Question and Example in blue; Warning and "Caution is a warning" in orange; Failure, Danger and Bug in
red; Quote in gray; "TL;DR is an abstract" looks like Abstract and "An unknown type is a note" like Note; no `[!` text
is visible.

### 1.7-05 · Folding callouts

1. In the Folding section click the title of "A folded question (click to open)" twice.
2. Click the title of "An open tip (click to fold)".

**Pass when:** the folded question starts closed with a chevron and opens and closes with each click (the title text
is not selected); the open tip starts open and folds.

### 1.7-06 · Nesting

1. Look at the Nesting section and the Example callout. Then press Mod+E (Edit).

**Pass when:** "Level one" holds "Level two", which holds "Level three", each with its own color; the Example callout
shows `val servings = 6` as colored code without `>` signs; the last quote is a plain quote with a bar.

### 1.17-01 · The formatting toolbar

1. Open Editor/Formatting practice in Edit and hover each button of the bar under the title.
2. Open More (⋯) and look at it; press Esc.
3. Click **Preview**, then press Mod+E.

**Pass when:**
- Under the title: the Edit and Preview tabs (the selected one underlined), then one rounded capsule with Heading,
  Bold, Italic · Link to a note, Tag · Checklist, Bulleted list · Insert template · More (⋯), then Find in note.
- Each tooltip names the action with its shortcut where it has one: Bold Mod+B, Italic Mod+I, Link to a note Mod+K,
  Insert template Mod+T, Find in note Mod+F, the tabs Mod+E.
- More holds Web link (Mod+Shift+K), Numbered list, Callout and Code block (their types open beside the menu), Image or
  file, Add property (its types beside the menu), then Quote, Divider and Table; Callout and Image or file are
  explained under their names.
- Preview shows the note rendered with only the tabs and Find in note; Mod+E goes back to Edit.

### 1.17-02 · Headings, bold and italic

1. Click in "Weekend arepas"; choose Heading › Large heading, then Heading › Large heading again, then Heading ›
   Medium heading.
2. Select "corn flour" and click Bold twice.
3. Select "fresh" and press Mod+I.
4. Click inside the word "beans" and press Mod+B twice.
5. Click at the end of the first line and, with nothing selected, click Bold and type `soft`.

**Pass when:** the line becomes `# Weekend arepas` (large), then plain again, then `## Weekend arepas`; `**corn
flour**` shows bold, then plain again; `*fresh*` shows italic; with the cursor in a word Mod+B makes `**beans**` and
takes it off again; at the end of the line Bold inserts `**bold text**` with "bold text" selected, and typing replaces
it: `**soft**`.

### 1.17-03 · Lists on several lines

1. Select the three lines from "Buy corn flour" to "Soak the beans" with the mouse.
2. Without selecting again, click Checklist, then Numbered list (in More), Bulleted list and Checklist again.
3. Click the checkbox of "Soak the beans".

**Pass when:** every click changes all three lines and they stay selected: a checklist (`- [ ] `, with checkboxes),
then `1.` `2.` `3.`, then `- ` bullets, then a checklist again; the click checks "Soak the beans" (`- [x] Soak the
beans`).

### 1.17-04 · Link to a note

1. Select "coffee" and click Link to a note; type `cafe` and press Enter.
2. Press Mod+K, then Esc, and type a word.
3. Click Preview and click "coffee"; go back to Formatting practice and press Mod+E.

**Pass when:** the palette says "Choose a note to link" and has no Notes / Content tabs; Enter writes `[[Café con
leche|coffee]]` and the cursor follows it; after Esc nothing is inserted and the word goes into the note at the cursor;
in Preview "coffee" is a link that opens Café con leche.

### 1.17-05 · Web links and tags

1. Select "hot pan", press Mod+Shift+K and type `https://example.com`.
2. Click at the end of the "## Weekend arepas" line and click Tag; type `rec` and press Enter.
3. Click Tag again, type `weekend` and press Enter.
4. Wait for "Saved"; click Tag, type `week`, press Esc and type a word.

**Pass when:** Web link writes `[hot pan](https://)` with `https://` selected, so typing completes `[hot
pan](https://example.com)`; Tag opens "Find or create a tag" listing the vault's tags, the most used first: #journal,
#recipe, #smoke, #travel, #crlf, #editor, #ideas, #index; `rec` leaves #recipe, #recipe/drinks and "New tag: #rec",
and Enter adds #recipe; `weekend` offers only "New tag: #weekend"; the line ends `## Weekend arepas #recipe #weekend`;
once saved, `week` finds #weekend; Esc closes the picker and the word goes into the note at the cursor.

### 1.17-06 · Callouts from the toolbar

1. Select the line "Remember that the pan stays hot for a long time."; open More › Callout, check **Start folded** and
   choose Warning.
2. Click in the empty last line; open More › Callout (Start folded is still checked: uncheck it) and choose Tip; type
   `Use a cast-iron pan`.
3. Click Preview and click the Warning's title; then press Mod+E.

**Pass when:** Callout opens beside the More menu, explained under its name ("A colored box for a tip, a warning…"),
listing every callout type with its icon, with Start folded first and how to use it under it; it stays open when
checked; the selected line becomes `> [!warning]- Warning` / `> Remember that the pan stays hot for a long time.`; Tip
inserts `> [!tip] Tip` / `> Write here` with "Write here" selected, replaced by what you type; in Preview the Warning
is an orange card that starts folded and opens on click, and the Tip is a blue card.

### 1.17-07 · Table, code block, divider and quote

1. Click in the empty last line; open More and point at Table; move over the grid and click the cell of 3 columns × 2
   rows.
2. Click in the empty last line again; choose More › Code block › Kotlin.
3. From the empty last line choose More › Divider; then More › Quote.
4. Click Preview; then press Mod+E.

**Pass when:** Table opens a grid that fills as the pointer moves and says "3 × 2 table"; the click inserts a table with
the headers Column 1, Column 2, Column 3 and two rows of "Text", "Column 1" selected; Code block lists Plain text,
Python, JavaScript, TypeScript, Java, Kotlin, C#, C++, Go, Rust and Shell, and Kotlin writes ```` ```kotlin ```` with
"code" selected; Divider writes `---` and Quote `> Quote` on lines of their own; Preview shows a three-column table,
the Kotlin code colored, a line and a quote.

### 1.17-08 · The toolbar while scrolling and in a narrow window

1. Open Editor/Tasks and callouts; click in a line in the middle of the screen and type a word.
2. Scroll down.
3. Click Preview, then press Mod+E twice.
4. Make the window narrow until the bar no longer fits on one line; open More (⋯).

**Pass when:** clicking and typing never move the note (the title stays in view); the bar scrolls up with the title,
then stays at the top over the text, and its buttons still work there; no focus ring is left on the Edit or Preview
tab; in a narrow window the bar stays one line, and the buttons that do not fit (Checklist, Bulleted list and Insert
template, then Link to a note and Tag) move to the top of More, where they still work. Make the window wide again.

### 1.17-09 · Undo, redo and Obsidian

1. In Editor/Formatting practice press Mod+Z once, then Mod+Shift+Z; wait for "Saved".
2. If Obsidian is installed, open the Playground Vault there and look at Editor/Formatting practice. Without Obsidian,
   check only step 1 and write PASS or FAIL for it.

**Pass when:** Mod+Z undoes the last toolbar change in one step and Mod+Shift+Z redoes it; Obsidian shows the same
heading, bold and italic, checklist, links, tags, callouts (the Warning folded), table, code block and quote as Preview.

### 1.17-10 · Enter, Tab and Backspace in lists

1. Click at the end of "- [x] Soak the beans" and press Enter; type `Buy salt`; press Enter twice.
2. Click in "Buy fresh cheese" and press Tab, then Shift+Tab.
3. Click right after the `- [ ] ` of "Buy salt" and press Backspace.
4. In the Tip callout, press Enter at the end of its last line.

**Pass when:** Enter adds `- [ ] ` (unchecked) under "Soak the beans"; on the empty item the second Enter ends the
list (the marker goes); Tab nests "Buy fresh cheese" under "Buy corn flour" (two spaces) and Shift+Tab takes it back;
Backspace leaves "Buy salt" as plain text; in the callout Enter starts a new `> ` line.

### 1.17-11 · Typing code

1. In the Kotlin code block of 1.17-07, double-click "code" and type `val hello = "World" -> x // != y`, with its
   spaces.
2. In a plain line type `->`.

**Pass when:** the double-click selects "code"; the whole block is shaded from fence to fence; every space moves the
cursor at once (even at the end of the line) and the text shows exactly what you typed, with `->`, `//` and `!=` as
separate characters; the plain line also keeps `->` (no arrow).

**End of session 2.**

---

## Session 3: pictures, outline, daily notes and tabs (1.8 to 1.11)

Run `prepare` before starting, then open `smoke-data/scratch/Playground Vault`. `Editor/Images and files` embeds the
files of the vault's `attachments` folder, and `smoke-data/scratch/Outside files` holds files outside the vault to
locate, pick and drop.

### 1.8-01 · Pictures and files in Preview

1. Open Editor/Images and files and press Mod+E (Preview); scroll through it.

**Pass when:** the orange and blue plate shows three times: at its own size (480 wide, or the note's width if
narrower), 200 wide, and from the Markdown link; Weekly menu.pdf and broken.png are file chips with a paperclip and
"Opens with its app"; Lost photo.png shows "This file is missing" with its name and **Locate…**; in the last paragraph
"the menu" is a link.

### 1.8-02 · The same in Edit

1. Press Mod+E (Edit); type a line at the top of the note.

**Pass when:** every embed line keeps its text as written (`![[Arepas plate.png|200]]`…) and shows its picture, chip
or missing file right under it; typing above moves them along with their lines.

### 1.8-03 · Opening a file

1. Click the Weekly menu.pdf chip.
2. Press Mod+E (Preview) and click "the menu"; press Mod+E again.

**Pass when:** the PDF ("Weekly menu: arepas") opens in the system's PDF viewer both times.

### 1.8-04 · Locating a missing file

1. Click **Locate…** under Lost photo.png and choose `smoke-data/scratch/Outside files/Found plate.png`.

**Pass when:** the line becomes `![[Found plate.png]]` and the green plate shows under it; Found plate.png is now in
the vault's `attachments` folder (in the tree).

### 1.8-05 · Adding a file from the toolbar

1. Click at the end of the note; open More (⋯) and click **Image or file** (read the explanation under it); choose
   `Outside files/Shopping list.pdf`.

**Pass when:** `![[Shopping list.pdf]]` is written on a line of its own with its chip under it and the cursor on the
next line; the PDF is copied into the vault's `attachments` folder.

### 1.8-06 · Dropping files

1. Open the file manager next to Notekea. Drag `Found plate.png` from `smoke-data/scratch/Outside files` and drop it on
   the note.
2. Drag `Arepas plate.png` from `Playground Vault/attachments` and drop it on the note.
3. Change the number after `|` in one of the new lines.

**Pass when:** the first is copied as `Found plate 1.png` (the name was taken) and written as `![[Found plate
1.png|320]]`; the second is already in the vault, so it is written as `![[Arepas plate.png|400]]` without a copy (it is
480 wide, so it gets the default 400); changing the number resizes the picture.

### 1.8-07 · Pasting a screenshot

1. Copy a screenshot to the clipboard (see "The keys of your system").
2. Click on an empty line of the note and press Mod+V.
3. Copy a word from anywhere and press Mod+V.

**Pass when:** `![[Pasted image <date and time>.png|400]]` is written (its own width when narrower than 400) with the
screenshot under it, and the file is in the `attachments` folder; the copied word is pasted as text. On Linux with
Wayland (GNOME, KDE, COSMIC), pasting pictures needs the `wl-clipboard` package installed.

### 1.8-08 · Obsidian's attachment setting

1. Quit Notekea.
2. Open `Playground Vault/.obsidian/app.json` with the text editor (the `.obsidian` folder is hidden: on Windows turn on
   View › Show › Hidden items; on macOS press Cmd+Shift+. in Finder; on Linux press Ctrl+H) and add the setting, so it
   reads:

   ```json
   {
     "alwaysUpdateLinks": true,
     "attachmentFolderPath": "./"
   }
   ```

3. Start Notekea, open Editor/Images and files and paste a screenshot (1.8-07).

**Pass when:** the pasted picture is saved next to the note, in the `Editor` folder, as Obsidian would.

### 1.8-09 · The note in Obsidian

Without Obsidian: **SKIP**.

1. Open the Playground Vault in Obsidian and look at Editor/Images and files.

**Pass when:** the text is unchanged and every picture, the PDFs and the pasted screenshot show as in Notekea; only
Lost photo.png is gone, replaced by Found plate.png.

### 1.8-10 · Pictures inside a table

1. In Preview, scroll to the table at the end of Editor/Images and files.
2. Press Mod+E (Edit); at the end of the table add the row `| Cachapa |  |`, put the cursor in its empty second cell
   and choose More › Image or file; pick `Outside files/Found plate.png`.
3. Press Mod+E (Preview); then Mod+E again.

**Pass when:** the plate shows 80 wide inside the Photo cell, not the text "80"; the new embed is written inside the
cell with its width (`![[Found plate.png\|320]]`, the `|` escaped), the row keeps its two columns, and Preview shows the
green plate in it.

### 1.8-11 · Linux with Wayland only: pasting without wl-clipboard

Windows, macOS, and Linux without Wayland (X11): **SKIP**.

1. In the terminal run `wl-paste --version`. If it answers, uninstall it for this case: `sudo apt remove wl-clipboard`
   (Ubuntu, Pop!_OS) or `sudo dnf remove wl-clipboard` (Fedora).
2. Copy a screenshot, click in the note and press Ctrl+V; read the notice and click Close.
3. Install it again (`sudo apt install wl-clipboard` or `sudo dnf install wl-clipboard`), restart Notekea and press
   Ctrl+V in the note.

**Pass when:** without wl-clipboard, within about a second a notice says "To paste images on this desktop, install
wl-clipboard (for example: sudo apt install wl-clipboard) and paste again." with Close, and nothing is pasted; with it
installed the screenshot is pasted at once. On GNOME, write in the report whether it pasted.

### 1.8-12 · Files in the tree

1. In the sidebar open the `attachments` folder; click `Arepas plate.png`, then `Weekly menu.pdf`, then `broken.png`.
2. Click Weekly menu.pdf again; in its tab click **Open with its app**, then **Show in File Explorer** (Windows),
   **Show in Finder** (macOS) or **Show in Files** (Linux).
3. Right-click `shopping.txt`, choose Rename and type `groceries`; press Enter.

**Pass when:**
- The folder's number counts all its files; it lists them with their extensions, notes and files together by name:
  pictures with an image icon, the rest with a file icon.
- Each file opens in a tab of its own: Arepas plate.png shows "attachments · Image · 480 × 240"; Weekly menu.pdf shows
  its name with Open with its app and Show in Files; broken.png says Notekea can't show it and offers to open it.
- Open with its app opens the PDF viewer and Show in Files opens the folder with the file selected.
- The rename keeps the extension: `groceries.txt`.
- The files added in 1.8-05 to 1.8-07 are listed.

### 1.8-13 and 1.8-14 · A note inside a note

1. Open Editor/Images and files in Preview and scroll to the end.
2. Press Mod+E (Edit); click the title of the second embedded frame.
3. Go back to Images and files; drag any `.md` file from the file manager (for example
   `smoke-data/vaults/Personal Notes/Welcome.md`) onto the note.

**Pass when:** Preview shows "Shopping basics" in a frame with a blue bar on its left: its heading, the #shopping tag
and both lists, without the `tags:` frontmatter; below it a second frame titled "Shopping basics › Dairy" shows only
the Dairy section; in Edit both lines keep their text (`![[Shopping basics]]`, `![[Shopping basics#Dairy]]`) with the
same frames under them; clicking a title opens Shopping basics; the dropped note is copied into `attachments` and its
text shows in a frame too.

### 1.9-01 · The outline

Make the window at least 1,300 pixels wide (maximized is fine) with the sidebar shown for the 1.9 cases, except 1.9-07.

1. Open Editor/Outline practice in Edit.

**Pass when:** a panel on the right of the note, on **Inspector** (its Inspector | Chat control at the top), with an
OUTLINE section above NOTE INFO, lists: Arepas guide; Ingredients one step in; "For the filling and the shopping list"
two steps in (no `**` or brackets); Steps one step in; "Tip: resting the dough" and Cooking two steps in, at the same
step; Not headings one step in; Serving at the left; Last section one step in. `#recipe` and `# a comment` (in the code
block) are not listed; Arepas guide is highlighted.

### 1.9-02 · Going to a heading

1. Click "Steps" in the outline and type ` today`.
2. Click "Cooking".

**Pass when:** the note scrolls so the `## Steps` line sits right under the toolbar, with the cursor at its end; the
typing goes there and the outline shows "Steps today" a moment later; Cooking then scrolls to the top the same way and
is highlighted.

### 1.9-03 · The highlight follows the scroll

1. Scroll the note slowly with the mouse wheel from the top to the end.
2. Click "Last section".

**Pass when:** the highlight moves down the outline as each heading passes the top of the view (Arepas guide,
Ingredients… Serving, Last section); Last section stays highlighted after the click even though the note cannot scroll
it to the top.

### 1.9-04 · The outline in Preview

1. Press Mod+E (Preview); click "Serving" in the outline; scroll up and down.

**Pass when:** the rendered note scrolls so "Serving" sits right under the toolbar and it is highlighted; scrolling
moves the highlight as in Edit; no cursor is shown.

### 1.9-05 · The outline with the keyboard

1. Press Mod+E (Edit) and click in the note.
2. Press Mod+Shift+O, then ↓ twice and Enter; type `x`.
3. Press Mod+Shift+O again, then Esc; type `y`.
4. Press Mod+Z twice to undo both letters.

**Pass when:** Mod+Shift+O puts a focus ring on the highlighted heading; ↓ moves the ring; Enter goes to that heading
and `x` is typed at the end of its line; after Esc the keyboard is back in the note and `y` is typed where the cursor
was.

### 1.9-06 · Hiding and resizing the side panel

1. Hover the panel's minimize button (–) and the tool rail's **Side panel** button; read their tooltips and the
   OUTLINE header's.
2. Click the minimize button, then the Side panel button.
3. Press Mod+Shift+\ twice.
4. Drag the panel's left edge to its narrowest, then its widest.
5. Press Mod+Shift+\ to hide it; quit and restart Notekea; press Mod+Shift+O.

**Pass when:** the tooltips say "Minimize side panel" and "Side panel" with Mod+Shift+\, and the OUTLINE header shows
Mod+Shift+O; the panel slides closed and open each time and the note widens and narrows; the pointer becomes a resize
cursor and the panel stays between a narrow and a wide limit (280 to 560), never squeezing the note below about 480;
after the restart the panel is still hidden, at the width it was left; Mod+Shift+O shows it with a focus ring on a
heading.

### 1.9-07 · The panel over a narrow window

1. Make the window narrow (about 900 pixels wide) with the sidebar shown.
2. Click the tool rail's Side panel button and choose "Cooking".
3. Press Mod+Shift+O, then Esc.
4. Make the window wide again.

**Pass when:** when narrow the panel is not beside the note; Side panel opens it floating over the note's right side,
with a shadow; choosing Cooking goes there and closes it; Mod+Shift+O opens it with the focus ring and Esc closes it,
the keyboard back in the note; widened, the panel is beside the note again as it was left.

### 1.9-08 · A note without headings

1. Press Mod+N to create a note; look at the outline.
2. Type `# Hello` and wait a moment.

**Pass when:** the new note's outline says "This note has no headings" and how to add one; after typing, "Hello" is
listed and highlighted. Delete the new note afterwards.

### 1.10-01 · Today's note

"Today" is your computer's date. The Playground Vault has no Obsidian settings for daily notes, so they go to `Journal`
named like `2026-10-27`.

1. Hover the calendar (Today) on the tool rail; click it.
2. Click Welcome in the tree, then Today again.

**Pass when:** its tooltip says "Today" and Mod+D; the first click creates today's note in Journal, opens Journal in the
tree, selects the note and opens it empty; Journal's number grows by one; the second click opens the same note, no
second one is made.

### 1.10-02 · Mod+D from anywhere

1. Click in Welcome's text and type a word; press Mod+D.

**Pass when:** today's note opens at once, whatever had the keyboard; the word stays saved in Welcome.

### 1.10-03 · Previous and next day

1. Open Journal/2026-10-21; hover the ‹ and › beside its title.
2. Click ‹; then › twice; then › once more.
3. Press Mod+Alt+← and Mod+Alt+→.
4. Open Welcome and press Mod+Alt+←.

**Pass when:** a daily note shows ‹ and › beside its title, with "Previous daily note" and "Next daily note" and their
shortcuts as tooltips; ‹ opens 2026-10-20, where ‹ is greyed out; › twice reaches 2026-10-22, and from there › opens
today's note, where › is greyed out; the shortcuts step the same way and the tree follows; Welcome shows no arrows and
the shortcut does nothing. If your desktop uses Ctrl+Alt+arrows to switch workspaces, write it in the report.

### 1.10-04 · Daily notes with Obsidian's settings

1. Open `smoke-data/scratch/Daily Vault` and press Mod+D.

**Pass when:** today's note is created in Diary, in a folder for the year and one for the month (for October 27th 2026:
`Diary/2026/October/Tuesday, October 27th 2026`), made if missing; its text is the template filled in: the note's name
as its heading, "Created at" with the time it was made (`HH:mm`), "Plan for" today's weekday, and an empty task.

### 1.10-05 · Stepping over days without a note

1. From today's note press Mod+Alt+← until the arrow is greyed out.
2. Open Diary/Ideas.

**Pass when:** the days go back to Thursday, October 22nd 2026, then Monday, October 19th 2026, skipping days without a
note; Ideas is not a daily note: no arrows.

### 1.10-06 · Obsidian opens the same daily note

Without Obsidian: **SKIP**.

1. Open the Daily Vault in Obsidian (Daily notes core plugin on) and run "Open today's daily note" from the command
   palette (Mod+P in Obsidian).

**Pass when:** Obsidian opens the note Notekea created, with the same name and folder, instead of making another.

### 1.11-01 · The tool rail

Open the Playground Vault again and close every open tab with its ✕ before the 1.11 cases.

1. Hover each icon of the tool rail on the left edge.
2. Click the sidebar's Minimize (—); hover and click the rail's icons again (press Esc to close what opens); click
   **Files**.

**Pass when:** a narrow rail of icons stays on the left edge whether the sidebar is shown or not: Files (selected while
the sidebar is shown), Search notes, Today and Side panel (selected while it is shown), and at the bottom Open another
vault, About and Settings, each with a tooltip naming it and its shortcut (Mod+\, Mod+P, Mod+D, Mod+Shift+\); the
sidebar has no Search notes or Today rows and no New note or New folder buttons; with the sidebar hidden every icon
still works and nothing covers the tabs; Files brings the sidebar back.

### 1.11-02 · The sidebar's header

1. Hover the icons beside the vault's name.
2. Click Expand all; quit and restart Notekea.
3. Click Collapse all, then the sort icon (press Esc to close its menu).

**Pass when:** the header shows the vault's name, then Sort (its menu has Name and Date modified), Expand all, Collapse
all and Minimize, each with a tooltip; Expand all opens every folder and Collapse all closes them all; the open folders
come back after the restart.

### 1.11-03 · Tabs

1. In the sidebar click Welcome, then Recipes/Arepas, the Editor folder, and Welcome again.

**Pass when:** each opens in a tab of its own after the current one, with an icon and its name (notes without `.md`);
the active tab stands out; clicking Welcome again goes to its tab instead of opening a second one; the tree highlights
the active tab's file.

### 1.11-04 · Opening in the background

1. Hold Mod and click Recipes/Pasta e fagioli in the sidebar.
2. Click Café con leche with the middle mouse button (the wheel).
3. Open Editor/Read mode, press Mod+E (Preview) and Mod+click the "Arepas" link; press Mod+E again.

**Pass when:** each opens in a new tab behind the current one: the tab shown does not change.

### 1.11-05 · Closing tabs

1. Close a tab that is not active with its ✕; close another with the middle button; close the active one with Mod+W.
2. Right-click a tab and choose **Close other tabs**; then close the last one.

**Pass when:** each tab closes; closing the active one shows the tab to its right (or to its left when it was the
last); Close other tabs leaves only that one; with no tab left the area says "No open tabs" and offers Find a note, New
note, Today's note and Search in notes with their shortcuts.

### 1.11-06 · Moving between tabs

1. Open four notes from the sidebar.
2. Press Ctrl+Tab three times, Ctrl+Shift+Tab once (Control on macOS too).
3. Press Mod+1, Mod+3 and Mod+9.
4. Click in a note's text and press Ctrl+Tab.

**Pass when:** Ctrl+Tab goes to the next tab and wraps from the last to the first; Ctrl+Shift+Tab goes back; Mod+1
and Mod+3 go to the first and third tab, Mod+9 to the last; the shortcuts work while typing in a note.

### 1.11-07 · Splitting the window

1. Press Mod+Shift+D; press it again.
2. Right-click a tab of the left pane and look at its menu (press Esc).
3. Click in the left pane's note and press Mod+F (Esc), then Mod+Shift+\ twice.

**Pass when:** the active tab moves to a new pane on the right, which gets the keyboard; the second Mod+Shift+D does
nothing; the menu offers "Move to the other pane" instead of Split right; a click in a pane makes its tab the active
one, and Find opens in that pane only; the side panel's outline shows the headings of the active pane's note.

### 1.11-08 · Dragging tabs between panes

1. Drag a tab from the left pane and drop it on the right pane's tabs.
2. Close every tab of one pane.
3. Drag a tab to the right edge of the remaining pane.

**Pass when:** the tab moves to the end of the other pane's tabs and opens there; a pane without tabs closes and the
other fills the space; dropped on the right edge, the tab opens in a new pane on the right.

### 1.11-09 · A tab remembers where you were

1. Open Editor/Outline practice, scroll to its middle and click inside a word.
2. Click another tab, then come back.
3. Press Mod+E (Preview), scroll a little, switch tabs and come back; press Mod+E.

**Pass when:** the note shows the same part it showed and the cursor is where it was, in Edit and in Preview.

### 1.11-10 · Tabs after a restart

1. With two panes and a few tabs open, quit and restart Notekea.
2. Open Editor/Shared note in a tab; quit Notekea; run `delete-note`; start Notekea.

**Pass when:** the vault opens with the same tabs, the same active ones and the same split; the tab of the note deleted
while Notekea was closed is gone.

### 1.11-11 · Folder cards

1. Click the Editor folder in the tree, then the `attachments` folder; click a card of each.

**Pass when:** a folder opens in a tab as a grid of cards under its name and number of files: subfolders first, then
notes and files by name; "Images and files" shows the orange and blue plate as its card's picture, the others their
icon; in attachments, Arepas plate.png and Market photo.jpg show themselves, broken.png and the other files their
icon; a card opens its note, file or folder in a tab.

### 1.11-12 · The title bar is the system's

1. Look at the top of the window with tabs open.

**Pass when:** the tabs are inside the window, under the system's own title bar; the title bar and its buttons are the
system's.

### 1.11-13 · The window's size

1. With the Playground Vault as the last vault, quit Notekea, start it, quit and start it again.
2. Click Open another vault and look at the window; open the Playground Vault from the recent list.
3. Repeat step 2 twice more.
4. Resize the window by hand, then repeat step 2 once.

**Pass when:** the window opens centred, never larger than the screen, large enough to show the tool rail, the sidebar
with the tree, a note and the outline beside it; the welcome screen and the open vault keep that same size, and the
window never resizes itself when a vault opens or closes; a size you give it stays.

### 1.11-14 · Opening folders quickly

1. Close every tab and click Collapse all.
2. Open and close folders of the tree quickly, one after another, about twenty times.

**Pass when:** each folder opens in a tab as its grid and the tree opens and closes it; "Something went wrong" never
shows.

### 1.11-15 and 1.11-16 · Reordering tabs

1. Open Welcome, Recipes/Arepas and Ideas in tabs (close the others first).
2. Drag the Welcome tab slowly along the row and drop it between Arepas and Ideas.
3. Drag Ideas to the first place.

**Pass when:** while dragged, the tab stays on the row and the others slide aside to open the place it would take; once
let go it glides into place and stays active: Arepas, Welcome, Ideas, then Ideas, Arepas, Welcome; the tree selects the
dropped tab's file.

**End of session 3.**

---

## Session 4: templates, bookmarks, properties, embeds and pictures (1.12 to 1.15, 1.18)

Run `prepare` before starting. Templates use two vaults: `smoke-data/scratch/Playground Vault`, which has no
templates folder, and `smoke-data/scratch/Templates Vault`, set up as Obsidian would (templates in `Snippets`, dates
as `DD/MM/YYYY`, times as `h:mm A`), with `Projects/Kickoff notes` and `Projects/Empty note` to fill. "Today" and
"now" are your computer's clock.

### 1.12-01 · The first template

1. Open the Playground Vault; open Welcome, click at the end of its text and press Mod+T.
2. Read the picker and click **Create**.
3. Press Enter.

**Pass when:** the picker says "No templates folder yet" and "Create a Templates folder to add templates. It starts
with an example you can change." with a Create button; Create makes `Templates/Meeting notes` (it shows in the tree)
and lists it at once; Enter writes it at the cursor: "# Welcome", today's date (`YYYY-MM-DD`) and the time now
(`HH:mm`), Attendees and Action items; Welcome gains the property `tags: meeting`.

### 1.12-02 · Inserting a template

1. Open `smoke-data/scratch/Templates Vault`; open Projects/Kickoff notes, click at the end of its text and press
   Mod+T.
2. Type `wee`, then erase it.
3. Choose Meeting with the arrow keys and press Enter; look at the properties.

**Pass when:** the picker says "Find a template to insert" and lists Meeting and Work/Weekly review; `wee` leaves only
Work/Weekly review; Enter writes Meeting at the cursor with "# Kickoff notes", today's date as `DD/MM/YYYY` with its
weekday, and the time now as `h:mm` with AM or PM (like `2:05 PM`); the properties now hold the tags project and
meeting, `status: active` as it was, and `type: meeting`.

### 1.12-03 · A template in an empty note

1. Open Projects/Empty note; hover **Insert template** in the toolbar, then click it; press Enter on Meeting.

**Pass when:** its tooltip says "Insert template" and Mod+T; the empty note becomes the whole template: the properties
`tags: [meeting]` and `type: meeting`, then "# Empty note" and the rest of Meeting.

### 1.12-04 · A new note from a template

1. Right-click the Projects folder and choose **New note from template**; choose Meeting.
2. Type `Team sync` as its name and press Enter.

**Pass when:** the picker says "Choose a template for the new note"; an "Untitled" note appears in Projects with its
name ready to type and opens in a tab; once named, it is "Team sync" and holds Meeting with "# Team sync" and today's
date.

### 1.12-05 · Mod+T on a folder

1. Click the Projects folder (its tab shows the folder's cards) and press Mod+T; choose Work/Weekly review.
2. Press Esc to keep the name.

**Pass when:** the picker opens for a new note: an "Untitled" note is made in Projects and filled with "## Week of"
today's month and day, then Done and Next.

### 1.12-06 · Templates and undo

1. Open Projects/Kickoff notes, press Mod+E (Preview), then Mod+T, then Esc.
2. Select the word "Goals" and press Mod+T; choose Work/Weekly review.
3. Press Mod+Z.

**Pass when:** Mod+T switches the note to Edit; Esc closes the picker and gives the keyboard back to the text with the
selection kept; the template replaces "Goals"; Mod+Z undoes the whole template in one step.

### 1.12-07 · The same templates in Obsidian

Without Obsidian: **SKIP**.

1. Open the Templates Vault in Obsidian (Templates core plugin on); in Projects/Kickoff notes run "Templates: Insert
   template".

**Pass when:** Obsidian lists the same templates of Snippets and fills in the date and time in the same formats as
Notekea.

### 1.12-08 · Panel widths on a first launch

1. Quit Notekea; run `first-launch`; start Notekea.
2. Open `smoke-data/scratch/Playground Vault` and open a note; compare the sidebar's and the outline panel's widths.
3. Drag the sidebar's edge to make it wider; quit and restart.
4. Quit Notekea and run `restore-settings`.

**Pass when:** both panels open equally wide (340), the note between them; the width set by dragging stays after the
restart.

### 1.13-01 · Bookmarking the open note

Start Notekea and open the Playground Vault; it has no bookmarks yet.

1. Open Welcome; hover the star beside its title, then click it.
2. Click it again, then once more.

**Pass when:** its tooltip says "Bookmark this note" and Mod+Shift+B; the first click fills the star in yellow, a
"Bookmarks 1" section appears above the tree with Welcome, and a small yellow star follows Welcome's name in the tree;
the second removes it and the section goes; the third brings it back.

### 1.13-02 · Bookmarking from the tree

1. Right-click the Recipes folder and choose **Bookmark**.
2. Open `attachments`, right-click `Arepas plate.png` and choose Bookmark.
3. Open Ideas in a tab and press Mod+Shift+B.
4. Right-click a bookmarked row.

**Pass when:** the menu shows "Bookmark" with Mod+Shift+B; the section lists Welcome, Recipes, Arepas plate.png and
Ideas in that order, each with its icon; each bookmarked row in the tree shows a small yellow star; on a bookmarked row
the menu says "Remove bookmark", with the whole label in view.

### 1.13-03 · Opening bookmarks

1. Click Collapse all in the sidebar header.
2. Click the Arepas plate.png bookmark, then the Recipes one.
3. Click the "Bookmarks" header twice.

**Pass when:** each opens in a tab and is selected in the tree with its folders opened (attachments for the plate); the
header folds the list and unfolds it.

### 1.13-04 · Order and per-vault bookmarks

1. Drag the Ideas bookmark up above Welcome.
2. Quit and restart Notekea.
3. Click Open another vault and open `smoke-data/vaults/Work Obsidian`; then open the Playground Vault again.

**Pass when:** Ideas moves first and the others shift down; after the restart the bookmarks are in the same order;
Work Obsidian has none of them (bookmarks belong to each vault).

### 1.13-05 · Bookmarks follow renames and deletions

1. Rename the Recipes folder to `Cooking` in the tree.
2. Open Welcome and rename it to `Start here` from its title.
3. Delete the Ideas folder from the tree.

**Pass when:** the bookmarks follow: Cooking and Start here keep their places, and Arepas plate.png stays; Ideas'
bookmark goes with the folder.

### 1.13-06 · A bookmark whose note is gone

1. Open Editor/Shared note and click its star.
2. Quit Notekea; run `delete-note`; start Notekea.
3. Hover the bookmark's ✕ and click it.

**Pass when:** the Shared note bookmark is greyed out with a ✕, whose tooltip says "Remove: it is no longer in the
vault"; the click removes it.

### 1.13-07 · Bookmarks never touch the vault

1. With the file manager, look in `Playground Vault/.obsidian` (hidden folder, see 1.8-08) and the vault's folders.

**Pass when:** no file was added or changed by the bookmarks (there is no `bookmarks.json`).

### 1.14-01 · Properties

Run `prepare` again before 1.14 (1.13 renamed and deleted notes the next cases use). The `diff-properties` command
shows which lines of Editor/Properties practice differ from the original.

1. Open the Playground Vault; open Editor/Properties practice and look under the title.
2. Hover a property row.
3. Click the "Properties" header (with the number 7); quit and restart; click it again.
4. Run `diff-properties`.

**Pass when:** a soft card lists status (text), rating (number), done (checkbox), due (date, shown as `Nov 2, 2026`),
tags (`#` icon, chips `#recipe` `#weekend`), aliases (chips Practice and Form test) and source (`book: …`, "edit in
source"), each with its type's icon; hovering a row highlights it and shows its ✕; the header folds the block and it is
still folded after the restart; `diff-properties` prints "No change": opening never writes.

### 1.14-02 · Editing properties

Run `diff-properties` after each step.

1. Set status to `ready` and press Enter.
2. Type `five` in rating, then replace it with `5`.
3. Check done.
4. Type `2026-11-09` in due; then click the date again and pick the 10th on the calendar.
5. Add `brunch` to tags.
6. Remove the Practice alias with its ✕ and add `Notekea`.

**Pass when:** each edit is saved at once; `five` is outlined in red and not saved; due shows `2026-11-09` while edited
and `Nov 9, 2026` after; the calendar opens under the date (above it near the window's bottom), small, on the date's
month with weeks starting on Monday, and the pick closes it; `diff-properties` shows only the edited property's lines
changed each time, and `'Form test'` keeps its quotes.

### 1.14-03 · Adding, renaming and removing properties

1. Click **Add property** and choose Checkbox.
2. Rename `property` to `favorite`; then try `status`, then `bad: name`.
3. Remove `rating` with its ✕.

**Pass when:** a checkbox `property` appears at the end; it is renamed to favorite; `status` (taken) and `bad: name`
are outlined in red and not saved; rating disappears and the note keeps everything else.

### 1.14-04 · Properties in a note without any

1. Open Welcome (no properties); open the toolbar's More (⋯), point at **Add property** and choose Text.
2. Remove that property with its ✕.

**Pass when:** Welcome shows no Properties block until one is added; then the block appears with an empty text property
`property` and the note starts with a frontmatter; removing the last property removes the frontmatter too.

### 1.14-05 · Editing the YAML source

1. In Editor/Properties practice click **Show source**.
2. Change `page: 12` to `page: 13` and wait a second; run `diff-properties`; then click in the note's text.
3. Click **Show properties**.

**Pass when:** the YAML shows as written, comment included; it is saved a second after you stop typing
(`diff-properties` shows `page: 13` before you click anywhere); the form comes back with the nested source unchanged
but page 13.

### 1.14-06 · Broken properties

1. Open Editor/Broken properties.
2. In its source, delete the line `this line is not a property` and wait a second without clicking.
3. Click in the note's text.

**Pass when:** a red banner says the properties are not valid YAML (line 2), above the YAML as written; a second after
the fix the banner goes while the YAML stays open; the click shows the form with title.

### 1.14-07 · Properties in Preview

1. Open Editor/Properties practice, press Mod+E (Preview) and edit status; press Mod+E.
2. Open Recipes/Arepas.

**Pass when:** properties can be edited in Preview too; Arepas shows its tags, aliases and servings as properties.

### 1.14-08 · Note info

1. Open Editor/Tasks and callouts and look at NOTE INFO in the side panel.
2. Check "Buy corn flour".
3. Click the OUTLINE and NOTE INFO headers; quit and restart.
4. Press Mod+Shift+O.

**Pass when:** Note info lists Words 236, Reading time 2 min, Links 0, Tags 2, Tasks 4 of 9, Created (when `prepare`
ran, where the system keeps it) and Modified; after the check Tasks says 5 of 9 and Modified shows now; each header
folds its section and both are still folded after the restart; Mod+Shift+O opens the outline again with its focus
ring.

### 1.14-09 · Chat, coming soon

1. With a note open, click **Chat** in the side panel's Inspector | Chat control; try to type in its field.
2. Quit and restart; press Mod+Shift+O.

**Pass when:** Chat fills the panel with "Chat with your notes", what it will do and a "Coming soon" badge, above a
dashed, dimmed "Your questions will go here" box that takes no typing; after the restart the panel is still on Chat;
Mod+Shift+O goes back to Inspector with the outline focused.

### 1.14-10 · The rail, About and Settings

1. Hover each icon at the bottom of the tool rail.
2. Click **About**, look at it and press Esc.
3. Click **Settings**, then close it with ✕.
4. Click the rail's Side panel button twice.
5. Click **Open another vault**; then open the Playground Vault again.

**Pass when:**
- The rail's top has the Notekea logo, then Files, Side panel, Search notes and Today; its bottom has Open another
  vault, About Notekea and Settings.
- The open note's tab rises with rounded corners into the note; the other tabs are quiet with a thin line between
  them and show their ✕ only on hover.
- The sidebar has no "Open another vault" button, and the toolbar no Outline button.
- About shows the logo, Notekea, "Version 0.1.0-alpha", GitHub and the shortcuts of your system as keycaps (Notes,
  Writing, Workspace) with a scrollbar beside them; Esc closes it.
- Settings shows its sections dimmed and "Coming soon".
- Side panel hides and shows the panel, selected while it is shown.
- Open another vault shows the welcome screen: the logo, Notekea and the actions on the left, the recent vaults on the
  right.

### 1.14-11 · Scrollbars

1. Run `prepare`. With Open another vault, open one after another: Personal Notes (in `vaults`), Work Obsidian (in
   `vaults`), Empty Vault (in `vaults`), then `scratch/Playground Vault`, `scratch/Daily Vault` and
   `scratch/Templates Vault`.
2. Click Open another vault and scroll the recent list.
3. Open the Playground Vault, click Expand all and scroll the tree.
4. Open Editor/Outline practice and scroll its outline.

**Pass when:** the recent list shows five vaults with a thin scrollbar on its right and scrolls inside to the sixth;
the tree and the outline show a scrollbar only while they have more than fits, and it darkens when hovered.

### 1.14-12 · The status bar

1. Run `clear-cache`; open `smoke-data/vaults/Ghost Serializer` and watch the bottom of the window closely while it
   loads (it can be quick).
2. Run `prepare`; open the Playground Vault.
3. Open Editor/Tasks and callouts and click at the start of "Buy corn flour"; hover the line:column on the right.
4. Open Editor/Windows line endings; press Mod+E (Preview); press Mod+E again.
5. Open the Recipes folder in a tab.

**Pass when:** a thin bar runs along the bottom of the window; while Ghost Serializer loads it shows "Loading notes…"
with a moving bar; then "Playground Vault › Editor › Tasks and callouts" on the left and "8:7 | LF | UTF-8 | 236 words"
on the right (hovering 8:7 says "Line 8, column 7"); Windows line endings shows CRLF; in Preview the line:column goes
away; the folder's tab shows only "Playground Vault › Recipes".

### 1.14-13 · Many tabs and focus

1. Open ten notes in tabs and make the window narrower.
2. Click the ⌄ at the end of the tab strip and pick a tab.
3. Click a note in the tree, then click in the note's text; look at the tree's selected row each time.
4. Type a word and watch the end of the status bar.
5. Hover the vault's name at the top of the sidebar.

**Pass when:** the strip fades out at its end and the ⌄ lists every open tab, the open one checked, and picks one; no
tab is narrower than a short name; the tree's selected row is blue while the tree has the keyboard and grey once the
text has it, with no ring around it; the sidebar shows BOOKMARKS (when there are bookmarks) and FILES headers; the
panes meet at thin lines with no dark gap; the status bar ends with "Unsaved" while typing, then "Saved"; the vault's
folder shows only in the name's tooltip.

### 1.14-14 · Page width and zoom

1. Open Editor/Outline practice with the window maximized; then make the window half the screen wide.
2. Press Mod+= three times, Mod+- once (Ctrl on Windows and Linux, Cmd on macOS).
3. Quit and restart Notekea.
4. Press Mod+0; open About and look at its Workspace group; press Esc.

**Pass when:** maximized, the note's page is wider than at half width but its lines stop well before the right panel,
with the same margin on both sides; at half width the page fills the pane between its margins; each Mod+= makes the
whole window larger (text, icons, rail, tabs) and Mod+- smaller; the restarted app keeps that size; Mod+0 brings it
back to normal; About's Workspace group lists Zoom in, Zoom out and Actual size.

### 1.14-15 · Small details

1. Run `prepare`; open the Playground Vault; bookmark Welcome (its star) and hover the BOOKMARKS header.
2. Open Editor/Formatting practice and type a word, watching its tab.
3. Click "Editor" in the status bar; hide the sidebar with Mod+\ and click the folder name above the note's title.
4. Press Mod+= and click the zoom at the end of the status bar.
5. Narrow the window until the toolbar's buttons leave it and open More (⋯); widen it again.
6. Fold and open OUTLINE in the side panel.
7. Close every tab and try each of the four start actions (press Esc or close what each opens).
8. Expand `attachments` in the tree.

**Pass when:** no tooltip covers the first bookmark; the tab shows a dot in place of its ✕ while the note is unsaved
(the ✕ when the pointer is on it); each click shows the Editor folder selected in Files, the sidebar opening again;
"110 %" shows at the end of the status bar and clicking it brings the window back to normal size, the zoom leaving the
bar; the toolbar stays one line and More starts with the buttons that left it; OUTLINE slides shut and open; with no
tab the editor offers Find a note, New note, Today's note and Search in notes, each with its shortcut, and each works;
in attachments the pictures' icons are green and the PDF's red.

### 1.15-01 · Embedded notes

Every 1.15 case runs in the Playground Vault, in `Editor/Embeds/Embeds practice`, in order. Its notes embed each other
in a chain (Pantry › Shelf › Bin › Deep corner), in a circle (Loop A and Loop B), and by blocks (Blocks).

1. Open Editor/Embeds/Embeds practice, press Mod+E (Preview) and scroll through it.

**Pass when**, in order: a Pantry frame; a Loop A frame; for the note itself a muted frame with a yellow bar saying
"Circular embed: this note is already shown above, so it stops here."; a "Shopping basics › Dairy" frame with the Dairy
list; a muted frame "Shopping basics › Dessert" saying it has no heading "Dessert", with Open note; three Blocks
frames; a muted frame saying Blocks has no block "^nothing", with Open note; a muted "Weekend menu" frame saying "This
note does not exist yet." with Create note; the last line keeps "see Pantry" as a link inside the sentence.

### 1.15-02 · Embeds inside embeds

1. Look inside the Pantry and Loop A frames.

**Pass when:** Pantry shows its text (no `place:` frontmatter), the plate 120 wide and a Shelf frame inside it; Shelf
holds a Bin frame; Bin holds a muted "Deep corner" frame with a yellow bar, "Too deep to show: embeds go 3 notes deep
at most." and Open note; Loop A holds a Loop B frame, which holds a muted "Loop A" frame with a yellow bar saying
"Circular embed…".

### 1.15-03 · Embedded blocks

1. Look at the three Blocks frames.

**Pass when:** they are titled "Blocks › ^dough", "Blocks › ^corn" and "Blocks › ^prices", and show the dough paragraph
("Soak the corn flour… into discs."), only the "Buy fresh corn" item, and the price table, none with its `^` marker.

### 1.15-04 · Open note and Create note

1. Click Open note in the Dessert frame; go back to the Embeds practice tab.
2. Click Open note in the Deep corner frame; go back.
3. Click Create note in the Weekend menu frame; go back to Embeds practice.

**Pass when:** Shopping basics opens, then Deep corner; Create note creates `Weekend menu.md` at the top of the vault,
selected in the tree, and opens it in a tab; back in Embeds practice, the Weekend menu frame is a normal frame with its
title in blue and no text.

### 1.15-05 · Embeds in Edit

1. Press Mod+E (Edit) and scroll through Embeds practice.
2. Type a word above the `![[Pantry]]` line.

**Pass when:** every embed line keeps its text as written (`![[Pantry]]`, `![[Blocks#^corn]]`…), with the same frames
and notices right under it as in Preview; typing above moves them along with their lines.

### 1.18-01 · Leaving a vault right after typing

Run `prepare` before the 1.18 cases. The `attachments` folder of the Playground Vault now also holds
`Market photo.jpg`, a 4000 × 3000 photo.

1. Open the Playground Vault and Recipes/Arepas; click at the end of its text.
2. Type a word and at once, before the status bar says "Saved", click **Open another vault** in the tool rail.
3. Open the Playground Vault again from the recent list and open Arepas.
4. Open `Recipes/Arepas.md` with the text editor.

**Pass when:** the welcome screen shows; Arepas ends with the word, and so does its file.

### 1.18-02 · A large photo on a card

1. Open the `attachments` folder in a tab (click its name in the tree) and look at the Market photo.jpg card.
2. Click the card.

**Pass when:** the card shows the photo cropped to the card and sharp ("Market photo 4000 x 3000" readable at its top);
the file opens in a tab and shows the whole photo, sharp.

### 1.18-03 · Pictures come back after another note

1. Open Editor/Images and files and press Mod+E (Preview).
2. Press Mod+P, type `arepas` and press Enter: Arepas opens in a tab of its own.
3. Click the Images and files tab again; press Mod+E.

**Pass when:** the pictures of Images and files show again as before, at their widths.

### 1.18-04 · Names in natural order

1. Press Mod+P and type `2026-10`; press Esc.
2. Open the Journal folder in the tree.

**Pass when:** the palette lists 2026-10-20, 2026-10-21 and 2026-10-22 in that order; the tree lists them in the same
order.

**End of session 4. Thank you!**
