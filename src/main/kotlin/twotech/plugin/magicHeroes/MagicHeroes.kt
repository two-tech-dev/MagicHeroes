package twotech.plugin.magicHeroes

import org.bukkit.plugin.java.JavaPlugin
import twotech.plugin.magicHeroes.api.MagicHeroesApi
import twotech.plugin.magicHeroes.api.MagicHeroesApiImpl
import twotech.plugin.magicHeroes.attribute.AttributeService
import twotech.plugin.magicHeroes.calculator.StatCalculator
import twotech.plugin.magicHeroes.combat.CombatService
import twotech.plugin.magicHeroes.command.MagicHeroesCommand
import twotech.plugin.magicHeroes.data.ResourceService
import twotech.plugin.magicHeroes.integration.Integration
import twotech.plugin.magicHeroes.integration.IntegrationService
import twotech.plugin.magicHeroes.integration.MagicHeroesPlaceholderExpansion
import twotech.plugin.magicHeroes.item.ItemService
import twotech.plugin.magicHeroes.listener.ChatInputListener
import twotech.plugin.magicHeroes.listener.CombatLifecycleListener
import twotech.plugin.magicHeroes.listener.DamageEventListener
import twotech.plugin.magicHeroes.listener.DurabilityEventListener
import twotech.plugin.magicHeroes.listener.EquipmentEventListener
import twotech.plugin.magicHeroes.listener.GUIClickListener
import twotech.plugin.magicHeroes.listener.PlayerEventListener
import twotech.plugin.magicHeroes.listener.QuestListener
import twotech.plugin.magicHeroes.manager.ClassManager
import twotech.plugin.magicHeroes.manager.DurabilityManager
import twotech.plugin.magicHeroes.manager.HeroPlayerManager
import twotech.plugin.magicHeroes.manager.LanguageManager
import twotech.plugin.magicHeroes.manager.TooltipManager
import twotech.plugin.magicHeroes.migration.MigrationService
import twotech.plugin.magicHeroes.party.PartyService
import twotech.plugin.magicHeroes.quest.QuestService
import twotech.plugin.magicHeroes.skill.SkillService
import twotech.plugin.magicHeroes.stat.StatCalculationService
import twotech.plugin.magicHeroes.task.ActionbarTask
import twotech.plugin.magicHeroes.util.PdcKeys
import twotech.plugin.magicHeroes.waypoint.WaypointService

class MagicHeroes : JavaPlugin() {
    private lateinit var resources: ResourceService
    private lateinit var actionbarTask: ActionbarTask
    private lateinit var combatService: CombatService
    private lateinit var attributeService: AttributeService
    private lateinit var integrations: IntegrationService

    lateinit var itemService: ItemService
        private set
    lateinit var statCalculationService: StatCalculationService
        private set
    lateinit var skillService: SkillService
        private set
    lateinit var questService: QuestService
        private set
    lateinit var partyService: PartyService
        private set
    lateinit var waypointService: WaypointService
        private set
    lateinit var api: MagicHeroesApi
        private set

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
        questService = QuestService(this, itemService)
        partyService = PartyService(config.getInt("party.max-size", 5))
        waypointService = WaypointService(this)
        integrations = IntegrationService(server.pluginManager)
        if (integrations.isAvailable(Integration.PLACEHOLDER_API)) MagicHeroesPlaceholderExpansion(this).register()
        api = MagicHeroesApiImpl(this, itemService, combatService, skillService, twotech.plugin.magicHeroes.resource.ResourceService())

        LanguageManager.getInstance(this).initialize()
        ClassManager.getInstance(this).initialize()
        HeroPlayerManager.getInstance(this)
        TooltipManager.getInstance(this).initialize()
        DurabilityManager.getInstance(this)

        val errors = itemService.initialize() + skillService.initialize() + questService.initialize() + waypointService.initialize()
        if (errors.isNotEmpty()) logger.severe("Registry templates rejected: ${errors.joinToString("; ")}")
        logger.info("Optional integrations: ${integrations.available().joinToString().ifBlank { "none" }}")

        val commandHandler = MagicHeroesCommand(this, resources, attributeService, skillService, questService, partyService, waypointService)
        getCommand("magicheroes")?.also { command ->
            command.setExecutor(commandHandler)
            command.tabCompleter = commandHandler
        } ?: error("magicheroes command missing from plugin.yml")

        server.pluginManager.registerEvents(GUIClickListener(), this)
        server.pluginManager.registerEvents(ChatInputListener(this), this)
        server.pluginManager.registerEvents(PlayerEventListener(this, resources, skillService, partyService, waypointService), this)
        server.pluginManager.registerEvents(EquipmentEventListener(this, resources, itemService.requirements, itemService.advance), this)
        server.pluginManager.registerEvents(DamageEventListener(combatService), this)
        server.pluginManager.registerEvents(CombatLifecycleListener(), this)
        server.pluginManager.registerEvents(QuestListener(questService, partyService), this)
        server.pluginManager.registerEvents(DurabilityEventListener(this), this)

        actionbarTask = ActionbarTask(this, resources)
        actionbarTask.start()
        server.onlinePlayers.forEach { player ->
            val data = HeroPlayerManager.getInstance(this).loadPlayerData(player, resources)
            StatCalculator.updateEquipmentStats(player, data)
            resources.applyMaxHealth(player, data.getTotalMaxHealth())
            waypointService.restore(player, data.discoveredWaypoints)
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
        partyService.configure(config.getInt("party.max-size", 5))
        ClassManager.get()?.loadClasses()
        LanguageManager.get()?.reload()
        TooltipManager.get()?.reload()
        val errors = itemService.reload() + skillService.reload() + questService.reload() + waypointService.reload()
        if (errors.isNotEmpty()) {
            logger.severe("Reload rejected: ${errors.joinToString("; ")}")
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
