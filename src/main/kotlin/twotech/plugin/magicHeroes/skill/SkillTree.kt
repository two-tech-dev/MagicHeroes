package twotech.plugin.magicHeroes.skill

data class SkillTreeNode(
    val id: String,
    val skillId: String? = null,
    val parent: String? = null,
    val incompatible: Set<String> = emptySet(),
    val maxLevel: Int = 1,
    val cost: Int = 1
)

class SkillTree(private val nodes: Map<String, SkillTreeNode>) {
    fun node(id: String): SkillTreeNode? = nodes[id]
    fun all(): Collection<SkillTreeNode> = nodes.values
    fun canUnlock(levels: Map<String, Int>, id: String, points: Int): Boolean {
        val node = nodes[id] ?: return false
        if (points < node.cost || (levels[id] ?: 0) >= node.maxLevel) return false
        if (node.parent != null && (levels[node.parent] ?: 0) <= 0) return false
        return node.incompatible.none { (levels[it] ?: 0) > 0 }
    }
}
