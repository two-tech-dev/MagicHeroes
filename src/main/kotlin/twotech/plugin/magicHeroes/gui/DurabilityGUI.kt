package twotech.plugin.magicHeroes.gui

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import twotech.plugin.magicHeroes.manager.DurabilityManager
import twotech.plugin.magicHeroes.manager.LanguageManager
import net.kyori.adventure.text.Component

/**
 * GUI for managing item durability
 * Shows options: Set, Infinite, Check, Reset
 */
class DurabilityGUI(private val player: Player, private val item: ItemStack) : InventoryHolder {
    
    companion object {
        const val GUI_SIZE = 27
        const val SET_SLOT = 10
        const val INFINITE_SLOT = 12
        const val CHECK_SLOT = 14
        const val RESET_SLOT = 16
        const val INFO_SLOT = 22
        const val BACK_SLOT = 26
    }
    
    private lateinit var inventory: Inventory
    private val durabilityManager = DurabilityManager.get()
    
    /**
     * Creates and opens the durability GUI for a player
     */
    fun openGUI() {
        val langManager = LanguageManager.get()
        val title = langManager?.getComponent(player, "gui.durability-title") ?: Component.text("Durability Manager")
        
        inventory = Bukkit.createInventory(this, GUI_SIZE, title)
        
        // Fill background
        fillBackground()
        
        // Add action buttons
        addSetButton()
        addInfiniteButton()
        addCheckButton()
        addResetButton()
        addInfoDisplay()
        addBackButton()
        
        // Open the inventory for player
        player.openInventory(inventory)
    }
    
    /**
     * Fills GUI background with gray stained glass panes
     */
    private fun fillBackground() {
        val glassPane = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
        val meta = glassPane.itemMeta ?: return
        meta.displayName(Component.empty())
        glassPane.itemMeta = meta
        
        for (i in 0 until GUI_SIZE) {
            if (i != SET_SLOT && i != INFINITE_SLOT && i != CHECK_SLOT && 
                i != RESET_SLOT && i != INFO_SLOT && i != BACK_SLOT) {
                inventory.setItem(i, glassPane)
            }
        }
    }
    
    /**
     * Adds the Set Durability button
     */
    private fun addSetButton() {
        val button = ItemStack(Material.ANVIL)
        val meta = button.itemMeta ?: return
        val langManager = LanguageManager.get()
        
        meta.displayName(langManager?.getComponent(player, "gui.durability-set-button") ?: Component.text("Set Durability"))
        val lore = listOf(
            langManager?.getComponent(player, "gui.durability-set-lore-1") ?: Component.empty(),
            langManager?.getComponent(player, "gui.durability-set-lore-2") ?: Component.empty(),
            langManager?.getComponent(player, "gui.durability-set-lore-3") ?: Component.empty()
        )
        meta.lore(lore)
        
        meta.persistentDataContainer.set(
            org.bukkit.NamespacedKey(org.bukkit.Bukkit.getPluginManager().getPlugin("MagicHeroes")!!, "button_type"),
            PersistentDataType.STRING,
            "durability_set"
        )
        button.itemMeta = meta
        
        inventory.setItem(SET_SLOT, button)
    }
    
    /**
     * Adds the Set Infinite button
     */
    private fun addInfiniteButton() {
        val button = ItemStack(Material.NETHER_STAR)
        val meta = button.itemMeta ?: return
        val langManager = LanguageManager.get()
        
        meta.displayName(langManager?.getComponent(player, "gui.durability-infinite-button") ?: Component.text("Set Infinite"))
        val lore = listOf(
            langManager?.getComponent(player, "gui.durability-infinite-lore-1") ?: Component.empty(),
            langManager?.getComponent(player, "gui.durability-infinite-lore-2") ?: Component.empty()
        )
        meta.lore(lore)
        
        meta.persistentDataContainer.set(
            org.bukkit.NamespacedKey(org.bukkit.Bukkit.getPluginManager().getPlugin("MagicHeroes")!!, "button_type"),
            PersistentDataType.STRING,
            "durability_infinite"
        )
        button.itemMeta = meta
        
        inventory.setItem(INFINITE_SLOT, button)
    }
    
    /**
     * Adds the Check Durability button
     */
    private fun addCheckButton() {
        val button = ItemStack(Material.SPYGLASS)
        val meta = button.itemMeta ?: return
        val langManager = LanguageManager.get()
        
        meta.displayName(langManager?.getComponent(player, "gui.durability-check-button") ?: Component.text("Check Durability"))
        val lore = listOf(
            langManager?.getComponent(player, "gui.durability-check-lore-1") ?: Component.empty(),
            langManager?.getComponent(player, "gui.durability-check-lore-2") ?: Component.empty()
        )
        meta.lore(lore)
        
        meta.persistentDataContainer.set(
            org.bukkit.NamespacedKey(org.bukkit.Bukkit.getPluginManager().getPlugin("MagicHeroes")!!, "button_type"),
            PersistentDataType.STRING,
            "durability_check"
        )
        button.itemMeta = meta
        
        inventory.setItem(CHECK_SLOT, button)
    }
    
    /**
     * Adds the Reset Durability button
     */
    private fun addResetButton() {
        val button = ItemStack(Material.BARRIER)
        val meta = button.itemMeta ?: return
        val langManager = LanguageManager.get()
        
        meta.displayName(langManager?.getComponent(player, "gui.durability-reset-button") ?: Component.text("Reset Durability"))
        val lore = listOf(
            langManager?.getComponent(player, "gui.durability-reset-lore-1") ?: Component.empty(),
            langManager?.getComponent(player, "gui.durability-reset-lore-2") ?: Component.empty()
        )
        meta.lore(lore)
        
        meta.persistentDataContainer.set(
            org.bukkit.NamespacedKey(org.bukkit.Bukkit.getPluginManager().getPlugin("MagicHeroes")!!, "button_type"),
            PersistentDataType.STRING,
            "durability_reset"
        )
        button.itemMeta = meta
        
        inventory.setItem(RESET_SLOT, button)
    }
    
    /**
     * Displays current durability info
     */
    private fun addInfoDisplay() {
        val infoItem = ItemStack(Material.BOOK)
        val meta = infoItem.itemMeta ?: return
        val langManager = LanguageManager.get()
        
        meta.displayName(langManager?.getComponent(player, "gui.durability-current-info") ?: Component.text("Current Durability:"))
        
        val lore = mutableListOf<Component>()
        if (durabilityManager != null && durabilityManager.hasDurability(item)) {
            if (durabilityManager.isInfinite(item)) {
                lore.add(langManager?.getComponent(player, "gui.durability-current-infinite") ?: Component.text("Infinite"))
            } else {
                val current = durabilityManager.getCurrentDurability(item)
                val max = durabilityManager.getMaxDurability(item)
                lore.add(langManager?.getComponent(player, "gui.durability-current-value", "current" to current.toString(), "max" to max.toString()) 
                    ?: Component.text("$current/$max"))
            }
        } else {
            lore.add(langManager?.getComponent(player, "gui.durability-current-none") ?: Component.text("No custom durability set"))
        }
        
        meta.lore(lore)
        infoItem.itemMeta = meta
        
        inventory.setItem(INFO_SLOT, infoItem)
    }
    
    /**
     * Adds the back button
     */
    private fun addBackButton() {
        val arrow = ItemStack(Material.ARROW)
        val meta = arrow.itemMeta ?: return
        val langManager = LanguageManager.get()
        
        meta.displayName(langManager?.getComponent(player, "gui.back-button") ?: Component.text("Back"))
        val lore = listOf(langManager?.getComponent(player, "gui.back-lore") ?: Component.empty())
        meta.lore(lore)
        
        meta.persistentDataContainer.set(
            org.bukkit.NamespacedKey(org.bukkit.Bukkit.getPluginManager().getPlugin("MagicHeroes")!!, "button_type"),
            PersistentDataType.STRING,
            "back"
        )
        arrow.itemMeta = meta
        
        inventory.setItem(BACK_SLOT, arrow)
    }
    
    override fun getInventory(): Inventory = inventory
}
