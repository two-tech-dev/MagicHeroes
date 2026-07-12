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
import twotech.plugin.magicHeroes.manager.LanguageManager

class MainGUI(private val player: Player) : InventoryHolder {
    private lateinit var inventory: Inventory
    override fun getInventory(): Inventory = inventory

    fun open() {
        inventory = Bukkit.createInventory(this, 54, text("gui.main-hub-title", "MagicHeroes"))
        val filler = ItemStack(Material.PURPLE_STAINED_GLASS_PANE)
        filler.itemMeta = filler.itemMeta?.apply { displayName(Component.empty()) }
        (0 until 54).forEach { inventory.setItem(it, filler) }
        button(10, Material.BOOK, "quest", "gui.hub-quests", "Quests")
        button(12, Material.PLAYER_HEAD, "party", "gui.hub-party", "Party")
        button(14, Material.NETHER_STAR, "skills", "gui.hub-skills", "Skills")
        button(16, Material.COMPASS, "waypoint", "gui.hub-waypoints", "Waypoints")
        button(28, Material.ANVIL, "item", "gui.hub-items", "Items")
        button(30, Material.CRAFTING_TABLE, "craft", "gui.hub-crafting", "Crafting")
        button(32, Material.CHEST, "loot", "gui.hub-loot", "Loot")
        button(34, Material.BOOKSHELF, "stats", "gui.hub-stats", "Stats")
        button(49, Material.BARRIER, "close", "gui.close", "Close")
        player.openInventory(inventory)
    }

    private fun button(slot: Int, material: Material, action: String, key: String, fallback: String) {
        val item = ItemStack(material)
        val meta = item.itemMeta ?: return
        meta.displayName(text(key, fallback))
        meta.lore(listOf(text("gui.hub-click", "Click to open")))
        meta.persistentDataContainer.set(actionKey, PersistentDataType.STRING, action)
        item.itemMeta = meta
        inventory.setItem(slot, item)
    }

    private fun text(key: String, fallback: String): Component = LanguageManager.get()?.getComponent(player, key) ?: Component.text(fallback)

    companion object {
        val actionKey: NamespacedKey
            get() = NamespacedKey(Bukkit.getPluginManager().getPlugin("MagicHeroes")!!, "main_gui_action")
    }
}
