package io.github.hawah.shakenstir.lib.util;

public class DelayedTask {
    private int ticks;
    private final Runnable task;

    public DelayedTask(int ticks, Runnable task) {
        this.ticks = ticks;
        this.task = task;
    }

    public boolean tick() {
        ticks--;

        if (ticks <= 0) {
            task.run();
            return true;
        }

        return false;
    }
}