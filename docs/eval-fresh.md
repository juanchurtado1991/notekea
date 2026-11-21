# Fresh evaluation set

`smoke-data/vaults/Fresh Eval/` (47 notes) and `AgentFreshCases` (79 tasks), measured by the test
`the one loop is measured on a fresh vault it was never tuned on` (report `loop-fresh`).

- **Who the vault is:** Inés Alvarado, a bilingual (English/Spanish) freelance illustrator in Málaga who
  runs a small print shop (Tinta & Higo), rents a studio under a long Spanish contract, coaches a girls'
  under-12 football team, looks after her father Ramón with a carer, learns Portuguese, keeps an
  allotment and a bike-touring log.
- **Tasks:** 39 questions answered from the notes (`AgentFreshAnswers`, three of them with no answer in
  the notes), 28 changes (`AgentFreshChanges`), 12 requests that need asking (`AgentFreshAsks`); six of
  the answers and changes look ambiguous but the words settle them.
- **Written blind on 2026-11-17**, without reading the agent's code or the other evaluation sets. It is not
  to be tuned on: when a task fails, read the failure for its kind (finding, reading, adding up, asking)
  and fix that kind; never write to these tasks or this vault. Once it has been used to tune, it no
  longer measures generalisation: replace it with a new blind vault and set.
- The eval's day is 2026-11-11 (`AgentEvalSetup`): "how many days until" answers count from it.

## First run (2026-11-17, commit `54991f4d`)

Both models, 77 tasks: **4B 48 (62 %), 9B 57 (74 %)**. On the tuned sets the same commit got about 88 %: most of the
gap is what the tuning did not carry over. The 9B's 20 failures, read one by one:

| Kind | 9B | Examples |
|---|---|---|
| A wrong figure or fact said with confidence | 6 | 8 league points (10); 135 € for the tournament (180, every player on the roster); 2,630 owed (2,030, a paid client added); "papá is not allergic to any medicine" (penicillin, in the care plan) |
| A question the loop's checks forced where the task was clear | 4 | "the studio's deposit" asked between the studio's contract and a client named Studio Norte; "what I owe Lucía for October" asked after reading the carer's hours |
| Did not ask where the task names one of several entries of a note | 6 | "change the appointment to 11:00", "mark the pending order as shipped" |
| A change to more notes than the task asks | 2 | notes that already had `season` |
| A tool that cannot do it | 1 | days between two dates: `calculate` does not count them |
| Other | 1 | a tag made of the task's words |

The 4B adds about ten more wrong figures (a trip budget of 718 € for 676, 7 kg of tomatoes for 13, 3 pills a day
for 5, "Martina, 2 goals" for "Irene, 4"). None of these was tuned on, and none is to be: the next changes are
measured on this set as it is, and a new blind set is written once they have been.
