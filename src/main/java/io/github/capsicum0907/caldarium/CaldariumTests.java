package io.github.capsicum0907.caldarium;

import io.github.capsicum0907.caldarium.data.TestStructures;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
}
