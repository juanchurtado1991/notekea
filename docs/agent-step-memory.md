# A memory of the turn's steps

Asked for by the developer on 2026-11-16 (`ROADMAP.md`, Post-MVP 15), built in the same pull request as the chat's
memory and the web (`feature/chat-memory-web`).

## The problem

What a turn's calls give back is put in the model's prompt as long as it fits. When it does not, `ContextBudget.fit`
drops the oldest results first, and both the next calls and the answer are written from what is left. In the web's
evaluation of 2026-11-16 the 9B read three pages on memorizing, then twelve sections of unrelated notes, and answered
that nothing on memorizing was found: the three pages had been dropped.

## What it does

Every result of a turn is kept outside the prompt, in SQLite, cut into parts (`StepChunks`): its first line (what was
called, on what) and its paragraphs, joined up to 700 characters. When the budget has to drop results, the parts of
the dropped ones that match the task come back, as many as the room left holds, right after the line that says what
was dropped:

```
(The 6 earliest results are no longer shown, …)
(Kept aside from those earlier results, the parts this task needs, in their steps' order:)
From step 1, fetch "https://athenify.io/es/blog/como-memorizar-rapido":
Repetición espaciada: …
<the results that still fit, newest last>
```

The answer is written from the same fit, so it sees those parts too.

**Asked back, not kept in mind.** A small model should not have to remember what it read. Once something is no
longer shown, the line that says so tells it the results are kept, and the loop offers a `recall` tool (only then:
before, there is nothing to ask back). `recall` takes what is needed ("the price of the sensor") and gives back the
parts of the dropped results about it, 1,200 tokens at most, without reading or searching again. The developer
asked for it on 2026-11-16: the memory works when the model can lean on it. The order of the prompt does not change: what
was dropped stays dropped, and the kept-aside parts sit before the newer results.

- **Where:** `SqliteStepMemory`, a full-text table (FTS5) in an in-memory SQLite database, never in a file: it holds
  only what the turn found while it runs, and is forgotten when the turn ends. Bound on the desktop
  (`jvmChatModule`); without it the loop drops what does not fit, as before.
- **Ranked by:** BM25 against the task's words, any of them, a word of more than six letters by its start
  ("memorizar" also matches "memorizing"), without the words that only say what to do ("make", "note", "busca").
- **Not changed:** what the model is told it found, word for word; the old agent's budget.

## Shown less on purpose

The fourth web run (2026-11-16) dropped nothing: every turn's results fit the room, so the memory was never asked
and nothing changed (the loop 212 against 212 on the 9B, 204 against 207 on the 4B). Holding a result in mind
was still the model's load. So the prompt the model picks its next step from now shows only the last three results
whole (`StepRecall.WHOLE_LAST`); each longer one before them is one line, its first, saying it is kept aside and
can be recalled. A short result (under 120 tokens) stays whole: its line would save nothing. The answer is still
written from every result whole. A read made again of a folded result gets it back whole rather than "its result
is above". `-PstepMemory=whole` turns the folding off, to measure one against the other.

## What it does not do

- It does not stop a model from reading unrelated notes: that is a choice, not a lost result.
- Across turns of the same conversation (a correction to a proposal not yet made, "remove Yaxa from that list"): the
  history still keeps only a proposal's summary. The store can hold a conversation's steps by its id; whether it
  helps or brings back old results that confuse the model is to be measured before it is turned on.

## Measured

Against the loop as it was (the web's third measurement, the same day): the web's 50 tasks and the 201 tasks on the
9B and the 4B. `-PstepMemory=off` runs the evaluation without it.
