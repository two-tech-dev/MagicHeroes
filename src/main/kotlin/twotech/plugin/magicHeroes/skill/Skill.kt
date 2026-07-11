package twotech.plugin.magicHeroes.skill

enum class SkillTarget { SELF, SINGLE_ENTITY, AREA }

enum class SkillMechanic { DAMAGE, HEAL, PROJECTILE, SHIELD, KNOCKBACK, COMMAND }

data class Skill(
    val id: String,
    val displayName: String,
    val description: String,
    val manaCost: Double,
    val cooldownMillis: Long,
    val castMillis: Long = 0,
    val range: Double = 0.0,
    val target: SkillTarget = SkillTarget.SELF,
    val damageType: String? = null,
    val power: Double = 0.0,
    val mechanics: List<SkillMechanic> = emptyList(),
    val requiredLevel: Int = 1,
    val requiredClass: String? = null
)
