package twotech.plugin.magicHeroes.quest

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import twotech.plugin.magicHeroes.item.ItemService
import twotech.plugin.magicHeroes.manager.HeroPlayerManager
import java.nio.file.Path

data class QuestResult(val success: Boolean, val message: String)

class QuestService(private val plugin: JavaPlugin, private val items: ItemService) {
    private val registry = QuestRegistry(Path.of(plugin.dataFolder.path, "quests"))

    fun initialize(): List<String> {
        plugin.saveResource("quests/first-blood.yml", false)
        plugin.saveResource("quests/reach-spawn.yml", false)
        return registry.reload()
    }

    fun reload(): List<String> = registry.reload()
    fun quests(): Collection<Quest> = registry.all()
    fun quest(id: String): Quest? = registry.get(id)

    fun addSimple(id: String, displayName: String, type: QuestObjectiveType, target: String, required: Int, rewardExp: Int): QuestResult {
        val cleanId = id.lowercase().replace(Regex("[^a-z0-9_-]"), "-").trim('-')
        if (cleanId.isBlank()) return QuestResult(false, "Invalid quest id.")
        if (registry.get(cleanId) != null) return QuestResult(false, "Quest already exists.")
        if (required <= 0 || rewardExp < 0) return QuestResult(false, "Invalid quest numbers.")
        if (type == QuestObjectiveType.REACH && ReachTarget.parse(target) == null) return QuestResult(false, "Invalid REACH target. Use world,x,y,z,radius.")
        val file = Path.of(plugin.dataFolder.path, "quests", "$cleanId.yml")
        java.nio.file.Files.createDirectories(file.parent)
        java.nio.file.Files.writeString(file, "id: $cleanId\ndisplay-name: \"$displayName\"\nobjectives:\n  - id: main\n    type: ${type.name}\n    target: $target\n    required: $required\nrewards:\n  exp: $rewardExp\nrepeatable: false\n")
        val errors = reload()
        return if (errors.isEmpty()) QuestResult(true, "Quest $cleanId added.") else QuestResult(false, errors.joinToString(" | "))
    }

    fun remove(id: String): QuestResult {
        val quest = registry.get(id) ?: return QuestResult(false, "Unknown quest: $id")
        val file = Path.of(plugin.dataFolder.path, "quests", "${quest.id}.yml")
        if (!java.nio.file.Files.deleteIfExists(file)) return QuestResult(false, "Quest file not found: ${quest.id}")
        reload()
        return QuestResult(true, "Quest ${quest.id} removed.")
    }

    fun editDisplayName(id: String, displayName: String): QuestResult {
        val quest = registry.get(id) ?: return QuestResult(false, "Unknown quest: $id")
        val file = Path.of(plugin.dataFolder.path, "quests", "${quest.id}.yml")
        if (!java.nio.file.Files.exists(file)) return QuestResult(false, "Quest file not found: ${quest.id}")
        val config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file.toFile())
        config.set("display-name", displayName)
        config.save(file.toFile())
        val errors = reload()
        return if (errors.isEmpty()) QuestResult(true, "Quest ${quest.id} edited.") else QuestResult(false, errors.joinToString(" | "))
    }

    fun progressLines(player: Player): List<String> {
        val data = HeroPlayerManager.get()?.getPlayerData(player.uniqueId) ?: return emptyList()
        return data.questProgress.mapNotNull { (questId, progress) ->
            val quest = registry.get(questId) ?: return@mapNotNull null
            val objectives = quest.objectives.joinToString { objective ->
                "${objective.id} ${progress[objective.id] ?: 0}/${objective.required}"
            }
            "${quest.displayName}: $objectives"
        }
    }

    fun start(player: Player, questId: String): QuestResult {
        val quest = registry.get(questId) ?: return QuestResult(false, "Unknown quest: $questId")
        val data = HeroPlayerManager.get()?.getPlayerData(player.uniqueId) ?: return QuestResult(false, "Profile not loaded.")
        if (!quest.prerequisites.all { it in data.completedQuests }) return QuestResult(false, "Quest prerequisites not met.")
        if (!quest.repeatable && quest.id in data.completedQuests) return QuestResult(false, "Quest already completed.")
        if (quest.id in data.questProgress) return QuestResult(false, "Quest already active.")
        data.questProgress[quest.id] = quest.objectives.associate { it.id to 0 }.toMutableMap()
        return QuestResult(true, "Started ${quest.displayName}.")
    }

    fun progressReach(player: Player, location: Location): List<QuestResult> {
        val world = location.world?.name ?: return emptyList()
        val data = HeroPlayerManager.get()?.getPlayerData(player.uniqueId) ?: return emptyList()
        val results = mutableListOf<QuestResult>()
        data.questProgress.keys.toList().forEach { questId ->
            val quest = registry.get(questId) ?: return@forEach
            val progress = data.questProgress[questId] ?: return@forEach
            quest.objectives.filter { it.type == QuestObjectiveType.REACH }.forEach { objective ->
                val target = ReachTarget.parse(objective.target) ?: return@forEach
                val current = progress[objective.id] ?: 0
                if (current < objective.required && target.contains(world, location.x, location.y, location.z)) {
                    progress[objective.id] = objective.required
                    results += QuestResult(true, "${quest.displayName}: ${objective.id} ${objective.required}/${objective.required}")
                }
            }
            if (quest.objectives.all { (progress[it.id] ?: 0) >= it.required }) results += complete(player, quest)
        }
        return results
    }

    fun progress(player: Player, type: QuestObjectiveType, target: String, amount: Int = 1): List<QuestResult> {
        if (amount <= 0 || type == QuestObjectiveType.REACH) return emptyList()
        val data = HeroPlayerManager.get()?.getPlayerData(player.uniqueId) ?: return emptyList()
        val results = mutableListOf<QuestResult>()
        data.questProgress.keys.toList().forEach { questId ->
            val quest = registry.get(questId) ?: return@forEach
            val progress = data.questProgress[questId] ?: return@forEach
            quest.objectives.filter { it.type == type && it.target.equals(target, true) }.forEach { objective ->
                val current = progress[objective.id] ?: 0
                val next = (current + amount).coerceAtMost(objective.required)
                if (next > current) {
                    progress[objective.id] = next
                    results += QuestResult(true, "${quest.displayName}: ${objective.id} $next/${objective.required}")
                }
            }
            if (quest.objectives.all { (progress[it.id] ?: 0) >= it.required }) results += complete(player, quest)
        }
        return results
    }

    private fun complete(player: Player, quest: Quest): QuestResult {
        val data = HeroPlayerManager.get()?.getPlayerData(player.uniqueId) ?: return QuestResult(false, "Profile not loaded.")
        data.questProgress.remove(quest.id)
        data.completedQuests += quest.id
        data.addExperience(quest.rewardExp)
        quest.rewardItems.forEach { (itemId, amount) -> items.give(player, itemId, amount) }
        return QuestResult(true, "Completed ${quest.displayName}.")
    }
}
