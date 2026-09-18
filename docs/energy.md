# How energy moves

English | [日本語](energy.ja.md)

Back to the [README](../README.md).

- Every block of this mod passes energy to the blocks touching it, every tick. There is
  nothing to set up.
- Generators and batteries pass energy straight into machines from other mods that they
  touch.
- Batteries next to each other even out. Energy moves from the fuller one to the emptier
  one, going by how full each is rather than how much each holds.
- A charger only takes energy in.

## Cables

- A cable joins this mod's blocks: generators, batteries, chargers, cables, importers and
  exporters. A generator or battery touching a cable feeds it.
- A cable does not connect to blocks from other mods. It neither takes energy from them nor
  gives energy to them.
- To take energy from another mod's block, place an importer against it. To give energy to
  another mod's block, place an exporter against it.
- An importer or exporter faces the block it was placed against, and keeps that direction.
  Through that face an importer draws energy in and an exporter sends it out. Its other
  faces join cables as a cable does.
