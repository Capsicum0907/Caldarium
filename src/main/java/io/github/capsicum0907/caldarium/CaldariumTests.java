package io.github.capsicum0907.caldarium;

import java.util.List;

import io.github.capsicum0907.caldarium.data.TestStructures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
        check(made.burning() == 0, "it should start with nothing to burn");

        LivingEntity pig = helper.spawn(EntityType.PIG, WHERE.above());
        int paid = made.reap(helper.getLevel(), pig);

        check(pig.isDeadOrDying(), "a pig that stepped on it should be dead");
        check(paid > 0, "and its health should have been paid for: " + paid);
        check(made.burning() > 0, "so it has something to burn: " + made.burning());
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
        check(made.burning() == 0, "so there is nothing to burn: " + made.burning());
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

        check(made.burning() == 0,
                "a hundred ticks of standing on it should still be nothing: "
                        + made.burning());
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

    @GameTest(template = TestStructures.HALL)
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

    @GameTest(template = TestStructures.HALL)
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

    @GameTest(template = TestStructures.HALL)
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

    @GameTest(template = TestStructures.HALL)
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

    @GameTest(template = TestStructures.HALL)
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

    @GameTest(template = TestStructures.HALL)
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

    @GameTest(template = TestStructures.HALL)
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

    @GameTest(template = TestStructures.HALL)
    public static void aSunIsHeldByItsNearSide(GameTestHelper helper) {
        Player player = holdingASun(helper);
        BlockState placed = CaldariumRegistry.SOL.get().defaultBlockState();
        BlockPos core = SolItem.core(player, placed);
        player.getMainHandItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        check(helper.getLevel().getBlockState(core).getBlock() instanceof SolBlock,
                "a sun should be set down where it was held, at " + core);
        double gap = player.getEyePosition().distanceTo(SolBlock.centre(core)) - SolBlock.radius(placed);
        check(gap > 0.0, "and the one holding it should be outside its ball: " + gap);
        helper.succeed();
    }

    @GameTest(template = TestStructures.HALL)
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
}
