package twotech.plugin.magicHeroes.data

import kotlin.test.Test
import kotlin.test.assertEquals

class HeroPlayerDataTest {
    @Test
    fun `experience can level multiple times`() {
        val data = HeroPlayerData("test", "00000000-0000-0000-0000-000000000001")
        assertEquals(2, data.addExperience(300))
        assertEquals(3, data.level)
        assertEquals(0, data.exp)
        assertEquals(2, data.attributePoints)
        assertEquals(2, data.skillPoints)
    }

    @Test
    fun `level cap prevents further level gain`() {
        val data = HeroPlayerData("test", "00000000-0000-0000-0000-000000000001", level = 2)
        assertEquals(0, data.addExperience(1000, levelCap = 2))
        assertEquals(2, data.level)
    }
}
