package twotech.plugin.magicHeroes.manager

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import twotech.plugin.magicHeroes.calculator.StatCalculator
import twotech.plugin.magicHeroes.data.ResourceService
import twotech.plugin.magicHeroes.event.ItemBreakEvent
import twotech.plugin.magicHeroes.util.ItemUtils
import twotech.plugin.magicHeroes.util.PdcKeys

class DurabilityManager(private val plugin: JavaPlugin) {
    private val keys = PdcKeys(plugin)

    companion object {
        @Volatile
        private var instance: DurabilityManager? = null

        fun getInstance(plugin: JavaPlugin): DurabilityManager = instance ?: synchronized(this) {
            instance ?: DurabilityManager(plugin).also { instance = it }
        }

        fun get(): DurabilityManager? = instance
    }

    fun hasDurability(item: ItemStack): Boolean = item.itemMeta?.persistentDataContainer
        ?.has(keys.maxDurability, PersistentDataType.INTEGER) == true

    fun isInfinite(item: ItemStack): Boolean = item.itemMeta?.persistentDataContainer
        ?.get(keys.maxDurability, PersistentDataType.INTEGER) == -1

    fun isBroken(item: ItemStack): Boolean {
        val pdc = item.itemMeta?.persistentDataContainer ?: return false
        val max = pdc.get(keys.maxDurability, PersistentDataType.INTEGER) ?: return false
        val current = pdc.get(keys.durability, PersistentDataType.INTEGER) ?: return false
        return max != -1 && current <= 0
    }

    fun syncFakeDurability(meta: ItemMeta, item: ItemStack) {
        meta.isUnbreakable = true
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
        val maxVanilla = item.type.maxDurability.toInt()
        if (maxVanilla <= 0 || meta !is Damageable) return
        val pdc = meta.persistentDataContainer
        val max = pdc.get(keys.maxDurability, PersistentDataType.INTEGER) ?: return
        val current = pdc.get(keys.durability, PersistentDataType.INTEGER) ?: return
        val ratio = if (max == -1) 1.0 else current.toDouble().coerceIn(0.0, max.toDouble()) / max
        meta.damage = (maxVanilla - maxVanilla * ratio).toInt().coerceIn(0, maxVanilla - 1)
    }

    fun syncFakeDurability(item: ItemStack) {
        val meta = item.itemMeta ?: return
        syncFakeDurability(meta, item)
        item.itemMeta = meta
    }

    fun reduceDurability(player: Player, item: ItemStack, amount: Int) {
        if (amount <= 0 || !hasDurability(item) || isInfinite(item) || isBroken(item)) return
        val meta = item.itemMeta ?: return
        val pdc = meta.persistentDataContainer
        val max = pdc.get(keys.maxDurability, PersistentDataType.INTEGER) ?: return
        val current = pdc.get(keys.durability, PersistentDataType.INTEGER) ?: return
        if (max <= 0) return
        val newCurrent = (current - amount).coerceAtLeast(0)
        pdc.set(keys.durability, PersistentDataType.INTEGER, newCurrent)
        syncFakeDurability(meta, item)
        item.itemMeta = meta
        ItemUtils.resetAndUpdateTooltip(player, item)
        if (newCurrent != 0) return

        Bukkit.getPluginManager().callEvent(ItemBreakEvent(player, item))
        val itemName = meta.displayName()?.let(PlainTextComponentSerializer.plainText()::serialize) ?: item.type.name
        player.sendMessage(Component.text("$itemName is broken."))
        player.playSound(player.location, Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f)
        val data = HeroPlayerManager.get()?.getPlayerData(player.uniqueId) ?: return
        StatCalculator.updateEquipmentStats(player, data)
        ResourceService().applyMaxHealth(player, data.getTotalMaxHealth())
    }

    fun getCurrentDurability(item: ItemStack): Int = item.itemMeta?.persistentDataContainer
        ?.get(keys.durability, PersistentDataType.INTEGER) ?: 0

    fun getMaxDurability(item: ItemStack): Int = item.itemMeta?.persistentDataContainer
        ?.get(keys.maxDurability, PersistentDataType.INTEGER) ?: 0

    fun setDurability(item: ItemStack, current: Int, max: Int) {
        require(max > 0) { "max durability must be positive" }
        require(current in 0..max) { "current durability must be between zero and max" }
        val meta = item.itemMeta ?: return
        meta.persistentDataContainer.set(keys.durability, PersistentDataType.INTEGER, current)
        meta.persistentDataContainer.set(keys.maxDurability, PersistentDataType.INTEGER, max)
        syncFakeDurability(meta, item)
        item.itemMeta = meta
    }

    fun setInfiniteDurability(item: ItemStack) {
        val meta = item.itemMeta ?: return
        meta.persistentDataContainer.set(keys.durability, PersistentDataType.INTEGER, 1)
        meta.persistentDataContainer.set(keys.maxDurability, PersistentDataType.INTEGER, -1)
        syncFakeDurability(meta, item)
        item.itemMeta = meta
    }

    fun resetDurability(item: ItemStack) {
        val meta = item.itemMeta ?: return
        meta.persistentDataContainer.remove(keys.durability)
        meta.persistentDataContainer.remove(keys.maxDurability)
        meta.isUnbreakable = false
        meta.removeItemFlags(ItemFlag.HIDE_UNBREAKABLE)
        if (meta is Damageable) meta.damage = 0
        item.itemMeta = meta
    }
}
