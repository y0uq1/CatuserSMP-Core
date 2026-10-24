package com.catusersmp.core.util;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Thin, discoverable wrapper around the Bukkit scheduler. Keeping scheduling in one place makes it
 * easy to migrate to Folia's region schedulers later on.
 */
public final class Tasks {

    private final Plugin plugin;

    public Tasks(Plugin plugin) {
        this.plugin = plugin;
    }

    public BukkitTask run(Runnable task) {
        return this.plugin.getServer().getScheduler().runTask(this.plugin, task);
    }

    public BukkitTask runLater(long delayTicks, Runnable task) {
        return this.plugin.getServer().getScheduler().runTaskLater(this.plugin, task, delayTicks);
    }

    public BukkitTask runTimer(long delayTicks, long periodTicks, Runnable task) {
        return this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, task, delayTicks, periodTicks);
    }

    public BukkitTask runAsync(Runnable task) {
        return this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, task);
    }

    public BukkitTask runAsyncLater(long delayTicks, Runnable task) {
        return this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin, task, delayTicks);
    }

    public BukkitTask runAsyncTimer(long delayTicks, long periodTicks, Runnable task) {
        return this.plugin.getServer().getScheduler()
                .runTaskTimerAsynchronously(this.plugin, task, delayTicks, periodTicks);
    }

    public void cancel(BukkitTask task) {
        if (task != null) {
            task.cancel();
        }
    }

    /** Cancels every task that belongs to this plugin. */
    public void cancelAll() {
        this.plugin.getServer().getScheduler().cancelTasks(this.plugin);
    }
}
