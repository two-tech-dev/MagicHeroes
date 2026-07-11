package twotech.plugin.magicHeroes.crafting

import org.bukkit.configuration.file.YamlConfiguration
import java.nio.file.Files
import java.nio.file.Path

class RecipeRegistry(private val directory: Path) {
    @Volatile private var recipes: Map<String, Recipe> = emptyMap()
    fun get(id: String): Recipe? = recipes[id.lowercase()]
    fun all(): Collection<Recipe> = recipes.values

    fun reload(): List<String> {
        Files.createDirectories(directory)
        val loaded = linkedMapOf<String, Recipe>()
        val errors = mutableListOf<String>()
        Files.walk(directory).use { paths ->
            paths.filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".yml") }.forEach { path ->
                val config = YamlConfiguration.loadConfiguration(path.toFile())
                val id = config.getString("id")?.lowercase()
                val result = config.getString("result")?.lowercase()
                val amount = config.getInt("amount", 1)
                val chance = config.getDouble("success-chance", 1.0)
                if (id.isNullOrBlank() || result.isNullOrBlank() || amount <= 0 || chance !in 0.0..1.0) {
                    errors += "$path has invalid recipe fields"
                    return@forEach
                }
                if (id in loaded) {
                    errors += "$path duplicates recipe $id"
                    return@forEach
                }
                val ingredients = config.getConfigurationSection("ingredients")?.getKeys(false)?.associateWith { key ->
                    config.getInt("ingredients.$key", 0)
                }.orEmpty()
                if (ingredients.values.any { it <= 0 }) {
                    errors += "$path has invalid ingredient amount"
                    return@forEach
                }
                loaded[id] = Recipe(id, result, amount, ingredients, config.getString("station"), chance)
            }
        }
        if (errors.isEmpty()) recipes = loaded.toMap()
        return errors
    }
}
