---
tags: [project, ledger]
status: active
owner: Priya Castell
---
# Ledger Migration

Move the double-entry ledger from the legacy `accounts_v1` schema to the event-sourced ledger described in
[[ADR-012 Event sourcing for ledger]]. Owner: [[Priya Castell]].

Target cutover: 2027-01-12.

## Why
The legacy ledger stores balances as mutable rows. Every reconciliation bug of the last two years came from a balance
being updated twice or not at all. The new ledger stores immutable entries and derives balances from them, so a
balance can always be rebuilt and audited. Finance has asked for this since the 2025 audit, and the external auditors
listed it as a recommendation in their management letter.

## Scope
- All merchant balance accounts (about 41000 accounts).
- Fee accounts and reserve accounts.
- Out of scope: card issuing accounts, which stay on the issuing processor's ledger.
- Out of scope: historical statements before 2024; they stay readable from the archive.

## Phases

### Phase 1 — Dual write (done)
Every write goes to both the legacy tables and the new entry tables. Shipped in sprint 45 as LED-180. The dual-write
path is behind the `ledger.dualWrite` flag and has been at 100 % since 2026-10-15.

### Phase 2 — Backfill (in progress)
Historical entries from 2024 onwards are rebuilt into the new tables. The dry run (LED-195) finished in sprint 46 and
found 112 accounts whose rebuilt balance differs from the legacy balance by more than one cent. Most of them are
accounts touched by the September duplicate payouts incident; Finance is checking the rest by hand.

### Phase 3 — Shadow reads
Reads go to both ledgers and the results are compared; the legacy result is still returned. The balance snapshot job
(LED-201) is needed first so the comparison does not hammer the entry tables.

### Phase 4 — Cutover
Reads switch to the new ledger. Legacy tables become read-only, then are dropped after one quarter.

## Dependencies
- Partner bank settlement files must keep working through the switch. The bank team asked for two weeks' notice of
  any change to the settlement file format; we do not plan one.
- Finance's month-end close: no cutover in the last three or first three business days of a month.
- Payouts v2 beta (2026-12-04) must not be moved by this project; the two teams share on-call.
- Reporting team needs the new read views at least one sprint before cutover.

## Team
- [[Priya Castell]] — owner, backfill and cutover plan
- [[Jonas Weller]] — database and replication
- Finance contact: the controller's team, weekly sync on Wednesdays

## Metrics we watch
- Accounts with a balance difference after backfill (target: 0 before shadow reads)
- Write latency p99 with dual write on (currently +6 ms, budget +10 ms)
- Replication lag on the entry tables (target under 2 s)

## Meeting notes

### 2026-10-08
Kick-off of the backfill. Agreed that differences under one cent are rounding in the legacy fee code and can be
ignored. Priya to write the backfill runbook.

### 2026-10-22
Dry run results reviewed. 112 accounts flagged. Jonas found the replication lag spikes to 9 s during the backfill;
we will throttle the backfill to night hours.

### 2026-11-02
Bank call. The partner bank announced a change freeze on their side from 2027-01-04 to 2027-01-22. Our cutover touches
the settlement reconciliation, so we cannot cut over inside their freeze.

## Cutover plan (draft)
1. Freeze ledger-related deploys 48 hours before.
2. Final backfill catch-up during the night.
3. Switch the read flag for 1 % of merchants, then 10 %, then 100 %, one hour apart.
4. Keep dual write on until the rollback window closes.
5. Rollback: flip the read flag back; legacy tables are still written to, so nothing is lost. The rollback window is
   72 hours after the 100 % switch; after that we stop dual write and rollback means a full restore.

## Decisions log
- 2026-09-30: no separate read replica for now; revisit after shadow reads.
- 2026-10-22: backfill throttled to 01:00–05:00 UTC.
- Update 2026-11-02: cutover moved to 2027-01-26 because of the partner bank's change freeze. Communicated to Finance
  and to the Payouts v2 team the same day.

#ledger
