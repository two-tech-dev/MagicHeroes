package twotech.plugin.magicHeroes.util

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import twotech.plugin.magicHeroes.manager.TooltipManager

/**
 * ItemUtils - Central utility for item operations
 * 
 * IMPORTANT CONVENTION:
 * =====================
 * EVERY function that modifies an item (rename, lore, durability, enchant, etc.)
 * MUST call `ItemUtils.resetAndUpdateTooltip()` after applying changes.
 * 
 * This ensures tooltip stays synchronized with item data stored in PDC.
 * 
 * Example:
 * ```kotlin
 * // After modifying item
 * meta.persistentDataContainer.set(key, type, value)
 * item.itemMeta = meta
 * 
 * // MUST call this to sync tooltip
 * ItemUtils.resetAndUpdateTooltip(player, item)
 * ```
 */
object ItemUtils {

    /**
     * Resets and updates the tooltip for an item based on its current PDC data.
     * 
     * This function MUST be called after ANY item modification operation:
     * - Rename (sets mh_name in PDC)
     * - Set Lore (sets mh_custom_lore in PDC)
     * - Set Durability (sets mh_durability, mh_max_durability in PDC)
     * - Reset Durability
     * - Set Infinite Durability
     * - Add/Remove Enchantment
     * - Set Tooltip Template (sets mh_tooltip_template in PDC)
     * 
     * The function reads the cached tooltip template from PDC and re-renders
     * the tooltip with all current stats.
     * 
     * @param player The player who owns/modified the item (for i18n support)
     * @param item The ItemStack to update tooltip for
     */
    fun resetAndUpdateTooltip(player: Player?, item: ItemStack) {
        TooltipManager.get()?.applyTooltip(player, item, null)
    }

    /**
     * Updates tooltip with a specific template.
     * 
     * Use this when changing the tooltip style/template for an item.
     * The template name will be cached in PDC for future automatic updates.
     * 
     * @param player The player who owns the item
     * @param item The ItemStack to update
     * @param templateName The tooltip template to apply
     */
    fun setTooltipTemplate(player: Player?, item: ItemStack, templateName: String) {
        TooltipManager.get()?.applyTooltipByName(player, item, templateName)
    }
}
