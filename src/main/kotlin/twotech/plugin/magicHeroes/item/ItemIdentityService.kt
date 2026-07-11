package twotech.plugin.magicHeroes.item

import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import twotech.plugin.magicHeroes.item.template.ItemTemplate
import twotech.plugin.magicHeroes.manager.DurabilityManager
import twotech.plugin.magicHeroes.util.ItemUtils
import twotech.plugin.magicHeroes.util.PdcKeys
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class ItemIdentityService(private val plugin: JavaPlugin) {
    private val keys = PdcKeys(plugin)

    fun write(item: ItemStack, template: ItemTemplate, owner: String? = null) {
        val meta = item.itemMeta ?: return
        val pdc = meta.persistentDataContainer
        pdc.set(keys.itemId, PersistentDataType.STRING, template.id)
        pdc.set(keys.itemType, PersistentDataType.STRING, template.type)
        pdc.set(keys.itemVersion, PersistentDataType.INTEGER, template.version)
        pdc.set(keys.templateHash, PersistentDataType.STRING, hash(template))
        pdc.set(keys.upgradeLevel, PersistentDataType.INTEGER, 0)
        pdc.set(keys.identified, PersistentDataType.BYTE, 1.toByte())
        owner?.let { pdc.set(keys.soulboundOwner, PersistentDataType.STRING, it) }
        pdc.set(keys.name, PersistentDataType.STRING, template.displayName)
        if (template.levelRequirement > 0) pdc.set(keys.levelRequirement, PersistentDataType.INTEGER, template.levelRequirement)
        template.classRequirement?.let { pdc.set(keys.classRequirement, PersistentDataType.STRING, it) }
        item.itemMeta = meta
        template.maxDurability?.let { DurabilityManager.get()?.setDurability(item, it, it) }
        if (template.infiniteDurability) DurabilityManager.get()?.setInfiniteDurability(item)
        ItemUtils.resetAndUpdateTooltip(null, item)
    }

    fun id(item: ItemStack): String? = item.itemMeta?.persistentDataContainer
        ?.get(keys.itemId, PersistentDataType.STRING)

    fun type(item: ItemStack): String? = item.itemMeta?.persistentDataContainer
        ?.get(keys.itemType, PersistentDataType.STRING)

    private fun hash(template: ItemTemplate): String {
        val bytes = "${template.id}|${template.version}|${template.material}|${template.baseStats}".toByteArray(StandardCharsets.UTF_8)
        return MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
    }
}
