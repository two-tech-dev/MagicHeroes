package twotech.plugin.magicHeroes

import org.bukkit.plugin.java.JavaPlugin
import twotech.plugin.magicHeroes.attribute.AttributeService
import twotech.plugin.magicHeroes.calculator.StatCalculator
import twotech.plugin.magicHeroes.combat.CombatService
import twotech.plugin.magicHeroes.command.MagicHeroesCommand
import twotech.plugin.magicHeroes.data.ResourceService
import twotech.plugin.magicHeroes.item.ItemService
import twotech.plugin.magicHeroes.listener.ChatInputListener
import twotech.plugin.magicHeroes.listener.CombatLifecycleListener
import twotech.plugin.magicHeroes.listener.DamageEventListener
import twotech.plugin.magicHeroes.listener.DurabilityEventListener
import twotech.plugin.magicHeroes.listener.EquipmentEventListener
import twotech.plugin.magicHeroes.listener.GUIClickListener
import twotech.plugin.magicHeroes.listener.PlayerEventListener
import twotech.plugin.magicHeroes.manager.ClassManager
import twotech.plugin.magicHeroes.manager.DurabilityManager
import twotech.plugin.magicHeroes.manager.HeroPlayerManager
import twotech.plugin.magicHeroes.manager.LanguageManager
import twotech.plugin.magicHeroes.manager.TooltipManager
import twotech.plugin.magicHeroes.migration.MigrationService
import twotech.plugin.magicHeroes.skill.SkillService
import twotech.plugin.magicHeroes.stat.StatCalculationService
import twotech.plugin.magicHeroes.task.ActionbarTask
import twotech.plugin.magicHeroes.util.PdcKeys

class MagicHeroes : JavaPlugin() {
    private lateinit var resources: ResourceService
    private lateinit var actionbarTask: ActionbarTask
    lateinit var itemService: ItemService
        private set
    lateinit var statCalculationService: StatCalculationService
        private set
    lateinit var skillService: SkillService
        private set
    private lateinit var combatService: CombatService
    private lateinit var attributeService: AttributeService

    override fun onEnable() {
        saveDefaultConfig()
        MigrationService(this).migrateLegacyFiles()
        resources = ResourceService()
        StatCalculator.initialize(PdcKeys(this))
        itemService = ItemService(this)
        statCalculationService = StatCalculationService(this)
        combatService = CombatService(statCalculationService, itemService)
        attributeService = AttributeService()
        skillService = SkillService(this, combatService)

        LanguageManager.getInstance(this).initialize()
        ClassManager.getInstance(this).initialize()
        HeroPlayerManager.getInstance(this)
        TooltipManager.getInstance(this).initialize()
        DurabilityManager.getInstance(this)

        val itemErrors = itemService.initialize()
        if (itemErrors.isNotEmpty()) logger.severe("Item templates rejected: ${itemErrors.joinToString("; ")}")
        val skillErrors = skillService.initialize()
        if (skillErrors.isNotEmpty()) logger.severe("Skill templates rejected: ${skillErrors.joinToString("; ")}")

        val commandHandler = MagicHeroesCommand(this, resources, attributeService, skillService)
        getCommand("magicheroes")?.also { command ->
            command.setExecutor(commandHandler)
            command.tabCompleter = commandHandler
        } ?: error("magicheroes command missing from plugin.yml")

        server.pluginManager.registerEvents(GUIClickListener(), this)
        server.pluginManager.registerEvents(ChatInputListener(this), this)
        server.pluginManager.registerEvents(PlayerEventListener(this, resources, skillService), this)
        server.pluginManager.registerEvents(EquipmentEventListener(this, resources, itemService.requirements, itemService.advance), this)
        server.pluginManager.registerEvents(DamageEventListener(combatService), this)
        server.pluginManager.registerEvents(CombatLifecycleListener(), this)
        server.pluginManager.registerEvents(DurabilityEventListener(this), this)

        actionbarTask = ActionbarTask(this, resources)
        actionbarTask.start()
        server.onlinePlayers.forEach { player ->
            val data = HeroPlayerManager.getInstance(this).loadPlayerData(player, resources)
            StatCalculator.updateEquipmentStats(player, data)
            resources.applyMaxHealth(player, data.getTotalMaxHealth())
        }
        logger.info("MagicHeroes enabled: ${pluginMeta.version}")
    }

    override fun onDisable() {
        if (::actionbarTask.isInitialized) actionbarTask.stop()
        HeroPlayerManager.get()?.let { manager ->
            manager.saveAllPlayerData()
            manager.shutdown()
        }
        logger.info("MagicHeroes disabled.")
    }

    fun reloadPlugin(): Boolean = try {
        reloadConfig()
        ClassManager.get()?.loadClasses()
        LanguageManager.get()?.reload()
        TooltipManager.get()?.reload()
        val itemErrors = itemService.reload()
        if (itemErrors.isNotEmpty()) {
            logger.severe("Item reload rejected: ${itemErrors.joinToString("; ")}")
            return false
        }
        val skillErrors = skillService.reload()
        if (skillErrors.isNotEmpty()) {
            logger.severe("Skill reload rejected: ${skillErrors.joinToString("; ")}")
            return false
        }
        actionbarTask.restart()
        server.onlinePlayers.forEach { player ->
            HeroPlayerManager.get()?.getPlayerData(player.uniqueId)?.let { data ->
                StatCalculator.updateEquipmentStats(player, data)
                resources.applyMaxHealth(player, data.getTotalMaxHealth())
            }
        }
        true
    } catch (exception: Exception) {
        logger.severe("Reload rejected: ${exception.message ?: exception.javaClass.simpleName}")
        false
    }
}
