package twotech.plugin.magicHeroes.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.entity.Player
import org.bukkit.enchantments.Enchantment
import org.bukkit.persistence.PersistentDataType
import org.bukkit.inventory.ItemStack
import twotech.plugin.magicHeroes.gui.ItemEditorGUI
import twotech.plugin.magicHeroes.gui.EnchantmentGUI
import twotech.plugin.magicHeroes.gui.DurabilityGUI
import twotech.plugin.magicHeroes.gui.TooltipGUI
import twotech.plugin.magicHeroes.manager.DurabilityManager
import twotech.plugin.magicHeroes.manager.LanguageManager
import twotech.plugin.magicHeroes.manager.TooltipManager
import twotech.plugin.magicHeroes.util.ItemUtils

/**
 * Listener to handle click events inside item editor GUIs
 */
class GUIClickListener : Listener {
    
    companion object {
        // Cache to store player input state (playerUUID -> input_type)
        private val playerInputState = ConcurrentHashMap<String, String>()
        
        fun setInputState(playerUUID: String, state: String) {
            playerInputState[playerUUID] = state
        }
        
        fun getInputState(playerUUID: String): String? = playerInputState[playerUUID]
        
        fun clearInputState(playerUUID: String) {
            playerInputState.remove(playerUUID)
        }
    }
    
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val topInventory = event.view.topInventory
        val isEditor = topInventory.holder is ItemEditorGUI ||
            topInventory.holder is EnchantmentGUI ||
            topInventory.holder is DurabilityGUI ||
            topInventory.holder is TooltipGUI
        if (!isEditor) return

        event.isCancelled = true
        if (event.rawSlot !in 0 until topInventory.size || isUnsafeClick(event)) return
        when (topInventory.holder) {
            is ItemEditorGUI -> handleMainGUIClick(event, player)
            is EnchantmentGUI -> handleEnchantmentGUIClick(event, player)
            is DurabilityGUI -> handleDurabilityGUIClick(event, player)
            is TooltipGUI -> handleTooltipGUIClick(event, player)
        }
    }
    
    @EventHandler
    fun onInventoryDrag(event: InventoryDragEvent) {
        if (event.inventory.holder is ItemEditorGUI ||
            event.inventory.holder is EnchantmentGUI ||
            event.inventory.holder is DurabilityGUI ||
            event.inventory.holder is TooltipGUI
        ) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        (event.player as? Player)?.let { clearInputState(it.uniqueId.toString()) }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        clearInputState(event.player.uniqueId.toString())
    }

    /**
     * Handles clicks on the main editor GUI (ItemEditorGUI)
     */

    private fun isUnsafeClick(event: InventoryClickEvent): Boolean =
        event.click == org.bukkit.event.inventory.ClickType.SHIFT_LEFT ||
            event.click == org.bukkit.event.inventory.ClickType.SHIFT_RIGHT ||
            event.click == org.bukkit.event.inventory.ClickType.NUMBER_KEY ||
            event.click == org.bukkit.event.inventory.ClickType.DOUBLE_CLICK

    /**
     * Handles clicks on the main editor GUI (ItemEditorGUI)
     */
    private fun handleMainGUIClick(event: InventoryClickEvent, player: Player) {
        val clickedItem = event.currentItem ?: return
        val meta = clickedItem.itemMeta ?: return
        val langManager = LanguageManager.get()
        
        // Get button type from PersistentDataContainer
        val buttonType = meta.persistentDataContainer.get(
            org.bukkit.NamespacedKey(
                org.bukkit.Bukkit.getPluginManager().getPlugin("MagicHeroes")!!,
                "button_type"
            ),
            PersistentDataType.STRING
        ) ?: return
        
        when (buttonType) {
            "rename" -> {
                player.closeInventory()
                setInputState(player.uniqueId.toString(), "rename")
                player.sendMessage(langManager?.getComponent(player, "input.rename-prompt") ?: return)
                player.sendMessage(langManager.getComponent(player, "input.rename-hint"))
            }
            "lore" -> {
                player.closeInventory()
                setInputState(player.uniqueId.toString(), "lore")
                player.sendMessage(langManager?.getComponent(player, "input.lore-prompt") ?: return)
            }
            "enchant" -> {
                player.closeInventory()
                EnchantmentGUI(player).openGUI()
            }
            "durability" -> {
                player.closeInventory()
                val item = player.inventory.itemInMainHand
                if (item.type.isAir) {
                    player.sendMessage(langManager?.getComponent(player, "gui.no-item-error") ?: return)
                    return
                }
                DurabilityGUI(player, item).openGUI()
            }
            "tooltip" -> {
                player.closeInventory()
                val item = player.inventory.itemInMainHand
                if (item.type.isAir) {
                    player.sendMessage(langManager?.getComponent(player, "gui.no-item-error") ?: return)
                    return
                }
                TooltipGUI(player).openGUI()
            }
        }
    }
    
    /**
     * Handles clicks on the enchantment manager GUI (EnchantmentGUI)
     */
    private fun handleEnchantmentGUIClick(event: InventoryClickEvent, player: Player) {
        val clickedItem = event.currentItem ?: return
        val meta = clickedItem.itemMeta ?: return
        val playerPlugin = org.bukkit.Bukkit.getPluginManager().getPlugin("MagicHeroes") ?: return
        val langManager = LanguageManager.get()
        
        // Check if back button clicked
        val buttonType = meta.persistentDataContainer.get(
            org.bukkit.NamespacedKey(playerPlugin, "button_type"),
            PersistentDataType.STRING
        )
        
        if (buttonType == "back") {
            player.closeInventory()
            ItemEditorGUI(player).openGUI()
            return
        }
        
        // Get enchantment name from item PDC
        val enchantmentName = meta.persistentDataContainer.get(
            org.bukkit.NamespacedKey(playerPlugin, "enchantment_name"),
            PersistentDataType.STRING
        ) ?: return
        
        // Retrieve Enchantment object
        val enchantment = findEnchantment(enchantmentName) ?: run {
            player.sendMessage("Enchantment not found!")
            return
        }
        
        // Ensure player is still holding the item
        val item = player.inventory.itemInMainHand
        if (item.type.isAir) {
            player.sendMessage(langManager?.getComponent(player, "gui.no-item-dropped") ?: return)
            player.closeInventory()
            return
        }
        
        when (event.click) {
            org.bukkit.event.inventory.ClickType.LEFT -> {
                // Left click: Add enchantment level
                addEnchantment(player, item, enchantment, langManager)
            }
            org.bukkit.event.inventory.ClickType.RIGHT -> {
                // Right click: Remove enchantment
                removeEnchantment(player, item, enchantment, langManager)
            }
            else -> {}
        }
        
        // Keep the GUI open after action
        player.openInventory(EnchantmentGUI(player).inventory)
    }
    
    /**
     * Adds an enchantment to the item
     */
    private fun addEnchantment(player: Player, item: ItemStack, enchantment: Enchantment, langManager: LanguageManager?) {
        val currentLevel = item.getEnchantmentLevel(enchantment)
        val newLevel = currentLevel + 1
        
        item.addUnsafeEnchantment(enchantment, newLevel)
        ItemUtils.resetAndUpdateTooltip(player, item)
        player.sendMessage(
            langManager?.getComponent(
                player,
                "gui.enchant-added",
                "enchantment" to enchantment.key.key,
                "level" to newLevel.toString()
            ) ?: return
        )
    }
    
    /**
     * Removes an enchantment from the item
     */
    private fun removeEnchantment(player: Player, item: ItemStack, enchantment: Enchantment, langManager: LanguageManager?) {
        if (!item.containsEnchantment(enchantment)) {
            player.sendMessage(
                langManager?.getComponent(player, "gui.enchant-not-exist", "enchantment" to enchantment.key.key) ?: return
            )
            return
        }
        
        item.removeEnchantment(enchantment)
        ItemUtils.resetAndUpdateTooltip(player, item)
        player.sendMessage(
            langManager?.getComponent(player, "gui.enchant-removed", "enchantment" to enchantment.key.key) ?: return
        )
    }
    
    /**
     * Helper to find Enchantment by name or key namespace
     */
    private fun findEnchantment(name: String): Enchantment? {
        val enchant = Enchantment.getByName(name.uppercase())
        if (enchant != null) return enchant
        
        return Enchantment.values().firstOrNull { 
            it.key.key.equals(name, ignoreCase = true) 
        }
    }
    /**
     * Handles clicks on the durability GUI (DurabilityGUI)
     */
    private fun handleDurabilityGUIClick(event: InventoryClickEvent, player: Player) {
        val clickedItem = event.currentItem ?: return
        val meta = clickedItem.itemMeta ?: return
        val langManager = LanguageManager.get()
        
        val buttonType = meta.persistentDataContainer.get(
            org.bukkit.NamespacedKey(
                org.bukkit.Bukkit.getPluginManager().getPlugin("MagicHeroes")!!,
                "button_type"
            ),
            PersistentDataType.STRING
        ) ?: return
        
        val item = player.inventory.itemInMainHand
        val durabilityManager = DurabilityManager.get() ?: return
        when (buttonType) {
            "durability_set" -> {
                player.closeInventory()
                setInputState(player.uniqueId.toString(), "durability_set")
                player.sendMessage(langManager?.getComponent(player, "durability.set-usage") ?: return)
            }
            "durability_infinite" -> {
                durabilityManager.setInfiniteDurability(item)
                ItemUtils.resetAndUpdateTooltip(player, item)
                player.sendMessage(langManager?.getComponent(player, "durability.infinite-success") ?: return)
            }
            "durability_check" -> {
                if (durabilityManager.hasDurability(item)) {
                    if (durabilityManager.isInfinite(item)) {
                        player.sendMessage(langManager?.getComponent(player, "durability.check-infinite") ?: return)
                    } else {
                        val current = durabilityManager.getCurrentDurability(item)
                        val max = durabilityManager.getMaxDurability(item)
                        player.sendMessage(langManager?.getComponent(player, "durability.check", "current" to current.toString(), "max" to max.toString()) ?: return)
                    }
                } else {
                    player.sendMessage(langManager?.getComponent(player, "durability.no-durability") ?: return)
                }
            }
            "durability_reset" -> {
                durabilityManager.resetDurability(item)
                ItemUtils.resetAndUpdateTooltip(player, item)
                player.sendMessage(langManager?.getComponent(player, "durability.reset-success") ?: return)
            }
            "back" -> {
                player.closeInventory()
                ItemEditorGUI(player).openGUI()
            }
        }
    }
    /**
     * Handles clicks on the tooltip style selector GUI (TooltipGUI)
     */
    private fun handleTooltipGUIClick(event: InventoryClickEvent, player: Player) {
        val clickedItem = event.currentItem ?: return
        val meta = clickedItem.itemMeta ?: return
        val langManager = LanguageManager.get()
        
        val buttonType = meta.persistentDataContainer.get(
            org.bukkit.NamespacedKey(
                org.bukkit.Bukkit.getPluginManager().getPlugin("MagicHeroes")!!,
                "button_type"
            ),
            PersistentDataType.STRING
        ) ?: return
        
        val item = player.inventory.itemInMainHand
        val tooltipManager = TooltipManager.get()
        
        when (buttonType) {
            "tooltip_select" -> {
                val template = meta.persistentDataContainer.get(
                    org.bukkit.NamespacedKey(
                        org.bukkit.Bukkit.getPluginManager().getPlugin("MagicHeroes")!!,
                        "tooltip_template"
                    ),
                    PersistentDataType.STRING
                ) ?: return
                
                if (tooltipManager != null) {
                    tooltipManager.applyTooltipByName(player, item, template)
                    player.sendMessage(langManager?.getComponent(player, "tooltip.set-success", "template" to template) ?: return)
                }
                player.closeInventory()
            }
            "back" -> {
                player.closeInventory()
                ItemEditorGUI(player).openGUI()
            }
        }
    }
    
}
