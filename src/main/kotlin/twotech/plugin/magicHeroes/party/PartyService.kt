package twotech.plugin.magicHeroes.party

import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class Party(val leader: UUID, val members: MutableSet<UUID> = mutableSetOf(leader))

class PartyService(maxSize: Int = 5) {
    private val parties = ConcurrentHashMap<UUID, Party>()
    private val invitations = ConcurrentHashMap<UUID, UUID>()
    private var maxSize = 5

    init {
        configure(maxSize)
    }

    fun configure(maxSize: Int) {
        this.maxSize = maxSize.coerceAtLeast(2)
    }

    fun invite(leader: Player, target: Player): Boolean {
        val existing = partyOf(leader.uniqueId)
        if (existing != null && existing.leader != leader.uniqueId) return false
        if (existing != null && existing.members.size >= maxSize) return false
        if (partyOf(target.uniqueId) != null) return false
        parties.computeIfAbsent(leader.uniqueId) { Party(leader.uniqueId) }
        invitations[target.uniqueId] = leader.uniqueId
        return true
    }

    fun accept(player: Player): Boolean {
        val leader = invitations[player.uniqueId] ?: return false
        val party = parties[leader] ?: return false
        if (party.members.size >= maxSize) return false
        invitations.remove(player.uniqueId)
        party.members += player.uniqueId
        return true
    }

    fun kick(leader: Player, target: Player): Boolean {
        val party = partyOf(leader.uniqueId) ?: return false
        if (party.leader != leader.uniqueId || target.uniqueId == leader.uniqueId || target.uniqueId !in party.members) return false
        party.members -= target.uniqueId
        return true
    }

    fun disband(leader: Player): Boolean {
        val party = partyOf(leader.uniqueId) ?: return false
        if (party.leader != leader.uniqueId) return false
        parties.remove(party.leader)
        invitations.entries.removeIf { it.value == leader.uniqueId }
        return true
    }

    fun isLeader(playerId: UUID): Boolean = partyOf(playerId)?.leader == playerId

    fun leave(player: Player): Boolean {
        val party = partyOf(player.uniqueId) ?: return false
        party.members -= player.uniqueId
        if (party.members.isEmpty()) parties.remove(party.leader)
        else if (party.leader == player.uniqueId) {
            parties.remove(party.leader)
            val nextLeader = party.members.first()
            parties[nextLeader] = Party(nextLeader, party.members.toMutableSet())
        }
        return true
    }

    fun partyOf(playerId: UUID): Party? = parties.values.firstOrNull { playerId in it.members }
    fun memberIds(playerId: UUID): Set<UUID> = partyOf(playerId)?.members?.toSet() ?: setOf(playerId)
    fun clear(playerId: UUID) { invitations.remove(playerId); leaveId(playerId) }
    private fun leaveId(playerId: UUID) { partyOf(playerId)?.let { party -> party.members -= playerId; if (party.members.isEmpty()) parties.remove(party.leader) } }
}
