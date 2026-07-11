package twotech.plugin.magicHeroes.item

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import twotech.plugin.magicHeroes.manager.HeroPlayerManager
import twotech.plugin.magicHeroes.util.PdcKeys

data class RequirementResult(val allowed: Boolean, val reason: String? = null)

class RequirementService(plugin: JavaPlugin) {
    private val keys = PdcKeys(plugin)

    fun check(player: Player, item: ItemStack): RequirementResult {
        if (player.hasPermission("magicheroes.bypass.requirements")) return RequirementResult(true)
        val pdc = item.itemMeta?.persistentDataContainer ?: return RequirementResult(true)
        val data = HeroPlayerManager.get()?.getPlayerData(player.uniqueId) ?: return RequirementResult(false, "Profile not loaded")
        val requiredLevel = pdc.get(keys.levelRequirement, PersistentDataType.INTEGER) ?: 0
        if (data.level < requiredLevel) return RequirementResult(false, "Requires level $requiredLevel")
        val requiredClass = pdc.get(keys.classRequirement, PersistentDataType.STRING)?.lowercase()
        if (requiredClass != null && data.rpgClass?.id?.lowercase() != requiredClass) {
            return RequirementResult(false, "Requires class $requiredClass")
        }
        return RequirementResult(true)
    }
}
