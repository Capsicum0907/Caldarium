# Caldarium

English | [日本語](README.ja.md)

Generators, batteries, chargers and cables for Forge Energy (FE), and for Team Reborn Energy on
Fabric.

## Blocks

| Block | |
|---|---|
| Burner | Generates from anything a furnace burns |
| Crucible | Generates from fluid fuels such as lava |
| Solar Panel | Generates from sunlight |
| Lucernarium | Generates from light, sunlight or block light |
| Hypocaustum | Generates from the difference in temperature between opposite faces |
| Experientia | Generates from experience poured into it |
| Spoliarium | Generates from the health of creatures that stand on it |
| Palus | Generates when hit |
| Bidental | Generates from lightning |
| Battery | Stores energy |
| Charger | Charges items |
| Cable, Importer, Exporter | Carry energy |
| Sol | An artificial sun that powers solar panels and lucernaria around it |

Generators have Tier 1 to 8. Batteries, chargers and cables have Tier 1 to 6. Bidental and
Sol have no tiers.

## Documents

| | |
|---|---|
| [Blocks](docs/blocks.md) | What each block does, and its values for each tier |
| [How energy moves](docs/energy.md) | How energy passes between blocks, and how cables connect |
| [Config](docs/config.md) | Every setting |

## Target

| Minecraft | Loader | Also needs | Java |
|---|---|---|---|
| 1.21.1 | NeoForge 21.1 | | 21 |
| 1.21.1 | Fabric Loader 0.16 or later | Fabric API 0.116 or later | 21 |
| 1.20.1 | Forge 47 or later | | 17 |
| 1.20.1 | Fabric Loader 0.15 or later | Fabric API 0.92 or later | 17 |
| 1.19.2 | Forge 43 or later | | 17 |
| 1.19.2 | Fabric Loader 0.14 or later | Fabric API 0.77 or later | 17 |
| 1.18.2 | Forge 40 or later | | 17 |
| 1.18.2 | Fabric Loader 0.14 or later | Fabric API 0.77 or later | 17 |
| 1.16.5 | Forge 36 or later | | 8 |
| 1.16.5 | Fabric Loader 0.11 or later | Fabric API 0.42 or later | 8 |

Each pair has its own jar, named `caldarium-<loader>-<minecraft>-<version>.jar`.

Fabric has no Forge Energy. There the blocks speak Team Reborn Energy, the energy API that Tech
Reborn, Modern Industrialization and Applied Energistics 2 use, and it is bundled inside the jar:
nothing else needs installing. In game the unit is shown as E, as Tech Reborn shows it; one FE in
these documents is one E.

On Fabric, right-clicking towards a sun with a block in hand does nothing at all, where on
Forge and NeoForge the block that was clicked can still be used.

### Minecraft 1.16.5

- Copper does not exist yet. The copper tier is made from copper ingots added by another mod,
  under the `forge:ingots/copper` tag on Forge and `c:copper_ingots` on Fabric. Without such a
  mod the copper tier, and every tier above it, cannot be crafted.
- Bidental takes iron bars where later versions take a lightning rod.
- Any pickaxe breaks every block, whatever its tier.
- The readings in an item's tooltip are lines of text rather than a table.
- On Fabric a hopper cannot fill a burner, and a crucible is filled with buckets only.

## Building

```
run.bat                         # compile and launch a dev client for the active node
gradlew build                   # produce a jar for every node
gradlew runGameTestServer       # run the game tests on every node that has them
gradlew runData                 # regenerate textures, models, recipes and language files
python tools/autopilot.py <node> --world <saved world>   # play the checks in a dev client, then exit
```

Each Minecraft version and loader pair is a [Stonecutter](https://stonecutter.kikugie.dev/)
node, listed in `settings.gradle.kts` and given its own folder under `versions/`. A node's jar
is written to `versions/<node>/build/libs/`. The active node, named in `stonecutter.gradle.kts`,
is the one `run.bat` launches and the one the source tree shows.

The autopilot is a dev-only mod in `src/autopilot`, loaded only by the `runAutopilot` task and
never part of the jar. It opens the given world, checks the recipes, the screens, the sun and
its reach, and on Fabric that energy reaches a Team Reborn Energy store. It records PASS/FAIL
lines in `versions/<node>/autopilot-run/autopilot/result.txt` and takes screenshots for what
only the eye can judge.

Minecraft 1.16.5 has no game test framework, so its nodes are checked by the autopilot only.
The 1.16.5 Forge dev client also loads `versions/1.16.5-forge/devfix/`, a coremod that renames
two calls in `BlockMath` that Architectury Loom leaves unmapped. It is not part of the jar.

`runData` runs on the 1.21.1 NeoForge node only. The other nodes convert its output to their
own format when they build.

`JAVA_HOME` must point at a JDK 21, or `java` must be on `PATH`. Gradle fetches the older JDKs
the other nodes compile against.

## License

MIT. See [LICENSE](LICENSE).
