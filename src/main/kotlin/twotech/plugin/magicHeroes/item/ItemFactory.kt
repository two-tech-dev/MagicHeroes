package twotech.plugin.magicHeroes.item

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import twotech.plugin.magicHeroes.item.template.ItemTemplate
import twotech.plugin.magicHeroes.util.ColorTranslator
import twotech.plugin.magicHeroes.util.PdcKeys
import twotech.plugin.magicHeroes.stat.StatType

class ItemFactory(private val plugin: JavaPlugin, private val identity: ItemIdentityService) {
    private val keys = PdcKeys(plugin)

    fun create(template: ItemTemplate): ItemStack {
        val material = Material.matchMaterial(template.material) ?: error("Invalid material ${template.material}")
        val item = ItemStack(material)
        val meta = item.itemMeta ?: error("Material ${template.material} has no item meta")
        meta.displayName(ColorTranslator.translate(template.displayName))
        if (template.lore.isNotEmpty()) meta.lore(template.lore.map(ColorTranslator::translate))
        template.baseStats.forEach { (stat, value) -> writeLegacyStat(meta.persistentDataContainer, stat, value) }
        item.itemMeta = meta
        identity.write(item, template)
        return item
    }

    private fun writeLegacyStat(pdc: org.bukkit.persistence.PersistentDataContainer, stat: String, value: Double) {
        val normalized = stat.uppercase()
        val statType = runCatching { StatType.valueOf(normalized) }.getOrElse {
            require(normalized in setOf("DAMAGE", "HEALTH", "MANA")) { "Unknown stat $stat" }
            when (normalized) {
                "DAMAGE" -> StatType.ATTACK_DAMAGE
                "HEALTH" -> StatType.MAX_HEALTH
                else -> StatType.MAX_MANA
            }
        }
        pdc.set(keys.stat(statType.name), PersistentDataType.DOUBLE, value)
        val legacyKey = when (statType) {
            StatType.ATTACK_DAMAGE -> keys.damage
            StatType.MAX_HEALTH -> keys.health
            StatType.MAX_MANA -> keys.mana
            StatType.DEFENSE -> keys.defense
            StatType.HEALTH_REGEN -> keys.healthRegen
            StatType.MANA_REGEN -> keys.manaRegen
            else -> return
        }
        pdc.set(legacyKey, PersistentDataType.DOUBLE, value)
    }
}
