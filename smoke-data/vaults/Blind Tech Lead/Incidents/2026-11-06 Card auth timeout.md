---
tags: [postmortem, incident]
date: 2026-11-06
severity: SEV3
status: draft
---
# Postmortem: Card auth timeout (2026-11-06)

- Severity: SEV3
- Duration: 18 minutes
- On-call: Omar Haddad

## Summary
About 2 % of card authorisations timed out at the issuer gateway between 14:02 and 14:20 UTC.

## Root cause
Under investigation. The issuer has not yet shared their side of the logs.

## Action items
- [ ] Capture issuer latency traces for every timeout — Priya Castell
- [x] Lower the client timeout alarm threshold — Omar Haddad

#postmortem
