package twotech.plugin.magicHeroes.gui

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import twotech.plugin.magicHeroes.manager.LanguageManager
import net.kyori.adventure.text.Component

/**
 * Main GUI interface for quick item editing
 * Shows 3 choices: Rename, Change Lore, Manage Enchantments
 */
class ItemEditorGUI(private val player: Player) : InventoryHolder {
    companion object {
        const val GUI_SIZE = 27
        const val RENAME_SLOT = 11
        const val LORE_SLOT = 13
        const val ENCHANT_SLOT = 15
        const val DURABILITY_SLOT = 22
        const val TOOLTIP_SLOT = 20
    }
    
    private lateinit var inventory: Inventory
    
    /**
     * Create and open the item editor GUI for a player
     */
    fun openGUI() {
        val langManager = LanguageManager.get()
        val title = langManager?.getComponent(player, "gui.main-title") ?: Component.text("Item Editor")
        
        inventory = Bukkit.createInventory(this, GUI_SIZE, title)
        
        // Fill background with decorative stained glass panes
        fillBackground()
        
        // Add main action buttons
        addRenameButton()
        addLoreButton()
        addEnchantButton()
        addDurabilityButton()
        addTooltipButton()
        // Open the inventory for player
        player.openInventory(inventory)
    }
    
    /**
     * Fills GUI background with black stained glass panes
     */
    private fun fillBackground() {
        val glassPane = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
        val meta = glassPane.itemMeta ?: return
        meta.displayName(Component.empty()) // Hide display name
        glassPane.itemMeta = meta
        
        // Fill all slots except action slots
        for (i in 0 until GUI_SIZE) {
            if (i != RENAME_SLOT && i != LORE_SLOT && i != ENCHANT_SLOT && i != DURABILITY_SLOT && i != TOOLTIP_SLOT) {
                inventory.setItem(i, glassPane)
            }
        }
    }
    
    /**
     * Adds the rename button (using Name Tag)
     */
    private fun addRenameButton() {
        val nameTag = ItemStack(Material.NAME_TAG)
        val meta = nameTag.itemMeta ?: return
        val langManager = LanguageManager.get()
        
        meta.displayName(langManager?.getComponent(player, "gui.rename-button") ?: Component.text("Rename"))
        val lore = listOf(
            langManager?.getComponent(player, "gui.rename-lore-1") ?: Component.empty(),
            langManager?.getComponent(player, "gui.rename-lore-2") ?: Component.empty()
        )
        meta.lore(lore)
        
        // Set metadata to identify button type
        meta.persistentDataContainer.set(
            org.bukkit.NamespacedKey(org.bukkit.Bukkit.getPluginManager().getPlugin("MagicHeroes")!!, "button_type"),
            PersistentDataType.STRING,
            "rename"
        )
        nameTag.itemMeta = meta
        
        inventory.setItem(RENAME_SLOT, nameTag)
    }
    
    /**
     * Adds the change lore button (using Painting)
     */
    private fun addLoreButton() {
        val art = ItemStack(Material.PAINTING)
        val meta = art.itemMeta ?: return
        val langManager = LanguageManager.get()
        
        meta.displayName(langManager?.getComponent(player, "gui.lore-button") ?: Component.text("Change Lore"))
        val lore = listOf(
            langManager?.getComponent(player, "gui.lore-lore-1") ?: Component.empty(),
            langManager?.getComponent(player, "gui.lore-lore-2") ?: Component.empty()
        )
        meta.lore(lore)
        
        // Set metadata to identify button type
        meta.persistentDataContainer.set(
            org.bukkit.NamespacedKey(org.bukkit.Bukkit.getPluginManager().getPlugin("MagicHeroes")!!, "button_type"),
            PersistentDataType.STRING,
            "lore"
        )
        art.itemMeta = meta
        
        inventory.setItem(LORE_SLOT, art)
    }
    
    /**
     * Adds the enchantment manager button (using Enchantment Table)
     */
    private fun addEnchantButton() {
        val enchantTable = ItemStack(Material.ENCHANTING_TABLE)
        val meta = enchantTable.itemMeta ?: return
        val langManager = LanguageManager.get()
        
        meta.displayName(langManager?.getComponent(player, "gui.enchant-button") ?: Component.text("Manage Enchantments"))
        val lore = listOf(
            langManager?.getComponent(player, "gui.enchant-lore-1") ?: Component.empty(),
            langManager?.getComponent(player, "gui.enchant-lore-2") ?: Component.empty()
        )
        meta.lore(lore)
        
        // Set metadata to identify button type
        meta.persistentDataContainer.set(
            org.bukkit.NamespacedKey(org.bukkit.Bukkit.getPluginManager().getPlugin("MagicHeroes")!!, "button_type"),
            PersistentDataType.STRING,
            "enchant"
        )
        enchantTable.itemMeta = meta
        
        inventory.setItem(ENCHANT_SLOT, enchantTable)
    }
    
    /**
     * Adds the durability manager button (using Diamond Pickaxe)
     */
    private fun addDurabilityButton() {
        val pickaxe = ItemStack(Material.DIAMOND_PICKAXE)
        val meta = pickaxe.itemMeta ?: return
        val langManager = LanguageManager.get()
        
        meta.displayName(langManager?.getComponent(player, "gui.durability-button") ?: Component.text("Manage Durability"))
        val lore = listOf(
            langManager?.getComponent(player, "gui.durability-lore-1") ?: Component.empty(),
            langManager?.getComponent(player, "gui.durability-lore-2") ?: Component.empty()
        )
        meta.lore(lore)
        
        // Set metadata to identify button type
        meta.persistentDataContainer.set(
            org.bukkit.NamespacedKey(org.bukkit.Bukkit.getPluginManager().getPlugin("MagicHeroes")!!, "button_type"),
            PersistentDataType.STRING,
            "durability"
        )
        pickaxe.itemMeta = meta
        
        inventory.setItem(DURABILITY_SLOT, pickaxe)
    }
    
    /**
     * Adds the tooltip manager button (using Spyglass)
     */
    private fun addTooltipButton() {
        val spyglass = ItemStack(Material.SPYGLASS)
        val meta = spyglass.itemMeta ?: return
        val langManager = LanguageManager.get()
        
        meta.displayName(langManager?.getComponent(player, "gui.tooltip-button") ?: Component.text("Manage Tooltips"))
        val lore = listOf(
            langManager?.getComponent(player, "gui.tooltip-lore-1") ?: Component.empty(),
            langManager?.getComponent(player, "gui.tooltip-lore-2") ?: Component.empty()
        )
        meta.lore(lore)
        
        // Set metadata to identify button type
        meta.persistentDataContainer.set(
            org.bukkit.NamespacedKey(org.bukkit.Bukkit.getPluginManager().getPlugin("MagicHeroes")!!, "button_type"),
            PersistentDataType.STRING,
            "tooltip"
        )
        spyglass.itemMeta = meta
        
        inventory.setItem(TOOLTIP_SLOT, spyglass)
    }

    override fun getInventory(): Inventory = inventory
}
