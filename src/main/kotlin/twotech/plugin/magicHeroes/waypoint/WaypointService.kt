package twotech.plugin.magicHeroes.waypoint

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class WaypointService(private val plugin: JavaPlugin) {
    private val discovered = ConcurrentHashMap<UUID, MutableSet<String>>()
    private val locations = ConcurrentHashMap<String, Location>()
    private val cooldowns = ConcurrentHashMap<UUID, Long>()
    private var cooldownMillis = 5_000L

    fun initialize(): List<String> {
        plugin.saveResource("waypoints.yml", false)
        return reload()
    }

    fun reload(): List<String> {
        locations.clear()
        cooldownMillis = plugin.config.getLong("waypoints.teleport-cooldown-seconds", 5).coerceAtLeast(0) * 1_000L
        val config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(java.io.File(plugin.dataFolder, "waypoints.yml"))
        val section = config.getConfigurationSection("waypoints") ?: return emptyList()
        val errors = mutableListOf<String>()
        section.getKeys(false).forEach { id ->
            val path = "waypoints.$id"
            val world = plugin.server.getWorld(config.getString("$path.world").orEmpty())
            val x = config.getDouble("$path.x")
            val y = config.getDouble("$path.y")
            val z = config.getDouble("$path.z")
            if (world == null || !config.contains("$path.x") || !config.contains("$path.y") || !config.contains("$path.z")) errors += "$path has invalid world or coordinates"
            else register(id, Location(world, x, y, z, config.getDouble("$path.yaw").toFloat(), config.getDouble("$path.pitch").toFloat()))
        }
        return errors
    }

    fun register(id: String, location: Location) { locations[id.lowercase()] = location.clone() }
    fun discover(player: Player, id: String): Boolean {
        val key = id.lowercase()
        if (!locations.containsKey(key)) return false
        discovered.computeIfAbsent(player.uniqueId) { mutableSetOf() }.add(key)
        twotech.plugin.magicHeroes.manager.HeroPlayerManager.get()?.getPlayerData(player.uniqueId)?.discoveredWaypoints?.add(key)
        return true
    }

    fun restore(player: Player, ids: Collection<String>) {
        discovered.computeIfAbsent(player.uniqueId) { mutableSetOf() }.addAll(ids.map(String::lowercase))
    }
    fun teleport(player: Player, id: String, cost: Double = 0.0): Boolean {
        val key = id.lowercase()
        if (key !in discovered[player.uniqueId].orEmpty()) return false
        val location = locations[key] ?: return false
        if (!isSafe(location)) return false
        val now = System.currentTimeMillis()
        if ((cooldowns[player.uniqueId] ?: 0L) > now) return false
        cooldowns[player.uniqueId] = now + cooldownMillis
        player.teleportAsync(location)
        return true
    }

    fun discovered(player: Player): Set<String> = discovered[player.uniqueId].orEmpty()
    fun clear(playerId: UUID) { discovered.remove(playerId); cooldowns.remove(playerId) }

    private fun isSafe(location: Location): Boolean {
        val world = location.world ?: return false
        val feet = world.getBlockAt(location)
        val head = world.getBlockAt(location.clone().add(0.0, 1.0, 0.0))
        val ground = world.getBlockAt(location.clone().add(0.0, -1.0, 0.0))
        return feet.isPassable && head.isPassable && ground.type.isSolid
    }
}
