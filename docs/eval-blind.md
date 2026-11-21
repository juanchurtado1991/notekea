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

## Since 2026-11-17: the nine are the development set

The plan to reach the blind targets (`docs/loop-goal-plan.md`) was drawn from these nine exams' failures, read task
by task. So they are no longer blind: from then on they are the **development set**, read task by task like the
tuned sets, and the pool below replaces them as the blind measure.

## The pool

50 exams written and checked blind on 2026-11-17 the same way as the nine (`BlindPool`, vaults
`smoke-data/vaults/Blind *`), each with about 44 tasks, on other kinds of people and other topics.

- **Ten are sealed** and never measured until the final number: AcademiaIdiomas, CareHome, Choir, ClubFutbol,
  Conductor, Expedition, FilmStudent, FishingCharter, Homeschool and TouringBand. Picked at random with the seed
  20261117.
- **The reference:** one commit measured on all 40 unsealed exams with both models
  (`-PchatPool=all`, `scripts/loop-eval/measure.sh reference reference <size>`).
  - **2026-11-18, `fe69455b`:** 4B 1,210 of 1,744 (69.4 %), 9B 1,270 (72.8 %). By cause, and the budget the
    targets leave, in `docs/loop-goal-plan.md`, "The real starting point".
- **A round** measures ten of the 40 picked by a seed (`-PchatPool=sample -PchatPoolSeed=<n>`), at its opening and
  after each change in it. Exams differ a lot in how hard they are, so a sample is compared with the reference on
  the same exams, never with another sample.
  - **Round 1 (seed 1), opened 2026-11-18 at `f3d22656`:** 4B 310 of 436 (71.1 %), +5 over the reference on the
    same exams (+1.1 points: about 70.5 % blind); 9B 326 of 436 (74.8 %), +14 (+3.2 points: about 76.0 % blind).
    Closed 2026-11-20 at `ad27622b` (r1-line, `change_line` kept): 4B 316 (72.5 %), +11 (+2.5 points: about 71.9 %
    blind); 9B 337 (77.3 %), +25 (+5.7 points: about 78.5 % blind).
- **Read by cause only**, with `scripts/loop-eval/buckets.py` and `compare.py`: never task by task.

## Runs

Right answers by set. "Tuned" is the 259 tasks the loop was tuned on. "Blind" is the nine exams of this file, 393
tasks; with Fresh Eval (77) they make 470 tasks never tuned on. Since 2026-11-17 the nine are the development set
and the pool is the blind measure: see `docs/loop-goal-plan.md`, section 7, for the runs after that.

| Run | Commit | 4B tuned | 4B fresh | 4B blind | 4B never tuned on | 9B tuned | 9B fresh | 9B blind | 9B never tuned on |
|---|---|---|---|---|---|---|---|---|---|
| Baseline, before the foundations | `6aea8474` | 228 (88 %) | 53 (68 %) | 261 (66 %) | 314 (67 %) | 234 (90 %) | 58 (75 %) | 277 (70 %) | 335 (71 %) |
| The foundations, first run | `9d319805` | 198 (76 %) | 53 (69 %) | 258 (66 %) | 311 (66 %) | 201 (78 %) | 57 (74 %) | 282 (72 %) | 339 (72 %) |
