# Blocks

English | [日本語](blocks.ja.md)

Back to the [README](../README.md).

The tables show the default values for each tier. They can be changed in the
[config](config.md). A block's tooltip shows its own values.

## Generators

Every generator sends the energy it makes into the blocks next to it.

### Burner

- Burns anything a furnace can burn. Put fuel in its slot by hand or with a hopper.
- Fuel cannot be taken back out. Leftovers, such as an empty bucket, can.
- Generates while burning.

| Tier | Generates (FE/t) | Stores (FE) | Transfers (FE/t) |
|---|---:|---:|---:|
| 1 | 167 | 1,002,000 | 3,340 |
| 2 | 334 | 2,004,000 | 6,680 |
| 3 | 668 | 4,008,000 | 13,360 |
| 4 | 2,004 | 12,024,000 | 40,080 |
| 5 | 8,016 | 48,096,000 | 160,320 |
| 6 | 48,096 | 288,576,000 | 961,920 |
| 7 | 384,768 | 2,147,483,647 | 7,695,360 |
| 8 | 3,078,144 | 2,147,483,647 | 61,562,880 |

### Crucible

- Burns fluid fuels such as lava. Fill its tank with a bucket or a pipe.
- The tank holds 10,000 mB and uses 1,000 mB at a time.
- Fluid cannot be taken back out.
- Generates while burning.

| Tier | Generates (FE/t) | Stores (FE) | Transfers (FE/t) |
|---|---:|---:|---:|
| 1 | 167 | 1,002,000 | 3,340 |
| 2 | 334 | 2,004,000 | 6,680 |
| 3 | 668 | 4,008,000 | 13,360 |
| 4 | 2,004 | 12,024,000 | 40,080 |
| 5 | 8,016 | 48,096,000 | 160,320 |
| 6 | 48,096 | 288,576,000 | 961,920 |
| 7 | 384,768 | 2,147,483,647 | 7,695,360 |
| 8 | 3,078,144 | 2,147,483,647 | 61,562,880 |

### Solar Panel

- Generates in the daytime while it is not raining and nothing solid is above it.
- Each transparent block above it lowers the output to 60%.
- The table shows full sunlight.

| Tier | Generates (FE/t) | Stores (FE) | Transfers (FE/t) |
|---|---:|---:|---:|
| 1 | 8 | 48,000 | 160 |
| 2 | 16 | 96,000 | 320 |
| 3 | 32 | 192,000 | 640 |
| 4 | 96 | 576,000 | 1,920 |
| 5 | 384 | 2,304,000 | 7,680 |
| 6 | 2,304 | 13,824,000 | 46,080 |
| 7 | 18,432 | 110,592,000 | 368,640 |
| 8 | 147,456 | 884,736,000 | 2,949,120 |

### Lucernarium

- Generates from the block light (torches, lamps and so on) in the space above it.
- The table shows light level 15. Dimmer light gives less.

| Tier | Generates (FE/t) | Stores (FE) | Transfers (FE/t) |
|---|---:|---:|---:|
| 1 | 0.125 | 750 | 3 |
| 2 | 0.25 | 1,500 | 6 |
| 3 | 0.5 | 3,000 | 12 |
| 4 | 1.5 | 9,000 | 36 |
| 5 | 6 | 36,000 | 144 |
| 6 | 36 | 216,000 | 864 |
| 7 | 288 | 1,728,000 | 6,912 |
| 8 | 2,304 | 13,824,000 | 55,296 |

### Hypocaustum

- Generates from the difference in temperature between opposite faces. Each of the three
  pairs of opposite faces counts separately.
- A face with nothing on it takes the temperature of the biome. Block temperatures are set
  in the config: lava 10, fire 5, magma block 4, soul fire 3, campfire 3, soul campfire 2.5,
  snow block −0.5, ice and powder snow −1.
- The table shows one pair that differs by 11, such as lava against ice. All three pairs
  give three times as much.

| Tier | Generates (FE/t) | Stores (FE) | Transfers (FE/t) |
|---|---:|---:|---:|
| 1 | 8 | 48,000 | 160 |
| 2 | 16 | 96,000 | 320 |
| 3 | 32 | 192,000 | 640 |
| 4 | 96 | 576,000 | 1,920 |
| 5 | 384 | 2,304,000 | 7,680 |
| 6 | 2,304 | 13,824,000 | 46,080 |
| 7 | 18,432 | 110,592,000 | 368,640 |
| 8 | 147,456 | 884,736,000 | 2,949,120 |

### Experientia

- Pour experience in from its screen: 10, 100 or 1,000 points at a time, or all of it.
- The energy is added at once. It takes only as much as it has room for.

| Tier | Generates (FE/exp) | Stores (FE) | Transfers (FE/t) |
|---|---:|---:|---:|
| 1 | 10 | 5,000 | 4,096 |
| 2 | 20 | 10,000 | 8,192 |
| 3 | 40 | 20,000 | 16,384 |
| 4 | 120 | 60,000 | 49,152 |
| 5 | 480 | 240,000 | 196,608 |
| 6 | 2,880 | 1,440,000 | 1,179,648 |
| 7 | 23,040 | 11,520,000 | 9,437,184 |
| 8 | 184,320 | 92,160,000 | 75,497,472 |

### Spoliarium

- Kills any creature that stands on it, except players, and generates from the health the
  creature had.
- It only kills creatures whose maximum health is within the limit for its tier.
- Creatures it kills drop no items and no experience.
- It kills nothing while it is full.

| Tier | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| Maximum health it kills | 20 | 40 | 60 | 80 | 100 | 200 | No limit | No limit |

| Tier | Generates (FE/health) | Stores (FE) | Transfers (FE/t) |
|---|---:|---:|---:|
| 1 | 0.1 | 5,000 | 4,096 |
| 2 | 0.2 | 10,000 | 8,192 |
| 3 | 0.4 | 20,000 | 16,384 |
| 4 | 1.2 | 60,000 | 49,152 |
| 5 | 4.8 | 240,000 | 196,608 |
| 6 | 28.8 | 1,440,000 | 1,179,648 |
| 7 | 230.4 | 11,520,000 | 9,437,184 |
| 8 | 1,843.2 | 92,160,000 | 75,497,472 |

### Palus

- Hit it to generate. A hit is worth your attack damage, less if your swing has not
  recharged.
- Enchantments and a mace's falling bonus do not count.

| Tier | Generates (FE/damage) | Stores (FE) | Transfers (FE/t) |
|---|---:|---:|---:|
| 1 | 5 | 5,000 | 4,096 |
| 2 | 10 | 10,000 | 8,192 |
| 3 | 20 | 20,000 | 16,384 |
| 4 | 60 | 60,000 | 49,152 |
| 5 | 240 | 240,000 | 196,608 |
| 6 | 1,440 | 1,440,000 | 1,179,648 |
| 7 | 11,520 | 11,520,000 | 9,437,184 |
| 8 | 92,160 | 92,160,000 | 75,497,472 |

### Bidental

- A lightning strike within 3 blocks generates at once. A lightning rod on top draws strikes
  to it.
- Has no tiers.

| Generates (FE/strike) | Stores (FE) | Transfers (FE/t) |
|---:|---:|---:|
| 50,000,000 (natural) / 100,000 (called down by a player) | 1,000,000,000 | 250,000 |

Experientia, spoliarium, palus and bidental light up for a moment when they generate.

## Storage and charging

### Battery

- Stores energy and passes it on to the blocks next to it.
- Has one slot for charging an item.

| Tier | Stores (FE) | Transfers (FE/t) |
|---|---:|---:|
| 1 | 4,008,000 | 4,096 |
| 2 | 8,016,000 | 8,192 |
| 3 | 16,032,000 | 16,384 |
| 4 | 48,096,000 | 49,152 |
| 5 | 192,384,000 | 196,608 |
| 6 | 1,154,304,000 | 1,179,648 |

### Charger

- Charges the items in its slots, using energy from the blocks next to it.

| Tier | Stores (FE) | Transfers (FE/t) | Slots |
|---|---:|---:|---:|
| 1 | 16,000 | 4,096 | 3 |
| 2 | 32,000 | 8,192 | 5 |
| 3 | 64,000 | 16,384 | 7 |
| 4 | 192,000 | 49,152 | 9 |
| 5 | 768,000 | 196,608 | 9 |
| 6 | 4,608,000 | 1,179,648 | 9 |

## Cables

- **Cable**: carries energy between this mod's blocks. It does not connect to blocks from
  other mods.
- **Importer**: takes energy out of a block from another mod and into the cable.
- **Exporter**: sends energy from the cable into a block from another mod.

Cables, importers and exporters share these values.

| Tier | Stores (FE) | Transfers (FE/t) |
|---|---:|---:|
| 1 | 8,192 | 4,096 |
| 2 | 16,384 | 8,192 |
| 3 | 32,768 | 16,384 |
| 4 | 98,304 | 49,152 |
| 5 | 393,216 | 196,608 |
| 6 | 2,359,296 | 1,179,648 |

## Sol

- An artificial sun, 64 blocks across. It generates nothing itself.
- When you place it, it appears in front of you. It cannot be placed where it would
  overlap a creature.
- Solar panels and lucernaria within 14 blocks of its surface generate at their full rate,
  day and night. It lights the ground within the same distance.
- It lasts 30 in-game days (720,000 ticks). Rain and snow wear it down five times as fast.
  It shrinks as it wears down.
- Touching it deals heavy damage, and standing within 1 block of its surface sets you on
  fire.
- Anything living within 14 blocks of its surface, players included, takes 5 damage every
  second.
- It can only be broken with a diamond pickaxe or better. Breaking it causes an explosion
  that deals 1,000 damage within 8 blocks of its surface. It drops nothing.
