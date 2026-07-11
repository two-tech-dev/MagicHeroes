package twotech.plugin.magicHeroes.resource

import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import twotech.plugin.magicHeroes.manager.HeroPlayerManager

/** Runtime bridge for Bukkit health and profile-backed mana. */
class ResourceService {
    fun currentMana(player: Player): Double? = HeroPlayerManager.get()?.getPlayerData(player.uniqueId)?.currentMana

    fun maxMana(player: Player): Double? = HeroPlayerManager.get()?.getPlayerData(player.uniqueId)?.getTotalMaxMana()

    fun spendMana(player: Player, amount: Double): Boolean {
        if (!amount.isFinite() || amount < 0.0) return false
        val data = HeroPlayerManager.get()?.getPlayerData(player.uniqueId) ?: return false
        if (data.currentMana < amount) return false
        data.currentMana -= amount
        return true
    }

    fun restoreMana(player: Player) {
        HeroPlayerManager.get()?.getPlayerData(player.uniqueId)?.let { data ->
            data.currentMana = data.getTotalMaxMana()
        }
    }

    fun heal(player: Player, amount: Double): Boolean {
        if (!amount.isFinite() || amount <= 0.0) return false
        val max = player.getAttribute(Attribute.MAX_HEALTH)?.value ?: return false
        player.health = (player.health + amount).coerceAtMost(max)
        return true
    }
}
