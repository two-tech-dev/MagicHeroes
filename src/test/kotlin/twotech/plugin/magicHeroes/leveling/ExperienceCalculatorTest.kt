package twotech.plugin.magicHeroes.leveling

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExperienceCalculatorTest {
    @Test
    fun `linear curve returns level times base`() {
        assertEquals(100, ExperienceCalculator.requiredForLevel(1))
        assertEquals(500, ExperienceCalculator.requiredForLevel(5))
    }

    @Test
    fun `exponential curve scales by growth`() {
        val level3 = ExperienceCalculator.requiredForLevel(3, ExperienceCurve.EXPONENTIAL, base = 100, growth = 2.0)
        assertEquals(400, level3)
    }

    @Test
    fun `rejects invalid level`() {
        assertFailsWith<IllegalArgumentException> { ExperienceCalculator.requiredForLevel(0) }
    }
}
