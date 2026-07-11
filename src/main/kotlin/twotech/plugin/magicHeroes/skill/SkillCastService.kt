package twotech.plugin.magicHeroes.skill

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import twotech.plugin.magicHeroes.combat.CombatService
import twotech.plugin.magicHeroes.manager.HeroPlayerManager
import twotech.plugin.magicHeroes.resource.ResourceService
import java.util.UUID

sealed interface SkillCastResult {
    data class Success(val skill: Skill) : SkillCastResult
    data class Rejected(val reason: String) : SkillCastResult
}

class SkillCastService(
    private val registry: SkillRegistry,
    private val resources: ResourceService,
    private val combat: CombatService
) {
    private val cooldowns = CooldownManager()

    fun cast(player: Player, id: String, target: LivingEntity? = null): SkillCastResult {
        val skill = registry.get(id) ?: return SkillCastResult.Rejected("Unknown skill: $id")
        val profile = HeroPlayerManager.get()?.getPlayerData(player.uniqueId)
            ?: return SkillCastResult.Rejected("Profile not loaded.")
        if (skill.id !in profile.unlockedSkills) return SkillCastResult.Rejected("Skill is locked.")
        if (profile.level < skill.requiredLevel) return SkillCastResult.Rejected("Requires level ${skill.requiredLevel}.")
        if (skill.requiredClass != null && profile.rpgClass?.id?.lowercase() != skill.requiredClass) {
            return SkillCastResult.Rejected("Requires class ${skill.requiredClass}.")
        }
        val recipient = resolveTarget(player, skill, target) ?: return SkillCastResult.Rejected("Skill needs a target.")
        val remaining = cooldowns.remaining(player.uniqueId, skill.id)
        if (remaining > 0 && !player.hasPermission("magicheroes.bypass.cooldown")) {
            return SkillCastResult.Rejected("Cooldown: ${remaining}ms.")
        }
        if (!resources.spendMana(player, skill.manaCost)) return SkillCastResult.Rejected("Not enough mana.")
        cooldowns.start(player.uniqueId, skill.id, skill.cooldownMillis)
        execute(player, skill, recipient)
        return SkillCastResult.Success(skill)
    }

    fun clear(playerId: UUID) = cooldowns.clear(playerId)

    private fun resolveTarget(player: Player, skill: Skill, target: LivingEntity?): LivingEntity? = when (skill.target) {
        SkillTarget.SELF, SkillTarget.AREA -> player
        SkillTarget.SINGLE_ENTITY -> target?.takeIf { it.location.world == player.location.world && it.location.distanceSquared(player.location) <= skill.range * skill.range }
    }

    private fun execute(player: Player, skill: Skill, recipient: LivingEntity) {
        skill.mechanics.forEach { mechanic ->
            when (mechanic) {
                SkillMechanic.HEAL -> combat.heal(player, skill.power)
                SkillMechanic.DAMAGE -> if (recipient !== player) recipient.damage(skill.power)
                SkillMechanic.KNOCKBACK -> if (recipient !== player) {
                    recipient.velocity = recipient.location.toVector().subtract(player.location.toVector())
                        .normalize().multiply(skill.power.coerceAtLeast(0.0))
                }
                SkillMechanic.PROJECTILE, SkillMechanic.SHIELD, SkillMechanic.COMMAND -> Unit
            }
        }
    }
}
