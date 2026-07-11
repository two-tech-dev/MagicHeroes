package twotech.plugin.magicHeroes.party

import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class Party(val leader: UUID, val members: MutableSet<UUID> = mutableSetOf(leader))

class PartyService {
    private val parties = ConcurrentHashMap<UUID, Party>()
    private val invitations = ConcurrentHashMap<UUID, UUID>()

    fun invite(leader: Player, target: Player): Boolean {
        val existing = partyOf(leader.uniqueId)
        if (existing != null && existing.leader != leader.uniqueId) return false
        if (partyOf(target.uniqueId) != null) return false
        parties.computeIfAbsent(leader.uniqueId) { Party(leader.uniqueId) }
        invitations[target.uniqueId] = leader.uniqueId
        return true
    }

    fun accept(player: Player): Boolean {
        val leader = invitations.remove(player.uniqueId) ?: return false
        val party = parties.computeIfAbsent(leader) { Party(leader) }
        party.members += player.uniqueId
        return true
    }

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
    fun clear(playerId: UUID) { invitations.remove(playerId); leaveId(playerId) }
    private fun leaveId(playerId: UUID) { partyOf(playerId)?.let { party -> party.members -= playerId; if (party.members.isEmpty()) parties.remove(party.leader) } }
}
