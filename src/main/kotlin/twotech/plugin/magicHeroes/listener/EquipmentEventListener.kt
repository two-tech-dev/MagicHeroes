package twotech.plugin.magicHeroes.listener

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.plugin.java.JavaPlugin
import twotech.plugin.magicHeroes.calculator.StatCalculator
import twotech.plugin.magicHeroes.data.ResourceService
import twotech.plugin.magicHeroes.item.RequirementService
import twotech.plugin.magicHeroes.item.advance.ItemAdvanceService
import twotech.plugin.magicHeroes.manager.HeroPlayerManager

class EquipmentEventListener(
    private val plugin: JavaPlugin,
    private val resources: ResourceService,
    private val requirements: RequirementService,
    private val advance: ItemAdvanceService
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
        if (!result.allowed || !advance.canUse(attacker, item)) {
            event.isCancelled = true
            attacker.sendMessage(Component.text(result.reason ?: "You cannot use this item."))
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val item = event.item ?: return
        val result = requirements.check(event.player, item)
        if (!result.allowed || !advance.canUse(event.player, item)) {
            event.isCancelled = true
            event.player.sendMessage(Component.text(result.reason ?: "You cannot use this item."))
        }
    }

    private fun updatePlayerStats(player: Player) {
        val data = playerManager.getPlayerData(player.uniqueId) ?: return
        StatCalculator.updateEquipmentStats(player, data)
        resources.applyMaxHealth(player, data.getTotalMaxHealth())
    }
}
