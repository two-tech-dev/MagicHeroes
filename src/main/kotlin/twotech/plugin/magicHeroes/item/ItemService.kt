package twotech.plugin.magicHeroes.item

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import twotech.plugin.magicHeroes.item.registry.ItemRegistry
import twotech.plugin.magicHeroes.item.template.ItemTemplate
import twotech.plugin.magicHeroes.util.ItemUtils
import java.nio.file.Path

class ItemService(private val plugin: JavaPlugin) {
    private val registry = ItemRegistry(Path.of(plugin.dataFolder.path, "items"))
    private val identity = ItemIdentityService(plugin)
    private val factory = ItemFactory(plugin, identity)
    val requirements = RequirementService(plugin)

    fun initialize(): List<String> {
        plugin.saveResource("items/weapons/firebrand.yml", false)
        return registry.reload()
    }

    fun reload(): List<String> = registry.reload()

    fun template(id: String): ItemTemplate? = registry.get(id)

    fun templates(): Collection<ItemTemplate> = registry.all()

    fun create(id: String): ItemStack? = registry.get(id)?.let(factory::create)

    fun has(id: String): Boolean = registry.contains(id)

    fun give(player: Player, id: String, amount: Int): Boolean {
        val template = registry.get(id) ?: return false
        if (amount !in 1..64) return false
        repeat(amount) {
            val item = factory.create(template)
            val overflow = player.inventory.addItem(item)
            overflow.values.forEach { player.world.dropItemNaturally(player.location, it) }
        }
        return true
    }

    fun inspect(item: ItemStack): ItemIdentity? {
        val id = identity.id(item) ?: return null
        val template = registry.get(id)
        return ItemIdentity(id, identity.type(item), template?.version)
    }

    fun refreshTooltip(player: Player?, item: ItemStack) {
        ItemUtils.resetAndUpdateTooltip(player, item)
    }
}

data class ItemIdentity(val id: String, val type: String?, val templateVersion: Int?)
