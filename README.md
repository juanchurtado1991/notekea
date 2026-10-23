# Notekea

**Your notes, connected by local AI.** Open your Markdown folder and ask it anything: Notekea links related notes
and answers with exact citations — all on your machine.

> **Status:** early development. Nothing is released yet; follow the [changelog](CHANGELOG.md).

## Why Notekea

- **Private by design.** The AI runs on your computer. No account, no cloud, no telemetry.
- **Zero setup.** No Ollama, Docker or API keys: download the app, pick a model with one click, done.
- **Works with the notes you already have.** Notekea opens a plain folder of Markdown files, including an
  Obsidian vault, without importing or changing anything.
- **Answers you can verify.** Every answer cites the note and paragraph it came from.
- **Related notes, automatically.** While you read or write, Notekea surfaces notes about the same idea, even if
  you never linked them.
- **Sync with Git.** Optional automatic commit, pull and push of your vault.

Planned for the MVP: Windows, macOS and Linux desktop. Mobile comes later. See the [roadmap](ROADMAP.md).

## Build from source

Requirements: JDK 21 and Git.

```bash
git clone https://github.com/juanchurtado1991/notekea.git
cd notekea
./gradlew :composeApp:run
```

Run lint, tests and coverage:

```bash
./gradlew check koverVerify
```

## Tech stack

Kotlin Multiplatform, Compose Multiplatform (desktop), llama.cpp for local inference, Lucene for hybrid search,
JGit for sync, Koin and MVI. The full list, with the reason for each library, is in [`LIBRARIES.md`](LIBRARIES.md).

## Contributing

Contributions are welcome. Start with [`CONTRIBUTING.md`](CONTRIBUTING.md), [`GUIDELINES.md`](GUIDELINES.md) and
[`PLATFORM_GUIDELINES.md`](PLATFORM_GUIDELINES.md). Security issues: see [`SECURITY.md`](SECURITY.md).

## License

[Apache License 2.0](LICENSE)
