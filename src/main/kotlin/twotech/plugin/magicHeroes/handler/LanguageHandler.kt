package twotech.plugin.magicHeroes.handler

import org.bukkit.entity.Player
import twotech.plugin.magicHeroes.manager.LanguageManager

/**
 * Handler for Sub-Command: /mh language <code>
 * Allows players to change their language preference
 */
object LanguageHandler {

    /**
     * Executes the language switch command
     * 
     * @param player Player executing the command
     * @param args Arguments after /mh language
     */
    fun execute(player: Player, args: Array<out String>) {
        val langManager = LanguageManager.get() ?: run {
            player.sendMessage("§cLanguage system not initialized!")
            return
        }
        
        // Show current status if no arguments provided
        if (args.size < 2) {
            val currentLang = langManager.getPlayerLanguage(player)
            player.sendMessage(
                langManager.getComponent(player, "language.current", "language" to currentLang)
            )
            
            val availableLanguages = langManager.getAvailableLanguages().joinToString(", ")
            player.sendMessage(
                langManager.getComponent(player, "language.available", "languages" to availableLanguages)
            )
            player.sendMessage(
                langManager.getComponent(player, "language.usage")
            )
            return
        }
        
        val languageCode = args[1].lowercase()
        
        // Set new player language preference
        if (langManager.setPlayerLanguage(player, languageCode)) {
            player.sendMessage(
                langManager.getComponent(player, "language.changed", "language" to languageCode)
            )
        } else {
            player.sendMessage(
                langManager.getComponent(player, "language.invalid", "code" to languageCode)
            )
            
            val availableLanguages = langManager.getAvailableLanguages().joinToString(", ")
            player.sendMessage(
                langManager.getComponent(player, "language.available", "languages" to availableLanguages)
            )
        }
    }
}
