package twotech.plugin.magicHeroes.item.set

import org.bukkit.configuration.file.YamlConfiguration
import java.nio.file.Files
import java.nio.file.Path

data class SetBonus(val pieces: Int, val stats: Map<String, Double>)
data class ItemSet(val id: String, val bonuses: List<SetBonus>)

class SetRegistry(private val directory: Path) {
    @Volatile private var sets: Map<String, ItemSet> = emptyMap()

    fun bonuses(id: String, pieces: Int): List<SetBonus> = sets[id.lowercase()]?.bonuses?.filter { pieces >= it.pieces }.orEmpty()

    fun reload(): List<String> {
        Files.createDirectories(directory)
        val loaded = linkedMapOf<String, ItemSet>()
        val errors = mutableListOf<String>()
        Files.walk(directory).use { paths ->
            paths.filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".yml") }.forEach { path ->
                val config = YamlConfiguration.loadConfiguration(path.toFile())
                val id = config.getString("id")?.lowercase()
                if (id.isNullOrBlank() || id in loaded) {
                    errors += "$path has invalid or duplicate id"
                    return@forEach
                }
                val bonuses = config.getConfigurationSection("bonuses")?.getKeys(false)?.mapNotNull { required ->
                    val pieces = required.toIntOrNull()
                    val stats = config.getConfigurationSection("bonuses.$required")?.getKeys(false)?.associateWith { key ->
                        config.getDouble("bonuses.$required.$key")
                    }.orEmpty()
                    if (pieces == null || pieces <= 0 || stats.any { !it.value.isFinite() }) {
                        errors += "$path has invalid bonus $required"
                        null
                    } else SetBonus(pieces, stats)
                }.orEmpty()
                if (bonuses.isEmpty()) {
                    errors += "$path has no bonuses"
                    return@forEach
                }
                loaded[id] = ItemSet(id, bonuses.sortedBy(SetBonus::pieces))
            }
        }
        if (errors.isEmpty()) sets = loaded.toMap()
        return errors
    }
}
