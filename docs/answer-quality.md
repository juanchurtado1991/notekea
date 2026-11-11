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
| Small (2B) | 75 % | 0.9 s |
| Medium (4B) | 92 % | 1.9 s |
| Large (9B) | 91 % | 2.5 s |

Medium, the recommendation from 16 GB, passes the 90 % of 3.9. Small's misses are mostly fields under the wrong name
(the period of tasks inside the request) and questions turned into searches; every change still waits for the card,
which shows exactly what will be written, so a wrong plan is seen before anything is done.

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
