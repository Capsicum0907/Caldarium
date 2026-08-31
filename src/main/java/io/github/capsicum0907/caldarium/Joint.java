package io.github.capsicum0907.caldarium;

import java.util.Locale;

import net.minecraft.util.StringRepresentable;

/**
 * What one face of a thing that carries wears.
 *
 * <p>⭐ A door reaches out of the line on exactly one face, and which one is decided
 * when it is placed and never again. That face wears the drill; every other face of it,
 * and every face of a cable, is either a plain joint or nothing at all.
 *
 * <p>⚠ The aim is kept here rather than in a facing of its own, because it is the same
 * fact: the face that is aimed is the face that is not joined along the line. Two
 * properties saying it would be two properties free to disagree.
 */
public enum Joint implements StringRepresentable {
    /** Nothing energy could cross to. */
    NONE,
    /** Joined, and wearing the plain arm every such joint wears. */
    ALONG,
    /**
     * The one face a door was aimed at when it was placed. It wears the drill, and it
     * is the only face that block reaches outside the line on — whether or not there
     * is anything standing there yet, because where it would work is what you aim it by.
     */
    AIMED;

    private final String id = name().toLowerCase(Locale.ROOT);

    @Override
    public String getSerializedName() {
        return id;
    }
}
