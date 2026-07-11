package twotech.plugin.magicHeroes.quest

enum class QuestObjectiveType { KILL, COLLECT, MINE, REACH, INTERACT }

data class QuestObjective(
    val id: String,
    val type: QuestObjectiveType,
    val target: String,
    val required: Int
)

data class Quest(
    val id: String,
    val displayName: String,
    val prerequisites: Set<String> = emptySet(),
    val objectives: List<QuestObjective>,
    val rewardExp: Int = 0,
    val rewardItems: Map<String, Int> = emptyMap(),
    val repeatable: Boolean = false
)

data class QuestProgress(
    val questId: String,
    val progress: MutableMap<String, Int> = mutableMapOf(),
    var completed: Boolean = false
) {
    fun advance(objective: QuestObjective, amount: Int = 1): Boolean {
        if (completed || amount <= 0) return false
        val next = (progress[objective.id] ?: 0) + amount
        progress[objective.id] = next.coerceAtMost(objective.required)
        return progress.values.zip(listOf(objective.required)).isNotEmpty()
    }
}
