package twotech.plugin.magicHeroes.gui

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import twotech.plugin.magicHeroes.manager.LanguageManager
import twotech.plugin.magicHeroes.manager.TooltipManager
import net.kyori.adventure.text.Component

/**
 * GUI to select and set a tooltip style for the item in hand
 */
class TooltipGUI(private val player: Player) : InventoryHolder {

    companion object {
        const val GUI_SIZE = 27
        const val BACK_SLOT = 26
    }

    private lateinit var inventory: Inventory
    private val tooltipManager = TooltipManager.get()

    /**
     * Create and open the Tooltip selector GUI
     */
    fun openGUI() {
        val langManager = LanguageManager.get()
        val title = langManager?.getComponent(player, "gui.tooltip-title") ?: Component.text("Tooltip Styles")

        inventory = Bukkit.createInventory(this, GUI_SIZE, title)

        // Fill background
        fillBackground()

        // Add tooltip buttons
        addTooltipButtons()

        // Add back button
        addBackButton()

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
            if (i != BACK_SLOT) {
                inventory.setItem(i, glassPane)
            }
        }
    }

    /**
     * Adds buttons for each loaded tooltip template
     */
    private fun addTooltipButtons() {
        if (tooltipManager == null) return

        val templates = tooltipManager.getAvailableTooltips()
        var slot = 10 // Start at slot 10 (center-left)

        for (template in templates) {
            if (slot >= BACK_SLOT) break // Avoid overflowing GUI size

            // Get metadata or defaults
            val config = tooltipManager.getTooltipConfig(template)
            val materialName = config?.itemGui?.uppercase() ?: "NAME_TAG"
            val material = Material.matchMaterial(materialName) ?: Material.NAME_TAG
            val displayNameText = config?.tooltipName ?: template

            val item = ItemStack(material)
            val meta = item.itemMeta ?: continue

            // Colorize tooltip style name
            meta.displayName(Component.text("§a§l$displayNameText"))
            
            // Set metadata to identify click action
            meta.persistentDataContainer.set(
                org.bukkit.NamespacedKey(org.bukkit.Bukkit.getPluginManager().getPlugin("MagicHeroes")!!, "button_type"),
                PersistentDataType.STRING,
                "tooltip_select"
            )
            meta.persistentDataContainer.set(
                org.bukkit.NamespacedKey(org.bukkit.Bukkit.getPluginManager().getPlugin("MagicHeroes")!!, "tooltip_template"),
                PersistentDataType.STRING,
                template
            )
            item.itemMeta = meta

            inventory.setItem(slot, item)
            slot++
        }
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
