package twotech.plugin.magicHeroes.storage

import org.bukkit.configuration.file.YamlConfiguration
import twotech.plugin.magicHeroes.data.HeroPlayerSnapshot
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.UUID

class YamlPlayerStorage(private val dataDirectory: Path) {
    fun load(playerId: UUID, playerName: String): HeroPlayerSnapshot {
        val path = playerPath(playerId)
        if (Files.notExists(path)) return defaultSnapshot(playerId, playerName)
        val configuration = YamlConfiguration.loadConfiguration(path.toFile())
        return HeroPlayerSnapshot(
            playerName = configuration.getString("player.name", playerName) ?: playerName,
            playerUuid = configuration.getString("player.uuid", playerId.toString()) ?: playerId.toString(),
            classId = configuration.getString("class.id")?.takeIf(String::isNotBlank),
            level = configuration.getInt("class.level", 1).coerceAtLeast(1),
            experience = configuration.getInt("class.exp", 0).coerceAtLeast(0),
            attributePoints = configuration.getInt("progression.attribute-points", 0).coerceAtLeast(0),
            skillPoints = configuration.getInt("progression.skill-points", 0).coerceAtLeast(0),
            attributes = configuration.getConfigurationSection("attributes")?.getKeys(false)?.associateWith { key ->
                configuration.getInt("attributes.$key", 0).coerceAtLeast(0)
            }.orEmpty(),
            unlockedSkills = configuration.getStringList("skills.unlocked").toSet(),
            skillBindings = configuration.getConfigurationSection("skills.bindings")?.getKeys(false)?.associate { key ->
                key.toInt() to (configuration.getString("skills.bindings.$key") ?: "")
            }?.filterValues(String::isNotBlank).orEmpty(),
            skillTreeLevels = configuration.getConfigurationSection("skill-tree")?.getKeys(false)?.associateWith { key ->
                configuration.getInt("skill-tree.$key", 0).coerceAtLeast(0)
            }.orEmpty(),
            maxMana = configuration.getDouble("base.maxMana", 100.0).coerceAtLeast(0.0),
            baseDefense = configuration.getDouble("base.defense", 0.0).coerceAtLeast(0.0),
            baseHealthRegen = configuration.getDouble("base.healthRegen", 0.5).coerceAtLeast(0.0),
            baseManaRegen = configuration.getDouble("base.manaRegen", 5.0).coerceAtLeast(0.0),
            currentHealth = if (configuration.contains("current.health")) configuration.getDouble("current.health") else null,
            currentMana = configuration.getDouble("current.mana", 100.0).coerceAtLeast(0.0),
            killCount = configuration.getInt("stats.kills", 0).coerceAtLeast(0),
            deathCount = configuration.getInt("stats.deaths", 0).coerceAtLeast(0),
            schemaVersion = configuration.getInt("schema-version", 0)
        )
    }

    fun save(snapshot: HeroPlayerSnapshot) {
        Files.createDirectories(dataDirectory)
        val target = playerPath(UUID.fromString(snapshot.playerUuid))
        val existing = if (Files.exists(target)) YamlConfiguration.loadConfiguration(target.toFile()) else YamlConfiguration()
        val configuration = existing
        configuration.set("schema-version", HeroPlayerSnapshot.CURRENT_SCHEMA_VERSION)
        configuration.set("player.name", snapshot.playerName)
        configuration.set("player.uuid", snapshot.playerUuid)
        configuration.set("base.maxMana", snapshot.maxMana)
        configuration.set("base.defense", snapshot.baseDefense)
        configuration.set("base.healthRegen", snapshot.baseHealthRegen)
        configuration.set("base.manaRegen", snapshot.baseManaRegen)
        snapshot.currentHealth?.let { configuration.set("current.health", it) }
        configuration.set("current.mana", snapshot.currentMana)
        configuration.set("stats.kills", snapshot.killCount)
        configuration.set("stats.deaths", snapshot.deathCount)
        configuration.set("class.id", snapshot.classId.orEmpty())
        configuration.set("progression.attribute-points", snapshot.attributePoints)
        configuration.set("progression.skill-points", snapshot.skillPoints)
        snapshot.attributes.forEach { (key, value) -> configuration.set("attributes.$key", value) }
        configuration.set("skills.unlocked", snapshot.unlockedSkills.toList())
        snapshot.skillBindings.forEach { (slot, id) -> configuration.set("skills.bindings.$slot", id) }
        snapshot.skillTreeLevels.forEach { (id, level) -> configuration.set("skill-tree.$id", level) }
        configuration.set("class.level", snapshot.level)
        configuration.set("class.exp", snapshot.experience)
        atomicSave(configuration, target)
    }

    private fun atomicSave(configuration: YamlConfiguration, target: Path) {
        val temporary = Files.createTempFile(dataDirectory, target.fileName.toString(), ".tmp")
        try {
            configuration.save(temporary.toFile())
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING)
            }
        } finally {
            Files.deleteIfExists(temporary)
        }
    }

    private fun playerPath(playerId: UUID): Path = dataDirectory.resolve("$playerId.yml")

    private fun defaultSnapshot(playerId: UUID, playerName: String) = HeroPlayerSnapshot(
        playerName = playerName,
        playerUuid = playerId.toString(),
        classId = null,
        level = 1,
        experience = 0,
        attributePoints = 0,
        skillPoints = 0,
        maxMana = 100.0,
        baseDefense = 0.0,
        baseHealthRegen = 0.5,
        baseManaRegen = 5.0,
        currentHealth = null,
        currentMana = 100.0,
        killCount = 0,
        deathCount = 0
    )
}
