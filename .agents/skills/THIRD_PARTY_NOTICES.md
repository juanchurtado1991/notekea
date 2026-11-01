# Third-party agent skills

These skills are vendored copies used by AI coding agents working on this repository. Each skill folder keeps
its original `LICENSE`. They were reviewed before being added (no network access or destructive commands).

| Skills | Source | Commit | License |
|---|---|---|---|
| `compose`, `compose-architecture`, `compose-data`, `compose-feature`, `compose-platform`, `compose-project`, `compose-ui` | [Meet-Miyani/compose-skill](https://github.com/Meet-Miyani/compose-skill) | `dbaa268a8131fd5ddf5041ceb0d1011e7ac2f12c` | MIT |
| `macos-design-guidelines` | [ehmo/platform-design-skills](https://github.com/ehmo/platform-design-skills) (`skills/macos`) | `dc2be825d8b439caea78e9eaa8fb3ac23b0ff3e9` | MIT |

Precedence: when a skill disagrees with `GUIDELINES.md`, `PLATFORM_GUIDELINES.md` or `LIBRARIES.md`,
the project documents win. Known differences:

- `compose-*` assume Navigation 3, Room and Ktor-based repositories. Notekea has no navigation library
  (3–4 screens) and uses SQLDelight; follow `LIBRARIES.md`.
- `macos-design-guidelines` shows SwiftUI/AppKit examples. Apply its conventions (menus, shortcuts, windows),
  not its code, and only on macOS; the visual design is Notekea's own on every platform.

To update a skill, copy the folder again from the source repository and update the commit in this table.
