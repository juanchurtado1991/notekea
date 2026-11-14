# macOS redesign (5.x, PR #142)

The developer approved every recommendation of the macOS Human Interface Guidelines review on 2026-11-11. The
options page is the artifact https://claude.ai/artifact/WyxniDuGJM9yuLzs1SXSbX. One rule comes with the approval:
**the close, minimize and maximize buttons stay the system's own on every platform**, so they are never drawn by
Notekea.

This file is the dependency audit the developer asked for before anything was applied, and the order of the work.

## The picks

| # | Pick | What changes |
|---|---|---|
| 3A | macOS type scale with Inter | Interface text is 13 (it was 14), small text 11 and titles 15 / 17 / 22. The note keeps its own scale. |
| 4A | Sidebar and chrome surfaces | Two new color roles in all 11 themes: the sidebar a clearer gray than the note, and the chrome (tabs) between them. |
| 2A | Source list | Section headers, folder icons in the accent color, the selected row filled with the accent, and no guides or count pills. |
| 5A | Push buttons | Buttons 24 dp tall with radius 5, and a white secondary button with a fine border. The pop-up button gets a double arrow. |
| 1A | Unified title bar on macOS | The sidebar reaches the top, and the tabs share the title row with the system's own traffic lights. Windows and Linux keep their title bar. |
| 6A | The note's page | More space at the top, the folder above the title, and one line with the date, the word count and the tags. |
| 7B | Chat as a document | The answer as note text, and sources as file · line. The question was in bold over it; since 2026-11-14 it is a bubble at the right, at the developer's request: one voice read as two. |
| 8A | Compact alert | On macOS, Return chooses the safe button and the buttons are stacked. Windows and Linux keep a row. |
| 9A | Springs without bounce | `motionSpring()` beside `motionTween()`, which snaps when motion is reduced. |

**Extras:**
- The menu bar (File, Edit, View, Window, Help).
- A sidebar toggle in the title row.
- The status bar on the note's color.
- Popovers for rename and new folder (8C).
- An optional system accent color (4B).
- High-contrast checks of the new colors.

## Dependencies found, and how each is handled

- **The note's headings share the interface's text styles.** `MarkdownStyles` and `NotekeaMarkdown` take H1–H6
  from `headlineMedium`, `titleLarge` and `titleMedium`, so 3A alone would shrink every note's headings.
  - **Handled:** the note gets its own slots, which no interface text uses: `displayMedium` (H1, 28),
    `displaySmall` (H2, 20) and `bodyLarge` (H3–H6 at 16, and the body).
  - `NoteTextSize` already scales every slot, so Settings → Editor → Text size keeps working.
- **Some interface text uses Material's default font.** `titleSmall`, `labelSmall` and `headlineSmall` were never
  given Inter, so they fell back to the system's font.
  - **Handled:** 3A gives every slot Inter, in the macOS sizes.
- **Settings, as already rebuilt.** It uses `NotekeaSettingsGroup`, `NotekeaSettingsRow`, `NotekeaSwitch` and
  `NotekeaPopUpButton`.
  - **Handled:** they read the type scale and `ControlHeight`, so 3A and 5A reach them without a change of their
    own.
- **Row heights.**
  - 28 dp tree rows are the macOS sidebar's size at 13 pt, so they stay.
  - `ControlHeight` (32) is used by text fields as well as buttons. 5A gives buttons a size of their own
    (`PushButtonHeight`, 24), and fields keep 32 until they are restyled.
- **Colors.**
  - `ColorThemeSchemesTest` checks WCAG AA for every text on every background of the 11 themes, in both modes.
  - The new roles join it: ink, muted text and links on the sidebar and on the chrome, and the selected row's text
    on its accent fill.
- **High contrast.** Notekea has no high-contrast mode, so there is nothing to vary. The new colors pass AA in every
  theme, and a mode that follows the system's "Increase contrast" is left for after the MVP.
- **The title bar (1A).**
  - It applies on macOS only, through the window's `apple.awt.fullWindowContent` and
    `apple.awt.transparentTitleBar`. The traffic lights stay the system's own.
  - The title row leaves their width free at the start, and the row stays empty in full screen, where macOS hides
    them.
  - Windows and Linux keep their native title bar.
- **Previews.** Each changed component keeps its preview, and the design system gallery shows the new controls.
- **Auto-Test.**
  - Automated cases match text and roles, not sizes, so the redesign does not touch them.
  - The cases that look (1.2-0x, 1.14-1x, 2.12-0x) get their expected results reworded where the look changed.
  - Screen tests that measure (toolbar fit, compact controls) are run after each step.
- **Platform conventions.**
  - 8A's stacked buttons and 1A's title row branch on `PlatformConventions.operatingSystem`, which the app already
    uses for shortcuts and file managers.
  - The previews run with each system's conventions.

## Order of the work

1. 3A: the type scale, with the note's own slots.
2. 4A: the sidebar and chrome surfaces, and the status bar on the note's color.
3. 2A: the source list.
4. 5A: the push buttons and the pop-up button.
5. 1A: the unified title bar on macOS.
   - The row of tabs and the sidebar's top band move the window, and a double click maximizes it.
   - The sidebar toggle stays the rail's Files button, right under the traffic lights. A second toggle in the title
     row would do the same thing twice.
6. 6A: the note's page header.
   - The line under the title shows when the note was last edited and its word count.
   - The extra space above the page stays at 24 dp: 37 screen tests click the page at fixed points, and they move
     first.
7. 7B: the chat as a document.
8. 8A: the alert.
   - 8C, the popovers for rename and new folder, is not built. Both already edit the name in place, in the tree's
     row, as Finder does, and a popover over the row would add a step.
9. 9A: the springs.
10. The menu bar.
11. 4B: the system accent.
    - A switch in Settings → Appearance turns it on. It reads the accent from macOS's `defaults` and from Windows'
      registry, once, at start.
    - The accent is moved toward the ink until links reach WCAG AA, and the text on it is white or black, whichever
      reads better. `AccentSchemesTest` checks macOS's eight accents and Windows' blue in the 11 themes.
    - Linux has no common accent and keeps the theme's color.

Each step renders its screens for review in the chat, runs the whole test suite, and is committed on its own.
