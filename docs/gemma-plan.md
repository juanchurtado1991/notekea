# Gemma 4 as the one model family (decided 2026-11-23)

The developer decided on 2026-11-23 that Notekea uses **one model family, and that it is Gemma 4 (QAT)**, in place
of Qwen3.5. This file is the plan to get there: where Gemma stands in the loop today, how it is tuned, what is taken
from opencode, and what changes in the product at the end. Rules for all of it: `AGENTS.md` and
`docs/loop-goal-plan.md` (one measurement at a time, the pool read by cause only, no fix for one task).

## 1. Where it stands

Pool round 10 (439 tasks), commit `35f0260a`, the harness as tuned on Qwen. Label `gemma4-qat` in
`~/.cache/notekea-loop-runs`; Qwen is `s68-review` on the same commit. Files: unsloth's `UD-Q4_K_XL` of Google's QAT
weights, each checked against its SHA-256, in `~/.cache/notekea-spike/models-gemma-qat` (linked under the names the
evaluation loads: E2B as small, E4B as medium, 12B as large).

| Model | File | Memory (estimate) | Right of 439 |
|---|---|---|---|
| Qwen3.5-2B Q4_K_M | 1.28 GB | 1.5 GB (measured) | 163 (37.1 %) |
| **Gemma 4 E2B QAT** | 2.62 GB | ~3.0 GB | **242 (55.1 %)** |
| Qwen3.5-4B Q4_K_M | 2.74 GB | 3.1 GB (measured) | 324 (73.8 %) |
| **Gemma 4 E4B QAT** | 4.22 GB | ~4.7 GB | **315 (71.8 %)** |
| Qwen3.5-9B Q4_K_M | 5.68 GB | 5.9 GB (measured) | 330 (75.2 %) |
| **Gemma 4 12B QAT** | 6.72 GB | ~7.2 GB | being measured |

Gemma's memory is the file plus what the tournament's models added; it is not measured yet.

How the failures end, by the report's kind:

| Kind | Qwen 4B | Gemma E4B | Gemma E2B |
|---|---|---|---|
| ended the wrong way | 35 | 54 | 68 |
| never found it | 11 | 24 | 36 |
| found it, then did not say it | 13 | 21 | 17 |
| ran out of steps or repeated itself | 54 | 17 | 67 |
| routed to a reply of the chat's own, or stopped | 1 | 3 | 22 |

By cause, the E4B against the Qwen 4B: asked when it should not 25 against 17, figures 20 against 14, days 8 against
4; it is ahead on changes (30 against 36), on asking when it must (28 against 32) and on facts (10 against 12). 51
tasks went from right to wrong and 42 the other way: the two families fail on different tasks.

**One difference of the engine.** Gemma's template wraps a reply held to a schema in a ` ```json ` fence (166 of 166
replies of the frame evaluation). The schema still holds inside it. The rounds above ran with the fence taken off in
`LlamaLlmEngine` in the measuring worktree only, uncommitted: step 1 writes it in the app, with its test.

## 2. The plan

1. **A base that can be read, measured twice.** The pool is blind, so nothing is tuned on it. Measure the E4B and
   the E2B on the sets whose tasks may be read (`measure.sh <label> dev` and `tuned`, about 90 minutes a size), on
   the current commit with the fence fix committed. A second pool round with another seed goes beside round 10: one
   round moves about 4 tasks by chance, and a change that gains 5 must be told from that.
2. **What is the harness's and what is the model's, before anything is written.** The holds were set on Qwen. In the
   E4B's failed tasks "Not asked: the task names what it means" shows in 8 and "the answer says a figure no tool
   gave" in 7. Measure dev with each hold off, one at a time: taking one out may gain more than adding one.
3. **The instructions written again for Gemma, not adjusted.** They are long and shaped by what Qwen needed. Gemma
   takes half the steps (3 a task against 6), so it needs fewer warnings against repeating a call and a clearer
   word on how a turn ends.
4. **Tune by cause, the largest first, on the E4B**, then check the E2B and the 12B:
   - ending the wrong way (54): an answer where a proposal or a question was due, or the reverse;
   - never finding it (24) and finding it without saying it (21);
   - asking when the task's words settle it (25);
   - figures and days (28);
   - on the E2B also the route (22 turns sent to a reply of the chat's own).
5. **Each change the same way**: read the failures on dev, fix their cause in the foundation
   (`feedback`: no word list, no case for one task), measure dev, then one pool round. It stays when the pool gains
   4 or more and no other cause falls.
6. **The ceiling, once.** One pool round with Gemma 4 26B-A4B or 31B (17 to 22 GB, hours on this machine: they do not
   fit its 16 GB of video memory). Not a product size: it says how much of what is missing is the model's and how
   much the harness's or the exams', before 90 % is chased.
7. **The product, last**: the model catalog and its downloads, the memory budgets (the E2B does not fit the 2.5 GB
   the Small size was given with the reranker and the editor), the texts that name the models, and the answer and
   Spanish evaluations of the tournament again, where plain-Q4 Gemma was weakest. Speed counts here: the 12B took 93
   seconds a task on its first exam, against 27 for the Qwen 4B.

Targets are the loop plan's: 85 % on the medium size and 90 % on the large one.

## 3. The web needs a set of its own

The web is the weakest part and the least measured. Its evaluation (`docs/agent-web.md`, `WebEvalCases`) is 50 tasks
over one vault, on the real web, graded by a person from the report, not blind, and never run with Gemma. Qwen's
latest grades (2026-11-16) are 29½ of 50 on the 9B and 25 on the 4B: links left out, research that should end in a
note, deep notes of a page, prices. The set that file designed first, replayed without the network, was never built.

To build, with no GPU needed, while measurements run:

- **Recorded and replayed**: search results and pages kept in the repository and served by a fake provider, so
  every model and every commit reads the same web.
- **Graded by the build**, as the loop's sets are: each task's expected facts, each fact with the address of the
  page it came from, the untrusted sources left out, and the safety cases (a page that gives orders, a private
  person, a note asked for as a search) changing nothing.
- **About 100 tasks, part of them blind**: written and checked by agents that read nothing of the chat's code, as
  the blind exams were (`docs/eval-blind.md`), over several vaults and both languages.
- Then the web is tuned for Gemma by the plan above, cause by cause. Until it exists, a change to the web is graded
  by hand.

## 4. From opencode

Read on 2026-11-23 (`anomalyco/opencode`, its tools, edit, compaction and prompts). Its loop is built for large cloud
models and code, so the loop is not copied. Taken, each inside the step it serves:

- **Typographic marks when a line is looked for** (step 2, "never found it"): after the exact text fails, `’` reads
  as `'`, `–` and `—` as `-`, a no-break space as a space. `TaskMatch.plain` does not do it, and 677 of the vaults'
  2,209 notes hold one of these.
- **`calculate` over rows** (step 2, figures): a sum or a count of a table's column, the safe part of its Code Mode.
- **A summary of the chat with sections** (apart from the pool): what was asked, decided, which notes and figures
  came up, what is open; exact names kept, the latest turns left whole.

Not taken: prompts by family (one family now: the prompts are rewritten for Gemma), its repair of malformed
arguments (the schema already holds them) and its fixed text at the step limit (the loop already ends on what it
found).
