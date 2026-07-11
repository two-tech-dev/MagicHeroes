package twotech.plugin.magicHeroes.gui

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import twotech.plugin.magicHeroes.manager.LanguageManager
import net.kyori.adventure.text.Component

/**
 * GUI for managing item enchantments: lists selectable enchantments to add/remove
 */
class EnchantmentGUI(private val player: Player) : InventoryHolder {
    
    companion object {
        const val GUI_SIZE = 54 // 6 rows
    }
    
    private lateinit var inventory: Inventory
    
    /**
     * Creates and opens the enchantment manager GUI for a player
     */
    fun openGUI() {
        val langManager = LanguageManager.get()
        val title = langManager?.getComponent(player, "gui.enchant-title") ?: Component.text("Enchantment Manager")
        inventory = Bukkit.createInventory(this, GUI_SIZE, title)
        
        // Fill background with glass panes
        fillBackground()
        
        // Add enchantment selector buttons
        addEnchantmentButtons()
        
        // Add back arrow button
        addBackButton()
        
        player.openInventory(inventory)
    }
    
    /**
     * Fills GUI background with purple stained glass panes
     */
    private fun fillBackground() {
        val glassPane = ItemStack(Material.PURPLE_STAINED_GLASS_PANE)
        val meta = glassPane.itemMeta ?: return
        meta.displayName(Component.empty()) // Hide display name
        glassPane.itemMeta = meta
        
        for (i in 0 until GUI_SIZE) {
            inventory.setItem(i, glassPane)
        }
    }
    
    /**
     * Adds enchantment selector buttons inside slots
     */
    private fun addEnchantmentButtons() {
        val enchantments = listOf(
            Enchantment.SHARPNESS,
            Enchantment.KNOCKBACK,
            Enchantment.FIRE_ASPECT,
            Enchantment.LOOTING,
            Enchantment.SWEEPING_EDGE,
            Enchantment.UNBREAKING,
            Enchantment.FORTUNE,
            Enchantment.SILK_TOUCH,
            Enchantment.EFFICIENCY,
            Enchantment.PROTECTION,
            Enchantment.THORNS,
            Enchantment.RESPIRATION
        )
        
        var slot = 10
        for (enchant in enchantments) {
            // Shift to next row if slot goes out of inner bounds
            if (slot % 9 >= 8) {
                slot += 2
            }
            
            val item = createEnchantmentItem(enchant)
            inventory.setItem(slot, item)
            slot++
        }
    }
    
    /**
     * Creates a button item for a specific enchantment
     */
    private fun createEnchantmentItem(enchantment: Enchantment): ItemStack {
        val langManager = LanguageManager.get()
        
        // Choose appropriate material depending on enchantment type
        val material = when {
            enchantment.isTreasure -> Material.NETHER_STAR
            enchantment.key.key.contains("protection") -> Material.DIAMOND_HELMET
            enchantment.key.key.contains("sharpness") -> Material.DIAMOND_SWORD
            enchantment.key.key.contains("efficiency") -> Material.DIAMOND_PICKAXE
            enchantment.key.key.contains("fortune") -> Material.DIAMOND_PICKAXE
            enchantment.key.key.contains("looting") -> Material.DIAMOND_SWORD
            else -> Material.ENCHANTED_BOOK
        }
        
        val item = ItemStack(material)
        val meta = item.itemMeta ?: return item
        
        val enchantName = enchantment.key.key.replace("_", " ")
            .split(" ")
            .joinToString(" ") { word -> word.replaceFirstChar(Char::uppercase) }
        
        meta.displayName(
            twotech.plugin.magicHeroes.util.ColorTranslator.translate("&e&l${enchantName.uppercase()}")
        )
        
        val lore = listOf(
            langManager?.getComponent(player, "gui.enchant-left") ?: Component.empty(),
            langManager?.getComponent(player, "gui.enchant-right") ?: Component.empty(),
            langManager?.getComponent(player, "gui.enchant-id", "enchantment" to enchantment.key.key) ?: Component.empty()
        )
        meta.lore(lore)
        
        // Store enchantment ID inside persistent data container
        meta.persistentDataContainer.set(
            org.bukkit.NamespacedKey(
                org.bukkit.Bukkit.getPluginManager().getPlugin("MagicHeroes")!!,
                "enchantment_name"
            ),
            PersistentDataType.STRING,
            enchantment.key.key
        )
        
        item.itemMeta = meta
        return item
    }
    
    /**
     * Adds the back button (using Arrow)
     */
    private fun addBackButton() {
        val backItem = ItemStack(Material.ARROW)
        val meta = backItem.itemMeta ?: return
        val langManager = LanguageManager.get()
        
        meta.displayName(langManager?.getComponent(player, "gui.back-button") ?: Component.text("Back"))
        val lore = listOf(langManager?.getComponent(player, "gui.back-lore") ?: Component.empty())
        meta.lore(lore)
        
        // Set metadata to identify back button type
        meta.persistentDataContainer.set(
            org.bukkit.NamespacedKey(
                org.bukkit.Bukkit.getPluginManager().getPlugin("MagicHeroes")!!,
                "button_type"
            ),
            PersistentDataType.STRING,
            "back"
        )
        
        backItem.itemMeta = meta
        inventory.setItem(GUI_SIZE - 1, backItem)
    }
    
    override fun getInventory(): Inventory = inventory
}
