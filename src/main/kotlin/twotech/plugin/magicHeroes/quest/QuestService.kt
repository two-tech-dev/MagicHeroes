package twotech.plugin.magicHeroes.quest

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
        return registry.reload()
    }

    fun reload(): List<String> = registry.reload()
    fun quests(): Collection<Quest> = registry.all()

    fun start(player: Player, questId: String): QuestResult {
        val quest = registry.get(questId) ?: return QuestResult(false, "Unknown quest: $questId")
        val data = HeroPlayerManager.get()?.getPlayerData(player.uniqueId) ?: return QuestResult(false, "Profile not loaded.")
        if (!quest.prerequisites.all { it in data.completedQuests }) return QuestResult(false, "Quest prerequisites not met.")
        if (!quest.repeatable && quest.id in data.completedQuests) return QuestResult(false, "Quest already completed.")
        if (quest.id in data.questProgress) return QuestResult(false, "Quest already active.")
        data.questProgress[quest.id] = quest.objectives.associate { it.id to 0 }.toMutableMap()
        return QuestResult(true, "Started ${quest.displayName}.")
    }

    fun progress(player: Player, type: QuestObjectiveType, target: String, amount: Int = 1): List<QuestResult> {
        if (amount <= 0) return emptyList()
        val data = HeroPlayerManager.get()?.getPlayerData(player.uniqueId) ?: return emptyList()
        return data.questProgress.keys.toList().mapNotNull { questId ->
            val quest = registry.get(questId) ?: return@mapNotNull null
            val progress = data.questProgress[questId] ?: return@mapNotNull null
            quest.objectives.filter { it.type == type && it.target.equals(target, true) }.forEach { objective ->
                progress[objective.id] = ((progress[objective.id] ?: 0) + amount).coerceAtMost(objective.required)
            }
            if (quest.objectives.all { (progress[it.id] ?: 0) >= it.required }) complete(player, quest) else null
        }
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
