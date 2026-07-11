package twotech.plugin.magicHeroes.loot

import org.bukkit.configuration.file.YamlConfiguration
import java.nio.file.Files
import java.nio.file.Path

class LootTableRegistry(private val directory: Path) {
    @Volatile private var tables: Map<String, LootTable> = emptyMap()
    fun get(id: String): LootTable? = tables[id.lowercase()]

    fun reload(): List<String> {
        Files.createDirectories(directory)
        val loaded = linkedMapOf<String, LootTable>()
        val errors = mutableListOf<String>()
        Files.walk(directory).use { paths ->
            paths.filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".yml") }.forEach { path ->
                val config = YamlConfiguration.loadConfiguration(path.toFile())
                val id = config.getString("id")?.lowercase()
                if (id.isNullOrBlank() || id in loaded) {
                    errors += "$path has invalid or duplicate id"
                    return@forEach
                }
                fun entries(key: String) = config.getMapList(key).mapNotNull { raw ->
                    val item = raw["item"]?.toString()?.lowercase() ?: return@mapNotNull null
                    val weight = (raw["weight"] as? Number)?.toInt() ?: 1
                    val amount = (raw["amount"] as? Number)?.toInt() ?: 1
                    if (weight <= 0 || amount <= 0) null else LootEntry(item, weight, amount)
                }
                val guaranteed = entries("guaranteed")
                val weighted = entries("weighted")
                if (guaranteed.isEmpty() && weighted.isEmpty()) {
                    errors += "$path has no entries"
                    return@forEach
                }
                loaded[id] = LootTable(id, guaranteed, weighted)
            }
        }
        if (errors.isEmpty()) tables = loaded.toMap()
        return errors
    }
}
