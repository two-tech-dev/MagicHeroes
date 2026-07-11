package twotech.plugin.magicHeroes.handler

import org.bukkit.entity.Player
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import twotech.plugin.magicHeroes.manager.LanguageManager
import twotech.plugin.magicHeroes.util.ItemEditor
import twotech.plugin.magicHeroes.util.ItemUtils

/**
 * Handler for Sub-Commands: /mh addenchant and /mh removeenchant
 */
object EnchantmentHandler {

    /**
     * Executes adding an enchantment to the item in player's main hand (allows unsafe levels)
     * 
     * @param player Player executing the command
     * @param args Arguments after /mh addenchant
     */
    fun executeAdd(player: Player, args: Array<out String>) {
        val langManager = LanguageManager.get()
        
        // Check argument count: requires <enchantment> <level>
        if (args.size < 3) {
            player.sendMessage(langManager?.getComponent(player, "enchant.add-usage") ?: return)
            return
        }

        val enchantmentName = args[1]
        val levelStr = args[2]

        // Check if player is holding an item
        val item = ItemEditor.getItemInHand(player)
        if (item == null) {
            player.sendMessage(langManager?.getComponent(player, "general.no-item") ?: return)
            return
        }

        // Validate enchantment level (must be a positive integer)
        val level: Int
        try {
            level = levelStr.toInt()
            if (level <= 0) {
                player.sendMessage(langManager?.getComponent(player, "enchant.level-positive") ?: return)
                return
            }
        } catch (e: NumberFormatException) {
            player.sendMessage(langManager?.getComponent(player, "enchant.level-invalid") ?: return)
            return
        }

        // Find Enchantment by name
        val enchantment = findEnchantment(enchantmentName)
        if (enchantment == null) {
            player.sendMessage(
                langManager?.getComponent(player, "enchant.not-found", "enchantment" to enchantmentName) ?: return
            )
            player.sendMessage(langManager?.getComponent(player, "enchant.hint") ?: return)
            return
        }

        // Add unsafe enchantment to the item
        item.addUnsafeEnchantment(enchantment, level)
        ItemUtils.resetAndUpdateTooltip(player, item)

        player.sendMessage(
            langManager?.getComponent(
                player,
                "enchant.add-success",
                "enchantment" to enchantment.key.key,
                "level" to level.toString()
            ) ?: return
        )
    }

    /**
     * Executes removing an enchantment from the item in player's main hand
     * 
     * @param player Player executing the command
     * @param args Arguments after /mh removeenchant
     */
    fun executeRemove(player: Player, args: Array<out String>) {
        val langManager = LanguageManager.get()
        
        // Check argument count: requires <enchantment>
        if (args.size < 2) {
            player.sendMessage(langManager?.getComponent(player, "enchant.remove-usage") ?: return)
            return
        }

        val enchantmentName = args[1]

        // Check if player is holding an item
        val item = ItemEditor.getItemInHand(player)
        if (item == null) {
            player.sendMessage(langManager?.getComponent(player, "general.no-item") ?: return)
            return
        }

        // Find Enchantment by name
        val enchantment = findEnchantment(enchantmentName)
        if (enchantment == null) {
            player.sendMessage(
                langManager?.getComponent(player, "enchant.not-found", "enchantment" to enchantmentName) ?: return
            )
            return
        }

        // Check if item contains the enchantment
        if (!item.containsEnchantment(enchantment)) {
            player.sendMessage(
                langManager?.getComponent(player, "enchant.remove-not-found", "enchantment" to enchantment.key.key) ?: return
            )
            return
        }

        // Remove enchantment
        item.removeEnchantment(enchantment)
        ItemUtils.resetAndUpdateTooltip(player, item)

        player.sendMessage(
            langManager?.getComponent(player, "enchant.remove-success", "enchantment" to enchantment.key.key) ?: return
        )
    }

    /**
     * Helper to find Enchantment by name or key namespace
     * 
     * @param name Enchantment name to find
     * @return Enchantment instance or null if not found
     */
    private fun findEnchantment(name: String): Enchantment? {
        val enchant = Enchantment.getByName(name.uppercase())
        if (enchant != null) return enchant

        return Enchantment.values().firstOrNull { 
            it.key.key.equals(name, ignoreCase = true) 
        }
    }
}
