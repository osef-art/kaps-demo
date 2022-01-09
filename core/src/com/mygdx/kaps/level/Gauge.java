package com.mygdx.kaps.level;

public class Gauge {
    private final int max;
    private int value;

    Gauge(int max) {
        this(0, max);
    }

    Gauge(int value, int max) {
        this.max = max;
        this.value = value;
    }

    static Gauge full(int max) {
        return new Gauge(max);
    }

    @Override
    public String toString() {
        return value + " / " + max;
    }

    void empty() {
        value = 0;
    }

    void increase() {
        value++;
    }

    public double ratio() {
        return (double) value / max;
    }
}
