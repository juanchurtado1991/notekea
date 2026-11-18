---
tags: [client]
status: active
company: Müller Logistik GmbH
billing: day rate
---
# Müller Logistik

Freight and warehousing company, head office in Berlin-Spandau, main depot in Hamburg-Billbrook.
Main contact: [[Jonas Weber]] (Head of Operations). Contract: [[Müller Logistik contract]].
Not to be confused with [[Müller Kliniken]].

## Project: route demand forecast
Weekly parcel volume forecast per route (about 140 routes) so dispatch can plan drivers two weeks ahead.
Stack: their Postgres replica, Python, LightGBM baseline, Airflow on their side for the production run.

## Milestones
| # | Milestone | Due | Status |
|---|-----------|-----|--------|
| M1 | Data audit | 2026-09-15 | done |
| M2 | Baseline model | 2026-10-20 | done |
| M3 | Production pipeline | 2026-12-04 | in progress |
| M4 | Handover and docs | 2027-01-22 | open |

## Invoices
| Invoice | Date | Amount (EUR) | Status | Paid on |
|---------|------|--------------|--------|---------|
| ML-2026-07 | 2026-09-18 | 7,680 | paid | 2026-10-02 |
| ML-2026-08 | 2026-10-23 | 9,600 | paid | 2026-11-04 |
| ML-2026-09 | 2026-11-06 | 4,320 | unpaid | — |

Day rate, see [[Rates]]. Invoices go to buchhaltung@ (accounts), with Jonas in copy.

## Open tasks
- [ ] Ask Jonas Weber for the October shipment export
- [ ] Draft the Airflow DAG review for M3
- [x] Send ML-2026-09

#client #forecasting
