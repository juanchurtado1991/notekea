# Spike — Model tournament on 504 questions (2026-11-08)

Goal: check that the 3.0 defaults (Qwen3.5 2B, 4B and 9B) are still the best open models for Notekea's answers,
against the other current small models, on a question set large enough to separate them. Asked by the developer on
2026-11-08, after `docs/spikes/2026-11-08-answer-quality.md` showed that 79 questions leave about ±10 points of
noise.

Like the other spikes it ran on Linux only, outside the repository, with the 3.0 bench
(`~/.cache/notekea-spike/bench`, scripts `qset.py`, `q1b.py`, `q12.py`–`q15.py`). Nothing in the app changed. Same
machine and settings: RTX 5060 Ti by Vulkan, 4 slots, context 4,096 a slot, seed 42, temperature 0.2, reasoning off,
Q4_K_M files. Every model answers the same way: the one-call answer of the quality spike (five reranked chunks,
numbered citations, NOT_FOUND when they do not answer) behind the −8 reranker gate.

## Candidates

All public and not gated, with an Apache 2.0 or MIT license; each file checked against its SHA-256. Memory is the
resident memory of the process on the CPU without mmap, with 1 slot and context 4,096, after one answer (MB).

| Model | Source | File | Memory |
|---|---|---|---|
| Qwen3.5-2B | unsloth | 1.28 GB | **1,526** |
| SmolLM3-3B | ggml-org | 1.92 GB | 2,307 |
| Ministral-3-3B-Instruct-2512 | mistralai | 2.15 GB | 2,724 |
| Phi-4-mini-instruct | unsloth (MIT) | 2.49 GB | 3,087 |
| Gemma 4 E2B | unsloth | 3.11 GB | 3,428 |
| Qwen3.5-4B | unsloth | 2.74 GB | 3,068 |
| Gemma 4 E4B | unsloth | 4.98 GB | 5,435 |
| Qwen3.5-9B | unsloth | 5.68 GB | 5,879 |

**Left out by the developer:** the "Qwen3.8" 2B, 4B and 9B of `empero-ai`, community distillations of Qwen3.8 into
the Qwen3.5 architecture (Apache 2.0). Alibaba has published no small Qwen3.8 (only a 27B and a 2.4T flagship). The
product downloads its models from these links, so a small community account that may move or delete its files is
a risk; if one is ever wanted, Notekea would serve a copy of its own.

## Questions

504 questions over two vaults, each with the facts a right answer must contain (accepted spellings in English and
Spanish), checked by script to exist in the expected note; the two exceptions are on purpose, computed answers
(180 = 300 − 120).

| Vault | Answerable | Traps | In Spanish |
|---|---|---|---|
| Ghost Serializer (371 technical notes) | 331 | 30 | 56 |
| Sample Vault (40 personal notes) | 106 | 37 | 41 |
| **Total** | **437** | **67** | **97** |

- 92 come from the quality spike (the 80 of 2.8 and its 12 traps); about 410 are new, written by Claude from the
  notes: one per Ghost note, and for the Sample Vault harder ones (sums, tables, dates, 19 that need two notes).
- **Traps** sit next to the vaults' topics with no answer in the notes ("Which Ghost class reads CSV files?", "¿En
  qué hotel de Sintra dormimos?"); each was checked by searching the whole vault, and the doubtful ones were dropped.
- The search finds an expected note in the top 5 for 427 of the 437 answerable questions.
- With 437 answerable questions, a difference is clear above about ±5 points (95 %).

## Grading

- **Facts:** right when every expected fact is in the answer; on a trap, right when it declines.
- **Strict facts:** the same, and the answer must also be in the question's language and must not be a pasted
  excerpt (markdown headings, table rows or task boxes, or a run of 25 words copied from a chunk).
- **Manual (Claude):** every Sample Vault answer of four models (143 each), with the codes of the quality spike;
  pasting an excerpt instead of answering, or answering a Spanish question in English, counts as partial.

How often each automatic grade agrees with the manual one on those 143 answers:

| Model | Facts | Strict facts |
|---|---|---|
| Qwen3.5-2B | 0.87 | 0.88 |
| SmolLM3 | **0.62** | **0.93** |

The plain facts grade can be fooled: SmolLM3 often pastes the chunks it was given, which contain the facts, so it
scores 87 % on the Sample Vault by facts and 37 % right by hand. The tables below use the strict grade.

## Results

Strict grade; answerable out of 437 and by vault and language, traps declined out of 67, median time of a batch of
4 answers on the GPU.

| Model | Memory | Right | Sample Vault | Ghost | Spanish | Traps declined | Pasted | Other language | 4 answers |
|---|---|---|---|---|---|---|---|---|---|
| **Qwen3.5-2B** | **1.5 GB** | **72 %** | 58 % | 76 % | 59 % | 73 % | 1 | 5 | 0.9 s |
| SmolLM3 | 2.3 GB | 34 % | 40 % | 32 % | 6 % | 60 % | 175 | 70 | 1.2 s |
| Ministral 3B | 2.7 GB | 62 % | 59 % | 63 % | 50 % | **96 %** | 0 | 3 | 1.2 s |
| Phi-4-mini | 3.1 GB | 50 % | 56 % | 48 % | 39 % | 87 % | 41 | 28 | 1.0 s |
| Gemma 4 E2B | 3.4 GB | 69 % | 77 % | 66 % | 62 % | 55 % | 13 | 9 | 1.0 s |
| **Qwen3.5-4B** | **3.1 GB** | **83 %** | 81 % | 84 % | 82 % | 93 % | 1 | 1 | 2.2 s |
| Gemma 4 E4B | 5.4 GB | 75 % | 76 % | 74 % | 61 % | 94 % | 11 | 18 | 1.9 s |
| **Qwen3.5-9B** | **5.9 GB** | **83 %** | 87 % | 82 % | 79 % | 96 % | 3 | 2 | 3.2 s |

Manual grade of the Sample Vault (106 answerable, 37 traps):

| Model | Right | Partial | Wrong | Missed | Traps declined |
|---|---|---|---|---|---|
| Qwen3.5-2B | 51 % | 14 % | 11 % | 24 % | 31 of 37 |
| SmolLM3 | 37 % | 43 % | 17 % | 3 % | 25 of 37 |
| Qwen3.5-4B | 83 % | 9 % | 2 % | 6 % | 35 of 37 |
| Qwen3.5-9B | 87 % | 8 % | 3 % | 3 % | 35 of 37 |

**Qwen3.5 wins every size.**
- **Small:** Qwen3.5-2B is right most often (72 %) with the least memory (1.5 GB, the only candidate under 2 GB).
  Gemma 4 E2B does better on the personal notes (77 %) but answers 45 % of the traps and needs 3.4 GB, more than
  Qwen3.5-4B. Ministral 3B is the most careful (96 % of traps declined) but also declines 23 % of the answerable
  questions. SmolLM3 and Phi-4-mini paste chunks and answer Spanish questions in English (SmolLM3 answers 6 % of the
  Spanish questions right).
- **Medium:** Qwen3.5-4B (83 %, 3.1 GB) beats Gemma 4 E4B (75 %, 5.4 GB) with less memory, and gets 2 % of the
  personal-note answers wrong against Small's 11 %.
- **Large:** Qwen3.5-9B equals Medium on these questions (83 %), a little better on the personal notes (87 % against
  81 %), at twice the memory and 1.5 times the time. Its value stays in plans and agent mode (3.0) and in harder
  questions than these.
- The 79-question spike put Small at 54 % right on its Sample Vault questions; here, on 106 harder ones, 51 %. The
  measures agree.

## Decisions

- **The defaults stay:** Small Qwen3.5-2B, Medium Qwen3.5-4B, Large Qwen3.5-9B, now measured on 504 questions
  against five other models.
- **3.4 grades with strict facts:** facts plus the question's language and no pasted excerpt. Plain facts let a model
  that pastes its chunks look good.
- **The 504 questions start the 3.4 set** (`bench/qset.json`, with the manual grades in `bench/mineS.py` and
  `bench/mineS2.py`). Moving them into `smoke-data/eval/` is part of 3.4.

## Not measured

- **The Qwen3.8 distillations** of `empero-ai` (left out, above), and models released after mid-2026 that the
  searches did not find.
- **Questions from someone else:** questions, facts, traps and manual grades are by the same agent, and 361 of the
  504 are about Ghost; the Sample Vault, closer to real use, has 143.
- **Voting and the other strategies** of the quality spike, which ran on 92 questions only.
- **Windows, macOS, CUDA**, reasoning turned on, as in the other spikes.
