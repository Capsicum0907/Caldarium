# Types, tiers and the ladder

English | [日本語](tables.ja.md)

How a block gets to exist: what the tables hold, and what a row decides for itself.
Back to the [README](../README.md).

**Generators come in types; everything else comes in tiers.** A type is what a
generator draws on: something solid in a slot, something molten in a tank, or the
sky. What counts as fuel belongs to the type rather than being a rule they all
share — and the two that burn ask the item and the bucket rather than keeping a
list, so a fuel any mod adds is one they already accept.

A tier is how much and how fast, and a kind is what a thing does with what it
holds: a battery hands it on, a charger puts it into what is in its slots. One kind
at one tier is one block, so a kind or a tier added later is a row and not a class.

**The ladder has eight rungs**: copper, iron, gold, diamond, netherite, nether star,
and two compressed stars above that.

**Every row says how far it climbs.** Generators climb all eight; cables, importers
and exporters stop at the nether star. Compressing is worth doing for something that
holds, because it is the same block holding eight times as much, and worth nothing
for something that carries: a line is limited by what is at each end of it rather
than by the line.

**A rung says how much bigger it is than the one under it**, and the steps grow while
the ladder is climbed: two, two, three, four, six. Their geometric mean is about three
- the right average for steps that multiply, since what they have to add up to is a
product rather than a sum. The two compressed rungs step by eight, which is how many
blocks go into one of them, so compressing trades eight blocks for one holding exactly
what they held: what it buys is the space and the tick cost of seven fewer blocks, and
it costs a medium, and none of it is energy made out of arithmetic.

**The height of the ladder was measured against other mods rather than chosen.** The
sixth rung is meant to stand beside what a mod of the same effort offers - Caldarium's
generators are single blocks laid by the handful, so that comparison is Powah's
furnator and panel rather than a multiblock reactor - and the two compressed rungs are
what goes past them. What that came out as, and what it used to be, is in
[2026-09-09/1330 in Atrium](../../Atrium/2026-09-09/1330_fe-scale-comparison/) if that
is to hand; the short version is that the ladder used to multiply by thirty-three
million where Powah's multiplies by two thousand, and now multiplies by eighteen
thousand.

⚠ Nothing reaches the ceiling an int imposes any more, which is what the ladder being
this steep used to cost: a battery that held the same at two rungs running, and a cable
with one tick of slack where the design says two.

**A rung says how it is built.** The first six are the rung below inside a frame of
their own metal. The last two are eight of the rung below squeezed around one thing in
the middle, so they need no material of their own: there is no compressed nether star
in the game and none has to be invented.

The thing in the middle is a ghast tear on the seventh rung and a totem of undying on
the eighth. **Neither is chosen for being dear.** Sixty-four sixth-rung blocks go into
one eighth-rung block and every one of those took four nether stars, so two hundred
and fifty-six withers are the price and a tear beside them is nothing. What the middle
chooses is which places you have to have been - and both of those are places the
withers already took you past, which is why nothing newer is asked for. ⚠ A tear also
predates every version this might be carried back to, which a breeze rod and a heavy
core do not.

**The ladder is expected to grow.** Nothing counts the rungs: the numbers come from
where a tier sits in the list, the slots from the same, the recipe from the rung
below, and the colour from the tier itself. A rung added to the list needs a colour
on its line and, if it is built from a frame, a metal in the recipes - and the
compiler asks for the second one.

**A rung added underneath moves nothing above it.** The starting figures are written
for iron rather than for the first rung, so copper arriving below left every other
rung where it was, and a machine with no rung of its own reads iron by name.

**The one that draws on the sky is a panel rather than a box** — three pixels of
it, because all it needs is the face it points upwards, and a full cube of
machinery underneath would be a cube that does nothing. It asks three questions
and no more: is it day, is the weather clear, and is there anything overhead. Anything solid above it stops it
entirely; anything light passes through takes a share, and what share is a setting
because there is no defensible number for it.

**Fuel goes in and does not come back out.** A hopper under a furnace is the
arrangement everybody builds, and one under a generator would otherwise pull the
coal straight back out of it; a pipe set to extract would empty a tank the same
way. Both refuse. What is left over is not fuel and may still be taken, which is
how the bucket a lava bucket leaves behind gets collected — a tank has no such
leftovers, so fuel put in one by mistake stays there until the block is broken.

## The one that draws on a difference

Heat is not energy; a difference in heat is. A single hot thing has nowhere to send
its heat, so nothing passes through anything, and a generator that made energy from
being next to lava would be making it out of nothing. **Hypocaustum sits between a hot
thing and a cold one and takes a share of what crosses it.**

**Every face has a temperature, and a face with nothing on it reads the biome.** The
game already keeps one number per biome — 0.0 in a snowy plain, 0.8 in plains, 2.0 in
a desert or the nether — and blocks are given values on the same scale in the config.
That one decision folds two machines into one: lava on one face and bare air on the
other is a hot machine, ice on one face and bare air the other is a cold one, and both
are the same block put down differently.

**Three axes, read separately and added.** The block holds three elements, one per
pair of opposite faces, because heat crosses from a face to the one facing it and not
to the one beside it. So every face counts for something, and how much work went into
the arrangement is how much it makes.

⚠ It also means **burying one in lava produces nothing at all**: every axis then has
lava against lava, and the difference on each is zero. That is the rule teaching
itself, and it is why the sixth face was not left out.

⚠ And the nether, where lava is free, is the worst place to run one: the biome reads
2.0, so a bare face there is nearly as hot as the lava on the other side.

It climbs the ladder like every other generator. Placing it better is a second lever
on top of that one, not instead of it.

## The one that runs on what a player earned

Experience is the one resource in the game that a machine cannot make, but it is not a
scarce one: a player who can build a farm for it gets it by the thousand. **So a point is
worth little** - ten FE on the first rung - and a better machine is what makes it worth
more. By the time a farm is built, the rung that pays for it is within reach too.

**It is paid for as it is poured.** Each point is worth the row's own number at once, and
nothing burns afterwards. Only as much is taken as the block has room for, so a full one
takes nothing and no experience is lost into it. There is no slot, no tank and no flame.

**Everything paid at once lights for a moment when it is paid** - experience, a blow, a
death, a strike - so a payment can be seen from where it happened. The face changes; no
light is given off.

**It has tiers**, by the same rule as the panel and for the same reason: there is no
better experience to feed it, so a better machine is the only way it gets better.

⚠ Pouring is buttons rather than a click on the block. A block that drank
experience when you touched it would take it while you were building.

**It is poured in points, not levels.** A level is worth a different amount at every
level, so "one level" was never one amount; the buttons are `pourSteps` points each, plus
all of it, and the screen shows how many points the player has.

⚠⚠ **Points are taken by working out the level again, not through
`giveExperiencePoints`.** Vanilla subtracts by turning the loss into a fraction of the
current level and walking down, in floats: landing exactly on the start of a level could
come out a hair below it and drop a second level. The mod works out what is left, finds
its level and progress in whole numbers, and sets them - and lowers the total so the
client is told.

## Palus, which is paid for being hit

The post a Roman soldier trained against. Hit it and it is worth what the blow was
worth, which is a measurement combat already makes and nothing outside combat ever
uses.

**It is a block, not an entity.** `BlockBehaviour#attack` fires on a left click, so
there is no dummy standing there to be knocked over or killed, and the hard part of the
idea - keeping the thing being hit alive - was never a part of it.

**The blow is worked out the way the game works one out**: the attack damage the player
has, scaled by how far the swing has come back, which is `0.2 + scale * scale * 0.8` in
`Player#attack`. What one point of it is worth is the row's own number, so the ladder
scales the hit. ⚠ **Enchantments are not in it.** The game asks `getEnchantedDamage`
with the thing being hit, and nothing is being hit here, so this measures the arm and
the weapon rather than the sharpness on it. ⚠ **A mace's smash is not in it either**: its
fall bonus is worked out against the thing being hit in the same way, so a drop from a
height is worth one ordinary blow here and the swing's own recovery is the ceiling.

⚠ **The swing is reset afterwards.** Hitting a block does not reset it the way hitting
something alive does, so without that a held-down click lands at full strength every
time and the cooldown is decoration. ⚠ A game test checks exactly that, and caught the
first version of the test instead: a mock player starts with the timer at nought, which
is a player who has just swung, so both blows came out weak and equal.

## Bidental, which is paid in weather

Named for the place the Romans fenced off after lightning struck it. A vanilla
lightning rod goes on top and this stands under it; the rod does the attracting, which
it is already good at within a hundred and twenty-eight blocks of a strike.

**It has no rungs, and a row with no rungs is not given one.** Its numbers are its own,
untouched by the ladder: a billion held, and a quarter of a million a tick out, which
empties a strike in ten seconds and a full one in two hundred. ⚠ It used to be read as
standing on the iron rung - a leftover from when the burner had no ladder either - and
was quietly getting twice the capacity it asked for and a hundredth of the transfer.

**It holds a billion.** What it takes in has to last until the next
storm and it arrives all at once, so the buffer is the machine.

**A storm is worth five hundred times what a trident is.** ⚠ What separates them is one
field on the bolt. The game sets a cause in exactly one place - the enchantment that
calls lightning down - so a cause present means somebody summoned it. ⚠⚠ **A cause
absent means only that nobody said**: weather sets none, a command sets none, a trapped
skeleton horse sets none, and another mod has no reason to set one either. This was
asked about and answered: no cooldown guards the rest, because somebody who has gone as
far as installing a mod that calls lightning down has earned what it gives them.

⚠⚠ **The bolt is judged on its first tick, not when it joins the level.** The enchantment
spawns the bolt first and sets its cause after (`SummonEntityEffect.apply`), so a bolt
read the moment it joined never had a cause, and every trident paid as much as a storm.
Each bolt is counted once.

⚠ It does not read `LightningRodBlock#onLightningStrike`, which is handed the block and
the place but not the bolt, and so cannot say who called it.

⚠ Its recipe is a first pass. What it must not become is the answer to "what do I power
the early game with", since one strike is worth more than anything else here makes in a
minute - so the way in has to sit well past the early game.

## Sol, which makes no energy at all

Not a generator: it is a sun, and what a sun does is shine on the panels somebody else
put down. Inside its reach a solar panel is told it is midday with a clear sky, so a
field of them keeps working at night and underground.

**It is paid for in durability rather than in energy.** A sun that ate energy to let
panels make energy would be either free power or pointless, depending on which way the
sum came out. So it has none: it is crafted whole, it burns for thirty in-game days -
720,000 ticks, at 24,000 a day - and then it is gone. ⚠ **Rain and snow spend it five
times as fast**, which takes those thirty days down to six.

**It is a light and it is hot.** Brightest light the game has, dimming by a third of
its range for each quarter of its durability spent, so how far through it is can be
read off the room. And standing near it sets you alight - a sun that was only bright
would not be a sun. The heat reaches `burnReach` blocks past the surface, measured as a
ball rather than a box, so a sun can be touched from outside the heat as long as the
player's reach is longer than that.

Touching the ball costs `touchDamage` a tick in place of the burn damage - two hundred
a second at its default, which takes a wither down in a second and a half and anything
smaller at once. How much health a thing has is the only thing that buys it any time at
all, which is the one thing that ought to.

⚠ **The touch is a damage type of this mod's own, and deliberately not a fire.** The
game keeps one tag, `is_fire`, for both the fire resistance effect and the mobs born
proof against fire, so a fire cannot turn one aside without turning the other aside too
- and a wither standing in a star unharmed is worse than a potion being no help. The
type carries no `is_fire`, so a blaze, a wither and a warden all burn.

⚠ It does carry `bypasses_cooldown`. Damage normally leaves ten ticks of grace in which
only a bigger hit lands, which would have made ten a tick into ten every twenty. A sun
does not wait.

The heat around it is still an ordinary fire, so a blaze standing beside one is
untroubled by the warmth. It is the surface that kills.

⚠ **Touching means within a skin of the surface, not inside the sphere.** Nothing can
be inside it - the ball is solid - so a mob standing on top of one rests on the solid,
which is built of quarter-block slabs and stands up to about 0.18 of a block proud of
the true sphere. Asking for the sphere itself left everything that climbed a sun taking
the two points of the burn instead of the hundred of the touch. The skin is the slab
step, which is wider than the widest bulge, and a test holds the solid to it.

**It is drawn as a ball, not as a block.** A block model cannot leave its own three
cubes and is made of boxes besides, so the sphere is drawn by a block entity renderer
instead - a cube with its six faces blown out into a ball, sixteen by sixteen quads to a
face, turning slowly on two axes, every vertex at full brightness so it lights rather than being lit. The block itself renders
nothing at all. `size` is its width. It shrinks a step for each quarter of its
durability spent, down to a little over half, so a sun near the end of it is visibly
smaller as well as dimmer.

⚠ **The faces are wound outward.** The render type culls back faces, and the ball was
first wound the other way: the near half was culled and the inside of the far half was
drawn instead. Lit evenly all over, that reads as a ball from a distance and as a bowl
up close.

The size comes from the quarters in the block state, not from the durability count.
The state reaches the client on its own; the count never did, so a size read from it
never shrank.

**It is as solid as it looks, and it is one thing.** The core's shape is the whole ball,
built from sixteen slices across. That shape alone is not enough, because the game only
looks at a block's shape from nearby:

| What | Where the game looks | Source |
|---|---|---|
| Collision | cells the moving box overlaps, plus one around it | `BlockCollisions.computeNext` |
| Clicking | only the cells the line of sight passes through | `BlockGetter.clip`, `traverseBlocks` |
| Reach on the server | the distance to the clicked block's own cell | `Player.canInteractWithBlock` |

So a ball wider than its cell would be walked into, looked through and refused from
most places. `Suns` keeps the loaded suns of each level - added by the block entity's
`onLoad`, dropped by `setRemoved` and `onChunkUnloaded` - and three mixins ask it:

- `Entity.collectColliders` adds the part of any sun near the moving box, cut there and then as quarter-block slabs. The core's own sixteen-slice shape would be four blocks to a step at the largest size; slabs cut to the box stay a quarter block whatever the size, and only as many as the box needs are made.
- `Entity.pick` takes the sun if the line of sight meets it nearer than what vanilla
  found. The meeting point is worked out against a true sphere, not the slices: a line
  running exactly along the seam between two slices meets neither.
- `Player.canInteractWithBlock` measures to the surface rather than to the core's cell,
  so a sun can be broken from outside its heat.

The outline is one wire sphere of three great circles, drawn in place of the stepped
outline the slices would give. From inside - which only a player who is not stopped by
it can reach - the ball is drawn wound the other way, so it still shows. A block cannot
be placed with its centre inside a ball: the right-click is refused before the item is
used, on both sides, so nothing appears and vanishes. A place event check stays behind
it for placements that do not come from a right-click.

**It is held by its near side.** A ball wider than a block, put in the cell beside a
face like any other block, would swallow the player putting it there - a player's reach
is shorter than the ball is wide. So a sun is not put against anything. It is placed
the way it would be carried: out along the line of sight, as far as it takes to leave a
gap of at least `hold` between the ball and the body - the body, not the eyes, or looking
down puts the feet inside it - whether or not a block is being looked at. The gap allows
for the ball snapping to a cell.
A right-click on a block is passed over in favour of that, so where it goes does not
depend on what happens to be under the crosshair.

While a sun is held, the ball it would make is drawn where it would go: white where it
can, red where it cannot. It is part of the interface, so it is hidden with the rest of
it by F1. It cannot go where its core cell is taken, outside the world,
where the core would be inside another sun, or where anything alive is inside the ball -
the last is vanilla's own check, which a sun's shape makes the size of the ball.

**Its icon is the ball.** The item is drawn by the same code as the placed sun, turning
and boiling, through a custom item renderer on a `builtin/entity` model.

**Projectiles burn up in it.** They trace their own lines through `Level.clip`, which none
of the mixins touches, so rather than being stopped they are caught: every tick the sun
looks for projectiles near it whose last move - from where they were to where they are -
crossed the ball, or that are inside it, and removes them with smoke and a hiss. Checking
the move rather than the position is what catches an arrow fast enough to cross the ball
between two ticks.

⚠ **Explosions still pass through.** Their rays go through `Level.clip` as well, and only
the core's own cell is in their way.

⚠ The block is registered with `dynamicShape()`. Its shape reads the config, and block
states build their shape caches while the mod is being constructed, before any config
is loaded; without it the mod fails to start.

⚠ A mixin reaches into the game's own code. If a later build of the game moves these
methods, the mod fails to start rather than quietly losing its shape, because every
injection is required.

**It lights what is around it through a shell of glows.** A block's light reaches fifteen
blocks from its own cell, so the core alone lights nothing outside a ball wider than
about twenty-eight. Around the ball, one block out from its surface, `sol_glow` blocks are
spread evenly - a golden-angle spiral, as many as the shell's area over `glowSpacing`
squared, and never fewer than six - each shining as brightly as the core. They are
invisible, have no shape to walk into or click, give way to anything built there, drop
nothing, and are only ever put into air.

- The core lays them when it is placed, takes them away in `onRemove`, and lays them
  again at the new size when it shrinks.
- Every hundred ticks it lays any that are missing, which covers cells whose chunk was
  not loaded when it tried.
- A glow checks on a random tick that some loaded sun's shell passes through it and
  removes itself if not. A glow in a loaded chunk next to a sun in an unloaded one goes
  too, and comes back when that sun next lays its shell.

**It is drawn to look dangerous rather than pretty.** The colours run from dark red
through red and orange to yellow and stop there - no white - and the heat is weighted
towards the dark end, so the hot cells are islands. `SolPalette` holds the colours for
both the drawn ball and the particle picture. Outside the ball, facing the camera, a
thin red rim cuts it out of the background - thin on the screen, whatever the
size: its width grows with the distance to the edge, not with the ball, the way an inked
outline would - and past the rim an orange corona fades
out; the corona is alpha-blended rather than added, so it stays orange against a blue sky
instead of washing to white.

The corona is two rings drawn one over the other, because a single even band of glow
reads as fog rather than as a sun. The near one is bright, hugs the rim and falls away
in a few tenths of the radius. The far one reaches past twice the radius but is faint,
and its strength varies with the angle round the ball, so it comes out as rays of
uneven length that drift slowly. Both fade as a power of the distance, not in a straight
line, which is what keeps the bright part narrow.

⚠ **The corona is drawn with the lightning shader, for its fog.** `position_color` has
no fog of any kind, so a distant sun faded into the weather while its corona stayed as
sharp as ever. The lightning shader takes the same position-and-colour vertices and
fades them out with the distance. The ball turns towards the colour of the fog and the
corona turns transparent, which is the right end for a glow.

⚠ **The corona is drawn last, and writes no depth.** A render type left at its default
writes depth, and the corona first used one that did: a sun behind another's corona
failed the depth test and vanished. The corona now has its own render type with depth
testing but no depth writing, and it is not drawn with the ball: each sun queues it, and
all of them are drawn after the particles, farthest first, so a nearer corona lies over
a farther sun rather than the other way round. Neither is drawn from inside the ball, nor on the icon.

Every `pulseEvery` ticks it flares for `pulseLength`: the colours slide towards a yellow
palette, and the corona widens, brightens and turns yellow, then all of it settles back.
Each sun is offset by its position, so a row of them does not flare together.

⚠ **The placed ball is not shaded.** The entity shader darkens a face by how far it is
turned from two fixed lights, down to 0.4 of its colour, which made the far side of a
light source look like the dark side of a planet. Every vertex of the placed ball is
given an upward normal, which faces both lights, so it comes out at full colour; in the
Nether one of the lights points down and it comes out at about 0.88. The icon keeps its
real normals, because inventory lighting points elsewhere.

**Its surface is a picture painted from the ball, not wrapped round it.** A flat picture
on a sphere pinches to a point at both poles and stretches along the lines of latitude -
a mirror ball rather than a sun - so the picture is not made flat and then wrapped. Each
of the six faces has its own square of texels, and each texel is coloured by asking the
three-dimensional noise how hot the point of the ball under it is. No pole, and no seam:
every face carries a border of one texel painted the same way, so filtering across an
edge blends into what is really there.

It used to be coloured per vertex instead, and that set the finest grain the ball could
show at the spacing of its vertices - smeared into triangles in between.

**The grain is a size in blocks, not a share of the ball.** The noise is sampled at the
ball's point multiplied by its radius over four, so a sun of width eight looks as it
always did and a wider one has more cells rather than bigger ones. The picture is sized
to match - a texel is about 0.3 blocks, from sixteen to a hundred and sixty to a face.

**It is repainted a little at a time.** Sampling a whole face at the largest size
measured 2.4 ms, so each frame samples half of one face, and the whole ball turns over in
twelve frames; the boil is slow enough that the faces never visibly disagree. The colours
are laid over the stored heat every frame through a 256-entry table, so a flare changes
every face at once. The picture belongs to its block entity and is released when the
block entity leaves the client.

⚠ **Two things kept resetting the picture to nearest filtering**, and every texel showed as
a square until both were dealt with. `DynamicTexture.upload()` passes no blur, so the
picture is uploaded with its own call. And every entity render type sets the filter again
just before drawing, with no blur - `RenderStateShard.TextureStateShard` - so the
picture's texture ignores the filter it is given and always stays linear.

⚠ **The renderer declares the whole ball as its bounds.** A block entity is skipped when
`getRenderBoundingBox` - one cell by default - is outside the view, even in the list of
things drawn from afar, so a sun whose core was off screen vanished with most of its ball
still in view. The bounds cover the ball and the widest corona.

The icon has its own small picture, sixty-four texels to a face with the grain of a sun
four times the reference size, so it reads as a shrunken sun rather than a few big
squares.

⚠ **It takes a diamond pickaxe and nothing else will do.** Not slowly with a stone one
- not at all. A sun anybody can get through given the patience is a sun anybody gets
through. It is as hard as obsidian to the tool that does work.

⚠ **Breaking it is an explosion.** Everything alive within `blastReach` of the surface
takes `blastDamage`, the one who broke it included, and no block is touched. The damage
is dealt directly rather than through a vanilla explosion: at the moment of breaking,
the ball is still there and would shield everything behind it from its own blast.

⚠ **Breaking it destroys it.** A sun that could be picked up would be a sun with a
pause button, and its durability would only ever be spent by somebody who forgot to
take it back in.

⚠ Its reach is one block and that is a placeholder rather than a decision.
