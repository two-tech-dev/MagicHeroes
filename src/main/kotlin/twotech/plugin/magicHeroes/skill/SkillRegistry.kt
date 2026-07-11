package twotech.plugin.magicHeroes.skill

import org.bukkit.configuration.file.YamlConfiguration
import java.nio.file.Files
import java.nio.file.Path

class SkillRegistry(private val directory: Path) {
    private val idPattern = Regex("[a-z0-9][a-z0-9._-]*")
    @Volatile private var skills: Map<String, Skill> = emptyMap()

    fun get(id: String): Skill? = skills[id.lowercase()]
    fun ids(): Set<String> = skills.keys
    fun all(): Collection<Skill> = skills.values

    fun reload(): List<String> {
        Files.createDirectories(directory)
        val loaded = linkedMapOf<String, Skill>()
        val errors = mutableListOf<String>()
        Files.walk(directory).use { paths ->
            paths.filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".yml") }.forEach { path ->
                val config = YamlConfiguration.loadConfiguration(path.toFile())
                val id = config.getString("id")?.lowercase()
                if (id.isNullOrBlank() || !id.matches(idPattern)) {
                    errors += "$path has invalid id"
                    return@forEach
                }
                if (id in loaded) {
                    errors += "$path duplicates $id"
                    return@forEach
                }
                val mana = config.getDouble("cost.mana", 0.0)
                val cooldown = config.getLong("cooldown-ms", 0L)
                val cast = config.getLong("cast-ms", 0L)
                val power = config.getDouble("power", 0.0)
                if (!mana.isFinite() || mana < 0 || cooldown < 0 || cast < 0 || !power.isFinite()) {
                    errors += "$path has invalid numeric values"
                    return@forEach
                }
                val mechanics = config.getStringList("mechanics").mapNotNull { value ->
                    runCatching { SkillMechanic.valueOf(value.uppercase()) }.getOrNull()
                        ?: run { errors += "$path has invalid mechanic $value"; null }
                }
                val targetName = config.getString("target", "SELF") ?: "SELF"
                val target = runCatching { SkillTarget.valueOf(targetName.uppercase()) }.getOrElse {
                    errors += "$path has invalid target"; SkillTarget.SELF
                }
                if (errors.any { it.startsWith("$path ") }) return@forEach
                loaded[id] = Skill(
                    id = id,
                    displayName = config.getString("display-name", id) ?: id,
                    description = config.getString("description", "") ?: "",
                    manaCost = mana,
                    cooldownMillis = cooldown,
                    castMillis = cast,
                    range = config.getDouble("range", 0.0).coerceAtLeast(0.0),
                    target = target,
                    damageType = config.getString("damage-type"),
                    power = power,
                    mechanics = mechanics,
                    requiredLevel = config.getInt("requirements.level", 1).coerceAtLeast(1),
                    requiredClass = config.getString("requirements.class")?.lowercase()
                )
            }
        }
        if (errors.isEmpty()) skills = loaded.toMap()
        return errors
    }
}
