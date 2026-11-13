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
| Android Gradle Plugin | `com.android.kotlin.multiplatform.library` | 9.4.1 | Android target in `app` (compileSdk 36, minSdk 26) so Android Studio renders the common `@Preview`s; there is no Android app module yet |
| Compose UI tooling | `org.jetbrains.compose.ui:ui-tooling` (on `androidRuntimeClasspath`) | 1.12.2 | Renders previews in Android Studio |
| Compose preview | `org.jetbrains.compose.ui:ui-tooling-preview` | 1.12.2 | `@Preview` annotation (`androidx.compose.ui.tooling.preview`) for IDE previews of every UI element (`GUIDELINES.md` §6) |
| FileKit | `io.github.vinceglb:filekit-dialogs-compose` | 0.16.0 | Native folder picker for the vault |
| Lucide icons | `com.composables:icons-lucide-cmp` | 2.2.1 | UI icons as `ImageVector`s (MIT wrapper, ISC icons). Lucide is the icon set of Obsidian, so its users recognize folders, notes and actions at a glance; ~5.7 MB on the JVM against ~38 MB for the frozen `material-icons-extended`. Only `NotekeaIcons` imports it (Konsist) |
| Markdown renderer | `com.mikepenz:multiplatform-markdown-renderer-m3` | 0.46.0 | Renders notes and chat answers |
| Markdown renderer code | `com.mikepenz:multiplatform-markdown-renderer-code` | 0.46.0 | Colors code blocks by language in Read mode, like GitHub's preview; brings `dev.snipme:highlights` 1.1.0 (Apache-2.0, ~15 languages). Edit mode keeps code plain on purpose |

Decisions:
- The MVP editor is a `BasicTextField` with custom highlighting (no library); Read mode renders with the Markdown
  renderer through the design system's `NotekeaMarkdown`.
- No Navigation library: with 3–4 screens it is not worth it.
- Icons come from Lucide through `core.presentation.designsystem.theme.NotekeaIcons`, never from the library directly, so the set
  can change in one file. `material-icons-extended` is not used: JetBrains stopped publishing it after 1.7.3.
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
| java-llama.cpp | `net.ladenthin:llama` (a maintained fork of `de.kherud:llama`, whose 4.2.0 never frees a model; `ROADMAP.md` 2.9), with one natives jar per system (`LlamaNatives`) | 6.0.0 | A single engine for embeddings (GGUF such as nomic-embed / bge), chat (~2B, ~4B or ~8B model) and reranking (`rerank()`). Parallel slots (`setParallel` + continuous batching) share one loaded model; grammars (`jsonSchemaToGrammar`) constrain structured output (`ARCHITECTURE.md` §12). Avoids a separate ONNX Runtime and tokenizer stack |
| Embedding model (bundled) | `granite-embedding-107m-multilingual`, Q8_0 GGUF (bartowski) | Hugging Face commit `52fed1c8` | Multilingual embeddings for the index (`ROADMAP.md` 2.2): 384 dimensions, 512 tokens, 121 MB, Apache 2.0. Chosen over multilingual-e5-small and bge-m3 (`docs/spikes/2026-11-01-embedding-models.md`); downloaded by the build with its SHA-256 checked (`downloadEmbeddingModel`) and packaged in the app's resources |
| Lucene | `org.apache.lucene:lucene-core` | 10.5.2 | Hybrid search: vectors (HNSW) + keywords (BM25). Requires Java 21 |
| Lucene analysis | `org.apache.lucene:lucene-analysis-common` | 10.5.2 | The keywords of the index (`ROADMAP.md` 2.3) without accents and with Spanish words reduced to their stem, so "cancion" finds "canción" and "receta" finds "recetas" (approved by the developer on 2026-11-03) |
| Ktor Client | `io.ktor:ktor-client-core` + `io.ktor:ktor-client-okhttp` | 3.6.0 | Downloads the chat model and the reranker (`ROADMAP.md` 3.1) with progress, pause and resume (`Range`), on the desktop side only |

### Agents

**Not in the build yet:** approved, and added back with the code that uses them (M3–M4), so the app does not ship them unused.

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

Known limitation: the fork has natives for Linux x64/arm64, Windows x64/arm64 and macOS on Apple silicon, but **not Intel
macOS**, and the macOS installer is built for Apple silicon. It logs every embedded text at its default level:
`LlamaTextEmbedder` sets errors only.

Alternative to evaluate for the mobile phase: **Llamatik** (`com.llamatik:library` 1.10.1), on-device KMP LLM (Android, iOS, Desktop). Young (~200 ⭐).

## 5. Git sync

In the build since 4.1 (2026-11-11). About 7 MB on the JVM: JGit 3.3 MB, JNA and its platform classes 3.3 MB (shared
with nothing else), Commons Codec 0.4 MB, JavaEWAH 0.2 MB, java-keyring 32 KB.

| Library | Coordinates | Version | Purpose |
|---|---|---|---|
| JGit | `org.eclipse.jgit:org.eclipse.jgit` | 7.8.0.202609011348-r | Pure-Java Git: init, commit, fetch, rebase, merge, push over HTTPS (no `git` install needed) |
| Java Keyring | `com.github.javakeyring:java-keyring` | 1.0.4 | Keeps the HTTPS token in the system's keychain (Keychain, Credential Manager, Secret Service), never in a file |

HTTPS tokens only, decided with the developer on 2026-11-11: no SSH keys, so `org.eclipse.jgit.ssh.apache` is not
added. How it behaves (`JGitRepository`, `SyncEngine`):
1. Connect the vault to a repository: an existing one, or `init` + remote; a vault made in Notekea follows the remote's
   branch (`main`, `master`).
2. Commit 10 s after the last change, then pull and push.
3. `pull --rebase` when the vault opens and every 1 to 30 minutes, then `push`; a rebase that clashes becomes a merge.
4. Conflicts are never merged by hand: the note keeps the remote's version and ours is written beside it as
   `Note (conflict).md`; a banner offers Keep mine, Keep theirs or Keep both.
5. The AI index lives in the app's data folder, outside the vault; Notekea's own files (`*.notekea-tmp`, `.trash/`,
   Obsidian's window layout) are excluded through `.git/info/exclude`, which is never committed.

## 5b. Documents and web clips

In the build since 4.5 and 4.4 (2026-11-11). Documents add about 38 MB of jars on the JVM (51 jars, before the
installer compresses them): the largest are BouncyCastle 7.2 MB (PDFBox opens encrypted PDFs with it), POI 7 MB in
three jars (Word, Excel, PowerPoint), Commons Math 2.2 MB, XMLBeans 2.2 MB and PDFBox 2.1 MB. Only the parser modules
of the formats Notekea reads are added, not Tika's standard package (OCR, audio, images, mail, archives). Web clips
add 2.5 MB (Readability4J and jsoup).

| Library | Coordinates | Version | Purpose |
|---|---|---|---|
| Apache Tika | `org.apache.tika:tika-core` and the `tika-parser-{pdf,microsoft,miscoffice,html}-module`s | 4.1.0 | A document's text as XHTML, turned into Markdown with a heading per page, slide or sheet (`TikaDocumentReader`): PDF, Word, OpenDocument, EPUB, PowerPoint, Excel, HTML |
| PDFBox | `org.apache.pdfbox:pdfbox` | 3.0.8 | A PDF's real pages drawn in the document's tab, and the cited passage found on its page by its letters' positions (`PdfBoxPages`) |
| Readability4J | `net.dankito.readability4j:readability4j` | 1.0.8 | Mozilla's Readability in Kotlin: the article of a web page without its menus, ads and comments (`ReadabilityArticleExtractor`) |
| POI OOXML full | `org.apache.poi:poi-ooxml-full` (tests only) | 5.5.1 | Writes the Word, Excel and PowerPoint files the tests read; Tika reads them without it |

The roadmap asked to weigh Tika against one small library per format: the developer chose Tika for every format on
2026-11-11, before measuring, and these sizes are what that choice costs.

## 5c. Videos

Videos in notes (the developer's request of 2026-11-11). Decided after a spike:

- **Rejected:**
  - vlcj is GPL v3, which Notekea's Apache 2.0 cannot ship without Caprica's commercial license.
  - KCEF (Chromium) was archived in October 2025, and its Chromium has no H.264.
  - ComposeNativeWebView needs the whole window on Nucleus Tao.
- **The player is the system's:** Media Foundation on Windows, AVFoundation on macOS, GStreamer on Linux. It adds
  0.8 MB with its natives, and 170–290 MB of memory only while a 1080p video plays (measured on Linux).

| Library | Coordinates | Version | Purpose |
|---|---|---|---|
| ComposeMediaPlayer | `io.github.kdroidfilter:composemediaplayer` (MIT) | 0.11.4 | Plays a video file in its note with the system's player (`DesktopVideoPlayer`); a format the system cannot play opens with its app |
| FFmpeg (downloaded, not built in) | `org.bytedeco:ffmpeg` (LGPL v3: `--enable-version3`, no `--enable-gpl`) | 8.1.2-1.5.14 | Makes a video over GitHub's 100 MB smaller before it is attached: H.264 with OpenH264 and AAC (`FfmpegVideoCompressor`). Its jar for the system (20–30 MB) is downloaded from Maven Central the first time and checked against the SHA-256 pinned in `FfmpegTool` (#141). A 2-minute 1080p video, 153 MB to 93.6 MB, took 47 s with the download, 4 threads and 0.12 GB of memory |
| webview-kmp | `cn.enaium.webview:webview-kmp` (MIT) | 1.0.1 | The YouTube player window: the system's web view in Notekea started again as a process of its own (`YouTubePlayerWindow`), about 290 MB while it is open, all given back when it closes |
| JNA | `net.java.dev.jna:jna` (Apache 2.0) | 5.19.1 | Already in the build through Java Keyring; used directly to make the YouTube window on macOS's main thread, the one AppKit wants (`CocoaMainThread`) |

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
| Compose UI Test | `org.jetbrains.compose.ui:ui-test` | 1.12.2 (with Compose Multiplatform) | The automated smoke runs (`ROADMAP.md` 1.16): the real app driven through its UI (find by text, click, type, wait), headless on the three systems; the automated cases of the Notekea Auto-Test, in CI and on the testers' machines (`qa/AUTO_TEST.md`) |
| JUnit Jupiter | `org.junit.jupiter:junit-jupiter` (+ `junit-platform-launcher` 6.1.3) | 6.1.3 | Test runner of `:ktlint-rules` and `:test-buddy`; Test Buddy's JUnit bridge (`TestBuddyJUnit`, compile-only: the host brings JUnit) |
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
| Custom rules | `:ktlint-rules` module | — | the layout and size rules of `GUIDELINES.md` §6 (`notekea:code-line-length`, `notekea:max-function-length`, `notekea:chain-per-line`…) |
| Kover | `org.jetbrains.kotlinx.kover` | 0.9.11 | `koverVerify` requires 80% line coverage in every module on its own (Composable UI excluded); the root also publishes a merged report. Rules live once in the root build script |
| GitHub Actions | `.github/workflows/ci.yml` | — | `./gradlew check koverVerify` on Windows, macOS and Linux on every pull request and merge to `main` |
| Dependabot | `.github/dependabot.yml` | — | Monthly grouped minor/patch PRs for the version catalog and GitHub Actions; majors and Kotlin/KSP/Compose/Ghost upgraded by hand (`GUIDELINES.md` §14) |
| Git hooks | `.githooks/pre-commit` | — | `ktlintFormat` + `check` + `koverVerify` before each commit; installed by the first build (`installGitHooks`) |

## Source set split

Packages follow Clean Architecture by feature (`app.notekea.<feature>.{domain,data,presentation}`, see `GUIDELINES.md`).

```
commonMain → Ghost, Okio, JetBrains Markdown, SQLDelight, Koin, Settings, Kermit,
             Koog (agents-core, agents-tools; from M3–M4), Compose UI, ViewModel
             + domain interfaces: ai.domain.LlmEngine, index.domain.VectorIndex,
               sync.domain.GitSync, tree.domain.FileWatcher

jvmMain    → java-llama.cpp, Lucene, directory-watcher, SQLDelight SQLite driver,
             JGit (+SSH), Java Keyring, Ktor OkHttp (from M3–M4)
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
