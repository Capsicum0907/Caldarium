package io.github.capsicum0907.caldarium;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.block.Block;

public final class Tooltips {
    private static final int DECIMALS = 3;

    private Tooltips() {
    }

    public record Row(String label, String amount, String unit) {
    }

    public record Readings(List<Row> rows) implements TooltipComponent {
    }

    public static int number(Tier tier) {
        return tier.ordinal() + 1;
    }

    public static List<Row> of(Block block) {
        if (!CaldariumConfig.SPEC.isLoaded()) {
            return List.of();
        }
        if (block instanceof GeneratorBlock generator) {
            return generator(generator.row());
        }
        if (block instanceof KindBlock kind) {
            return kind(kind.kind(), kind.tier());
        }
        return List.of();
    }

    public static List<Row> generator(Generator.Made made) {
        CaldariumConfig.Rates rates = CaldariumConfig.GENERATORS.get(made);
        double each = rates.makes().get() / (double) Math.max(1, rates.every().get());
        Row makes = switch (made.source()) {
            case ITEM, FLUID, SUN, HEAT, LAMP -> row("makes", each, "rate");
            case EXPERIENCE -> row("makes", each, "point");
            case LIFE -> row("makes", each, "health");
            case BLOW -> row("makes", each, "damage");
            case STORM -> row("makes", CaldariumConfig.stormNatural(), "strike");
        };
        return List.of(makes,
                row("holds", rates.capacity().get(), "stored"),
                row("sends", rates.transfer().get(), "rate"));
    }

    public static List<Row> kind(Kind kind, Tier tier) {
        CaldariumConfig.Rates rates = CaldariumConfig.rates(kind, tier);
        return List.of(
                row("holds", rates.capacity().get(), "stored"),
                row("sends", rates.transfer().get(), "rate"));
    }

    public static String key(String rest) {
        return "tooltip." + Caldarium.MODID + "." + rest;
    }

    private static Row row(String label, double value, String unit) {
        return new Row(key("label." + label), amount(value), key("unit." + unit));
    }

    private static String amount(double value) {
        BigDecimal rounded = BigDecimal.valueOf(value).setScale(DECIMALS, RoundingMode.HALF_UP).stripTrailingZeros();
        return String.format(Locale.ROOT, "%,." + Math.max(0, rounded.scale()) + "f", rounded);
    }
}
