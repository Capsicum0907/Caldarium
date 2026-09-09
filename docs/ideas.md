# Ideas

Not built. Kept here so they are not lost and so the ones that argue with the design
are known to argue with it before anything is written.
Back to the [README](../README.md).

## Generators

**Life.** A mob standing on it dies at once, and it makes energy in proportion to the
health that was lost. `entityInside` is the hook. A farm that summons iron golems is
the obvious way to run it, which is a normal way to run a Minecraft machine.

**Damage taken.** Hit it, and it makes energy in proportion to the damage the hit
would have done. The model is Target Dummy: a measurement that only combat cares
about, given a use outside combat. It does not need to be an entity —
`BlockBehaviour.attack` fires on left click — so it cannot be destroyed by the thing
that runs it, which was the hard part. The damage figure is not passed in and has to
be worked out from the attacker's attack damage and swing cooldown.

**Steam.** Water and heat, two inputs. Left for later: it needs either two tanks or a
heat side, and if it takes the heat side it should take it the way hypocaustum does.

## A panel that runs on something other than daylight, and an artificial sun

This used to be recorded as arguing with why the panel has tiers. It does not any
more: every generator climbs the ladder now, and the sentence that made solar the
exception was dropped on 2026-09-08. Nothing stands in the way of it.

What is left is a plain question of what the panel reads. Daylight is the one thing
the game hands a block for free; an artificial sun is a block that had to be paid
for, so the two are not one source wearing different hats.

## A relay: same frequency, any distance, any dimension

The shape asked for is Flux Networks without the rest of Flux Networks — the wireless
transfer and nothing else, because the rest was complication that bought nothing.

⚠ **It reads as arguing with "no network", and it does not have to.** What the README
refuses is routing: a graph to plan, keep correct and walk. A frequency is not a
route. Blocks that share one are a set, and the push rule this mod already has —
downhill into our own batteries, freely into anything else — works over that set the
same way it works over six neighbours. **The same rule, a different set of
neighbours**, and no pathfinding appears.

⚠ What it does bring is chunk loading. A block in an unloaded chunk does not tick, so
the membership of a frequency has to live in level `SavedData` rather than in the
blocks, and only the loaded members can take part. That is the real cost of the idea,
and it is not the one it looks like it has.
