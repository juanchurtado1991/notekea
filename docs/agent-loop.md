# One agent loop — design

Asked for by the developer on 2026-11-14 (`ROADMAP.md`, post-MVP item 12): the chat's work as solid as Claude Code's,
on foundations made for it and not on patches. This is the design to agree on **before** anything is built. Branch
`feature/one-agent-loop`. Second version, after two reviews with the developer the same day.

## Why

The smoke of 2026-11-14 (`docs/HANDOFF.md`) showed what the chat lacked: it could not read the document, it wrote
without a source, and it had no memory of its own work. Each was fixed, but on a base with three engines:

| Engine | Decides | Its own |
|---|---|---|
| `PlanRequestUseCase` | a plan of up to 5 actions, in one reply | prompt, schema, 74 regexes that reshape its plan |
| `AgentAnswerUseCase` | up to 3 look-ups for a question | prompt, `AgentMove`, reader |
| `AgentTaskUseCase` | up to 30 moves for a task | prompt, `AgentTaskMove`, reader |

A tool is a string in a prompt, a field of a flat schema, a branch of a `when`. The conversation is rebuilt from
what the screen shows (`ChatMemory`, `WorkRecord` over `ChatEntry`). Budgets are 49 character constants; nothing
counts tokens, though the model's context bounds everything.

## What it becomes

One loop. The model is given the conversation and a small set of tools; it calls one, sees its real result, and
goes on until it answers, proposes or asks. Everything it did stays in the conversation.

```
RunTurnUseCase(conversation, message): Flow<TurnEvent>
    toolset = Route(message, conversation)            // one cheap call: which kind of work this is
    loop:
        prompt  = ContextBudget.fit(system + skill + toolset + plan + notes + conversation)
        call    = model(prompt, grammar = toolset.grammar)      // first the tool, then only its arguments
        result  = toolset[call.name].run(call.arguments, context)   // an error is a result it can correct
        conversation += ToolCall(call) + ToolResult(result)
        emit(TurnEvent.Step(...))
    until call is `answer`, `propose` or `ask_user`, or no moves or time are left
    TurnVerifier.check(...)                           // one last stage; may send it back once
```

### The pieces (`chat/domain`)

- **`ChatTool`** — `name`, `description`, `arguments` (a typed schema), `effect` (reads / proposes a change /
  ends the turn), and `suspend fun run(arguments, context): ToolResult`. Shaped as an MCP tool is (name,
  description, JSON schema of its input), so the roadmap's extensions (post-MVP item 1), web search (item 8) and
  OCR (item 9) plug in later as tools and not as new engines. The prompt's list of tools and the grammar are
  **generated** from the registered tools: adding a tool is adding one class.
- **`ToolResult`** — three faces of one result: `forModel` (compact text, a budget of its own), `forPerson` (the
  step's line, and what opens when the step is expanded), and `passages` (what can be cited, with note and range).
  An error is a result too, written to be corrected: "no note named X; the closest are Y and Z". Every result
  carries where it came from: the person's notes, or **untrusted** text (a web page, a clipped source).
- **`Toolset`** — the few tools given for one kind of work (below). A 4B model chooses worse the more it is offered.
- **`Conversation`** — typed turns: `UserMessage`, `AssistantText`, `ToolCall`, `ToolResult`, `Proposal` and its
  `Outcome` (done, cancelled, undone, failed). Persisted by `ChatHistoryStore` as the turn goes, so a chat reopened
  knows what it read and did, and a turn cut by closing the app is shown as stopped where it was. `ChatEntry`
  becomes a projection of it for the screen.
- **Working memory, outside what is trimmed** — the **plan** (`plan` tool: steps the model writes and ticks; the
  list the person sees) and the **notes** (`note` tool: a fact and its passage). Both are always in the prompt, so
  a long task does not forget what it was doing or what it found, and an answer written from notes is cited.
- **`ContextBudget`** — one place that knows the model's context. `LlmEngine` gains `countTokens(text)` and
  `contextTokens`. It trims whole entries, oldest tool results first, in blocks and not at every step, so the
  model's cache of the prompt's start is reused; it never drops the task, the plan, the notes or the last result.
- **`ModelProfile`** — what the model in use allows: its context, the format it calls tools in, how many tools and
  moves it is given. The loop reads it; nothing else knows the model's limits. A larger model, or an installed
  Ollama (post-MVP item 10), is a different profile and not a different design.
- **`TurnVerifier`** — the last stage (below).

### The tools

Reading, never more than is needed:

| Tool | Gives |
|---|---|
| `search(query, in?)` | the passages that match, in the whole vault or in a folder, a note or a tag |
| `outline(note)` | its headings and how long each part is, and the documents attached to it |
| `read(note, section?)` | a part of a note, or all of a short one |
| `digest(notes, question)` | what long notes or documents say about a question, read part by part in calls of their own, with the passages; the heavy reading happens outside the turn's context |
| `list(folder \| tag \| recent)` | what is there, by name |
| `links(note)` | the notes that link to it and that it links to |
| `find(query)` | notes by their properties |
| `tasks(filter?)` | open tasks and their dates |
| `fetch(url)` | a web page's article, only an address the person wrote |
| `sources(question)` | pages of the vault's web sources not clipped yet that may hold it |

Done by the app, not asked of the model: `calculate(expression)`, `count(what, where)`, `date(phrase)`.

Working: `plan(steps)`, `note(fact, passage)`.

Changing, which only ever add to the proposal on the card: `write_note(title, folder?, from, what)` (the body is
written afterwards from `from`, section by section when it is long, so no table is cut by a token cap),
`edit_note(note, section, what)` (shown on the card as what changes), `append`, `set_property`, `add_tag`,
`add_task`, `link`, `move`, `rename`, `create_folder`, `bookmark`, `trash`, `clip(url)`, `make_cards(note)`.

Ending the turn: `answer(text)`, `propose()`, `ask_user(question, options)` — a question with buttons instead of a
guess, when the request can be read two ways or something it needs is missing.

**Toolsets**, chosen by the first call: *ask* (search, outline, read, digest, links, calculate, answer),
*write* (the same and write_note, edit_note, plan, note, propose), *organize* (list, find, tasks and the changing
tools), *web* (fetch, sources, clip), *app* (how to use Notekea, settings). `ask_user`, `plan` and `answer` are in
all. This routing replaces the planner's regexes; a request recorded as failing without one keeps it as a rule of
the router.

### Skills and the vault's instructions

- **Skills** — short instructions for a kind of work, loaded only when the router names it: a comparison table, a
  summary of a document, study cards, meeting notes. For a small model the shape of the task said plainly is worth
  more than a longer prompt. They ship with the app; a folder of the vault may add the person's own.
- **The vault's instructions** — one note of the vault (its place a setting), always read: where new notes go, the
  tags in use, the language, how the person names things. What `CLAUDE.md` is to Claude Code.
- **Memory between chats** is left for later: what it may keep, and how the person sees and deletes it, needs its
  own decision.

### `TurnVerifier`

1. A note is written only from what was read (`AttachedTexts`, `NotesWriter`); with nothing read, nothing is
   written. A clipped page is read from its own text.
2. No placeholders (`Placeholders`), no figure the sources lack (`BackedFigures`).
3. **Each sentence of an answer or a written note is backed by a passage of the turn**, scored by the reranker as
   the study cards already are (`SupportCheck`); an unbacked sentence is sent back once, then left out and said.
4. **A critic call**, when the evaluation shows it earns its time: "does this do what was asked; what is missing?",
   answered in a grammar of ok or one thing to redo.
5. **Nothing stale**: a note read during the turn that changed on disk since is read again before the card's Do it.
6. **Nothing from untrusted text acts**: an address or an instruction found in a web page or a clipped source can
   be reported, never fetched, clipped or followed. Today's rule for `fetch`, made general.

### What does not change

- **Nothing changes in the vault without the card.** Reading tools act at once; changing tools only propose.
- `VaultActionRunner`, its journal and Undo; `CheckPlanUseCase` and `VaultNames`, now as argument validation.
- A move made twice ends the loop; a limit of moves and of time; grammar-constrained output; Stop.
- Answers from the notes with citations (`AnswerFromNotesUseCase`), as the core of `search` and of the fast path.
- What the person sees: the steps with a check, the card, the answer with its sources. New: a step opens to show
  what it read, and Copy chat holds the whole trace.

### What goes

`AgentAnswerUseCase`, `AgentMove`, `AgentTaskMove`, `AgentLookUp`'s `when`, three of `ActionPlanReader`'s four
readers, `ChatMemory` and `WorkRecord` (their job moves into `Conversation`), most of the plan-shaping regexes, and
most of the 18 classes around `ChatViewModel`, which keeps intents, the entry list and effects.

## Fast path, and the small model

A greeting or a plain question is settled by the router's one call and costs what it costs today. The Small model
gets the same loop with a smaller profile; if its evaluation falls short it keeps the fast path only, behind the
same interface.

## How it is built, and the gate at each step

**The evaluation sets decide**, not an impression. `smoke-data/eval/actions.json` (106 requests), `answers.json`,
`AgentEvalCases` (30 tasks) and `chat-probes.txt` exist. Each step ends by running them with the real model; none
may fall, and time is measured beside correctness. The old path stays, switchable by a setting, until step 6.

0. **A net, and what the design rests on, measured first.**
   - Record today's requests with the model's raw replies as fixtures, replayed through `FakeLlmEngine` in `check`:
     a routing regression suite that needs no model.
   - Add to the agent's evaluation: tasks over documents (the contract of 2026-11-14), ambiguous requests, long
     notes, and for each the tools it should use, whether the result is backed, and the time.
   - Four experiments, a day each at most, whose results are written here before step 1: the model's own format
     for calling tools against our JSON; a grammar that picks the tool and then only its arguments against the
     flat schema; how many tools the 4B model chooses well among; and **a larger context for the loop** (it runs
     one call at a time, so its slot could be 8,192 tokens or more where memory allows; today every slot is 4,096).
   - Whether an agent runtime would carry the loop better than our own: the roadmap names Koog with MCP. To be
     read in its current documentation, not assumed; the tool contract is MCP-shaped either way.
1. **`ChatTool`, `ToolResult` and the registry**, with today's look-ups as tools behind the existing
   `AgentTaskUseCase`. Nothing visible changes.
2. **`countTokens`, `ModelProfile` and `ContextBudget`**, replacing the chat's character constants. Plus a test
   that no prompt exceeds the context, and property tests of the trimming.
3. **`Conversation` in the domain**, persisted as the turn goes; `ChatEntry` projected from it; `ChatMemory` and
   `WorkRecord` go. The old history is read once and converted.
4. **The reading and working tools**: `outline`, `read` by section, `digest`, `links`, `list`, `calculate`,
   `count`, `date`, `plan`, `note`, `ask_user`; skills and the vault's instructions.
5. **The changing tools and the single loop** in agent mode: `RunTurnUseCase`, the router and the toolsets,
   `write_note` by sections, `edit_note`.
6. **The planner retired**: every mode on the loop, compared with the old path on the sets and on every Auto-Test
   case of 3.3, 3.6, 3.8, 3.9, 4.4, 4.6 and 4.7; then the old path and its setting are removed.
7. **`TurnVerifier`** as the one last stage: the backed-sentence check, the critic if it earns its time, the stale
   and the untrusted rules.

Each step is its own set of commits and leaves the app working; the work can stop after any of them. Tests of the
loop use a scripted model, including replies that are wrong on purpose: an unknown tool, bad arguments, a call cut
short. The loop feeds the error back and recovers, twice at most.

## Risks, said plainly

- **The local model bounds it.** A 4B model will not reason like Claude. The loop makes it honest, grounded and
  consistent; it does not make it smarter. A larger context and the model's own tool format may help; step 0 says.
- **More calls, more time.** A task that took one plan call may take four. Time is a gate of every step, and the
  budget's trimming in blocks is there for it.
- **One loop may route worse than the planner** on the requests its regexes were tuned for. Step 0's fixtures show
  where; the router keeps the rule where it does.
- **Size.** Steps 3, 5 and 6 touch most of `chat/`. They are done behind the same `ChatViewModel` intents, and the
  old path stays switchable until step 6, so there is always a working chat to fall back to.

## To decide with the developer

1. **The commit hook.** It runs `check koverVerify`, the whole real-app suite (about 30 minutes), so every commit
   of this work would skip it. Proposed: ktlint and the unit and architecture tests (about two minutes); the
   real-app suite stays in CI and on demand.
2. **The chat history kept on disk changes shape** (step 3). Proposed: yes, the old history converted once.
3. **The vault's instructions and the person's own skills** add a setting and a folder to the product. Proposed:
   yes to both, in step 4.
4. **The repair of a repository left half merged**: proposed after step 1, since it is small and unrelated.
