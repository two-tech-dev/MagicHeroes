package twotech.plugin.magicHeroes.util

import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

class PdcKeys(plugin: JavaPlugin) {
    val health = NamespacedKey(plugin, "mh_health")
    val mana = NamespacedKey(plugin, "mh_mana")
    val damage = NamespacedKey(plugin, "mh_damage")
    val defense = NamespacedKey(plugin, "mh_defense")
    val healthRegen = NamespacedKey(plugin, "mh_health_regen")
    val manaRegen = NamespacedKey(plugin, "mh_mana_regen")
    val name = NamespacedKey(plugin, "mh_name")
    val customLore = NamespacedKey(plugin, "mh_custom_lore")
    val durability = NamespacedKey(plugin, "mh_durability")
    val maxDurability = NamespacedKey(plugin, "mh_max_durability")
    val levelRequirement = NamespacedKey(plugin, "mh_level_req")
    val classRequirement = NamespacedKey(plugin, "mh_class_req")

    val itemId = NamespacedKey(plugin, "item_id")
    val itemType = NamespacedKey(plugin, "item_type")
    val itemVersion = NamespacedKey(plugin, "item_version")
    val templateHash = NamespacedKey(plugin, "template_hash")
    val upgradeLevel = NamespacedKey(plugin, "upgrade_level")
    val identified = NamespacedKey(plugin, "identified")
    val soulboundOwner = NamespacedKey(plugin, "soulbound_owner")
    val tooltipTemplate = NamespacedKey(plugin, "mh_tooltip_template")
    val buttonType = NamespacedKey(plugin, "button_type")

    fun stat(id: String): NamespacedKey = NamespacedKey(plugin, "stat_${id.lowercase()}")
}
