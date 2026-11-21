---
tags: [tidewell, design]
status: draft
version: 0.8
updated: 2026-11-01
---
# Tidewell — Design Document

## 1. Pitch
Tidewell is a short pixel-art adventure about a young lighthouse keeper who has to relight five lighthouses along a
coast where the tide rises a little more every night. You explore by day, and when the tide comes in you retreat to
higher ground, carrying your lantern to keep the fog away. Target length: 4 to 6 hours. Target price: 17.99 CAD.

## 2. Story
Odile, the keeper, wakes up to find the great lighthouse of Tidewell dark and the old keeper gone. The fog that comes
in with the tide hides strange creatures, and the fishing village below has started to empty. Each region has its own
lighthouse, its own small story, and a villager who needs help. The truth comes slowly: the old keeper made a bargain
with something under the water, and the tide is the price.

Tone: melancholy but warm. No text walls; most of the story is told by the places and short lines of dialogue.

## 3. Characters
- **Odile** — the player. Quiet, stubborn, carries the lantern.
- **Old Corentin** — the missing keeper. Appears in letters and in the last region.
- **Marguerite** — runs the harbour shop, gives the upgrades.
- **The twins (Anouk and Basile)** — kids who follow Odile around and leave hints.
- **The Drowned Keeper** — the final boss, what is left of the bargain.

## 4. Core loop
1. Day: explore a region, talk to villagers, gather driftwood and oil.
2. Dusk: a bell rings; the tide timer starts.
3. Night: the tide rises. Reach the region's lighthouse, solve its light puzzle, and relight it.
4. Morning: the region is safe; spend oil and driftwood at Marguerite's shop.

The tide timer is the heart of the game. It must feel tense but never unfair. If the tide catches you, you wake up
at the last lighthouse with your items, minus half the oil you carried.

## 5. Mechanics
### Lantern
The lantern pushes back fog in a radius that shrinks as oil runs low. Oil is the main resource. The lantern also
reveals hidden platforms written in old keeper's chalk.

### Tide
Water level rises in steps, not smoothly, so the player can read it. Each step is announced by a wave sound and a
small screen shake (can be turned off).

### Swing
The lantern swing is the only attack. It stuns small enemies and lights braziers.

### Fishing
Optional minigame at each pier. Fish can be sold or traded for oil. It must stay optional: no story item hides
behind fishing.

## 6. Regions
| # | Region | Tileset | New idea |
|---|---|---|---|
| 1 | Forest | Forest | basics, lantern, first tide |
| 2 | Harbour | Harbour | boats as moving platforms |
| 3 | Caves | Caves | darkness, oil management |
| 4 | Lighthouse Isle | Lighthouse | vertical climb against the tide |
| 5 | Storm Coast | Storm Coast | wind pushes the player, final boss |

## 7. Enemies
- **Crabs** — walk on ledges, stunned by the swing.
- **Fog moths** — fly toward the lantern and drain oil.
- **Eels** — only in water, punish the player for being late.
- **Gulls** — steal the item you are carrying if you stand still.
- **Mimic buoys** — look like platforms until you jump on them.
- **Lantern ghosts** — copy your swing a second later.
- **Tide wraiths** — come with the last tide step of each night.

## 8. Bosses
Three bosses in total. The first two are short, mostly puzzles.

- **The Kelp Matron** (Harbour) — knock her lanterns out of the water.
- **The Hollow Bell** (Caves) — a bell that rings the tide forward; you ring it back.

## 9. Progression
Upgrades come from Marguerite's shop: bigger oil tank, longer swing, a rope to climb, a boat for the Harbour, and
the keeper's coat that slows oil use. No skill tree. Every upgrade changes how you move or how long you last at night.

## 10. Save system
Autosave at every relit lighthouse and every morning. Three manual save slots. Quitting during an autosave must never
corrupt a save (see the bug tracker).

## 11. UI
Minimal. Oil gauge on the lantern sprite itself, tide level on a small post at the left of the screen. No minimap; a
hand-drawn map you fill in as you explore. All text must fit French, which runs about 20% longer than English.

## 12. Audio
Music by Priya Nair: one theme per region plus a night variation that layers in as the tide rises. Sound effects by
Lou Bastien. The bell at dusk is the most important sound in the game.

## 13. Accessibility
- Assist mode: the tide timer can be slowed to 60% of its normal speed, or paused entirely.
- Screen shake off, flashing off.
- Full controller remapping, and keyboard only play.
- Text size in three steps.

## 14. Technical
Internal resolution 384x216, scaled to the screen by whole numbers only. Target 60 fps on a mid-range laptop from
2020. Builds for Windows, macOS and Linux first; console later if Brightkite or another publisher helps.

## 15. Scope and cut list
- Co-op mode — cut on 2026-07-08, too much work for one person.
- Procedural night layouts — cut, the hand-made ones are better.
- A sixth region (the Sunken Village) — moved to a possible update.

## 16. Final boss
**The Drowned Keeper** waits at the top of the Storm Coast lighthouse. Three phases:
1. He floods the room step by step; you relight the four braziers before the water reaches them.
2. He copies your swing like a lantern ghost; you have to bait him into the bell.
3. He pulls the tide over the whole tower; you climb while the water chases you.

He has 1,800 HP across the three phases (600 each). Phase 2 must not be skippable (bug B-09).

## 17. Open questions
- Does the ending need a choice, or is one ending stronger?
- Should fishing records go on a leaderboard? Probably not.
