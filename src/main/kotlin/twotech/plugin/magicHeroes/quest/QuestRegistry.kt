package twotech.plugin.magicHeroes.quest

import org.bukkit.configuration.file.YamlConfiguration
import java.nio.file.Files
import java.nio.file.Path

class QuestRegistry(private val directory: Path) {
    @Volatile private var quests: Map<String, Quest> = emptyMap()
    fun get(id: String): Quest? = quests[id.lowercase()]
    fun all(): Collection<Quest> = quests.values

    fun reload(): List<String> {
        Files.createDirectories(directory)
        val loaded = linkedMapOf<String, Quest>()
        val errors = mutableListOf<String>()
        Files.walk(directory).use { paths ->
            paths.filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".yml") }.forEach { path ->
                val config = YamlConfiguration.loadConfiguration(path.toFile())
                val id = config.getString("id")?.lowercase()
                if (id.isNullOrBlank() || id in loaded) {
                    errors += "$path invalid or duplicate id"
                    return@forEach
                }
                val objectives = config.getMapList("objectives").mapNotNull { raw ->
                    val objectiveId = raw["id"]?.toString() ?: return@mapNotNull null
                    val type = runCatching { QuestObjectiveType.valueOf(raw["type"]?.toString()?.uppercase() ?: "") }.getOrNull()
                    val target = raw["target"]?.toString() ?: return@mapNotNull null
                    val required = (raw["required"] as? Number)?.toInt() ?: 0
                    if (type == null || required <= 0 || (type == QuestObjectiveType.REACH && ReachTarget.parse(target) == null)) null else QuestObjective(objectiveId, type, target, required)
                }
                if (objectives.isEmpty()) {
                    errors += "$path has no valid objectives"
                    return@forEach
                }
                val rewards = config.getConfigurationSection("rewards.items")?.getKeys(false)?.associateWith { key ->
                    config.getInt("rewards.items.$key", 0)
                }?.filterValues { it > 0 }.orEmpty()
                loaded[id] = Quest(
                    id,
                    config.getString("display-name", id) ?: id,
                    config.getStringList("prerequisites").map(String::lowercase).toSet(),
                    objectives,
                    config.getInt("rewards.exp", 0).coerceAtLeast(0),
                    rewards,
                    config.getBoolean("repeatable", false)
                )
            }
        }
        if (errors.isEmpty()) quests = loaded.toMap()
        return errors
    }
}
