package twotech.plugin.magicHeroes.skill

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class CooldownManager {
    private val expiresAt = ConcurrentHashMap<Pair<UUID, String>, Long>()

    fun remaining(playerId: UUID, skillId: String, now: Long = System.currentTimeMillis()): Long =
        ((expiresAt[playerId to skillId] ?: 0L) - now).coerceAtLeast(0L)

    fun start(playerId: UUID, skillId: String, cooldownMillis: Long, now: Long = System.currentTimeMillis()) {
        if (cooldownMillis > 0) expiresAt[playerId to skillId] = now + cooldownMillis
    }

    fun clear(playerId: UUID) {
        expiresAt.keys.removeIf { it.first == playerId }
    }
}
