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
