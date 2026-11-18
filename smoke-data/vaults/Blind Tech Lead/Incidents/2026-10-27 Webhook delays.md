---
tags: [postmortem, incident]
date: 2026-10-27
severity: SEV2
status: published
---
# Postmortem: Webhook delays (2026-10-27)

- Severity: SEV2
- Duration: 47 minutes
- On-call: [[Priya Raman]]

## Summary
Merchant webhooks for payout events were delayed by up to 40 minutes.

## Root cause
Connection pool exhaustion in the webhook dispatcher after a slow merchant endpoint held connections open.

## Action items
- [x] Raise the dispatcher pool size and add per-endpoint limits — Omar Haddad
- [ ] Add queue depth alert on the webhook queue — Omar Haddad
- [ ] Add retry jitter to the webhook backfill job — Jonas Weller

#postmortem
