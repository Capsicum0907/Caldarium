package io.github.capsicum0907.caldarium.client;

final class Point {
    float x;
    float y;
    float z;

    Point() {
    }

    Point(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    Point set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    Point normalize() {
        float scalar = 1.0F / (float) Math.sqrt(x * x + (y * y + z * z));
        x *= scalar;
        y *= scalar;
        z *= scalar;
        return this;
    }
}
