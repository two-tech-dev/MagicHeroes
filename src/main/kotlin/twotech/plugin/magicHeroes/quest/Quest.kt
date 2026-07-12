package twotech.plugin.magicHeroes.quest

enum class QuestObjectiveType { KILL, COLLECT, MINE, REACH, INTERACT }

data class ReachTarget(val world: String, val x: Double, val y: Double, val z: Double, val radius: Double) {
    fun contains(worldName: String, x: Double, y: Double, z: Double): Boolean {
        if (!world.equals(worldName, true)) return false
        val dx = this.x - x
        val dy = this.y - y
        val dz = this.z - z
        return dx * dx + dy * dy + dz * dz <= radius * radius
    }

    companion object {
        fun parse(raw: String): ReachTarget? {
            val parts = raw.split(',').map(String::trim)
            if (parts.size != 5) return null
            val x = parts[1].toDoubleOrNull() ?: return null
            val y = parts[2].toDoubleOrNull() ?: return null
            val z = parts[3].toDoubleOrNull() ?: return null
            val radius = parts[4].toDoubleOrNull()?.takeIf { it > 0.0 } ?: return null
            val world = parts[0].takeIf(String::isNotBlank) ?: return null
            return ReachTarget(world, x, y, z, radius)
        }
    }
}

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
