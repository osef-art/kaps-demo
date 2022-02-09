package com.mygdx.kaps.level;

import java.util.Locale;

public class Gauge {
    private final int max;
    private int value;

    Gauge(int max) {
        this(0, max);
    }

    public Gauge(int value, int max) {
        this.max = max;
        this.value = value;
    }

    public static Gauge full(int max) {
        return new Gauge(max, max);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "[ %d / %d ]", value, max);
    }

    public int getMax() {
        return max;
    }

    public int getValue() {
        return value;
    }

    boolean isFull() {
        return ratio() >= 1;
    }

    public boolean isEmpty() {
        return value <= 0;
    }

    public double ratio() {
        return (double) value / max;
    }

    void increase() {
        value++;
    }

    private void decrease() {
        value--;
    }

    public void decreaseIfPossible() {
        if (!isEmpty()) decrease();
    }

    void increaseIfPossible() {
        if (!isFull()) increase();
    }

    public void fill() {
        value = max;
    }
}
