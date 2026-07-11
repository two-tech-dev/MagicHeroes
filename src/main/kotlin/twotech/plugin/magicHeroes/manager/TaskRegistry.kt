package twotech.plugin.magicHeroes.manager

import org.bukkit.scheduler.BukkitTask

class TaskRegistry {
    private val tasks = mutableSetOf<BukkitTask>()

    fun track(task: BukkitTask): BukkitTask = task.also { tasks += it }

    fun cancelAll() {
        tasks.forEach(BukkitTask::cancel)
        tasks.clear()
    }
}
