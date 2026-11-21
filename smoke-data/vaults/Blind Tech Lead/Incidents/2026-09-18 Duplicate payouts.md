---
tags: [postmortem, incident, idempotency]
date: 2026-09-18
severity: SEV1
status: published
---
# Postmortem: Duplicate payouts (2026-09-18)

## Summary
On 2026-09-18 the payout service sent some merchant payouts twice. Duplicates were caught by the partner bank's
daily file check and by merchants writing to support. Impact window from 09:12 to 11:47 UTC.

- Severity: SEV1
- Duration: 2h 35m
- Incident commander: Dana (me)
- Communications: Support lead and [[Priya Raman]]

## Impact
First estimate during the incident was about 300 duplicate payouts. The number was revised several times as the bank
files came in; see the final count further down in the impact analysis.

Merchants affected received the same payout twice. No merchant lost money; Ledgerline did, until the duplicates were
recovered.

## Timeline (UTC)
- 09:12 — Payout batch 3 starts. Retry storm begins after a slow response from the bank gateway.
- 09:31 — First support ticket: merchant reports two identical payouts.
- 09:40 — On-call (Omar) paged by the payout mismatch alert.
- 09:52 — Incident declared, SEV2.
- 10:05 — Raised to SEV1 once the bank confirmed duplicates across many merchants.
- 10:20 — Payout batches paused.
- 10:48 — Cause narrowed to retries carrying expired idempotency keys.
- 11:30 — Fix deployed: retries reuse the original key and check the payout table first.
- 11:47 — Batches resumed; no new duplicates. Incident resolved.

## What went well
- The mismatch alert fired within ten minutes of the first duplicate.
- Pausing batches was a single flag, and the runbook said so.
- Support had a macro ready within the hour.

## What went wrong
- The retry path generated a fresh idempotency key when the original one had expired.
- Idempotency keys had a TTL of 24 hours, but a delayed batch could retry 30 hours later after a weekend backlog.
- The payout mismatch alert pages only one person, and the second responder joined 20 minutes later.
- We had no tool to replay or reverse a payout batch; recovery was manual SQL.

## Root cause
Payout retries are scheduled by the batch runner. When the bank gateway answered slowly, the runner marked the
payouts as failed and rescheduled them. Most rescheduled payouts carried their original idempotency key, but a
payout whose key had already expired got a new key. The bank saw a new key and treated it as a new payout. Both the
original request (which had in fact succeeded) and the retry went through.

The expired keys came from the weekend backlog: batches queued on Saturday were only retried on Monday morning, past
the 24-hour TTL.

## Contributing factors
- The batch runner treats a gateway timeout as a failure, not as an unknown outcome.
- No check against the payout table before a retry.
- The TTL was chosen in 2023 when batches never waited more than a few hours.

## Impact analysis
Bank files for 2026-09-18 and 2026-09-19 were reconciled by Finance over the following week.

- Final count: 412 duplicate payouts across 37 merchants.
- Recovered by reversal with the bank: 389 payouts.
- Recovered by deducting from the merchant's next payout, with consent: 23 payouts.
- Customer support tickets: 64.
- Ledger accounts touched: these later showed up in the [[Ledger Migration]] backfill dry run.

## Action items
- [x] Purge duplicate ledger entries — Priya Raman
- [x] Notify affected merchants — Support lead
- [ ] Extend idempotency key TTL (decision in the idempotency ADR) — Jonas Weller
- [ ] Add a duplicate payout detector before batch submit — Priya Raman
- [ ] Write a runbook for payout replay and reversal — Jonas Weller

## Lessons
Treat timeouts from money-moving partners as unknown outcomes. Look up before you retry. And keys that guard money
must outlive the longest possible queue, not the usual one.

#postmortem #idempotency
