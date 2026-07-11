package twotech.plugin.magicHeroes.listener

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import twotech.plugin.magicHeroes.combat.DamageContext
import twotech.plugin.magicHeroes.combat.DamageType
import twotech.plugin.magicHeroes.combat.MagicHeroesDeathEvent
import twotech.plugin.magicHeroes.combat.MagicHeroesKillEvent

/** Emits RPG lifecycle events after Bukkit confirms player death. */
class CombatLifecycleListener : Listener {
    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val victim = event.entity
        val context = DamageContext(
            attacker = victim.killer,
            victim = victim,
            damageType = DamageType.ENVIRONMENTAL,
            baseDamage = 0.0,
            finalDamage = 0.0
        )
        Bukkit.getPluginManager().callEvent(MagicHeroesDeathEvent(context))
        if (victim.killer != null) Bukkit.getPluginManager().callEvent(MagicHeroesKillEvent(context))
    }
}
