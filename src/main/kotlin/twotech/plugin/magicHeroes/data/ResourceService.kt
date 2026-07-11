package twotech.plugin.magicHeroes.data

import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import kotlin.math.max

class ResourceService {
    fun applyMaxHealth(player: Player, maxHealth: Double) {
        val attribute = player.getAttribute(Attribute.MAX_HEALTH) ?: return
        val safeMax = max(1.0, maxHealth)
        attribute.baseValue = safeMax
        player.health = player.health.coerceIn(0.0, safeMax)
    }

    fun applyLegacyHealth(player: Player, legacyCurrentHealth: Double?, maxHealth: Double) {
        val attribute = player.getAttribute(Attribute.MAX_HEALTH) ?: return
        val safeMax = max(1.0, maxHealth)
        attribute.baseValue = safeMax
        if (legacyCurrentHealth != null) {
            player.health = legacyCurrentHealth.coerceIn(0.0, safeMax)
        } else {
            player.health = player.health.coerceIn(0.0, safeMax)
        }
    }

    fun restoreHealth(player: Player) {
        player.health = player.getAttribute(Attribute.MAX_HEALTH)?.value ?: player.health
    }

    fun restoreMana(data: HeroPlayerData) {
        data.currentMana = data.getTotalMaxMana()
    }

    fun regenerate(data: HeroPlayerData, elapsedSeconds: Double) {
        val elapsed = elapsedSeconds.coerceIn(0.0, 10.0)
        data.currentMana += data.getTotalManaRegen() * elapsed
    }
}
