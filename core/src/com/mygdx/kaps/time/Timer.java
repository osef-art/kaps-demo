package com.mygdx.kaps.time;

public class Timer {
    private final Chrono chrono;
    private double limit;

    public Timer(double limit) {
        chrono = new Chrono();
        this.limit = limit;
    }

    public static Timer ofSeconds(double limit) {
        return new Timer(limit * 1_000_000_000);
    }

    public boolean isExceeded() {
        return chrono.value() > limit;
    }

    public boolean resetIfExceeds() {
        if (isExceeded()) {
            chrono.reset();
            return true;
        }
        return false;
    }

    public void reset() {
        chrono.reset();
    }

    public void updateLimit(int newLimit) {
        limit = newLimit;
    }

    public double ratio() {
        return chrono.value() / limit;
    }
}
