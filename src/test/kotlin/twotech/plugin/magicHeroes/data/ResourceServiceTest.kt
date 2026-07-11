package twotech.plugin.magicHeroes.data

import kotlin.test.Test
import kotlin.test.assertEquals

class ResourceServiceTest {
    @Test
    fun `mana regeneration uses elapsed seconds`() {
        val data = HeroPlayerData("test", "00000000-0000-0000-0000-000000000001", maxMana = 100.0, baseManaRegen = 5.0)
        data.currentMana = 0.0
        ResourceService().regenerate(data, 2.0)
        assertEquals(10.0, data.currentMana)
    }

    @Test
    fun `mana clamps at max`() {
        val data = HeroPlayerData("test", "00000000-0000-0000-0000-000000000001", maxMana = 100.0, baseManaRegen = 5.0)
        data.currentMana = 99.0
        ResourceService().regenerate(data, 2.0)
        assertEquals(100.0, data.currentMana)
    }
}
