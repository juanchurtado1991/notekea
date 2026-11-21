---
tags: [adr, idempotency]
status: accepted
date: 2026-09-25
---
# ADR-013 Idempotency keys

## Context
Follow-up of the September duplicate payouts incident: keys expired before delayed retries ran.

## Decision
- Idempotency keys for money-moving requests live for 7 days.
- A retry must reuse the original key and must look up the payout table before sending.
- Gateway timeouts are recorded as unknown outcomes, never as failures.

## Consequences
- Key store grows; estimated 18 GB, fine for the current cluster.
- Implementation owned by Jonas Weller.

#idempotency
