# Open questions

Architecture questions waiting for an ADR or a decision.

- [ ] Should the snapshot job live in the ledger service or in a separate worker?
- [ ] Do we keep the legacy ledger tables for a quarter or for a year after cutover?
- [ ] Should webhook signatures move to the shared signing service?
- [ ] Is a token bucket per merchant enough, or do we need one per card BIN too?
- [x] Event sourcing or mutable balances? — event sourcing, see [[ADR-012 Event sourcing for ledger]]
- [x] How long do idempotency keys live? — see [[ADR-013 Idempotency keys]]

#questions
