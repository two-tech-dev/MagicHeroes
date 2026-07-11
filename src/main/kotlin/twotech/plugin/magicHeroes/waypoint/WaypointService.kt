package twotech.plugin.magicHeroes.waypoint

import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class WaypointService {
    private val discovered = ConcurrentHashMap<UUID, MutableSet<String>>()
    private val locations = ConcurrentHashMap<String, Location>()

    fun register(id: String, location: Location) { locations[id.lowercase()] = location.clone() }
    fun discover(player: Player, id: String): Boolean {
        val key = id.lowercase()
        if (key !in locations) return false
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
        return player.teleport(location)
    }
    fun discovered(player: Player): Set<String> = discovered[player.uniqueId].orEmpty()
    fun clear(playerId: UUID) { discovered.remove(playerId) }
}
