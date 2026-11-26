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
7. **The study cards, once the tuning ends** (the developer, 2026-11-24): section 8, in this same pull request, in
   commits of their own. It begins when steps 4 and 5 are done and does not wait for step 6. It moves neither target.

No round with Gemma 4 26B-A4B or 31B: the developer left them out on 2026-11-23, as neither is a size of the
product. How much of what is missing is the model's is seen by tuning.

Targets: 80 % on the medium size, held on more than one sample of the blind pool and in a round without kept replies
(the developer, 2026-11-25; the loop plan's was 85 %), and 90 % on the large one.

**Decided with the developer, 2026-11-24:**

- **Time**: a task may take up to 1.5 times what it takes today. A reason written before a call fits (1.4 measured);
  thinking on every call does not (2.4).
- **The target not reached**: if the E4B ends the tuning near 80 %, that is accepted, what is missing is written down by
  cause, and the plan goes on. No patch is made to reach a number.
- **The web** (section 3): its set and its tuning go in a pull request after this one, designed with the developer.
- **A card's evidence** (section 8): a hidden HTML comment on the card, tried with the plugin first.

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

**What was kept, 2026-11-24** (E4B; dev and the pool without kept replies):

| | Right | Should have asked | Asked needlessly | Days | Figure | Change | Fact | Minutes |
|---|---|---|---|---|---|---|---|---|
| Dev (393), three bases | 281, 284, 285 | 16 to 17 | 32 to 36 | 7 to 9 | 17 to 21 | 23 to 26 | 7 | 43 to 47 |
| Dev, all of it (`gemma4-w3-full`) | **299** | 14 | 22 | 5 | 20 | 24 | 9 | 46 |
| Pool (439), the base (`gemma4-qat`) | 315 | 28 | 25 | 8 | 20 | 30 | 10 | 49 |
| Pool, the reason for figures alone (`gemma4-fig-pool`) | 322 | 28 | 29 | 5 | 14 | 31 | 7 | 49 |
| Pool, all of it (`gemma4-w3-pool`) | **328** | 29 | 25 | 4 | 12 | 31 | 7 | 48 |

- **A reason in the tools that work out a figure** (`tally`, `calculate`, `days`): `why`, written after the tool's name
  and before its figures. It costs no time, as the turns are shorter, and it is what figures and days gain by.
- **The frame's first question**: the shortest words for the thing, and what tells it from another of its name in a
  field of its own (`which`); "new" for what is added; a note tagged, linked or renamed is a note, not an item. By the
  code, a table's heading row is no entry, and a note's own title and a folder of three letters tell a set apart. The
  frame alone: 37 errors over dev and tuned against the base's 57.
- **Each note of a change to several is marked** yes or no under a grammar (`ProposalReview`). Asked to name the notes
  to drop, the E4B named the ones it meant to keep, and the proposal was left empty. A title that says its folder and a
  name written with its letters split are read for what they are.

The frame and the changes add 6 on the pool to the 7 of the reason, above what one round moves by chance (about 4) but
not by much, and the 14 tasks dev gained are twice that: the question's examples are near dev's tasks. Asking whether
the notes left open are all about one person was tried and left out, as the E4B said yes to two different meetings.

**What is next, read on dev:** of the wrong figures and changes, 24 of 44 on the E4B and 18 of 29 on the 12B are tasks
that pick several notes or lines by a condition ("every report card with a grade under 65", "the unpaid invoices").

**Measured the night of 2026-11-24 and not kept then** (patches in `~/.cache/notekea-loop-runs`, none in the code):

- **Picking by a condition** (a `select` tool; `gemma4-select`): the E4B writes the condition wrong as often as it picks
  wrong.
- **A last look before a change** (`cover-c9`): what else the request could be about, asked when something is. Kept the
  day after, once measured alone (below).
- **Each note of a change judged on its own text**, its mentions in other notes and those like it that were missed
  (`group-g9`). Wrong changes on dev went 24 to 15 over nine versions and the pool did not follow: nine versions on the
  same 393 tasks fit those tasks. At most three versions of an idea are tried on dev before the pool.

**Two bugs found by them, fixed with a test each (2026-11-25):** a right total worked out from copied figures was taken
for a made-up one and the note written again without it; and a card's summary named notes the review had taken out.

**The same on a pool that holds still** (E4B, seed 10, replies kept: section 7), each over the one before:

| | Right, of 439 | Gained | Lost | Should have asked | Change |
|---|---|---|---|---|---|
| Without the two fixes (`gemma4-nofix-pool`) | 335 | | | 28 | 30 |
| With them (`gemma4-fix-pool`) | **336** (76.5 %) | 1 | 0 | 28 | 29 |
| And the group review (`gemma4-g9k-pool`) | 337 | 2 | 1 | 28 | 28 |
| And the last look (`gemma4-c9k-pool`) | 339 | 2 | 0 | 24 | 30 |

Every other cause is level. 328 and 336 are the same code but for one task, so an E4B round's own error is about 8, and
20 of its tasks end another way: a difference of 4 between two rounds without kept replies says nothing.

**The 12B on the pool with the last look and the group review** (`gemma4-12b-pool`): 364 of 439 (82.9 %) against 357,
which is the base of 2026-11-23, before what was kept on the 24th. Missed questions 34 to 25, needless ones 19 to 23,
figures 8 to 3, changes 16 to 18. Dev 323 to 336 of 393.

**Each change on its own, on three still pools** (2026-11-25; the E4B's seeds 10 and 11, the 12B's seed 10; every
version over the base with the two fixes):

| | E4B, seed 10 (of 439) | E4B, seed 11 (of 435) | 12B, seed 10 (of 439) | Gained | Lost |
|---|---|---|---|---|---|
| Base with the two fixes | 336 | 317 | 359 (81.8 %) | | |
| The last look alone (`gemma4-last-e4b`, `-s11`, `-12b`) | 338 | 321 | **363** (82.7 %) | 12 | 2 |
| The group review alone (`gemma4-g9k-pool`, `gemma4-s11g`, `gemma4-12b-kg`) | 337 | 320 | 358 | 5 | 2 |
| Both (`gemma4-c9k-pool`, `gemma4-s11c`, `gemma4-12b-kc`) | 339 | 324 | 362 (82.5 %) | 17 | 4 |

- The E4B's two samples share three vaults; without repeats they are 742 tasks: base 553 (74.5 %), the last look 559
  (75.3 %), both 562 (75.7 %). 594 are its 80 %.
- The last look asks what had to be asked: missed questions 28 to 24, 26 to 21 and 35 to 27. Needless ones rise by 0, 2
  and 2; wrong changes by 2, -1 and 2.
- The group review is level on the 12B and 3 tasks better on the E4B's seed 11, within chance (5 to 2).

**Kept: the last look, on both models** (the developer, 2026-11-25; `OtherTargets`, `docs/agent-frame.md`). The group
review stays out.

**The night of 2026-11-25** (alone, at the developer's word; `docs/agent-frame.md`): of the needless questions, those
the model asked of a question with nothing open were right 0 times of 33, and the sets left open between notes of other
folders and other names were all wrong, on both models. Two changes, each measured on dev, then on the pools that hold
still, then on a sample not seen before (seed 12, which shares 7 of its 10 exams with seeds 10 and 11):

| | Dev (393) | E4B seed 10 | E4B seed 11 | E4B seed 12 | 12B seed 10 |
|---|---|---|---|---|---|
| Base: the last look | 310 | 338 | 321 | 340 | 363 |
| A question not asked back | 316 | 343 | 332 | | 363 |
| And only rivals left open | **318** | **349** | **337** | **351** | **372** (84.7 %) |
| Gained / lost, both | 8 / 0 | 12 / 1 | 17 / 1 | 12 / 1 | 9 / 0 |

The E4B's seeds 10 and 11 without repeats: 559 to 581 of 742 (78.3 %); 13 short of 80 %. Needless questions fell
from 22, 32 and 29 to 6, 12 and 12; figures rose by 3, 2 and 3, tasks now answered that had been asked.

Then two more, on the same five rounds (`DaysTool`; the tally's as a patch, `~/.cache/notekea-loop-runs/tally1.patch`):

| | Dev | E4B seed 10 | E4B seed 11 | E4B seed 12 | 12B seed 10 | Gained / lost on the pools |
|---|---|---|---|---|---|---|
| The days said with today, and when a day of an earlier year next comes round | 321 | 351 | 339 | 356 | 372 | 12 / 3 |
| A tally from a note seen only in a search held once, for it to be read | 325 | 350 | 342 | 355 | 372 | 9 / 8 |

- **Days, kept.** The answer is written from what the tools gave, and the days tool did not say which day today
  was: "days until the cutover" was answered "the notes do not hold today's date". Days fell from 20 to 10 on the
  three E4B samples.
- **The tally, not kept.** Dev's figures fell from 18 to 14 and the pool's did not follow.

**The E2B with all of them** (commit `5c7b79fa`, 2026-11-25): dev 215 to 212 (gained 22, lost 25), its pool seed 10
242 to 255 of 439 (58.1 %). The E2B is not made worse: dev moves within what a round's own noise moves it, and on the
pool should-have-asked fell from 49 to 37 and asked-needlessly from 26 to 23; "other" rose from 22 to 26.

## 7. Rounds made shorter: the model's replies kept

A dev round takes 47 minutes on the E4B, and most of its calls are the ones the round before made: the route and
the frame of every task, and each step up to where a change shows. `ReplyCache` (the evaluation's code, never the
product's) keeps each reply on disk under what decides it: the model's file and its length, the messages, the tokens,
the temperature, the seed and the schema. A call made again is read back; any of these changed asks the model.

`NOTEKEA_REPLIES=<folder> measure.sh <label> <frame|tuned|dev> <size>` turns it on; one folder holds the three sizes,
each filled by its first round. The pool's replies go to `<folder>-pool`, which nobody reads (2026-11-25); the script
never passes it for the reference nor the sealed exams.

What it costs, and so how it is used:

- The same call gives another reply now and then on a GPU (the base measured 281 and 284). A kept reply fixes one of
  the two, so a task on the edge stays where its first round left it. Kept replies say what a change is worth, as two
  rounds then differ only where it shows; they do not say the score itself, which is told by rounds without them.
- It says nothing of the time a task takes.
- The engine's own settings (thinking, the sampler's other values, llama.cpp's version) are not in what a reply is
  kept under: empty the folder when one of them changes.

**Checked on the E2B, 2026-11-24** (dev, 393 tasks, the folder `~/.cache/notekea-loop-runs/replies`):

| Round | Minutes | Replies read from disk | Replies the model wrote | Right |
|---|---|---|---|---|
| The first, which fills it (`gemma4-cache-fill`) | 30 | 2 | 3,340 | 219 |
| The same again (`gemma4-cache-read`) | 3 | 3,342 | 0 | 219 |

The second round asks the model nothing and no task of the 393 ends another way, by cause as well. The 3,342 replies
take 14 MB. The three minutes left are the build, the models loaded and the notes indexed. The E4B and the 12B filled
the same folder the same day (commit `2ef940de`), and it holds 43 MB. This is the 12B's first dev round:

| Dev, 393 tasks | Minutes | Right | Should have asked | Asked needlessly | Days | Figure | Change | Fact | Other |
|---|---|---|---|---|---|---|---|---|---|
| E4B (`gemma4-e4b-fill`) | 44 | 285 (72.5 %) | 17 | 32 | 7 | 21 | 23 | 7 | 1 |
| 12B (`gemma4-12b-fill`) | 161 | 323 (82.2 %) | 23 | 15 | 0 | 14 | 15 | 3 | 0 |

The 12B's largest cause is the reverse of the E4B's: going on when it must ask. 37 of its 70 failures end the wrong
way, 10 never find it and 10 run out of steps.

**A fast set.** Over the E4B's 17 dev rounds, 173 of the 393 tasks were right every time, 31 never and 189 now and
then. `fastset.py <set> <size> <prefix> [cause]` lists the tasks that failed in at least one round (220 on dev), or
only those that ever failed by one cause (asking needlessly 105, changes 47, figures 35), and
`NOTEKEA_TASKS=<file> measure.sh …` measures those alone. It does not see a change that breaks a task that never
failed: like the kept replies, it is for tuning, and the whole dev set and the pool confirm.

**Checked on the pool, 2026-11-25** (E4B, seed 10): filled in 48 minutes (`gemma4-fix-pool`, 3,706 replies), and read
again in 4 (`gemma4-fix-pool2`): no reply asked, the ten exams alike task by task. A changed version takes 4 to 5.

## 8. After the tuning: the study cards

A list of ideas for a local notes app (evidence on cards, structured replies, small steps, retries) was read against
the code on 2026-11-24. Most of what it says of reliability is here already or was measured and left out: grammars on
every structured call, quotes checked against the note, temperature 0 (section 5), and evidence extracted before
answering (`docs/answer-quality.md`, 3.5). The study cards (4.6) are where it helps. Read in the code:

- `CardsPrompt` gives the model the note's first 8,000 characters, so a long note's end makes no cards.
- The sentence a card comes from is checked and then dropped: `CardFormat.blockOf` writes `Question::Answer` alone,
  while `docs/roadmap/M4b.md` says each question cites it.
- A card's figures are not checked; `BackedFigures` checks them in the other written notes.
- A card that fails its checks is left out and never asked for again.
- No evaluation measures the cards.

In this order, each change measured alone and kept only when it beats the cards as they are. No patches here either
(`AGENTS.md`): a bad card's cause is fixed in how cards are made, never by a word list or a case for one note.

1. **A set that measures the cards**, on the dev vaults, by counts: cards kept, backed, right, and the part of the
   note they cover. Without it nothing below is judged.
2. **The making of a card**, one change at a time: the sentence asked for before the question and the answer; a long
   note by its sections; the figures checked; one retry that says what failed.
3. **The evidence kept with the card**, a click that opens it, and a card marked when its sentence is no longer in
   the note. It is kept in a hidden HTML comment on the card, before the schedule's: the sentence itself, so an edit of
   it is seen, and the note's text is not touched. First it is tried with Obsidian's Spaced Repetition plugin, whose
   format the cards keep; should the plugin not take it, the place is asked again.
4. **A card failed again and again written anew** from its sentence, proposed and never changed alone. The failures
   in a row are not kept today, only the ease.

Later, not decided: explaining a note in one's own words and being told what was left out (no mark given), and a
briefing of the day made without the model from what Today, Tasks and the cards due already know. Left out: a note's
gaps found before studying, and cards that cross notes with the web, as a small model makes both up.
