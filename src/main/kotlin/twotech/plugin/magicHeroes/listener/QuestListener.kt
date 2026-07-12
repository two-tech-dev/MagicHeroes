package twotech.plugin.magicHeroes.listener

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import twotech.plugin.magicHeroes.party.PartyService
import twotech.plugin.magicHeroes.quest.QuestObjectiveType
import twotech.plugin.magicHeroes.quest.QuestService

class QuestListener(
    private val quests: QuestService,
    private val parties: PartyService
) : Listener {
    @EventHandler(ignoreCancelled = true)
    fun onEntityDeath(event: EntityDeathEvent) {
        val killer = event.entity.killer ?: return
        progressParty(killer.uniqueId, QuestObjectiveType.KILL, event.entity.type.name)
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        progressParty(event.player.uniqueId, QuestObjectiveType.MINE, event.block.type.name)
    }

    @EventHandler(ignoreCancelled = true)
    fun onItemPickup(event: EntityPickupItemEvent) {
        val player = event.entity as? org.bukkit.entity.Player ?: return
        progressParty(player.uniqueId, QuestObjectiveType.COLLECT, event.item.itemStack.type.name, event.item.itemStack.amount)
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockInteract(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        val block = event.clickedBlock ?: return
        progressParty(event.player.uniqueId, QuestObjectiveType.INTERACT, block.type.name)
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityInteract(event: PlayerInteractEntityEvent) {
        progressParty(event.player.uniqueId, QuestObjectiveType.INTERACT, event.rightClicked.type.name)
    }

    @EventHandler(ignoreCancelled = true)
    fun onMove(event: PlayerMoveEvent) {
        if (event.from.blockX == event.to.blockX && event.from.blockY == event.to.blockY && event.from.blockZ == event.to.blockZ) return
        parties.memberIds(event.player.uniqueId)
            .mapNotNull(Bukkit::getPlayer)
            .forEach { player ->
                quests.progressReach(player, player.location)
                    .forEach { result -> player.sendMessage(Component.text(result.message)) }
            }
    }

    private fun progressParty(playerId: java.util.UUID, type: QuestObjectiveType, target: String, amount: Int = 1) {
        parties.memberIds(playerId)
            .mapNotNull(Bukkit::getPlayer)
            .forEach { player ->
                quests.progress(player, type, target, amount)
                    .forEach { result -> player.sendMessage(Component.text(result.message)) }
            }
    }
}
