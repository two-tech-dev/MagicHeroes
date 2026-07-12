package twotech.plugin.magicHeroes.combat

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CombatMathTest {
    @Test
    fun `critical damage applies configured multiplier`() {
        assertEquals(150.0, CombatMath.criticalDamage(100.0, true, 1.5))
        assertEquals(100.0, CombatMath.criticalDamage(100.0, false, 1.5))
    }

    @Test
    fun `penetration reduces effective defense`() {
        assertEquals(50.0, CombatMath.defenseDamage(100.0, 100.0, 0.0))
        assertEquals(100.0, CombatMath.defenseDamage(100.0, 100.0, 100.0))
    }

    @Test
    fun `elemental resistance clamps safe range`() {
        assertEquals(50.0, CombatMath.elementalDamage(100.0, 0.5))
        assertEquals(5.0, CombatMath.elementalDamage(100.0, 99.0), 1e-12)
    }

    @Test
    fun `combat math rejects invalid values`() {
        assertFailsWith<IllegalArgumentException> { CombatMath.defenseDamage(1.0, -1.0, 0.0) }
    }
}
