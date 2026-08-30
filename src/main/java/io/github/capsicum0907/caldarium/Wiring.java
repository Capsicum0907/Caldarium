package io.github.capsicum0907.caldarium;

/**
 * Which side of the line a block stands on, and which side it offers to.
 *
 * <p>The line is what {@link Kind#CABLE}, {@link Kind#IMPORTER} and
 * {@link Kind#EXPORTER} make between them. Everything else in the mod — a generator,
 * a battery, a charger, and every block any other mod ever added — is outside it.
 *
 * <p><b>The boundary is a rule about pushing and not about connecting.</b> A cable
 * refuses to <em>offer</em> to anything outside the line, because a cable that gave
 * to whatever it touched would power a machine merely by being laid past it, and
 * then there would be no way to run one anywhere. It has no matching rule about
 * being offered <em>to</em>: a burner or a battery beside a cable fills it, which is
 * what makes the importer a tool for something else's machines rather than a fitting
 * this mod needs against its own.
 *
 * <p>⚠ Three values, not two flags. Being in the line and offering into the line are
 * different questions with exactly three sensible answers between them, and an
 * exporter is the one that shows it: it is in the line and offers out of it.
 */
public enum Wiring {
    /** Outside the line, and offers what it holds to anything that will take it. */
    OPEN,
    /** In the line, and offers only back into it. A cable, and the way in. */
    ALONG,
    /** In the line, and offers only out of it. The one way out. */
    OUT;

    /** Whether this block is part of the line rather than something it serves. */
    public boolean inLine() {
        return this != OPEN;
    }

    /** Whether a neighbour on the given side of the boundary may be offered to. */
    public boolean mayOffer(boolean neighbourInLine) {
        return switch (this) {
            case OPEN -> true;
            case ALONG -> neighbourInLine;
            case OUT -> !neighbourInLine;
        };
    }
}
