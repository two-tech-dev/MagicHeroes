package twotech.plugin.magicHeroes.calculator

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import twotech.plugin.magicHeroes.data.HeroPlayerData
import twotech.plugin.magicHeroes.manager.DurabilityManager
import twotech.plugin.magicHeroes.util.PdcKeys

object StatCalculator {
    private var keys: PdcKeys? = null

    fun initialize(pdcKeys: PdcKeys) {
        keys = pdcKeys
    }

    fun updateEquipmentStats(player: Player, data: HeroPlayerData) {
        data.resetEquipmentStats()
        val inventory = player.inventory
        listOf(
            inventory.helmet,
            inventory.chestplate,
            inventory.leggings,
            inventory.boots,
            inventory.itemInMainHand,
            inventory.itemInOffHand
        ).forEach { addStatsFromItem(it, data) }
        data.currentMana = data.currentMana
    }

    private fun addStatsFromItem(item: ItemStack?, data: HeroPlayerData) {
        if (item == null || item.type.isAir || !item.hasItemMeta() || DurabilityManager.get()?.isBroken(item) == true) return
        val pdc = item.itemMeta?.persistentDataContainer ?: return
        val itemKeys = keys ?: return
        data.armorHealth += pdc.double(itemKeys.stat("max_health"), itemKeys.health)
        data.armorMana += pdc.double(itemKeys.stat("max_mana"), itemKeys.mana)
        data.armorDamage += pdc.double(itemKeys.stat("attack_damage"), itemKeys.damage)
        data.armorDefense += pdc.double(itemKeys.stat("defense"), itemKeys.defense)
        data.armorHealthRegen += pdc.double(itemKeys.stat("health_regen"), itemKeys.healthRegen)
        data.armorManaRegen += pdc.double(itemKeys.stat("mana_regen"), itemKeys.manaRegen)
    }

    private fun org.bukkit.persistence.PersistentDataContainer.double(
        primary: org.bukkit.NamespacedKey,
        legacy: org.bukkit.NamespacedKey
    ): Double = get(primary, PersistentDataType.DOUBLE) ?: get(legacy, PersistentDataType.DOUBLE) ?: 0.0

    fun calculateDamageAfterDefense(baseDamage: Double, defense: Double): Double {
        require(baseDamage.isFinite() && baseDamage >= 0.0) { "baseDamage must be finite and non-negative" }
        require(defense.isFinite() && defense >= 0.0) { "defense must be finite and non-negative" }
        return baseDamage * (100.0 / (100.0 + defense))
    }
}
