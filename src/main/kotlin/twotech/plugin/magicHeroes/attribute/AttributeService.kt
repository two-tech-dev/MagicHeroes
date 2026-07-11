package twotech.plugin.magicHeroes.attribute

import org.bukkit.entity.Player
import twotech.plugin.magicHeroes.manager.HeroPlayerManager

data class AttributeResult(val success: Boolean, val message: String)

class AttributeService {
    fun points(player: Player): Int = HeroPlayerManager.get()?.getPlayerData(player.uniqueId)?.attributePoints ?: 0

    fun spend(player: Player, type: AttributeType, amount: Int = 1): AttributeResult {
        if (amount <= 0) return AttributeResult(false, "Amount must be positive.")
        val data = HeroPlayerManager.get()?.getPlayerData(player.uniqueId)
            ?: return AttributeResult(false, "Profile not loaded.")
        if (data.attributePoints < amount) return AttributeResult(false, "Not enough attribute points.")
        data.attributePoints -= amount
        data.attributes[type] = data.attribute(type) + amount
        return AttributeResult(true, "Added $amount ${type.name.lowercase()}.")
    }

    fun reset(player: Player): Int {
        val data = HeroPlayerManager.get()?.getPlayerData(player.uniqueId) ?: return 0
        val refunded = data.attributes.values.sum()
        data.attributes.clear()
        data.attributePoints += refunded
        return refunded
    }
}
