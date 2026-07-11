package twotech.plugin.magicHeroes.party

import kotlin.test.Test
import kotlin.test.assertEquals

class PartyServiceTest {
    @Test
    fun `new party starts with leader`() {
        val leader = java.util.UUID.randomUUID()
        assertEquals(setOf(leader), Party(leader).members)
    }
}
