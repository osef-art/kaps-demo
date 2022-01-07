package com.mygdx.kaps.time;

import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Timer {
    private static class Chrono {
        private long startStamp;

        private Chrono() {
            reset();
        }

        private double elapsedTime() {
            return TimeUtils.nanoTime() - startStamp;
        }

        private void reset() {
            startStamp = TimeUtils.nanoTime();
        }
    }

    private final List<Runnable> jobs = new ArrayList<>();
    private final Chrono chrono;
    private final double limit;

    private Timer(double limit, Runnable... jobs) {
        chrono = new Chrono();
        this.limit = limit;
        this.jobs.addAll(Arrays.asList(jobs));
    }

    public static Timer ofSeconds(double limit, Runnable... jobs) {
        return new Timer(limit * 1_000_000_000, jobs);
    }

    public static Timer ofMilliseconds(double limit, Runnable... jobs) {
        return new Timer(limit * 1_000_000, jobs);
    }

    private boolean isExceeded() {
        return chrono.elapsedTime() > limit;
    }

    public void resetIfExceeds() {
        if (isExceeded()) reset();
    }

    public void reset() {
        chrono.reset();
        jobs.forEach(Runnable::run);
    }

    public double ratio() {
        return chrono.elapsedTime() / limit;
    }
}
