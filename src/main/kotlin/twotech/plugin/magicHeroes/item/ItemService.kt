package twotech.plugin.magicHeroes.item

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import twotech.plugin.magicHeroes.crafting.Recipe
import twotech.plugin.magicHeroes.crafting.RecipeRegistry
import twotech.plugin.magicHeroes.item.advance.ItemAdvanceService
import twotech.plugin.magicHeroes.item.registry.ItemRegistry
import twotech.plugin.magicHeroes.item.template.ItemTemplate
import twotech.plugin.magicHeroes.loot.LootTableRegistry
import twotech.plugin.magicHeroes.util.ItemUtils
import java.nio.file.Path
import java.util.Random

class ItemService(private val plugin: JavaPlugin) {
    private val registry = ItemRegistry(Path.of(plugin.dataFolder.path, "items"))
    private val identity = ItemIdentityService(plugin)
    private val factory = ItemFactory(plugin, identity)
    private val recipes = RecipeRegistry(Path.of(plugin.dataFolder.path, "recipes"))
    private val loot = LootTableRegistry(Path.of(plugin.dataFolder.path, "loot-tables"))
    val requirements = RequirementService(plugin)
    val advance = ItemAdvanceService(plugin)

    fun craftRecipe(id: String): Recipe? = recipes.get(id)

    fun craft(player: Player, id: String, random: Random = Random()): Boolean {
        val recipe = recipes.get(id) ?: return false
        if (!has(recipe.resultItem)) return false
        val requiredMaterials = recipe.ingredients.mapNotNull { (name, amount) ->
            org.bukkit.Material.matchMaterial(name.uppercase())?.let { it to amount }
        }
        if (requiredMaterials.size != recipe.ingredients.size) return false
        if (requiredMaterials.any { (material, amount) -> player.inventory.contents.filterNotNull().filter { it.type == material }.sumOf { it.amount } < amount }) return false
        requiredMaterials.forEach { (material, amount) -> removeMaterial(player, material, amount) }
        if (random.nextDouble() > recipe.successChance) return true
        return give(player, recipe.resultItem, recipe.resultAmount)
    }

    private fun removeMaterial(player: Player, material: org.bukkit.Material, amount: Int) {
        var remaining = amount
        player.inventory.contents.forEachIndexed { index, stack ->
            if (remaining == 0 || stack == null || stack.type != material) return@forEachIndexed
            val consumed = minOf(remaining, stack.amount)
            stack.amount -= consumed
            remaining -= consumed
            if (stack.amount == 0) player.inventory.setItem(index, null)
        }
    }

    fun rollLoot(id: String, random: Random = Random()): List<Pair<String, Int>> {
        val table = loot.get(id) ?: return emptyList()
        return buildList {
            table.guaranteed.forEach { add(it.itemId to it.amount) }
            table.choose(random)?.let { add(it.itemId to it.amount) }
        }
    }

    fun initialize(): List<String> {
        plugin.saveResource("items/weapons/firebrand.yml", false)
        plugin.saveResource("recipes/firebrand.yml", false)
        plugin.saveResource("loot-tables/firebrand.yml", false)
        return reload()
    }

    fun reload(): List<String> = registry.reload() + recipes.reload() + loot.reload()
    fun template(id: String): ItemTemplate? = registry.get(id)
    fun templates(): Collection<ItemTemplate> = registry.all()
    fun create(id: String): ItemStack? = registry.get(id)?.let(factory::create)
    fun has(id: String): Boolean = registry.contains(id)

    fun give(player: Player, id: String, amount: Int): Boolean {
        val template = registry.get(id) ?: return false
        if (amount !in 1..64) return false
        repeat(amount) {
            val overflow = player.inventory.addItem(factory.create(template))
            overflow.values.forEach { player.world.dropItemNaturally(player.location, it) }
        }
        return true
    }

    fun inspect(item: ItemStack): ItemIdentity? {
        val id = identity.id(item) ?: return null
        return ItemIdentity(id, identity.type(item), registry.get(id)?.version)
    }

    fun refreshTooltip(player: Player?, item: ItemStack) = ItemUtils.resetAndUpdateTooltip(player, item)
}

data class ItemIdentity(val id: String, val type: String?, val templateVersion: Int?)
