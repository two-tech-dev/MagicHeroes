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
        if (state.startsWith("quest_")) {
            handleQuestInput(player, state, input)
            GUIClickListener.clearInputState(player.uniqueId.toString())
            return
        }
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

    private fun handleQuestInput(player: Player, state: String, input: String) {
        if (!player.hasPermission("magicheroes.quest.admin")) return
        val quests = (plugin as? twotech.plugin.magicHeroes.MagicHeroes)?.questService ?: return
        val result = when (state) {
            "quest_add" -> {
                val parts = input.split('|').map(String::trim)
                if (parts.size != 6) null else {
                    val type = runCatching { twotech.plugin.magicHeroes.quest.QuestObjectiveType.valueOf(parts[2].uppercase()) }.getOrNull()
                    val required = parts[4].toIntOrNull()
                    val exp = parts[5].toIntOrNull()
                    if (type == null || required == null || exp == null) null else quests.addSimple(parts[0], parts[1], type, parts[3], required, exp)
                }
            }
            "quest_remove" -> quests.remove(input)
            "quest_edit" -> input.split('|', limit = 2).map(String::trim).let { if (it.size == 2) quests.editDisplayName(it[0], it[1]) else null }
            else -> null
        }
        player.sendMessage(net.kyori.adventure.text.Component.text(result?.message ?: "Invalid quest input."))
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
