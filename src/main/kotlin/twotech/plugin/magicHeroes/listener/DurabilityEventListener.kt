package twotech.plugin.magicHeroes.listener

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import twotech.plugin.magicHeroes.manager.DurabilityManager
import java.util.concurrent.ThreadLocalRandom

/**
 * Listens for game events to deduct custom durability from items
 */
class DurabilityEventListener(private val plugin: JavaPlugin) : Listener {

    private val durabilityManager = DurabilityManager.getInstance(plugin)

    /**
     * Prevents vanilla item durability damage
     */
    @EventHandler(priority = EventPriority.LOWEST)
    fun onVanillaItemDamage(event: PlayerItemDamageEvent) {
        val item = event.item
        if (durabilityManager.hasDurability(item)) {
            // Cancel vanilla damage so only the custom durability system handles it
            event.isCancelled = true
        }
    }

    /**
     * Deduct durability for weapons on attack, shields on block, and armor on taking damage
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        // Broken custom weapons cannot attack.
        val attacker = event.damager as? Player
        if (attacker != null) {
            val weapon = attacker.inventory.itemInMainHand
            if (durabilityManager.isBroken(weapon)) {
                event.isCancelled = true
                return
            }
            if (durabilityManager.hasDurability(weapon)) {
                durabilityManager.reduceDurability(attacker, weapon, 1)
            }
        }

        // Handle victim: deduct armor or shield durability
        val victim = event.entity as? Player ?: return
        
        // Check if victim blocked with a shield
        if (victim.isBlocking) {
            val mainHand = victim.inventory.itemInMainHand
            val offHand = victim.inventory.itemInOffHand
            
            val shield = when {
                mainHand.type == Material.SHIELD && durabilityManager.hasDurability(mainHand) -> mainHand
                offHand.type == Material.SHIELD && durabilityManager.hasDurability(offHand) -> offHand
                else -> null
            }
            
            if (shield != null) {
                // Deduct durability for successfully blocking attacks
                durabilityManager.reduceDurability(victim, shield, 1)
                return // Shield absorbed the hit, skip armor checks
            }
        }

        // Randomly select one armor piece to deduct durability instead of damaging all pieces at once
        val armors = listOfNotNull(
            victim.inventory.helmet,
            victim.inventory.chestplate,
            victim.inventory.leggings,
            victim.inventory.boots
        ).filter { durabilityManager.hasDurability(it) && !durabilityManager.isInfinite(it) }

        if (armors.isNotEmpty()) {
            val randomIndex = ThreadLocalRandom.current().nextInt(armors.size)
            val selectedArmor = armors[randomIndex]
            durabilityManager.reduceDurability(victim, selectedArmor, 1)
        }

        // Randomly select accessories / offhand items to simulate battle wear-and-tear
        // We look at offhand and inventory items matching common accessories (e.g. Totem, etc. if configured as accessories)
        val offhand = victim.inventory.itemInOffHand
        if (offhand.type != Material.AIR && durabilityManager.hasDurability(offhand) && !durabilityManager.isInfinite(offhand)) {
            // 30% chance to deduct accessory durability on taking hit to avoid excessive wear
            if (ThreadLocalRandom.current().nextDouble() < 0.3) {
                durabilityManager.reduceDurability(victim, offhand, 1)
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val item = event.item ?: return
        if (durabilityManager.isBroken(item)) {
            event.isCancelled = true
        }
    }

    /**
     * Deduct durability for tools when breaking blocks
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        val tool = player.inventory.itemInMainHand
        if (durabilityManager.isBroken(tool)) {
            event.isCancelled = true
            return
        }
        if (durabilityManager.hasDurability(tool)) {
            durabilityManager.reduceDurability(player, tool, 1)
        }
    }
}
