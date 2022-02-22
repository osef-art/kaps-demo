package com.mygdx.kaps.time;

import java.util.Objects;
import java.util.function.Supplier;

public class PeriodicTask extends Timer {
    public static class TaskBuilder {
        private final Runnable mainJob;
        private final double period;
        private final Runnable finalJob = () -> {};
        private Supplier<Boolean> stopCondition = () -> false;

        private TaskBuilder(Runnable job, double period) {
            this.mainJob = job;
            this.period = period;
        }

        static TaskBuilder everySeconds(double secs, Runnable job) {
            return new TaskBuilder(job, secs * 1_000_000_000);
        }

        public static TaskBuilder everyMilliseconds(double millis, Runnable job) {
            return new TaskBuilder(job, millis * 1_000_000);
        }

        public TaskBuilder endWhen(Supplier<Boolean> condition) {
            stopCondition = condition;
            return this;
        }

        public PeriodicTask build() {
            return new PeriodicTask(period, mainJob, finalJob, stopCondition);
        }
    }

    private final Runnable mainJob;
    private final Runnable finalJob;
    private final Supplier<Boolean> stopCondition;

    private PeriodicTask(double period, Runnable job, Runnable finalJob, Supplier<Boolean> condition) {
        super(period);
        stopCondition = Objects.requireNonNull(condition);
        this.finalJob = Objects.requireNonNull(finalJob);
        mainJob = Objects.requireNonNull(job);
    }

    public static PeriodicTask everySeconds(double secs, Runnable job) {
        return TaskBuilder.everySeconds(secs, job).build();
    }

    public static PeriodicTask everyMilliseconds(double millis, Runnable job) {
        return TaskBuilder.everyMilliseconds(millis, job).build();
    }

    boolean canStop() {
        return stopCondition.get();
    }

    void runFinalJob() {
        finalJob.run();
    }

    public void resetIfExceeds() {
        if (isExceeded()) {
            reset();
            mainJob.run();
        }
    }
}
