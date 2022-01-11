package com.mygdx.kaps.level;

class Gauge {
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
        return new Gauge(max, max);
    }

    @Override
    public String toString() {
        return value + " / " + max;
    }

    public int getMax() {
        return max;
    }

    public int getValue() {
        return value;
    }

    boolean isFull() {
        return value >= max;
    }

    boolean isEmpty() {
        return value <= 0;
    }

    double ratio() {
        return (double) value / max;
    }

    void increase() {
        value++;
    }

    private void decrease() {
        value--;
    }

    void decreaseIfPossible() {
        if (!isEmpty()) decrease();
    }

    void increaseIfPossible() {
        if (!isFull()) increase();
    }

    void empty() {
        value = 0;
    }

    void fill() {
        value = max;
    }
}
