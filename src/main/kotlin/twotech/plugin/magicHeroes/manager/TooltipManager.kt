package twotech.plugin.magicHeroes.manager

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

/**
 * Manages custom tooltips loaded from tooltips/ folder
 */
data class TooltipConfig(
    val itemGui: String = "name_tag",
    val tooltipName: String? = null
)

class TooltipManager(private val plugin: JavaPlugin) {

    private val formats = HashMap<String, List<String>>()
    private val tooltipConfigs = HashMap<String, TooltipConfig>() // Store item-gui and tooltip-name
    private val defaultTemplateName = "default"

    companion object {
        private var instance: TooltipManager? = null

        fun getInstance(plugin: JavaPlugin): TooltipManager {
            if (instance == null) {
                instance = TooltipManager(plugin)
            }
            return instance!!
        }

        fun get(): TooltipManager? = instance
    }

    /**
     * Initialize and load all tooltip files from tooltips/ directory
     */
    fun initialize() {
        val tooltipFolder = File(plugin.dataFolder, "tooltips")
        if (!tooltipFolder.exists()) {
            tooltipFolder.mkdirs()
        }

        // Create default.yml if it does not exist
        val defaultFile = File(tooltipFolder, "default.yml")
        if (!defaultFile.exists()) {
            plugin.saveResource("tooltips/default.yml", false)
        }

        loadTooltips()
    }

    /**
     * Loads all .yml files in the tooltips/ directory
     */
    fun loadTooltips() {
        formats.clear()
        tooltipConfigs.clear()
        val tooltipFolder = File(plugin.dataFolder, "tooltips")
        val files = tooltipFolder.listFiles { _, name -> name.endsWith(".yml") } ?: return

        for (file in files) {
            val templateName = file.nameWithoutExtension.lowercase()
            val config = YamlConfiguration.loadConfiguration(file)
            val format = config.getStringList("display-format")
            
            if (format.isNotEmpty()) {
                formats[templateName] = format
                
                // Load metadata
                val itemGui = config.getString("item-gui") ?: "name_tag"
                val tooltipName = config.getString("tooltip-name") ?: file.nameWithoutExtension
                tooltipConfigs[templateName] = TooltipConfig(itemGui, tooltipName)
                
                plugin.logger.info("Loaded tooltip template: $templateName")
            }
        }
    }
    fun reload() {
        loadTooltips()
    }

    /**
     * Converts Bukkit legacy color codes (&) into MiniMessage format
     */
    private fun legacyToMiniMessage(text: String): String {
        return text.replace("&0", "<black>")
            .replace("&1", "<dark_blue>")
            .replace("&2", "<dark_green>")
            .replace("&3", "<dark_aqua>")
            .replace("&4", "<dark_red>")
            .replace("&5", "<dark_purple>")
            .replace("&6", "<gold>")
            .replace("&7", "<gray>")
            .replace("&8", "<dark_gray>")
            .replace("&9", "<blue>")
            .replace("&a", "<green>")
            .replace("&b", "<aqua>")
            .replace("&c", "<red>")
            .replace("&d", "<light_purple>")
            .replace("&e", "<yellow>")
            .replace("&f", "<white>")
            .replace("&k", "<obfuscated>")
            .replace("&l", "<bold>")
            .replace("&m", "<strikethrough>")
            .replace("&n", "<underlined>")
            .replace("&o", "<italic>")
            .replace("&r", "<reset>")
    }

    /**
     * Applies a tooltip template format onto an ItemStack lore based on its PDC stats
     */
    fun applyTooltip(player: Player?, item: ItemStack, templateName: String?) {
        val meta = item.itemMeta ?: return
        applyTooltip(player, item, meta, templateName)
        item.itemMeta = meta
    }

    /**
     * Applies a tooltip template format directly to an ItemMeta object
     * This avoids multiple NBT writes and improves performance significantly
     * 
     * @param player The player viewing the item (used for i18n translation)
     * @param item Target ItemStack (used for type checks)
     * @param meta ItemMeta object to apply changes to
     * @param templateName Name of the tooltip template to use (e.g. "default")
     */
    fun applyTooltip(player: Player?, item: ItemStack, meta: ItemMeta, templateName: String?) {
        val pdc = meta.persistentDataContainer
        val templateKey = NamespacedKey(plugin, "mh_tooltip_template")
        
        // Determine which template to use (parameter, cached in PDC, or default)
        val template = templateName?.lowercase() 
            ?: pdc.get(templateKey, PersistentDataType.STRING)?.lowercase()
            ?: defaultTemplateName
            
        // Cache template name in PDC for future updates
        pdc.set(templateKey, PersistentDataType.STRING, template)

        // Hide default Minecraft tooltips (attributes, modifiers, etc.)
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES)
        try {
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.valueOf("HIDE_ADDITIONAL_TOOLTIP"))
        } catch (e: Exception) {
            // Fallback for older Paper/Minecraft versions
        }

        // Retrieve statistics - calculate from player data when available
        var name = pdc.get(NamespacedKey(plugin, "mh_name"), PersistentDataType.STRING) ?: item.type.name
        
        var damage = 0.0
        var defense = 0.0
        var mana = 0.0
        var health = 0.0
        
        // If player exists, calculate stats from player data + equipment
        if (player != null) {
            val playerData = HeroPlayerManager.get()?.getPlayerData(player.uniqueId)
            if (playerData != null) {
                health = playerData.getTotalMaxHealth()
                mana = playerData.getTotalMaxMana()
                defense = playerData.getTotalDefense()
                
                // Calculate damage from weapon in main hand (vanilla attribute)
                val mainHandItem = player.inventory.itemInMainHand
                if (mainHandItem.hasItemMeta()) {
                    val weaponMeta = mainHandItem.itemMeta
                    val attributes = weaponMeta?.attributeModifiers
                    if (attributes != null && attributes.size > 0) {
                        // Get generic.attackDamage attribute
                        val attackDamage = attributes.get(org.bukkit.attribute.Attribute.GENERIC_ATTACK_DAMAGE)
                        if (attackDamage != null && attackDamage.size > 0) {
                            damage = attackDamage[0].amount
                        }
                    }
                }
            }
        }
        
        // Fallback to PDC if player unavailable
        if (player == null) {
            damage = pdc.get(NamespacedKey(plugin, "mh_damage"), PersistentDataType.DOUBLE) ?: 0.0
            defense = pdc.get(NamespacedKey(plugin, "mh_defense"), PersistentDataType.DOUBLE) ?: 0.0
            mana = pdc.get(NamespacedKey(plugin, "mh_mana"), PersistentDataType.DOUBLE) ?: 0.0
            health = pdc.get(NamespacedKey(plugin, "mh_health"), PersistentDataType.DOUBLE) ?: 0.0
        }
        
        val levelReq = pdc.get(NamespacedKey(plugin, "mh_level_req"), PersistentDataType.INTEGER) ?: 0
        val classReq = pdc.get(NamespacedKey(plugin, "mh_class_req"), PersistentDataType.STRING) ?: "None"

        // Fetch format list from template name
        val formatLines = formats[template] ?: formats[defaultTemplateName] ?: return

        // Build durability display string
        var durabilityStr = ""
        val langManager = LanguageManager.get()
        val keyDurability = NamespacedKey(plugin, "mh_durability")
        val keyMaxDurability = NamespacedKey(plugin, "mh_max_durability")
        
        if (pdc.has(keyMaxDurability, PersistentDataType.INTEGER)) {
            val maxDur = pdc.get(keyMaxDurability, PersistentDataType.INTEGER) ?: 100
            val curDur = pdc.get(keyDurability, PersistentDataType.INTEGER) ?: 100
            
            val lang = player?.let { langManager?.getPlayerLanguage(it) } ?: "en"
            
            if (maxDur == -1) {
                // Infinite durability
                val infiniteText = langManager?.getDisplayText(lang, "durability.infinite") ?: "&b&lInfinite"
                durabilityStr = legacyToMiniMessage(infiniteText)
            } else if (curDur <= 0) {
                // Broken item
                val brokenText = langManager?.getDisplayText(lang, "durability.broken-lore") ?: "&c&l[BROKEN]"
                durabilityStr = legacyToMiniMessage(brokenText)
                
                // Add broken indicator to the item name display as well
                name = "$brokenText &r$name"
            } else {
                // Normal durability
                val rawFormat = langManager?.getDisplayText(lang, "durability.lore-format") ?: "&7Durability: &f{current}/{max}"
                val formatted = rawFormat
                    .replace("{current}", curDur.toString())
                    .replace("{max}", maxDur.toString())
                durabilityStr = legacyToMiniMessage(formatted)
            }
        }

        // Calculate attack speed (default weapon is 1.6)
        val attackSpeed = 1.6
        
        val mm = MiniMessage.miniMessage()
        val loreComponents = mutableListOf<Component>()

        // Process placeholders and build lore components
        for (line in formatLines) {
            val formattedLine = line
                .replace("%name%", name)
                .replace("%damage%", String.format("%.0f", damage))
                .replace("%defense%", String.format("%.0f", defense))
                .replace("%mana%", String.format("%.0f", mana))
                .replace("%health%", String.format("%.0f", health))
                .replace("%level_req%", levelReq.toString())
                .replace("%class_req%", classReq)
                .replace("%durability%", durabilityStr)
                .replace("%attack_speed%", String.format("%.2f", attackSpeed))
            // Convert MiniMessage format into Component
            val component = mm.deserialize(formattedLine)
            loreComponents.add(component)
        }
        // Append custom lore if present in PDC
        val customLoreKey = NamespacedKey(plugin, "mh_custom_lore")
        if (pdc.has(customLoreKey, PersistentDataType.STRING)) {
            val customLoreText = pdc.get(customLoreKey, PersistentDataType.STRING)
            if (!customLoreText.isNullOrEmpty()) {
                val customLines = customLoreText.split("|")
                for (line in customLines) {
                    loreComponents.add(twotech.plugin.magicHeroes.util.ColorTranslator.translate(line.trim()))
                }
            }
        }

        // Apply updated lore to item metadata
        meta.lore(loreComponents)
    }
    
    /**
     * Gets all available tooltip names
     */
    fun getAvailableTooltips(): List<String> {
        return formats.keys.toList()
    }

    /**
     * Gets metadata for a tooltip by name
     */
    fun getTooltipConfig(name: String): TooltipConfig? {
        return tooltipConfigs[name.lowercase()]
    }

    /**
     * Sets cached template name and applies tooltip directly
     */
    fun applyTooltipByName(player: Player?, item: ItemStack, templateName: String) {
        val meta = item.itemMeta ?: return
        applyTooltip(player, item, meta, templateName)
        item.itemMeta = meta
    }

    /**
     * Gets currently cached tooltip name of the item
     */
    fun getCurrentTooltipName(item: ItemStack): String? {
        val meta = item.itemMeta ?: return null
        val pdc = meta.persistentDataContainer
        return pdc.get(NamespacedKey(plugin, "mh_tooltip_template"), PersistentDataType.STRING)
    }
}
