package twotech.plugin.magicHeroes.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import twotech.plugin.magicHeroes.combat.CombatService

class DamageEventListener(private val combat: CombatService) : Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onEntityDamage(event: EntityDamageEvent) {
        val external = combat.consumeExternalContext(event.entity.uniqueId)
        if (external != null) {
            event.damage = external.finalDamage
            return
        }
        combat.apply(event)
    }
}
