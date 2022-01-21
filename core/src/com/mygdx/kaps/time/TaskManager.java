package com.mygdx.kaps.time;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TaskManager {
    private final List<PeriodicTask> tasks;

    private TaskManager(List<PeriodicTask> tasks) {
        this.tasks = tasks;
    }

    public TaskManager(PeriodicTask... tasks) {
        this(Arrays.asList(tasks));
    }

    public TaskManager() {
        this(new ArrayList<>());
    }

    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    public void add(PeriodicTask task) {
        tasks.add(task);
    }

    public void add(PeriodicTask task, Runnable finalJob) {
        add(new PeriodicTask(task, finalJob));
    }

    public void pauseTasks() {
        tasks.forEach(t -> {
            t.resetIfExceeds();
            t.pause();
        });
    }

    public void resumeTasks() {
        tasks.forEach(PeriodicTask::resume);
    }

    public void update() {
        tasks.forEach(task -> {
            task.resetIfExceeds();
            if (task.canStop()) task.runFinalJob();
        });
        tasks.removeIf(PeriodicTask::canStop);
    }
}
