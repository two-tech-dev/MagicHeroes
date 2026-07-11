package twotech.plugin.magicHeroes.combat

import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.plugin.java.JavaPlugin
import twotech.plugin.magicHeroes.item.ItemService
import twotech.plugin.magicHeroes.manager.HeroPlayerManager
import twotech.plugin.magicHeroes.stat.StatCalculationService
import twotech.plugin.magicHeroes.stat.StatType
import kotlin.random.Random
import java.util.concurrent.ConcurrentHashMap

class CombatService(
    private val stats: StatCalculationService,
    private val items: ItemService,
    private val random: Random = Random.Default
) {
    private val externalContexts = ConcurrentHashMap<java.util.UUID, DamageContext>()

    fun damage(context: DamageContext): Boolean {
        if (context.cancelled || !context.finalDamage.isFinite() || context.finalDamage <= 0.0 || context.victim.isDead) return false
        externalContexts[context.victim.uniqueId] = context
        context.victim.damage(context.finalDamage)
        return true
    }

    fun consumeExternalContext(victimId: java.util.UUID): DamageContext? = externalContexts.remove(victimId)

    fun apply(event: EntityDamageEvent): DamageContext? {
        val context = calculate(event) ?: return null
        if (context.cancelled) {
            event.isCancelled = true
            return context
        }
        event.damage = context.finalDamage
        Bukkit.getPluginManager().callEvent(MagicHeroesPostDamageEvent(context))
        return context
    }

    fun calculate(event: EntityDamageEvent): DamageContext? {
        val victim = event.entity as? LivingEntity ?: return null
        val attacker = (event as? EntityDamageByEntityEvent)?.damager as? LivingEntity
        val context = DamageContext(
            attacker = attacker,
            victim = victim,
            damageType = event.cause.toDamageType(),
            baseDamage = event.damage,
            itemId = (attacker as? Player)?.let { items.inspect(it.inventory.itemInMainHand)?.id }
        )
        Bukkit.getPluginManager().callEvent(MagicHeroesPreDamageEvent(context))
        if (context.cancelled) return context

        val attackerStats = (attacker as? Player)?.let(stats::snapshot)
        val victimStats = (victim as? Player)?.let(stats::snapshot)
        val weaponDamage = if (context.damageType == DamageType.MELEE || context.damageType == DamageType.PROJECTILE) {
            attackerStats?.value(StatType.ATTACK_DAMAGE) ?: 0.0
        } else 0.0
        context.outgoingDamage = (context.baseDamage + weaponDamage).coerceAtLeast(0.0)

        val critChance = (attackerStats?.value(StatType.CRITICAL_STRIKE_CHANCE) ?: 0.0).coerceIn(0.0, 100.0)
        context.isCritical = critChance > 0.0 && random.nextDouble(100.0) < critChance
        if (context.isCritical) {
            val criticalEvent = MagicHeroesCriticalHitEvent(context)
            Bukkit.getPluginManager().callEvent(criticalEvent)
            if (criticalEvent.isCancelled) context.isCritical = false
        }
        context.critMultiplier = 1.0 + (attackerStats?.value(StatType.CRITICAL_STRIKE_POWER) ?: 0.0).coerceAtLeast(0.0) / 100.0
        context.outgoingDamage = CombatMath.criticalDamage(context.outgoingDamage, context.isCritical, context.critMultiplier)

        context.penetration = when (context.damageType) {
            DamageType.MAGIC, DamageType.FIRE, DamageType.ICE, DamageType.LIGHTNING, DamageType.POISON -> attackerStats?.value(StatType.MAGIC_PENETRATION) ?: 0.0
            else -> attackerStats?.value(StatType.ARMOR_PENETRATION) ?: 0.0
        }
        context.defense = if (context.damageType == DamageType.TRUE) 0.0 else {
            val profileDefense = HeroPlayerManager.get()?.getPlayerData(victim.uniqueId)?.baseDefense ?: 0.0
            profileDefense + (victimStats?.value(StatType.DEFENSE) ?: 0.0)
        }
        context.finalDamage = CombatMath.defenseDamage(context.outgoingDamage, context.defense, context.penetration)
        context.elementalResistance = resistance(context.damageType, victimStats)
        context.finalDamage = CombatMath.elementalDamage(context.finalDamage, context.elementalResistance)
        context.finalDamage = (context.finalDamage - context.shieldAbsorb).coerceAtLeast(0.0)
        return context
    }

    fun heal(player: Player, amount: Double): Boolean {
        if (!amount.isFinite() || amount <= 0.0) return false
        val healEvent = MagicHeroesHealEvent(player, amount)
        Bukkit.getPluginManager().callEvent(healEvent)
        if (healEvent.isCancelled || !healEvent.amount.isFinite() || healEvent.amount <= 0.0) return false
        val maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)?.value ?: return false
        player.health = (player.health + healEvent.amount).coerceAtMost(maxHealth)
        return true
    }

    private fun resistance(type: DamageType, stats: twotech.plugin.magicHeroes.stat.StatSnapshot?): Double = when (type) {
        DamageType.FIRE -> stats?.value(StatType.FIRE_RESISTANCE) ?: 0.0
        DamageType.ICE -> stats?.value(StatType.ICE_RESISTANCE) ?: 0.0
        DamageType.LIGHTNING -> stats?.value(StatType.LIGHTNING_RESISTANCE) ?: 0.0
        DamageType.POISON -> stats?.value(StatType.POISON_RESISTANCE) ?: 0.0
        else -> 0.0
    } / 100.0

    private fun EntityDamageEvent.DamageCause.toDamageType(): DamageType = when (this) {
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
}
