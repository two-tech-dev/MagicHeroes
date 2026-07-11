package twotech.plugin.magicHeroes.handler

import org.bukkit.entity.Player
import twotech.plugin.magicHeroes.manager.LanguageManager
import twotech.plugin.magicHeroes.manager.TooltipManager
import twotech.plugin.magicHeroes.util.ItemEditor

/**
 * Handler for Sub-Command: /mh settooltip <template>
 * Sets a specific tooltip style for the item in player's main hand
 */
object SetTooltipHandler {

    fun execute(player: Player, args: Array<out String>) {
        val langManager = LanguageManager.get()
        
        if (args.size < 2) {
            player.sendMessage(langManager?.getComponent(player, "tooltip.set-usage") ?: return)
            return
        }

        val templateName = args[1].lowercase()
        val item = ItemEditor.getItemInHand(player)
        
        if (item == null) {
            player.sendMessage(langManager?.getComponent(player, "general.no-item") ?: return)
            return
        }

        val tooltipManager = TooltipManager.get()
        if (tooltipManager == null) {
            player.sendMessage("§cTooltip system not initialized!")
            return
        }

        if (!tooltipManager.getAvailableTooltips().contains(templateName)) {
            player.sendMessage(langManager?.getComponent(player, "tooltip.not-found", "template" to templateName) ?: return)
            return
        }

        // Apply and cache template
        tooltipManager.applyTooltipByName(player, item, templateName)
        player.sendMessage(langManager?.getComponent(player, "tooltip.set-success", "template" to templateName) ?: return)
    }
}
