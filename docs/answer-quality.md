# Answer quality

What the chat's answers measure on the answer evaluation set (3.4), item by item of M3, so a change is kept only when
it helps (`ROADMAP.md` M3, `docs/HANDOFF.md`). The spikes before it are `docs/spikes/2026-11-08-answer-quality.md`
and `docs/spikes/2026-11-08-model-tournament.md`.

## How it is measured

```bash
./gradlew :app:chatModelTest -PchatModels=<folder> -PchatSize=small --tests '*AnswerEval*'
```

runs every question of `smoke-data/eval/answers.json` through the app's own answer (`AnswerFromNotesUseCase`): the
sample vaults as committed, indexed by the bundled embedding model in the real index, the chat model of the size given
(`-PchatSize=small|medium|large`, files from the catalog in the folder given) and the reranker, on the GPU unless
`-PchatGpu=false`. The report goes to `app/build/reports/answer-eval-<size>.txt`, each answer to `.tsv` next to it.
Below the baseline of its size the test fails. It never runs in `check` nor in the nightly build: the models weigh
gigabytes.

- **The set:** the 504 questions of the tournament: 437 the notes answer (106 on the Sample Vault, 331 on the Ghost
  Serializer, 80 of them in Spanish) and 67 traps they do not. Each answerable question has its facts, each written
  in the ways that count (`2½`, `two and a half`, `dos tazas y media`).
- **Right (strict):** every fact, in the question's language, not an excerpt pasted back (a heading, a table, a task
  box, or 25 words in a row of an excerpt). A trap is right when it is declined.
- **Cited right:** of the right answers, those whose cited passages hold the facts: what a person checking the
  citations finds.
- **Time:** from the question to the first words shown, and to the whole answer, with the GPU's 4 slots busy.

## 3.4 — the baseline: one call (3.2)

Linux, RTX 5060 Ti 16 GB, Vulkan, 4 slots; Qwen3.5 Q4_K_M and bge-reranker-v2-m3; 2026-11-10.

| Size | Right | Sample Vault | Ghost Serializer | Spanish | Traps declined | Cited right | First words (median / p90) | Whole answer (median / p90) |
|---|---|---|---|---|---|---|---|---|
| Small (2B) | 80 % | 60 % | 86 % | 63 % | 81 % | 99 % | 0.9 / 1.2 s | 1.6 / 2.4 s |
| Medium (4B) | 89 % | 84 % | 90 % | 83 % | 91 % | 98 % | 1.3 / 1.6 s | 2.6 / 4.0 s |
| Large (9B) | 85 % | 83 % | 86 % | 78 % | 96 % | 99 % | 1.5 / 2.0 s | 3.7 / 5.3 s |

What it says:

- **The app answers better than the tournament measured** (72 / 83 / 83 %): the app's index and its chunks, cut at
  headings and sized for the embedding model, find the passages better than the spike's 900-character pieces.
- **Small is weakest on the Sample Vault** (60 %): its notes are short, personal and full of tables (the weekly menu,
  the budget, the reading list), and the model often answers "not found" over a table it was given, or names the
  wrong row. Questions in Spanish over English notes follow it (63 %).
- **Small's answers that miss** mostly say "the excerpts do not say…" with a citation instead of `NOT_FOUND`, or mix
  two notes; Medium's misses are mostly a fact written another way than the set expects.
- **Medium beats Large** (89 against 85 %), as in the tournament (83 and 83 %): Large declines more traps (96 %) and
  writes facts in its own words more often than the set expects. Medium stays the recommendation from 16 GB.
- All three stay inside the latency budget of 3.0 (GPU 4 / 6 s); Large's p90 (5.3 s) is closest to it.
- **Baselines** (the test fails under them): 77 / 86 / 82 %, three points under this run, as batching on the GPU
  moves an answer now and then.

## 3.5 — steps measured against the one call

Each step of 3.5 is measured alone on the 3.4 set; one that does not beat the one call is not built (no dead code).

- **Rewriting the question into 2–3 queries:** not measured as a step. Retrieval already finds an expected note for
  94 % (Sample Vault) and 90 % (Ghost Serializer) of the questions (`RetrievalEvalRealModelTest`), and Medium declines
  an answerable question only 2 % of the time, so a rewrite could win at most two points while adding a model call
  (0.3–0.6 s on the GPU, seconds on the CPU) before every answer.
- **Extracting evidence, and removing unsupported sentences:** measured in the spike of 2026-11-08 on every size; both
  lose against the one call (extraction drops answers in tables and lists: Small 39 against 43 right, Medium 52
  against 59) and are four to eight times slower. Not built.
- **The note and the headings of each excerpt** (`(Arepas › Ingredients) Mix the flour…`), meant for answers that name
  what a passage only describes (a class of Ghost Serializer, a date in a daily note's name): Medium 389 right
  against 388, the same within noise (Ghost Serializer 91 against 90 %, Spanish 81 against 83 %). Not built.

So 3.2's single call stays the answer pipeline: none of 3.5's steps beats it.

## 3.6 — Deep answer

Three drafts written at once (temperature 0.7, seeds 1–3) over the same five passages; the draft whose words the
other two share most wins (`DraftVote`), and the notes do not answer when most drafts say so. Same set, same GPU.

| Size | One answer | Three drafts and a vote | Traps declined | Whole answer (median, GPU busy) |
|---|---|---|---|---|
| Small (2B) | 80 % (348) | 79 % (346) | 81 → 82 % | 1.6 → 3.3 s |
| Medium (4B) | 89 % (388) | **91 % (397)** | 91 → 94 % | 2.6 → 7.0 s |
| Large (9B) | 85 % (372) | 85 % (373) | 96 → 94 % | 3.7 → 9.9 s |

The times are with the 4 slots busy answering other questions; alone, the three drafts share the slots and take
about one answer's time more. Deep answer is offered only for Medium, the one size where it wins (`LlamaLlmEngine.DEEP_ANSWER_SIZES`), as the
spike of 2026-11-08 found: Small's drafts disagree without a majority being right, and Large is as good alone.

## 3.9 — the plans of actions

`smoke-data/eval/actions.json`: 106 requests over the Sample Vault (questions that must stay questions, notes by
their properties, tasks, the window, writing, organizing, refusals; English and Spanish), graded by
`ActionEvalChatModelTest`: right when the plan has the expected actions, in order, with the expected fields (a
folder's notes listed one by one count as the folder, and one action repeated per note as one).

| Size | Right | Median time |
|---|---|---|
| Small (2B) | 75 % → 73 % | 0.9 s |
| Medium (4B) | 92 % → 94 % | 1.9 s |
| Large (9B) | 91 % | 2.5 s |

Medium, the recommendation from 16 GB, passes the 90 % of 3.9. Small's misses are mostly fields under the wrong name
(the period of tasks inside the request) and questions turned into searches; every change still waits for the card,
which shows exactly what will be written, so a wrong plan is seen before anything is done.

After the M3 Auto-Test ran the model cases (the second figures, Large not measured again): the plan's period of
tasks is kept only when the request names it, and taken from the request's words when the model left it out or wrote
it under another field (`TaskPeriods`); a property the request gives a new note (", status draft") is set when the
plan leaves it out (`PropertiesNamed`); a template it names ("from the Meeting template") is kept (`TemplateNamed`);
a word of the title is no longer read as the note's content ("Call with Ana"); the plan sees every folder, also one
that holds only folders; a later step may name a note an earlier one creates. Small went from 75 % to 73 %,
still over its 72 % baseline.

With M4b the set has 126 requests (help, the vault's overview, clips and study cards added): Medium 95 % (120 of
126), Small 73 %. A request that writes a URL and asks for nothing else is a clip (`UrlsNamed`), and only a URL the
request writes is ever clipped.

## After the first smoke run — follow-ups, talk and a second try

- **Follow-ups:** the planner sees the chat's last four exchanges and writes the question as it stands alone
  ("and how long does it rest?" → "How long does the arepa dough rest?"), which is what the notes are searched for.
- **Talk:** a greeting, thanks, or a request for an opinion is a `chat` step, answered by `ConverseUseCase` in the
  message's language, saying an opinion is the model's own and reading the note it is about, shown as the source.
- **A second try (the light agent):** when the first search answers nothing in the whole vault, `AgentAnswerUseCase`
  makes up to three moves (search with other words, read a whole note) and answers with 3.2's prompt. Medium on the
  3.4 set, `-PchatAgent=true`:

| | Right | Spanish | Traps declined | Whole answer (median / p90) |
|---|---|---|---|---|
| Without | 89 % (388) | 83 % | 91 % (61 of 67) | 2.6 / 4.0 s |
| With the second try | 90 % (392) | 88 % | 88 % (59 of 67) | 3.1 / 5.6 s |

Four more answers right, Spanish questions most of them, against two traps answered that should have been declined;
the developer asked for it after the smoke run, and it stays on, recorded here.
- **Notes by their properties that find nothing** ("what trips do I have?" in a vault with a Travel folder and no
  property for it) are searched as any question instead of answering "No notes match".

## 4.4 — web clips

`ClipEvalChatModelTest`: 20 saved pages in `smoke-data/eval/web-pages` (news, blogs, docs, a recipe, and pages that are
not articles), clipped with Medium. All 20 got notes, every article was taken out without the site's menu, and 91 % of
the kept sentences are backed by the article's words (at least half the words of each, past its opening, are in the
article).

## 4.7 — agent mode

`AgentEvalChatModelTest`: 30 tasks over the committed Sample Vault that need a look-up before the change or the answer
(tag the books not finished, set a property on the project on hold, write a note from several notes, add a task to
the right note, link, move, and questions whose answer is in a property), graded on the card's steps or the final
answer (`AgentEvalCases`). A size offers agent mode at 80 % right.

| Size | Right | Moves (mean / max) | Time a task (mean / max) |
|---|---|---|---|
| Small (2B) | 10 % | 3 / 7 | 6 / 24 s |
| Medium (4B) | 23 % → 43 % → 73 % → 83 % | 3 / 8 | 11 / 32 s |
| Large (9B) | 70 % | 2 / 8 | 12 / 43 s |

Medium went up as the evaluation showed what was wrong, each fix with its test:

- **It looped:** the same `find` 24 times to the 30-move limit. A move made again now leaves only `propose` and `finish`
  in the grammar (`AgentTaskPrompt.END_SCHEMA`); the old "already done" note had the model believe a change was made.
- **Properties were invisible:** `read` gave a note's body without its frontmatter, and the index reads no properties,
  so the owner, the servings or a vendor could not be found. `read` writes the properties first, and `search` also
  lists the notes whose property values hold the query's words.
- **A task went to today's note:** a step that named its note in its list, not its `note` field, lost it; plan mode had
  the same gap (`VaultNames`).
- **An empty find said nothing:** it now says nothing was found and shows the query it ran, so the next move can try
  other words.

Medium and Large offer it (`LlamaLlmEngine.AGENT_SIZES`); Large below the 80 % by the developer's choice of
2026-11-11. Large proposes changes for plain questions and gives up after one search more often; Small rarely ends on
the right plan, and does not offer it. Every change still waits for the card, so a wrong plan is
seen before anything is written.

## The chat probe — talking to it as a person does

`ChatProbeChatModelTest` (2026-11-11) drives the app's own `ChatViewModel` over the real models and a copy of the
committed Sample Vault, with the 62 conversations of `smoke-data/eval/chat-probes.txt`: questions and follow-ups in
English, Spanish and French, actions, refusals, prompt injection, gibberish, complaints, creative requests. It writes
each conversation as Copy chat does, with the model's plans, to `build/reports/chat-probe-<size>.md`, a report to read
rather than a pass or fail. Six rounds on Medium found, each fixed with a test (`ChatProbeRulesTest`,
`ChatProbeRoundsTest`, `ChatProbeTest`):

- **Language:** replies came in English to Spanish questions. Every reply prompt now ends with the person's language
  (`SpokenLanguage`, `inLanguage`), the chat's when a message is too short to tell; `NOT_FOUND` is kept untranslated,
  and its paraphrases ("No se menciona en los extractos…") count as not found, so Auto still hands them to the agent.
- **Planner prompt leaks:** the language line named "a refusal's why", and the model refused follow-ups and copied the
  line as the question. The language and the retry after an unfounded refusal are said before the request now.
- **Wrong routes:** the vault's overview, a search for a question, a bookmark planned as showing the bookmarks, a folder
  named as a note, a whole folder listed note by note until the plan was cut (at most 12 notes now), a plain "delete"
  refused as deleting for good. Each has a shaping rule (`OverviewAsked`, `QuestionKept`, `PlanWords`).
- **A claimed change:** "delete the note Pancakes" refused twice was answered by the agent "has been deleted". A change
  refused twice keeps its refusal, the agent can propose the trash, and its summary never says it changed anything.
- **Answers:** properties are passages (`PropertyPassages`), sums are checked (`SumsChecked`), tables found are
  answered in words while the turn still answers, and a poem or a joke is written from the notes, short.

The action eval went from 92 % to 97 % (123 of 126) over the same work. Two more rounds on Medium and two on Large
followed:

- **Finished books:** "marked as finished" now reads the checkbox `finished` (`QueryPrompt`), the agent names the
  property the task means, and the tables found are answered with every property of their notes, so "and which one
  did I like most?" reads the rating. Medium answers 3 in Auto and in Agent mode.
- **The chat's start:** the first exchange is always given beside the last four, so "going back to the start: how
  much flour was it?" after ten turns is answered about the arepas on both sizes.
- **Compacted after a while:** a question asked half an hour after the last one, or in a chat from an earlier
  session, compacts the chat first, as a new session would.
- **Large:** "?" is talk, a move's target listed among its notes is its target, "in spanish please" is always the
  last reply said again, word counts come from the app (the model said 109, 128 and 63 for a note of 49 words).

Open: the Sample Vault contradicts itself on finished books (Reading list checks The Pragmatic Programmer and Atomic
Habits; the `finished` properties say Deep Work, Dune and The Pragmatic Programmer), so a model reading the list
answers 2. On Medium the complaint and the joke still vary from run to run.
