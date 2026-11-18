# Blind exams

Nine vaults and their tasks, written blind on 2026-11-17 at the developer's request. Together with the Fresh Eval
vault (`docs/eval-fresh.md`), they make ten exams the loop was never tuned on, so its average over many kinds of
users is more reliable than one set's.

| Exam | Vault | Person | Language | Tasks |
|---|---|---|---|---|
| tech-lead | Blind Tech Lead | an engineering tech lead at a fintech | English | 43 |
| grad-student | Blind Grad Student | a marine biology PhD student | English | 44 |
| restaurante | Blind Restaurante | a restaurant and food truck owner in Guadalajara | Spanish (Mexico) | 43 |
| household | Blind Household | a couple managing a home and its money in Toronto | English | 44 |
| familia | Blind Familia | a parent of three in Bogotá | Spanish, a quarter English | 44 |
| freelance-writer | Blind Freelance Writer | a freelance journalist in Dublin | English | 44 |
| health-log | Blind Health Log | a marathon runner back from a knee injury | English | 44 |
| inmuebles | Blind Inmuebles | a letting agent in Madrid | Spanish (Spain) | 44 |
| hobbies | Blind Hobbies | a retired teacher in Lisbon | English | 43 |

Each exam has 28 to 36 notes. Its tasks are:

- **Questions** to answer from the notes. Three of them have no answer there, and each of those has a tempting
  wrong figure in `lacks`.
- **Changes**.
- **Requests that must be asked back**: two notes of one name, two people of one first name, one entry of several
  in a note.
- **Traps that look ambiguous** but whose words settle them.

## How they were written

- **One agent per exam wrote it.** It read nothing of the repository but its own vault and case files: no chat code,
  no other vault, no other evaluation set. It was given only the `AgentWideCase` type and the tools' names.
- **A second, independent agent checked it.** It worked out every expected value again from the notes, checked that
  each ask really is open and each trap really is settled, and fixed what was wrong. One rule was added during the
  checks: a worked-out figure must not appear anywhere in the vault, or a wrong answer quoting a note would pass.
- **`BlindExamsTest` checks the cases on every build.** Every `holds` must be written in its vault or task, every tool
  must be one of the design's, and every exam must have every kind of task and every end.

## How they are used

- **Measured, never tuned on.** They are measured before a change to the loop and after it, with the test
  `blind exams on nine vaults the loop was never tuned on`, and the reports go to `build/reports/blind-<exam>-<size>.md`.
  The test's name keeps them out of `the one loop is measured*`.
- **Failures are read only by kind** (finding, reading, adding up, asking), never task by task to make one pass.
- **Replaced once tuned on.** Once a change has been shaped by them, they no longer measure generalisation, and a new
  blind set replaces them.

## Known weak points of the checks

The checks are substrings, so a right answer worded differently can fail:

- a date the notes write as 2026-12-06, answered as "6 December";
- "4 years" answered as "four years";
- a question that names notes by heading instead of by path.

These weak points are the same before and after a change, so they lower both numbers alike. They do not favour
either side.
