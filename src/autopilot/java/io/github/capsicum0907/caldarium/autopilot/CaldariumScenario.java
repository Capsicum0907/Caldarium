package io.github.capsicum0907.caldarium.autopilot;

import io.github.capsicum0907.caldarium.Caldarium;
import io.github.capsicum0907.caldarium.CaldariumConfig;
import io.github.capsicum0907.caldarium.CaldariumRegistry;
import io.github.capsicum0907.caldarium.FuelTank;
import io.github.capsicum0907.caldarium.GeneratorBlockEntity;
import io.github.capsicum0907.caldarium.MachineMenu;
import io.github.capsicum0907.caldarium.Tooltips;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

public final class CaldariumScenario {
    private static final String WORLD = System.getProperty("autopilot.world", "Test World");
    private static final long TIMEOUT_SECONDS = Long.getLong("autopilot.timeout", 420L);
    private static final int Y = 200;
    private static final int SETTLE = 40;
    private static final int SHORT = 10;
    private static final int BURN = 60;
    private static final double SUN_SIZE = 8.0;
    private static final double SUN_HOLD = 2.0;
    private static final int LAVA = 4000;
    private static final int COAL = 8;
    private static final int RECIPES_AT_LEAST = 90;
    private static final double NEAR = 4.0;
    private static final double FAR = 6.5;

    private static final BlockPos BURNER = new BlockPos(2, Y, 4);
    private static final BlockPos CRUCIBLE = new BlockPos(4, Y, 4);
    private static final BlockPos EXPERIENTIA = new BlockPos(6, Y, 4);
    private static final BlockPos SINK_BURNER = new BlockPos(8, Y, 4);
    private static final BlockPos SINK = new BlockPos(9, Y, 4);
    private static final BlockPos SUN = new BlockPos(0, Y + 6, 24);

    private CaldariumScenario() {
    }

    public static Pilot create() {
        Minecraft minecraft = Minecraft.getInstance();
        Pilot pilot = new Pilot(minecraft.gameDirectory.toPath().resolve("autopilot").resolve("result.txt"),
                TIMEOUT_SECONDS);
        double centreZ = SUN.getZ() + 0.5;
        double radius = SUN_SIZE / 2.0;

        pilot.until("title screen", () -> minecraft.screen instanceof TitleScreen)
                .then("open " + WORLD, 0, () -> pilot.openWorld(WORLD))
                .until("in the world", () -> minecraft.player != null && minecraft.level != null
                        && minecraft.getSingleplayerServer() != null && minecraft.player.tickCount > SETTLE)
                .then("settle the world", SETTLE, () -> {
                    pilot.command("gamerule doDaylightCycle false");
                    pilot.command("gamerule doWeatherCycle false");
                    pilot.command("gamerule doMobSpawning false");
                    pilot.command("time set noon");
                    pilot.command("weather clear");
                    pilot.command("gamemode creative");
                    pilot.command("tp @s 0.5 " + Y + " 0.5 0 0");
                    CaldariumConfig.hold(CaldariumConfig.SOL_SIZE, SUN_SIZE);
                    CaldariumConfig.hold(CaldariumConfig.SOL_HOLD, SUN_HOLD);
                    pilot.onServer(() -> {
                        //? if >=1.17 {
                        pilot.serverPlayer().getAbilities().flying = true;
                        //?} else
                        /*pilot.serverPlayer().abilities.flying = true;*/
                        pilot.serverPlayer().onUpdateAbilities();
                        return null;
                    });
                })
                .then("count the recipes", 0, () -> {
                    int recipes = pilot.onServer(() -> recipes(pilot.minecraft().getSingleplayerServer()));
                    pilot.check(recipes + " recipes loaded", recipes >= RECIPES_AT_LEAST,
                            "expected at least " + RECIPES_AT_LEAST);
                })
                .then("tooltip rows", 0, () -> pilot.check("a burner has tooltip rows",
                        !Tooltips.of(block("copper_burner")).isEmpty(), "no rows for the copper burner"))
                .then("tab holds the items", 0, () -> pilot.check(CaldariumRegistry.items().size() + " items in the tab",
                        CaldariumRegistry.items().size() > 1, "the tab is empty"))
                .then("lay the floor", SHORT, () -> {
                    pilot.command("fill -2 " + (Y - 1) + " 0 12 " + (Y - 1) + " 8 minecraft:barrier");
                    pilot.command("setblock " + at(BURNER) + " caldarium:copper_burner");
                    pilot.command("setblock " + at(CRUCIBLE) + " caldarium:copper_crucible");
                    pilot.command("setblock " + at(EXPERIENTIA) + " caldarium:copper_experientia");
                })
                .then("fuel the machines", SHORT, () -> pilot.onServer(() -> {
                    Level level = level(pilot.serverPlayer());
                    GeneratorBlockEntity burner = (GeneratorBlockEntity) level.getBlockEntity(BURNER);
                    burner.fuel().setStackInSlot(0, new ItemStack(Items.COAL, COAL));
                    GeneratorBlockEntity crucible = (GeneratorBlockEntity) level.getBlockEntity(CRUCIBLE);
                    crucible.tank().fill(new FluidStack(Fluids.LAVA, LAVA), FuelTank.FluidAction.EXECUTE);
                    return null;
                }))
                .then("let them burn", BURN, () -> pilot.command("tp @s 4.5 " + Y + " 1.5 0 30"));

        screen(pilot, "burner", BURNER);
        screen(pilot, "crucible", CRUCIBLE);
        screen(pilot, "experientia", EXPERIENTIA);

        pilot.then("machines row", SHORT, () -> {
                    minecraft.options.hideGui = true;
                    pilot.command("tp @s 4.5 " + (Y + 1) + " 0.5 0 25");
                })
                .then("machines shot", SHORT, () -> pilot.screenshot("machines"));

        //? if fabric {
        /*pilot.then("burner beside another mod's store", BURN, () -> {
                    pilot.command("setblock " + at(SINK) + " " + Sink.BLOCK);
                    pilot.command("setblock " + at(SINK_BURNER) + " caldarium:copper_burner");
                    pilot.onServer(() -> {
                        ((GeneratorBlockEntity) level(pilot.serverPlayer()).getBlockEntity(SINK_BURNER)).fuel()
                                .setStackInSlot(0, new ItemStack(Items.COAL, COAL));
                        return null;
                    });
                })
                .then("check the other mod's store", 0, () -> pilot.check(
                        "a Team Reborn Energy store beside a burner holds " + Sink.amount(),
                        Sink.amount() > 0, "nothing arrived"));
        *///?}

        pilot.then("place the sun", SETTLE, () -> {
                    minecraft.options.hideGui = true;
                    pilot.command("setblock " + at(SUN) + " caldarium:sol");
                    pilot.command("tp @s 0.5 " + (SUN.getY() + 0.5 - 1.62) + " " + (centreZ - radius - 9.0) + " 0 0");
                })
                .then("sun shot", SHORT, () -> pilot.screenshot("sun"))
                .then("near the sun", SHORT, () -> {
                    minecraft.options.hideGui = false;
                    pilot.command("tp @s 0.5 " + (SUN.getY() + 0.5 - 1.62) + " " + (centreZ - radius - 3.0) + " 0 0");
                })
                .then("outline shot", SHORT, () -> pilot.screenshot("outline"))
                .then("hold a sun", SHORT, () -> {
                    //? if >=1.17 {
                    pilot.command("item replace entity @s weapon.mainhand with caldarium:sol 1");
                    //?} else
                    /*pilot.command("replaceitem entity @s weapon.mainhand caldarium:sol 1");*/
                    pilot.command("tp @s 0.5 " + Y + " " + (centreZ - radius - 3.0) + " 90 10");
                })
                .then("preview shot", SHORT, () -> pilot.screenshot("preview"))
                .then("step far back", SHORT, () -> {
                    pilot.command("clear");
                    pilot.command("tp @s 0.5 " + (SUN.getY() + 0.5 - 1.62) + " " + (centreZ - radius - FAR) + " 0 0");
                })
                .then("break from far", SHORT, () -> minecraft.gameMode.startDestroyBlock(SUN, Direction.NORTH))
                .then("still there", 0, () -> pilot.check("a sun " + FAR + " blocks from its surface is out of reach",
                        pilot.onServer(() -> level(pilot.serverPlayer()).getBlockState(SUN).is(CaldariumRegistry.SOL.get())),
                        "it broke"))
                .then("step close", SHORT, () -> pilot.command(
                        "tp @s 0.5 " + (SUN.getY() + 0.5 - 1.62) + " " + (centreZ - radius - NEAR) + " 0 0"))
                .then("break from close", SHORT, () -> minecraft.gameMode.startDestroyBlock(SUN, Direction.NORTH))
                .then("gone", 0, () -> pilot.check("a sun " + NEAR + " blocks from its surface breaks",
                        pilot.onServer(() -> !level(pilot.serverPlayer()).getBlockState(SUN).is(CaldariumRegistry.SOL.get())),
                        "it is still there"));
        return pilot;
    }

    private static void screen(Pilot pilot, String name, BlockPos pos) {
        pilot.then("open the " + name, SETTLE, () -> pilot.onServer(() -> {
                    Player player = pilot.serverPlayer();
                    MachineMenu.open(level(player), pos, player);
                    return null;
                }))
                .then(name + " screen shot", SHORT, () -> pilot.screenshot("screen-" + name))
                .then("close the " + name, SHORT, () -> pilot.minecraft().setScreen(null));
    }

    private static net.minecraft.world.level.block.Block block(String id) {
        //? if >=1.19.3 {
        return net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(Caldarium.id(id));
        //?} else
        /*return net.minecraft.core.Registry.BLOCK.get(Caldarium.id(id));*/
    }

    private static int recipes(MinecraftServer server) {
        //? if >=1.20.2 {
        return (int) server.getRecipeManager().getRecipes().stream()
                .filter(recipe -> recipe.id().getNamespace().equals(Caldarium.MODID)).count();
        //?} else
        /*return (int) server.getRecipeManager().getRecipes().stream().filter(recipe -> recipe.getId().getNamespace().equals(Caldarium.MODID)).count();*/
    }

    private static String at(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    private static Level level(Player player) {
        //? if >=1.20 {
        return player.level();
        //?} else
        /*return player.level;*/
    }

    //? if fabric && >=1.17 {
    /*public static final class Sink {
        static final String BLOCK = "minecraft:lodestone";
        private static final team.reborn.energy.api.base.SimpleEnergyStorage STORE =
                new team.reborn.energy.api.base.SimpleEnergyStorage(1_000_000, 10_000, 10_000);

        public static void register() {
            team.reborn.energy.api.EnergyStorage.SIDED.registerForBlocks(
                    (level, pos, state, entity, side) -> STORE, net.minecraft.world.level.block.Blocks.LODESTONE);
        }

        static long amount() {
            return STORE.amount;
        }
    }
    *///?} else if fabric {
    /*public static final class Sink implements team.reborn.energy.EnergyStorage {
        static final String BLOCK = "minecraft:jukebox";
        private static final Sink STORE = new Sink();
        private static final double CAPACITY = 1_000_000.0;
        private double stored;

        public static void register() {
            team.reborn.energy.Energy.registerHolder(
                    entity -> entity instanceof net.minecraft.world.level.block.entity.JukeboxBlockEntity, entity -> STORE);
        }

        static long amount() {
            return (long) STORE.stored;
        }

        @Override
        public double getStored(team.reborn.energy.EnergySide side) {
            return stored;
        }

        @Override
        public void setStored(double amount) {
            stored = amount;
        }

        @Override
        public double getMaxStoredPower() {
            return CAPACITY;
        }

        @Override
        public team.reborn.energy.EnergyTier getTier() {
            return team.reborn.energy.EnergyTier.INFINITE;
        }
    }
    *///?}
}
