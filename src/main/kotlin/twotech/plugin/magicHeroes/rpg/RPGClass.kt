package twotech.plugin.magicHeroes.rpg

/**
 * Represents an RPG Class (Warrior, Mage, Archer, etc.)
 */
data class RPGClass(
    val id: String,
    val displayName: String,
    
    // Base stats at level 1
    val baseHealth: Double,
    val baseMana: Double,
    val baseDefense: Double,
    
    // Stats scaling per level
    val healthPerLevel: Double,
    val manaPerLevel: Double,
    val defensePerLevel: Double
) {
    
    /**
     * Calculate max HP at a specific level
     */
    fun getHealthAtLevel(level: Int): Double {
        return baseHealth + (level - 1) * healthPerLevel
    }
    
    /**
     * Calculate max Mana at a specific level
     */
    fun getManaAtLevel(level: Int): Double {
        return baseMana + (level - 1) * manaPerLevel
    }
    
    /**
     * Calculate Defense at a specific level
     */
    fun getDefenseAtLevel(level: Int): Double {
        return baseDefense + (level - 1) * defensePerLevel
    }
}
