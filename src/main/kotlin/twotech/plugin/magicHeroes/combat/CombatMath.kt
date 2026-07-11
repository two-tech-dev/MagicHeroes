package twotech.plugin.magicHeroes.combat

object CombatMath {
    fun criticalDamage(base: Double, critical: Boolean, multiplier: Double): Double {
        require(base.isFinite() && base >= 0.0) { "base must be finite and non-negative" }
        require(multiplier.isFinite() && multiplier >= 1.0) { "critical multiplier must be at least one" }
        return if (critical) base * multiplier else base
    }

    fun defenseDamage(base: Double, defense: Double, penetration: Double): Double {
        require(base.isFinite() && base >= 0.0) { "base must be finite and non-negative" }
        require(defense.isFinite() && defense >= 0.0) { "defense must be finite and non-negative" }
        require(penetration.isFinite() && penetration >= 0.0) { "penetration must be finite and non-negative" }
        val effectiveDefense = (defense - penetration).coerceAtLeast(0.0)
        return base * 100.0 / (100.0 + effectiveDefense)
    }

    fun elementalDamage(base: Double, resistance: Double): Double {
        require(base.isFinite() && base >= 0.0) { "base must be finite and non-negative" }
        require(resistance.isFinite()) { "resistance must be finite" }
        return base * (1.0 - resistance.coerceIn(-1.0, 0.95))
    }
}
