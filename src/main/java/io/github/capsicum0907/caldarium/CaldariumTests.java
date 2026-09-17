package io.github.capsicum0907.caldarium;

import io.github.capsicum0907.caldarium.data.TestStructures;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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

    private static final BlockPos SUN = new BlockPos(2, 2, 2);

    private static BlockPos sun(GameTestHelper helper) {
        helper.setBlock(SUN, CaldariumRegistry.SOL.get());
        return helper.absolutePos(SUN);
    }

    private static int bodies(GameTestHelper helper, BlockPos core) {
        int span = SolBlock.span() + 1;
        int found = 0;
        for (BlockPos at : BlockPos.betweenClosed(core.offset(-span, -span, -span),
                core.offset(span, span, span))) {
            if (helper.getLevel().getBlockState(at).getBlock() instanceof SolBodyBlock) {
                found++;
            }
        }
        return found;
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void aSunFillsItsBall(GameTestHelper helper) {
        BlockPos core = sun(helper);
        BlockState state = helper.getLevel().getBlockState(core);
        List<BlockPos> cells = SolBlock.body(core, SolBlock.radius(state));
        int filled = 0;
        for (BlockPos cell : cells) {
            BlockState there = helper.getLevel().getBlockState(cell);
            if (there.getBlock() instanceof SolBodyBlock) {
                filled++;
            } else {
                check(!there.canBeReplaced(), "an empty cell inside the ball was left open: " + cell);
            }
        }
        check(filled > 0, "a sun should fill its ball");
        check(bodies(helper, core) == filled, "nothing outside the ball should be filled");
        helper.succeed();
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void nothingIsLeftWhenTheSunGoes(GameTestHelper helper) {
        BlockPos core = sun(helper);
        helper.setBlock(SUN, Blocks.AIR);
        check(bodies(helper, core) == 0, "a sun that is gone should leave no body behind: "
                + bodies(helper, core));
        helper.succeed();
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void breakingItsBodyBreaksTheSun(GameTestHelper helper) {
        BlockPos core = sun(helper);
        BlockPos cell = core.above();
        BlockState state = helper.getLevel().getBlockState(cell);
        check(state.getBlock() instanceof SolBodyBlock, "the cell above a sun should be its body");
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        state.getBlock().playerWillDestroy(helper.getLevel(), cell, state, player);
        check(!(helper.getLevel().getBlockState(core).getBlock() instanceof SolBlock),
                "breaking the body should break the sun");
        check(bodies(helper, core) == 0, "and take the rest of the body with it: "
                + bodies(helper, core));
        helper.succeed();
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void aSpentSunGivesUpItsOuterCells(GameTestHelper helper) {
        BlockPos core = sun(helper);
        int whole = bodies(helper, core);
        BlockState spent = helper.getLevel().getBlockState(core).setValue(SolBlock.SPENT, 3);
        helper.getLevel().setBlockAndUpdate(core, spent);
        float radius = SolBlock.radius(spent);
        int span = SolBlock.span() + 1;
        for (BlockPos at : BlockPos.betweenClosed(core.offset(-span, -span, -span),
                core.offset(span, span, span))) {
            BlockState there = helper.getLevel().getBlockState(at);
            if (there.getBlock() instanceof SolBodyBlock) {
                check(at.distSqr(core) <= radius * radius, "a cell outside the smaller ball remained: " + at);
                check(there.getValue(SolBlock.SPENT) == 3, "a remaining cell should dim with the sun");
            }
        }
        check(bodies(helper, core) < whole, "a spent sun should be smaller: "
                + bodies(helper, core) + " against " + whole);
        helper.succeed();
    }
}
