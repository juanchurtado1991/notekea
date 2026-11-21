---
tags: [project, payouts]
status: active
owner: Priya Raman
---
# Payouts v2

Rebuild of merchant payouts on the new payout scheduler, with instant payouts for eligible merchants.
Owner: [[Priya Raman]]. Related: [[Sprint 47]].

## Milestones

| Milestone | Description | Date | Status |
|---|---|---|---|
| M1 Design review | Architecture sign-off with Platform | 2026-09-15 | done |
| M2 Sandbox launch | Sandbox API open to partners | 2026-10-30 | done |
| M3 Beta | Live with 5 pilot merchants | 2026-12-04 | planned |
| M4 GA | General availability | 2027-02-15 | planned |

## Risks
- Pilot merchants need the new webhook signature format (see PAY-388).
- Instant payouts depend on the partner bank's real-time rail being enabled in all regions.

## Notes
- 2026-10-30: sandbox shipped on time, 3 partners already testing.
- 2026-11-05: Priya wants a load test of the scheduler before the beta.

#payouts
