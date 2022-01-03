package com.mygdx.kaps.time;

import com.badlogic.gdx.Gdx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Timer {
    private static class Chrono {
        private float elapsedTime = 0;

        private Chrono() {
            reset();
        }

        private double elapsedTime() {
            return elapsedTime;
        }

        private void reset() {
            elapsedTime = 0;
        }

        private void update() {
            elapsedTime += Gdx.graphics.getDeltaTime();
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
        return new Timer(limit, jobs);
    }

    public static Timer ofMilliseconds(double limit, Runnable... jobs) {
        return new Timer(limit / 1000, jobs);
    }

    private boolean isExceeded() {
        return chrono.elapsedTime() > limit;
    }

    public void updateAndResetIfExceeds() {
        update();
        if (isExceeded()) reset();
    }

    private void update() {
        chrono.update();
    }

    public void reset() {
        chrono.reset();
        jobs.forEach(Runnable::run);
    }

    public double ratio() {
        return chrono.elapsedTime() / limit;
    }
}
