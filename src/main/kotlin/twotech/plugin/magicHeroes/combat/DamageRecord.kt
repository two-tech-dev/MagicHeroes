package twotech.plugin.magicHeroes.combat

import java.util.UUID

data class DamageRecord(
    val attackerId: UUID?,
    val victimId: UUID,
    val damageType: DamageType,
    val finalDamage: Double,
    val itemId: String?
)
