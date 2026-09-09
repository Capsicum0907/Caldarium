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

## A panel that runs on something other than daylight

Built: the panel is `lucernarium` and the artificial sun is `sol`. What is left of the
idea is a question nobody has asked yet - whether a panel should read something else
again - and it is not open work.

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

## A rung that means "no rungs"

A row says how far up the ladder it goes, and a row that does not climb says so with
nothing at all - `null`. That absence then has to be given a meaning everywhere it is
met: what a missing tier is worth, what it is called, what it is made of.

⚠ **It has already been given the wrong one once.** A missing tier was read as the iron
rung, which was right while the burner had no ladder and quietly wrong the moment a row
turned up that genuinely has none: bidental was handed twice the capacity it was given
and the first rung's transfer rate, and nothing said so.

So: a rung that means no rungs. `Tier.NONE`, or whatever it ends up called, sitting in
the table like the others with a step of one - and then there is no absence to
interpret, because standing on nothing is a place to stand rather than a place not to
be.

⚠ What it costs is the other side of the same coin: it would appear in `Tier.values()`,
so everything that walks the ladder has to leave it out, and it would want a colour and
a metal it has no use for. That is a handful of places that fail loudly against a
scattering of null branches that failed quietly. Not built; recorded because the
argument for it is a fault that actually happened.
