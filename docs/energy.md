# How energy moves

No network, one asymmetry, and a boundary drawn around this mod's own blocks.
Back to the [README](../README.md).

**No network.** Energy is not routed, planned or cabled. Each block, on its tick,
offers what it has to the six blocks touching it, and that is the entire transport
layer. A network would be a second system to keep correct, and nothing here needs
one: a row of batteries already carries, because energy can only move towards the
emptier end.

**What that costs was measured rather than argued about.** Two thousand cables, every
one of them carrying, added about a millisecond to a fifty-millisecond tick — and at
that size it is not distinguishable from the world itself: an empty world's own tick
time moved further between two readings than either this or a cable mod added. So the
honest finding is that they are the same order, not that either wins.

⚠ The other end of the trade is not close, and it is the end nobody thinks to look
at. Two thousand of these are laid in a tenth of a second; two thousand pipes of a mod
that keeps a network took **half a minute**, three times running. A network is walked
again every time it is edited, and there is none here to walk.

**Cables are rows in the same table.** A cable is a battery that holds almost
nothing and moves a great deal, so carrying over distance needed no new mechanism —
only a smaller capacity and a boundary. Three blocks draw that boundary: a **cable**,
an **importer** that draws out of what is outside it, and an **exporter** that feeds
into it. Each has tiers.

**The boundary is about pushing, not about connecting**, and splitting it that way
is what keeps both halves honest. A cable offers only to this mod's own blocks, so
one laid past somebody else's machine does not power it and a corridor of cable is
possible at all. It has no matching rule about being offered *to*: a burner or a
battery standing against a cable fills it directly.

**A door is a cable that also does one thing through the one face it was aimed at.**
That face is the whole of the difference between the three: a cable does nothing
through it, an importer draws out of what is there, an exporter gives to it. Everything
else about them is the same block, so all three join to each other and to a battery of
ours the way any two cables do.

⭐ **And that face is the end of the line rather than part of it.** Through it a door
reaches anything that is not the line and nothing that is, so a cable behind a drill is
not joined to it — from either side, because a door offers nothing to be read on that
face at all. A line that carried on past its own end would have no end.

⚠ The doors had a boundary of their own before this — an importer that accepted
nothing, an exporter that would not offer to the line — and it was true to its own
rules and impossible to hold in your head: two exporters side by side would not join,
nor two importers, but an importer and an exporter would. Which is what a second
boundary buys, and it bought nothing else.

**So a door is optional between two of these and required at the edge of the set.** A
cable laid straight against a burner is fed by it, because the burner pushes and it is
one of ours; a cable laid against another mod's generator is not, however that
generator behaves, and the importer is what fetches from it. Neither arrangement is
the wrong one to reach for.

**The corridor runs both ways, and the second one needed no caller to be identified.**
A cable refuses to offer to another mod's machine, so laying one past a machine will
not power it. The other direction looked impossible: `receiveEnergy` does not say who
is calling, and the capability is handed out without regard to which face asked, on
purpose — a face that behaved differently would be a routing decision, and this mod
does not make those. ⭐ But nobody has to be identified. **Everything that carries
refuses every offer**, and this mod's own push goes around that refusal by handing
over rather than offering. So a generator from another mod cannot fill a cable it is
touching, however willing it is: what reaches the line from outside comes through an
importer, which draws instead of offering, and that is exactly what an importer was
for. **A battery is not sealed**, so anything that can only push still has a port — it
just is not the pipe. ⭐ An exporter is the only way out for the same reason and by the
same one fact: it is the only thing here aimed at giving.

An importer is the only thing here that asks, and it asks through its aimed face only.
An exporter never asks: its way in is something pushing into it, so one stood between
two machines that both wait to be asked does nothing at all, and there is no window on
it to say so.

**The arm is what says so instead.** A block that carries grows one towards every
neighbour energy can actually cross to, and towards no others: a cable laid past
another mod's machine keeps a bare face on that side, and a cable that has joined the
line has an arm on it. That is not decoration. These blocks have no window, no wrench
and nothing to configure, and the rule they follow refuses some neighbours on
purpose — without the arm there is nothing at all to tell a refusal from a line that
has simply not filled yet, and the two look identical for as long as you stare at
them. The arm makes the rule something you look at rather than something you work out.

**And where a door reaches out of the line, the arm is a drill instead**: three square
steps between the thickness of the wire and a flange. A door has two jobs at once —
along the line and across the boundary — and the drill is which of its faces is doing
the second. **A door reaches outside the line on exactly one face**, decided when it is
placed and never again, and the drill is on that face and no other — standing there
before anything is built against it, because where it will work is what you aim it by.
Every other face of it is a plain joint or nothing, the same as a cable's.

⭐ **The same three steps go on either way round, and which way round is what the block
does.** An importer is a mouth and puts its widest step against what it draws from; an
exporter is a nozzle and puts its narrowest against what it feeds. So the direction the
shape narrows in is the direction the energy goes, and that is legible across a room —
which an arrow on a six-pixel face is not.

An importer is a `SOURCE`, the same as a generator: energy that arrives in the line
arrives through one, and nothing may push into it. Where it came from — burnt,
gathered off the sky, or drawn out of somebody else's machine — is not a distinction
anything downstream has to make.

The reason to build these rather than lean on a cable mod is the handling. **No
wrench.** A cable has no direction at all; a door has one and is aimed by being put
down, at whatever it was placed against — the way a hopper faces the block it was set
on, which is also the block an importer is made out of. With no exception: a door
aimed somewhere useless shows it in the drill the moment it lands, which is one break
and one place to put right, and cheaper than a rule with an "except" in it. **No
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
