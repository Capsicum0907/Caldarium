# Caldarium

Forge Energy: something that makes it, something that keeps it.

*Caldarium* is the hot room of a Roman bath — the one the fire under the
floor keeps warm.

> **Status: it makes energy and moves it.** A burner and an iron battery, watched
> working in a running game on 2026-08-30 — the burner runs, the batteries fill and
> level off against each other, a hopper feeds it and does not empty it, and the
> level shows in a tooltip. Both blocks open a screen — a bar for the charge, a
> flame for the fuel, and the exact figures on hover — watched working the same day.

## Target

| | |
|---|---|
| Minecraft | 1.21.1 |
| Loader | NeoForge 21.1.248 |
| Java | 21 |

1.21.1 is the version large tech mods stayed on, so it is where this mod is useful.

## Design

A generator that turns fuel into Forge Energy, a battery that keeps it, and a
charger that puts it into the things you carry.

They ship together because a battery on its own has nothing to fill it. Everything
else in this set is one thing; this is one thing that needs more than one block to
be a thing at all.

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

**The ladder is expected to grow.** Nothing counts the rungs: the numbers come from
where a tier sits in the list, the slots from the same, the recipe from the rung
below, and the colour from the tier itself. A rung added to the list needs a colour
on its line and a metal in the recipes, and the compiler asks for the second one.

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

**No network.** Energy is not routed, planned or cabled. Each block, on its tick,
offers what it has to the six blocks touching it, and that is the entire transport
layer. A network would be a second system to keep correct, and nothing here needs
one: a row of batteries already carries, because energy can only move towards the
emptier end.

**Cables are rows in the same table.** A cable is a battery that holds almost
nothing and moves a great deal, so carrying over distance needed no new mechanism —
only a smaller capacity and a boundary. Three blocks draw that boundary: a **cable**,
an **importer** that draws out of what is outside it, and an **exporter** that feeds
into it. Each has tiers.

**The boundary is about pushing, not about connecting**, and splitting it that way
is what keeps both halves honest. A cable offers only to the line, so one laid past
a machine does not power it and a corridor of cable is possible at all. It has no
matching rule about being offered *to*: a burner or a battery standing against a
cable fills it directly, which leaves the importer to do the job nothing else here
can — asking, for the machines in other mods that wait to be asked rather than
pushing. The exporter is the one block that offers outside the line, and it offers to
everything outside it, this mod's own batteries included.

An importer is a `SOURCE`, the same as a generator: energy that arrives in the line
arrives through one, and nothing may push into it. Where it came from — burnt,
gathered off the sky, or drawn out of somebody else's machine — is not a distinction
anything downstream has to make.

The reason to build these rather than lean on a cable mod is the handling. **No
wrench**, because the direction is which of the three blocks was placed. **No
upgrade to fit**, because the speed is the tier. **No window to open**, because
there is nothing to configure — a right-click passes straight through, so a cable is
something you can build against rather than something that opens. A pipe that needs
three of those before it moves anything is a pipe that is faster to lay by hand.

**A cable can only forward what it is holding**, so its capacity is its throughput
rather than a comfort: it holds two ticks of its own rate and no more. And a cable is
not a machine you improve but wire you draw — the one thing here made from its own
metal at every rung, six at a time, rather than built up from the rung below, because
four ingots a block is a price nothing laid by the hundred can pay.

Pushing has one asymmetry, and it is the whole of the rule:

- **To another Caldarium battery**, only downhill — to a neighbour holding a
  smaller share of what it can hold. Without this two touching batteries push into
  each other every tick, forever. With it, a row of batteries is a line that
  carries.
- **To anything else**, freely, and let `receiveEnergy` decide. A running machine
  keeps its own buffer near full, so a rule that compares levels would refuse the
  one block that actually wants the energy.

Both sides of that need only `getEnergyStored` and `getMaxEnergyStored`, which are
on `IEnergyStorage` itself.

The energy interface is NeoForge's own, so anything that already speaks Forge
Energy — from any mod — connects without either side knowing about the other.

**The level is visible in a screen**, the way a furnace shows its fire and its
progress, and in a tooltip for the mods that read one. The panel is drawn from a
formula like everything else here, but the flame is the furnace’s own sprite,
named rather than copied: no vanilla art ships with this mod, and somebody who has
restyled the furnace with a resource pack has restyled this too.

Capacity, transfer rates and generation rate are settings. There is no defensible
number to write into the code here: what is right depends entirely on what else is
installed.

## Build

```
run.bat                   # compile and launch a dev client - double-clickable
gradlew build             # produce the jar
gradlew runGameTestServer # run every game test, headless, then exit
gradlew runData           # regenerate models, recipes and language
```

`JAVA_HOME` must point at a JDK 21, or `java` must be on `PATH`.

## Roadmap

- [x] **0** — scaffold; the mod loads
- [x] **1** — one generator type and one battery tier, as the first rows of the
  table, pushing to whatever touches them
- [x] **2** — the screen: fuel, progress and level, the way a furnace shows them
- [x] **3** — charging: a slot on the battery, and a charger block that is the same
  idea with more slots
- [x] **4** — the rest of the types and tiers, as rows
- [ ] **5** — cables: a cable, an importer and an exporter, in tiers
- [ ] **6** — checked by game tests rather than by eye

## Related

One of a set of small, independent mods, each doing one thing and depending on
none of the others: [Fodina](https://github.com/Capsicum0907/Fodina),
[Trivium](https://github.com/Capsicum0907/Trivium),
[Magnes](https://github.com/Capsicum0907/Magnes),
[Cella](https://github.com/Capsicum0907/Cella),
[Acervus](https://github.com/Capsicum0907/Acervus),
[Fornax](https://github.com/Capsicum0907/Fornax),
[Caldarium](https://github.com/Capsicum0907/Caldarium).

## License

Not decided yet. Until it is, the metadata says All Rights Reserved.
