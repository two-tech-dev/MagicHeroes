package twotech.plugin.magicHeroes.skill

import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import twotech.plugin.magicHeroes.combat.CombatService
import twotech.plugin.magicHeroes.manager.HeroPlayerManager
import twotech.plugin.magicHeroes.resource.ResourceService
import java.nio.file.Path
import java.util.UUID

class SkillService(private val plugin: JavaPlugin, combat: CombatService) {
    private val registry = SkillRegistry(Path.of(plugin.dataFolder.path, "skills"))
    private val trees = SkillTreeRegistry(Path.of(plugin.dataFolder.path, "skill-trees"))
    private val cast = SkillCastService(registry, ResourceService(), combat)

    fun initialize(): List<String> {
        plugin.saveResource("skills/warrior/slash.yml", false)
        plugin.saveResource("skills/mage/minor-heal.yml", false)
        plugin.saveResource("skill-trees/warrior.yml", false)
        plugin.saveResource("skill-trees/mage.yml", false)
        return registry.reload() + trees.reload()
    }

    fun reload(): List<String> = registry.reload() + trees.reload()
    fun ids(): Set<String> = registry.ids()
    fun register(skill: Skill): Boolean = registry.register(skill)
    fun unregister(id: String): Boolean = registry.unregister(id)

    fun bind(player: Player, slot: Int, id: String): Boolean {
        val data = HeroPlayerManager.get()?.getPlayerData(player.uniqueId) ?: return false
        if (slot !in 1..9 || registry.get(id) == null || id !in data.unlockedSkills) return false
        data.skillBindings[slot] = id
        return true
    }

    fun unbind(player: Player, slot: Int): Boolean {
        val data = HeroPlayerManager.get()?.getPlayerData(player.uniqueId) ?: return false
        return data.skillBindings.remove(slot) != null
    }

    fun bound(player: Player, slot: Int): String? = HeroPlayerManager.get()?.getPlayerData(player.uniqueId)?.skillBindings?.get(slot)

    fun unlock(player: Player, id: String): Boolean {
        if (registry.get(id) == null) return false
        val data = HeroPlayerManager.get()?.getPlayerData(player.uniqueId) ?: return false
        data.unlockedSkills += id
        return true
    }

    fun reset(player: Player) {
        val data = HeroPlayerManager.get()?.getPlayerData(player.uniqueId) ?: return
        data.skillBindings.clear()
        data.unlockedSkills.clear()
        data.skillTreeLevels.clear()
    }

    fun isUnlocked(player: Player, id: String): Boolean = id in HeroPlayerManager.get()?.getPlayerData(player.uniqueId)?.unlockedSkills.orEmpty()

    fun cast(player: Player, id: String) = cast.cast(player, id)

    fun unlockTreeNode(player: Player, nodeId: String): String {
        val data = HeroPlayerManager.get()?.getPlayerData(player.uniqueId) ?: return "Profile not loaded."
        val tree = data.rpgClass?.id?.let(trees::get) ?: return "No skill tree for class."
        if (!tree.canUnlock(data.skillTreeLevels, nodeId, data.skillPoints)) return "Node cannot be unlocked."
        val node = tree.node(nodeId) ?: return "Unknown node."
        data.skillPoints -= node.cost
        data.skillTreeLevels[nodeId] = (data.skillTreeLevels[nodeId] ?: 0) + 1
        node.skillId?.let(data.unlockedSkills::add)
        return "Unlocked $nodeId."
    }

    fun clear(playerId: UUID) = cast.clear(playerId)
}
