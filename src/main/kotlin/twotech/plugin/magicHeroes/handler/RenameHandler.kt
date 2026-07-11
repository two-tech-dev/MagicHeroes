package twotech.plugin.magicHeroes.handler

import org.bukkit.entity.Player
import twotech.plugin.magicHeroes.manager.LanguageManager
import twotech.plugin.magicHeroes.util.ItemEditor
import twotech.plugin.magicHeroes.util.ColorTranslator
import twotech.plugin.magicHeroes.util.ItemUtils

/**
 * Handler for Sub-Command: /mh rename <text>
 */
object RenameHandler {

    /**
     * Executes renaming of the item in player's main hand
     * 
     * @param player Player executing the command
     * @param args Arguments after /mh rename
     */
    fun execute(player: Player, args: Array<out String>) {
        val langManager = LanguageManager.get()
        
        // Check if player provided enough arguments
        if (args.size < 2) {
            player.sendMessage(
                langManager?.getComponent(player, "rename.usage") ?: return
            )
            return
        }

        // Get all text after args[0] and join with spaces
        val textToRename = args.drop(1).joinToString(" ")

        // Check if player is holding an item
        val item = ItemEditor.getItemInHand(player)
        if (item == null) {
            player.sendMessage(
                langManager?.getComponent(player, "general.no-item") ?: return
            )
            return
        }

        // Retrieve and update ItemMeta
        val meta = item.itemMeta

        if (meta == null) {
            player.sendMessage(
                langManager?.getComponent(player, "general.no-meta") ?: return
            )
            return
        }

        // Save name to PDC and apply tooltip style
        val plugin = org.bukkit.Bukkit.getPluginManager().getPlugin("MagicHeroes")!!
        meta.persistentDataContainer.set(
            org.bukkit.NamespacedKey(plugin, "mh_name"),
            org.bukkit.persistence.PersistentDataType.STRING,
            textToRename
        )
        item.itemMeta = meta
        
        ItemUtils.resetAndUpdateTooltip(player, item)

        player.sendMessage(
            langManager?.getComponent(player, "rename.success") ?: return
        )
    }
}
