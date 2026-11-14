# History — how Notekea got here

What `docs/HANDOFF.md` said about each milestone while it was the current work, kept here when it stopped being so.
For where the work stands now, read the handoff.

## Milestones, M0 to M4

- **M0 — Foundation:** ✅
- **M1 — Vault and editor:** ✅ 18 of 18 items. Its QA run (`qa/runs/M1.md`) closed on 2026-11-01 with the macOS run
  of `m1-rc1`, as the developer decided: the Windows tester is not available until M2, and Linux was not run.
- **M2 — Indexing, links and related notes:** ✅ 13 of 13 items. 2.12 Color themes is done (#107), 2.1 Markdown
  chunking (#108), 2.2 Embedding engine (#109), 2.3 Hybrid index (#110), 2.4 Related notes panel (#111) and **2.5
  Backlinks** (#112) are done; Workspace Blue is the default theme (#113). `notes` was split into features (#118), and
  **2.6 Hybrid search** (#120), the eight hand-tuned themes (#123) and **2.7 Graph view** (#122) are done. **2.10
  Vault queries** (#124) are done. **2.11 Vault tasks** (#125, a grouped list, as Reminders) is done; on
  branch `feature/tasks-view-cards` (from `feature/one-editing-mode`, to open as its own pull request after that
  one) its groups are cards, the design the developer chose on 2026-11-10 (`TaskCards.kt`: each piece of a card is
  one item of the lazy list). **2.8 Retrieval
  evaluation set** (#126) is done: recall@5 measures 0.94 and 0.90 against baselines of 0.92 and 0.86. **2.13 Packaged
  check** (#127) is done: the nightly installs and starts the app on the three systems. **2.9 M2 performance pass**
  (#128) is done: the app moved to `net.ladenthin:llama`, because java-llama.cpp 4.2.0 leaked each model it loaded;
  measures in `docs/performance.md`, Linux only.
- **M3 — Chat with citations:** started. **3.0 Parallel inference spike** (#129) is done
  (`docs/spikes/2026-11-07-llm-parallel.md`, Linux only): Qwen3.5 2B, 4B and 9B as Small, Medium and Large,
  bge-reranker-v2-m3, Vulkan or Metal for the GPU, no mmap on the CPU, slots 1 / 2 / 4 by memory. Preliminary until
  3.4. The **answer quality spike** (`docs/spikes/2026-11-08-answer-quality.md`) (#130) is done: answering in one call
  beats the extraction pipeline on every model; Small is right 53 % and wrong 13 %, Medium right 71 % and wrong 3 %;
  the reranker gate moves from −5 to −8; parallel calls and judges did not improve Small, and voting over 3 drafts
  helped only Medium (3.6's starting point). A **model tournament** on 504 questions
  (`docs/spikes/2026-11-08-model-tournament.md`) kept Qwen3.5 2B, 4B and 9B against five other open models. On 8 GB,
  Small answers with its sources first and a line asking to check them (decided 2026-11-08, in 3.3). **3.1 Model
  manager** (#132) is done: Settings → AI model downloads the recommended size and the reranker from pinned Hugging
  Face commits, checked by SHA-256, into the app data's `models` folder (`app.notekea.ai`).
  **3.2 to 3.9 in one pull request** (decided with the developer on 2026-11-10), on `feature/m3-chat` from `main`,
  one commit or more per item, in this order: 3.2 Chat engine, 3.4 Answer evaluation, 3.3 Chat panel, 3.5 Answer
  pipeline, 3.6 Deep answer, 3.8 Questions in plain words, 3.9 Actions from chat, 3.7 M3 performance pass. The
  developer took every decision up front so the work runs without stopping; a smoke list and the pull request come at
  the end, after `./gradlew check koverVerify` and a code review. Decisions:
  - **Chat panel: design A, conversation:** the question on the right, the answer on the left with citation chips
    `[1]`, its sources listed under it; the scope above the messages, Deep answer beside the send button. With the
    Small model the sources come first, as decided on 2026-11-08.
  - **3.9: every action of the roadmap but clipping a web page**, which waits for 4.4: read and find, the app's
    actions (Edit and Preview are gone: the chat turns Source on and off), writing, organizing (rename, move, trash,
    up to 50 notes), one confirmation card for the plan and one undo.
  - **A step that does not beat the baseline is dropped:** each step of 3.5 and Deep answer (3.6) is measured with
    the 3.4 set against the single call of 3.2; what does not help is not shipped (no dead code), the numbers go
    into the spike's document and the roadmap item says why. Deep answer is offered only for the sizes it helps, and
    not at all if it helps none.
  - **Memory:** if chatting with the Small model cannot get under 2.5 GB, 3.7 records how close it got in
    `docs/performance.md` and the gap goes to 5.4; the pull request is not held for it.
  - **Models and measures:** the files already downloaded for the tournament (`~/.cache/notekea-spike/models`:
    Qwen3.5 2B, 4B and 9B Q4_K_M and bge-reranker-v2-m3), on this Linux machine (RTX 5060 Ti 16 GB, and CPU only);
    no new downloads. Windows and macOS are measured in the release run. Evaluation runs by hand, never in CI.
  - **Asked by the developer the same day:** the actions must be useful, not only correct: the chat can make a note
    from other notes (a summary of a folder, of a tag, of the notes it found), and what it writes follows what was
    asked for: summing up 30 notes gives a longer summary than summing up 3, but never an enormous one (a length
    that grows with the sources, with a ceiling). The chat panel must feel alive, with motion and colour (the answer
    streaming in, the step line, the citation chips, the action card), within the design system and reduced motion.
    The actions must work perfectly: they are exercised for hours if needed, on the vaults of `smoke-data`, before
    the pull request, and every bug found gets its test. A code review closes the work.
    Added by the developer on 2026-11-10: a long pasted text can become a structure of notes (`notes_from_text`): a
    folder, up to about 15 notes that rewrite its parts (never pasted, nothing invented), an index note linking them,
    tags or properties where the text suggests them, all on the confirm card and undone at once; a text longer than
    the context is read in sections, and one too long says so instead of being cut.
    Also from the developer (2026-11-10): notes the chat writes use everything the formatting toolbar offers
    (headings, bold, italic, lists, numbered lists, tasks, quotes, callouts, tables, links, tags) where the content
    calls for it (`WritePrompt.FORMATTING`). The WhatsApp-like chat (question bubble right in the theme's color,
    answer bubble left in its accent, numbers centred in the chips) was approved.
  - **Progress:** 3.2 is done: `LlmEngine` (`core.llm`, so the chat, the queries and the actions share it) on
    java-llama.cpp (`LlamaLlmEngine`, `ai.data`), the Vulkan natives next to the CPU ones on Windows and Linux (the
    embedding model stays on the CPU, so the index keeps its vectors), and `AnswerFromNotesUseCase` (`chat.domain`):
    20 passages from the index, reranked, the gate at −8, one call over the best 5, citations checked and numbered
    again. Tests on the real model run by hand: `./gradlew :app:chatModelTest -PchatModels=<folder>` (5 tests, about
    10 s on the GPU). Not yet: the history of a conversation (each question is answered alone).
    3.4 is done: `smoke-data/eval/answers.json` (the 504 questions of the tournament) and
    `AnswerEvalChatModelTest`, run by hand per size, which grades the app's own answers strictly
    (`docs/answer-quality.md`): Small 80 %, Medium 89 %, Large 85 % right; the sample vaults are read as committed
    (`EvalVault`), so local edits to `smoke-data` change no number.
    3.3 is done: the chat panel (`chat.presentation`, design A · Conversation) in the side panel's Chat mode, one
    `ChatViewModel` per vault: the question on the right, the step line, the answer streamed with a caret and inline
    citation chips (`NotekeaCitationChip`, tooltip with the passage, click reveals it), Sources under it (above it
    for Small, with a line to check them), the scopes (Whole vault, This note, This folder), `Mod+L`, Stop (`Esc`),
    retry, the history per vault (`FileChatHistoryStore`, JSON in the app data's `chat` folder, last 100 turns) with
    Clear history, the notices without a model or memory ("Choose a model" opens Settings at AI model, the new
    `WindowDialog.AI_MODEL`), and the model's download in the status bar (`ModelDownloadStatus`).
    3.5 is measured and nothing is built: none of its steps beats 3.2's single call (`docs/answer-quality.md`, 3.5).
    3.6 Deep answer: three drafts at temperature 0.7 and a vote (`DraftVote`, the draft the others agree with; not
    found when most say so); Medium 91 % against 89 %, traps 94 against 91 %, so it is offered for Medium
    (`LlamaLlmEngine.DEEP_ANSWER_SIZES`); Small and Large are measured next. The panel shows the toggle only then.
    3.8 and 3.9 share one planner (`PlanRequestUseCase`, JSON grammar, `ActionPrompt`): a request becomes up to five
    steps of `ActionKind`; `CheckPlanUseCase` resolves names against the vault, `ComposeNotesUseCase` writes the text
    first (`WritePrompt`, length 120 + 20 words per note, at most 600; `TextNotesPrompt` for a pasted text), the card
    confirms, and the shell's `VaultActionRunner` (`shell.data`, composing the features' use cases) does it with a
    journal for Undo. Notes by their properties go through the query feature's `TranslateQueryUseCase`, also behind
    the Query tab's "Describe what you want". Evaluated by `ActionEvalChatModelTest` over `smoke-data/eval/actions.json`.
    Plans: Small 73 %, Medium 94 %, Large 91 % of 106 requests. `ActionRunChatModelTest` runs 13 requests end to end
    on a copy of the Sample Vault with the real model (files checked, Undo leaves it as it was): all pass on Medium.
    3.7: the CPU setup names no GPU device (it kept Vulkan and 1.9 GB with no layer on it); Small on the CPU peaks at
    3.3 GB, over 5.4's 2.5 GB, recorded in `docs/performance.md` as decided. The code review's nine findings are fixed.
    Not built: rewriting a note as a difference (3.9), web clips (4.4). All of 3.2–3.9 is one pull request.
    After the first smoke run (2026-11-11): design B for the chat (quiet question bubble, the answer beside the
    assistant's mark, sources as pills), replies in the question's language (`ReplyLanguage`, `replyString`, the
    `_es` strings), the chat's last four exchanges given to the planner so follow-ups stand alone, a `chat` step for
    greetings and opinions (`ConverseUseCase`), and a second try when nothing is found (`AgentAnswerUseCase`, the light
    agent; Medium 90 against 89 % right, traps 88 against 91 %). CrashBuddy shows the Swing notice only when the
    Compose one could not show. Running the app from the build folder while it is rebuilt fails with
    NoClassDefFoundError: rebuild only with the app closed.

## M4 and M4b, done on `feature/m4-sync-sources` (one pull request, awaiting review)

Where it stands (2026-11-11): 4.1–4.8 built, with the chat's help guide and the developer's chat smoke fixes.

- **Measured:** plans Medium 95 % / Small 73 % (126 requests); clips 20 of 20 pages, 91 % of sentences backed; agent
  mode Medium 83 %, Large 70 %, Small 10 %; Medium and Large offer it (`docs/answer-quality.md`). Sync under a second,
  large documents ready in 4.6 s (`docs/performance.md`).
- **Auto-Test:** 4.2-01/02, 4.4-01, 4.5-01, 4.6-01, 4.7-01, 4.7-03, 4.7-05 and 4.7-06 automated; 4.2-03, 4.4-02,
  4.5-02, 4.6-02, 4.7-02 and 4.7-04 manual (`MVP_SMOKE_TEST.md`).
- **Decided 2026-11-11:** agent mode on Medium and Large (Large at 70 %), and the chat's modes Auto, Fast, Deep
  thinking and Agent in a menu at the field's bottom right. **For the developer to decide:** Web clip moved from
  the editor's More menu to the start actions (and the chat); Tika's ~38 MB in the installers; HTTPS tokens only.
- **CI (all three systems green on fddd9e98):** it found two Windows/macOS-only bugs, both fixed with tests. Git for
  Windows' core.autocrlf rewrote pulled notes' line ends, and PDFBox scanned every system font on its first
  non-embedded font, slowing the Playground Vault's opening until Auto-Test cases timed out; it now uses its own fonts.
- **Known:** a test JVM that ran two model cases sometimes aborts at exit (native teardown), with every case passed.
- **Docs sections (added 2026-11-11, the developer's request):** clipping a page that links pages under its own path
  asks first, in the dialog and the chat: only it, or it and those (at most 30), into a folder with the page as their
  index. Tried on the Traceloop docs; 4.4-03 is the manual case.
- **Chat probe (2026-11-11, overnight):** six rounds of 62 conversations in English, Spanish and French against
  Medium (`ChatProbeChatModelTest`, `docs/answer-quality.md`): replies in the person's language, follow-ups,
  honest refusals, the memory ring with compaction, answers from properties and tables, and no claimed changes.
  Action eval 97 %. Eight rounds on Medium, two on Large; the chat also compacts itself after half an hour idle.
  4.7-05 and 4.7-06 automated and passing on the real app with Medium. Compared turn by turn with Claude's own
  answers to the same probe: every factual answer matches on both sizes (`docs/answer-quality.md`). Open: the Sample Vault's Reading list and
  `finished` properties disagree.

Decided before the work started:

One pull request for 4.1–4.8, branch `feature/m4-sync-sources`, decided with the developer on 2026-11-11, with
these choices taken before the work started:

- **Sync:** JGit, HTTPS token only (in the OS keychain through java-keyring), no SSH keys. Tests use local bare
  repositories (`file://`) only; the developer tries GitHub in the smoke.
- **Documents:** Apache Tika for every format. A citation, or opening the file, shows the document in a read-only tab
  at the cited passage, highlighted, the whole document readable: PDFs as real pages rendered with PDFBox at the
  cited page; Word, EPUB and text as extracted text with headings; Excel as one table per sheet; PowerPoint as each
  slide's text. Every one has "Open in the system's app".
- **Agent mode:** all of 4.7: at most 30 steps and 10 minutes, the plan on one card at the end, about 30 tasks in the
  evaluation, offered only for the sizes that pass.
- **Libraries approved:** JGit, Apache Tika with PDFBox, Readability4J (web clips) and java-keyring; each goes in
  `LIBRARIES.md` with its size.
- **Chat help (added):** "how do I…" and "what can you do" are answered from a guide built into the app, with the
  system's real shortcuts, never by creating a note; when the chat can do it, the answer has a button that opens the
  usual card. The guide is for the chat only (no Help page).
- **Chat fixes from the developer's smoke (added):** "what is in my notes" answers from an overview of the vault, not
  from a few passages; "in Spanish" says the last answer again in Spanish instead of searching; a question about how
  to do something or a complaint never creates a note; asking the same question again answers it again.
- **Ghost (added):** the readers of the model's JSON catch only Ghost Serializer's parse error, so a programming
  error is not taken for a bad reply.
- **Not in it:** web search (dropped by the developer for now).

## Videos and M5 on `feature/videos` (#142): the rounds that are no longer the current work

Moved from the handoff on 2026-11-14. The round of that day (the chat's work stands on what it read, the tree's drag
and Duplicate, a new note's properties) is the current work and is in `docs/HANDOFF.md`.

### The smoke of 2026-11-13: the chat and the web clips

- **Chat:** answers are drawn as Markdown (`AnswerMarkdown`, `NotekeaMarkdown(isCompact, inlineContent)`; citations
  stay chips, drawn inline through `InlineLinks`), the steps are a list of tasks with checks (`ChatTasks`,
  `ChatEntry.passed`), and the list follows the text by what it measures (`FollowNewest`: Markdown is laid out a
  moment after its text changes). The prompts ask for a shaped reply (`ReplyFormat.MARKDOWN`).
- **Web clips:** a probe of fifteen real sites drove the fixes. The article is Markdown blocks (`HtmlMarkdown`,
  `HtmlInline`, `HtmlTable`) cleaned of the site's chrome (`ArticleBlocks`, the extractor's `CHROME`); the section
  search takes links from `<a>` only, walks `..`, drops a wiki's namespaces, and offers no section for a page with
  hundreds of pages beside it in a site's first folder (`SectionLinks.mostBeside`). Notes clipped before read as
  changed at their next update and are rewritten once in the new shape.
- **Also:** the welcome waits for the vault's first scan (`OnboardingGate`, `WindowLoading`), and the YouTube
  player hides under anything drawn over the window (`WindowCovers`, told by each overlay with `CoversWindow()`).
- **A page's links (chat):** four rows (`ChatSectionQuestion`, `NotekeaOptionRow`), a deep choice counted first
  (`WebPageClipper.counted`, `ClipWords.COUNT_FIRST`, `SectionRequest`), the scan kept for the clip
  (`ClipsForChat.scan`), the whole documentation up to 1,000 pages (`SectionLinks.FULL_DEPTH`), no model summary
  past 40 pages. `SectionLinks.isIndex` suggests going deeper.
- **Agent answers:** written apart from the moves, from what they found only (`AgentFinalAnswer`): asked for
  Markdown inside the move's own summary, the model made up an answer (Auto-Test 3.3-05).
- **Follow-ups:** `FollowUps`, `ChatFollowUps`, under the last answer.
- **Not found, look in the site:** `ChatSiteSearch` (`ChatEntry.sites`, `sitePages`, `pickedPages`), the port
  `SourceSites` with `SourceSitesForChat`, `WebSourceSearch` in clips (pages not clipped yet, ranked with the
  embedding model, clipped into their section by `UpdatedSection.add`), and `AnswerFromPagesUseCase`, which answers
  from the pages just clipped since the index has not read them yet.
- **Study cards of a subject:** `CardsNamed` makes a request for cards a card step for each note it is about (five
  at most); `ActOnRequestUseCase.composed` leaves out a note that backs none. Agent mode makes no cards.
- **A move cut short:** `AgentTaskUseCase.readMove` asks again with `AgentTaskPrompt.CUT_SHORT` and more room, then
  ends on what it found (`unread`).
- **Fixed since from the code review of 2026-11-13** (each with its test):
  - **Chat:** an action never writes or moves outside the vault (`RelativePath.isInside`); a plan that throws is
    taken back (`VaultActionRunner.takenBack`); Undo says when it cannot, and leaves a note the person changed;
    Clear history cancels a compaction; a button of an older turn starts no second turn; `AnswerPrompt.SAYS_NONE`
    keeps "No hay azúcar… [1]." an answer; a question of a page's links is not kept in the history.
  - **Editor:** Locate stays in its own text; one compression at a time; FFmpeg's exit code and install lock.
  - **Settings and design:** Files reads the vault again when shown; date samples with a day over 12; the theme
    sets `surfaceContainer*`, `secondary*` and `scrim`; darker light warning and success; focus rings of the switch
    and the pop-up button.
  - **Tree:** a case-only rename leaves no ghost; an unclosed `---` keeps the note's tags; a line is read up to
    1 MB; the watcher logs when it stops; the welcome ignores clicks while a vault opens.
  - **Sync:** the status never stays at Syncing; a merge that fails goes back clean; an older conflict's copy is
    set aside (`FreeFile`); errors hide a token; fetch and push time out; a new file over 100 MB is left out
    (`LargeFiles`).
  - **Core, shell, others:** links encode `#`, parentheses and `%`; Windows device names refused; Obsidian's
    settings written atomically; the cache database kept unless it is broken; preference saves in order; the chat
    forgets a closed note; the side panel waits for its restore; three PDFs stay open; a deck's tag in any form;
    a page deleted from a section stays deleted; reserved address ranges refused; the hardware probed once; a
    selection of no case refused; releases test before packaging.
- **Fixed in the last round:** empty folders in Settings › Files (`AttachmentStore.folders`); a copy that fails
  leaves no file; two Notekeas merge what each changed into settings.properties; a comment after a YAML value
  (`YamlValues.withoutComment`); a token written in a remote's address goes to the keychain (`RemoteAddress`); a
  tree read before a rename (`TreeRenaming.withPending`); a poster is kept once made; pictures are decoded no wider
  than drawn (`decodedImage(maxWidth)`); the YouTube player joins the window by its title and hands the keyboard
  back (`MacWebView.contentView`, `yieldKeyboard`); CI counts a deleted document as code; the chat's web sources
  resolve from the real modules (`DesktopModulesTest`).
- **Left open on purpose** (none loses a note):
  - **Sync:** a repository left MERGING or REBASING by a crash is not repaired by itself: the only repair is a hard
    reset, which would discard what the person wrote since. It needs a decision, and a dialog.
  - **Editor:** a broken embed lists the vault again when the text changes. That listing is what finds a file
    added outside Notekea (`EditorAttachmentsTest`); a throttle needs the vault's watcher to tell the editor.
  - **YouTube:** the page's own full screen is not followed, and the window match was not seen on screen.
  - **Tree:** the note cache of a vault removed from the recents stays in the database (vault and tree are two
    features; it needs a port in the shell).
  - **Core:** `NoteTextCache` is one per consumer and unbounded; the three `HttpClient`s live as long as the app.
  - **Tests and CI:** `expectNoText` passes at once by design (`expectTextStaysAway` waits); milestone builds are
    versioned 1.M.K, above 1.0.0, which the first release will not install over; Koin's own `verify()` is not
    used (it needs koin-test).
