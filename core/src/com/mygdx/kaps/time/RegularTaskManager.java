package com.mygdx.kaps.time;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RegularTaskManager {
    private static class RegularTask {
        private final Timer timer;
        private final Runnable job;
        private final Supplier<Boolean> stopCondition;

        private RegularTask(Timer timer, Runnable job, Supplier<Boolean> condition) {
            stopCondition = condition;
            this.timer = timer;
            this.job = job;
        }

        private RegularTask(Timer timer, Runnable job) {
            this(timer, job, () -> false);
        }

        public static RegularTask everySeconds(double limit, Runnable job, Supplier<Boolean> condition) {
            return new RegularTask(Timer.ofSeconds(limit), job, condition);
        }

        public static RegularTask everyMilliseconds(double limit, Runnable job, Supplier<Boolean> condition) {
            return new RegularTask(Timer.ofMilliseconds(limit), job, condition);
        }

        public static RegularTask everySeconds(double limit, Runnable job) {
            return new RegularTask(Timer.ofSeconds(limit), job);
        }

        public static RegularTask everyMilliseconds(double limit, Runnable job) {
            return new RegularTask(Timer.ofMilliseconds(limit), job);
        }

        public void resetIfExceeds() {
            if (timer.isExceeded()) {
                timer.reset();
                job.run();
            }
        }
    }

    private final List<RegularTask> tasks = new ArrayList<>();

    private void update() {
        tasks.removeIf(t -> t.stopCondition.get());
        tasks.forEach(RegularTask::resetIfExceeds);
    }
}
