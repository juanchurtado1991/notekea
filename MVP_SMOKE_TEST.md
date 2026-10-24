# Notekea — MVP Smoke Test

The full manual test run before releasing v0.1.0 (`ROADMAP.md` 5.4). Unit tests and CI prove the parts work; this
list proves the **app** works for a real person on a real machine. It is the main source of bugs before release.

## How this file works

- **Every feature pull request adds its cases here**, under its milestone, in the same pull request
  (`GUIDELINES.md` §8). A case is something a person can do and see, written so that someone who never saw the
  feature can follow it.
- Cases that depend on a feature not built yet say so in **Testable from**; they still go in now, so nothing is
  forgotten.
- Before the release, the whole list is run on **Windows, macOS and Linux** with a packaged build (not
  `./gradlew run`). Each OS column gets ✅, ❌ (with a link to the bug issue) or — (not applicable).
- Test data lives in `smoke-data/` (sample vaults plus `smoke.sh` / `smoke.ps1` for scenarios Git cannot hold);
  every case that needs data names it there.
- Bugs found are filed as issues, fixed on `fix/` branches, and the failing case is run again.

## Test environment (fill in for the release run)

| OS | Version | Hardware (CPU / RAM / GPU) | Build | Tester | Date |
|---|---|---|---|---|---|
| Windows | | | | | |
| macOS | | | | | |
| Linux | | | | | |

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
| 1.3-01 | Run `smoke.sh first-launch`, then start the app | Welcome screen: "Notekea" wordmark, tagline, "Open folder…" (with the Ctrl+O / ⌘O hint) and "Create new vault"; no recent list | 1.3 | | | |
| 1.3-02 | Click "Open folder…" and choose `smoke-data/vaults/Personal Notes` | The native folder picker opens; the vault opens as "Personal Notes" in the window title with "12 notes found" (`.obsidian`, `.trash` and the `.txt` file are not counted) | 1.3 | | | |
| 1.3-03 | Quit and relaunch the app | The last vault reopens directly, without flashing the welcome screen | 1.3 | | | |
| 1.3-04 | Click "Open another vault", open `Work Obsidian` and `Empty Vault` ("No notes yet"), then check the recent list | Vaults appear most recent first with their folder (`~/…` on macOS and Linux) and when they were opened ("Just now", "2 days ago"…) | 1.3 | | | |
| 1.3-05 | Press Ctrl+O (⌘O on macOS) on the welcome screen and inside an open vault | The folder picker opens in both places; cancelling it changes nothing | 1.3 | | | |
| 1.3-06 | Run `smoke.sh prepare`, open `Movable Vault`, quit, run `smoke.sh move` and relaunch | The vault is muted with "Folder not found", "Remove" and "Locate…"; "Locate…" lets you pick its new place and opens it; "Remove" only removes it from the list | 1.3 | | | |
| 1.3-07 | Click "Create new vault", keep the suggested name and location, and create it | A "My Notes" folder is created in Documents (or the home folder) and opens empty ("No notes yet"); nothing else is written inside it | 1.3 | | | |
| 1.3-08 | Create "My Notes" in `New Vault Location` (from `prepare`), then a vault named `a/b` | A plain-language error explains each problem and the form stays open with the name to fix | 1.3 | | | |
| 1.3-09 | Open `Locked Vault` (from `prepare`) | An error banner names the folder and says to check its permissions; "Dismiss" hides it | 1.3 | | | |
