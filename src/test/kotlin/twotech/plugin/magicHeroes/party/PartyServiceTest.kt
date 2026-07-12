package twotech.plugin.magicHeroes.party

import org.bukkit.entity.Player
import java.lang.reflect.Proxy
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PartyServiceTest {
    @Test
    fun `new party starts with leader`() {
        val leader = java.util.UUID.randomUUID()
        assertEquals(setOf(leader), Party(leader).members)
    }

    @Test
    fun `member ids returns solo player without party`() {
        val player = java.util.UUID.randomUUID()

        assertEquals(setOf(player), PartyService().memberIds(player))
    }

    @Test
    fun `party rejects invitation when full`() {
        val leader = player()
        val first = player()
        val second = player()
        val service = PartyService(2)

        assertTrue(service.invite(leader, first))
        assertTrue(service.accept(first))
        assertFalse(service.invite(leader, second))
    }

    @Test
    fun `member ids returns party snapshot`() {
        val leader = java.util.UUID.randomUUID()
        val member = java.util.UUID.randomUUID()
        val party = Party(leader)
        party.members += member

        val service = PartyService()
        val partiesField = PartyService::class.java.getDeclaredField("parties")
        partiesField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val parties = partiesField.get(service) as MutableMap<java.util.UUID, Party>
        parties[leader] = party

        assertEquals(setOf(leader, member), service.memberIds(member))
    }

    private fun player(id: UUID = UUID.randomUUID()): Player = Proxy.newProxyInstance(
        Player::class.java.classLoader,
        arrayOf(Player::class.java)
    ) { _, method, _ ->
        when (method.name) {
            "getUniqueId" -> id
            "getName" -> id.toString()
            "hashCode" -> id.hashCode()
            "equals" -> false
            "toString" -> id.toString()
            else -> throw UnsupportedOperationException(method.name)
        }
    } as Player
}
