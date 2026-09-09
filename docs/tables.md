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

**The ladder has eight rungs**: copper, iron, gold, diamond, netherite, nether star,
and two compressed stars above that.

**Every row says how far it climbs.** Generators climb all eight; cables, importers
and exporters stop at the nether star. Compressing is worth doing for something that
holds, because it is the same block holding eight times as much, and worth nothing
for something that carries: a line is limited by what is at each end of it rather
than by the line.

**A rung says how much bigger it is than the one under it**, and the steps grow while
the ladder is climbed: two, two, three, four, six. Their geometric mean is about three
- the right average for steps that multiply, since what they have to add up to is a
product rather than a sum. The two compressed rungs step by eight, which is how many
blocks go into one of them, so compressing trades eight blocks for one holding exactly
what they held: what it buys is the space and the tick cost of seven fewer blocks, and
it costs a medium, and none of it is energy made out of arithmetic.

**The height of the ladder was measured against other mods rather than chosen.** The
sixth rung is meant to stand beside what a mod of the same effort offers - Caldarium's
generators are single blocks laid by the handful, so that comparison is Powah's
furnator and panel rather than a multiblock reactor - and the two compressed rungs are
what goes past them. What that came out as, and what it used to be, is in
[2026-09-09/1330 in Atrium](../../Atrium/2026-09-09/1330_fe-scale-comparison/) if that
is to hand; the short version is that the ladder used to multiply by thirty-three
million where Powah's multiplies by two thousand, and now multiplies by eighteen
thousand.

⚠ Nothing reaches the ceiling an int imposes any more, which is what the ladder being
this steep used to cost: a battery that held the same at two rungs running, and a cable
with one tick of slack where the design says two.

**A rung says how it is built.** The first six are the rung below inside a frame of
their own metal. The last two are eight of the rung below squeezed around one thing in
the middle, so they need no material of their own: there is no compressed nether star
in the game and none has to be invented.

The thing in the middle is a ghast tear on the seventh rung and a totem of undying on
the eighth. **Neither is chosen for being dear.** Sixty-four sixth-rung blocks go into
one eighth-rung block and every one of those took four nether stars, so two hundred
and fifty-six withers are the price and a tear beside them is nothing. What the middle
chooses is which places you have to have been - and both of those are places the
withers already took you past, which is why nothing newer is asked for. ⚠ A tear also
predates every version this might be carried back to, which a breeze rod and a heavy
core do not.

**The ladder is expected to grow.** Nothing counts the rungs: the numbers come from
where a tier sits in the list, the slots from the same, the recipe from the rung
below, and the colour from the tier itself. A rung added to the list needs a colour
on its line and, if it is built from a frame, a metal in the recipes - and the
compiler asks for the second one.

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

## The one that draws on a difference

Heat is not energy; a difference in heat is. A single hot thing has nowhere to send
its heat, so nothing passes through anything, and a generator that made energy from
being next to lava would be making it out of nothing. **Hypocaustum sits between a hot
thing and a cold one and takes a share of what crosses it.**

**Every face has a temperature, and a face with nothing on it reads the biome.** The
game already keeps one number per biome — 0.0 in a snowy plain, 0.8 in plains, 2.0 in
a desert or the nether — and blocks are given values on the same scale in the config.
That one decision folds two machines into one: lava on one face and bare air on the
other is a hot machine, ice on one face and bare air the other is a cold one, and both
are the same block put down differently.

**Three axes, read separately and added.** The block holds three elements, one per
pair of opposite faces, because heat crosses from a face to the one facing it and not
to the one beside it. So every face counts for something, and how much work went into
the arrangement is how much it makes.

⚠ It also means **burying one in lava produces nothing at all**: every axis then has
lava against lava, and the difference on each is zero. That is the rule teaching
itself, and it is why the sixth face was not left out.

⚠ And the nether, where lava is free, is the worst place to run one: the biome reads
2.0, so a bare face there is nearly as hot as the lava on the other side.

It climbs the ladder like every other generator. Placing it better is a second lever
on top of that one, not instead of it.

## The one that runs on what a player earned

Experience is the one resource in the game that a machine cannot make. Something has
to go and get it, which is why it can be worth a great deal without breaking anything:
there is no rate to optimise, only a person deciding to spend what they have.

**It is a burner whose fuel is not a thing.** Levels are poured in from the screen,
turned into burning time at a rate the config sets, and spent the way a log is spent.
There is no slot, no tank, no flame and no glow — the block holds a number.

**It has tiers**, by the same rule as the panel and for the same reason: there is no
better experience to feed it, so a better machine is the only way it gets better.

⚠ Pouring is three buttons rather than a click on the block. A block that drank
experience when you touched it would take it while you were building.

## Sol, which makes no energy at all

Not a generator: it is a sun, and what a sun does is shine on the panels somebody else
put down. Inside its reach a solar panel is told it is midday with a clear sky, so a
field of them keeps working at night and underground.

**It is paid for in durability rather than in energy.** A sun that ate energy to let
panels make energy would be either free power or pointless, depending on which way the
sum came out. So it has none: it is crafted whole, it burns for thirty in-game days -
720,000 ticks, at 24,000 a day - and then it is gone. ⚠ **Rain and snow spend it five
times as fast**, which takes those thirty days down to six.

**It is a light and it is hot.** Brightest light the game has, dimming by a third of
its range for each quarter of its durability spent, so how far through it is can be
read off the room. And standing near it sets you alight - a sun that was only bright
would not be a sun.

⚠ Its reach is one block and that is a placeholder rather than a decision.
