package twotech.plugin.magicHeroes.manager

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import twotech.plugin.magicHeroes.util.ColorTranslator
import java.io.File
import java.util.UUID

/**
 * Manages multi-language support and messages in the plugin
 */
class LanguageManager(private val plugin: JavaPlugin) {
    
    private val languages = mutableMapOf<String, YamlConfiguration>()
    private val playerLanguages = mutableMapOf<UUID, String>()
    private var defaultLanguage = "en"
    private var prefix = "" // Holds the prefix loaded from config.yml
    
    companion object {
        private var instance: LanguageManager? = null
        
        fun getInstance(plugin: JavaPlugin): LanguageManager {
            if (instance == null) {
                instance = LanguageManager(plugin)
            }
            return instance!!
        }
        
        fun get(): LanguageManager? = instance
    }
    
    /**
     * Initialize language files and load default configurations
     */
    fun initialize() {
        val langFolder = File(plugin.dataFolder, "lang")
        
        // Create lang folder if not exists
        if (!langFolder.exists()) {
            langFolder.mkdirs()
        }
        
        // Save resource language files
        plugin.saveResource("lang/en.yml", false)
        plugin.saveResource("lang/vie.yml", false)
        plugin.saveResource("lang/gui/en.yml", false)
        plugin.saveResource("lang/gui/vie.yml", false)
        
        loadLanguageFile("en")
        loadLanguageFile("vie")
        
        // Load prefix configurations
        loadConfig()
        
        plugin.logger.info("Language system initialized. Available languages: ${languages.keys}")
    }
    
    /**
     * Load config.yml file to fetch plugin prefix
     */
    private fun loadConfig() {
        val configFile = File(plugin.dataFolder, "config.yml")
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false)
        }
        val config = YamlConfiguration.loadConfiguration(configFile)
        prefix = config.getString("prefix") ?: ""
        plugin.logger.info("Loaded prefix from config.yml")
    }
    
    /**
     * Load a language configuration file by language code, including GUI strings
     */
    private fun loadLanguageFile(code: String) {
        val langFile = File(plugin.dataFolder, "lang/$code.yml")
        
        // Create file if it does not exist
        if (!langFile.exists()) {
            plugin.saveResource("lang/$code.yml", false)
        }
        
        val config = YamlConfiguration.loadConfiguration(langFile)
        
        // Load and merge GUI language file
        val guiFile = File(plugin.dataFolder, "lang/gui/$code.yml")
        if (guiFile.exists()) {
            val guiConfig = YamlConfiguration.loadConfiguration(guiFile)
            // Merge GUI keys under "gui." section
            for (key in guiConfig.getKeys(true)) {
                config.set("gui.$key", guiConfig.get(key))
            }
        }
        
        languages[code] = config
        
        plugin.logger.info("Loaded language file: $code.yml (including GUI strings)")
    }
    
    /**
     * Reload all language files and configuration prefix
     */
    fun reload() {
        languages.clear()
        loadLanguageFile("en")
        loadLanguageFile("vie")
        loadConfig()
        plugin.logger.info("Language files reloaded successfully")
    }
    
    /**
     * Get a formatted message for a player in their language
     */
    fun getMessage(player: Player, key: String, vararg replacements: Pair<String, String>): String {
        val lang = playerLanguages[player.uniqueId] ?: defaultLanguage
        return getMessage(lang, key, *replacements)
    }
    
    /**
     * Get a formatted message by language code
     */
    fun getMessage(lang: String, key: String, vararg replacements: Pair<String, String>): String {
        val config = languages[lang] ?: languages[defaultLanguage] ?: return "Missing: $key"
        var message = config.getString(key) ?: return "Missing: $key"
        
        // Apply replacements
        for ((placeholder, value) in replacements) {
            message = message.replace("{$placeholder}", value)
        }
        
        // Only add prefix for actual chat messages, not display content (GUI, help menu, lore/tooltips)
        val isDisplayContent = key.startsWith("gui.") || 
                              key.startsWith("help.") || 
                              key == "durability.infinite" ||
                              key == "durability.lore-format" ||
                              key == "durability.broken-lore" ||
                              key == "durability.broken-warning"
        
        return if (isDisplayContent) message else prefix + message
    }
    /**
     * Get display text without prefix (for GUI, tooltips, lore, titles)
     */
    fun getDisplayText(player: Player, key: String, vararg replacements: Pair<String, String>): String {
        val lang = playerLanguages[player.uniqueId] ?: defaultLanguage
        return getDisplayText(lang, key, *replacements)
    }
    
    /**
     * Get display text without prefix by language code
     */
    fun getDisplayText(lang: String, key: String, vararg replacements: Pair<String, String>): String {
        val config = languages[lang] ?: languages[defaultLanguage] ?: return "Missing: $key"
        var message = config.getString(key) ?: return "Missing: $key"
        
        // Apply replacements
        for ((placeholder, value) in replacements) {
            message = message.replace("{$placeholder}", value)
        }
        
        // Return without prefix (for display content)
        return message
    }
    
    /**
     * Get message component (colored) for a player
     */
    fun getComponent(player: Player, key: String, vararg replacements: Pair<String, String>): net.kyori.adventure.text.Component {
        val message = getMessage(player, key, *replacements)
        return ColorTranslator.translate(message)
    }
    
    /**
     * Get message component by language code
     */
    fun getComponent(lang: String, key: String, vararg replacements: Pair<String, String>): net.kyori.adventure.text.Component {
        val message = getMessage(lang, key, *replacements)
        return ColorTranslator.translate(message)
    }
    
    /**
     * Set a player's language preference
     */
    fun setPlayerLanguage(player: Player, lang: String): Boolean {
        if (!languages.containsKey(lang)) {
            return false
        }
        playerLanguages[player.uniqueId] = lang
        return true
    }
    
    /**
     * Get a player's language preference
     */
    fun getPlayerLanguage(player: Player): String {
        return playerLanguages[player.uniqueId] ?: defaultLanguage
    }
    
    /**
     * Get list of all available languages
     */
    fun getAvailableLanguages(): List<String> {
        return languages.keys.toList()
    }
}
