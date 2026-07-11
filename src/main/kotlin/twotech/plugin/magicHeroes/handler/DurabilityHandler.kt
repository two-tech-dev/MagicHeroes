package twotech.plugin.magicHeroes.handler

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import twotech.plugin.magicHeroes.manager.LanguageManager
import twotech.plugin.magicHeroes.manager.DurabilityManager

/**
 * Handles /mh durability commands for custom item durability
 */
object DurabilityHandler {

    fun execute(player: Player, args: Array<out String>) {
        val langManager = LanguageManager.get()
        val durabilityManager = DurabilityManager.get()

        if (langManager == null || durabilityManager == null) {
            player.sendMessage("&cDurability system not initialized!")
            return
        }

        // Check if player is holding item
        val item = player.inventory.itemInMainHand
        if (item.type.isAir) {
            player.sendMessage(langManager.getComponent(player, "general.no-item"))
            return
        }

        // Get sub-command
        if (args.size < 2) {
            player.sendMessage(langManager.getComponent(player, "durability.usage"))
            return
        }

        val subcommand = args[1].lowercase()

        when (subcommand) {
            "set" -> executeSet(player, args, langManager, durabilityManager, item)
            "infinite" -> executeInfinite(player, langManager, durabilityManager, item)
            "check" -> executeCheck(player, langManager, durabilityManager, item)
            "reset" -> executeReset(player, langManager, durabilityManager, item)
            else -> player.sendMessage(langManager.getComponent(player, "durability.usage"))
        }
    }

    private fun executeSet(
        player: Player,
        args: Array<out String>,
        langManager: LanguageManager,
        durabilityManager: DurabilityManager,
        item: ItemStack
    ) {
        if (args.size < 4) {
            player.sendMessage(langManager.getComponent(player, "durability.set-usage"))
            return
        }

        val currentStr = args[2]
        val maxStr = args[3]

        val current = currentStr.toIntOrNull()
        val max = maxStr.toIntOrNull()

        if (current == null) {
            player.sendMessage(langManager.getComponent(player, "durability.invalid-number", "value" to currentStr))
            return
        }

        if (max == null) {
            player.sendMessage(langManager.getComponent(player, "durability.invalid-number", "value" to maxStr))
            return
        }

        if (max <= 0 || current !in 0..max) {
            player.sendMessage(langManager.getComponent(player, "durability.max-positive"))
            return
        }

        durabilityManager.setDurability(item, current, max)
        twotech.plugin.magicHeroes.util.ItemUtils.resetAndUpdateTooltip(player, item)

        player.sendMessage(
            langManager.getComponent(player, "durability.set-success", "current" to current.toString(), "max" to max.toString())
        )
    }

    private fun executeInfinite(
        player: Player,
        langManager: LanguageManager,
        durabilityManager: DurabilityManager,
        item: ItemStack
    ) {
        durabilityManager.setInfiniteDurability(item)
        twotech.plugin.magicHeroes.util.ItemUtils.resetAndUpdateTooltip(player, item)


        player.sendMessage(langManager.getComponent(player, "durability.infinite-success"))
    }

    private fun executeCheck(
        player: Player,
        langManager: LanguageManager,
        durabilityManager: DurabilityManager,
        item: ItemStack
    ) {
        if (!durabilityManager.hasDurability(item)) {
            player.sendMessage(langManager.getComponent(player, "durability.no-durability"))
            return
        }

        val current = durabilityManager.getCurrentDurability(item)
        val max = durabilityManager.getMaxDurability(item)

        if (max == -1) {
            player.sendMessage(langManager.getComponent(player, "durability.check-infinite"))
        } else {
            player.sendMessage(
                langManager.getComponent(player, "durability.check", "current" to current.toString(), "max" to max.toString())
            )
        }
    }

    private fun executeReset(
        player: Player,
        langManager: LanguageManager,
        durabilityManager: DurabilityManager,
        item: ItemStack
    ) {
        if (!durabilityManager.hasDurability(item)) {
            player.sendMessage(langManager.getComponent(player, "durability.no-durability"))
            return
        }

        durabilityManager.resetDurability(item)
        twotech.plugin.magicHeroes.util.ItemUtils.resetAndUpdateTooltip(player, item)


        player.sendMessage(langManager.getComponent(player, "durability.reset-success"))
    }
}
