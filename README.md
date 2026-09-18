# Caldarium

English | [日本語](README.ja.md)

Generators, batteries, chargers and cables for Forge Energy (FE).

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

## Requirements

| | |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.248 |
| Java | 21 |

## Building

```
run.bat                   # compile and launch a dev client
gradlew build             # produce the jar
gradlew runGameTestServer # run the game tests
gradlew runData           # regenerate textures, models, recipes and language files
```

`JAVA_HOME` must point at a JDK 21, or `java` must be on `PATH`.

## License

MIT. See [LICENSE](LICENSE).
