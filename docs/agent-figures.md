# Figures worked out by the app, and a topic read whole

Steps 2 and 3 of the loop's foundations, built on 2026-11-17 after the task's frame (`docs/agent-frame.md`).

## Why

The biggest kind of failure on the vault written blind (`docs/eval-fresh.md`) was a wrong figure or fact said with
confidence: the 9B made 6 such mistakes and the 4B about 16. Some examples:

- **Sums of the wrong lines:** 135 € for a tournament every player pays (180), and 2,630 owed when one client had
  already paid (2,030).
- **Days between two dates:** no tool could count them.
- **A fact in a second note:** "papá is not allergic to any medicine", when the allergy was in the care plan, a
  second note with the same tag.

## Figures: the model picks the lines, the app works them out

- **`rows`** (`RowsTool`, `NoteRows`, 2026-11-20, the plan's step 3.2) numbers a note's rows R1, R2…: a table's
  rows with their cells under the header's names, list items, and `Key: value` lines that write a figure, each under
  its heading. A word keeps the rows about it (a column, a heading, a label).
- **`tally`** (`TallyTool`) takes `how` (sum, average, count, product, min, max or difference), the note, the rows by
  id (or all) and, when a row writes several figures, the column or the word the figure is under. The code reads each
  row's figure (dates and times are none), works the result out, and lists the rows counted and those of the note it
  did not count, so a row left out is seen. It replaced the items the model copied as "figure | line", which dropped
  rows and copied figures wrong (the loop's dev runs, 2026-11-19 and 2026-11-20).
- **`FigureValues`** reads figures as notes write them in English or Spanish style: 1,234.50, 1.234,50, 38,5 and
  2.500 €.
- **`days`** (`DaysTool`) counts the days between two days. Each day can be a date as the notes write it or a phrase
  `DatePhrases` reads, such as "today" or "el 15 de noviembre".
- **`AnswerFigures`** checks the answer. An answer whose figure (two digits or more) is in no tool result and not in
  the task is held back once. The model is told to work it out with a tool from the lines it read, or to leave it
  out.
- **`calculate`** stays for expressions, such as interest over two months. `count` stays for a figure a note writes
  many times.
- **The question toolset** now holds `tally` and `days` too.

## A topic read whole before answering

- **`TopicNotes`.** When `read` reads a note, it also looks at the note's neighbours: the notes it links, and the
  notes that share one of its tags. The ones that write a word of the task are added to the turn (`TurnReads.related`),
  and `read` tells the model they are about the same topic. A tag on more than 25 notes says nothing about the topic,
  so such a tag is ignored.
- **`TopicUnread`.** An answer given while any of those notes is unread is held back once, and the model is told to
  read them, above all before saying the notes do not hold something.

Both are found by the note's own links and tags and by the task's words. No list says what a topic is.
