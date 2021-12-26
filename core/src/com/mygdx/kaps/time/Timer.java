package com.mygdx.kaps.time;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Timer {
    private final List<Runnable> jobs = new ArrayList<>();
    private final Chrono chrono;
    private double limit;

    public Timer(double limit, Runnable... jobs) {
        chrono = new Chrono();
        this.limit = limit;
        this.jobs.addAll(Arrays.asList(jobs));
    }

    public static Timer ofSeconds(double limit, Runnable... jobs) {
        return new Timer(limit * 1_000_000_000, jobs);
    }

    // TODO: use delta time
    public static Timer ofMilliseconds(double limit, Runnable... jobs) {
        return new Timer(limit * 1_000_000, jobs);
    }

    public boolean isExceeded() {
        return chrono.value() > limit;
    }

    public boolean resetIfExceeds() {
        if (isExceeded()) {
            reset();
            return true;
        }
        return false;
    }

    public void reset() {
        chrono.reset();
        jobs.forEach(Runnable::run);
    }

    public void updateLimit(int newLimit) {
        limit = newLimit;
    }

    public double ratio() {
        return chrono.value() / limit;
    }
}
