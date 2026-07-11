package twotech.plugin.magicHeroes.item.registry

import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import twotech.plugin.magicHeroes.item.template.ItemTemplate
import java.nio.file.Files
import java.nio.file.Path

class ItemRegistry(private val itemsDirectory: Path) {
    @Volatile
    private var templates: Map<String, ItemTemplate> = emptyMap()

    fun get(id: String): ItemTemplate? = templates[id.lowercase()]
    fun contains(id: String): Boolean = get(id) != null
    fun all(): Collection<ItemTemplate> = templates.values

    fun reload(): List<String> {
        val loaded = linkedMapOf<String, ItemTemplate>()
        val errors = mutableListOf<String>()
        if (Files.notExists(itemsDirectory)) {
            Files.createDirectories(itemsDirectory)
            templates = emptyMap()
            return errors
        }
        Files.walk(itemsDirectory).use { paths ->
            paths.filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".yml") }
                .forEach { path -> load(path, loaded, errors) }
        }
        if (errors.isEmpty()) templates = loaded.toMap()
        return errors
    }

    private fun load(path: Path, loaded: MutableMap<String, ItemTemplate>, errors: MutableList<String>) {
        val config = YamlConfiguration.loadConfiguration(path.toFile())
        val id = config.getString("id")?.trim()?.lowercase()
        val type = config.getString("type")?.trim()?.lowercase()
        val materialName = config.getString("material")?.trim()?.uppercase()
        if (id.isNullOrBlank() || !id.matches(Regex("[a-z0-9][a-z0-9._-]*")) || type.isNullOrBlank() || materialName.isNullOrBlank()) {
            errors += "$path requires valid id, type, material"
            return
        }
        if (loaded.containsKey(id)) {
            errors += "$path duplicates item id $id"
            return
        }
        if (Material.matchMaterial(materialName) == null) {
            errors += "$path has invalid material $materialName"
            return
        }
        val stats = config.getConfigurationSection("base-stats")?.getKeys(false)?.associateWith { key ->
            config.getDouble("base-stats.$key").also { value ->
                if (!value.isFinite()) errors += "$path has non-finite stat $key"
            }
        }.orEmpty()
        val level = config.getInt("requirements.level", 0)
        if (level < 0) errors += "$path has negative level requirement"
        val maxDurability = if (config.contains("durability.max")) config.getInt("durability.max") else null
        if (maxDurability != null && maxDurability <= 0) errors += "$path has non-positive durability.max"
        if (errors.any { it.startsWith("$path ") }) return
        loaded[id] = ItemTemplate(
            id = id,
            type = type,
            material = materialName,
            displayName = config.getString("display-name", id) ?: id,
            lore = config.getStringList("lore"),
            baseStats = stats,
            levelRequirement = level,
            classRequirement = config.getString("requirements.class")?.trim()?.lowercase(),
            version = config.getInt("version", 1).coerceAtLeast(1),
            maxDurability = maxDurability,
            infiniteDurability = config.getBoolean("durability.infinite", false),
            tier = config.getString("tier", "COMMON")?.uppercase() ?: "COMMON",
            setId = config.getString("set")?.lowercase(),
            socketCount = config.getInt("sockets", 0).coerceIn(0, 6)
        )
    }
}
