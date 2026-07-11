package twotech.plugin.magicHeroes.skill

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CooldownManagerTest {
    @Test
    fun `cooldown remaining and clear`() {
        val manager = CooldownManager()
        val id = UUID.randomUUID()
        manager.start(id, "slash", 1000, now = 1000)
        assertEquals(1000, manager.remaining(id, "slash", now = 1000))
        assertTrue(manager.remaining(id, "slash", now = 2001) == 0L)
        manager.clear(id)
        assertEquals(0, manager.remaining(id, "slash", now = 1000))
    }
}
