package twotech.plugin.magicHeroes.handler

import org.bukkit.entity.Player
import twotech.plugin.magicHeroes.gui.ItemEditorGUI
import twotech.plugin.magicHeroes.manager.LanguageManager
import twotech.plugin.magicHeroes.util.ItemEditor

/**
 * Handler for Sub-Command: /mh gui
 * Opens the item editor GUI
 */
object GUIHandler {

    /**
     * Executes opening the item editor GUI for a player
     * 
     * @param player Player executing the command
     * @param args Arguments after /mh gui (unused)
     */
    fun execute(player: Player, args: Array<out String>) {
        val langManager = LanguageManager.get()
        
        // Check if player is holding a valid item in main hand
        if (!ItemEditor.hasValidItemInHand(player)) {
            player.sendMessage(langManager?.getComponent(player, "gui.no-item-error") ?: return)
            return
        }

        // Open the item editor GUI
        ItemEditorGUI(player).openGUI()
    }
}
