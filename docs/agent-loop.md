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
        call    = model(prompt, grammar = toolset.grammar)      // our JSON: first the tool, then only its arguments
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
  **generated** from the registered tools: adding a tool is adding one class. Measured in step 0: a generated
  prompt and a grammar per tool beat today's hand-written prompt and flat schema.
- **`ToolResult`** — three faces of one result: `forModel` (compact text, a budget of its own), `forPerson` (the
  step's line, and what opens when the step is expanded), and `passages` (what can be cited, with note and range).
  An error is a result too, written to be corrected: "no note named X; the closest are Y and Z". Every result
  carries where it came from: the person's notes, or **untrusted** text (a web page, a clipped source).
- **`Toolset`** — the few tools given for one kind of work (below), **ten at most** (step 0: the 4B model chooses
  right 98 % of the time among 5 tools, 96 % among 10, 91 % among 20, 84 % among 33).
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

## Step 0, what was measured

Written as each experiment ends; the design above is changed where a result says so.

### A larger context for the loop — measured 2026-11-14, Medium (Qwen3.5 4B), Apple silicon, 16 GB

`ContextSizeChatModelTest` (`./gradlew :app:chatModelTest --tests '*ContextSizeChatModelTest*'
-PchatModels=<folder>`): a document of numbered clauses, asked for its second and its last, with the same memory
for the context (16,384 tokens) split among 4, 2 or 1 slots.

| Slots | Tokens a slot | Longest document read right | Seconds | Resident memory |
|---|---|---|---|---|
| 4 | 4,096 | 5,900 characters (100 clauses) | 8 | 4.3 GB |
| 2 | 8,192 | 13,200 characters (220 clauses) | 13 | 4.3 GB |
| 1 | 16,384 | 27,200 characters (450 clauses) | 25 | 4.2 GB |

- **The memory is the same.** The context's memory is its total, however it is split: a slot of 16,384 tokens
  costs what four of 4,096 cost today. What is given up is replies side by side.
- **It reads right to the end**: the last clause of a 27,000-character document, four and a half times what a
  slot reads today, with no fact confused.
- **Time grows with what is read**, about 1,100 characters a second: 25 seconds to read all of it once. The
  budget's trimming in blocks, so the prompt's start is not read again at every move, matters as much as its size.
- **Past its context a call fails at once** (`EngineFailed`, in a second), it is not cut short. The loop can count
  on that, and `ContextBudget` must never let it happen.

**What it changes in the design.** The loop is given one wide slot, not a narrow one of four. Two ways to have it,
to be measured next: load the model with fewer slots when the work is a loop (Deep thinking's three drafts and
`digest`'s parts then run one after another), or keep four slots over one shared context (`setKvUnified(true)`;
today each slot has its own), so a lone call may use it whole and four still run side by side when each is short.
The second keeps both if it holds; the 3.0 spike chose separate caches, for a reason to be read there first.

### The shared context — measured 2026-11-14, Medium, Apple silicon, 16 GB

The same test, with a second case (`build/reports/context-shared.md`): four slots over one context of 16,384
tokens (`setKvUnified(true)`), against today's four of 4,096 each. Times are the model's own (reading and writing),
not the clock's; the first table's were the clock's, through the app's engine.

| Calls at once (tokens each) | A context per slot (today) | One shared context |
|---|---|---|
| 1 × 2,328 / 5,328 / 11,078 | 5 s / fails / fails | 5 s / 7.5 s / 14 s, as one slot of 16,384 |
| 4 × 2,328 | all right, 19 s | all right, 12 to 19 s |
| 1 × 11,078, when every slot holds an earlier call | fails | right, 20 s: the idle slots are emptied for it |
| 2 × 5,328 | both fail (over 4,096) | both right, 12 and 15 s |
| 4 × 5,328 (21,312 in all) | all fail at once | **all four fail**, after 11 s of work |
| 11,078 + 3 × 2,328 (18,062 in all) | the long one fails at once, the three short are right | **all four fail** |

- **It holds.** A lone call reads the whole context with four slots loaded (the 27,000-character document, 14 s),
  and four short calls still run side by side. Memory: about 170 MB more than separate caches (3.87 against 3.70 GB
  resident).
- **Its one danger: calls that together pass the total all fail**, the short ones with the long one, and only
  after working for seconds. With a context per slot a call too long fails alone and at once. llama.cpp does not
  queue or refuse the call that does not fit; whoever calls it must.
- **A slot's kept prompt is lost when another call needs its room.** The loop's own slot is reused while it runs
  (its start is not read again), but a long call after others have run reads everything again.
- **Reading side by side saves no time on Metal:** four prompts of 2,328 tokens take 19 s together and 5 s each
  alone. What slots side by side save is the writing (the 3.0 spike), so they pay for many short calls, not for
  long reads.
- **Why 3.0 chose separate caches:** the spike gives no reason of its own. It measured the slots that way
  (`setKvUnified(false)`, every call a short one of the same size), and the engine kept the setting. Nothing
  measured then speaks against sharing.

**What it changes in the design.** The engine keeps four slots and shares the context among them; fewer slots are
not needed. In exchange the engine admits calls **by their tokens, not by a count of slots**: a call waits while
its tokens and those of the calls running would pass the context (today's `Semaphore` of slots becomes a budget of
tokens, fed by `countTokens`, step 2). `ContextBudget` then bounds the loop's prompt to what a lone call may use,
and the short calls of the pipeline, Deep thinking's drafts and `digest`'s parts go on side by side as today. The
change to the engine belongs to step 2, with a test of calls that together pass the total.

### The format of a tool call — measured 2026-11-14, Medium

`ToolFormatChatModelTest` (`build/reports/tool-format.md`, and a `.tsv` of every reply): the 126 requests of
`smoke-data/eval/actions.json`, each asked once in every format, graded on the first step **as the model wrote it**,
with none of the planner's repairs (with them today's planner gets 92 %). Today's 33 actions stand in for the
loop's tools, written as tools are (`EvalTools`: a name, a description, the fields it takes).

| Format | Right tool | Tool and fields right | Prompt tokens | Tokens written | Time a call |
|---|---|---|---|---|---|
| Today: the plan, one flat schema (any action, any field) | 115 | 107 of 126 | 3,011 | 42 | 1.9 s |
| The plan, **a grammar per tool** (the action, then only its fields) | 115 | 111 | 3,011 | 41 | 1.9 s |
| **One call in our JSON, a grammar per tool**, the prompt generated from the tools' descriptions | 115 | **113** | **1,492** | 25 | 1.4 s |
| The model's own tool calls, the same descriptions | 101 | 100 | 3,528 | 30 | 1.6 s |
| The model's own tool calls, with today's instructions and examples | 108 | 108 | 5,974 | 30 | 1.7 s |

- **Our JSON beats the model's own format**, 113 against 100 with the very same descriptions. Qwen3.5's template
  writes every tool out as its JSON schema, which costs 2.4 times the prompt (3,528 tokens against 1,492) and reads
  worse: with its own format the model opens the note a question is about instead of answering (19 of its 26
  misses are `open_note` or `search`, 15 of them for a question). It also wrote prose where a call was required,
  once, and twice with today's instructions.
- **A grammar per tool is better than the flat schema**, 111 against 107 on the same prompt: the misses it removes
  are fields a tool does not have (a folder written under `notes` for `create_note`, three times).
- **A prompt generated from the tools' descriptions is as good as today's hand-written one**, with half the
  tokens and none of its examples (about sixty): 113 against 111. What the examples buy is no longer visible once the grammar
  holds the shape. The misses left are choices between near tools (`append` or `add_task`, `answer` or
  `open_note`), which is the router's and the toolsets' work, not the format's.

**What it changes in the design.** Nothing in its shape, and two open points close: `ModelProfile`'s format for
this model is **our JSON under a grammar generated from the registered tools** (`{"tool": …}` first, then only
that tool's arguments), not the model's own tool calls; and the list of tools in the prompt is generated from
each tool's name, arguments and description, as the design says. The model's own format stays a property of the
profile for a model that is better at it (an installed Ollama, a larger model): the loop must not know which is in
use. A tool's description is its whole instruction, so it is written and tested as a prompt is.

### How many tools — measured 2026-11-14, Medium

`ToolCountChatModelTest` (`build/reports/tool-count.md`): the 45 requests whose tool is one of five (`answer`,
`find_notes`, `write_from_notes`, `help`, `show_tasks`), asked with 5, 10, 20 and 33 tools on offer.

| Tools on offer | Our JSON, a grammar per tool | The model's own calls | Prompt tokens (ours / its own) |
|---|---|---|---|
| 5 | 44 of 45 | 42 | 789 / 1,280 |
| 10 | 43 | 26 | 926 / 1,640 |
| 20 | 41 | 26 | 1,132 / 2,315 |
| 33 | 38 | 24 | 1,492 / 3,528 |

- **Each doubling costs a little, and it adds up:** 98 % right among 5, 96 % among 10, 91 % among 20, 84 % among
  33. Time does not change (the prompt's start is kept between calls); the prompt grows by about 25 tokens a tool.
- **Which tools are offered matters more than how many:** the model's own format falls from 42 to 26 the moment
  `open_note` is on offer beside `answer`. Two tools that can both be read as "do something with this note" must
  not be in one toolset unless their descriptions say which is which.

**What it changes in the design.** The toolsets stay, and get a number: **ten tools at most in a toolset**, the
ending tools counted. The design's *ask*, *web* and *app* sets are under it; *write* and *organize* (every changing
tool) are over it and are split in step 5: *organize* into the tools for notes' properties and
tags and the tools for files and folders, *write* without the reading tools it does not need once the router has
named the notes. The router's own choice is among five kinds, where the model is at its best.

### Koog against a loop of our own — read 2026-11-14

Koog's documentation as it is today (docs.koog.ai and the repository; version 1.3.0, stable since 1.0 with
`agents-core` under a promise of no breaking change without a major version; `agents-mcp` is still beta):

| What the loop needs | What Koog gives |
|---|---|
| The model in the app's own process, through llama.cpp | Clients for hosted models and Ollama (over HTTP). No llama.cpp or in-process client; one can be written against `LLMClient`, which the documentation names but does not describe |
| Every call held by a grammar | Structured output through the provider's own support, or instructions in the prompt and a second model call that repairs the reply (three tries by default). No grammar is passed down |
| A choice of tool forced on the model | Marked as not supported for Ollama, its one local route |
| A budget in tokens, old tool results dropped first | History compression by summarizing with the model (the whole history, the last N messages, in chunks), started by a count of messages. A strategy of our own can be written; dropping results by tokens is not built in |
| Nothing changes without the card | No confirmation before a tool acts; a rollback after it has |
| The conversation persisted as the turn goes | Checkpoints after each node, to memory or to files, with a storage of our own possible: the nearest fit |
| Steps shown as they happen | Events for a tool call starting and ending: a fit |
| MCP servers as tools, later | `agents-mcp` (beta): stdio and SSE |

**Decided: a loop of our own.** What makes this loop hard is what Koog leaves to the caller: a 4B model in
process, a grammar on every call (measured above: it is worth 13 requests in 126 against the model's own format,
which is what Koog would drive), a budget counted in tokens, and a card before any change. Its graph of nodes,
its hosted clients and its Ktor stack would come along unused. What is taken from it is the shape, so the door
stays open: a tool is a name, a description, typed arguments and a result with a text for the model, as Koog's
`Tool<Args, Result>` and MCP both have it. When extensions arrive (post-MVP item 1) the question is asked again
for the MCP client alone, Koog's module or the MCP Kotlin SDK directly; `LIBRARIES.md` does not change now.

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
