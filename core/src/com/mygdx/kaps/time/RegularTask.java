package com.mygdx.kaps.time;

import java.util.function.Supplier;

public class RegularTask extends Timer {
    private final Runnable job;
    private final Supplier<Boolean> stopCondition;

    private RegularTask(double limit, Runnable job, Supplier<Boolean> condition) {
        super(limit);
        stopCondition = condition;
        this.job = job;
    }

    private RegularTask(double timer, Runnable job) {
        this(timer, job, () -> false);
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
