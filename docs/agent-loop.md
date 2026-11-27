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
  own decision. Designed on 2026-11-15 in `docs/agent-memory.md`, and the web search on it in `docs/agent-web.md`.

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

## The modes go

Decided with the developer on 2026-11-14. Auto, Fast, Deep thinking and Agent are a menu for choosing among three
engines; with one loop there is nothing to choose. The menu is removed in step 6, and what it gave is kept another
way:

- **The router decides the effort as well as the kind of work**: the short path for a greeting or a plain
  question; more moves, whole documents and the strong verification for a task over a long document.
- **It can be said in words** ("answer quickly", "check it thoroughly"), which the router reads.
- **Stop** ends a turn that takes longer than wanted, with what it had.

What stays where the menu was: the model in use (Small, Medium, Large) and the memory ring.

**The condition**, measured in step 6: the evaluation sets get the effort each request should have, and the menu
goes only if the router gets it right for nearly all. If it does not, one control of effort stays (Fast, Auto,
Thorough) in the menu's place. Deep thinking's three drafts and vote go either way: reading side by side saves no
time on Metal (measured above), and step 7's verification serves what it was for.

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
6. **The planner retired, and the modes with it** ("The modes go"): everything on the loop, compared with the old path on the sets and on every Auto-Test
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

### The net: the model's replies as fixtures — recorded 2026-11-14, Medium

`smoke-data/eval/fixtures/actions-medium.json` holds, for each of the 126 requests of the action evaluation, every
reply the Medium model gave the planner, word for word, and whether the plan that came of it was the expected one
(123 of 126 with one slot; the evaluation's own run, four at a time, gave 92 %). `ActionFixturesTest` replays them
through `FakeLlmEngine` in `check`: each reply must still become the plan it became. So everything between the
model's reply and the plan (the reader, the 74 regexes, the checks) is held in place with no model, which is what
steps 1 to 6 need while they move that work into tools and a router. `ActionFixturesChatModelTest` records them
again (five minutes) when the planner's prompt changes on purpose; a recording shows as a diff a request a line.

### The wider agent evaluation — today's agent on it, measured 2026-11-14, Medium

`AgentWideCases`: 21 tasks over a vault made for them (`smoke-data/vaults/Agent Eval`: a 9,400-character service
agreement with annexes of tables, 8,600 characters of reading notes on eight studies, and two budgets, two supplier
meetings and two projects a request can mean either of). Each task names the tools of this design it should use,
how the turn ends (an answer, a card, a question back) and what the answer or the card must hold; `AgentWideCasesTest`
checks in `check` that every such value is in the vault. Today's agent on them
(`AgentEvalChatModelTest`, "agent mode is measured on…", `build/reports/agent-wide-medium.md`):

| Kind of task | Right today | What goes wrong |
|---|---|---|
| A document (8) | 3 | The answer comes from the document's start: a clause in the middle, a table in an annex or a sum over it is missed |
| A long note (4) | 1 | The same: the last study, or a count over all eight, is not reached |
| A request that can be read two ways (5) | 0 | It never asks: it picks one budget, one meeting, one project, or stops |
| Several notes (4) | 3 | A difference between two notes' figures is not worked out |
| **All (21)** | **7 (33 %)** | 39 s a task on average, 140 s at most |

This is the mark steps 4 to 7 are measured against, beside the 30 tasks of 4.7 (83 %), which must not fall. Each
kind points at its piece of the design: `outline`, `read` by section and `digest` with the wider context for the
first two, `ask_user` for the third, `calculate` for the sums. Limits of the set, to know when reading its
numbers: a task that writes a note is graded on the card naming it, not yet on what the note says (that is
`TurnVerifier`'s backed-sentence check, step 7); the tools used are compared only once the loop exists (step 5);
and the agreement is Markdown, where the smoke of 2026-11-14 used a PDF pasted in a note.

### Step 0, closed — what is not measured

Everything above is the Medium model on Apple silicon (Metal). Not measured: the shared context on Vulkan and on
the CPU (step 2 measures it where it changes the engine); the Small and Large models in the tool formats (their
profiles are measured when the loop reaches them, step 5 and 6); the model's reasoning turned on.

## Step 1, done — 2026-11-14

`chat/domain/tool`: `ChatTool` (name, description, arguments, effect, the target of a call, `run`), `ToolCall`,
`ToolResult` (with where it comes from: the vault, or untrusted text), `ToolContext` (the vault, the task, what the
turn read in `TurnReads`, the vault's names indexed once a turn) and `ToolRegistry`, which generates the prompt's
list and the grammar of step 0 (`{"tool": …}` first, then only that tool's arguments). Today's five look-ups are
tools (`tool/lookup`: `SearchTool`, `ReadTool`, `FindTool`, `TasksTool`, `FetchTool`); `AgentLookUp` and its `when`
are gone, and `AgentTaskUseCase` runs a move through the registry.

Nothing the model sees changed: the agent's prompt and its flat schema are today's, the generated ones wait for
step 5. `ToolResult`'s other faces (`forPerson`, `passages`) come with the tools that need them, in step 4.

**The gate.** Every test of the chat passes untouched, the 126 recorded replies among them. With the real model
(Medium): the 30 tasks of 4.7, 27 right (90 %; the bar to offer agent mode is 80 %), 35 s a task; the wider
evaluation, 7 of 21 (33 %), the same seven as before, 42 s a task against 39.

## Step 2, done — 2026-11-14

**The engine.** The four slots share one context (`setKvUnified(true)`), and `TokenRoom` admits calls by their
tokens: a call waits while the calls under way and it would pass the total, a lone call is admitted whenever it
fits, and one longer than the whole context fails at once and is logged, where it used to fail inside llama.
`LlmEngine` gained `countTokens(text)` (the model's own tokenizer) and `LlmModelInfo.contextTokens` (the slots'
context together: 16,384 on a machine with four slots, 4,096 with one).

**The domain** (`chat/domain/context`). `ModelProfile` says what the loop may use of the model: its context, and
of it 8,192 tokens at most for the loop, so a move is read in seconds. `ContextBudget`, one a turn, fits what the
turn found beside the prompt's fixed part and the reply: whole entries, the oldest dropped first and several at
once (down to three quarters of the room, so the prompt's start is not rewritten at every move), the newest
always kept, a line said in place of what was dropped, and what was dropped stays dropped (`FittedContext`).

**What it replaced.** The agent kept the last 9,000 characters of what it found, cut wherever they fell: it now
fits it by tokens. `NotesWriter` assumed a 4,096-token context and three characters a token: it counts, so a
document that fits the context is read whole and digested only where it does not (12,000 characters: three calls
in a 4,096 context, one in 16,384). The other character constants of the chat are sizes of one result or belong
to what later steps remove, and go with them: `ChatMemory` and `WorkRecord` in step 3, the look-ups' own caps
(`ReadTool` 2,500, `FetchTool` 3,000) with `read` by section in step 4, the question agent's and the
conversation's in step 6.

**Tests.** `TokenRoomTest` (admission by tokens), `ContextBudgetTest`, `ContextBudgetPropertyTest` (300 turns made
at random: what is kept fits, whole and in order, the newest stays, what was dropped never comes back),
`AgentContextTest` (twelve long notes read one after another: no prompt longer than the context) and
`GroundedWorkTest` (a fitting document read whole).

**The gate.** Every test of the chat passes. With the real model (Medium, four slots over the shared context):
the 30 tasks of 4.7, 27 right (90 %), 30 s a task against 35; the wider evaluation, 7 of 21 (33 %), the same
count as before, 42 s a task. No call was refused for its length.

## Step 3, done — 2026-11-14

**The conversation** (`chat/domain/conversation`). `Conversation` is a vault's chat: its turns, the summary of
the first of them and how many it stands for. A `ChatTurn` is a question and its items in order (`TurnItem`): a
tool called (`Call`), what it gave back and where from (`Result`), what was said with its passages (`Text`), and
a `Proposal` with its changes and how it ended (`ProposalOutcome`: proposed, done, cancelled, undone, failed).
`ConversationMemory` says what the model is given of it (the summary, the chat's start, the last exchanges, each
with `TurnRecord`, what the chat did) and compacts it; both are functions of the conversation, in the domain.
`ChatMemory` and `WorkRecord`, which did this in the view model over what the screen showed, are gone.

**Persisted.** `ChatHistoryStore` loads and saves a `Conversation`. The file is version 2: a turn holds its
items, the chat its summary. A file of version 1 (a question, its answer and sources) is read as turns that only
answered, and written in the new form at the next save: no migration step, nothing to lose. A turn is written
when it is asked, as stopped, and again as it ends: closed in the middle, the app shows it stopped. Opened again,
a turn shows the steps it made and a line for how its proposal ended, where it used to say only "Done in this
vault" and forgot a proposal cancelled or undone; the summary survives too, where a new session started without it.

**What waits for step 5.** The screen's `ChatEntry` is still what a turn under way is built in, and the
conversation is made from it when it is kept (`EntryTurns`); a chat opened again is projected from the
conversation. The loop of step 5 writes its items straight into the conversation, results included (today's
paths keep no result, only the call), and saves at every item; `EntryTurns.turnOf` goes with the old paths in
step 6.

**Tests.** `FileChatHistoryStoreTest` (items, proposal and summary read back as written; a version 1 file read),
`ChatReopenedTest` (a chat opened again shows and keeps what it did; the model is told the summary and the
record; a question is kept from the moment it is asked), `ChatMemoryTest` and `WorkRecordTest` on the domain's
memory.

**The gate.** Every test passes. With the real model (Medium): 27 of 30 (90 %), 30 s a task; the wider
evaluation 7 of 21 (33 %), 42 s: where they stood. Not run: the chat probe, which is a transcript read by eye and
has no score; and `ChatMemoryChatModelTest`, which measures RAM with the Small model, not installed here.

## Step 4, done — 2026-11-14

**Reading no more than is needed** (`chat/domain/tool/reading`). `OutlineTool` gives a note's headings, nested,
with the words each part holds and what it attaches; `ReadTool` takes a `section` (a heading, its subsections with
it; a heading the note lacks gets the note's headings back); `ListTool` names what is in a folder or under a tag,
or the notes changed last; `LinksTool` the notes a note links to and that link to it, as the backlinks panel
finds them; `DigestTool` reads long notes part by part in calls of their own and gives the turn only what each
part says about the question. All open a note through `NoteOpener`, so what they read is what a note the turn
writes is written from.

**Done by the app, not asked of the model** (`tool/app`). `CalculateTool` (`Arithmetic`: precedence,
parentheses, percents, thousands; what is not arithmetic is said, never guessed), `CountTool` (notes, words,
tasks in the vault, a folder, a tag or a note) and `DateTool` (`DatePhrases`: "tomorrow", "next Friday", "in 3
weeks", "end of month", in English and Spanish, from `ToolContext.today`).

**Working** (`tool/work`). `TurnWork` holds the changes a turn adds to its proposal. The plan and the noted facts
(`PlanTool`, `NoteTool`) were written and never put in a toolset, and were taken out on 2026-11-26. `AskUserTool` ends the turn with a question and two to four
answers. A list argument reaches a tool one item a line (`ToolCall.list`).

**Skills and the vault's instructions** (`chat/domain/skill`). `BuiltInSkills`: a comparison table, a document
summary, study cards, meeting notes, each a few lines. `VaultGuide` reads the vault's instructions (one note,
`Notekea/Notekea.md` until a setting says otherwise; at the vault's root until 2026-11-15, when the developer put
it in a folder of Notekea's own, where the chat's memory goes too) and the person's own skills (the notes of
`Skills/`: the first line describes one, the rest instructs it; one named as a skill of the app replaces it).

**What waits for step 5.** None of this is called by today's agent, whose prompt and flat schema are its own:
the loop, its router and its toolsets are step 5, and with them the setting for where the instructions and the
skills live, and the first measure of these tools with the model. `sources(question)` stays where it is (the
chat's site search) until the *web* toolset.

**Tests.** `ReadingToolsTest`, `AppToolsTest`, `WorkToolsTest`, `VaultGuideTest`: each tool on a vault in
memory, the digest with a scripted model.

**The gate.** Every test passes. With the real model (Medium), today's agent, which none of this touches but
`ReadTool`: 27 of 30 (90 %), 30 s a task; the wider evaluation 7 of 21 (33 %), 42 s. The tools themselves are
measured with the model in step 5, when the loop calls them.

## Step 5, the loop measured — 2026-11-14 (not yet in the app)

**The loop** (`chat/domain/loop`). `RunTurnUseCase`: a first cheap call (`TurnRouter`) names the kind of work
(`WorkKind`: write, organize, web, ask) and the skill that fits; the loop is given that kind's tools
(`LoopTools`, `Toolsets`), the vault's instructions and the skill (`LoopPrompt`), and calls one tool at a time
under the registry's grammar (`ModelJson` reads it) until it answers, proposes or asks (`AgentStep.Asked`). The
changes are tools (`tool/change`: `ChangeTools`, each one of 3.9's actions; `ProposeTool` ends on the card, checked
and written by `ProposalCheck`, shared with today's agent). `TurnState` holds a turn as it goes.

**What the measures taught, and what the loop does for it.** Each was found in a report of the evaluation and
fixed in general, not for its task:
- A set that lacked the tool a task needed failed far more than a larger set chose wrong: sets hold what their
  work needs, up to 25 tools since `change_line` (4.2), 24 before (the developer lifted step 0's limit of ten: "if
  more tools work better, so be it").
  Looking (search, read, find, tasks, list) is in every set, and write_note in organize too.
- The model made the same call again and again: a call made again is told, and its tool is withdrawn for the next
  call; three repeats and only changing or ending tools are left. A change asked again is told it is already in
  the proposal.
- It proposed with nothing added, or asked what to put in a note: told once, and asked again.
- It asked when the task left nothing open ("my two budgets", "las recetas venezolanas"): a question is held back
  once unless the task points at one thing it does not name ("the project", "tag it"; `SingularTask`).
- It changed several notes when the task said "the" one: the change is held back once with the question to ask.
- A bare name several notes share is told with their paths; found notes are listed by their quoted paths.
- The router sent "find my recipes and tag them" to ask, which cannot tag: a request that changes anything is
  never ask.
- The answer is written from what was found as far as the context goes, and gives the figures it is worked out
  from (also today's agent's answer).

**Measured (Medium, 2026-11-14)**, `-PchatTasks=` runs a few tasks for a quick check:

| | Today's agent | The loop | Mark (the developer) |
|---|---|---|---|
| Wider evaluation (21) | 7 (33 %), 42 s a task | 20 (95 %), 127 s a task | 19 |
| Agent mode's tasks (30) | 27 (90 %), 30 s a task | 29 (96 %), 69 s a task | more than 27 |

Left wrong: the reading notes' studies that used naps (it reads every section, then concludes only one used naps),
and the home renovation's notes (it tags the note `Home` too). **Time is the cost:** two to three times today's,
from more calls (11 and 7 on average, 30 at most). Bringing it down is the next work of this step, with the loop
wired into the app behind a setting, ask_user's answers as buttons, `write_note` by sections and `edit_note`.

### Step 5, the loop in the app and the held-out tasks — 2026-11-14

The loop is reached from agent mode when Settings → General turns it on (`ChatLoop`); a question back shows its
options as buttons (`ChatAskedOptions`), the one chosen sent as the next message. 150 tasks written after the loop
was tuned, by agents that had not seen it, measure it on what it was not tuned on: 70 over the Sample Vault
(`AgentHeldOutCases`) and 80 over 25 new notes of `Agent Eval` (`AgentWideHeldOutCases`).

**Measured on the held-out tasks (Medium, 2026-11-14), before any tuning on them:**

| | Today's agent | The loop |
|---|---|---|
| Held-out, Sample Vault (70) | 53 (75 %), 26 s a task | 51 (72 %), 69 s a task |
| Held-out, `Agent Eval` (80) | 31 (38 %), 43 s a task | 60 (75 %), 116 s a task |
| Both (150) | 84 (56 %) | 111 (74 %) |

On the wider tasks the loop held what it was tuned to (75 % where today's agent gets 38 %); on agent mode's kind of
task it fell two short of today's. Twelve tasks today's agent gets right the loop got wrong, eleven the other way.
As agreed (half and half), the loop is tuned looking only at the odd-numbered held-out tasks of the Sample Vault;
the even-numbered ones stay unseen and say whether the tuning holds. What the odd half showed:

- It added changes nobody asked for after the right one: the list note tagged too (the reading list, the weekly
  menu), a task invented ("tag notes mentioning Lisbon"), the tag translated (`travel` for "viaje"). A change added
  now shows the whole proposal so far and says to add nothing the task does not ask for; a tag or a property name
  the task does not write is held back once (`ChangeTool`).
- It asked the person to choose among notes it had not read ("which meeting is about the sidebar?"). Such a
  question is held back once, the model told which options it has not read (`UnreadChoice`).
- It could not see which notes have a property ("the notes with a status"): `list` now gives each note's
  properties' names.

Two of the wider tasks it got wrong after that tuning showed a fault of the loop itself: a change refused the same
way (a template written "none") was asked again until the steps ran out, as a refusal never counted as a call made
again. A field written none, null or n/a is now left out, and a change refused twice the same way counts as made
again and is not offered for the next call.

**Measured after the tuning (Medium, 2026-11-14):**

| | Today's agent | The loop, before | The loop, tuned |
|---|---|---|---|
| Held-out, Sample Vault (70) | 53 (75 %), 26 s | 51 (72 %), 69 s | 53 (75 %), 46 s |
| — the odd half, looked at | | 26 | 25 |
| — the even half, unseen | | 25 | 28 |
| Held-out, `Agent Eval` (80) | 31 (38 %), 43 s | 60 (75 %), 116 s | 58 (72 %), 108 s |
| Wider evaluation (21), tuned on | 7 (33 %), 42 s | 20 (95 %), 127 s | 19 (90 %), 110 s |
| Agent mode's tasks (30), tuned on | 27 (90 %), 30 s | 29 (96 %), 69 s | 29 (96 %), 53 s |
| All (201) | 118 (59 %) | 160 (80 %) | 159 (79 %) |

The unseen half went up by three, so the tuning holds beyond what it looked at; the loop now matches today's agent
on agent mode's kind of task and is far ahead on the wider ones, at about twice its time. By the developer's rule
(accuracy first, time after) this lets step 6 begin.

### Step 6, what the old paths do that the loop does not — mapped 2026-11-14

Before the planner, the question agent and the modes go, the loop must do what only they do today. Mapped from the
code (the old paths: `ChatAnswering` → `ChatActions.act` → `ActOnRequestUseCase` / `PlanRequestUseCase`, the
question agent `AgentAnswerUseCase` retried on NOT_FOUND, `ChatReplies` for questions, talk, help and the overview,
and the modes through `AgentUse`):

- **App actions shown at once** (open_note, open_today, search, show_panel, split_pane, close_other_tabs,
  toggle_source, set_theme): `ActionTurnStep.Shown` → `ChatEffect.Show`. The loop needs an *app* toolset whose
  tools end the turn with the step shown.
- **Replies that are not looked up**: talk (`ConverseUseCase`), help with its shortcuts and offered requests
  (`AnswerHelpUseCase`, `HelpShortcuts`), the vault's overview (`DescribeVaultUseCase`). The router gets kinds for
  them (or tools of the ask set), reusing the use cases.
- **Found tables** (find_notes, show_tasks, list_untagged, list_broken_links): the loop reads them as text only;
  the person should get the clickable table as today.
- **Answers with sources**: today's answers carry `[n]` citations and stream as they are written; the loop's
  answer comes whole and without sources. `AgentStep.Finished` gains the sources it was written from, and the
  answer streams.
- **A note or folder as scope**: only the vault scope reaches the loop; `ToolContext` gets the scope, the looking
  tools keep to it.
- **A clip's section question** (`SectionAsked`): on the loop it ends as a failed card; it must ask as today.
- **Refusals** (`RefusalChecked`, `REFUSE`) and **Deep thinking**'s three drafts: the router decides the effort
  instead of the menu, as decided.

Sub-steps, each measured on the sets and its Auto-Test cases before the next: **6a** the app toolset and the
replies (talk, help, overview) on the loop; **6b** found tables, sources and streaming; **6c** scope and the
section question; **6d** everything on the loop, compared with the old path on the action evaluation and the
Auto-Test cases of 3.3, 3.6, 3.8, 3.9, 4.4, 4.6 and 4.7 (46 cases); **6e** the old paths, the modes, their menu
and the setting removed (about 2,950 lines: the planner and its word rules, the question agent, the old agent,
`ChatMode`, `DeepQuestion`, `DraftVote`, `AgentUse`), with the tests that only test them.

### Step 6a, the app's actions and the chat's own replies on the loop — 2026-11-14

With the setting on, every request of the whole vault goes to the loop, whatever the mode (`ChatAnswering`); a note
or a folder as scope still goes to today's answer until 6c. The router has four more kinds: *app* (a note or today's
note opened, a panel, the app's search, the theme, the panes, the note as Markdown: `ShowTool`, `ShowTools`, ending
the turn with `AgentStep.Shown`, done at once with no card), and *talk*, *help* and *overview*, which the loop hands
back (`AgentStep.Replied`) to the chat's own replies (`ConverseUseCase`, `AnswerHelpUseCase`, `DescribeVaultUseCase`)
with no tool called. As a router's slip costs a whole turn, *app* has a `help` tool (`HandTool`) and *organize* has
`append`. Wider overlaps were tried first (*ask* with `help` and `overview`, *organize* and *write* with `answer`):
the loop then took the wrong way out more often (27 of 30, 16 of 21, 49 of 70, 54 of 80), so they were taken out.

**The router on the action evaluation's 126 requests** (`RouterEvalChatModelTest`, refusals not graded): 101 of 120
at first ("show my tasks" and "how do I…" sent to *app*, "shorter" to *app*); with the kinds described by what they
are not and two rules ("how do I…" is help; a request naming anything in the notes is never talk), **116 of 120**.
Left: "explain the reading challenge" to help, "what happened at the October retro" to talk, "what can you do for
me?" to talk, "explain what is in my notes" to talk.

After the overlaps were taken out, the router gives **111 of 120**, and three changes came from the wide tasks it
still failed: a request with a note to write is *write* even when it compares or summarizes, *write* makes study
cards and quizzes, and *ask* counts, compares and lists what it finds.

**The loop with 6a, on the four sets** (4B, 2026-11-14), against today's agent on the same tasks:

| Set | Loop before 6a | Loop with 6a | Today's agent |
|---|---|---|---|
| Agent mode's 30 | 29 | 28 | 27 |
| The wide 21 | 17 | 17 | 7 |
| Held-out 70 | 53 | 53 | 53 |
| Held-out wide 80 | 58 | 63 | 31 |

6a holds: the loop now takes every request of the whole vault and does no worse on any set. The tuning target of
19 of 21 is still not reached.

**The three models** (2026-11-14, every set; time is the average a task, in seconds):

| Set | 2B loop | 2B today | 4B loop | 4B today | 9B loop | 9B today |
|---|---|---|---|---|---|---|
| Router (120) | 83 | — | 111 | — | 113 | — |
| Agent mode's 30 | 12 (36 s) | 10 (16 s) | 28 (55 s) | 27 (30 s) | 22 (76 s) | 23 (46 s) |
| The wide 21 | 9 (44 s) | 6 (25 s) | 17 (97 s) | 7 (42 s) | 17 (118 s) | 8 (58 s) |
| Held-out 70 | 21 (39 s) | 18 (13 s) | 53 (45 s) | 53 (26 s) | 45 (61 s) | 46 (36 s) |
| Held-out wide 80 | 27 (39 s) | 17 (22 s) | 63 (95 s) | 31 (43 s) | 65 (150 s) | 27 (65 s) |
| All 201 | 69 | 51 | 161 | 118 | 149 | 104 |

The loop beats today's agent on every model. The 9B is not better than the 4B: it is better on the wide held-out
tasks and worse on the short ones, with fewer moves (3 against 5 on the held-out 70), so it ends before it has
looked enough; the loop was tuned on the 4B. The 2B is far below on both paths: agent mode is not for it as it
stands. Why each one fails is the next measure: every call of a failed task, the route and a kind of failure are
now in the reports (`EvalTrace`, `EvalTraceReport`).

### Why the loop fails, read call by call — 2026-11-15

Every task the loop failed on 2026-11-14 was run again with every call in its report (`EvalTrace`): 40 on the 4B,
52 on the 9B, 132 on the 2B (3, 1 and 0 of them came out right this time). Each failure is given a kind
(`EvalTraceReport`; the first that fits):

| Kind | 4B | 9B | 2B |
|---|---|---|---|
| Did the wrong thing (wrong notes, wrong tool for the change) | 13 | 25 | 7 |
| Ran out of steps or repeated a call | 14 | 10 | 108 |
| Ended the wrong way (changed or answered where it should ask) | 5 | 12 | 5 |
| Never found what the answer needs | 3 | 3 | 3 |
| Routed to work that cannot end that way | 1 | 1 | 5 |
| Stopped | 1 | 0 | 3 |
| Found it, then did not say it | 0 | 0 | 1 |

What the calls show, by what would fix the most:

1. **The 9B routes "add a task to X" to *write*** (10 tasks): *write* has `append_text`, not `add_task`, so it
   appends a line and no task is added. A router rule (a task, tag, property or link added to a note is
   *organize*) fixes all of them.
2. **A note with content made with `create_note`** (9B 7, 4B 1): an empty note is made and nothing written.
   `create_note` should refuse when the task says what the note holds, pointing to `write_note`.
3. **Changes where the task should be asked back** (4B 5, 9B 10): "call Laura", "the trip", "the contract", "my
   ideas note" when two notes could be meant. A check before `propose` and `answer`, like the one for notes that
   share a name: two or more notes match what the task names and none was read → ask.
4. **The wrong set of notes in a change to many** (about 8 on each model): the list note tagged along with the
   books it lists, every recipe moved for "my Venezuelan recipes", a search for "the notes that mention the
   garden" asked about instead of done. `propose` should list the notes back once with what each was chosen for,
   so the model drops the ones it cannot justify; the rules say the note holding a list is not one of its items.
5. **A tool that hides what the task needs:** `tasks` shows "this week" and "later", not the due date, so "my open
   tasks due in January 2027" cannot be answered by either model. It should show the date and take a range.
6. **The wrong change tool in *organize*** (4B 2): `complete_task` called to add a task or set a status. Their
   descriptions should say what each is not for.
7. **The 2B repeats itself** (108 of its 132): the loop refuses the same call and the 2B makes it again until the
   turn ends. No prompt fixes that: the loop should not be offered on the 2B, which would keep today's path.

Items 1, 2, 5 and 6 are changes to the router and the tools, checked by the trace on the tasks above; 3 and 4 are
the first pieces of step 7's verifier.

**Items 1, 2, 5 and 6, done (2026-11-15):** the router's rule (a task, tag, property or link added to a note is
*organize*) and *write* given `add_task` and `date`, for when it is sent there anyway; `ChangeMisuse`, which turns
down once `create_note` for a note the task says what to hold, `append` for a task, and `complete_task` for a
status or a task added, saying which tool to call; the descriptions of the change tools say what each is not for;
`tasks` gives each task's date (`FoundRow.date`) and takes `from` and `to`.

Measured on the same four sets the same day (201 tasks; the 30, 21, 70 and 80):

| Model | Before | With 1, 2, 5, 6 | By set | Router |
|---|---|---|---|---|
| 4B (medium) | 161 (80 %) | 158 (79 %) | 28, 17, 51, 62 | 111 → 111 |
| 9B (large) | 149 (74 %) | 159 (79 %) | 27, 17, 54, 61 | 113 → 116 |
| 2B (small) | 69 (34 %) | 78 (39 %) | 12, 10, 27, 29 | 83 → 79 |

| Failures by kind | 4B before → after | 9B before → after | 2B before → after |
|---|---|---|---|
| did the wrong thing | 13 → 13 | 25 → 13 | 7 → 8 |
| ran out of steps or repeated itself | 14 → 17 | 10 → 10 | 108 → 104 |
| ended the wrong way | 5 → 8 | 12 → 13 | 5 → 6 |
| never found it | 3 → 4 | 3 → 5 | 3 → 1 |

The fixes did what they were for on the 9B, the model they were mostly drawn from: half its wrong changes are gone,
and it now ties the 4B. The 4B lost 3, inside the ±2 % two runs of the same set differ by; its new failures are
repeats and wrong endings, not the misuses the fixes turn down. The 2B gained 9 but still fails by repeating
itself, as item 7 said. What is left on the 4B and 9B is items 3 and 4 (wrong endings, wrong note sets): step 7's
verifier.

**Items 3 and 4, the verifier's first checks (2026-11-15):** before a turn ends, two checks the app makes with no
model call.

- **A task that can mean several notes is asked back** (`AmbiguousTask`, `UnsettledEnd`): two notes of one name the
  task says whole ("my 2027 goals"), a first name two people have and nothing else in the task tells apart
  ("Laura"), or "the X" said bare where several notes are an X ("the budget", "the contract", "the supplier
  meeting"). An answer, a card or a note shown that picks one of them is held once, and the next call may only be
  `ask_user`; an end that covers all of them goes through. Asking among them is not held back to read them first.
  On the evaluation's tasks with no model (`AmbiguousTaskEvalTest`) it holds 11 of the 17 that should ask and none
  of the 184 others. Those tasks are the ones it was written against, so the gain it brings is measured here
  rather than taken as general.
- **A change to several notes is listed back once** (`ProposalReview`): the first `propose` whose changes act on two
  notes or more is held, each note listed with its properties and open tasks, and the model told that a note
  listing the others is not one of them; `propose` takes `drop`, the notes to take out.

Measured on the same four sets the same day (201 tasks; the 30, 21, 70 and 80):

| Model | With 1, 2, 5, 6 | With the verifier | By set | Router |
|---|---|---|---|---|
| 4B (medium) | 158 (79 %) | 165 (82 %) | 26, 18, 54, 67 | 111 → 111 |
| 9B (large) | 159 (79 %) | 166 (83 %) | 27, 18, 54, 67 | 116 → 116 |
| 2B (small) | 78 (39 %) | 81 (40 %) | 11, 10, 26, 34 | 79 → 79 |

| Failures by kind | 4B before → after | 9B before → after | 2B before → after |
|---|---|---|---|
| did the wrong thing | 13 → 10 | 13 → 13 | 8 → 5 |
| ran out of steps or repeated itself | 17 → 19 | 10 → 11 | 104 → 105 |
| ended the wrong way | 8 → 2 | 13 → 5 | 6 → 4 |
| never found it | 4 → 4 | 5 → 5 | 1 → 1 |

The question back did what it was for: wrong endings went from 8 to 2 on the 4B and from 13 to 5 on the 9B, most of
the 7 tasks each gained. The list back before a change to many did little: the 9B's wrong changes stayed at 13 and
the 4B's went from 13 to 10, inside what two runs differ by; those failures need reading one by one before the
check is kept or taken out. The 2B is unchanged in kind. The detector was written against these tasks, so tasks it
has not seen say how much of the gain holds.

**On tasks it was not written against (2026-11-15):** 24 new tasks over the same vault (`AgentUnseenCases`), written
before the checks were run on them: 12 that can mean several notes, in words the tuned tasks do not use, and 12 that
look like them and should not ask (a name told apart by what follows it or by what the notes hold, "both of my
Ideas notes"). With no model the detector holds 9 of the 12 that ask and 2 of the 12 that do not ("Tomás from the
chess club", "my Sunday weekly review"). Measured without the verifier (at `5f662a2c`) and with it:

| Model | Without | With | Asking right (12) | Not asking right (12) |
|---|---|---|---|---|
| 4B (medium) | 19 (79 %) | 21 (87 %) | 8 → 12 | 11 → 9 |
| 9B (large) | 16 (66 %) | 19 (79 %) | 6 → 8 | 10 → 11 |

The gain holds, smaller than on the tuned tasks, and the two wrong holds cost what was expected: the 4B lost both,
the 9B the second. The 9B's misses that remain are not the detector's: it answered with both Lauras' numbers, which
an end covering all the notes is let through for, and summed both budgets; "my goals" and "the presupuesto" are not
held at all (a plural title word, a Spanish word for an English title).

**Item 7, done (2026-11-15):** with the small model the chat keeps today's agent even when the loop is turned on in
Settings (`ChatLoop.isOn`), and the setting's hint says so.

## Risks, said plainly

- **The local model bounds it.** A 4B model will not reason like Claude. The loop makes it honest, grounded and
  consistent; it does not make it smarter. A larger context and the model's own tool format may help; step 0 says.
- **More calls, more time.** A task that took one plan call may take four. Time is a gate of every step, and the
  budget's trimming in blocks is there for it.
- **One loop may route worse than the planner** on the requests its regexes were tuned for. Step 0's fixtures show
  where; the router keeps the rule where it does.
- **Size.** Steps 3, 5 and 6 touch most of `chat/`. They are done behind the same `ChatViewModel` intents, and the
  old path stays switchable until step 6, so there is always a working chat to fall back to.

## Decided with the developer (2026-11-14)

- **The marks the loop must reach before it takes over** (2026-11-14): at least 19 of the 21 wider tasks, and
  more than today's 27 of agent mode's 30.
- **Accuracy before time** (2026-11-14): the loop replaces today's path in step 6 when it is right more often,
  even at two to three times the time; the time is brought down afterwards.
- **The held-out tasks** (2026-11-14): the ~150 tasks written after the loop was tuned only measure it. If it
  falls short on them, half may be used to tune and the other half stays unseen, to keep the final figure honest.
- **The ten-tool limit of a toolset goes** (2026-11-14, on seeing step 5's first measures): "hagamos lo que
  funcione mejor; si más herramientas funciona mejor, que así sea". A toolset holds what its work needs, and the
  evaluation sets decide its size.
- **Looking things up on the web, the next pull request after this one** (2026-11-14): besides a link the person
  writes, the agent may search the web, as a tool of the loop (`web_search`) and, when the notes do not hold the
  answer, by asking first ("It is not in your notes: shall I look it up on the web?", with buttons). Plug and play:
  **it works with nothing to set up**, through DuckDuckGo's HTML results, marked in Settings as "may stop working",
  with an honest user agent (Notekea's own, never a browser's), a pause of 2 to 3 seconds between requests behind a
  mutex, and no aggressive retries. **Brave Search with a free key** the person pastes in Settings is the stable
  option. When DuckDuckGo blocks or fails, the chat says so, suggests a Brave key and explains how to get one.
  Self-hosting SearXNG is not offered: asking a person to run Docker is the opposite of plug and play. Built on Ktor
  and Ksoup, behind a `WebSearchProvider` in the domain. The query is shown before it is sent and never carries the
  notes' text; pages found are data, cited with their link, and any change still waits for the card. It starts as
  one search and two or three pages read, answered with their links; searches that follow each other (deep
  research) come once the larger model is measured. DuckDuckGo's terms (read 2026-11-14) say nothing of automated
  queries, but forbid showing "any portion of the services within another service" and reserve cutting access off:
  hence the label, and Brave beside it.

1. **The commit hook** runs ktlint and the unit and architecture tests (about two minutes); the real-app suite
   stays in CI and on demand. Done.
2. **The chat history kept on disk changes shape** in step 3, the old history converted once.
3. **The vault's instructions and the person's own skills**: yes to both, in step 4.
4. **The repair of a repository left half merged** comes after step 1.
5. **A loop of our own, not Koog**: confirmed after reading the comparison above.
6. **The modes go**, on the condition above.
