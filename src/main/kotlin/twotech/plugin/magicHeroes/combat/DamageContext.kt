package twotech.plugin.magicHeroes.combat

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

data class DamageContext(
    val attacker: LivingEntity?,
    val victim: LivingEntity,
    val damageType: DamageType,
    val baseDamage: Double,
    var outgoingDamage: Double = baseDamage,
    var isCritical: Boolean = false,
    var critMultiplier: Double = 1.0,
    var penetration: Double = 0.0,
    var defense: Double = 0.0,
    var elementalResistance: Double = 0.0,
    var shieldAbsorb: Double = 0.0,
    var finalDamage: Double = baseDamage,
    var cancelled: Boolean = false,
    val tags: MutableSet<String> = mutableSetOf(),
    val skillId: String? = null,
    val itemId: String? = null
) {
    val attackerPlayer: Player? get() = attacker as? Player
    val victimPlayer: Player? get() = victim as? Player
}
