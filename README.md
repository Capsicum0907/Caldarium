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

**The one that draws on the sky asks three questions and no more:** is it day, is
the weather clear, and is there anything overhead. Anything solid above it stops it
entirely; anything light passes through takes a share, and what share is a setting
because there is no defensible number for it.

**No network.** Energy is not routed, planned or cabled. Each block, on its tick,
offers what it has to the six blocks touching it, and that is the entire transport
layer. A network would be a second system to keep correct, and nothing here needs
one: a row of batteries already carries, because energy can only move towards the
emptier end.

**Cables come later, and they are rows in the same table.** A cable is a battery
that holds almost nothing and moves a great deal, so carrying over distance needs
no new mechanism — only a smaller capacity. Three blocks make the boundary
explicit: a **cable** that speaks only to other Caldarium transport, an **importer**
that draws out of something else, and an **exporter** that feeds into it. Each has
tiers.

The reason to build these rather than lean on a cable mod is the handling. **No
wrench**, because the direction is which of the three blocks was placed. **No
upgrade to fit**, because the speed is the tier. **No window to open**, because
there is nothing to configure. A pipe that needs three of those before it moves
anything is a pipe that is faster to lay by hand.

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
progress, and in a tooltip for the mods that read one.

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
