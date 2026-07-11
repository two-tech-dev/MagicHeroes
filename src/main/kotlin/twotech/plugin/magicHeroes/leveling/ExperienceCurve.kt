package twotech.plugin.magicHeroes.leveling

import kotlin.math.pow

enum class ExperienceCurve { LINEAR, EXPONENTIAL }

object ExperienceCalculator {
    fun requiredForLevel(level: Int, curve: ExperienceCurve = ExperienceCurve.LINEAR, base: Int = 100, growth: Double = 1.25): Int {
        require(level >= 1) { "level must be at least one" }
        require(base > 0) { "base must be positive" }
        require(growth >= 1.0 && growth.isFinite()) { "growth must be finite and at least one" }
        return when (curve) {
            ExperienceCurve.LINEAR -> Math.multiplyExact(level, base)
            ExperienceCurve.EXPONENTIAL -> (base * growth.pow((level - 1).toDouble())).toInt().coerceAtLeast(base)
        }
    }
}
