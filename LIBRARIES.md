# Notekea — MVP Libraries

Open source note-taking app with local AI: it opens a Markdown folder (compatible with an Obsidian vault),
shows related notes and answers questions citing the exact paragraph. Everything runs on the user's computer.

- **MVP platform:** desktop (Windows, macOS, Linux) with Kotlin Multiplatform + Compose Desktop.
- **Mobile:** phase 2. The architecture is prepared from day 1 (see "Source set split" and "Mobile risks").
- **Platform requirement:** JDK 21 (required by Lucene 10). Compose Desktop bundles it in the installer; users do not install Java.
- **Versions:** verified on Maven Central on 2026-10-23.

## 1. Core and serialization

| Library | Coordinates | Version | Purpose |
|---|---|---|---|
| Ghost (plugin) | `com.ghostserializer.ghost` | 1.3.5 | Wires KSP and Ghost automatically |
| Ghost | `com.ghostserializer:ghost-serialization` | 1.3.5 | YAML: note frontmatter (tags, aliases, dates). JSON: settings, chat history, model catalog, index manifest |
| Ghost Ktor | `com.ghostserializer:ghost-ktor` | 1.3.5 | Reads the JSON of the downloadable model catalog |
| Kotlin | `org.jetbrains.kotlin.multiplatform` | 2.4.21 | Ghost is tested up to 2.4.0; with 2.4.21 its KSP codegen works (checked with the first DTOs, the frontmatter reader of 1.4) |
| KSP | `com.google.devtools.ksp` | 2.3.12 | Required by Ghost |
| Coroutines | `org.jetbrains.kotlinx:kotlinx-coroutines-core` / `-swing` | 1.11.0 | Background indexing and streaming chat |

## 2. Desktop UI

| Library | Coordinates | Version | Purpose |
|---|---|---|---|
| Compose Multiplatform | `org.jetbrains.compose` (plugin) + `org.jetbrains.compose.{runtime,foundation,ui}` | 1.12.2 | UI and packaging (.dmg / .msi / .deb) |
| Material 3 | `org.jetbrains.compose.material3:material3` | 1.9.0 | Components |
| ViewModel | `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose` | 2.11.0 | Host of the MVI state (`StateFlow<State>`) per screen |
| Compose resources | `org.jetbrains.compose.components:components-resources` | 1.12.2 | UI strings, icons and bundled fonts (`Res.*`, `ARCHITECTURE.md` §7); generated accessors in `app.notekea.resources`, excluded from coverage |
| Android Gradle Plugin | `com.android.kotlin.multiplatform.library` | 9.4.1 | Android target in `composeApp` (compileSdk 36, minSdk 26) so Android Studio renders the common `@Preview`s; there is no Android app module yet |
| Compose UI tooling | `org.jetbrains.compose.ui:ui-tooling` (on `androidRuntimeClasspath`) | 1.12.2 | Renders previews in Android Studio |
| Compose preview | `org.jetbrains.compose.ui:ui-tooling-preview` | 1.12.2 | `@Preview` annotation (`androidx.compose.ui.tooling.preview`) for IDE previews of every UI element (`GUIDELINES.md` §6) |
| FileKit | `io.github.vinceglb:filekit-dialogs-compose` | 0.16.0 | Native folder picker for the vault |
| Markdown renderer | `com.mikepenz:multiplatform-markdown-renderer-m3` | 0.46.0 | Renders notes and chat answers |

Decisions:
- The MVP editor is a `BasicTextField` with custom highlighting (no library).
- No Navigation library: with 3–4 screens it is not worth it.
- Compose dependencies use direct coordinates; the `compose.*` accessors of the Gradle plugin are deprecated.
- **MVI** (see `GUIDELINES.md`) is implemented with ViewModel + `StateFlow` + `sealed interface` intents/effects.
  No MVI library: the pattern is small enough that a framework would only add indirection.

## 3. Notes and files

| Library | Coordinates | Version | Purpose |
|---|---|---|---|
| Okio | `com.squareup.okio:okio` | 3.18.2 | Read, write and walk the folder (KMP) |
| JetBrains Markdown | `org.jetbrains:markdown` | 0.7.18 | Parse Markdown into an AST: chunk by heading and detect `[[links]]` (KMP) |
| Directory Watcher | `io.methvin:directory-watcher` | 0.19.1 | Detect folder changes. The JDK `WatchService` falls back to polling on macOS |

## 4. Local AI

| Library | Coordinates | Version | Purpose |
|---|---|---|---|
| java-llama.cpp | `de.kherud:llama` | 4.2.0 | A single engine for embeddings (GGUF such as nomic-embed / bge), chat (~2B, ~4B or ~8B model) and reranking (`rerank()`). Parallel slots (`setParallel` + continuous batching) share one loaded model; grammars (`jsonSchemaToGrammar`) constrain structured output (`ARCHITECTURE.md` §12). Avoids a separate ONNX Runtime and tokenizer stack |
| Lucene | `org.apache.lucene:lucene-core` | 10.5.2 | Hybrid search: vectors (HNSW) + keywords (BM25). Requires Java 21 |
| Ktor Client | `io.ktor:ktor-client-core` + `io.ktor:ktor-client-okhttp` | 3.6.0 | Downloads the model (~2 GB) with progress and resume |

### Agents

| Library | Coordinates | Version | Purpose |
|---|---|---|---|
| Koog agents core | `ai.koog:agents-core` | 1.3.0 | JetBrains agent framework (KMP): agent strategies and tool calling, groundwork for extensions |
| Koog agents tools | `ai.koog:agents-tools` | 1.3.0 | Tool definitions the agent can call |

Decisions:
- Only the core modules are used, not the `ai.koog:koog-agents` umbrella: it bundles cloud clients (OpenAI, Anthropic, Bedrock…) that Notekea does not need.
- Koog has no in-process llama.cpp client; a custom client wrapping `LlmEngine` is required.
- Extensions are planned as MCP servers via `ai.koog:agents-mcp`, added once it ships a stable release (only `1.3.0-beta` as of 2026-10-23).
- Small local models (2B–8B) are unreliable at tool calling; agent features stay out of the MVP UI until tested.
  The MVP answer pipeline uses plain coroutines, not Koog (`ARCHITECTURE.md` §12).

Known limitation (spike of 2026-10-23, `docs/spikes/2026-10-23-ghost-llama.md`): java-llama.cpp bundles natives
for Linux x64/arm64, macOS x64/arm64 and Windows x64/x86, but **not Windows ARM64**; those users would need a
separate build.

Alternative to evaluate for the mobile phase: **Llamatik** (`com.llamatik:library` 1.10.1), on-device KMP LLM (Android, iOS, Desktop). Young (~200 ⭐).

## 5. Git sync

| Library | Coordinates | Version | Purpose |
|---|---|---|---|
| JGit | `org.eclipse.jgit:org.eclipse.jgit` | 7.8.0.202609011348-r | Pure-Java Git: clone, commit, pull, push (no `git` install needed) |
| JGit SSH | `org.eclipse.jgit:org.eclipse.jgit.ssh.apache` | 7.8.0.202609011348-r | SSH key authentication |
| Java Keyring | `com.github.javakeyring:java-keyring` | 1.0.4 | Stores the token in the OS keychain (never in plain text) |

MVP behavior:
1. Connect the vault to a repository (clone an existing one, or `init` + remote).
2. Auto-commit a few seconds after the user stops typing.
3. `pull --rebase` when the app opens and every X minutes, then `push`.
4. Conflicts: no merging; both versions are kept (`note.md` and `note (conflict).md`) and the user is notified.
5. The AI index is in `.gitignore`; each device rebuilds it.

## 6. Infrastructure

| Library | Coordinates | Version | Purpose |
|---|---|---|---|
| SQLDelight | `app.cash.sqldelight` (+ `sqlite-driver` on JVM) | 2.4.1 | Index state (file, hash, date) so only changed files are re-indexed (KMP) |
| Koin | `io.insert-koin:koin-core` / `koin-compose` / `koin-compose-viewmodel` | 4.2.2 | **Mandatory** dependency injection (see `GUIDELINES.md`): one module per feature, platform modules bind the platform implementations |
| Multiplatform Settings | `com.russhwolf:multiplatform-settings` | 1.3.0 | Simple preferences (last vault, theme, selected model) |
| Kermit | `co.touchlab:kermit` | 2.2.0 | Logging |

## 7. Tests

| Library | Coordinates | Version | Purpose |
|---|---|---|---|
| Kotlin Test | `org.jetbrains.kotlin:kotlin-test` | (from Kotlin) | Unit tests |
| Coroutines Test | `org.jetbrains.kotlinx:kotlinx-coroutines-test` | 1.11.0 | Testing asynchronous code |
| Turbine | `app.cash.turbine:turbine` | 1.2.1 | Testing `Flow` (indexing progress, chat streaming) |
| Okio FakeFileSystem | `com.squareup.okio:okio-fakefilesystem` | 3.18.2 | In-memory file system for `data` tests |
| Multiplatform Settings Test | `com.russhwolf:multiplatform-settings-test` | 1.3.0 | In-memory `MapSettings` for settings adapters |
| Konsist | `com.lemonappdev:konsist` | 0.18.1 | Architecture tests (`jvmTest`): layer boundaries and the conventions of `ARCHITECTURE.md` |
| JUnit Jupiter | `org.junit.jupiter:junit-jupiter` (+ `junit-platform-launcher` 6.1.3) | 6.1.3 | Test runner of `:ktlint-rules` |
| ktlint test | `com.pinterest.ktlint:ktlint-test` | 1.8.0 | Assertions for the custom ktlint rules |

## 8. Bundled assets

| Asset | Source | License | Purpose |
|---|---|---|---|
| Inter | [rsms/inter](https://github.com/rsms/inter) | SIL OFL 1.1 | UI and editor body typeface, identical on every platform |
| JetBrains Mono | [JetBrains/JetBrainsMono](https://github.com/JetBrains/JetBrainsMono) | SIL OFL 1.1 | Code blocks in notes and chat |

Font files are added with feature 1.2 (`feature/design-system`, see `ROADMAP.md`), together with their license texts.

## 9. Tooling

| Tool | Coordinates | Version | Purpose |
|---|---|---|---|
| ktlint Gradle plugin | `org.jlleitschuh.gradle.ktlint` | 14.2.0 | `ktlintCheck` / `ktlintFormat` tasks |
| ktlint | `com.pinterest.ktlint` | 1.8.0 | Lint engine; rules configured in `.editorconfig` |
| Custom rules | `:ktlint-rules` module | — | `notekea:expression-on-assignment-line`, `notekea:max-function-length` (40 lines), `notekea:parameter-order` |
| Kover | `org.jetbrains.kotlinx.kover` | 0.9.11 | `koverVerify` requires 80% line coverage in every module on its own (Composable UI excluded); the root also publishes a merged report. Rules live once in the root build script |
| GitHub Actions | `.github/workflows/ci.yml` | — | `./gradlew check koverVerify` on Windows, macOS and Linux on every pull request and merge to `main` |
| Dependabot | `.github/dependabot.yml` | — | Monthly grouped minor/patch PRs for the version catalog and GitHub Actions; majors and Kotlin/KSP/Compose/Ghost upgraded by hand (`GUIDELINES.md` §14) |
| Git hooks | `.githooks/pre-commit` | — | `ktlintFormat` + `check` + `koverVerify` before each commit; installed by the first build (`installGitHooks`) |

## Source set split

Packages follow Clean Architecture by feature (`app.notekea.<feature>.{domain,data,presentation}`, see `GUIDELINES.md`).

```
commonMain → Ghost, Okio, JetBrains Markdown, SQLDelight, Koin, Settings, Kermit,
             Koog (agents-core, agents-tools), Compose UI, ViewModel
             + domain interfaces: llm.domain.LlmEngine, index.domain.VectorIndex,
               sync.domain.GitSync, notes.domain.FileWatcher

jvmMain    → java-llama.cpp, Lucene, JGit (+SSH), Java Keyring, directory-watcher,
             Ktor OkHttp, SQLDelight SQLite driver
```

Rule: no type from a `jvmMain` library (JGit, Lucene, llama.cpp) appears in `commonMain`.
Everything goes through the interfaces, so the mobile phase means adding implementations, not rewriting.

## Mobile risks

| Piece | Android | iOS | Plan |
|---|---|---|---|
| JGit | 🟡 Works with care: JGit 6.x/7.x uses JDK APIs Android does not always provide; may require pinning an older version. Must be tested | ❌ Not available (iOS does not run Java) | `libgit2` via cinterop behind `GitSync`. Alternative: "no Git" on mobile (iCloud / Drive / Syncthing on the folder) |
| Lucene | 🟡 Heavy and requires Java 21 | ❌ Not available | Alternative `VectorIndex` implementation (e.g. SQLite + sqlite-vec) |
| java-llama.cpp | ❌ Desktop JVM only | ❌ | Llamatik or another llama.cpp integration behind `LlmEngine` |
| directory-watcher | 🟡 | ❌ | Re-index on app open / on save from the editor |
| Java Keyring | ❌ | ❌ | Android Keystore / iOS Keychain behind a credentials interface |

Other mobile constraints: iOS limits background sync (sync on app open/close),
SSH keys are awkward on mobile (prefer an HTTPS token), and large vaults (use a shallow clone, `depth = 1`).
