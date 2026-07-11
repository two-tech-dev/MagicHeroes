package twotech.plugin.magicHeroes.listener

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import twotech.plugin.magicHeroes.manager.DurabilityManager
import twotech.plugin.magicHeroes.manager.LanguageManager
import twotech.plugin.magicHeroes.util.ItemEditor
import twotech.plugin.magicHeroes.util.ItemUtils

class ChatInputListener(private val plugin: JavaPlugin) : Listener {
    @EventHandler
    fun onPlayerChat(event: AsyncChatEvent) {
        val playerId = event.player.uniqueId.toString()
        val state = GUIClickListener.getInputState(playerId) ?: return
        event.isCancelled = true
        val input = PlainTextComponentSerializer.plainText().serialize(event.message()).trim()
        Bukkit.getScheduler().runTask(plugin, Runnable {
            if (GUIClickListener.getInputState(playerId) != state) return@Runnable
            handleInput(event.player, state, input)
        })
    }

    private fun handleInput(player: Player, state: String, input: String) {
        val language = LanguageManager.get()
        if (input.equals("cancel", true)) {
            player.sendMessage(language?.getComponent(player, "input.cancel") ?: return)
            GUIClickListener.clearInputState(player.uniqueId.toString())
            return
        }
        val item = ItemEditor.getItemInHand(player)
        if (item == null) {
            player.sendMessage(language?.getComponent(player, "gui.no-item-dropped") ?: return)
            GUIClickListener.clearInputState(player.uniqueId.toString())
            return
        }
        when (state) {
            "rename" -> {
                val meta = item.itemMeta ?: return
                meta.persistentDataContainer.set(
                    twotech.plugin.magicHeroes.util.PdcKeys(plugin).name,
                    org.bukkit.persistence.PersistentDataType.STRING,
                    input
                )
                item.itemMeta = meta
                ItemUtils.resetAndUpdateTooltip(player, item)
                player.sendMessage(language?.getComponent(player, "rename.success") ?: return)
            }
            "lore" -> {
                val meta = item.itemMeta ?: return
                meta.persistentDataContainer.set(
                    twotech.plugin.magicHeroes.util.PdcKeys(plugin).customLore,
                    org.bukkit.persistence.PersistentDataType.STRING,
                    input
                )
                item.itemMeta = meta
                ItemUtils.resetAndUpdateTooltip(player, item)
                player.sendMessage(language?.getComponent(player, "lore.success") ?: return)
            }
            "durability_set" -> setDurability(player, input, language)
        }
        GUIClickListener.clearInputState(player.uniqueId.toString())
    }

    private fun setDurability(player: Player, input: String, language: LanguageManager?) {
        val parts = input.split(Regex("\\s+"))
        if (parts.size != 2) {
            player.sendMessage(language?.getComponent(player, "durability.set-usage") ?: return)
            return
        }
        val current = parts[0].toIntOrNull()
        val max = parts[1].toIntOrNull()
        if (current == null || max == null || max <= 0 || current !in 0..max) {
            player.sendMessage(language?.getComponent(player, "durability.set-usage") ?: return)
            return
        }
        val item = ItemEditor.getItemInHand(player) ?: return
        DurabilityManager.get()?.setDurability(item, current, max) ?: return
        ItemUtils.resetAndUpdateTooltip(player, item)
        player.sendMessage(language?.getComponent(player, "durability.set-success", "current" to current.toString(), "max" to max.toString()) ?: return)
    }
}
