package twotech.plugin.magicHeroes.calculator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StatCalculatorTest {
    @Test
    fun `defense reduces damage deterministically`() {
        assertEquals(50.0, StatCalculator.calculateDamageAfterDefense(100.0, 100.0))
        assertEquals(100.0, StatCalculator.calculateDamageAfterDefense(100.0, 0.0))
    }

    @Test
    fun `defense rejects invalid inputs`() {
        assertFailsWith<IllegalArgumentException> { StatCalculator.calculateDamageAfterDefense(-1.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { StatCalculator.calculateDamageAfterDefense(1.0, Double.NaN) }
    }
}
