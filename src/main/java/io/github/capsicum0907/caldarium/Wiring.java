package io.github.capsicum0907.caldarium;

public enum Wiring {
    OPEN,
    CABLE,
    INTAKE,
    OUTLET;

    public boolean inLine() {
        return this != OPEN;
    }

    public boolean feeds(Wiring other) {
        return this == INTAKE && (other == CABLE || other == OUTLET);
    }

    public boolean drawsFrom(Wiring other) {
        return this == OUTLET && (other == CABLE || other == INTAKE);
    }

    public boolean joins(Wiring other) {
        return this == CABLE && other == CABLE || feeds(other) || other.feeds(this)
                || drawsFrom(other) || other.drawsFrom(this);
    }
}
