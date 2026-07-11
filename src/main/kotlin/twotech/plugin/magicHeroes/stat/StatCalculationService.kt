package twotech.plugin.magicHeroes.stat

import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import twotech.plugin.magicHeroes.manager.DurabilityManager
import twotech.plugin.magicHeroes.util.PdcKeys
import java.util.UUID

class StatCalculationService(plugin: JavaPlugin) {
    private val keys = PdcKeys(plugin)
    private val cache = mutableMapOf<UUID, StatSnapshot>()
    private val dirty = mutableSetOf<UUID>()

    fun markDirty(playerId: UUID) {
        dirty += playerId
    }

    fun invalidate(playerId: UUID) {
        cache.remove(playerId)
        dirty.remove(playerId)
    }

    fun snapshot(player: org.bukkit.entity.Player): StatSnapshot {
        val id = player.uniqueId
        if (id !in dirty) cache[id]?.let { return it }
        val sources = mutableListOf<StatSource>()
        val values = mutableMapOf<StatType, Double>()
        listOf(
            "helmet" to player.inventory.helmet,
            "chestplate" to player.inventory.chestplate,
            "leggings" to player.inventory.leggings,
            "boots" to player.inventory.boots,
            "main-hand" to player.inventory.itemInMainHand,
            "off-hand" to player.inventory.itemInOffHand
        ).forEach { (source, item) -> collect(source, item, values, sources) }
        return StatSnapshot(values.toMap(), sources.toList()).also {
            cache[id] = it
            dirty.remove(id)
        }
    }

    private fun collect(source: String, item: ItemStack?, values: MutableMap<StatType, Double>, sources: MutableList<StatSource>) {
        if (item == null || item.type.isAir || DurabilityManager.get()?.isBroken(item) == true) return
        val pdc = item.itemMeta?.persistentDataContainer ?: return
        StatType.entries.forEach { stat ->
            val value = pdc.get(keys.stat(stat.name), PersistentDataType.DOUBLE) ?: legacyValue(pdc, stat) ?: return@forEach
            values[stat] = (values[stat] ?: 0.0) + value
            sources += StatSource(source, stat, value)
        }
    }

    private fun legacyValue(pdc: org.bukkit.persistence.PersistentDataContainer, stat: StatType): Double? = when (stat) {
        StatType.ATTACK_DAMAGE -> pdc.get(keys.damage, PersistentDataType.DOUBLE)
        StatType.MAX_HEALTH -> pdc.get(keys.health, PersistentDataType.DOUBLE)
        StatType.HEALTH_REGEN -> pdc.get(keys.healthRegen, PersistentDataType.DOUBLE)
        StatType.MAX_MANA -> pdc.get(keys.mana, PersistentDataType.DOUBLE)
        StatType.MANA_REGEN -> pdc.get(keys.manaRegen, PersistentDataType.DOUBLE)
        StatType.DEFENSE -> pdc.get(keys.defense, PersistentDataType.DOUBLE)
        else -> null
    }
}
