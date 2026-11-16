# The chat's memory between conversations — design

Asked about by the developer on 2026-11-15: "would it not be useful for the agent to know the person?". The loop's
design left it for later (`docs/agent-loop.md`, "Skills and the vault's instructions"): what it may keep, and how the
person sees and deletes it, needed its own decision. This is that decision, to agree on **before** anything is built.
It builds on the one agent loop (`feature/one-agent-loop`) and is not started until that branch is merged.

## Why

The loop already has two memories: the conversation (`Conversation`, a chat's turns and what they did) and the
vault's instructions (`Notekea/Notekea.md`, written by the person and always read). Neither carries what a chat learns into
the next one. The evaluation shows where that costs most:

- **The same question, every chat.** A task that can mean several notes is asked back (`AmbiguousTask`,
  `UnsettledEnd`): "Laura" with two Lauras, "the budget" with two budgets. That is right the first time, but the
  person who always means Laura Castro is asked again in every chat.
- **The same correction, every chat.** "No, the work budget", "put recipes in Cocina", "answer in Spanish": said
  once, forgotten when the chat closes.
- **The detector's misses.** On the 24 unseen tasks the 9B's misses were "my goals" and "the presupuesto", which the
  detector does not hold (`docs/agent-loop.md`, "On tasks it was not written against"). A line that says which note
  the person means settles those without a better detector.

## What it becomes

**One note of the vault, written only with the person's approval, read in every chat.** The vault's instructions
are what the person tells the chat; the memory is what the chat has learnt, and asked to keep. It is plain Markdown,
so the person sees it, edits it, deletes it and syncs it as any other note, and nothing about them lives outside
their vault.

```markdown
## Who and what I mean
- "Laura" is [[Laura Castro]]
- "the budget" is [[Budget 2027 (work)]]

## Where things go
- Recipes go in `Cocina/`

## How I like answers
- In Spanish, short, with the figures
```

- **Its place:** `Notekea/Memory.md`, beside the vault's instructions in the same folder (decided by the developer
  on 2026-11-15: "que se guarde en un folder Notekea/: Notekea.md, Memory.md"). The instructions move there from the
  vault's root, in the loop's branch, so what Notekea reads of the vault is in one folder the person can find.
  A note of its own and not a section of `Notekea.md`, so what the person wrote and what the chat proposed are
  never mixed, and the memory can be cleared without touching the instructions.

```
My vault/
├── Notekea/
│   ├── Notekea.md   what the person tells the chat
│   └── Memory.md    what the chat learnt, and the person approved
├── Cocina/
└── Trabajo/
```
- **Its sections:** three, by what each line is for. Lines are short, one fact each. A note is named by its
  wikilink, so a rename or a move of the note updates the memory as it updates every link (renames update links without asking, `docs/roadmap/M1.md`).

### What it keeps, and what it never keeps

| Kept | Never kept |
|---|---|
| Which note a name, a nickname or "the X" means | What a note says: it is already in the vault, and goes stale in a copy |
| Where a kind of note goes (folder, tags) | Anything from untrusted text: a web page, a clipped source (`docs/agent-loop.md`, rule 6) |
| How the person likes answers (language, length, form) | Passwords, keys, account or card numbers, health or money facts about the person |
| A correction the person made in the chat | What the model guesses about the person (mood, habits, who they are) |
| | A detail of one task ("this week's menu") |

The left column is what makes a later task go better; the right is what would be wrong, stale or not ours to keep.
`MemoryLine` checks a proposed line against the right column with no model call (a link to a note that exists, no
number that looks like an account, no text from an untrusted result) and refuses the rest.

### How a line is written

**Never by the model alone.** A 4B model that writes "Laura is Laura Méndez" once makes the mistake in every chat
after, and the verifier would take it as settled. So a line is proposed, shown and approved, in three ways only:

1. **After a question back is answered** (the most common, and the clearest signal). The person picks "Laura Castro"
   from the buttons; with the answer, the chat offers one more button: **Remember: "Laura" is Laura Castro**. It is
   built by the app from the question and the button chosen, not written by the model. One click adds the line.
2. **When the person asks**: "remember that recipes go in Cocina", "recuerda que…". A `remember(line, section)` tool,
   in every toolset, adds the line to the proposal; it shows on the card as every change does, and the card's Do it
   writes it.
3. **When the person corrects the chat**: "no, I meant the work budget". The loop may offer the same Remember button,
   with the line built from the note it was sent to and the one it was corrected to.

All three end as an `append` to the memory note, through `VaultActionRunner`, so they are in the journal and Undo
undoes them. `forget(line)` takes a line out the same way, on the card. The model never proposes a memory on its own
initiative in this first version; whether it should is measured later (open question 3).

### How it is read

- **Not all of it, every time.** The memory can grow; the loop's prompt cannot (8,192 tokens at most,
  `ModelProfile`). The section "How I like answers" is always read; the lines of the other two are read when a word
  of the task matches them (the quoted name, the folder, the note's title), found with no model call
  (`MemoryLines.relevantTo(task)`). At most 1,500 characters reach the prompt, after the vault's instructions, under
  their own heading ("What the person asked you to remember").
- **The verifier uses it too.** `AmbiguousTask` reads the "Who and what I mean" lines: a name the memory settles is
  not held, and the loop is told which note it means. This is where the gain is measured.
- **It is shown when it is used.** A turn that used a memory line says so in its steps ("From your memory: 'Laura' is
  Laura Castro"), with a link to the line. A wrong memory is seen the first time it matters and corrected in one
  place, which is the answer to the risk of a mistake repeated in every chat.
- **A line that no longer holds is skipped**, said in the step: a wikilink to a note that is gone, or a folder that is
  not there.

### How the person sees it and turns it off

- **Settings → General → Chat memory**: on or off, and **Open memory**, which opens the note in a
  tab. Off means nothing is read and no Remember is offered; the note is left as it is.
- **In the chat**: "what do you remember about me?" answers from the note with its lines; "forget that" proposes
  taking the line out.
- **The guide** (`composeResources/files/guide/guide.md`) says what it keeps, what it never keeps and where it is.

### The small model

The 2B keeps today's agent (`ChatLoop.isOn`), which has no memory. Its first version gives the 2B nothing; whether
today's agent should read the "How I like answers" section is left to the measure.

## How it is measured

The evaluation decides, as for the loop. A memory note is added to the `Agent Eval` vault, and three kinds of task
to `AgentUnseenCases` and `AgentWideCases`:

1. **A memory that settles the task**: the ambiguous tasks (the two Lauras, the two budgets, the two supplier
   meetings, "my goals", "the presupuesto") with a line that says which one. They should not ask, and should act on
   the note the line names. Today they ask, or pick one at random.
2. **A memory that does not apply**: the tasks that should not ask, with lines about other names. Nothing should
   change.
3. **A memory that is wrong**: a line that names the wrong Laura. The turn will follow it (that is what a memory is
   for); what is graded is that the step shows the line used.

The gates: none of the 201 tasks falls with a memory of 20 lines in the vault (its tokens and its distraction); the
ambiguous tasks with a memory that settles them go from asking to right; the time a task does not grow by more than
the lines read. Without the model, `MemoryLineTest` (what is refused), `MemoryLinesTest` (what is relevant to a
task) and `AmbiguousTaskTest` (a settled name is not held).

## The steps

Each its own set of commits, measured before the next, on a branch from `main` once the loop is merged.

1. **The note read.** `VaultGuide.memoryOf`, `MemoryLines.relevantTo`, the prompt's heading, and `AmbiguousTask`
   honouring a settled name; the memory note added to the `Agent Eval` vault, the three kinds of task measured.
2. **Remember after a question back.** The button built from the question and the answer, the line appended through
   the runner, Undo.
3. **`remember` and `forget` as tools**, on the card, with `MemoryLine`'s checks; "what do you remember about me?".
4. **Settings and the guide.** Chat memory on or off, Open memory; the guide's section; Auto-Test cases
   and `MVP_SMOKE_TEST.md`.

## Risks, said plainly

- **A wrong line repeated.** The answer is that no line is written without approval, and every line used is shown.
  It does not make a wrong approval impossible.
- **The prompt grows.** 1,500 characters at most, read only when relevant; measured against the 201 tasks.
- **It is in the vault, so it syncs.** It goes wherever the vault's Git remote goes. That is the promise (the
  person's files, under their control), but the guide says it plainly.
- **Names change meaning.** A second Laura Castro appears, or the person starts to mean the other budget. The line is
  shown each time it is used, and corrected or forgotten in one step.

## For the developer to decide

1. ~~A note of its own, or a section of `Notekea.md`?~~ Decided (2026-11-15): a note of its own, `Notekea/Memory.md`.
2. **On by default** (recommended: nothing is written without a click, and only relevant lines are read), or off
   until the person turns it on?
3. **May the model propose a line on its own** (for example after a correction it understood), always as a card? Not
   in the first version; measured after step 3.
4. **The sections' headings in the vault's language**, or always in English, which the loop's prompt is in?
