# Notekea — Platform UI Guidelines

How Notekea looks and behaves on Windows, macOS and Linux.

## Principle

**Native window frame + one Notekea design inside + each platform's conventions.**

- There is one visual design, not three. Notekea has its own identity (colors, typography, icons) and does not
  imitate macOS or Windows inside the window, the same way Obsidian, Notion or Linear do.
- The window frame and the behaviors users rely on (shortcuts, menus, dialogs) follow each platform, so the app
  feels at home everywhere.
- Platform differences are resolved in one place (see "Implementation"), never with `if (isMac)` scattered across
  composables.

## 1. Window frame

- Use the **native window decorations** (`Window` with the default `undecorated = false`). The operating system
  draws close, minimize and maximize:
  - macOS: traffic lights on the left.
  - Windows: minimize / maximize / close on the right.
  - Linux: decided by the desktop environment and the user's theme.
- Never draw macOS-style traffic lights on Windows or Linux, or Windows-style buttons on macOS.
- A custom title bar (tabs or search inside the title bar) is out of scope for the MVP. If it is ever needed, use
  JetBrains Jewel's `DecoratedWindow`, which keeps native controls per platform. Do not draw window controls by hand.

## 2. Content inside the window

- Identical on every platform: editor, note list, related notes panel and chat.
- Colors, typography, spacing, shapes and icons come from Notekea's theme tokens. No hard-coded colors in
  composables (`Color(0x...)` only in the theme).
- Support light and dark mode, following the system setting by default.

## 3. Keyboard shortcuts

The primary modifier is **Cmd on macOS** and **Ctrl on Windows and Linux**. Shortcuts are declared once with the
primary modifier and resolved per platform.

| Action | macOS | Windows / Linux |
|---|---|---|
| New note | `Cmd+N` | `Ctrl+N` |
| Save | `Cmd+S` | `Ctrl+S` |
| Find in note | `Cmd+F` | `Ctrl+F` |
| Quick switcher (find a note by name) | `Cmd+P` | `Ctrl+P` |
| Search all notes | `Cmd+Shift+F` | `Ctrl+Shift+F` |
| Settings | `Cmd+,` | `Ctrl+,` |
| Close tab | `Cmd+W` | `Ctrl+W` |
| Close window | `Cmd+Shift+W` | `Ctrl+Shift+W` |
| Quit | `Cmd+Q` | `Alt+F4` on Windows, `Ctrl+Q` on Linux |

Never reuse a shortcut the operating system reserves (e.g. `Cmd+H` / `Cmd+M` on macOS).

## 4. Menu bar

| | macOS | Windows / Linux |
|---|---|---|
| Location | Global menu bar at the top of the screen | Inside the window |
| App menu | `Notekea` menu with About, Settings… (`Cmd+,`) and Quit Notekea (`Cmd+Q`) | No app menu |
| About | In the `Notekea` menu | In the `Help` menu |
| Settings | In the `Notekea` menu | In the `File` menu (or `Edit` on Linux desktops that expect it) |
| Quit / Exit | Provided by macOS in the app menu | Added by us: `File → Exit` |

- On macOS, enable the global menu bar (`apple.laf.useScreenMenuBar=true`).
- Menu items without a shortcut must pass an explicit "no shortcut"; otherwise Compose may show an unwanted one
  on Windows.

## 5. Dialogs

- File and folder pickers are native (FileKit).
- Confirmation button order:
  - macOS and Linux (GNOME): `Cancel` on the left, primary action on the right.
  - Windows: primary action first, then `Cancel`.
- Destructive actions use a verb (`Delete note`), never just `OK`.

## 6. Typography

- One Notekea typeface bundled with the app for headings and UI, so text looks the same everywhere.
- The note editor uses a readable body font and a monospaced font for code blocks, both bundled.

## 7. System integration

- Tray icon (optional, off by default) via Compose's `Tray`, available on the three platforms.
- Notifications through the tray API, only for events the user asked for (e.g. sync conflicts).
- Application data in the platform's standard location (macOS `~/Library/Application Support/Notekea`,
  Windows `%APPDATA%\Notekea`, Linux `$XDG_DATA_HOME/notekea`), never next to the executable.

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
  (also available to agents as the `macos-design-guidelines` skill in `.claude/skills`)
- [Windows app design (Fluent)](https://learn.microsoft.com/windows/apps/design/)
- [GNOME Human Interface Guidelines](https://developer.gnome.org/hig/)
- [Compose Desktop components: menus, tray, windows](https://kotlinlang.org/docs/multiplatform/compose-desktop-components.html)
