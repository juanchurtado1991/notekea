---
tags: [adr, cards]
status: proposed
date: 2026-11-09
---
# ADR-016 Rate limiter for card API

## Context
During the card auth timeout of 2026-11-06, client retries amplified the load on the issuer gateway.

## Proposal
Token bucket per merchant in front of the card API, 200 requests per second by default.
