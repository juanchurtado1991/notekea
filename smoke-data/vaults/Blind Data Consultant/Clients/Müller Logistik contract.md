---
tags: [contract]
status: signed
signed: 2026-08-14
client: "[[Müller Logistik]]"
---
# Müller Logistik contract

My summary of the service agreement (Dienstleistungsvertrag) with Müller Logistik GmbH, signed 2026-08-14.
The signed PDF lives in the contracts folder on the NAS; this is the version I actually read when I need
something. Contact for contract questions on their side: [[Jonas Weber]], legal goes through their external
counsel.

## 1. Parties and scope
- Client: Müller Logistik GmbH, Berlin-Spandau.
- Contractor: Mara Lindgren, freelance data scientist, Berlin.
- Scope: design, build and hand over a weekly route demand forecast, including a production pipeline that
  runs on the client's own infrastructure. Milestones M1 to M4 as in the project plan.
- Out of scope: driver scheduling software, changes to their ERP, on-call support after handover.

## 2. Term
- Starts 2026-08-17, ends with the acceptance of M4, at the latest 2027-03-31.
- Can be extended by written agreement (email is enough) in blocks of three months.

## 3. Fees
- Day rate as agreed in [[Rates]]; one day is eight hours, partial days are billed in half days.
- No fixed price. Estimate in the offer was 42 days in total, not binding.
- Rate is fixed until 2027-03-31.

## 4. Invoicing and payment
- Invoice at the end of each milestone, plus a monthly invoice for work in between if it is more than
  three days.
- Payment within 14 days of the invoice date, bank transfer.
- Invoices go to their accounts department with Jonas Weber in copy.

## 5. Working mode
- Mostly remote. Two on-site days a month at most unless agreed otherwise.
- Access to the Postgres replica through their VPN; my own laptop is allowed if the disk is encrypted.
- Weekly status mail on Fridays.

## 6. Confidentiality
- Everything about routes, customers and volumes is confidential, also after the contract ends, for five
  years.
- I may name Müller Logistik as a client on my website, but not show numbers or route maps.
- The conference talk may use the method, not their data. (That is why the talk demo uses a public dataset.)

## 7. Intellectual property
- The client gets the rights to the code written for them once the invoice for the milestone that contains it
  is paid.
- My own general-purpose helpers (the forecasting utilities I bring to every project) stay mine; the client
  gets a free licence to use them inside the delivered pipeline.

## 8. Liability
- Limited to the fees paid in the twelve months before the damage, except for intent and gross negligence.
- I keep my professional liability insurance for the whole term (see [[Finance/Budget]]).

## 9. Termination
- Either side can end the contract with four weeks' notice to the end of a month.
- Work done until then is billed as usual.
- For cause: immediately, in writing.

## 10. Travel and expenses
- Travel time to Hamburg counts as half working time.
- Train tickets (2nd class) are reimbursed at cost, against the receipt, on the next invoice.
- If I drive to the Hamburg depot instead, mileage is reimbursed at 0.30 EUR per km; parking at cost.
- Hotel only when the client asks for two days in a row on site, up to 120 EUR per night.
- No reimbursement for meals.

## 11. Small print I wanted to remember
- German law, place of jurisdiction Berlin.
- Changes to the contract need text form, email is fine.
- If one clause is void, the rest still applies.

## My notes
- The 14-day payment term is shorter than with [[Müller Kliniken]]; Logistik usually pays on time anyway.
- Follow-up talk with Jonas planned for January about a possible phase two (driver capacity).

#contract
