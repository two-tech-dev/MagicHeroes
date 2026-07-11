package twotech.plugin.magicHeroes.combat

import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.entity.LivingEntity

fun EntityDamageEvent.sourceEntity(): LivingEntity? = (this as? EntityDamageByEntityEvent)?.damager as? LivingEntity

fun EntityDamageEvent.damageType(): DamageType = when (cause) {
    EntityDamageEvent.DamageCause.ENTITY_ATTACK,
    EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK -> DamageType.MELEE
    EntityDamageEvent.DamageCause.PROJECTILE -> DamageType.PROJECTILE
    EntityDamageEvent.DamageCause.MAGIC,
    EntityDamageEvent.DamageCause.DRAGON_BREATH -> DamageType.MAGIC
    EntityDamageEvent.DamageCause.FIRE,
    EntityDamageEvent.DamageCause.FIRE_TICK,
    EntityDamageEvent.DamageCause.LAVA,
    EntityDamageEvent.DamageCause.HOT_FLOOR -> DamageType.FIRE
    EntityDamageEvent.DamageCause.FALL -> DamageType.FALL
    EntityDamageEvent.DamageCause.VOID -> DamageType.VOID
    EntityDamageEvent.DamageCause.BLOCK_EXPLOSION,
    EntityDamageEvent.DamageCause.ENTITY_EXPLOSION -> DamageType.EXPLOSION
    else -> DamageType.ENVIRONMENTAL
}
