package com.mygdx.kaps.time;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RegularTaskManager {
    private final List<RegularTask> tasks;

    private RegularTaskManager(List<RegularTask> tasks) {
        this.tasks = tasks;
    }

    public RegularTaskManager(RegularTask... tasks) {
        this(Arrays.asList(tasks));
    }

    public RegularTaskManager() {
        this(new ArrayList<>());
    }

    public void add(RegularTask task, Runnable finalJob) {
        tasks.add(new RegularTask(task, finalJob));
    }

    public void pauseTasks() {
        tasks.forEach(t -> {
            t.resetIfExceeds();
            t.pause();
        });
    }

    public void resumeTasks() {
        tasks.forEach(RegularTask::resume);
    }

    public void update() {
        tasks.forEach(task -> {
            task.resetIfExceeds();
            if (task.canStop()) task.runFinalJob();
        });
        tasks.removeIf(RegularTask::canStop);
    }
}
