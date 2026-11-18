---
tags: [adr, ledger]
status: accepted
date: 2026-06-03
---
# ADR-012 Event sourcing for ledger

## Context
Mutable balance rows make reconciliation hard to audit.

## Decision
The ledger stores immutable entries; balances are projections rebuilt from entries. Snapshots every 10000 entries per
account keep reads fast.

## Consequences
- Backfill of historical data is required ([[Ledger Migration]]).
- Storage grows about 3x; acceptable.
