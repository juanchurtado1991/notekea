---
tags: [talk, conference]
status: preparing
conference: Nordwind Data Days 2027
city: Copenhagen
date: 2027-02-18
---
# Nordwind Data Days talk

**Title:** Forecasts people actually trust
**Format:** 30 minutes + 10 minutes questions
**Where:** Nordwind Data Days 2027, Copenhagen, 2027-02-18
**Slot:** TBC (organisers said they would confirm in November)

Abstract as submitted: [[Abstract]]. Travel money: [[Talks/Budget]].

## Why this talk
Every forecasting project I do ends in the same place: the model is fine, and nobody uses it. Dispatchers,
ward managers, growth leads — they keep their spreadsheet next to the dashboard and trust the spreadsheet.
The talk is about the boring things that change that: showing uncertainty in a way people can read, keeping
the forecast stable from week to week, and letting people overrule it without breaking it.

I want people to leave with three habits, not with a library.

## Outline
1. Opening story: the dispatcher who printed my forecast and wrote his own numbers next to it (anonymised,
   no client named — see the confidentiality clause in the [[Müller Logistik contract]]).
2. What "trust" means here: people act on the number without checking it somewhere else.
3. Habit one — intervals, not points. Fan charts vs. "between 80 and 110". Why the second one wins.
4. Habit two — stability. A forecast that jumps 20 % between two runs with no new information loses trust
   for months. Smoothing the revisions, and saying why when it does change.
5. Habit three — overrides as data. Let people overrule, log it, and learn from the overrides.
6. Live demo (see below).
7. What I got wrong: calibration plots nobody understood; the "confidence score" that meant nothing.
8. Closing: one slide with the three habits.

## Slides
- Template: the dark one from last year, but bigger fonts. Minimum 28 pt for anything on a chart.
- No more than 18 slides for 30 minutes.
- Every chart must be readable from the back row — test it on the TV at the co-working space.
- Speaker notes in English, short.

## Questions I expect
- "Isn't this just UX?" — partly, and that is the point.
- "How do you measure trust?" — share of decisions taken without an override, and how big the overrides are.
- "Which library?" — none in particular; everything in the demo is plain pandas and matplotlib.
- "What about LLM forecasts?" — out of scope, one sentence and move on.

## Demo plan
The demo must not use any client data — not Logistik, not Kliniken, not Nordlicht. Neither contract allows
it, and I would not want to anyway.

Options I looked at:
- Berlin weather and ice-cream sales — too cute, and the seasonality is too easy.
- A synthetic dataset — honest, but people do not believe synthetic data in a trust talk.
- Public bike-share data — real, messy, everybody understands what a bike rental is.

Decision (2026-10-30): the demo uses the anonymised Rostock bike-share export from the city's open data
portal, daily rentals per station, 2023 to 2025. I keep a frozen copy in the talk repo so the demo does not
depend on the conference Wi-Fi. The demo shows the same forecast three ways: as a point, as an interval, and
as an interval with a stability rule, and the audience votes which one they would act on.

Backup if the laptop dies: a recorded screen capture of the demo, on a USB stick and in the cloud.

## Rehearsal log
- 2026-11-07 — first full run, 41 minutes. Way too long; cut the history section.
- 2026-11-14 — planned: run for the co-working Friday crowd.
- 2026-12 — planned: run for Petra's data meetup group, if she still organises it.
- 2027-02 — one last run in the hotel the night before.

## Open questions
- Do they record the talks? If yes, ask for the video for my website.
- Can I bring stickers? (Probably yes.)

## Tasks
- [x] Submit abstract
- [x] Decide on the demo dataset
- [ ] Book flights
- [ ] Book hotel (3 nights)
- [ ] Freeze the demo data in the talk repo
- [ ] Cut the deck to 18 slides

#talk
