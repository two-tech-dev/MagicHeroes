package twotech.plugin.magicHeroes.util

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Utility class to check ItemStack validity
 */
object ItemEditor {
    
    /**
     * Checks if the player is holding a valid item in their main hand
     * 
     * @param player The player to check
     * @return true if the player holds a non-air item, false otherwise
     */
    fun hasValidItemInHand(player: Player): Boolean {
        val item = player.inventory.itemInMainHand
        return item.type != Material.AIR && item.amount > 0
    }
    
    /**
     * Retrieves the item stack in player's main hand
     * 
     * @param player The player
     * @return The item stack or null if not holding a valid item
     */
    fun getItemInHand(player: Player): ItemStack? {
        val item = player.inventory.itemInMainHand
        return if (item.type != Material.AIR) item else null
    }
}
