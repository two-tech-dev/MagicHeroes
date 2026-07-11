package twotech.plugin.magicHeroes.listener

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.plugin.java.JavaPlugin
import twotech.plugin.magicHeroes.calculator.StatCalculator
import twotech.plugin.magicHeroes.data.ResourceService
import twotech.plugin.magicHeroes.manager.HeroPlayerManager
import twotech.plugin.magicHeroes.skill.SkillService

class PlayerEventListener(
    private val plugin: JavaPlugin,
    private val resourceService: ResourceService,
    private val skills: SkillService
) : Listener {
    private val playerManager = HeroPlayerManager.getInstance(plugin)

    @EventHandler
    fun onPreLogin(event: AsyncPlayerPreLoginEvent) {
        playerManager.preload(event.uniqueId, event.name)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val data = playerManager.loadPlayerData(player, resourceService)
        StatCalculator.updateEquipmentStats(player, data)
        resourceService.applyMaxHealth(player, data.getTotalMaxHealth())
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        skills.clear(event.player.uniqueId)
        playerManager.savePlayerData(event.player)
        playerManager.removePlayerData(event.player.uniqueId)
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val victim = event.entity
        val victimData = playerManager.getPlayerData(victim.uniqueId) ?: return
        victimData.deathCount += 1
        victim.killer?.let { killer ->
            val killerData = playerManager.getPlayerData(killer.uniqueId) ?: return@let
            killerData.killCount += 1
        }
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        Bukkit.getScheduler().runTask(plugin, Runnable {
            val player = event.player
            val data = playerManager.getPlayerData(player.uniqueId) ?: return@Runnable
            resourceService.restoreHealth(player)
            resourceService.restoreMana(data)
        })
    }
}
