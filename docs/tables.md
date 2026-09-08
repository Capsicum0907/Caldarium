# Types, tiers and the ladder

How a block gets to exist: what the tables hold, and what a row decides for itself.
Back to the [README](../README.md).

**Generators come in types; everything else comes in tiers.** A type is what a
generator draws on: something solid in a slot, something molten in a tank, or the
sky. What counts as fuel belongs to the type rather than being a rule they all
share — and the two that burn ask the item and the bucket rather than keeping a
list, so a fuel any mod adds is one they already accept.

A tier is how much and how fast, and a kind is what a thing does with what it
holds: a battery hands it on, a charger puts it into what is in its slots. One kind
at one tier is one block, so a kind or a tier added later is a row and not a class.

**A generator may say that it comes in tiers, and only then is it multiplied.** The
sun gives the same everywhere, so a better panel is the only way that one gets
better; a burner gets better by being fed something better, and has no ladder at
all. The table stays flat by default and one row is allowed to be a ladder.

**The ladder has eight rungs**: copper, iron, gold, diamond, netherite, nether star,
and two compressed stars above that. Nothing is built on the last two yet — they are
the shape of the ladder rather than blocks. A compressed nether star is not a thing
the game has, so whoever first puts a machine up there is adding the item in the same
breath, and the recipes say so rather than choosing a stand-in.

**A row says how far it climbs, not only whether it climbs.** Cables, importers and
exporters stop at the nether star. Compressing is worth doing for something that
holds, because it is the same block holding eight times as much, and worth nothing
for something that carries: a line is limited by what is at each end of it rather
than by the line.

**The ladder is expected to grow.** Nothing counts the rungs: the numbers come from
where a tier sits in the list, the slots from the same, the recipe from the rung
below, and the colour from the tier itself. A rung added to the list needs a colour
on its line and a metal in the recipes, and the compiler asks for the second one.

**A rung added underneath moves nothing above it.** The starting figures are written
for iron rather than for the first rung, so copper arriving below left every other
rung where it was, and a machine with no rung of its own reads iron by name.

**The one that draws on the sky is a panel rather than a box** — three pixels of
it, because all it needs is the face it points upwards, and a full cube of
machinery underneath would be a cube that does nothing. It asks three questions
and no more: is it day, is the weather clear, and is there anything overhead. Anything solid above it stops it
entirely; anything light passes through takes a share, and what share is a setting
because there is no defensible number for it.

**Fuel goes in and does not come back out.** A hopper under a furnace is the
arrangement everybody builds, and one under a generator would otherwise pull the
coal straight back out of it; a pipe set to extract would empty a tank the same
way. Both refuse. What is left over is not fuel and may still be taken, which is
how the bucket a lava bucket leaves behind gets collected — a tank has no such
leftovers, so fuel put in one by mistake stays there until the block is broken.
