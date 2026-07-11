package twotech.plugin.magicHeroes.handler

import org.bukkit.entity.Player
import twotech.plugin.magicHeroes.manager.LanguageManager
import twotech.plugin.magicHeroes.manager.TooltipManager

/**
 * Handler for Sub-Command: /mh reload
 * Reloads all plugin configurations and language files
 */
object ReloadHandler {

    /**
     * Executes the reload operation
     * 
     * @param player Player executing the command
     * @param args Arguments after /mh reload
     */
    fun execute(player: Player, args: Array<out String>) {
        val langManager = LanguageManager.get() ?: run {
            player.sendMessage("§cLanguage system not initialized!")
            return
        }
        
        // Reload all configuration files
        langManager.reload()
        
        // Reload tooltip templates
        TooltipManager.get()?.reload()
        // Send success message to player
        player.sendMessage(
            langManager.getComponent(player, "general.reload-success")
        )
    }
}
