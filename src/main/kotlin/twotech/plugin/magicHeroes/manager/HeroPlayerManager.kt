package twotech.plugin.magicHeroes.manager

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import twotech.plugin.magicHeroes.data.HeroPlayerData
import twotech.plugin.magicHeroes.data.HeroPlayerSnapshot
import twotech.plugin.magicHeroes.data.ResourceService
import twotech.plugin.magicHeroes.attribute.AttributeType
import twotech.plugin.magicHeroes.storage.YamlPlayerStorage
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.logging.Level

class HeroPlayerManager(private val plugin: JavaPlugin) {
    private val playerDataCache = ConcurrentHashMap<UUID, HeroPlayerData>()
    private val preloadedProfiles = ConcurrentHashMap<UUID, CompletableFuture<HeroPlayerSnapshot>>()
    private val storage = YamlPlayerStorage(Path.of(plugin.dataFolder.path, "playerdata"))
    private val ioExecutor: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "MagicHeroes-profile-io").apply { isDaemon = true }
    }

    companion object {
        @Volatile
        private var instance: HeroPlayerManager? = null

        fun getInstance(plugin: JavaPlugin): HeroPlayerManager = instance ?: synchronized(this) {
            instance ?: HeroPlayerManager(plugin).also { instance = it }
        }

        fun get(): HeroPlayerManager? = instance
    }

    fun preload(playerId: UUID, playerName: String) {
        preloadedProfiles.computeIfAbsent(playerId) {
            CompletableFuture.supplyAsync({ storage.load(playerId, playerName) }, ioExecutor)
        }
    }

    fun loadPlayerData(player: Player, resources: ResourceService): HeroPlayerData {
        val profileFuture = preloadedProfiles.computeIfAbsent(player.uniqueId) {
            CompletableFuture.supplyAsync({ storage.load(player.uniqueId, player.name) }, ioExecutor)
        }
        val readySnapshot = profileFuture.getNow(null)
        if (readySnapshot != null) return applySnapshot(player, readySnapshot, resources)

        val fallback = createDefaultData(player)
        playerDataCache[player.uniqueId] = fallback
        resources.applyMaxHealth(player, fallback.getTotalMaxHealth())
        profileFuture.whenComplete { snapshot, error ->
            if (error != null) {
                plugin.logger.log(Level.SEVERE, "Could not load profile for ${player.uniqueId}", error)
                return@whenComplete
            }
            Bukkit.getScheduler().runTask(plugin, Runnable {
                if (player.isOnline) applySnapshot(player, snapshot, resources)
            })
        }
        return fallback
    }

    fun getPlayerData(playerUUID: UUID): HeroPlayerData? = playerDataCache[playerUUID]

    fun getOrCreatePlayerData(player: Player): HeroPlayerData = playerDataCache.getOrPut(player.uniqueId) { createDefaultData(player) }

    fun setPlayerData(playerUUID: UUID, data: HeroPlayerData) {
        playerDataCache[playerUUID] = data
    }

    fun removePlayerData(playerUUID: UUID) {
        playerDataCache.remove(playerUUID)
        preloadedProfiles.remove(playerUUID)
    }

    fun savePlayerData(player: Player) {
        val data = playerDataCache[player.uniqueId] ?: return
        saveSnapshot(snapshot(player, data))
    }

    fun saveSnapshot(snapshot: HeroPlayerSnapshot) {
        CompletableFuture.runAsync({ storage.save(snapshot) }, ioExecutor).whenComplete { _, error ->
            if (error != null) plugin.logger.log(Level.SEVERE, "Could not save profile for ${snapshot.playerUuid}", error)
        }
    }

    fun saveAllPlayerData() {
        Bukkit.getOnlinePlayers().forEach(::savePlayerData)
    }

    fun shutdown() {
        ioExecutor.shutdown()
        if (!ioExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
            plugin.logger.warning("Profile saves did not finish within 10 seconds.")
            ioExecutor.shutdownNow()
        }
    }

    private fun applySnapshot(player: Player, snapshot: HeroPlayerSnapshot, resources: ResourceService): HeroPlayerData {
        val configuredClass = snapshot.classId?.let { ClassManager.get()?.getClass(it) }
        val data = HeroPlayerData(
            playerName = player.name,
            playerUUID = player.uniqueId.toString(),
            rpgClass = configuredClass ?: ClassManager.get()?.getDefaultClass(),
            level = snapshot.level,
            exp = snapshot.experience,
            attributePoints = snapshot.attributePoints,
            skillPoints = snapshot.skillPoints,
            maxMana = snapshot.maxMana,
            baseDefense = snapshot.baseDefense,
            baseHealthRegen = snapshot.baseHealthRegen,
            baseManaRegen = snapshot.baseManaRegen,
            killCount = snapshot.killCount,
            deathCount = snapshot.deathCount
        )
        data.attributes.putAll(snapshot.attributes.mapNotNull { (key, value) ->
            runCatching { AttributeType.valueOf(key.uppercase()) to value }.getOrNull()
        })
        data.unlockedSkills += snapshot.unlockedSkills
        data.skillBindings.putAll(snapshot.skillBindings)
        data.skillTreeLevels.putAll(snapshot.skillTreeLevels)
        data.calculateBaseStats()
        data.currentMana = snapshot.currentMana
        playerDataCache[player.uniqueId] = data
        resources.applyLegacyHealth(player, snapshot.currentHealth, data.getTotalMaxHealth())
        return data
    }

    private fun createDefaultData(player: Player): HeroPlayerData {
        val data = HeroPlayerData(player.name, player.uniqueId.toString(), rpgClass = ClassManager.get()?.getDefaultClass())
        data.calculateBaseStats()
        return data
    }

    private fun snapshot(player: Player, data: HeroPlayerData): HeroPlayerSnapshot = HeroPlayerSnapshot(
        playerName = player.name,
        playerUuid = player.uniqueId.toString(),
        classId = data.rpgClass?.id,
        level = data.level,
        experience = data.exp,
        attributePoints = data.attributePoints,
        skillPoints = data.skillPoints,
        attributes = data.attributes.mapKeys { it.key.name.lowercase() },
        unlockedSkills = data.unlockedSkills,
        skillBindings = data.skillBindings,
        skillTreeLevels = data.skillTreeLevels,
        maxMana = data.maxMana,
        baseDefense = data.baseDefense,
        baseHealthRegen = data.baseHealthRegen,
        baseManaRegen = data.baseManaRegen,
        currentHealth = player.health,
        currentMana = data.currentMana,
        killCount = data.killCount,
        deathCount = data.deathCount
    )
}
