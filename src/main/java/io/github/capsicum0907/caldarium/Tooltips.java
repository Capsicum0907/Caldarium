package io.github.capsicum0907.caldarium;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class Tooltips {
    private static final int DECIMALS = 3;

    private Tooltips() {
    }

    public static int number(Tier tier) {
        return tier.ordinal() + 1;
    }

    public static String tierKey(Tier tier) {
        return "tier." + Caldarium.MODID + "." + tier.id();
    }

    public static void generator(Generator.Made made, List<Component> lines) {
        if (!CaldariumConfig.SPEC.isLoaded()) {
            return;
        }
        tier(made.tier(), made.generator().top(), lines);
        CaldariumConfig.Rates rates = CaldariumConfig.GENERATORS.get(made);
        double each = rates.makes().get() / (double) Math.max(1, rates.every().get());
        switch (made.source()) {
            case ITEM, FLUID, SUN, HEAT, LAMP -> line(lines, "makes.rate", each);
            case EXPERIENCE -> line(lines, "makes.point", each);
            case LIFE -> line(lines, "makes.health", each);
            case BLOW -> line(lines, "makes.damage", each);
            case STORM -> line(lines, "makes.strike", CaldariumConfig.stormNatural());
        }
        line(lines, "holds", rates.capacity().get());
        line(lines, "sends", rates.transfer().get());
    }

    public static void kind(Kind kind, Tier tier, List<Component> lines) {
        if (!CaldariumConfig.SPEC.isLoaded()) {
            return;
        }
        tier(tier, kind.top(), lines);
        CaldariumConfig.Rates rates = CaldariumConfig.rates(kind, tier);
        line(lines, "holds", rates.capacity().get());
        line(lines, "sends", rates.transfer().get());
    }

    private static void tier(Tier tier, Tier top, List<Component> lines) {
        if (tier == null) {
            return;
        }
        lines.add(Component.translatable("tooltip." + Caldarium.MODID + ".tier",
                number(tier), Component.translatable(tierKey(tier)),
                number(top), Component.translatable(tierKey(top))).withStyle(ChatFormatting.GRAY));
    }

    private static void line(List<Component> lines, String key, double value) {
        lines.add(Component.translatable("tooltip." + Caldarium.MODID + "." + key, amount(value))
                .withStyle(ChatFormatting.GRAY));
    }

    private static String amount(double value) {
        BigDecimal rounded = BigDecimal.valueOf(value).setScale(DECIMALS, RoundingMode.HALF_UP).stripTrailingZeros();
        return String.format(Locale.ROOT, "%,." + Math.max(0, rounded.scale()) + "f", rounded);
    }
}
