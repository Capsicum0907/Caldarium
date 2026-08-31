package io.github.capsicum0907.caldarium;

import java.util.Locale;

import net.minecraft.util.StringRepresentable;

/**
 * What one face of a thing that carries is joined to.
 *
 * <p>⭐ Three answers rather than two, because a door does two different jobs and the
 * shape should say which one a face is doing. A face on to the line wears a plain arm;
 * a face on to anything else is where the block reaches out of the line, and that is
 * the face worth putting a drill on.
 *
 * <p>A cable has no use for the difference — it wears an arm either way — but it
 * carries the property all the same, so that one class and one rule cover everything
 * laid in a line. ⚠ Six faces of three answers is seven hundred and twenty-nine states
 * per block, which is a lot to look at and nothing at all to a game that gives redstone
 * dust twice as many.
 */
public enum Joint implements StringRepresentable {
    /** Nothing energy could cross to. */
    NONE,
    /** Another part of the line. */
    LINE,
    /**
     * Anything that is not the line, including nothing at all. ⭐ A door wears this on
     * every face it has not given to the line, whether or not there is yet a machine
     * on the other side: where it would work is what you aim it by.
     */
    OUTSIDE;

    private final String id = name().toLowerCase(Locale.ROOT);

    @Override
    public String getSerializedName() {
        return id;
    }
}
