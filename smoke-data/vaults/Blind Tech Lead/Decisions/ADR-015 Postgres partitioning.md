---
tags: [adr, ledger]
status: proposed
date: 2026-11-04
---
# ADR-015 Postgres partitioning

## Context
Entry tables will pass 2 billion rows within a year of cutover.

## Proposal
Partition entry tables by month. Old partitions move to cheaper storage after 18 months.

## Open points
- Does the snapshot job need to be partition-aware?
