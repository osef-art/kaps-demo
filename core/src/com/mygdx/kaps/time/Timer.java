package com.mygdx.kaps.time;

import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Timer {
    private static class Chrono {
        private long startStamp;
        private long stopStamp;

        private Chrono() {
            reset();
        }

        private long endStamp() {
            return stopStamp == 0 ? TimeUtils.nanoTime() : stopStamp;
        }

        private long elapsedTime() {
            return endStamp() - startStamp;
        }

        private void reset() {
            startStamp = TimeUtils.nanoTime();
            stopStamp = 0;
        }

        private void stop() {
            stopStamp = endStamp();
        }
    }

    private final List<Chrono> offsets = new ArrayList<>();
    private final List<Runnable> jobs;
    private final Chrono chrono = new Chrono();
    private final double limit;

    private Timer(double limit, Runnable... jobs) {
        this.limit = limit;
        this.jobs = Arrays.asList(jobs);
    }

    public static Timer ofSeconds(double limit, Runnable... jobs) {
        return new Timer(limit * 1_000_000_000, jobs);
    }

    public static Timer ofMilliseconds(double limit, Runnable... jobs) {
        return new Timer(limit * 1_000_000, jobs);
    }

    private boolean isExceeded() {
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
}
