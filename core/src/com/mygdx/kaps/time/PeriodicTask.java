package com.mygdx.kaps.time;

import java.util.function.Supplier;

public class PeriodicTask extends Timer {
    private final Runnable job;
    private final Runnable finalJob;
    private final Supplier<Boolean> stopCondition;

    private PeriodicTask(double limit, Runnable job, Runnable finalJob, Supplier<Boolean> condition) {
        super(limit);
        stopCondition = condition;
        this.finalJob = finalJob;
        this.job = job;
    }

    private PeriodicTask(double limit, Runnable job, Supplier<Boolean> condition) {
        this(limit, job, () -> {},  condition);
    }

    private PeriodicTask(double timer, Runnable job) {
        this(timer, job, () -> false);
    }

    PeriodicTask(PeriodicTask task, Runnable finalJob) {
        this(task.getLimit(), task.job, finalJob, task.stopCondition);
    }

    public static PeriodicTask everySeconds(double limit, Runnable job, Supplier<Boolean> condition) {
        return new PeriodicTask(limit * 1_000_000_000, job, condition);
    }

    public static PeriodicTask everyMilliseconds(double limit, Runnable job, Supplier<Boolean> condition) {
        return new PeriodicTask(limit * 1_000_000, job, condition);
    }

    public static PeriodicTask everySeconds(double limit, Runnable job) {
        return new PeriodicTask(limit * 1_000_000_000, job);
    }

    public static PeriodicTask everyMilliseconds(double limit, Runnable job) {
        return new PeriodicTask(limit * 1_000_000, job);
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
            job.run();
        }
    }
}
