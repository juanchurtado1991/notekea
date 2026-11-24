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
| **Gemma 4 12B QAT** | 6.72 GB | ~7.2 GB | **357 (81.3 %)** |

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

The 12B fails 82: asking when it must 34, asking when it should not 19, changes 16, figures 8, facts 3, days 1,
other 1. By kind: ended the wrong way 50, never found it 11, ran out of steps 10, did the wrong thing 6, found it and
did not say it 4, routed 1. Finding and working out are nearly solved at this size; **53 of its 82 failures are the
choice between asking and going on**, which is the hold's and the instructions' ground (steps 2 and 3). Its round
took 195 minutes against 49 for the E4B. Neither video memory (10.5 of 16.3 GB in use) nor a fall to the CPU is the
cause: one call alone, timed on this card, reads 1,370 tokens a second and writes 49, against 2,210 and 65 for the
Qwen 9B and 3,760 and 101 for the E4B, the same with 1 slot as with 4 and with a schema as without. That is 1.3 to
1.6 times the Qwen 9B a token; the rest of the 2.8 times a task is in how much a task reads and writes, not looked
into yet.

**One difference of the engine.** Gemma's template wraps a reply held to a schema in a ` ```json ` fence (166 of 166
replies of the frame evaluation). The schema still holds inside it. The rounds above ran with the fence taken off in
the measuring worktree only; the app takes it off as the reply streams since `JsonFence` (`LlamaLlmEngine`).

**The base that can be read** (plan step 1). Label `gemma4-base`, the harness as tuned on Qwen, the fence taken off
by the app: the E4B on `e9dee957`, the E2B on `be6346bb` (the same loop: its replies read by Ghost, equal to the old
reader on 57,862 recorded cases).

| Set | Gemma E4B | Gemma E2B |
|---|---|---|
| dev (393) | **281 (71.5 %)**, 47 min | **215 (54.7 %)**, 33 min |
| tuned (259) | **201 (77.6 %)**, 39 min | **162 (62.5 %)**, 31 min |

Dev says what the pool said, within a point (71.8 % and 55.1 %), so a gain on dev can be believed. Failures by cause:

| Cause | E4B dev | E4B tuned | E2B dev | E2B tuned |
|---|---|---|---|---|
| should have asked, did not | 17 | 8 | 43 | 9 |
| asked, should not have | 36 | 6 | 27 | 6 |
| days between dates | 9 | 2 | 7 | 1 |
| figure worked out | 17 | 10 | 30 | 15 |
| change wrong or incomplete | 25 | 24 | 38 | 43 |
| fact wrong or incomplete | 7 | 6 | 11 | 11 |
| other (routed to talk, stopped) | 1 | 2 | 22 | 12 |

And by the report's kind:

| Kind | E4B dev | E4B tuned | E2B dev | E2B tuned |
|---|---|---|---|---|
| ended the wrong way | 46 | 13 | 58 | 13 |
| never found it | 27 | 10 | 36 | 18 |
| ran out of steps or repeated itself | 21 | 9 | 62 | 31 |
| found it, then did not say it | 11 | 4 | 15 | 5 |
| did the wrong thing | 4 | 21 | 3 | 25 |
| routed where the turn cannot end, stopped, call too long | 0 | 1 | 4 | 5 |

The E4B's largest cause on dev is asking when the task's words settle it (36 of 112), which is the holds' ground
(step 2). The E2B's are the reverse, going on when it must ask (43), and running out of steps (62 of 178), which the
E4B hardly does. On the tuned set both fail mostly on changes: the wrong change proposed, not the wrong ending.

**Each hold off, one at a time** (plan step 2, done 2026-11-24). Dev on the E4B, commit `ad03338d`, labels
`gemma4-off-<name>`; the switch lived in the measuring worktree only. A second base on the same commit
(`gemma4-base2`) gave 284, so the base is 281 to 284.

| Hold taken off | Right of 393 |
|---|---|
| none (the two bases) | 281, 284 |
| the question the frame says is needless | 283 |
| a related note not read before an answer | 287 |
| the review of a proposal on many notes | 287 |
| the note the frame says is meant | 288 |
| a figure no tool gave | 286 |
| an option not read before a question | 287 |
| each of several notes not read | 283 |
| an end where the frame says to ask | 288 |
| the change tools' own (several notes, a shared name) | 286 |
| a proposal with nothing added | 289 |

**No hold is the cause.** Every round lands within 5 tasks of the second base, whichever hold is off, and two rounds
of the same code differ by 3: none can be told from chance, and none is taken off. Asking when the task's words
settle it stays at 26 to 38 failures with any hold off, the needless question's own included (38): the model asks
on its own, not because a hold sends it there. That leaves the instructions and the engine's settings (step 3).

## 2. The plan

1. **A base that can be read, measured twice.** The pool is blind, so nothing is tuned on it. Measure the E4B and
   the E2B on the sets whose tasks may be read (`measure.sh <label> dev` and `tuned`, about 90 minutes a size), on
   the current commit with the fence fix committed: **done 2026-11-23**, the tables above. A second pool round with
   another seed goes beside round 10: one round moves about 4 tasks by chance, and a change that gains 5 must be told
   from that.
2. **What is the harness's and what is the model's, before anything is written.** The holds were set on Qwen. In the
   E4B's failed tasks "Not asked: the task names what it means" shows in 8 and "the answer says a figure no tool
   gave" in 7. Measure dev with each hold off, one at a time: taking one out may gain more than adding one.
   **Done 2026-11-24**: none gains more than chance, the table above.
3. **The engine's settings, then the instructions written again for Gemma, not adjusted.** First two dev rounds on
   the E4B, one each, as either changes how the instructions are best written (section 5): Google's sampling in
   place of temperature 0, and thinking on. **Done 2026-11-24**: neither is taken, section 5. Then the
   instructions: they are long and shaped by what Qwen needed.
   Gemma takes half the steps (3 a task against 6), so it needs fewer warnings against repeating a call and a
   clearer word on how a turn ends.
4. **Tune by cause, the largest first, on the E4B**, then check the E2B and the 12B:
   - ending the wrong way (54): an answer where a proposal or a question was due, or the reverse;
   - never finding it (24) and finding it without saying it (21);
   - asking when the task's words settle it (25);
   - figures and days (28);
   - on the E2B also the route (22 turns sent to a reply of the chat's own).
5. **Each change the same way**: read the failures on dev, fix their cause in the foundation
   (`feedback`: no word list, no case for one task), measure dev, then one pool round. It stays when the pool gains
   4 or more and no other cause falls.
6. **The product, last**: the model catalog and its downloads, the memory budgets (the E2B does not fit the 2.5 GB
   the Small size was given with the reranker and the editor), the texts that name the models, and the answer and
   Spanish evaluations of the tournament again, where plain-Q4 Gemma was weakest. Speed counts here: the 12B took 93
   seconds a task on its first exam, against 27 for the Qwen 4B. The 8-bit files of the E2B and the E4B
   (section 5) are weighed here, as they change the download and the memory.

No round with Gemma 4 26B-A4B or 31B: the developer left them out on 2026-11-23, as neither is a size of the
product. How much of what is missing is the model's is seen by tuning.

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

## 5. From Gemma's own documentation

Read on 2026-11-23 (gemma.ai's prompt formatting, function calling and best practices, Unsloth's guide, the
`google-gemma/cookbook`). No repository holds an agent's instructions written for Gemma 4, and none of these says how
to write them (length, order, examples). What they do say, against what the loop does:

- **Sampling**: Google asks for temperature 1.0, top_p 0.95 and top_k 64 "across all use cases". The loop's calls
  run at temperature 0. **Measured 2026-11-24** (E4B, dev, label `gemma4-sampling`): 269 of 393 against 281 and
  284, with more turns that should have asked (25 against 17) and more wrong changes (29 against 25). Temperature 0
  stays.
- **Thinking**: on with `<|think|>` at the start of the system message, off in the engine today (`enable_thinking`
  false, set for Qwen). The largest cause of the three sizes is the choice between asking and going on, which
  thinking first may help; it costs time a step, to be measured with it. **Measured 2026-11-24** (E4B, label
  `gemma4-think`, a budget of 256 tokens of thought a call, which the JSON schema allows): on the frame alone 15
  false alarms of 319 against 25, yet on dev **291 of 393 in 115 minutes against 281 and 284 in 47**. Wrong changes
  fall (11 against 25) and so do figures (11 against 17), while asking needlessly rises (45 against 36): a cause
  falls and each task costs 2.4 times the time, so it is not turned on. What it says for step 4: the changes and
  the figures are within the model's reach when it works them out first, so thought on those calls alone, or the
  working-out asked for in the reply, is worth a round there.
- **Its own tool calls**: Gemma 4 was trained on `<|tool_call>call:name{…}<tool_call|>` and the loop holds each reply
  to a JSON schema, which it wraps in a fence. The largest change of these; after the tuning, if the ending of a
  turn is still the main failure.
- **8 bits for the small ones**: Unsloth starts the E2B and the E4B at Q8; the files here are Q4. A matter of the
  product's sizes (step 6).

Not ours: the template's loop when a harness sends back the reasoning of earlier steps (the loop sends none), and
FunctionGemma (Gemma 3, 270M).

## 6. The frame and a reason before each call, tried on the E4B

Measured on 2026-11-24 on dev (393 tasks, base 281 and 284 in 47 minutes; one round moves about 4 by chance). None
has gone to the pool, so none is in the code: the changes are kept as a patch
(`~/.cache/notekea-loop-runs/gemma4-why/experiment.patch`) to go on from.

**Where the needless questions come from.** Of the 36 on the base, 19 are asked because the frame says the task is
open, not because the model chooses to in the loop:

- a table's heading row is shown as an entry and marked "yes" (`| Bill | Amount | Due | Paid |` for "mark the water
  bill as paid"): 8 tasks of the frame alone;
- a request that adds something new (a task, a row, a line) is called an item already written, so the note's entries
  are marked: 6;
- two notes of one person or thing are taken for two things (a school report and a school note of one child, a
  meeting and the person's note): 5, not tried yet.

| Tried, each on top of the one before | Right | Asked needlessly | Should have asked | Change | Figure | Minutes |
|---|---|---|---|---|---|---|
| Base | 281, 284 | 36 | 17 | 25 | 17 | 47 |
| Heading rows are no entries; "new" as a third kind of thing (`gemma4-f1`) | 286 | 30 | 17 | 26 | 16 | 45 |
| The open frame says a change to the whole note needs no question (`gemma4-f4`) | 285 | 27 | 17 | 28 | 20 | — |
| A sentence of why before the tool in each call (`gemma4-why`) | 291 | 37 | 18 | 27 | 6 | 67 |

The frame alone (`measure.sh <label> frame medium`, 11 minutes) was used to word the count question: three wordings
(`gemma4-f1`, `-f2`, `-f3`) end at 49 to 52 errors against the base's 57 over dev and tuned, each moving failures
between asking too much and too little. The gain is the two changes of the first; the wording after it is chance.

What it says:

- The frame's changes take the needless questions from 36 to 27 and lose no question that was due, yet the total
  stays within chance: what is gained there is lost in changes and figures.
- A reason written first does for figures what thinking did (17 to 6, thinking 11) at 1.4 times the time in place of
  2.4, and like thinking it brings the needless questions back (37). It does nothing for changes, which thinking
  halved.
- So the next round is the reason kept for the calls that work out a figure and left out of the choice to ask, with
  the frame's changes, then the pool. The changes (25 to 28 whatever is tried, but 11 with thinking) are the cause
  to read next.
