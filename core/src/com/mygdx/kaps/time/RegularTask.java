package com.mygdx.kaps.time;

import java.util.function.Supplier;

public class RegularTask extends Timer {
    private final Runnable job;
    private final Runnable finalJob;
    private final Supplier<Boolean> stopCondition;

    private RegularTask(double limit, Runnable job, Runnable finalJob, Supplier<Boolean> condition) {
        super(limit);
        stopCondition = condition;
        this.finalJob = finalJob;
        this.job = job;
    }

    private RegularTask(double limit, Runnable job, Supplier<Boolean> condition) {
        this(limit, job, () -> {},  condition);
    }

    private RegularTask(double timer, Runnable job) {
        this(timer, job, () -> false);
    }

    RegularTask(RegularTask task, Runnable finalJob) {
        this(task.getLimit(), task.job, finalJob, task.stopCondition);
    }

    public static RegularTask everySeconds(double limit, Runnable job, Supplier<Boolean> condition) {
        return new RegularTask(limit * 1_000_000_000, job, condition);
    }

    public static RegularTask everyMilliseconds(double limit, Runnable job, Supplier<Boolean> condition) {
        return new RegularTask(limit * 1_000_000, job, condition);
    }

    public static RegularTask everySeconds(double limit, Runnable job) {
        return new RegularTask(limit * 1_000_000_000, job);
    }

    public static RegularTask everyMilliseconds(double limit, Runnable job) {
        return new RegularTask(limit * 1_000_000, job);
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
