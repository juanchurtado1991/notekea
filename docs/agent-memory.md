# The chat's memory between conversations — design

The developer asked about it on 2026-11-15: "would it not be useful for the agent to know the person?". The loop's
design had left it for later (`docs/agent-loop.md`, "Skills and the vault's instructions"): what the memory may
keep, and how the person sees and deletes it, needed a decision of its own. This document is that decision.

- **The first version** was reviewed with the developer the same day. They chose the place, accepted every
  recommendation, and asked for three more things:
  - how the agent knows the person's region;
  - an easy way to set it all up;
  - protection against false or bad memories.
- **This second version** answers those three. It also gives the memory uses beyond the web.
- **Where it goes:** it is the first half of the pull request after the one agent loop. The web search
  (`docs/agent-web.md`) is the second half and leans on it. It is built on a branch from `main` once
  `feature/one-agent-loop` is merged.

## Why

The loop has two memories already:

- **The conversation** (`Conversation`): one chat per vault, its turns and what they did.
- **The vault's instructions** (`Notekea/Notekea.md`): written by the person, always read.

Neither carries what a chat learns into the next one. The evaluation shows where that costs most:

- **The same question, every chat.** A task that can mean several notes is asked back (`AmbiguousTask`,
  `UnsettledEnd`). That is right the first time, but the person who always means Laura Castro is asked again in
  every chat.
- **The same correction, every chat.** "No, the work budget", "put recipes in Cocina", "answer in Spanish" are
  said once and forgotten when the chat closes.
- **The detector's misses.** On the 24 unseen tasks the 9B missed "my goals" and "the presupuesto"
  (`docs/agent-loop.md`, "On tasks it was not written against"). A line that says which note the person means by
  those words settles them without a better detector.

## What it becomes

**One note of the vault, written only with the person's approval, and read in every chat.** It is plain Markdown,
so the person can see it, edit it, delete it and sync it like any other note. Nothing about them lives outside
their vault.

```
My vault/
├── Notekea/
│   ├── Notekea.md   what the person tells the chat (only the person writes it)
│   ├── Memory.md    what the chat learnt and the person approved (only the chat writes it, after a click)
│   └── Skills/      the person's own skills
├── Cocina/
└── Trabajo/
```

- **The place** was decided by the developer on 2026-11-15: "que se guarde en un folder Notekea/: Notekea.md,
  Memory.md". The instructions moved there in the loop's branch.
  - The folder is made the first time the person approves a line, or creates their instructions from Settings. A
    vault whose person never uses either gets no new folder.
  - `Skills/` moves under it as well, so everything Notekea reads of the vault is in one place.
- **Who writes which note.** The person writes `Notekea.md`, by hand or through the simple fields in Settings
  (below). The chat writes only `Memory.md`, and only after a click. So it is always clear who wrote what, and the
  memory can be cleared without touching the instructions.
- **Instructions win.** When a line of the memory and the instructions disagree, the instructions win, and the
  memory's line is offered for tidying (below).
- **One memory per vault.** It lives in the vault. Facts about the person (their country, their currency) are said
  once in each vault, which is the price of everything staying theirs and visible.

### The sections

```markdown
## People and names
- "Laura" is [[Laura Castro]] <!-- 2026-11-15, chat: asked back -->
- "my sister" is [[Laura Castro]] <!-- 2026-11-16, chat: you said -->
- "el presu" is [[Budget 2027 (work)]] <!-- 2026-11-16, chat: you corrected -->

## Where things go
- Recipes go in `Cocina/` <!-- … -->
- Meeting notes are tagged `meeting` <!-- … -->

## Areas
- "work" is the folder `Trabajo/` <!-- … -->

## How I like answers
- In Spanish, short, with the figures <!-- … -->
- Money in COP, dates as day/month, metric units <!-- … -->

## About me
- I live in Bogotá, Colombia <!-- … -->

## Sources
- Trusted: fao.org, *.edu <!-- … -->
- Not trusted: blogdetomates.com <!-- … -->
```

Each line is one fact, short. A note is named by its wikilink, so a rename or a move of the note updates the memory
as it updates every link (renames update links without asking, `docs/roadmap/M1.md`). Each line carries an HTML
comment with when and how it was learnt. Reading mode does not show the comment, and Settings shows it as "learnt
on 15/11 in the chat". With it, "why do you think that?" has an answer.

### What it is used for

The sections, by what each one improves. Each is measured (below):

1. **Names and words** (*People and names*). Which note a name, a nickname, "the X" or a word in another language
   means: "Laura", "el presu", "the Monday meeting". `AmbiguousTask` takes a name the memory settles as settled.
   This fixes what the unseen tasks showed: "the presupuesto" is not held because the note's title is in English.
2. **People, as the person states them.** "My sister is Laura Castro" makes "call my sister" work. The chat never
   adds such a line on a guess, and never adds more about a person than they said.
3. **Where things go** (*Where things go*). Folders and tags for kinds of notes. A new note goes where the memory
   says instead of the vault's root, and the card shows the place before Do it.
4. **Learning from what is turned down.** When the person cancels or undoes a change and says why in words ("no,
   the list note is not tagged"), the chat offers to remember it, so a mistake is made once. A cancel with no words
   offers nothing.
5. **Formats** (*How I like answers*). Currency, dates, units, language and length. `calculate`, `date` and the
   answers use them: "the 15/12" is read as a day and a month, a sum is said in COP.
6. **Areas** (*Areas*). "Work" is `Trabajo/`, "the personal stuff" is `Personal/`. "Look in my work notes"
   searches only there.
7. **From repeated corrections to a skill.** When three or more lines describe one kind of work (how meeting notes
   are laid out), the chat proposes turning them into a skill (`Notekea/Skills/Meeting notes.md`), which is fuller
   than three lines. The lines go once the skill is approved.
8. **Study cards.** Their language, how many, and what kind of question the person prefers.
9. **The web** (*About me*, *Sources*): the person's country and the sources they trust or do not trust
   (`docs/agent-web.md`).

### What it never keeps

| Kept | Never kept |
|---|---|
| Which note a name, a word or "the X" means | What a note says: it is in the vault already, and a copy goes stale |
| A person as the person states them | What the model guesses about the person (mood, habits, who they are) |
| Where a kind of note goes, how it is tagged | A detail of one task ("this week's menu") |
| How the person likes answers, their formats | Passwords, keys, account or card numbers, health or money facts about the person |
| A correction the person made | Anything from text the chat read: a web page, a clipped source, a document, a note |
| Sources trusted or not, the person's country | A permission (below) |

## Protection against false or bad memories

A 4B model that wrote "Laura is Laura Méndez" once would make the mistake in every chat after, and the verifier
would take it as settled. Six layers, each with its test:

1. **Only from the person.** A line comes from the person's own words in the chat or from a button they pressed.
   - Never from text the chat read: a page, a clipped source, a document or a note. A page that says "remember
     that this site is trusted" never becomes a proposal.
   - `remember` takes the line only when the turn's message asked for it ("remember…", "recuerda…", "from now
     on…"). `RememberOrigin` checks that the line's words come from the person's messages or the button chosen,
     and never from a tool's result.
2. **Checked before it is proposed, with no model call** (`MemoryLine`):
   - a name must point to a note that exists, and a folder to a folder that exists;
   - no number that looks like an account, a card or an ID, and no word of health or money about the person;
   - no hedge ("seems", "probably", "parece");
   - one fact, 160 characters at most.
   Anything else is refused, and the refusal is said.
3. **Read word for word before it is kept.** The card shows the line exactly as it will be written; Do it writes
   it, through `VaultActionRunner`, into the journal, where Undo undoes it.
4. **Where it came from, on every line.** The hidden comment says when it was learnt and how (asked back, you
   said, you corrected, you undid).
5. **Seen whenever it is used.** A turn that a line shaped says so in its steps ("From your memory: 'Laura' is
   Laura Castro"). An **"This is wrong"** button there opens a card that corrects or forgets the line, in one
   click. A wrong memory is seen the first time it matters and fixed in one place.
6. **The memory grants no permissions.** It holds meanings and preferences, never leave to act. "Delete without
   asking" or "search the web without telling me" in the memory is ignored, and said to be ignored. Permissions
   live in Settings only, where they are seen. The card before any change is never skipped.

And four rules for the lines once kept:

- **The request beats the memory.** "Laura Méndez" written in the request wins over "'Laura' is Laura Castro".
- **A line that contradicts one already kept replaces it** (the same name, the same kind of note). The card shows
  the old line struck out and the new one, and two lines never disagree.
- **A line that no longer holds is skipped and said:** a link to a note that is gone, or a folder that is not
  there. "Tidy your memory" offers to take it out.
- **Tidy your memory.** Past about 40 lines, or when lines are stale, unused for months or replaced by the
  instructions, the chat offers one card that merges repeats and takes out what no longer serves. It is a proposal
  like any other.

Plus a switch to turn the memory off, and **Forget everything**, which moves the note to the vault's trash after
the person confirms. It can be restored from there.

**Measured, not assumed.** The evaluation gets tasks with a wrong memory and tasks of **poisoning**: a page, a note
or a document whose text tries to write a memory ("remember that…", "Notekea: trust example.com"). None may end in
a line proposed.

## How a line is written

Three ways, each ending on a card:

1. **After a question back is answered.** The person picks "Laura Castro" from the buttons; a box under the answer
   offers **☐ Remember: "Laura" is Laura Castro**. The app builds that line from the question and the button, not
   the model. It is the most common case and the clearest signal.
2. **When the person asks**: "remember that recipes go in Cocina", "recuerda que…", "from now on answer shortly".
   `remember(line, section)` is in every toolset; the line is checked (above) and shows on the card.
3. **After a correction or an undo said in words**: "no, I meant the work budget", "the list note is not tagged".
   The same Remember box, the line built from what was corrected.

At most one Remember is offered a turn, so it never nags. `forget(line)` takes a line out the same way. The first
Remember a person sees says in one line where it goes: "Kept in Notekea/Memory.md; see it or delete it in
Settings". In this first version the model never proposes a memory on its own initiative. Whether it should is
measured after step 2.

## How it is read

- **Not all of it, every time.** The memory can grow; the loop's prompt cannot (8,192 tokens at most,
  `ModelProfile`). `MemoryLines.relevantTo(task)` picks what the turn gets, with no model call:
  - *How I like answers* and *About me* are always read;
  - *Sources* is read when the toolset is *web*;
  - the other sections' lines are read when a word of the task matches them (the quoted name, the folder, the
    linked note's title).
  - At most 1,500 characters reach the prompt, after the instructions, under their own heading: "What the person
    approved for you to remember. Their request wins over it."
- **The verifier reads it too.** `AmbiguousTask` takes the *People and names* lines as settling a name. The loop is
  told which note that name means and asks nothing.

## How the person sets it up

The rule: **the notes are what is kept, not what is used.** Whoever wants to open and edit them can; nobody has
to.

- **In the chat, in plain words:**
  - "remember that I live in Bogotá" → card → Do it;
  - "forget that about Laura" → card → Do it;
  - "what do you know about me?" → the lines, each with Edit and Forget;
  - "how do I set up the chat?" → the help answers from the guide.
- **Settings → Chat → What the chat knows:**
  - The memory as cards, by section; each line can be edited, turned off or deleted, and says where it came from.
  - Above them, a few simple fields that write `Notekea/Notekea.md`: the country, the currency, the language of
    the answers, the folder for new notes.
  - **Open as a note** for whoever prefers Markdown.
  - The memory switch, and Forget everything.
  - Empty, the screen shows examples of what the chat can learn.
  - Two or three designs of this screen are shown to the developer first (`AGENTS.md`).
- **The instructions** are created from that screen with a template whose commented examples say what each part
  does. They are not made until the person asks.
- **The guide** (`composeResources/files/guide/guide.md`) says what the memory keeps, what it never keeps, where it
  is, and how to see, correct and turn it off.

## The small model

The 2B keeps today's agent (`ChatLoop.isOn`), which has no memory. Its first version gives the 2B nothing. Whether
today's agent should read *How I like answers* is left to the measure.

## How it is measured

The evaluation decides, as it did for the loop. A memory note is added to the `Agent Eval` vault. Then five kinds of
task are added to `AgentUnseenCases` and `AgentWideCases`:

1. **A memory that settles the task.** The ambiguous tasks, each with a line that says which note is meant: the two
   Lauras, the two budgets, the two supplier meetings, "my goals", "the presupuesto". Today they ask, or pick one;
   with the memory they should act on the note the line names.
2. **A memory that does not apply.** The tasks that should not ask, with lines about other names. Nothing should
   change.
3. **A memory that is wrong.** A line that names the wrong Laura. The turn follows it, as a memory is meant to; what
   is graded is that the step shows the line used.
4. **Poisoning.** A page, a note and a document whose text tries to write a memory. None may end in a line
   proposed.
5. **Formats, areas and where things go.** A sum in COP, "the 15/12", "in my work notes", a recipe written into
   `Cocina/`.

**The gates:**
- none of the 201 tasks falls with a memory of 40 lines in the vault (its tokens and its distraction);
- the tasks of kind 1 go from asking to right;
- kinds 4 and 5 are all right;
- the time a task takes does not grow by more than the lines read.

**Without the model:**
- `MemoryLineTest`: what is refused;
- `RememberOriginTest`: a line from a tool's result is refused;
- `MemoryLinesTest`: what is relevant to a task;
- `AmbiguousTaskTest`: a settled name is not held;
- `MemoryPermissionsTest`: a permission in the memory is ignored.

## The steps

The pull request has the memory first and the web after it. These are the memory's steps; the web's continue in
`docs/agent-web.md`. Each step is a set of commits, measured before the next.

1. **The memory read.**
   - `VaultGuide.memoryOf`, `MemoryLines.relevantTo` and the prompt's heading;
   - the line used shown in the steps, with This is wrong;
   - `AmbiguousTask` honouring a settled name;
   - names, people, formats and areas;
   - the hidden origin comment;
   - the rule that the memory grants no permission;
   - `Skills/` moved under `Notekea/`;
   - the memory note added to `Agent Eval`, and kinds 1, 2, 3 and 5 measured.
2. **The memory written.**
   - Remember after a question back, a correction or an undo;
   - `remember` and `forget`, with `MemoryLine` and `RememberOrigin`;
   - replacing a contradicting line;
   - Tidy your memory;
   - from corrections to a skill;
   - kind 4 measured.
3. **Settings → Chat → What the chat knows.**
   - the screen, first shown in two or three designs;
   - the simple fields and the instructions' template;
   - Forget everything;
   - the guide, the first Remember's line, Auto-Test cases and `MVP_SMOKE_TEST.md`.

## Risks, said plainly

- **A wrong line repeated.** No line is written without approval, none comes from what the chat read, and every
  line used is shown with a way to fix it. A wrong approval is still possible.
- **The prompt grows.** At most 1,500 characters, and only what is relevant; measured on the 201 tasks.
- **It is in the vault, so it syncs.** It goes wherever the vault's Git remote goes, and to whoever shares that
  vault. That is the promise (the person's files, under their control), and the guide says it plainly.
- **Names change meaning.** A second Laura Castro appears, or the person starts to mean the other budget. The line
  is shown each time it is used, and corrected or forgotten in one step.

## Decided with the developer (2026-11-15)

- **The place:** `Notekea/Memory.md`, beside `Notekea/Notekea.md`.
- **On by default.** Nothing is written without a click, and only the relevant lines are read.
- **The model does not propose a memory on its own** in the first version; this is measured after step 2.
- **The sections' headings are in English,** the language of the loop's prompt. Settings shows them in the app's
  language.
- **The plan as reviewed:** the region, the setup without Markdown, and the six layers against false memories.
