package twotech.plugin.magicHeroes.loot

import java.util.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class LootTableTest {
    @Test
    fun `weighted selection deterministic with seed`() {
        val table = LootTable(
            "test",
            emptyList(),
            listOf(LootEntry("common", 1), LootEntry("rare", 1))
        )
        assertNotNull(table.choose(Random(1)))
    }

    @Test
    fun `empty weights return no entry`() {
        assertEquals(null, LootTable("empty", emptyList(), emptyList()).choose(Random(1)))
    }
}
