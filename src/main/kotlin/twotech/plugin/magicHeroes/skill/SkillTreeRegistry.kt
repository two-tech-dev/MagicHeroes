package twotech.plugin.magicHeroes.skill

import org.bukkit.configuration.file.YamlConfiguration
import java.nio.file.Files
import java.nio.file.Path

class SkillTreeRegistry(private val directory: Path) {
    @Volatile private var trees: Map<String, SkillTree> = emptyMap()
    fun get(classId: String): SkillTree? = trees[classId.lowercase()]

    fun reload(): List<String> {
        Files.createDirectories(directory)
        val loaded = mutableMapOf<String, SkillTree>()
        val errors = mutableListOf<String>()
        Files.walk(directory).use { paths ->
            paths.filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".yml") }.forEach { path ->
                val config = YamlConfiguration.loadConfiguration(path.toFile())
                val classId = config.getString("class")?.lowercase()
                if (classId.isNullOrBlank()) {
                    errors += "$path missing class"
                    return@forEach
                }
                val nodes = config.getConfigurationSection("nodes")?.getKeys(false)?.associateWith { id ->
                    val base = "nodes.$id"
                    SkillTreeNode(
                        id = id,
                        skillId = config.getString("$base.skill"),
                        parent = config.getString("$base.parent"),
                        incompatible = config.getStringList("$base.incompatible").toSet(),
                        maxLevel = config.getInt("$base.max-level", 1).coerceAtLeast(1),
                        cost = config.getInt("$base.cost", 1).coerceAtLeast(1)
                    )
                }.orEmpty()
                if (nodes.values.any { node -> node.parent != null && node.parent !in nodes }) {
                    errors += "$path references missing parent"
                    return@forEach
                }
                loaded[classId] = SkillTree(nodes)
            }
        }
        if (errors.isEmpty()) trees = loaded.toMap()
        return errors
    }
}
