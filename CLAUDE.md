# Notekea — Claude Code

**Read `AGENTS.md` and follow it.** It is the whole instruction set for every agent working on Notekea, Claude Code
included; this file only adds what is particular to Claude Code. Where the project stands: `docs/HANDOFF.md`.

- **Skills:** Claude Code loads `.claude/skills` on its own when it works from this folder. From another working
  directory they are not loaded: read the relevant `SKILL.md` files and references from disk before the work they
  cover (the table in `AGENTS.md`), including `compose-ui` before any UI.
- **Memory:** anything the next agent needs belongs in the repository (`AGENTS.md`, `docs/HANDOFF.md`, the project
  documents), not only in Claude Code's own memory, which other agents cannot read.
