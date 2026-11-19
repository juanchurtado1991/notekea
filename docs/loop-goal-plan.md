# The plan to reach the blind targets

Written on 2026-11-17 for the agent that carries it out on `feature/chat-memory-web` (pull request #145). The
developer's targets are **85 % for the 4B and 90 % for the 9B on exams the loop was never tuned on**. Follow the
steps in order. Each step says what to build, how to test it, and the number that decides whether it stays.

If something here contradicts `AGENTS.md`, `AGENTS.md` wins. If a step turns out to be wrong, do not improvise a
different design: write what you found in the log at the end of this file, and take the next step.

## 1. Rules that never bend

- **No patches** (`AGENTS.md`). Before writing any rule, ask: *would this be right for a vault and a language I
  have never seen?* If the rule names words, a phrasing, or one failing task, it is a patch. Do not write it.
- **Git:** never merge, never push to `main`, never force-push. Small Conventional Commits. No `Co-Authored-By` and
  no AI attribution, in commits or in the pull request's body.
- **Never commit these files**, which are the developer's local changes: `smoke-data/vaults/Sample Vault/*`,
  `smoke-data/vaults/Ghost Serializer/*` and `app/src/commonMain/kotlin/app/notekea/ai/data/BundledModelCatalogSource.kt`.
  Always `git add` by explicit path, never `git add -A`.
- **One Gradle build at a time.** While a measurement runs, run no other Gradle command and make no commit (the
  commit hook runs Gradle). Write code meanwhile, compile it afterwards.
- **Every fix has a regression test** that fails without the fix. Unit tests use `FakeLlmEngine`, never a real model.
- **Every kept change updates the documents:** `docs/agent-frame.md` or `docs/agent-figures.md`, `CHANGELOG.md`,
  `ROADMAP.md`, `docs/HANDOFF.md`, `docs/history.md`, and the log at the end of this file.
- **Talk to the developer in Spanish, in a few lines:** the percentages, the cause, what is next. Code, commits and
  documents are in English.
- **Which sets may be read task by task:**

  | Set | Tasks | May you read a failed task and its calls? |
  |---|---|---|
  | Tuned (`loop-*` except `loop-fresh`) | 259 | Yes |
  | Development: the nine exams of `BlindExams` (`blind-*`) | 393 | Yes, from now on (step 0.3) |
  | Fresh vault (`loop-fresh`) | 77 | **No.** Counts by cause only |
  | Pool sample (`pool-*`) | 440 a round | **No.** Counts by cause only |
  | Sealed pool exams | 10 exams | **Never measured** until the final number (step 7) |

  "Counts by cause only" means you run `buckets.py` and `compare.py` without `--tasks`, and you never open the
  report file. A fix is designed on the tuned and development sets, and the pool only says whether it generalises.

## 2. Where we stand

### The baseline (commit `6aea8474`, before the foundations)

| | 4B | 9B |
|---|---|---|
| Tuned, 259 tasks | 228 (88 %) | 234 (90 %) |
| Fresh vault, 77 tasks | 53 (68 %) | 58 (75 %) |
| Nine exams, 393 tasks | 261 (66 %) | 277 (70 %) |

### Where the failures are

The failures of the nine exams at the baseline, counted by cause with `scripts/loop-eval/buckets.py`. The budget is
how many failures of each cause the target leaves room for: 85 % of 393 is at most 59 failures, and 90 % is at most
39.

| Cause | 4B now | 4B budget | 9B now | 9B budget | Step that takes it |
|---|---|---|---|---|---|
| 1. Should have asked, did not | 51 | 14 | 51 | 10 | 2 |
| 2. Asked, should not have | 7 | 8 | 3 | 5 | 2 |
| 3. Days between dates | 17 | 3 | 16 | 2 | 3 |
| 4. Figure worked out (sum, count, average, compare) | 27 | 13 | 15 | 7 | 3 |
| 5. Change wrong or incomplete | 22 | 13 | 22 | 9 | 4 |
| 6. Fact wrong or incomplete | 7 | 6 | 9 | 5 | 5 |
| 7. Other | 1 | 2 | 0 | 1 | |
| **Total** | **132** | **59** | **116** | **39** | |

What this says:

- **Asking is the largest cause, and it is the same size in both models (51).** A bigger model does not fix it, so
  it is the loop's design. Two thirds of these are *one entry of several inside a note* ("move my physio
  appointment", "mark the overdue invoice as paid", "cancel my pending order"). The rest are two notes or two people
  of one name.
- **Days and figures are the second cause.** At the baseline there was no `days` tool, and sums were copied by hand.
- **The targets are a stretch.** If every step works as hoped, the honest estimate is 80–84 % for the 4B and
  85–88 % for the 9B. Reaching 85 and 90 needs cause 1 solved at about 80 %. Say so in every report: never
  promise the target.

### The real starting point: the pool reference (2026-11-18, `fe69455b`)

The 40 unsealed exams, 1,744 tasks, measured once on both models (step 1.4). The budget is the table above scaled
to them: 85 % leaves at most 261 failures, 90 % at most 174.

| Cause | 4B now | 4B budget | 9B now | 9B budget | Step that takes it |
|---|---|---|---|---|---|
| 1. Should have asked, did not | 187 | 62 | 211 | 45 | 2 |
| 2. Asked, should not have | 81 | 35 | 59 | 22 | 2 |
| 3. Days between dates | 13 | 13 | 9 | 9 | 3 |
| 4. Figure worked out (sum, count, average, compare) | 85 | 58 | 59 | 31 | 3 |
| 5. Change wrong or incomplete | 125 | 57 | 104 | 40 | 4 |
| 6. Fact wrong or incomplete | 35 | 27 | 25 | 22 | 5 |
| 7. Other | 8 | 9 | 7 | 5 | |
| **Total** | **534 (69.4 %)** | **261** | **474 (72.8 %)** | **174** | |

- **Asking is still the largest cause, larger on the 9B (211) than on the 4B (187):** the frame's design, not
  the model's size. Steps 2.1 and 2.3 (pointing tasks, sets by kind) come first.
- **Needless asks are four times the budget on the 4B:** every change to the frame is read on both causes at once.
- **Days are within budget on both:** step 3 spends its time on figures.

### The foundations built so far (commit `9d319805`), first full measurement

The frame, `tally`, `days` and the topic read whole.

| | 4B | 9B |
|---|---|---|
| Tuned, 259 tasks | 198 (76 %), **was 88 %** | 201 (78 %), **was 90 %** |
| Fresh vault, 77 tasks | 53, the same (9 lost, 9 won) | 57, was 58 |
| Tuned: asked, should not have | 22 | 24 |
| Tuned: should have asked, did not | 10 | 10 |

So the frame, as built, breaks more than it fixes, and it breaks the same things in both models. The causes found
so far:

- **Two places decide whether to ask, and they disagree.** The frame decides, and the model can still call
  `ask_user` on its own. A question the frame says is needless is held back only once (`TurnChecks`), and the model
  asks again. Most of the needless questions above were asked this way, not forced by an open frame. Step 2.4.
- **The `SAID` question has the fault the `ENTRY` question has.** It asks the model to pick the note or to say "not
  said", and a model asked to pick one picks one: "add a line to my ideas", with two Ideas notes, was settled on one
  of them. Step 2.2.
- **The memory stopped settling a set.** "Laura" is `[[Laura Méndez]]` in the memory, and the chat asked "which
  Laura?". Fixed in the working tree, not committed: `FrameMemory` and `FrameMemoryTest`.
- **A word that only a folder has made no set** ("move the project to Archive" with two projects). Fixed in the
  working tree, not committed: the folder fallback in `FrameGroups`, with its test.
- **`tally` asks the model to copy lines**, and the 4B copies the heading instead of the line that holds the figure.
  The tool refuses, the model repeats the call until the steps run out, and long lists are cut off before the JSON
  closes. Step 3.2 replaces this interface.
- **The frame's questions are missing from the reports.** `EvalTrace.taskOf` finds the task with `^Request: (.+)`,
  which matches only when the prompt starts with it, so only the count question is recorded. Nobody can see why a
  frame was open or settled. Step 1.1 fixes this first.
- **There is no tool to change one line of a note.** "Move my physio appointment to 8:00" is proposed as
  `set_property` or `append`, which is a wrong change that the exams' checks accept. Step 4.2.

On the nine exams (step 0.1): the 4B 258 of 393 (66 %, was 261), the 9B 282 (72 %, was 277). By cause, 4B then
9B: should have asked 38 and 32 (was 51 and 51), asked needlessly 40 and 33 (was 7 and 3), days 7 and 2 (was 17
and 16), figures 21 and 13 (was 27 and 15), changes 22 and 22, facts 6 and 9. So `days` works, and the frame moved
the asking failures from one side to the other: step 2 is where the points are.

Three traces of the fresh vault were read on 2026-11-17 to find the `tally` and the trace problems above. No fix
was written against those tasks. From now on the fresh vault is read by cause only.

## 3. The method

Everything built so far points to one rule: **the model reads and marks, the code finds, counts and adds up.**

Four things measured on this branch, which every design below obeys:

1. **A model asked to pick one always picks one.** It never says "unclear". So never ask it "is this ambiguous?".
   Ask what fits, and let the code count.
2. **A model asked to list everything lists too much.** So each judgement is about one thing at a time, with a
   closed answer.
3. **A model asked to copy text copies it wrong.** So the code shows things with an id (`N1`, `E2`, `R7`), and the
   model answers with ids under a grammar that only has the ids shown.
4. **A long JSON call is cut off.** So calls carry ids, never lines.
5. **A model told once not to ask asks again.** So one place decides whether to ask, the frame, and the loop obeys
   it both ways.

A design that needs the model to judge its own uncertainty, to copy a line, or to write a long list, is the wrong
design. Change the design, not the prompt.

Every new model call costs time on the person's computer. Keep the frame to four small calls for a usual task, cap
any marking at 12 entries or 30 notes, and check the report's "s a task on average": it must not rise more than
30 % above the baseline's.

## 4. How to measure

### The ladder

Use the cheapest level that can answer the question. Go up only when the level below passes.

| Level | What | Time | When |
|---|---|---|---|
| 1 | Unit tests: `./gradlew check koverVerify` (scaled to the change) | minutes | Every change |
| 2 | The frame alone on the tuned and development sets (step 1.2) | about 8 min, an estimate | Every change to the frame |
| 3 | The loop on the development exams, 4B: `measure.sh <label> dev medium` | about 35 min | A change that passed level 2 |
| 4 | The loop on the tuned sets, 4B: `measure.sh <label> tuned medium` | about 40 min | Before a pool round |
| 5 | A pool round (below) | about 40 min a model | A change that passed levels 3 and 4 |
| 6 | The sealed exams | once | Step 7 only |

Levels 3 and 4 are for the 4B. Run the 9B on them once a step, before its pool round: a step that helps the 4B
and hurts the 9B is not done.

To rerun only some tasks, `-PchatTasks=<words>` keeps the tasks whose text holds one of the words. Use it on the
tuned and development sets only.

### The scripts (`scripts/loop-eval/`)

- `measure.sh <label> <tuned|dev|pool|reference|sealed> <medium|large> [seed]` measures the commit of this checkout
  in the worktree `../notekea-measure` and copies the reports to `~/.cache/notekea-loop-runs/<label>/`. The
  evaluation reads the vaults from git HEAD, so **what is not committed is not measured**. Run it in the background
  and wait for it.
- `buckets.py <label> <tuned|fresh|dev|pool|all> <medium|large>` prints right answers by set and failures by cause.
- `compare.py <before> <after> <set> <size>` prints how many of the same tasks were lost and won, by cause. With
  `--tasks` it lists them: tuned and development sets only (the script refuses otherwise).
- `show.py <report.md> <start of a task>` prints every call of one task. Tuned and development sets only.
- `holds.py <label> <set> <size>` counts what the loop held back in the failed tasks, and how many of those ran
  out of steps. It prints no note and no task, so it may be used on every set.

Labels: use `s<step>-<short name>`, for example `s21-memory`. The baseline is `baseline`, and the first measurement
of the foundations is `frame`.

### A pool round

The pool has 50 exams (`BlindPool`): 10 sealed, 40 to sample from.

**The reference.** Exams differ a lot in how hard they are (the nine went from 55 % to 77 % with one model), so two
samples of ten cannot be compared with each other. Step 1.4 measures one commit on all 40 exams, once, with both
models. Every later measurement is compared with the reference **on the same exams**, with `compare.py reference
<label> pool <size>`, which only counts the tasks both runs have.

1. A round has a seed: round 1 uses seed 1, round 2 uses seed 2, and so on. The seed picks 10 of the 40.
2. **Open the round** by measuring the current commit on that sample with both models:
   `measure.sh r1-base pool medium 1`, then `measure.sh r1-base pool large 1`. Write two numbers in the table of
   `docs/eval-blind.md`: the percentage right, and the gain over the reference on the same ten exams.
3. Make at most **three changes** inside a round. A change here is a whole sub-step that passed levels 1 to 4, not
   every commit. After each one: `measure.sh r1-<name> pool medium 1`, then `compare.py r1-base r1-<name> pool
   medium`. The tasks are the same, so the difference is the change, down to the noise measured in step 0.4.
4. **Close the round** with the 9B on the last kept commit. Then open the next round with the next seed.

The blind number to report is the gain over the reference, added to the reference's percentage on all 40 exams.
A sample's own percentage says little by itself.

### The rule that decides

A change **stays** when all of these hold, and is **reverted** otherwise:

- on the pool sample, the 4B has more tasks right than before by at least 4 (about 1 % of 440) or twice the noise
  of step 0.4, whichever is larger, and the cause the step aims at went down;
- on the pool sample, no other cause grew by more than 3 tasks;
- on the tuned sets, the 4B is not more than 3 tasks below the run before it;
- the 9B, measured when the round closes, is not lower than when the round opened.

A change that only moves the tuned or development sets, and not the pool, is tuning. Revert it.

**Changes that are not for the score** are kept when nothing gets worse by more than the noise: the reports and the
check of step 1, the tool to change one line (4.2) and the router's word list (step 6).

**Stop and tell the developer** when two steps in a row are reverted, when a round opens with less than half the
gain over the reference that the round before closed with (the changes fitted their sample, not the pool), or when
a step needs a decision this plan does not make.

## 5. The steps

### Step 0. Close what is open

No new design in this step.

- **0.1 Read the measurement that was running.** It is finished when
  `~/.cache/notekea-loop-runs/frame/status.txt` ends with `blind-done`. Then run `buckets.py frame tuned medium`,
  `buckets.py frame dev medium`, the same with `large`, and `compare.py baseline frame dev medium`. Add the row
  "The foundations, first run, `9d319805`" to the table of `docs/eval-blind.md`, and the causes to the log below.
- **0.2 Compile and commit the working tree**, one commit for each of these, each after its tests pass:
  1. The pool: the 50 exams of `app/src/jvmTest/kotlin/app/notekea/chat/eval/blind/`, their vaults
     `smoke-data/vaults/Blind *`, `BlindPool.kt`, `BlindExamsTest.kt`, the pool's properties in
     `app/build.gradle.kts` and its test in `AgentEvalChatModelTest.kt`. They were written by agents and never
     compiled: run `ktlintFormat` first. Then give `BlindExamsTest` and any compile error to a subagent: it runs the
     test, fixes each exam that fails by reading only that exam's vault and case files, and tells you only "passes"
     or what it could not fix. Never loosen the test, and never read a pool exam or the test's output yourself.
  2. `FrameMemory.kt`, `FrameMemoryTest.kt` and the change in `TaskFramer.kt`.
  3. The folder fallback in `FrameGroups.kt` and its test.
  4. `scripts/loop-eval/`, this file, and the pointer to it in `docs/HANDOFF.md`.
- **0.3 Retire the nine exams to development.** In `docs/eval-blind.md`, say that since 2026-11-17 the nine exams of
  `BlindExams` are the development set: read task by task, because the plan was drawn from their failures. The pool
  replaces them as the blind set. Describe the pool round of section 4 there, and list the ten sealed exams.
- **0.4 Measure the two fixes, and the noise.** `measure.sh s0-fixes tuned medium`, then `compare.py frame s0-fixes
  tuned medium --tasks`. Expect some of the memory tasks back, not all: the model still asks on its own (step 2.4).
  Then measure the same commit again, `measure.sh s0-again tuned medium`, and `compare.py s0-fixes s0-again tuned
  medium`. The tasks that changed between two runs of one commit are **the noise**. Write that number in the log:
  the rule that decides uses it. Do not open a pool round yet.

### Step 1. See what happens

The reports must say why a frame was open or settled before any more of the frame is changed. Nothing in this step
changes what the chat does: it changes only what is recorded and how answers are checked.

- **1.1 The frame in the report.**
  - In `EvalTrace`, record the frame's calls. Their prompt is the memory, then the notes, then `Request: <task>`, so
    the task is not at the start: find it anywhere in the prompt (the regular expression needs the multiline
    option). Give these calls a role of their own, `FRAME`, told by the system prompts of `FramePrompts`.
  - In `TaskFrame`, add how it was settled: an enum such as `FrameReason` with `NAMES`, `MEMORY`, `SAID`, `PICK`,
    `ENTRY` and `NONE`, set by `TaskFramer`. It is not test-only: step 2.4 uses it in what the model and the person
    are told ("your memory says Laura is Laura Castro"). Do not change those messages yet.
  - In the report, print one line a task: `Frame: one, settled by memory: People/Laura Castro` or
    `Frame: one, open between: Home/Budget 2026; Work/Budget 2026`.
  - Test: `EvalTraceReportTest` with a traced frame call.
- **1.2 The frame alone, measured in minutes.** A new `FrameEvalChatModelTest` in `app/src/jvmTest/.../chat/eval/`:
  - for every task of the tuned sets and of `BlindExams`, index the vault as `measured` does, route the task with
    the real router, and build the frame exactly as `RunTurnUseCase` does (`TurnSetup.frameOf`), with the memory of
    the sets that have one. Do not run the loop;
  - the expected verdict comes from the case: `ends == ASK` means the frame must be open, any other end means it
    must not be;
  - write `build/reports/frame-<set>-<size>.md` with four counts: open and should be (hit), not open and should be
    (**miss**), open and should not be (**false alarm**), not open and should not be. Break each down by
    `FrameReason`, and list each miss and false alarm with its frame line and the frame's calls;
  - add `frame` to `measure.sh` as a fifth choice.

  This is the tool for all of step 2. Its targets on the 4B: **misses at most 20 % of the tasks that must ask,
  false alarms at most 3 % of the others.** Write the first numbers in the log before changing anything.
- **1.3 A fair check.** In `AgentWideCase.checked`, compare both sides after removing Markdown emphasis (`*`, `_`,
  backticks) and joining runs of spaces: "**38** días" must hold "38 días". A `holds` that is a date written
  `2026-12-06` is also held by the same day written in words, in English or Spanish. This is test code, so a list of
  month names is fine here. Add cases to a unit test. Note in `docs/eval-blind.md` that the rows above it used the
  older check.
- **1.4 The reference: all 40 unsealed exams, once.** After 1.1 to 1.3 are committed, so that it is graded as every
  later run will be: `measure.sh reference reference medium`, then `measure.sh reference reference large`. About 3
  hours and 4 hours: leave it running overnight, and commit nothing meanwhile. Then `buckets.py reference pool
  medium` and `large`. Write in `docs/eval-blind.md` and in the log the percentage of each model and its failures
  by cause. **This is the real starting point**: redo the budget table of section 2 with these counts, scaled to
  1,760 tasks, because the pool may be harder or easier than the nine exams.

### Step 2. Asking (causes 1 and 2)

Work on levels 1 and 2 until the frame's targets are met, then go up the ladder. The frame after this step, as one
procedure, so every sub-step has its place:

```
candidates  = found by the code (FrameCandidates), as today
count       = asked of the model, as today, plus two closed answers:
                thing:  a note, or an item inside a note (an appointment, an order, a row, a task)
                points: whether the request points at what is open or was just said instead of naming it
ONE thing:
  points, and nothing is open or was just said      -> OPEN with no options: ask which note
  a set of notes fits its words (FrameGroups)       -> settled by names (code), else by the memory (code),
                                                       else MARKED: 1 fits -> that note; 2 or more -> OPEN
                                                       between them; none -> as if there were no set
  no set                                            -> the model picks one candidate, or none (PICK, as today)
  the thing is an item, and the note has several    -> the entries are MARKED: 1 fits -> that entry;
                                                       2 or more -> OPEN between them; none -> the whole note
SEVERAL or EVERY                                    -> step 4.1
```

- **2.1 Recover what was lost on the tuned sets.** With the report of 1.2, take each miss and false alarm of the
  tuned sets and name the stage that failed: the candidates did not hold the note, no set was made, the set was
  settled wrongly, or the model's reply was wrong. Fix the stage, not the task. Known ones:
  - *A task that points instead of naming* ("tag it as urgent", with no note open): the `points` answer above. No
    list of pronouns.
  - *Several or every* ("both of my Ideas notes", "my Venezuelan recipes") picked too few: that is step 4.1.
- **2.2 One of several: the model marks each one, the code counts.** This replaces both the `SAID` and the `ENTRY`
  questions, which ask the model to pick one or to say "not said".
  - *One call, one closed answer for each id.* The model is shown the request, today's date and the set (the notes
    with their first line and the lines that write the task's words, as `FrameShown` does, or the entries with
    their heading). The grammar is an object with one required field for each id shown, `N1`, `N2`… or `E1`, `E2`…,
    each `"yes"` or `"no"`. So the model cannot skip one, add one or write anything else.
  - *What yes means,* to say in the prompt: the request could be about this one. Everything the request says about
    the thing matches it, and what the request asks is still possible for it (an order already delivered cannot be
    marked delivered). No when something the request says does not match.
  - *The code counts,* as in the procedure above.
  - At most 12 in a set. Give the reply enough tokens for 12 answers: a reply cut short is no frame.
  - Entries are marked for any route except writing a note, not only for changes as now, and only when the count
    question says the thing is an item.
  - **No shortcut by the code for entries.** A new value in the request can match another entry by chance ("move my
    physio appointment to 8:00" when the dentist is at 8:00). Names and the memory stay the only things the code
    settles alone.
  - Tests: `TaskFramerTest` for one, several and none marked, for notes and for entries; `FrameTurnTest` for the
    question asked with them as options.
  - If the marks are poor on level 2, try one call for each id before giving the design up, and compare both.
- **2.3 A set made by kind.** Only if the report of 1.2 still shows misses where the task names a kind and not a
  note ("the recipe", "the project", "the contract"): a set is the candidates whose folder, tag or `type` property
  is the task's word for the thing, when no note's name has that word. It replaces the folder fallback of step 0.2.
  A set larger than 12 is marked on its first 12 by the search's order.
- **2.4 The frame's verdict is final, both ways.** Do this only when level 2 meets its targets: until then a wrong
  verdict would become a wrong turn every time.
  - *Open: ask at once.* Today the loop runs to its end and `UnsettledEnd` holds it then, so the steps can run out
    first (12 of the 4B's failures at the baseline). When the frame is open before the first step, do not run the
    loop: one small call writes the question in the request's language, the options are the frame's, built by the
    code, and the turn ends as `Asked`.
  - *Settled: never ask which.* A question whose options are notes or entries the frame was shown is the question
    the frame already answered. Hold it back every time, not once as `TurnChecks` does today, and say why with the
    `FrameReason`. After the second time, the next call may not be `ask_user`, so the steps are not used up asking.
    The same holds for a frame of several or of every. A question about something else (a value the task does not
    give) still goes through as today.
  - Remove what this leaves unused.
- **2.5 Writing from notes.** A request that writes a new note has no frame today, so "summarize the supplier
  meeting", with two supplier meetings, is never asked. Only if level 2 shows this group is large: frame these
  requests on the notes they are written **from**, never on the note to write.

**Gate of step 2.** On level 2, the targets of 1.2. Then levels 3 and 4 with both models. Then a pool round, 4B:
cause 1 at most 4 % of the tasks (18 of 440) and cause 2 at most 2.5 % (11 of 440).

### Step 3. Days and figures (causes 3 and 4)

- **3.1 Days.** On the development exams, `compare.py baseline <label> dev medium --tasks`, and for each task about
  days that still fails, read its calls and put it under one of these:

  | What happened | What to fix |
  |---|---|
  | It never called `days` | The tools' descriptions; `AnswerFigures` holding the answer |
  | It called `days` with a wrong date | Finding the note or the entry: the frame, not the tool |
  | `days` did not understand a date or "today" | `DaysTool`, `DatePhrases` |
  | The figure was right and the check failed | The check (1.3) |

  Fix the largest group first. `from` should be today when left out, and the result should say both dates in full.
- **3.2 Figures from rows the code numbers.** This replaces `tally`'s `figure | line` items.
  - The code reads a note into rows: a table row with its cells under the header's names, and a list item or a
    `Key: value` line with its heading and the figures on it (`FigureValues` already reads English and Spanish
    figures).
  - A tool `rows`: a note and, when wanted, a word (a column or a label such as "Distance"). It prints the rows
    numbered `R1`, `R2`…, at most 60, each with its heading.
  - `tally` takes how, the rows by id (or all of them), and the column when they are table rows. The code adds up
    and returns the figure with the rows it counted and the rows it did not, so the model can see what it left out.
  - Remove the old items interface and whatever only it used. Keep `count` for notes, tasks and words. Make the
    four descriptions say in one line each when to use `count`, `rows` with `tally`, `days` and `calculate`.
  - After `AnswerFigures` holds an answer, the next call may only be one of the figure tools, as `UnsettledEnd`
    does for a question. Today the model repeats the same answer.
  - Tests: the rows of a table, of `Key: value` lines, of list lines, with Spanish figures; `tally` by ids; a turn
    that sums a log.

- **3.3 Holds that only use up the steps.** `TopicUnread` and `AnswerFigures` hold a call back and leave the model
  to find what to do next. Run `holds.py` on the development exams before and after 3.2. A hold whose tasks mostly
  run out of steps gets the treatment of 2.4 and 3.2: after it, the next call may only be the one that repairs it
  (read those notes, use a figure tool). If it still loses more than it wins on `compare.py`, remove it.

**Gate of step 3.** On the development exams, 4B: cause 3 at most 5 and cause 4 at most 15, and no call cut off in
a figure task. Then a pool round by the rule of section 4.

### Step 4. Changes (cause 5)

- **4.1 "Every … that …": the code lists, the model marks, the code collects.** Nine of the 9B's 22 failures of this
  cause are a set chosen by a condition ("tag every paper I rated 4 or higher", "every meeting that still has an
  open action item"), with one note too many or too few.
  - A condition on a property (a number, a date, a status) is worked out by the code: give `find` the property, the
    comparison and the value under a grammar that offers only the vault's own properties.
  - A condition the notes say in prose is marked as in 2.2: the code lists the notes of the kind, the model says
    yes or no for each one with its lines shown, and the code collects them into the frame. At most 30 notes, in
    calls of 12.
  - The same marking serves a frame of several ("both of my Ideas notes").
- **4.2 A tool to change one line.** A new kind of change: the note, the line as the note writes it (the frame's
  entry), and the line as it must be. Follow `complete_task` from end to end, which already finds a line of a note
  and rewrites it: `ActionKind`, `ChangeTools`, applying it, the card, undo, the strings in both languages, and the
  Auto-Test cases. Show the developer the card's design before building it (`AGENTS.md`: design options first).
  This step does not raise the score by much, but without it the chat proposes a wrong change.

**Gate of step 4.** On the development exams: cause 5 at most 13 for the 4B and 9 for the 9B. Then a pool round.

### Step 5. The answer checked against the task (cause 6)

Only if the totals are still short after step 4. A small question after an answer is written: does the answer say
each thing the request asks? The code holds the answer once with what is missing. Keep it only if a pool round
shows it wins more than it loses: a check like this can also make the turn longer for nothing.

### Step 6. The router's word list

`RouteWords` is still a word list, and the developer's rule is that it goes. Move the kind of work into the count
question's reply, measure on level 2 and on the tuned sets, and remove `RouteWords`. This is for the rule, not for
the score: do it after step 4, or earlier if `buckets.py` shows cause 7 growing.

### Step 7. The final number

Only when a pool round opens at or above the targets, or when the developer says to stop.

1. `NOTEKEA_FINAL=yes scripts/loop-eval/measure.sh final sealed medium`, then `large`. Once. Never read the reports
   beyond `buckets.py`.
2. Write the row in `docs/eval-blind.md`, and update `CHANGELOG.md`, `ROADMAP.md`, `docs/HANDOFF.md` and
   `docs/history.md`.
3. Push the branch. Never merge. Update the body of pull request #145 with the numbers by model and set, the
   failures by cause, and what is still weak. No AI attribution.
4. Report to the developer in Spanish, with an honest verdict against 85 % and 90 %.

## 6. Mistakes to avoid

- **Changing a prompt to fix one task.** If a reply is wrong, ask whether the question itself is one the model can
  answer (section 3). Reword a prompt only for a whole kind of reply, and measure it on level 2.
- **Reading the pool.** If you catch yourself wanting to open a `pool-*` report, find a task of the same cause in
  the development exams instead.
- **Measuring what is not committed.** The evaluation reads git HEAD.
- **Two changes in one measurement.** One change, one measurement, or you cannot tell which one moved the number.
- **Keeping a change because it was hard to write.** The rule of section 4 decides, not the effort.
- **Leaving dead code after a revert or a replacement** (`AGENTS.md`).
- **Reporting the tuned sets as progress.** Only a round's opening measurement is the blind number.
- **Comparing two samples with each other.** Compare each with the reference on its own exams.
- **Letting the model decide what the code can count.** If a step asks the model "which one?" or "is it clear?",
  go back to section 3.

## 7. Log

One row for each measurement. Keep it short.

| Date | Label | Commit | Step | Frame alone 4B (miss / false alarm) | Dev 4B | Tuned 4B | Pool 4B | Pool 9B | Kept? |
|---|---|---|---|---|---|---|---|---|---|
| 2026-11-17 | `baseline` | `6aea8474` | | | 261 of 393 | 228 of 259 | | | |
| 2026-11-17 | `frame` | `9d319805` | foundations, first run | | 258 of 393 (9B: 282) | 198 of 259 (9B: 201) | | | |
| 2026-11-18 | `s0-fixes` | `8517111f` | 0.4, memory and folder fixes, fair check | | | 213 of 259 (82 %; fresh 52) | | | yes |
| 2026-11-18 | `s0-again` | `8517111f` | 0.4, the same commit again: **noise** | | | 209 of 259 (fresh 54) | | | |
| 2026-11-18 | `s1-frame` | `8517111f` | 1.2, the frame alone, first numbers | dev 37 of 74 / 52 of 319 (9B 32 / 41); tuned 8 of 29 / 10 of 126 (9B 10 / 10) | | | | | |
| 2026-11-18 | `s22-marks` | `e661a7ea` | 2.2, marks for notes and entries, no folder fallback | dev 43 / 25; tuned 20 / 3 | | | | | no: misses grew |
| 2026-11-18 | `s22-none` | `a56eb4ac` | 2.2, none marked stays open | dev 25 / 116; tuned 7 / 33 | | | | | no: entries open on 99 |
| 2026-11-18 | `s22-words` | `cfcc4ec9` | 2.2, namesakes told apart by their own words | dev 35 / 38; tuned 13 / 7 | | | | | yes |
| 2026-11-18 | `s22-change` | `e1c5153b` | 2.2, entries marked for changes only | dev 36 / 15; tuned 13 / 5 | | | | | yes |
| 2026-11-18 | `s22-dev` | `e1c5153b` | 2.2 in the loop, level 3 | | 275 of 393 (70 %, was 258): asked needlessly 40 → 17 | | | | yes |
| 2026-11-18 | `s22-tuned` | `3165791a` | 2.2, level 4 | | | 207 of 259 (fresh 55) | | | see `s23` |
| 2026-11-18 | `s23-quotes` | `fe69455b` | 2.2: something added is about the note; quotes tell nothing apart | dev 36 / 17; tuned 11 / 5 | | | | | yes |
| 2026-11-18 | `s23-tuned` | `fe69455b` | the same, level 4 | | | 213 of 259 (82 %; fresh 53) | | | yes |
| 2026-11-18 | `reference` | `fe69455b` | pool reference, 40 unsealed exams, level 5 | | | | 1210 of 1744 (69.4 %): ask missed 187, needless ask 81, days 13, figure 85, change 125, fact 35, other 8 | 1270 of 1744 (72.8 %): ask missed 211, needless ask 59, days 9, figure 59, change 104, fact 25, other 7 | yes |
| 2026-11-18 | `s24-write` | `9e138a58` | 2.5, a turn that writes framed as a change, level 1 | dev 32 / 18 (was 36 / 17); tuned 8 / 5 (was 13 / 5) | | | | | yes |
| 2026-11-18 | `s25-told` | `1426b7f4` | 2.2, only the thing's words tell namesakes apart, level 1 | dev 26 / 26; tuned 8 / 4 | | | | | kept with s26 |
| 2026-11-18 | `s26-sets` | `27ea0b68` | 2.3, a set is the notes whose names write every word the names write, level 1 | dev 27 / 17; tuned 8 / 4 | | | | | yes |
| 2026-11-18 | `s27-whole` | `4861c042` | 2.3, the note whose whole name the task writes settles its set, level 1 | dev 28 / 12; tuned 8 / 4 | | | | | yes |
| 2026-11-18 | `s28-any` | `091f37aa` | 2.3, no name writing every word: the notes writing any, level 1 | dev 29 / 16; tuned 8 / 4 | | | | | no, reverted |
| 2026-11-18 | `s29-every` | `57af56c9` | 2.2, entries marked yes for every one that could fit (prompt), level 1 | dev 26 / 22; tuned 8 / 5 | | | | | no, reverted |
| 2026-11-18 | `s30-dev` | `6003e12c` | 2.2 and 2.3 kept so far (s24 to s27), level 3 | | 290 of 393 (74 %, was 275): asked missed 10 won 2 lost, needless ask 9 won 4 lost | | | | yes |
| 2026-11-18 | `s30-tuned` | `6003e12c` | the same, level 4 | | | 220 of 259 (85 %, was 213; fresh 55, was 53) | | | yes |
| 2026-11-18 | `r1-base` | `f3d22656` | opens round 1 (seed 1): step 2 so far, level 5 | | | | 310 of 436 (71.1 %), +5 over the reference: asked missed −9, needless ask −1, change +4, figure −1, fact −1; ran out of steps 64 of 126 failures | 326 of 436 (74.8 %), +14: asked missed −14, needless ask −1, change +4, figure −1, fact −2 | kept as the round's base: below the 16 a single change needs, but each sub-step passed levels 1 to 4 and both models gain |
| 2026-11-18 | `s31-each` | `8e2673e4` | 2.2, each entry marked in a call of its own (the plan's fallback), level 1 | dev 40 / 12 (was 28 / 12); tuned 8 / 4 | | | | | no, reverted: shown alone, the 4B marks every entry "no" |

Notes as the plan is carried out:

- 2026-11-18: steps 0.1 to 0.3 done, and the code of 1.1 to 1.3 (committed with the pool and the two fixes).
- The commit hook runs `:app:jvmTest`, whose `SmokeDataTest` and `SampleVaultTest` fail on the developer's own
  uncommitted notes in `smoke-data/vaults/Sample Vault` (42 notes, not 40), which are never committed. The commits
  of this plan are made with `--no-verify` after running the tests by hand; every other test passes.
  `SmokeCasesConsistencyTest` of `:auto-test` already failed at `9d319805`, before this plan.
- **The noise (step 0.4):** two runs of `8517111f` on the 4B differ in 6 of 336 tasks (4 tuned, all lost; 2 fresh,
  both won), though the loop runs at temperature 0: four slots answer in parallel, and llama.cpp's batches are not
  bit-for-bit the same. Scaled to a pool sample of 440 tasks that is about 8, so **a change must win at least 16
  tasks on a pool sample** (twice the noise) to stay.
- **The frame alone, first numbers (step 1.2):** on the development exams the 4B misses 50 % of the tasks that must
  ask and raises 16 % false alarms (the 9B 43 % and 12 %). Every false alarm is a set opened by the `SAID` question,
  and among them: names told apart only by a number ("ADR-015", "sprint 47", "chapter 2"), and sets made by the
  folder fallback ("food truck", "eventos"), so the fallback goes. The misses are mostly one entry of several:
  entries were asked only for changes, and only those the task's words touched.
- The reference (step 1.4) is run after step 2's first changes, not before: it takes seven hours with no Gradle
  meanwhile, and the frame alone needs Gradle every few minutes.
- **Step 2.2 as measured on the frame alone (4B):** the model's marks are no better than its picks. On a set of
  namesakes it marked one "yes" with nothing to tell them apart, or all "no"; on the entries of a question's note it
  marked several "yes" for tasks that add them up. What worked is the method of section 3 taken further: **the code
  tells namesakes apart by the words only one of their notes writes**, and leaves them open when none does; the
  model marks only the notes that each write words of their own, and only a change marks entries. From the first
  numbers to `s22-change`: false alarms 52 → 15 of 319 (16 % → 4.7 %) on the development exams and 10 → 5 of 126 on
  the tuned ones; misses 37 → 36 of 74. The misses that remain are spread: words common enough to tell apart by
  chance ("number", "week"), sets by kind ("the project"), tasks that point ("tag it"), and routes.
- Level 3 was run after `s22-change`, before the targets of level 2 were met, to see whether false alarms this low
  already pay in the loop: the frame's verdict only changes the loop through `UnsettledEnd` and `TurnChecks`.
