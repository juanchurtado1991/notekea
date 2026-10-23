# Security Policy

Notekea's promise is that your notes never leave your computer. We treat any way to break that promise as a
security issue.

## Reporting a vulnerability

**Do not open a public issue.** Report it privately through GitHub:
**Security** tab → **Report a vulnerability** (GitHub private vulnerability reporting).

Please include:

- What an attacker can do and under which conditions.
- Steps to reproduce, the affected version and your operating system.
- Any proof of concept, if you have one.

We aim to acknowledge reports within 7 days and to agree on a disclosure date once a fix is available.
Reporters are credited in the release notes unless they prefer otherwise.

## In scope

- Note content, embeddings, chat history or file paths leaving the device without the user's action.
- Credentials (Git tokens, SSH keys) stored or logged in plain text.
- Code execution through crafted notes, Markdown, frontmatter or downloaded models.
- Path traversal outside the vault folder.
- Tampering with model downloads (integrity checks).

## Supported versions

Until 1.0, only the latest release receives security fixes.
