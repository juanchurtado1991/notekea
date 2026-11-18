# The task's frame

The first step of every turn of the loop that works on notes, built on 2026-11-17 to replace the loop's word-list
checks. Decided with the developer the same day: foundations, not patches (`AGENTS.md`).

## Why

The loop scored about 88 % on the 259 tasks it was tuned on, but 62 % (4B) and 74 % (9B) on a vault written blind
(`docs/eval-fresh.md`). Much of the gap came from checks that read the task's words with lists and regular
expressions: `AmbiguousTask`, `SingularTask`, `ToldApart`, `SharedNames`, `MemoryChoice` and `SeveralUnread`.

- Each check was written against a failure the developer had seen ("the budget", "Laura's phone", "both of my Goals
  2027 notes").
- On new phrasings they asked when the task was clear, and did not ask when it named one entry of several ("change
  the appointment to 11:00").

## How it works

1. **The code finds the candidates** (`FrameCandidates`, `TurnFrames`). These are:
   - the notes the memory links for the task's words;
   - the open note;
   - the notes whose name or folder shares a word with the task;
   - the notes the hybrid search finds for it;
   - every note sharing a name with one of those.

   That is at most 14 notes. Inside each note, the entries the task's words touch are shown too: list items, tasks
   and table rows, by a word of their own or by the heading of their section. Words are matched by their stem (four
   letters or more, or three with a capital for names). No word list says what the task means.
2. **The model says what the task means** (`TaskFramer`). It answers in one call under a JSON grammar that can only
   name the candidates, by number:
   - `notes`: every shown note the task could mean;
   - `entries`: every shown entry it could mean;
   - `count`: one, several, every or none;
   - `words`: the task's own words for the thing.

   The prompt asks for every note and entry that fits, not the likeliest one. A reply that cannot be read is no
   frame.
3. **The code holds the turn to it** (`TaskFrame`). What it does depends on what the frame says:

   | Frame | What the loop does |
   |---|---|
   | Open: one meant, several fit | An end that does not ask is held, and the next call may only ask, with the frame's options (`UnsettledEnd`) |
   | Settled: one meant, one fits | A question is held once (`TurnChecks`). A change to another note it was shown is held once, and so is an answer from another note while the one meant is unread (`FrameChoice`) |
   | Several | An answer with one of them unread is held once (`SeveralUnread`). A shared name is read as each of them (`SharedNames`, `ChangeTool`) |
   | Every | Never asked which |
   | None, or no frame | Nothing is held back for it |

   The loop's prompt says the frame in one line. A memory line naming a note the frame says the task does not mean
   is not given (`TurnSetup.memoryOf`). The words of an open question become the memory's line once the person
   picks one (`TaskFrame.rememberingOf`).

## What it removed

These checks are gone, with the tests that described their word lists: `AmbiguousTask`, `ToldApart`,
`SingularTask`, `MemoryChoice`, `TurnSetup.apartOf` and `settledOf`. The loop's prompt also lost its rules about
"the" and "it".

The checks that are about the loop's own calls, not the task's words, stay: `UnreadChoice`, `SameNoteQuestion`, the
one-option question and `SharedNames`' message.

`RouteWords`, which corrects the router's kind of work by words, is still a word list. It is a separate step and is
open.

## Cost

The frame is one more model call per turn, of at most 160 tokens of reply. The prompt is about 1,000 tokens with 14
candidates.
