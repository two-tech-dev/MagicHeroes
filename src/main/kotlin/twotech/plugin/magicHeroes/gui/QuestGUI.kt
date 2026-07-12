package twotech.plugin.magicHeroes.gui

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import twotech.plugin.magicHeroes.manager.HeroPlayerManager
import twotech.plugin.magicHeroes.manager.LanguageManager
import twotech.plugin.magicHeroes.quest.Quest
import twotech.plugin.magicHeroes.quest.QuestService

class QuestGUI(private val player: Player, private val quests: QuestService) : InventoryHolder {
    private lateinit var inventory: Inventory
    override fun getInventory(): Inventory = inventory

    fun open() {
        inventory = Bukkit.createInventory(this, 54, language("gui.quest-title", "Quest Menu"))
        fillBackground()
        addHeader()
        addQuests()
        addFooter()
        player.openInventory(inventory)
    }

    private fun fillBackground() {
        val filler = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
        filler.itemMeta = filler.itemMeta?.apply { displayName(Component.empty()) }
        (0 until inventory.size).forEach { inventory.setItem(it, filler) }
    }

    private fun addHeader() {
        val data = HeroPlayerManager.get()?.getPlayerData(player.uniqueId)
        val active = data?.questProgress?.size ?: 0
        val completed = data?.completedQuests?.size ?: 0
        val item = ItemStack(Material.NETHER_STAR)
        val meta = item.itemMeta ?: return
        meta.displayName(language("gui.quest-header", "Your Adventures"))
        meta.lore(listOf(
            language("gui.quest-header-active", "Active: $active", "value" to active.toString()),
            language("gui.quest-header-completed", "Completed: $completed", "value" to completed.toString())
        ))
        item.itemMeta = meta
        inventory.setItem(4, item)
    }

    private fun addQuests() {
        val data = HeroPlayerManager.get()?.getPlayerData(player.uniqueId)
        val questSlots = (10..16) + (19..25) + (28..34)
        quests.quests().take(questSlots.size).forEachIndexed { index, quest ->
            val active = quest.id in data?.questProgress.orEmpty()
            val completed = quest.id in data?.completedQuests.orEmpty()
            val prerequisitesMet = quest.prerequisites.all { it in data?.completedQuests.orEmpty() }
            val item = ItemStack(
                when {
                    completed -> Material.LIME_DYE
                    active -> Material.ENCHANTED_BOOK
                    prerequisitesMet -> Material.BOOK
                    else -> Material.BARRIER
                }
            )
            val meta = item.itemMeta ?: return@forEachIndexed
            meta.displayName(language("gui.quest-entry", quest.displayName, "value" to quest.displayName))
            meta.lore(questLore(quest, active, completed, prerequisitesMet))
            if (!completed && !active && prerequisitesMet) {
                meta.persistentDataContainer.set(key, PersistentDataType.STRING, "start:${quest.id}")
            }
            item.itemMeta = meta
            inventory.setItem(questSlots[index], item)
        }
    }

    private fun questLore(quest: Quest, active: Boolean, completed: Boolean, prerequisitesMet: Boolean): List<Component> {
        val data = HeroPlayerManager.get()?.getPlayerData(player.uniqueId)
        val progress = data?.questProgress?.get(quest.id).orEmpty()
        val status = when {
            completed -> language("gui.quest-status-completed", "Completed")
            active -> language("gui.quest-status-active", "In Progress")
            prerequisitesMet -> language("gui.quest-status-available", "Available")
            else -> language("gui.quest-status-locked", "Locked")
        }
        return buildList {
            add(status)
            add(Component.empty())
            add(language("gui.quest-objectives", "Objectives"))
            quest.objectives.forEach { objective ->
                val value = "${progress[objective.id] ?: 0}/${objective.required}"
                add(language("gui.quest-objective", "- ${objective.type}: $value", "type" to objective.type.name, "target" to objective.target, "value" to value))
            }
            add(Component.empty())
            add(language("gui.quest-reward-exp", "Reward: ${quest.rewardExp} EXP", "value" to quest.rewardExp.toString()))
            if (!completed && !active && prerequisitesMet) add(language("gui.quest-click-start", "Click to start"))
        }
    }

    private fun addFooter() {
        setButton(49, Material.BARRIER, "close", "gui.quest-close", "Close")
        if (!player.hasPermission("magicheroes.quest.admin")) return
        setButton(45, Material.WRITABLE_BOOK, "admin:add", "gui.quest-add", "Add Quest")
        setButton(46, Material.LAVA_BUCKET, "admin:remove", "gui.quest-remove", "Remove Quest")
        setButton(47, Material.ANVIL, "admin:edit", "gui.quest-edit", "Edit Quest")
    }

    private fun setButton(slot: Int, material: Material, action: String, langKey: String, fallback: String) {
        val item = ItemStack(material)
        val meta = item.itemMeta ?: return
        meta.displayName(language(langKey, fallback))
        meta.persistentDataContainer.set(key, PersistentDataType.STRING, action)
        item.itemMeta = meta
        inventory.setItem(slot, item)
    }

    private fun language(key: String, fallback: String, vararg replacements: Pair<String, String>): Component =
        LanguageManager.get()?.getComponent(player, key, *replacements) ?: Component.text(fallback)

    companion object {
        val key: NamespacedKey
            get() = NamespacedKey(Bukkit.getPluginManager().getPlugin("MagicHeroes")!!, "quest_gui_action")
    }
}
