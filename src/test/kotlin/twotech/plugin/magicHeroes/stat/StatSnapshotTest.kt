package twotech.plugin.magicHeroes.stat

import kotlin.test.Test
import kotlin.test.assertEquals

class StatSnapshotTest {
    @Test
    fun `empty snapshot returns zero`() {
        assertEquals(0.0, StatSnapshot.EMPTY.value(StatType.ATTACK_DAMAGE))
    }
}
