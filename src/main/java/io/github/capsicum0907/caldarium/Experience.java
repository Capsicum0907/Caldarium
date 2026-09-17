package io.github.capsicum0907.caldarium;

import net.minecraft.world.entity.player.Player;

public final class Experience {
    private Experience() {
    }

    public static int points(Player player) {
        return total(player.experienceLevel)
                + Math.round(player.experienceProgress * player.getXpNeededForNextLevel());
    }

    public static int total(int level) {
        if (level <= 16) {
            return level * level + 6 * level;
        }
        if (level <= 31) {
            return (int) (2.5 * level * level - 40.5 * level + 360.0);
        }
        return (int) (4.5 * level * level - 162.5 * level + 2220.0);
    }

    public static int span(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        }
        if (level >= 15) {
            return 37 + (level - 15) * 5;
        }
        return 7 + level * 2;
    }

    public static int take(Player player, int wanted) {
        int have = points(player);
        int taken = Math.min(wanted, have);
        if (taken <= 0) {
            return 0;
        }
        int left = have - taken;
        int level = 0;
        while (total(level + 1) <= left) {
            level++;
        }
        player.experienceLevel = level;
        player.experienceProgress = (left - total(level)) / (float) span(level);
        player.totalExperience = Math.max(0, player.totalExperience - taken);
        return taken;
    }
}
