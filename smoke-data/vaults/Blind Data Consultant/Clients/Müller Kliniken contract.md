---
tags: [contract, health]
status: signed
signed: 2026-08-28
client: "[[Müller Kliniken]]"
---
# Müller Kliniken contract

Summary of the consulting agreement with Müller Kliniken gGmbH (signed 2026-08-28) plus the data processing
agreement that came with it. Their legal department sent a 19-page PDF; these are the parts that matter for
my day-to-day work. Questions about data go to [[Jonas Albrecht]], everything else to Dr. Sabine Korte.

## 1. Scope
- Build a bed occupancy dashboard for the three sites, with a 7-day outlook per ward.
- Train two people from medical controlling to maintain it.
- Not included: anything that writes back into the hospital information system.

## 2. Term
- From 2026-09-01 until go-live plus four weeks of hypercare, at the latest 2027-04-30.
- The hospital may extend once by six months under the same conditions.

## 3. Fees
- Hourly rate as in [[Rates]], billed in quarter hours.
- Budget cap for the whole project: 260 hours. Above that only with a written change request.
- Every invoice must show the purchase order number KL-PO-5512, otherwise accounts sends it back.

## 4. Invoicing and payment
- Monthly invoice with a timesheet extract attached.
- Payment within 30 days of receipt. Public-sector style approval chain, so it is usually day 28 or 29.
- No late fee was accepted; they struck that clause out.

## 5. Working mode
- On-site in Potsdam on Tuesdays during the build phase, remote otherwise.
- Hospital laptop provided; my own devices are not allowed anywhere near patient data.
- Status meeting every second Thursday with Dr. Korte.

## 6. Confidentiality
- Patient data is covered by medical confidentiality on top of the GDPR; I signed the separate
  confidentiality declaration on 2026-08-28.
- Aggregated, anonymised figures may be shown internally only. Nothing about this project in my talk.

## 7. Intellectual property
- The dashboard and all code written for it belong to the hospital from the start (work made for hire style).
- I may reuse general knowledge, not code.

## 8. Liability
- Limited to the total fees of the contract, except intent and gross negligence.
- Proof of professional liability insurance had to be sent before the start (done 2026-08-30).

## 9. Termination
- Six weeks' notice to the end of a calendar month, either side.
- For cause: immediately.

## 10. Data protection (data processing agreement)
- I act as a processor for the hospital under a data processing agreement.
- Only pseudonymised extracts; the key stays with the hospital's IT.
- Technical and organisational measures: annex 2 of the DPA, I have a copy.
- Retention: extracts are deleted at the end of each sprint, the deletion is logged.
- Patient-level data may be processed only on the hospital's on-premise analytics server KLX-04, in their
  Potsdam data centre. No copies, no exports, not even to the hospital laptop.
- Any new data source needs a DPIA update signed off by Jonas Albrecht before I touch it.
- Data breach: report to Jonas Albrecht within 24 hours.

## 11. Small print
- German law, jurisdiction Potsdam.
- Changes only in writing, signed by both sides (email is not enough here, unlike Logistik).

## My notes
- Slower to pay than [[Müller Logistik]], but very reliable once approved.
- Ask about the six-month extension in December, before go-live planning starts.

#contract
