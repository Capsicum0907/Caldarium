# Caldarium

Forge Energy: something that makes it, something that keeps it.

*Caldarium* is the hot room of a Roman bath — the one the fire under the
floor keeps warm.

> **Status: it makes energy and moves it.** A burner and an iron battery, watched
> working in a running game on 2026-08-30 — the burner runs, the batteries fill and
> level off against each other, a hopper feeds it and does not empty it, and the
> level shows in a tooltip. Both blocks open a screen — a bar for the charge, a
> flame for the fuel, and the exact figures on hover — watched working the same day.
>
> **The line carries, and it carries across the boundary.** Watched working on
> 2026-08-31. Between this mod's own blocks first: a burner, an importer, three
> cables, an exporter and a netherite battery — the burner holds nothing because what
> it makes leaves the same tick, each cable holds one tick of it in transit, and the
> battery climbed from nothing to 200.36 kFE. Then across it, with two other mods: an
> importer held a Generator Galore netherite generator at nothing while a diamond
> battery of ours climbed to 151.04 kFE, and an exporter filled an Acervus energy
> heap. Every block in the line had grown an arm towards the next, and the doors had
> grown one towards the other mod as well.
>
> **And the refusal holds, which is the claim the whole boundary is drawn for.**
> Watched the same day: a cable laid straight against a Generator Galore generator
> shows no arm and takes nothing from it, an importer between the two carries it
> again, and a burner of ours against the same cable feeds it as it always did. The
> line is closed to other mods in both directions and open to this one throughout.

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

The design is in `docs/`, a file to a subject:

| | |
|---|---|
| [Types, tiers and the ladder](docs/tables.md) | What the tables hold, how far a row climbs, and where the numbers come from |
| [How energy moves](docs/energy.md) | No network, the one asymmetry in pushing, the boundary, and what it measured |
| [Config](docs/config.md) | Every setting, and why the file itself carries no prose |

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
- [x] **5** — cables: a cable, an importer and an exporter, in tiers
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

MIT, the same as the rest of the set. See [LICENSE](LICENSE).
