# How energy moves

English | [日本語](energy.ja.md)

Back to the [README](../README.md).

- Generators and batteries pass energy every tick to the machines touching them, from this
  mod or from others. There is nothing to set up.
- Batteries next to each other even out. Energy moves from the fuller one to the emptier
  one, going by how full each is rather than how much each holds.
- A charger only takes energy in.
- Machines do not pass energy to cables, and cables do not pass energy to machines.

## Cables

- Cables connect only to cables, importers and exporters. A machine, from this mod or from
  another, connects to the line through an importer or an exporter.
- An importer takes energy out of the block it faces and passes it to the cables next to
  it whenever they have room.
- An exporter takes energy from the cables next to it whenever it has room, and sends it
  into the block it faces.
- Cables pass energy to each other, towards the emptier one. Energy does not go back into
  the cable it just came from.
- An importer or exporter faces the block that was clicked to place it, and keeps that
  direction.
