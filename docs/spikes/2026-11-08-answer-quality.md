# Spike — Answer quality (2026-11-08)

Goal: measure how good the answers of the 3.0 defaults (Small Qwen3.5-2B, Medium Qwen3.5-4B, Large Qwen3.5-9B) are,
before 3.1–3.5 are built on them. It answers the last question of `ROADMAP.md` 3.0 (does a 1–2B model reach a usable
quality?) and tries the grading 3.4 will automate. Asked by the developer on 2026-11-08, after the 3.0 report showed
that its "supported" score was the model grading itself.

**Grading by Claude.** The developer has no context of the test notes, so they asked Claude (Opus 5.5, the agent
that ran the spike) to read every answer against its note and grade it, standing in for the human review. The same
agent wrote the questions' expected facts and the trap questions, so the grades share one author's judgment; 3.4
should have someone else check a sample.

Like 3.0, it ran on Linux only, outside the repository, with the 3.0 bench (`~/.cache/notekea-spike/bench`, scripts
`q1.py`–`q6.py`). Nothing in the app changed. Same machine and settings as 3.0: RTX 5060 Ti by Vulkan with every
layer on the GPU, 4 slots, context 4,096 a slot, seed 42, temperature 0.2, reasoning off; Q4_K_M files.

## Questions and graders

- **92 questions:** the 80 of 2.8 (Sample Vault and Ghost Serializer, 8 in Spanish) and 12 new **trap questions**
  near the vaults' topics whose answer is not in the notes ("How long do the pancakes cook on each side?", "Which
  Ghost class parses XML?"). Question 29 of 2.8 ("Who is the supplier for the new counters?") has no answer in its
  note either, so it counts as a trap: **79 answerable, 13 traps**.
- **Expected facts:** for each answerable question, the facts the answer must contain, each with its accepted
  spellings (`2½` / `two and a half`; `comino` / `cumin`; a class name such as `GhostCoerce`).
- **Manual grade (Claude):** each answer against the note it should come from. **Right**: answers the question and
  agrees with the note. **Partial**: right but missing something important (describes the book without naming
  it), mixed with something wrong, or in the wrong language. **Wrong**: contradicts the note, answers something
  else, or states a fact the notes do not have. **Missed**: says it found nothing when the note has the answer. On
  a trap, declining is right and any answer is wrong.
- **Automatic grade:** right when every expected fact is in the answer; on a trap, right when it declines.
- **Model judge:** Qwen3.5-9B sees the expected note, the question and the answer and says correct, partial, wrong
  or declined; declining is graded in code, not by the judge.

How often each automatic grade agrees with the manual one (right or not, over the 92 questions):

| Answers graded | Expected facts | 9B judge |
|---|---|---|
| Small, 4 ways of answering | 0.90–0.95 | 0.76–0.87 |
| Medium, 2 ways | 0.97 | 0.87–0.88 |
| Large, 2 ways | 0.93 | 0.86–0.87 |

**The expected facts are the better automatic grade.** Its misses are partial answers that still contain the fact
(pancakes named instead of arepas, with the right five minutes) and two fact lists written too strictly (#58 asks
for "stream", #68 for "singleton"). The judge is kinder than the manual grade: left to grade declined answers itself,
it called 7–13 of them "correct" although the note had the answer (hence the rule in code), and it still calls some
partial and wrong answers correct, so it cannot replace the facts.

## Search

Embedding (top 20) then the bge reranker (top 5), as in 3.0; also BM25 and a hybrid of both (reciprocal rank),
reranked the same way.

| Search, then reranker | Right note in the top 5 | In the top 8 |
|---|---|---|
| Embedding | **78 / 80** | 78 / 80 |
| BM25 | 74 / 80 | 74 / 80 |
| Hybrid | 78 / 80 | 78 / 80 |

Hybrid search adds nothing on this set, and five chunks are enough. The two misses are #69 and #72, as in 3.0. Of
the eight times 3.0's pipeline missed the right note, six were not search: the right chunk was in the top 5 and the
extraction step threw it away.

## Ways of answering

All four answer from the same five chunks and end the prompt the same way: "Answer in one to three sentences using
only the excerpts, in the language of the question. Cite each sentence with the number of its excerpt, like [1].
If the excerpts do not contain the answer, reply exactly NOT_FOUND. Do not explain your reasoning."

- **Direct:** one call over the five chunks.
- **Direct, drop:** then one call per sentence ("Do the excerpts support this sentence?", grammar-constrained) and
  the unsupported sentences are removed; nothing left means NOT_FOUND.
- **Pipeline:** 3.0's steps from `ARCHITECTURE.md` §12: one call per chunk copies the sentence that answers, the
  answer is written only from those quotes, then each sentence is checked against them.
- **Pipeline, drop:** the pipeline with unsupported sentences removed.

The gate of 3.0 (best reranker score below −5: answer NOT_FOUND without the model) is on in all four.

## Results

Answerable questions out of 79; traps declined out of 13; median time of a whole answer on the GPU.

| Model | Way | Right | Partial | Wrong | Missed | Traps declined | Median |
|---|---|---|---|---|---|---|---|
| Small 2B | **Direct** | **42** | 14 | 10 | 13 | 10 | **0.23 s** |
| Small 2B | Direct, drop | 35 | 13 | 7 | 24 | 10 | 0.44 s |
| Small 2B | Pipeline | 20 | 20 | 9 | 30 | 11 | 1.9 s |
| Small 2B | Pipeline, drop | 18 | 18 | 5 | 38 | 12 | 1.9 s |
| Medium 4B | **Direct** | **56** | 14 | 2 | 7 | 12 | **0.44 s** |
| Medium 4B | Direct, drop | 49 | 13 | 3 | 14 | 13 | 0.87 s |
| Large 9B | **Direct** | **58** | 11 | 2 | 8 | 12 | **0.71 s** |
| Large 9B | Direct, drop | 56 | 10 | 3 | 10 | 12 | 1.4 s |

The pipeline of Medium and Large was graded automatically only: 42 and 44 answers with every fact (Direct: 67 and
70), 2.5 s median each.

**Direct is better for every model, and four to eight times faster.**
- The extraction step loses the answer when it is not one sentence: tables (the weekly menu, the budget), lists (the
  reading list, the garden beds) and answers spread over two lines come back as "none", and the pipeline says it
  found nothing. Small misses 30 of 79 with the pipeline and 13 with Direct.
- Dropping unsupported sentences removes more right answers than wrong ones: on Small, 7 fewer right answers for 3
  fewer wrong ones; on Large, 2 fewer right for no fewer wrong. The small checking call says "not supported" to
  correct sentences that cite a table or a heading.
- Citations point to the right note in 87–96 % of the answers that cite (Small 59 of 68, Medium 66 of 69, Large 68
  of 71).
- No answer printed its reasoning (3.0's #19 did): "Do not explain your reasoning" and NOT_FOUND fixed it.

### By model size (Direct)

| Model | Right | Right or partial | Wrong | Missed | Traps declined |
|---|---|---|---|---|---|
| Small 2B | 53 % | 71 % | 13 % | 16 % | 10 of 13 |
| Medium 4B | 71 % | 89 % | 3 % | 9 % | 12 of 13 |
| Large 9B | 73 % | 87 % | 3 % | 10 % | 12 of 13 |

- **Small** answers about half the questions well and gets one in eight wrong. Its wrong answers are believable:
  it mixes two chunks (the third garden bed "also has tomatoes, peppers and basil"), names the wrong dish (pancakes
  for arepas), picks a nearby note (black beans cooked "on Tuesday" from the menu instead of the journal date), or
  answers a trap from a similar chunk (pancakes "five minutes a side", which is the arepas). It also declines too
  often (16 % of answerable questions).
- **Medium** is the first size that answers well: two wrong of 79, and most partials describe the thing without
  naming it (Dune, Local AI, `GhostResilient`).
- **Large** is not better than Medium on these questions; its value is plans and agent mode (3.0) and harder
  questions than these one-note ones.
- All three repeat the same two mistakes: "tracer bullets are small, reversible decisions" (the note lists both,
  unrelated) and "the kitchen money is tracked in the budget" (the decision was "wait until spring").

## The gate

With the gate at −5, four answerable questions never reached the model (#1, #27, #32, #39, scores −7.6 to −6.1),
and four traps were stopped. Asked without the gate:

| Model | Of the 4 answerable | Of the 4 traps |
|---|---|---|
| Small | 1 right, 1 partial (answered a Spanish question in English) | all declined |
| Medium | 3 right | all declined |
| Large | 4 right | all declined |

The NOT_FOUND instruction already makes the models decline (12 of 13 traps for Medium and Large), so the −5 gate
costs answers and buys nothing here. On 3.0's question set (80 answerable, 16 unrelated):

| Gate | Answerable that reach the model | Unrelated stopped without the model |
|---|---|---|
| −5 (3.0) | 76 of 80 | 16 of 16 |
| −7 | 79 | 12 |
| **−8** | **80** | **10** |

The near-miss traps score up to −0.9, so no gate catches them: the model has to decline those itself.

## CPU time (Direct)

One slot, no mmap, the 46 even-numbered questions that pass a −8 gate; the time of the answer call (the reranker
adds 0.6–1.1 s per 20 chunks on the CPU, 3.0).

| Model | CPU | Median | 90th percentile | 3.0's pipeline (median) |
|---|---|---|---|---|
| Small | 4 threads | **2.3 s** | 3.8 s | 10.5 s |
| Small | 12 threads | 1.7 s | 3.1 s | 9.9 s |
| Medium | 12 threads | 3.2 s | 6.7 s | — |

With search and reranking, a whole Direct answer from Small on a 4-thread CPU takes about 3.5 s, well inside 3.0's
budget of 12 / 15 s, and Medium becomes usable on the CPU too.

## Decisions for 3.1–3.5 (preliminary until 3.4)

- **Answer in one call (Direct) in 3.2 and keep it in 3.5 unless 3.4 shows a step that beats it:** five reranked
  chunks, numbered citations, NOT_FOUND when they do not answer. The prompt above is the starting point.
- **Extraction and dropping unsupported sentences are not the default.** Both lowered quality on every model here.
  3.5 measures them again on the 3.4 set before keeping either; marking an unsupported sentence instead of removing
  it was not tried.
- **Gate at −8 instead of −5:** it keeps every answerable question and still skips the model for clearly unrelated
  ones.
- **3.4 grades with expected facts** (automatic, about 0.9–0.97 agreement with a manual grade) and uses a model judge
  only as a second opinion. The facts and traps of this spike (`bench/facts.json`) are the start of its set.
- **Model sizes:** Medium is the first size that answers well; Small gets one answer in eight wrong, so it needs its
  sources in view.

## Open decision for the developer

Small fits an 8 GB laptop and Medium does not (3.0: 2.7–3.0 GB with Small already). Options for 8 GB:
1. **Small, with the sources first:** the answer shows its cited notes prominently and a one-line note that answers
   from the small model should be checked. Recommended: the right note is in the top 5 for 78 of 80 questions, so
   the sources are useful even when the sentence is not.
2. Small that only finds notes (search, no written answer) on 8 GB; chat from Medium up.
3. Medium on 8 GB anyway, over the 2.5 GB budget of 5.4.

## Not measured

- **Questions from someone else:** the questions, facts, traps and manual grades are by the same agent; easy,
  one-note questions dominate (the 2.8 set), so 3.4 needs harder ones (two notes, dates, comparisons).
- **Windows, macOS and CUDA**, as in 3.0; and the CPU only for Direct.
- **Gemma 4 E4B**, the other Medium candidate of 3.0, and reasoning turned on.
- **Marking unsupported sentences** instead of removing them, and a stronger checking model.
