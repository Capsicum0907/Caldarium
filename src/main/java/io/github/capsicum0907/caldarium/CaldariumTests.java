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
