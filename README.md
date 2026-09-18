# Caldarium

English | [日本語](README.ja.md)

Forge Energy: something that makes it, something that keeps it, and something that
carries it.

*Caldarium* is the hot room of a Roman bath — the one the fire under the
floor keeps warm.

> **Status: in development, and private.** Every block below is in the game. What
> has been watched working in a running game, and what has not yet, is in
> [Status](docs/status.md).

## What is in it

| | |
|---|---|
| **Generators** | Nine, each drawing on something different — see below |
| **Battery** | Keeps energy. Four times what a burner of the same tier holds |
| **Charger** | Puts energy into what is in its slots |
| **Cable, importer, exporter** | Carry energy between this mod's blocks. Only an importer or an exporter reaches into another mod's |
| **Sol** | An artificial sun. It makes no energy itself |

| Generator | Draws on |
|---|---|
| Burner | Anything a furnace burns |
| Crucible | The same fuels, molten, in a tank |
| Solar panel | Daylight |
| Lucernarium | Lamplight |
| Hypocaustum | A difference in heat between opposite faces |
| Experientia | Experience, poured in from its screen |
| Spoliarium | The life of whatever stands on it |
| Palus | Being hit |
| Bidental | Lightning |

**Everything that has a size comes in tiers**: copper, iron, gold, diamond, netherite
and nether star, and above those two compressed tiers that only generators reach.
Bidental is the one generator with no tiers.

## Target

| | |
|---|---|
| Minecraft | 1.21.1 |
| Loader | NeoForge 21.1.248 |
| Java | 21 |

1.21.1 is the version large tech mods stayed on, so it is where this mod is useful.

## Design

A generator on its own has nowhere to put what it makes, and a battery on its own has
nothing to fill it. Everything else in this set is one thing; this is one thing that
needs more than one block to be a thing at all.

The design is in `docs/`, a file to a subject:

| | |
|---|---|
| [Types, tiers and the ladder](docs/tables.md) | What the tables hold, how far a row climbs, and where the numbers come from |
| [How energy moves](docs/energy.md) | No network, the one asymmetry in pushing, the boundary, and what it measured |
| [Config](docs/config.md) | Every setting, and why the file itself carries no prose |
| [Status](docs/status.md) | What has been watched working in a running game, and when |
| [Ideas](docs/ideas.md) | Not built, and kept so they are not lost |

## Build

```
run.bat                   # compile and launch a dev client - double-clickable
gradlew build             # produce the jar
gradlew runGameTestServer # run every game test, headless, then exit
gradlew runData           # regenerate textures, models, recipes and language
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
