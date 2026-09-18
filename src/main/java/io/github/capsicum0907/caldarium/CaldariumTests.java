package io.github.capsicum0907.caldarium;

import java.util.List;

import io.github.capsicum0907.caldarium.data.TestStructures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Run with {@code gradlew runGameTestServer}.
 */
@GameTestHolder(Caldarium.MODID)
@PrefixGameTestTemplate(false)
public final class CaldariumTests {
    private static final BlockPos WHERE = new BlockPos(2, 1, 2);

    private static void check(boolean held, String what) {
        if (!held) {
            throw new GameTestAssertException(what);
        }
    }

    private static GeneratorBlockEntity spoliarium(GameTestHelper helper) {
        helper.setBlock(WHERE, CaldariumRegistry.generators()
                .get(new Generator.Made(Generator.SPOLIARIUM, Tier.COPPER)).get());
        return (GeneratorBlockEntity) helper.getBlockEntity(WHERE);
    }

    private static GeneratorBlockEntity bidental(GameTestHelper helper) {
        helper.setBlock(WHERE, CaldariumRegistry.generators()
                .get(new Generator.Made(Generator.BIDENTAL, null)).get());
        return (GeneratorBlockEntity) helper.getBlockEntity(WHERE);
    }

    private static GeneratorBlockEntity palus(GameTestHelper helper) {
        helper.setBlock(WHERE, CaldariumRegistry.generators()
                .get(new Generator.Made(Generator.PALUS, Tier.COPPER)).get());
        return (GeneratorBlockEntity) helper.getBlockEntity(WHERE);
    }

    /**
     * ⚠ A swing that has not come back is worth less than one that has.
     *
     * <p>This is the part worth checking. Hitting a block does not reset the swing the
     * way hitting something alive does, so without resetting it here the same click held
     * down would land at full strength every time and the cooldown would be decoration.
     */
    @GameTest(template = TestStructures.FLOOR)
    public static void aRushedBlowIsWorthLess(GameTestHelper helper) {
        GeneratorBlockEntity post = palus(helper);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        // ⚠ A mock player starts with the swing timer at nought, which is a player who
        // has just swung rather than one standing ready. Let it come back first, or both
        // blows are the weak one and the check passes on nothing.
        for (int tick = 0; tick < 20; tick++) {
            player.tick();
        }
        int rested = post.hit(player);
        int rushed = post.hit(player);

        check(rested > 0, "a blow should be worth something: " + rested);
        check(rushed > 0, "and so should the one after it: " + rushed);
        check(rushed < rested,
                "but a swing that has not come back should be worth less: "
                        + rushed + " against " + rested);
        helper.succeed();
    }

    /** ⚠ Nothing else here is paid for being hit. */
    @GameTest(template = TestStructures.FLOOR)
    public static void onlyThePalusTakesABlow(GameTestHelper helper) {
        helper.setBlock(WHERE, CaldariumRegistry.generators()
                .get(new Generator.Made(Generator.BURNER, Tier.COPPER)).get());
        GeneratorBlockEntity burner = (GeneratorBlockEntity) helper.getBlockEntity(WHERE);

        check(burner.hit(helper.makeMockPlayer(GameType.SURVIVAL)) == 0,
                "a burner should take nothing from being hit");
        helper.succeed();
    }

    /**
     * A storm is worth a great deal and a trident is worth very little.
     *
     * <p>⚠ What separates them is one field on the bolt, set in exactly one place in the
     * game: the enchantment that calls lightning down. Weather sets none. So this checks
     * the thing that is actually being relied on rather than the outcome of a storm.
     */
    @GameTest(template = TestStructures.FLOOR)
    public static void aStormIsWorthMoreThanATrident(GameTestHelper helper) {
        GeneratorBlockEntity struck = bidental(helper);
        int natural = struck.strike(false);
        int summoned = struck.strike(true);

        check(natural > 0, "a storm should be worth something: " + natural);
        check(summoned > 0, "and so should a trident: " + summoned);
        check(natural > summoned,
                "but a storm should be worth more: " + natural + " against " + summoned);
        helper.succeed();
    }

    /**
     * ⚠ Nothing else here takes a strike, however close it lands.
     */
    @GameTest(template = TestStructures.FLOOR)
    public static void onlyTheBidentalTakesAStrike(GameTestHelper helper) {
        helper.setBlock(WHERE, CaldariumRegistry.generators()
                .get(new Generator.Made(Generator.BURNER, Tier.COPPER)).get());
        GeneratorBlockEntity burner = (GeneratorBlockEntity) helper.getBlockEntity(WHERE);

        check(burner.strike(false) == 0, "a burner should take nothing from lightning");
        helper.succeed();
    }

    /**
     * What steps on it dies, and what it was holding is what is paid.
     */
    @GameTest(template = TestStructures.FLOOR)
    public static void standingOnItIsFatal(GameTestHelper helper) {
        GeneratorBlockEntity made = spoliarium(helper);
        check(made.store().getEnergyStored() == 0, "it should start empty");

        LivingEntity pig = helper.spawn(EntityType.PIG, WHERE.above());
        int paid = made.reap(helper.getLevel(), pig);

        check(pig.isDeadOrDying(), "a pig that stepped on it should be dead");
        check(paid > 0, "and its health should have been paid for: " + paid);
        check(made.store().getEnergyStored() > 0, "so it holds something: " + made.store().getEnergyStored());
        helper.succeed();
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void aTooltipReadsTheSettings(GameTestHelper helper) {
        List<Tooltips.Row> rows = Tooltips.generator(new Generator.Made(Generator.BURNER, Tier.COPPER));
        check(rows.size() == 3, "makes, holds and sends: " + rows);
        check(rows.get(0).amount().equals("167") && rows.get(0).unit().equals(Tooltips.key("unit.rate")),
                "the rate: " + rows.get(0));
        check(rows.get(1).amount().equals("1,002,000") && rows.get(1).unit().equals(Tooltips.key("unit.stored")),
                "the capacity: " + rows.get(1));
        check(rows.get(2).amount().equals("3,340"), "the transfer: " + rows.get(2));

        Tooltips.Row lamp = Tooltips.generator(new Generator.Made(Generator.LUCERNARIUM, Tier.COPPER)).get(0);
        check(lamp.amount().equals("0.125"), "a rate below one: " + lamp);

        Tooltips.Row point = Tooltips.generator(new Generator.Made(Generator.EXPERIENTIA, Tier.COPPER)).get(0);
        check(point.amount().equals("10") && point.unit().equals(Tooltips.key("unit.point")),
                "experience is paid per point: " + point);
        helper.succeed();
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void whatDiesThereDropsNothing(GameTestHelper helper) {
        GeneratorBlockEntity made = spoliarium(helper);
        LivingEntity pig = helper.spawn(EntityType.PIG, WHERE.above());
        made.reap(helper.getLevel(), pig);
        helper.runAfterDelay(2, () -> {
            var near = new net.minecraft.world.phys.AABB(helper.absolutePos(WHERE)).inflate(4);
            var left = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, near);
            check(pig.isDeadOrDying(), "the pig should be dead");
            check(left.isEmpty(), "and have left nothing behind: " + left);
            helper.succeed();
        });
    }

    @GameTest(template = TestStructures.FLOOR, timeoutTicks = 60)
    public static void aPaymentLightsItForAMoment(GameTestHelper helper) {
        GeneratorBlockEntity made = spoliarium(helper);
        made.reap(helper.getLevel(), helper.spawn(EntityType.PIG, WHERE.above()));
        helper.startSequence()
                .thenExecuteAfter(2, () -> check(helper.getBlockState(WHERE).getValue(GeneratorBlock.LIT),
                        "it should light when it is paid"))
                .thenExecuteAfter(20, () -> check(!helper.getBlockState(WHERE).getValue(GeneratorBlock.LIT),
                        "and go out again soon after"))
                .thenSucceed();
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void aRungKillsNothingLargerThanItsLimit(GameTestHelper helper) {
        GeneratorBlockEntity made = spoliarium(helper);

        LivingEntity golem = helper.spawn(EntityType.IRON_GOLEM, WHERE.above());
        int paid = made.reap(helper.getLevel(), golem);

        check(!golem.isDeadOrDying(), "an iron golem should outlast the first rung");
        check(golem.getHealth() == golem.getMaxHealth(), "and not even be hurt: " + golem.getHealth());
        check(paid == 0, "and nothing should have been paid for it: " + paid);
        helper.succeed();
    }

    /**
     * ⚠ Nothing is paid for a blow that did not land.
     *
     * <p>Written for a real mod rather than a hypothetical one. MmmMmmMmmMmm adds a
     * target dummy that reads out the damage dealt to it and survives everything but
     * the void, a wither and {@code /kill} - so a dummy left standing here would be a
     * generator that never stops paying for a kill that never happens. The dummy is not
     * a dependency; what is checked is the shape it has, which is one that cannot die.
     */
    @GameTest(template = TestStructures.FLOOR)
    public static void whatCannotDiePaysNothing(GameTestHelper helper) {
        GeneratorBlockEntity made = spoliarium(helper);

        LivingEntity dummy = helper.spawn(EntityType.PIG, WHERE.above());
        dummy.setInvulnerable(true);
        int paid = made.reap(helper.getLevel(), dummy);

        check(!dummy.isDeadOrDying(), "the invulnerable one should still be standing");
        check(paid == 0, "and nothing should have been paid for it: " + paid);
        check(made.store().getEnergyStored() == 0, "so it holds nothing: " + made.store().getEnergyStored());
        helper.succeed();
    }

    /**
     * ⚠ And it stays nothing however long it stands there.
     */
    @GameTest(template = TestStructures.FLOOR)
    public static void whatCannotDieNeverPays(GameTestHelper helper) {
        GeneratorBlockEntity made = spoliarium(helper);

        LivingEntity dummy = helper.spawn(EntityType.PIG, WHERE.above());
        dummy.setInvulnerable(true);
        for (int tick = 0; tick < 100; tick++) {
            made.reap(helper.getLevel(), dummy);
        }

        check(made.store().getEnergyStored() == 0,
                "a hundred ticks of standing on it should still be nothing: "
                        + made.store().getEnergyStored());
        helper.succeed();
    }

    /**
     * What was already hurt pays only for what was left.
     */
    @GameTest(template = TestStructures.FLOOR)
    public static void itPaysForWhatWasThereRatherThanWhatWasWhole(GameTestHelper helper) {
        GeneratorBlockEntity whole = spoliarium(helper);
        LivingEntity full = helper.spawn(EntityType.PIG, WHERE.above());
        int forWhole = whole.reap(helper.getLevel(), full);

        helper.setBlock(WHERE, net.minecraft.world.level.block.Blocks.AIR);
        GeneratorBlockEntity hurt = spoliarium(helper);
        LivingEntity wounded = helper.spawn(EntityType.PIG, WHERE.above());
        wounded.setHealth(1.0F);
        int forWounded = hurt.reap(helper.getLevel(), wounded);

        check(forWounded < forWhole,
                "a wounded one should be worth less than a whole one: "
                        + forWounded + " against " + forWhole);
        helper.succeed();
    }

    private static final String SOL_BATCH = "sol";
    private static final double SOL_SIZE = 8.0;
    private static final double SOL_HOLD = 2.0;
    private static double sizeWas;
    private static double holdWas;
    private static int reachWas;

    @BeforeBatch(batch = SOL_BATCH)
    public static void fixTheSun(ServerLevel level) {
        sizeWas = CaldariumConfig.SOL_SIZE.get();
        holdWas = CaldariumConfig.SOL_HOLD.get();
        CaldariumConfig.SOL_SIZE.set(SOL_SIZE);
        CaldariumConfig.SOL_HOLD.set(SOL_HOLD);
        reachWas = CaldariumConfig.SOL_REACH.get();
        CaldariumConfig.SOL_REACH.set(0);
    }

    @AfterBatch(batch = SOL_BATCH)
    public static void restoreTheSun(ServerLevel level) {
        CaldariumConfig.SOL_SIZE.set(sizeWas);
        CaldariumConfig.SOL_HOLD.set(holdWas);
        CaldariumConfig.SOL_REACH.set(reachWas);
    }

    private static final BlockPos SUN = new BlockPos(TestStructures.HALL_SIZE / 2, TestStructures.HALL_SIZE / 2,
            TestStructures.HALL_SIZE / 2);
    private static final double TOLERANCE = 0.25;

    private static BlockPos sun(GameTestHelper helper) {
        helper.setBlock(SUN, CaldariumRegistry.SOL.get());
        return helper.absolutePos(SUN);
    }

    private static float radius(GameTestHelper helper, BlockPos core) {
        return SolBlock.radius(helper.getLevel().getBlockState(core));
    }

    private static Player standing(GameTestHelper helper, Vec3 eye, float yaw) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(eye.x, eye.y - player.getEyeHeight(), eye.z);
        player.xo = player.getX();
        player.yo = player.getY();
        player.zo = player.getZ();
        player.setYRot(yaw);
        player.setXRot(0.0F);
        player.yRotO = yaw;
        player.xRotO = 0.0F;
        player.setYHeadRot(yaw);
        player.yHeadRotO = yaw;
        return player;
    }

    private static Vec3 pushed(GameTestHelper helper, Vec3 start, double by) {
        AABB box = AABB.ofSize(start, 0.5, 0.5, 0.5);
        return Entity.collideBoundingBox(null, new Vec3(-by, 0.0, 0.0), box, helper.getLevel(), List.of());
    }

    @GameTest(template = TestStructures.HALL, batch = SOL_BATCH)
    public static void aSunStopsWhatMovesIntoIt(GameTestHelper helper) {
        BlockPos core = sun(helper);
        helper.runAfterDelay(1, () -> {
            float radius = radius(helper, core);
            Vec3 start = SolBlock.centre(core).add(radius + 1.0, 0.0, 0.0);
            Vec3 moved = pushed(helper, start, 1.0);
            check(Math.abs(moved.x + 0.75) < TOLERANCE,
                    "a box moving into the sun should stop at its surface: " + moved.x);
            helper.succeed();
        });
    }

    @GameTest(template = TestStructures.HALL, batch = SOL_BATCH)
    public static void aSunReachesPanelsWellOutsideItsBall(GameTestHelper helper) {
        BlockPos core = sun(helper);
        helper.runAfterDelay(1, () -> {
            int out = (int) Math.ceil(radius(helper, core)) + 6;
            BlockPos panel = core.offset(out, 0, 0);
            int reachWas = CaldariumConfig.SOL_REACH.get();
            CaldariumConfig.SOL_REACH.set(7);
            boolean near = Suns.shining(helper.getLevel(), panel);
            float sun = Sunlight.reaching(helper.getLevel(), panel);
            float lamp = Lamplight.reaching(helper.getLevel(), panel);
            CaldariumConfig.SOL_REACH.set(2);
            boolean beyond = Suns.shining(helper.getLevel(), panel);
            CaldariumConfig.SOL_REACH.set(reachWas);
            check(near, "a place six blocks past the ball should count as sunlit");
            check(sun == 1.0F, "so a solar panel there should read full daylight: " + sun);
            check(lamp == 1.0F, "and a lucernarium full light: " + lamp);
            check(!beyond, "but not once the reach is shorter than the gap");
            helper.succeed();
        });
    }

    @GameTest(template = TestStructures.HALL, batch = SOL_BATCH)
    public static void aSunLightsTheGroundWithinItsReach(GameTestHelper helper) {
        BlockPos core = sun(helper);
        BlockPos floor = helper.absolutePos(new BlockPos(SUN.getX(), 2, SUN.getZ()));
        int was = CaldariumConfig.SOL_REACH.get();
        CaldariumConfig.SOL_REACH.set(7);
        BlockState state = helper.getLevel().getBlockState(core);
        SolBlock.glow(helper.getLevel(), core, state);
        boolean lit = helper.getLevel().getBlockState(floor).getBlock() instanceof SolGlowBlock;
        SolBlock.unglow(helper.getLevel(), core, state);
        boolean cleared = !(helper.getLevel().getBlockState(floor).getBlock() instanceof SolGlowBlock);
        CaldariumConfig.SOL_REACH.set(was);
        check(lit, "the floor under a sun within its reach should carry a glow");
        check(cleared, "and lose it when the sun goes");
        helper.succeed();
    }

    @GameTest(template = TestStructures.HALL, batch = SOL_BATCH)
    public static void aSunHurtsCreaturesWithinItsReach(GameTestHelper helper) {
        BlockPos core = sun(helper);
        BlockState state = helper.getLevel().getBlockState(core);
        LivingEntity pig = helper.spawn(EntityType.PIG,
                SUN.offset((int) Math.ceil(SolBlock.radius(state)) + 4, 0, 0));
        int was = CaldariumConfig.SOL_REACH.get();
        CaldariumConfig.SOL_REACH.set(2);
        SolBlockEntity.harmWhatIsInReach(helper.getLevel(), core, state);
        float beyond = pig.getHealth();
        CaldariumConfig.SOL_REACH.set(7);
        SolBlockEntity.harmWhatIsInReach(helper.getLevel(), core, state);
        float within = pig.getHealth();
        CaldariumConfig.SOL_REACH.set(was);
        check(beyond == pig.getMaxHealth(), "a pig past the reach should be untouched: " + beyond);
        check(within < beyond, "and a pig within it hurt: " + within);
        helper.succeed();
    }

    @GameTest(template = TestStructures.HALL, batch = SOL_BATCH)
    public static void aSunThatIsGoneStopsNothing(GameTestHelper helper) {
        BlockPos core = sun(helper);
        helper.runAfterDelay(1, () -> {
            float radius = radius(helper, core);
            helper.setBlock(SUN, Blocks.AIR);
            Vec3 moved = pushed(helper, SolBlock.centre(core).add(radius + 1.0, 0.0, 0.0), 2.0);
            check(moved.x == -2.0, "nothing should be left in the way: " + moved.x);
            helper.succeed();
        });
    }

    @GameTest(template = TestStructures.HALL, batch = SOL_BATCH)
    public static void aSunIsReachedAtItsSurface(GameTestHelper helper) {
        BlockPos core = sun(helper);
        helper.runAfterDelay(1, () -> {
            float radius = radius(helper, core);
            Player near = standing(helper, SolBlock.centre(core).add(radius + 4.0, 0.0, 0.0), 90.0F);
            check(near.canInteractWithBlock(core, 1.0), "four blocks from its surface should be in reach");
            Player far = standing(helper, SolBlock.centre(core).add(radius + 6.0, 0.0, 0.0), 90.0F);
            check(!far.canInteractWithBlock(core, 1.0), "six blocks from its surface should not");
            helper.succeed();
        });
    }

    @GameTest(template = TestStructures.HALL, batch = SOL_BATCH)
    public static void lookingAtASunLandsOnIt(GameTestHelper helper) {
        BlockPos core = sun(helper);
        helper.runAfterDelay(1, () -> {
            float radius = radius(helper, core);
            Player player = standing(helper, SolBlock.centre(core).add(radius + 3.0, 0.0, 0.0), 90.0F);
            HitResult hit = player.pick(3.5, 1.0F, false);
            check(hit instanceof BlockHitResult block && block.getBlockPos().equals(core),
                    "looking at the sun should land on it: " + hit.getType());
            double distance = hit.getLocation().distanceTo(player.getEyePosition());
            check(Math.abs(distance - 3.0) < TOLERANCE, "and on its surface: " + distance);
            helper.succeed();
        });
    }

    @GameTest(template = TestStructures.HALL, batch = SOL_BATCH)
    public static void nothingIsPlacedInsideASun(GameTestHelper helper) {
        BlockPos core = sun(helper);
        helper.runAfterDelay(1, () -> {
            float radius = radius(helper, core);
            check(Suns.inside(helper.getLevel(), core.above()), "the cell above the core is inside the ball");
            check(!Suns.inside(helper.getLevel(), core.above((int) Math.ceil(radius) + 1)),
                    "a cell past the surface is not");
            helper.succeed();
        });
    }

    @GameTest(template = TestStructures.HALL, batch = SOL_BATCH)
    public static void breakingASunKillsWhatIsNearButLeavesTheBlocks(GameTestHelper helper) {
        BlockPos core = sun(helper);
        helper.runAfterDelay(1, () -> {
            float radius = radius(helper, core);
            BlockPos beside = SUN.east((int) Math.ceil(radius) + 1);
            helper.setBlock(beside, Blocks.STONE);
            LivingEntity pig = helper.spawn(EntityType.PIG, beside.above());
            BlockState state = helper.getLevel().getBlockState(core);
            Player breaker = helper.makeMockPlayer(GameType.SURVIVAL);
            state.getBlock().playerWillDestroy(helper.getLevel(), core, state, breaker);
            check(!pig.isAlive(), "a pig beside a sun that is broken should die");
            check(helper.getBlockState(beside).is(Blocks.STONE), "and the blocks around it should stay");
            helper.succeed();
        });
    }

    @GameTest(template = TestStructures.HALL, batch = SOL_BATCH)
    public static void placingIntoASunIsRefusedBeforeItHappens(GameTestHelper helper) {
        BlockPos core = sun(helper);
        helper.runAfterDelay(1, () -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE));
            BlockHitResult aim = new BlockHitResult(SolBlock.centre(core), Direction.UP, core, false);
            PlayerInteractEvent.RightClickBlock event = NeoForge.EVENT_BUS.post(
                    new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, core, aim));
            check(event.getUseItem() == TriState.FALSE, "a block aimed into a sun should not be used at all");
            helper.succeed();
        });
    }

    private static Player holdingASun(GameTestHelper helper) {
        Vec3 eye = Vec3.atCenterOf(helper.absolutePos(new BlockPos(1, SUN.getY(), SUN.getZ())));
        Player player = standing(helper, eye, -90.0F);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(CaldariumRegistry.SOL_ITEM.get()));
        return player;
    }

    @GameTest(template = TestStructures.HALL, batch = SOL_BATCH)
    public static void aSunIsHeldByItsNearSide(GameTestHelper helper) {
        Player player = holdingASun(helper);
        BlockState placed = CaldariumRegistry.SOL.get().defaultBlockState();
        BlockPos core = SolItem.core(player, placed);
        player.getMainHandItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        check(helper.getLevel().getBlockState(core).getBlock() instanceof SolBlock,
                "a sun should be set down where it was held, at " + core);
        double gap = SolItem.gap(player.getBoundingBox(), SolBlock.centre(core)) - SolBlock.radius(placed);
        check(gap >= CaldariumConfig.solHold(), "and held clear of the body by at least the hold: " + gap);
        helper.succeed();
    }

    @GameTest(template = TestStructures.HALL, batch = SOL_BATCH)
    public static void aSunHeldBelowStaysClearOfTheBody(GameTestHelper helper) {
        Vec3 eye = Vec3.atCenterOf(helper.absolutePos(new BlockPos(SUN.getX(), TestStructures.HALL_SIZE - 2, SUN.getZ())));
        Player player = standing(helper, eye, 0.0F);
        player.setXRot(90.0F);
        player.xRotO = 90.0F;
        BlockState placed = CaldariumRegistry.SOL.get().defaultBlockState();
        BlockPos core = SolItem.core(player, placed);
        double gap = SolItem.gap(player.getBoundingBox(), SolBlock.centre(core)) - SolBlock.radius(placed);
        check(gap >= CaldariumConfig.solHold(), "looking straight down, the feet should be clear too: " + gap);
        helper.succeed();
    }

    @GameTest(template = TestStructures.HALL, batch = SOL_BATCH)
    public static void aSunIsNotSetDownOnSomethingAlive(GameTestHelper helper) {
        Player player = holdingASun(helper);
        BlockPos core = SolItem.core(player, CaldariumRegistry.SOL.get().defaultBlockState());
        LivingEntity pig = EntityType.PIG.create(helper.getLevel());
        pig.moveTo(Vec3.atBottomCenterOf(core));
        helper.getLevel().addFreshEntity(pig);
        player.getMainHandItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        check(!(helper.getLevel().getBlockState(core).getBlock() instanceof SolBlock),
                "a sun whose ball would hold a pig should not be set down");
        helper.succeed();
    }

    @GameTest(template = TestStructures.HALL, batch = SOL_BATCH)
    public static void aLargeSunIsSmoothToTheTouch(GameTestHelper helper) {
        double radius = 32.0;
        Vec3 centre = Vec3.ZERO;
        Vec3 slant = new Vec3(1.0, 1.0, 1.0).normalize();
        for (double off : new double[] { -0.3, 0.3 }) {
            Vec3 point = slant.scale(radius + off);
            AABB probe = AABB.ofSize(point, 0.05, 0.05, 0.05);
            boolean touched = SolBlock.slabs(radius, centre, probe).stream()
                    .anyMatch(slab -> slab.bounds().intersects(probe));
            check(touched == (off < 0.0), "a point " + off + " from the surface of a wide sun should "
                    + (off < 0.0 ? "" : "not ") + "be inside it");
        }
        helper.succeed();
    }

    @GameTest(template = TestStructures.HALL, batch = SOL_BATCH)
    public static void anArrowBurnsUpPassingThroughASun(GameTestHelper helper) {
        BlockPos core = sun(helper);
        BlockState state = helper.getLevel().getBlockState(core);
        float radius = SolBlock.radius(state);
        Vec3 centre = SolBlock.centre(core);
        Arrow through = new Arrow(EntityType.ARROW, helper.getLevel());
        through.setPos(centre.add(-radius - 1.0, 0.0, 0.0));
        through.xo = through.getX();
        through.yo = through.getY();
        through.zo = through.getZ();
        through.setPos(centre.add(radius + 1.0, 0.0, 0.0));
        helper.getLevel().addFreshEntity(through);
        Arrow clear = new Arrow(EntityType.ARROW, helper.getLevel());
        clear.setPos(centre.add(radius + 2.0, 0.0, 0.0));
        clear.xo = clear.getX();
        clear.yo = clear.getY();
        clear.zo = clear.getZ();
        helper.getLevel().addFreshEntity(clear);
        SolBlockEntity.burnWhatFlies(helper.getLevel(), core, state);
        check(through.isRemoved(), "an arrow whose last move crossed the sun should burn up");
        check(!clear.isRemoved(), "an arrow that stayed clear of it should not");
        helper.succeed();
    }

    private static <T extends LivingEntity> T livingAt(GameTestHelper helper, EntityType<T> what, Vec3 where) {
        T living = what.create(helper.getLevel());
        living.moveTo(where);
        helper.getLevel().addFreshEntity(living);
        return living;
    }

    private static float lost(LivingEntity living) {
        return living.getMaxHealth() - living.getHealth();
    }

    @GameTest(template = TestStructures.HALL, batch = SOL_BATCH)
    public static void whatTouchesASunIsHurtFarWorseThanWhatStandsNearIt(GameTestHelper helper) {
        BlockPos core = sun(helper);
        BlockState state = helper.getLevel().getBlockState(core);
        double radius = SolBlock.radius(state);
        Vec3 centre = SolBlock.centre(core);
        double touchWas = CaldariumConfig.SOL_TOUCH_DAMAGE.get();
        double bite = 3.0;
        try {
            CaldariumConfig.SOL_TOUCH_DAMAGE.set(bite);
            LivingEntity inside = livingAt(helper, EntityType.PIG, centre);
            LivingEntity fireproof = livingAt(helper, EntityType.BLAZE, centre);
            double half = inside.getBbWidth() / 2.0;
            LivingEntity against = livingAt(helper, EntityType.PIG,
                    centre.add(radius + SolBlock.SKIN + half, 0.0, 0.0));
            LivingEntity beside = livingAt(helper, EntityType.PIG,
                    centre.add(radius + SolBlock.SKIN + half + 0.1, 0.0, 0.0));
            SolBlockEntity.burnWhatIsNear(helper.getLevel(), core, state);
            SolBlockEntity.burnWhatIsNear(helper.getLevel(), core, state);

            check(Math.abs(lost(inside) - 2.0 * bite) < 0.01F,
                    "a sun should not wait out the hurt cooldown: " + lost(inside));
            check(Math.abs(lost(against) - 2.0 * bite) < 0.01F,
                    "one resting on the solid should be touching it: " + lost(against));
            check(Math.abs(lost(fireproof) - 2.0 * bite) < 0.01F,
                    "being proof against fire should not be being proof against a sun: " + lost(fireproof));
            check(Math.abs(lost(beside) - CaldariumConfig.solBurnDamage()) < 0.01F,
                    "one beside it should take the burn damage, once: " + lost(beside));
        } finally {
            CaldariumConfig.SOL_TOUCH_DAMAGE.set(touchWas);
        }
        helper.succeed();
    }

    @GameTest(template = TestStructures.HALL, batch = SOL_BATCH)
    public static void aSunIsSolidNoFurtherOutThanItsSkin(GameTestHelper helper) {
        double radius = 8.0;
        Vec3 centre = Vec3.ZERO;
        AABB whole = AABB.ofSize(centre, 0.0, 0.0, 0.0).inflate(radius + 1.0);
        double worst = 0.0;
        for (var slab : SolBlock.slabs(radius, centre, whole)) {
            AABB box = slab.bounds();
            for (double x : new double[] { box.minX, box.maxX }) {
                for (double y : new double[] { box.minY, box.maxY }) {
                    for (double z : new double[] { box.minZ, box.maxZ }) {
                        worst = Math.max(worst, new Vec3(x, y, z).distanceTo(centre) - radius);
                    }
                }
            }
        }
        check(worst > 0.0, "the solid should stand proud of the sphere somewhere: " + worst);
        check(worst <= SolBlock.SKIN, "but never further out than its skin: " + worst);
        helper.succeed();
    }

    @GameTest(template = TestStructures.HALL, batch = SOL_BATCH)
    public static void aSunLightsWhatIsAroundIt(GameTestHelper helper) {
        BlockPos core = sun(helper);
        BlockState state = helper.getLevel().getBlockState(core);
        List<BlockPos> cells = SolBlock.glowCells(core, SolBlock.radius(state));
        int lit = 0;
        for (BlockPos cell : cells) {
            BlockState there = helper.getLevel().getBlockState(cell);
            if (there.getBlock() instanceof SolGlowBlock) {
                lit++;
                check(there.getLightEmission() == SolBlock.light(state), "a glow should shine as the sun does");
                check(there.canBeReplaced(), "a glow should give way to anything built there");
                check(there.getCollisionShape(helper.getLevel(), cell).isEmpty(), "a glow should not be in the way");
            }
        }
        check(lit > 0, "a sun should light its surroundings");
        helper.succeed();
    }

    @GameTest(template = TestStructures.HALL, batch = SOL_BATCH)
    public static void nothingGlowsWhenTheSunGoes(GameTestHelper helper) {
        BlockPos core = sun(helper);
        List<BlockPos> cells = SolBlock.glowCells(core, SolBlock.radius(helper.getLevel().getBlockState(core)));
        helper.setBlock(SUN, Blocks.AIR);
        for (BlockPos cell : cells) {
            check(!(helper.getLevel().getBlockState(cell).getBlock() instanceof SolGlowBlock),
                    "a glow was left behind at " + cell);
        }
        helper.succeed();
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void aBoltCalledDownIsCountedAsCalled(GameTestHelper helper) {
        GeneratorBlockEntity struck = bidental(helper);
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(helper.getLevel());
        bolt.moveTo(Vec3.atBottomCenterOf(helper.absolutePos(WHERE.above())));
        helper.getLevel().addFreshEntity(bolt);
        bolt.setCause(FakePlayerFactory.getMinecraft(helper.getLevel()));
        helper.runAfterDelay(1, () -> {
            int stored = struck.store().getEnergyStored();
            check(stored == CaldariumConfig.stormSummoned(),
                    "a bolt whose cause is set after it is spawned, as a trident does, should count as summoned: "
                            + stored);
            helper.succeed();
        });
    }

    private static void holding(Player player, int points) {
        int level = 0;
        while (Experience.total(level + 1) <= points) {
            level++;
        }
        player.experienceLevel = level;
        player.experienceProgress = (points - Experience.total(level)) / (float) Experience.span(level);
        player.totalExperience = points;
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void experienceIsTakenToThePoint(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        int[] starts = { 0, 7, 16, 17, 100, 352, 353, 1395, 1396, 5000, 30970, 1_000_000 };
        int[] amounts = { 1, 10, 100, 1000, 10_000 };
        for (int start : starts) {
            for (int amount : amounts) {
                holding(player, start);
                check(Experience.points(player) == start, "set up " + start + " but read " + Experience.points(player));
                int taken = Experience.take(player, amount);
                int expected = Math.max(0, start - amount);
                check(taken == start - expected, "from " + start + ", taking " + amount + " took " + taken);
                check(Experience.points(player) == expected,
                        "from " + start + ", taking " + amount + " should leave " + expected + " but left "
                                + Experience.points(player) + " at level " + player.experienceLevel);
                check(player.experienceProgress >= 0.0F && player.experienceProgress < 1.0F,
                        "progress out of range: " + player.experienceProgress);
            }
        }
        for (int level = 1; level < 60; level++) {
            for (int into = 0; into < Experience.span(level); into += 3) {
                int start = Experience.total(level) + into;
                int amount = start - Experience.total(level - 1);
                holding(player, start);
                Experience.take(player, amount);
                check(player.experienceLevel == level - 1 && Experience.points(player) == Experience.total(level - 1),
                        "landing on the start of level " + (level - 1) + " from " + start + " left level "
                                + player.experienceLevel + " and " + Experience.points(player) + " points");
            }
        }
        helper.succeed();
    }
}
