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
