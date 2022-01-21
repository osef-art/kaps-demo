package com.mygdx.kaps.time;

import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Timer {
    private static class Chrono {
        private long startStamp;
        private long endStamp;

        private Chrono() {
            reset();
        }

        private long endStamp() {
            return endStamp == 0 ? TimeUtils.nanoTime() : endStamp;
        }

        private long elapsedTime() {
            return endStamp() - startStamp;
        }

        private void reset() {
            startStamp = TimeUtils.nanoTime();
            endStamp = 0;
        }

        private void stop() {
            endStamp = endStamp();
        }
    }

    private final List<Chrono> offsets = new ArrayList<>();
    private final List<Runnable> jobs;
    private final Chrono chrono = new Chrono();
    private double limit;

    private Timer(double limit, Runnable... jobs) {
        if (limit < 0) throw new IllegalArgumentException("Invalid timer limit: " + limit);
        this.limit = limit;
        this.jobs = Arrays.asList(jobs);
    }

    public static Timer ofSeconds(double limit, Runnable... jobs) {
        return new Timer(limit * 1_000_000_000, jobs);
    }

    public static Timer ofMilliseconds(double limit, Runnable... jobs) {
        return new Timer(limit * 1_000_000, jobs);
    }

    public double getLimit() {
        return limit;
    }

    boolean isExceeded() {
        return ratio() >= 1;
    }

    private double totalOffset() {
        return offsets.stream().mapToLong(Chrono::elapsedTime).sum();
    }

    public double ratio() {
        return (chrono.elapsedTime() - totalOffset()) / limit;
    }

    public void resetIfExceeds() {
        if (isExceeded()) {
            reset();
            jobs.forEach(Runnable::run);
        }
    }

    public void reset() {
        chrono.reset();
        offsets.clear();
    }

    public void pause() {
        offsets.add(new Chrono());
    }

    public void resume() {
        if (offsets.isEmpty()) return;
        offsets.get(offsets.size() - 1).stop();
    }

    public void updateLimit(double newLimit) {
        limit = newLimit;
    }
}
