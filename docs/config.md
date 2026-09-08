# Config

Back to the [README](../README.md).

Capacity, transfer rates and generation rate are settings. There is no defensible
number to write into the code here: what is right depends entirely on what else is
installed.

The file is `config/caldarium-server.toml`, one section per generator and one per
kind per tier. **It carries no prose**: what a setting is for is here, and the file
itself is the settings and the range each one accepts, so the values are not buried
in paragraphs about them. Every number is a starting point the player then owns.

| Setting | |
|---|---|
| `capacity` | Forge Energy it holds. A generator stops and waits when it is full. |
| `transferRate` | How much crosses its boundary per tick, each way and each side. Also how fast a charger fills what is in its slots, and what a generator offers each neighbour. |
| `generates` | Forge Energy made per tick while a generator is working. |
| `tank` | Millibuckets of fuel a crucible holds. A bucket is spent at a time. |
| `sun.through` | What percentage of the sun is left after one block that light passes through. Anything solid overhead stops it entirely, whatever this is set to. |

Defaults are derived rather than written out: a rung is worth eight times the one
below it, so a ladder cannot end up with a step out of proportion and a new rung
never arrives with no numbers at all.
