package twotech.plugin.magicHeroes.item.advance

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import twotech.plugin.magicHeroes.util.ItemUtils
import twotech.plugin.magicHeroes.util.PdcKeys
import java.util.UUID

data class UpgradeResult(val success: Boolean, val level: Int, val message: String)

class ItemAdvanceService(plugin: JavaPlugin) {
    private val keys = PdcKeys(plugin)

    fun tier(item: ItemStack): ItemTier = item.itemMeta?.persistentDataContainer
        ?.get(keys.tier, PersistentDataType.STRING)?.let { runCatching { ItemTier.valueOf(it) }.getOrNull() } ?: ItemTier.COMMON

    fun setTier(item: ItemStack, tier: ItemTier) {
        val meta = item.itemMeta ?: return
        meta.persistentDataContainer.set(keys.tier, PersistentDataType.STRING, tier.name)
        item.itemMeta = meta
        ItemUtils.resetAndUpdateTooltip(null, item)
    }

    fun upgradeLevel(item: ItemStack): Int = item.itemMeta?.persistentDataContainer
        ?.get(keys.upgradeLevel, PersistentDataType.INTEGER) ?: 0

    fun upgrade(item: ItemStack, success: Boolean, maximum: Int = 20): UpgradeResult {
        val meta = item.itemMeta ?: return UpgradeResult(false, 0, "Item has no metadata.")
        val current = upgradeLevel(item)
        if (current >= maximum) return UpgradeResult(false, current, "Item is already max upgrade.")
        val next = if (success) current + 1 else current
        meta.persistentDataContainer.set(keys.upgradeLevel, PersistentDataType.INTEGER, next)
        item.itemMeta = meta
        ItemUtils.resetAndUpdateTooltip(null, item)
        return UpgradeResult(success, next, if (success) "Upgrade succeeded." else "Upgrade failed.")
    }

    fun socketCount(item: ItemStack): Int = item.itemMeta?.persistentDataContainer
        ?.get(keys.socketCount, PersistentDataType.INTEGER) ?: 0

    fun addSocket(item: ItemStack, maximum: Int = 6): Boolean {
        val meta = item.itemMeta ?: return false
        val count = socketCount(item)
        if (count >= maximum) return false
        meta.persistentDataContainer.set(keys.socketCount, PersistentDataType.INTEGER, count + 1)
        item.itemMeta = meta
        ItemUtils.resetAndUpdateTooltip(null, item)
        return true
    }

    fun socketGem(item: ItemStack, gemId: String): Boolean {
        val meta = item.itemMeta ?: return false
        val current = meta.persistentDataContainer.get(keys.sockets, PersistentDataType.STRING)?.split(',')?.filter(String::isNotBlank)?.toMutableList() ?: mutableListOf()
        if (current.size >= socketCount(item)) return false
        current += gemId.lowercase()
        meta.persistentDataContainer.set(keys.sockets, PersistentDataType.STRING, current.joinToString(","))
        item.itemMeta = meta
        ItemUtils.resetAndUpdateTooltip(null, item)
        return true
    }

    fun soulbind(item: ItemStack, owner: UUID) {
        val meta = item.itemMeta ?: return
        meta.persistentDataContainer.set(keys.soulboundOwner, PersistentDataType.STRING, owner.toString())
        item.itemMeta = meta
        ItemUtils.resetAndUpdateTooltip(null, item)
    }

    fun canUse(player: Player, item: ItemStack): Boolean {
        val owner = item.itemMeta?.persistentDataContainer?.get(keys.soulboundOwner, PersistentDataType.STRING) ?: return true
        return owner == player.uniqueId.toString() || player.hasPermission("magicheroes.bypass.soulbound")
    }
}
