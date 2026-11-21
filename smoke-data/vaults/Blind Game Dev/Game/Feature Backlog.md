---
tags: [tidewell, backlog]
updated: 2026-11-10
---
# Feature Backlog

Priorities: P1 = needed for the festival build, P2 = needed for launch, P3 = nice to have.

## P1 — festival build
- [x] Tide timer with steps
- [x] Lantern fog radius
- [x] Forest and Harbour regions playable
- [x] Fishing minigame (first version)
- [ ] Caves region playable end to end
- [ ] Title screen with the new logo
- [ ] Settings menu: volume, screen shake, text size
- [ ] Controller remapping
- [ ] French text pass in the first two regions

## P2 — launch
- [ ] Lighthouse Isle region
- [ ] Storm Coast region and final boss
- [ ] Assist mode (tide speed, pause)
- [ ] Hand-drawn map that fills in
- [ ] Steam achievements (12 planned)
- [ ] Cloud saves
- [ ] Credits screen with every freelancer
- [ ] Night variation of every music track

## P3 — nice to have
- [ ] Fishing journal with every fish caught
- [ ] Speedrun timer
- [ ] Colour-blind palette for the fog
- [ ] New Game+ with a faster tide

## Done this autumn
- [x] Fixed the fishing softlock (B-06)
- [x] Lantern swing feels better, shorter wind-up
- [x] Moved the shop to the Harbour

## Polish list (no priority yet)
- [ ] Footstep sounds change on wood, sand and stone
- [ ] Waves splash on the screen edge at the last tide step
- [ ] Marguerite waves when you walk past the shop
- [ ] The twins hide in a different place each morning
- [ ] Gulls drop the stolen item when you swing near them
- [ ] Rain on the Storm Coast hits the lantern glass
- [ ] Lighthouse beams sweep across the background at night
- [ ] Subtle vignette when oil is under 20%
- [ ] Pause menu shows the current region's lighthouse
- [ ] Short fade instead of a hard cut when you wake up

## Tech debt
- [ ] Move the tide steps out of code into the region files
- [ ] One save format for autosave and manual slots
- [ ] Text boxes measure the string instead of a fixed width
- [ ] Remove the old shop code from the Forest

## Decisions log
- 2026-09-02 — Fishing stays optional. No story item behind it.
- 2026-09-20 — Achievements: 12, no grind ones, no "play for 10 hours".
- 2026-10-12 — Map: hand-drawn, no minimap. Testers liked finding things.
- 2026-10-19 — Controller remapping moved from P2 to P1, Theo could not play on his Linux pad without it.
- 2026-10-30 — Photo mode moved out of the launch plan. It goes to the post-launch update, together with the Sunken
  Village region. Too much UI work for now.
- 2026-11-08 — Boss phase bug (B-09) blocks the Storm Coast work; fix first.

## Parking lot
Ideas that are not features yet live in the Game ideas note, not here.
