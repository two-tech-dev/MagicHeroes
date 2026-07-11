package twotech.plugin.magicHeroes.task

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import twotech.plugin.magicHeroes.data.ResourceService
import twotech.plugin.magicHeroes.manager.HeroPlayerManager
import twotech.plugin.magicHeroes.manager.LanguageManager
import twotech.plugin.magicHeroes.util.ColorTranslator

class ActionbarTask(
    private val plugin: JavaPlugin,
    private val resourceService: ResourceService
) {
    private val playerManager = HeroPlayerManager.getInstance(plugin)
    private var task: BukkitTask? = null
    private var lastNanos = System.nanoTime()

    fun start() {
        stop()
        val interval = plugin.config.getLong("actionbar.update-interval", 20L).coerceAtLeast(1L)
        lastNanos = System.nanoTime()
        task = Bukkit.getScheduler().runTaskTimer(plugin, Runnable { tick() }, 0L, interval)
    }

    fun stop() {
        task?.cancel()
        task = null
    }

    fun restart() = start()

    private fun tick() {
        val now = System.nanoTime()
        val elapsedSeconds = ((now - lastNanos).coerceAtLeast(0L) / 1_000_000_000.0).coerceIn(0.0, 10.0)
        lastNanos = now
        val showActionBar = plugin.config.getBoolean("actionbar.enable", true)
        for (player in Bukkit.getOnlinePlayers()) {
            val data = playerManager.getPlayerData(player.uniqueId) ?: continue
            val maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)?.value ?: continue
            val healthRegen = data.getTotalHealthRegen() * elapsedSeconds
            if (!player.isDead && healthRegen > 0.0) {
                player.health = (player.health + healthRegen).coerceIn(0.0, maxHealth)
            }
            resourceService.regenerate(data, elapsedSeconds)
            if (!showActionBar) continue
            val format = plugin.config.getString(
                "actionbar.format",
                "HP: {current_hp}/{max_hp} | Mana: {current_mana}/{max_mana} | Defense: {defense} | Kills: {kills}"
            ) ?: continue
            val formatted = format
                .replace("{current_hp}", "%.0f".format(player.health))
                .replace("{max_hp}", "%.0f".format(maxHealth))
                .replace("{current_mana}", "%.0f".format(data.currentMana))
                .replace("{max_mana}", "%.0f".format(data.getTotalMaxMana()))
                .replace("{defense}", "%.0f".format(data.getTotalDefense()))
                .replace("{kills}", data.killCount.toString())
            player.sendActionBar(ColorTranslator.translate(formatted))
        }
    }
}
