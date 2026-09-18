# Config

English | [日本語](config.ja.md)

Back to the [README](../README.md).

The settings are in `config/caldarium-server.toml`.

## Tiers

Every tier has its own section, so each tier's values can be changed on their own. The
defaults are the Tier 1 value times the ratio below. Values above 2,147,483,647 are set to
2,147,483,647.

| Tier | Ratio to the tier before | Ratio to Tier 1 |
|---|---:|---:|
| 1 | — | ×1 |
| 2 | ×2 | ×2 |
| 3 | ×2 | ×4 |
| 4 | ×3 | ×12 |
| 5 | ×4 | ×48 |
| 6 | ×6 | ×288 |
| 7 | ×8 | ×2,304 |
| 8 | ×8 | ×18,432 |

The default values for each block are in [Blocks](blocks.md).

## Generators

Sections: `generator.<tier>_<generator>`, for example `generator.gold_crucible`.
Bidental's section is `generator.bidental`.

| Setting | |
|---|---|
| `capacity` | Energy it stores, in FE. |
| `transferRate` | Energy it sends to each touching block per tick, in FE. |
| `generates` / `everyTicks` | It generates `generates` FE every `everyTicks` ticks. For experientia, spoliarium and palus, this is the energy per point of experience, health or damage. Bidental uses the `storm` settings instead. |
| `tank` | Crucible only. Tank size, in mB. |
| `lethalHealth` | Spoliarium only. The largest maximum health it kills. |

## Batteries, chargers and cables

Sections: `battery.<tier>`, `charger.<tier>`, `cable.<tier>`, `importer.<tier>`,
`exporter.<tier>`.

| Setting | |
|---|---|
| `capacity` | Energy it stores, in FE. |
| `transferRate` | Energy it sends to each touching block per tick, in FE. For a charger, also how fast it charges items. |

## Other settings

| Setting | |
|---|---|
| `sun.through` | The share of sunlight, in percent, that gets through each transparent block above a solar panel. |
| `heat.temperatures` | Block temperatures for the hypocaustum, as `block=value` or `#tag=value`. |
| `heat.span` | The temperature difference that gives a hypocaustum its full rate on one pair of faces. |
| `experience.pourSteps` | The amounts of experience the buttons on an experientia pour in. An "All" button is always added. |
| `storm.natural` / `storm.summoned` | Energy per lightning strike for a bidental: natural, and called down by a player. |
| `storm.reach` | How far from a bidental a strike still counts, in blocks. |
| `sol.size` | The sun's diameter, in blocks. |
| `sol.durability` | How long the sun lasts, in ticks. |
| `sol.weatherCost` | How much faster rain and snow wear the sun down. |
| `sol.reach` | How far from the sun's surface solar panels and lucernaria run at their full rate, the ground is lit, and creatures are hurt, in blocks. |
| `sol.reachDamage` / `sol.reachEvery` | Damage dealt to creatures within reach, and how often, in ticks. |
| `sol.touchDamage` | Damage per tick for touching the sun. |
| `sol.burnReach` / `sol.burnSeconds` / `sol.burnDamage` | How far from the surface creatures catch fire, in blocks, for how long, and the damage while in range. |
| `sol.blastReach` / `sol.blastDamage` | The explosion when the sun is broken: how far from the surface it reaches, and its damage. |
| `sol.hold` | The gap between you and the sun when you place it, in blocks. |
| `sol.glowSpacing` | Spacing of the light sources around the sun, in blocks. |
| `sol.groundSpacing` | Spacing of the light sources on the ground within reach, in blocks. |
| `sol.pulseEvery` / `sol.pulseLength` | How often the sun pulses brighter, and for how long, in ticks. |
