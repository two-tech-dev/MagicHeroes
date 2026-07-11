package twotech.plugin.magicHeroes.listener

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.plugin.java.JavaPlugin
import twotech.plugin.magicHeroes.calculator.StatCalculator
import twotech.plugin.magicHeroes.data.ResourceService
import twotech.plugin.magicHeroes.item.RequirementService
import twotech.plugin.magicHeroes.manager.HeroPlayerManager

class EquipmentEventListener(
    private val plugin: JavaPlugin,
    private val resources: ResourceService,
    private val requirements: RequirementService
) : Listener {
    private val playerManager = HeroPlayerManager.getInstance(plugin)

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        Bukkit.getScheduler().runTask(plugin, Runnable { updatePlayerStats(player) })
    }

    @EventHandler
    fun onPlayerItemHeld(event: PlayerItemHeldEvent) {
        Bukkit.getScheduler().runTask(plugin, Runnable { updatePlayerStats(event.player) })
    }

    @EventHandler
    fun onPlayerSwapHandItems(event: PlayerSwapHandItemsEvent) {
        Bukkit.getScheduler().runTask(plugin, Runnable { updatePlayerStats(event.player) })
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerDamage(event: EntityDamageByEntityEvent) {
        val attacker = event.damager as? Player ?: return
        val item = attacker.inventory.itemInMainHand
        val result = requirements.check(attacker, item)
        if (!result.allowed) {
            event.isCancelled = true
            attacker.sendMessage(net.kyori.adventure.text.Component.text(result.reason ?: "Item requirement not met."))
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val item = event.item ?: return
        val result = requirements.check(event.player, item)
        if (!result.allowed) {
            event.isCancelled = true
            event.player.sendMessage(net.kyori.adventure.text.Component.text(result.reason ?: "Item requirement not met."))
        }
    }

    private fun updatePlayerStats(player: Player) {
        val data = playerManager.getPlayerData(player.uniqueId) ?: return
        StatCalculator.updateEquipmentStats(player, data)
        resources.applyMaxHealth(player, data.getTotalMaxHealth())
    }
}
