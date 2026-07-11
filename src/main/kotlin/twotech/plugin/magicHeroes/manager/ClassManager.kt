package twotech.plugin.magicHeroes.manager

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import twotech.plugin.magicHeroes.rpg.RPGClass
import java.io.File

/**
 * Manages RPG Classes loaded from classes.yml
 */
class ClassManager(private val plugin: JavaPlugin) {
    
    private val classes = HashMap<String, RPGClass>()
    
    companion object {
        private var instance: ClassManager? = null
        
        fun getInstance(plugin: JavaPlugin): ClassManager {
            if (instance == null) {
                instance = ClassManager(plugin)
            }
            return instance!!
        }
        
        fun get(): ClassManager? = instance
    }
    
    /**
     * Initialize and load all classes from classes.yml
     */
    fun initialize() {
        val classFile = File(plugin.dataFolder, "classes.yml")
        
        // Create default classes.yml if not exists
        if (!classFile.exists()) {
            plugin.saveResource("classes.yml", false)
        }
        
        loadClasses()
    }
    
    /**
     * Load all classes from yml file into HashMap
     */
    fun loadClasses() {
        classes.clear()
        
        val classFile = File(plugin.dataFolder, "classes.yml")
        val config = YamlConfiguration.loadConfiguration(classFile)
        
        for (classId in config.getKeys(false)) {
            val section = config.getConfigurationSection(classId) ?: continue
            
            val displayName = section.getString("display-name", classId) ?: classId
            val baseHp = section.getDouble("base-hp", 100.0)
            val hpPerLevel = section.getDouble("hp-per-level", 10.0)
            val baseMana = section.getDouble("base-mana", 100.0)
            val manaPerLevel = section.getDouble("mana-per-level", 10.0)
            val baseDefense = section.getDouble("base-defense", 0.0)
            val defensePerLevel = section.getDouble("defense-per-level", 0.0)
            
            val rpgClass = RPGClass(
                id = classId,
                displayName = displayName,
                baseHealth = baseHp,
                baseMana = baseMana,
                baseDefense = baseDefense,
                healthPerLevel = hpPerLevel,
                manaPerLevel = manaPerLevel,
                defensePerLevel = defensePerLevel
            )
            
            classes[classId.lowercase()] = rpgClass
        }
        
        plugin.logger.info("Loaded ${classes.size} RPG classes from classes.yml")
    }
    
    /**
     * Get RPGClass by ID
     */
    fun getClass(id: String): RPGClass? {
        return classes[id.lowercase()]
    }
    
    /**
     * Get list of all available Class IDs
     */
    fun getAvailableClassIds(): Set<String> {
        return classes.keys
    }
    
    /**
     * Get default class (e.g. warrior)
     */
    fun getDefaultClass(): RPGClass {
        return classes.values.firstOrNull() ?: RPGClass(
            id = "default",
            displayName = "Default",
            baseHealth = 100.0,
            baseMana = 100.0,
            baseDefense = 0.0,
            healthPerLevel = 10.0,
            manaPerLevel = 10.0,
            defensePerLevel = 0.0
        )
    }
}
