# Config

English | [日本語](config.ja.md)

Back to the [README](../README.md).

Capacity, transfer rates and generation rate are settings. There is no defensible
number to write into the code here: what is right depends entirely on what else is
installed.

**A generator that makes energy steadily starts from its own rate**: it holds five minutes
of what it makes, and hands on twenty ticks' worth each tick, so a full one empties in
fifteen seconds. One that is paid in lumps - experience, a blow, a death, a strike - has
no rate to count from and keeps figures of its own. Anything a rung would put past the
ceiling an `int` allows is clamped to that ceiling.

The file is `config/caldarium-server.toml`, one section per generator and one per
kind per tier. **It carries no prose**: what a setting is for is here, and the file
itself is the settings and the range each one accepts, so the values are not buried
in paragraphs about them. Every number is a starting point the player then owns.

| Setting | |
|---|---|
| `capacity` | Forge Energy it holds. A generator stops and waits when it is full. |
| `transferRate` | How much crosses its boundary per tick, each way and each side. Also how fast a charger fills what is in its slots, and what a generator offers each neighbour. |
| `generatesPerEightTicks` | Forge Energy a generator makes every eight ticks while it is working. Eight rather than one so a rate below one a tick can be written down: the panel that reads lamplight makes two of these, which is a quarter of a unit a tick, and the block keeps the eighths rather than rounding them away. |
| `tank` | Millibuckets of fuel a crucible holds. A bucket is spent at a time. |
| `lethalHealth` | The largest maximum health a spoliarium will kill, on that rung. Anything larger is left alone, unhurt and unpaid for. Measured against the maximum rather than what is left, so a wither worn down first is still a wither. What it kills drops nothing and leaves no experience - except a player, whose own belongings still fall where they died, since losing them is no one's gain. |
| `experience.pourSteps` | The amounts, in experience points, that the buttons on an experience generator pour in. An "all" button always follows. |
| `sol.durability` | Ticks an artificial sun lasts. The default is thirty in-game days, at 24,000 ticks to a day. |
| `sol.weatherCost` | Ticks of durability spent per tick while rain or snow is falling on it. |
| `storm.natural` / `storm.summoned` | What a strike is worth, when nobody called it down and when somebody did. |
| `storm.reach` | How far from where the bolt lands a bidental will still take it. |
| `sol.size` | How wide the ball is, in blocks. The drawing, the solid shape, the outline and the heat all follow it. |
| `sol.reach` | How far its light counts as daylight for a solar panel. ⚠ Provisional. |
| `sol.burnReach` / `burnSeconds` / `burnDamage` | How far past the surface the heat reaches, in blocks, and what it costs. At 0 only touching burns. |
| `sol.touchDamage` | What a tick touching the ball costs, in place of the burn damage. |
| `sol.hold` | The least gap between a held sun and the body holding it, in blocks. Keep it above `burnReach`, or holding a sun burns. |
| `sol.glowSpacing` | Roughly how far apart the light sources around a sun are, in blocks. Smaller is brighter and even, and places more blocks. |
| `sol.pulseEvery` / `pulseLength` | How often a sun flares, and for how long, in ticks. A length of 0 turns it off. |
| `sol.blastReach` / `blastDamage` | How far past the surface breaking a sun reaches, in blocks, and the damage it does to everything alive there. Blocks are never harmed. |
| `heat.temperatures` | What a block is worth as a face of a hypocaustum, on the same scale the game uses for biomes. `block=value`, or `#tag=value` for a whole tag, so another mod's blocks can be given a temperature without touching this one. A face touching nothing listed here reads its biome instead. |
| `heat.span` | The difference across one axis that counts as a full one. Three axes add up, so a hypocaustum built out on all three makes three times what one pair makes. |
| `sun.through` | What percentage of the sun is left after one block that light passes through. Anything solid overhead stops it entirely, whatever this is set to. |

Defaults are derived rather than written out: a rung is worth eight times the one
below it, so a ladder cannot end up with a step out of proportion and a new rung
never arrives with no numbers at all.
