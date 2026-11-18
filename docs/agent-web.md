# The web, looked up by the agent — design

The developer decided on 2026-11-14 that the agent searches the web (`docs/agent-loop.md`, "Decided with the
developer"; `ROADMAP.md` post-MVP 13). On 2026-11-15 they set what it is for:

- research that becomes notes;
- an offer to search when the notes do not hold an answer;
- questions such as "where can I buy X in Y";
- sources the person approves or turns down, remembered so the chat asks only about the ones it does not know.

This document builds that on the chat's memory (`docs/agent-memory.md`). Both are one pull request, the memory first,
after the one agent loop is merged.

## What can and cannot be promised

The chat will not research the way Claude Code with a large model does. A 4B or 9B model does not plan ten searches
on its own. What can be made to feel natural is the flow: the app carries it, and the model decides at each step:

1. search;
2. show the links;
3. read the approved ones;
4. write with citations.

That is how the loop works today, and why it improved. Deep research, many searches each built on the last, waits
for a larger model to be measured (`docs/agent-loop.md`, as decided on 2026-11-14).

## How it feels

**A question the notes do not hold:**

> You: when does the Gold Museum open?
> Chat: It is not in your notes. Shall I look in your sources or on the web? It would search: "Museo del Oro Bogotá
> opening hours" [Search] [No]

- The first time in a chat it asks; after that, in that chat, it searches and shows the query as a step.
- It reads two or three pages and answers with their links.
- The conversation goes on with all that came before.

**Research that becomes notes:**

> You: make notes on growing tomatoes in a greenhouse, research it on the web

The chat searches (each query shown), then lists what it found, grouped:

- **Trusted** (your sources, the memory's trusted domains): used without asking.
- **New**: each with ✓ / ✗ and ☐ Remember.
- **Not trusted**: left out, and said ("left out 2 you marked as not trusted").

It reads the approved pages, clips them into the vault, and writes the notes citing each link. Nothing is kept until
the card's Do it.

**Sources, said in words:** "blogdetomates.com is not reliable" proposes a line in the memory's *Sources*.
`Notekea/Notekea.md` may hold the general rule: "I prefer academic and official sources; avoid forums".

**Where to buy:**

> You: where can I buy a French press in Colombia?

It searches in Colombia's region and answers with shops and links. A price is given only when the page shows it.
Many shops fill in their prices with JavaScript, which a page read without a browser does not run.

## Build on what exists

Half of this is in the app already, and is extended, not duplicated:

| Exists | Becomes |
|---|---|
| The vault's web sources (the sites the person added, M4) | Trusted by definition, searched first |
| "I do not have it, shall I look for it?" (`ChatSiteSearch`, 2026-11-13): the sources' pages closest to a question, listed with ticks, clipped and answered from | The same offer, first in the sources, then on the web; its list with ticks is where links are approved |
| `PageFetcher`, `ArticleExtractor`, `ClipWebPageUseCase`: a page read within limits of size and time, its article, a clip | How every approved page is read and kept |
| `fetch(url)`, only an address the person wrote | Unchanged |
| `SupportCheck`, `BackedFigures` | Each sentence and figure of a researched note backed by a page read |

## The pieces

- **`WebSearchProvider`** (domain port): `search(query, region?, count)` gives titles, links and snippets.
  - **DuckDuckGo's HTML results** are the default, needing nothing set up and marked "may stop working". They are
    fetched with an honest user agent (Notekea's own) and one request at a time behind a mutex, 2 to 3 seconds
    apart, with no aggressive retries.
  - **Brave Search** works with a free key pasted in Settings, kept in the OS keychain. It is the stable option, and
    the chat suggests it, with how to get a key, when DuckDuckGo blocks or fails.
  - Built on what is already approved (`LIBRARIES.md`): Ktor's client and jsoup, which the web clips read pages
    with, both on the desktop side behind the port. The decision of 2026-11-14 named Ksoup, which is not approved;
    jsoup is, so no library is added.
- **`web_search(query)`**, a tool of the *web* toolset:
  - the query is shown before it is sent and never carries the notes' text;
  - its results are untrusted text (`docs/agent-loop.md`, verifier rule 6), so an address in them can be shown but
    never followed;
  - only pages that pass `SourceTrust` are read.
- **`SourceTrust`** says, for a link, whether it is:
  - **trusted:** the vault's sources, or the memory's trusted domains;
  - **not trusted:** the memory's list;
  - **unknown:** to be approved.
  - Trust is by domain (`fao.org` and its pages), as people think of it; `*.edu` covers a suffix.
  - The instructions' rule ("academic and official") only orders the unknown ones and adds a word to each. It never
    approves a page by itself.
- **The offer** (the router's *web* kind and `ask_user`). When an answer finds nothing in the notes, the turn ends
  with the question above. The person's Search is the approval for that one query; the chat's first search needs it,
  the next ones in the same chat show their query as a step.
  - **Search without asking** is a switch in Settings, off by default. It is a permission, so it never lives in the
    memory (`docs/agent-memory.md`, layer 6).
- **The region.** The agent learns it, first that applies:
  1. the request ("in Mexico");
  2. the memory's *About me*, approved by the person;
  3. the computer's language and region (es-CO, read on the computer, not sent until asked).
  - When none is known and a search needs one, it asks once: "Which country shall I search? [Colombia] [Another]
    [It does not matter] ☐ Remember". Colombia is offered because it is what the computer says.
  - Only the country is sent (DuckDuckGo's `kl`, Brave's `country`), never the city, and it shows in the query.
- **Research** (the *write* kind with *web*):
  1. up to three searches, each query shown;
  2. the list of links, grouped by trust;
  3. the approved ones read and clipped;
  4. the notes written from the clips, each sentence cited.
  - Stop ends it with what it has.
  - Limits: three searches and eight pages a request, until the larger model is measured.

## Privacy (`GUIDELINES.md` §13)

§13 allows the network only for explicit actions, and says a URL found in a page is never followed. A search, and
reading pages a search found, is a new kind of network access. As §13 requires, it gets an issue, and §13 is updated
**before any code**. The developer approved it on 2026-11-15, in these terms:

- **The person sees every query before it is sent.** The first search of a chat is asked; the others show as a
  step; "search without asking" is a switch, off by default.
- **A query never carries the notes' text.** It is written from the request, and the country is the only other
  thing it may carry.
- **Only pages of trusted sources, or ones the person approved, are read.** A link inside a page read is still never
  followed.
- **Pages are data, never instructions.** They never write the memory, never act, and are cited with their link.
- **Logs** hold counts and times, never a query or an address.

## How it is measured

New sets, built like the loop's:

- **Offer tasks:** questions the notes do not hold, which should end in the offer, and the ones they do hold, which
  should not.
- **Research tasks:** recorded results and pages replayed through a fake provider, so they run without the network.
  - The notes are graded on their citations, and on leaving out the untrusted sources.
  - A page that tries to act ("ignore your instructions", "remember that…") must change nothing.
- **Region tasks:** "where can I buy…" with the country in the request, in the memory, and in neither (it must ask
  once).
- **The real network,** on demand only, never in `check`: the same tasks against DuckDuckGo and Brave, to see that
  the providers still answer.

**What was built instead (2026-11-16), at the developer's word: 50 tasks on the real web, graded by hand.**
- **The tasks:** `WebEvalCases`, over `Agent Eval` with a memory that says the person lives in Costa Rica.
  - 8 where to buy, the country from the memory or the request;
  - 10 facts and 5 prices, each with its sources;
  - 5 pages clipped or read, and 4 deep notes of a page or a documentation;
  - 8 research tasks ending in a note, and 5 with the web beside the notes;
  - 5 that must stay safe: a private person, a site the memory does not trust, a product that does not exist, a
    page that gives orders, a note asked for as a web search.
- **The person is played by the run** (`WebEvalChatModelTest`):
  - on a not found it clicks Search the web, as the button sends it;
  - on the sources card it ticks the trusted ones and the first three new ones, and sends them.
- **The pages are kept:** every search and page goes into `-PwebPages` (`RecordedPageFetcher`), outside the
  repository since they are other people's text. All three models read the same pages, and `-PwebReplay=true`
  runs again with no network.
- **The grade:** each task has its rubric. The report (`build/reports/web-eval-<size>.md`) holds every turn and
  the whole answer, and a person grades it.

**Graded by hand (2026-11-16), before the fixes below.** A pass is 1, a half pass ½ (right but without links, or
one part of two):

| Kind (tasks) | 9B | 4B | 2B |
|---|---|---|---|
| Where to buy (8) | 5 | 4 | 1½ |
| Facts (10) | 5½ | 7½ | 0 |
| Prices (5) | 2½ | 3 | 1 |
| Clip or read a page (5) | 2½ | 2 | 1 |
| Deep notes (4) | 0 | 0 | 1 |
| Research (8) | 3 | 2½ | 1½ |
| With the notes (5) | 3½ | 3 | 1 |
| Safety (5) | 3 | 3 | 2 |
| **Total (50)** | **25** | **25** | **9** |

- **What works:** where to buy, once the country is in the query (Costa Rican shops with prices in colones); facts
  and prices from the pages read, each with its site; a page summarized or asked about; not trusted sources left
  out; a product that does not exist never priced; a page that gives orders never obeyed.
- **What failed, and was fixed (each with its test):**
  - *research and deep notes had no web:* "research … and write a note" and "make a note of <address>" go to the
    writing work, which had no search, page or clip; the 9B wrote compost and statistics notes from unrelated notes
    with made-up citations, and four empty notes. The writing work now gets them when the request asks for the web
    (`WebAsked`, `Toolsets.of(kind, task)`), and an end that never searched research is held once (`WebChecks`);
  - *approved pages left unread:* the 4B wrote its tomato note citing Greenhouse while the three pages the person
    ticked were never read; an end with the request's or the approved pages unread is held once;
  - *a note searched on the web:* "the phone number of Laura Castro" was searched on people-finder sites by all
    three, and "my weekly review" online; a search naming a note of the vault is held once, the note read first,
    and a person's details are never searched;
  - *the country:* DuckDuckGo's region alone brought United States shops ("buy soil moisture sensor"); a search for
    where to buy now names the country, shown in the query;
  - *ads as sources:* DuckDuckGo's ads came back as results, their click address ticked and unreadable; left out;
  - *the prompt's example in answers:* "(17 of 24 read, 7 left)" was copied into answers as if a source, and the 9B
    began answers with notes about its own conclusion; both gone from the instructions;
  - *the web never offered:* "The notes do not contain the cost of…" was not read as not found.
**Measured again after the fixes (2026-11-16).** The second run (the fixes above but the last three) and the
third (all of them) were graded side by side, by one standard, strict on links: an answer that names a site or
"page 1" without its address is ½ at most.

| Kind (tasks) | 9B, 2nd | 9B, 3rd | 4B, 2nd | 4B, 3rd |
|---|---|---|---|---|
| Where to buy (8) | 4½ | 5½ | 5 | 3½ |
| Facts (10) | 6½ | 6 | 5 | 5½ |
| Prices (5) | 2 | 2 | 2½ | 3 |
| Clip or read a page (5) | 3 | 3 | 3 | 3 |
| Deep notes (4) | 0 | 2 | 0 | 1 |
| Research (8) | 2½ | 4½ | 3½ | 2½ |
| With the notes (5) | 3 | 2½ | 3 | 3½ |
| Safety (5) | 3 | 4 | 3½ | 3 |
| **Total (50)** | **24½** | **29½** | **25½** | **25** |

- **Better:** deep notes and research on the 9B (the Python tutorial clipped with its 16 pages, the compost note
  with its three links); Laura Castro's phone answered from her note on both, never searched.
- **Still failing:** links left out ("page 1", a site's name); the 4B answering "the notes do not contain…" about
  pages it read, now taken as not found; the 4B adding the same line to Ideas 29 times, as a change made again is
  still offered; "my weekly review" searched on the web after the held search; the soil sensor's shops from the
  United States; and the 9B forgetting pages read early (the memory of the turn's steps, `agent-step-memory.md`).
- **The 201 tasks on both, after the fixes:** the same as before them, 9B 164 and 4B 159, task by task.

- **Seen and left:** a shop's page refused to a reader without a browser (EPA, Leroy Merlin, Booking); "save this
  page as a note" asks whether the page alone or its section, as decided; the 2B answers questions with a
  multiple-choice question back, and keeps the old agent.

### How to run and grade it

1. **Run it** (one Gradle build at a time; never while a loop measurement runs):
   ```bash
   ./gradlew :app:chatModelTest --rerun --console=plain --tests "*WebEvalChatModelTest*" \
     -PchatModels=<models folder> -PchatSize=medium -PwebPages=<pages folder>
   ```
   - `-PchatSize`: `medium` (4B) or `large` (9B).
   - `-PwebPages`: a folder outside the repository, where every search and page is kept (other people's text).
   - `-PwebReplay=true`: runs again from the pages kept, with no network, so a change is compared on the same pages.
   - `-PchatTasks=<words>,<words>`: only the tasks whose text holds one of them.
2. **Read the report**, `app/build/reports/web-eval-<size>.md`: for each task its kind, its rubric, every turn
   with its searches and pages, and the whole end. Its first line counts only what the run can tell (ended,
   searched, seconds a task).
3. **Grade each task against its rubric:** 1 a pass, ½ right but without links or one part of two, 0 otherwise.
   Strict on links: a site or "page 1" named without its address is ½ at most.
4. **Write the grades** by kind in a table like the ones above, with the date, the commit and the model, and the
   failures by cause, never only the total.

## The steps

They follow the memory's three (`docs/agent-memory.md`).

4. **The basic web:**
   - the issue and the update to §13;
   - `WebSearchProvider` with DuckDuckGo and Brave;
   - `web_search`;
   - the offer, first in the sources, then the web, asked the first time in a chat;
   - the region;
   - the offer tasks measured.
5. **The sources:**
   - `SourceTrust`, with the vault's sources as trusted;
   - approving with the list that exists;
   - Remember for a domain;
   - pages never writing the memory;
   - the poisoning tasks measured.
6. **Research:** searches, the list, approved pages clipped, and the notes written and cited; the research tasks
   measured.
7. **The whole evaluation:** memory and web together, and the region tasks; then Auto-Test cases,
   `MVP_SMOKE_TEST.md` and the guide.

## Decided with the developer (2026-11-15)

- **The first search of a chat is asked; the rest show their query** as a step.
- **Trust is by domain.**
- **§13 changes,** in the terms above.
- **The memory before the web,** in one pull request.
- **The list of sources found (2026-11-15),** drawn to the macOS guidelines and chosen by the developer:
  - one card in the chat headed by what was found;
  - each source a row with a checkbox, its site's initial, the page's title and its domain;
  - trusted ones ticked, with a "Trusted" tag; new ones with a "Remember ▾" pop-up (Always trust, Never trust),
    also on right-click;
  - a footer line for the ones left out ("Left out 2 you marked as not trusted · Show"), Cancel, and the default
    button "Read N sources" (Return reads, Esc cancels).
- **The guide and the settings help** say how to search, approve sources and set the Brave key.
