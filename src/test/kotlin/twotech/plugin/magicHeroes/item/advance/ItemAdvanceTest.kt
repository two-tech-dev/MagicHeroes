package twotech.plugin.magicHeroes.item.advance

import kotlin.test.Test
import kotlin.test.assertEquals

class ItemAdvanceTest {
    @Test
    fun `tier has stable defaults`() {
        assertEquals(ItemTier.COMMON, ItemTier.entries.first())
    }
}
