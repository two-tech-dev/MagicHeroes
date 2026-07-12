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

class ItemEditorGUI(private val player: Player) : InventoryHolder {
    companion object {
        const val GUI_SIZE = 54
        const val RENAME_SLOT = 20
        const val LORE_SLOT = 22
        const val ENCHANT_SLOT = 24
        const val TOOLTIP_SLOT = 38
        const val DURABILITY_SLOT = 40
        const val CLOSE_SLOT = 49
        val buttonKey: NamespacedKey
            get() = NamespacedKey(Bukkit.getPluginManager().getPlugin("MagicHeroes")!!, "button_type")
    }

    private lateinit var inventory: Inventory
    override fun getInventory(): Inventory = inventory

    fun openGUI() {
        inventory = Bukkit.createInventory(this, GUI_SIZE, text("gui.item-editor-title", "Item Editor"))
        fillBackground()
        addPreview()
        button(RENAME_SLOT, Material.NAME_TAG, "rename", "gui.rename-button", "gui.rename-lore-1", "gui.rename-lore-2")
        button(LORE_SLOT, Material.WRITABLE_BOOK, "lore", "gui.lore-button", "gui.lore-lore-1", "gui.lore-lore-2")
        button(ENCHANT_SLOT, Material.ENCHANTING_TABLE, "enchant", "gui.enchant-button", "gui.enchant-lore-1", "gui.enchant-lore-2")
        button(TOOLTIP_SLOT, Material.SPYGLASS, "tooltip", "gui.tooltip-button", "gui.tooltip-lore-1", "gui.tooltip-lore-2")
        button(DURABILITY_SLOT, Material.DIAMOND_PICKAXE, "durability", "gui.durability-button", "gui.durability-lore-1", "gui.durability-lore-2")
        button(CLOSE_SLOT, Material.BARRIER, "close", "gui.close", "gui.item-close-lore", "gui.item-close-lore")
        player.openInventory(inventory)
    }

    private fun fillBackground() {
        val border = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
        border.itemMeta = border.itemMeta?.apply { displayName(Component.empty()) }
        val accent = ItemStack(Material.PURPLE_STAINED_GLASS_PANE)
        accent.itemMeta = accent.itemMeta?.apply { displayName(Component.empty()) }
        (0 until GUI_SIZE).forEach { inventory.setItem(it, if (it / 9 == 0 || it / 9 == 5 || it % 9 == 0 || it % 9 == 8) accent else border) }
    }

    private fun addPreview() {
        val item = player.inventory.itemInMainHand.clone()
        val meta = item.itemMeta ?: return
        val lore = meta.lore()?.toMutableList() ?: mutableListOf()
        lore += Component.empty()
        lore += text("gui.item-preview-lore", "Item currently being edited")
        meta.lore(lore)
        item.itemMeta = meta
        inventory.setItem(13, item)
    }

    private fun button(slot: Int, material: Material, action: String, nameKey: String, loreOneKey: String, loreTwoKey: String) {
        val item = ItemStack(material)
        val meta = item.itemMeta ?: return
        meta.displayName(text(nameKey, action))
        meta.lore(listOf(text(loreOneKey, "Click"), text(loreTwoKey, "Manage")))
        meta.persistentDataContainer.set(buttonKey, PersistentDataType.STRING, action)
        item.itemMeta = meta
        inventory.setItem(slot, item)
    }

    private fun text(key: String, fallback: String): Component = LanguageManager.get()?.getComponent(player, key) ?: Component.text(fallback)

}
