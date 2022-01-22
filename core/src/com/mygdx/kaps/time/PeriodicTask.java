package com.mygdx.kaps.time;

import java.util.Objects;
import java.util.function.Supplier;

public class PeriodicTask extends Timer {
    public static class TaskBuilder {
        private final Runnable mainJob;
        private final double period;
        private Runnable finalJob = () -> {};
        private Supplier<Boolean> stopCondition = () -> false;
        private double offset = 0;

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

        public TaskBuilder delayedByMilliseconds(double offset) {
            this.offset = offset * 1_000_000;
            return this;
        }

        public TaskBuilder whenFinishedDo(Runnable job) {
            finalJob = job;
            return this;
        }

        public TaskBuilder endWhen(Supplier<Boolean> condition) {
            stopCondition = condition;
            return this;
        }

        public PeriodicTask build() {
            return new PeriodicTask(offset, period, mainJob, finalJob, stopCondition);
        }
    }

    private final Runnable mainJob;
    private final Runnable finalJob;
    private final Supplier<Boolean> stopCondition;
    private final Timer offset;

    private PeriodicTask(double offset, double period, Runnable job, Runnable finalJob, Supplier<Boolean> condition) {
        super(period);
        this.offset = new Timer(offset);
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

    public void runFinalJob() {
        finalJob.run();
    }

    boolean canStop() {
        return stopCondition.get();
    }

    public void resetIfExceeds() {
        if (isExceeded()) {
            reset();
            mainJob.run();
        }
    }
}
