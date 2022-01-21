package com.mygdx.kaps.time;

import java.util.Arrays;
import java.util.List;

public class RegularTaskManager {
    private final List<RegularTask> tasks;

    public RegularTaskManager(RegularTask... tasks) {
        this.tasks = Arrays.asList(tasks);
    }

    public void update() {
        tasks.removeIf(RegularTask::canStop);
        tasks.forEach(RegularTask::resetIfExceeds);
    }

    public void resumeTasks() {
        tasks.forEach(RegularTask::resume);
    }

    public void pauseTasks() {
        tasks.forEach(t -> {
            t.resetIfExceeds();
            t.pause();
        });
    }
}
