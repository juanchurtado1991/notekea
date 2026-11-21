---
tags: [board, company]
date: 2026-08-20
attendees: [Dana Whitlock, Marcus Lee, Priya Nair]
---
# Board Meeting 2026-08-20

Q3 board meeting at the coworking space, 16:00 to 18:10. Pre-seed board: Dana and Marcus as
directors, Priya Nair as observer.

## 1. Business update (Dana)
- MRR at the end of July: $28,950, up from $18,400 in January.
- 30 paying customers. Logo churn this year is mostly small single-site accounts.
- Hill Country Cold Chain signed an annual prepaid contract in July, our first annual deal.
- Sales cycle for mid-size distributors is still about 70 days from first call to signature.

## 2. Product update (Marcus)
- Dock scheduler v2 shipped 2026-07-28: recurring slots, carrier self-booking links.
- Yard map in beta at two customers. Carriers like the SMS check-in.
- Biggest risk is integrations. Every distributor runs a different WMS; we built one-off
  connectors for three customers and it does not scale.
- Proposal: build a generic EDI layer in Q4. Marcus estimates 10 weeks for one engineer.

## 3. Pricing
Current price list (since 2025):
- Starter: $420 per dock per month, minimum 2 docks.
- Growth: $380 per dock per month from 10 docks.
- Annual prepay: 10% discount.

Problems we see:
- Customers hide docks to pay less; one account books 14 docks through 6 paid ones.
- Per-dock pricing punishes the customers who get the most value (large yards).
- Sales calls get stuck counting docks instead of talking about detention savings.

Options discussed:
1. Keep per dock and police usage. Rejected: bad for trust.
2. Per truck booked (usage based). Interesting, but revenue swings with the season and makes
   forecasting hard right before a raise.
3. Per site, flat, with a soft cap on docks. Simple to explain and to forecast.

Priya pushed for option 3 and suggested we grandfather current customers for six months.

## 4. Fundraising plan
- Start the seed process in late September, target $3,000,000.
- Aim for a lead by the end of October. Warm intros from Priya and the Austin founders dinner.
- Data room ready by 2026-09-15.

## 5. Hiring
- One Account Executive before the round closes if cash allows; everyone else after closing.
- Marcus wants a senior backend engineer for the EDI work first. Agreed in principle, after the
  round.

## 6. Decisions
- **Pricing:** move from per-dock to per-site pricing for all new customers from 2026-10-01. New
  list price $1,150 per site per month, up to 12 docks per site; $60 per extra dock.
- Existing customers keep their current price until 2027-04-01, then move to per-site.
- Build the EDI layer in Q4 2026 (Marcus owns it).
- One AE hire approved, budget up to $160,000 OTE.

## 7. Action items
- [x] Dana: update the pricing page and order forms by 2026-09-30
- [x] Dana: data room ready by 2026-09-15
- [ ] Marcus: EDI layer spec
- [x] Priya: three warm intros to seed funds

Next meeting: mid-October.
