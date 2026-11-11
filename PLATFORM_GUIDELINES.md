# Notekea — Platform UI Guidelines

How Notekea looks and behaves on Windows, macOS and Linux.

## Principle

**Native window frame + one Notekea design inside + each platform's conventions.**

- There is one visual design, not three: since the macOS redesign (2026-11-11, `docs/macos-redesign.md`) it follows
  macOS's Human Interface Guidelines on every system: macOS's type scale, a source list, push buttons, alerts. It reads
  well on Windows and Linux too, where the controls stay compact and familiar.
- The window frame and the behaviors users rely on (shortcuts, dialogs) follow each platform, so the app feels at home
  everywhere. macOS also gets what only it has: a unified title bar (§1) and a menu bar (§4).
- Platform differences are resolved in one place (see "Implementation"), never with `if (isMac)` scattered across
  composables.

## 1. Window frame

- Use the **native window decorations** (`Window` with the default `undecorated = false`). The operating system
  draws close, minimize and maximize:
  - macOS: traffic lights on the left.
  - Windows: minimize / maximize / close on the right.
  - Linux: decided by the desktop environment and the user's theme.
- Never draw macOS-style traffic lights on Windows or Linux, or Windows-style buttons on macOS.
- **macOS: a unified title bar** (redesign 1A). The content runs under a transparent title bar with no title
  (`apple.awt.fullWindowContent`, `apple.awt.transparentTitleBar`, `MacTitleBar`).
  - The sidebar reaches the top with the traffic lights on its color, and the tabs share the title row.
  - `LocalTitleBarSpace` gives the band and the lights' width. The rail and the tree leave the band free
    (`NotekeaTitleBarBand`), and the first pane's tabs start after the lights when the tree is hidden.
  - The band and the empty part of the tabs row move the window, and a double click maximizes it.
- **Windows and Linux** keep their native title bar above the content.
- Window controls are never drawn by hand, on any system.

## 2. Content inside the window

- Identical on every platform: editor, note list, related notes panel and chat.
- Colors, typography, spacing, shapes and icons come from Notekea's theme tokens. No hard-coded colors in
  composables (`Color(0x...)` only in the theme).
- Support light and dark mode, following the system setting by default.
- **Surfaces:** the note is `surface`. The sidebar (rail, tree, side panel) is `sidebarSurface`, the row of tabs is
  `chromeSurface`, and the status bar is the note's color. Every text on them reaches WCAG AA in the 11 themes
  (`ColorThemeSchemesTest`).
- **Controls:** buttons and pop-up buttons are macOS's push buttons, `PushButtonHeight` (24 dp) with `PushButtonRadius`
  (5 dp). Text fields keep `ControlHeight` (32 dp).
- **Accent:** the theme's own, or the system's on macOS and Windows when Appearance asks for it, moved until it reads
  at AA (`withAccent`).

## 3. Keyboard shortcuts

The primary modifier is **Cmd on macOS** and **Ctrl on Windows and Linux**. Shortcuts are declared once with the
primary modifier and resolved per platform.

| Action | macOS | Windows / Linux |
|---|---|---|
| New note | `Cmd+N` | `Ctrl+N` |
| Open the selected entry in Files | `Cmd+↓` (Enter renames, as in Finder) | `Enter` |
| Rename the selected entry in Files | `Enter` | `F2` |
| Save | `Cmd+S` | `Ctrl+S` |
| Find in note | `Cmd+F` | `Ctrl+F` |
| Quick switcher (find a note by name) | `Cmd+P` | `Ctrl+P` |
| Search all notes | `Cmd+Shift+F` | `Ctrl+Shift+F` |
| Toggle the task on the cursor line | `Cmd+Enter` | `Ctrl+Enter` |
| Bold | `Cmd+B` | `Ctrl+B` |
| Italic | `Cmd+I` | `Ctrl+I` |
| Link to a note | `Cmd+K` | `Ctrl+K` |
| Web link | `Cmd+Shift+K` | `Ctrl+Shift+K` |
| Show or hide Files (the sidebar) | `Cmd+\` | `Ctrl+\` |
| Show or hide the side panel | `Cmd+Shift+\` | `Ctrl+Shift+\` |
| Source (the note as written) | `Cmd+E` | `Ctrl+E` |
| Go to the outline | `Cmd+Shift+O` | `Ctrl+Shift+O` |
| Ask your notes (the chat) | `Cmd+L` | `Ctrl+L` |
| Today's daily note | `Cmd+D` | `Ctrl+D` |
| Previous / next daily note | `Cmd+Option+←` / `Cmd+Option+→` | `Ctrl+Alt+←` / `Ctrl+Alt+→` |
| Insert template | `Cmd+T` | `Ctrl+T` |
| Bookmark the active note, folder or file | `Cmd+Shift+B` | `Ctrl+Shift+B` |
| Zoom in / zoom out / actual size (the whole window, remembered) | `Cmd+=` / `Cmd+-` / `Cmd+0` | `Ctrl+=` / `Ctrl+-` / `Ctrl+0` |
| Settings (from 5.2; until then the tool rail's Settings) | `Cmd+,` | `Ctrl+,` |
| Close tab | `Cmd+W` | `Ctrl+W` |
| Next / previous tab | `Ctrl+Tab` / `Ctrl+Shift+Tab` | `Ctrl+Tab` / `Ctrl+Shift+Tab` |
| Go to tab 1…8, last tab | `Cmd+1`…`Cmd+8`, `Cmd+9` | `Ctrl+1`…`Ctrl+8`, `Ctrl+9` |
| Split right | `Cmd+Shift+D` | `Ctrl+Shift+D` |
| Close window | `Cmd+Shift+W` | `Ctrl+Shift+W` |
| Quit | `Cmd+Q` | `Alt+F4` on Windows, `Ctrl+Q` on Linux |

Never reuse a shortcut the operating system reserves (e.g. `Cmd+H` / `Cmd+M` on macOS). About lists every shortcut
of the platform as keycaps, so a new shortcut is added there too (`AboutShortcuts`).

## 4. Menu bar

- **macOS** has a menu bar at the top of the screen (`MacMenuBar`, `apple.laf.useScreenMenuBar`): File, Note, View, Go
  and Help.
  - Each item is one of the app's shortcuts, shown beside it, and does what pressing it does
    (`ShortcutDispatcher.trigger`).
  - A press the window just handled is not done again by the menu.
  - A new window-level shortcut that belongs in a menu is added there too.
  - The `Notekea` menu's About, Settings… (`Cmd+,`) and Quit Notekea (`Cmd+Q`) open the app's own dialogs.
- **Windows and Linux** have none: Swing would draw one in the window, outside the app's theme. Commands live in the
  tool rail, the toolbars and their shortcuts, which About lists.

## 5. Dialogs

- File and folder pickers are native (FileKit).
- Confirmation button order:
  - macOS and Linux (GNOME): `Cancel` on the left, primary action on the right.
  - Windows: primary action first, then `Cancel`.
- Destructive actions use a verb (`Delete note`), never just `OK`.
- On macOS a confirmation is macOS's alert (redesign 8A). The app's icon sits over a centered title, and the buttons
  are stacked, the action over Cancel (`stacksAlertButtons`). Return chooses the safe button when the action
  destroys, and Esc cancels.

## 6. Typography

- One Notekea typeface bundled with the app (Inter) for headings and UI, so text looks the same everywhere.
- **The interface uses macOS's type scale** (redesign 3A): text 13, small 11, titles 15, 17 and 22. A `Text` with no
  style is interface text (`bodyMedium`).
- **The note has its own reading scale,** in slots no interface text uses: `displayMedium` (H1 and the title, 28),
  `displaySmall` (H2, 20) and `bodyLarge` (the body and H3–H6, 16). Note content (the editor, the Markdown renderer,
  documents) names these slots, so changing the interface's scale never resizes a note.
- The note editor uses a monospaced font for code blocks, also bundled.

## 7. System integration

- Tray icon (optional, off by default) via Compose's `Tray`, available on the three platforms.
- Notifications through the tray API, only for events the user asked for (e.g. sync conflicts).
- Application data in the platform's standard location (macOS `~/Library/Application Support/Notekea`,
  Windows `%APPDATA%\Notekea`, Linux `$XDG_DATA_HOME/notekea`), never next to the executable.
- Pasting images: Java's clipboard works on Windows, macOS and Linux on X11. On Linux Wayland desktops (GNOME, KDE,
  COSMIC) Java reads the clipboard through XWayland, which does not pass images on, so they are read with `wl-paste`
  first and Java waits one second at most; without wl-clipboard (GNOME does not install it) the editor says to
  install it.

## Implementation

- `PlatformConventions` (in `commonMain`, provided by Koin) exposes the current OS and the platform conventions:
  primary shortcut modifier, quit shortcut, dialog button order, app data directory.
- It is computed by `ResolvePlatformConventionsUseCase` from the `SystemEnvironment` port, whose desktop adapter
  `JvmSystemEnvironment` is the only code allowed to read the OS name (enforced by a Konsist test).
- Menus, shortcuts and dialogs read `PlatformConventions`; composables never check the OS name directly.
- Every platform rule above is covered by unit tests on the use case, simulating each OS with a fake environment,
  and those tests run on all three OSes in CI.

## References

- [Apple Human Interface Guidelines — macOS](https://developer.apple.com/design/human-interface-guidelines/designing-for-macos)
  (also available to agents as the `macos-design-guidelines` skill in `.agents/skills`)
- [Windows app design (Fluent)](https://learn.microsoft.com/windows/apps/design/)
- [GNOME Human Interface Guidelines](https://developer.gnome.org/hig/)
- [Compose Desktop components: menus, tray, windows](https://kotlinlang.org/docs/multiplatform/compose-desktop-components.html)
