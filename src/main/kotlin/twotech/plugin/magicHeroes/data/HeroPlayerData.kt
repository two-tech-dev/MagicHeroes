package twotech.plugin.magicHeroes.data

import twotech.plugin.magicHeroes.attribute.AttributeType
import twotech.plugin.magicHeroes.rpg.RPGClass

/** Runtime profile. Bukkit owns current health; this class owns non-vanilla RPG state. */
data class HeroPlayerData(
    val playerName: String,
    val playerUUID: String,
    var rpgClass: RPGClass? = null,
    var level: Int = 1,
    var exp: Int = 0,
    var attributePoints: Int = 0,
    var skillPoints: Int = 0,
    val attributes: MutableMap<AttributeType, Int> = mutableMapOf(),
    val unlockedSkills: MutableSet<String> = mutableSetOf(),
    val skillBindings: MutableMap<Int, String> = mutableMapOf(),
    val skillTreeLevels: MutableMap<String, Int> = mutableMapOf(),
    var maxHealth: Double = 20.0,
    var maxMana: Double = 100.0,
    var baseDefense: Double = 0.0,
    var baseHealthRegen: Double = 0.5,
    var baseManaRegen: Double = 5.0,
    var armorHealth: Double = 0.0,
    var armorMana: Double = 0.0,
    var armorDamage: Double = 0.0,
    var armorDefense: Double = 0.0,
    var armorHealthRegen: Double = 0.0,
    var armorManaRegen: Double = 0.0,
    var killCount: Int = 0,
    var deathCount: Int = 0
) {
    private var _currentMana: Double = maxMana

    fun getTotalMaxHealth(): Double = (maxHealth + armorHealth + attribute(AttributeType.VITALITY) * 2.0).coerceAtLeast(1.0)
    fun getTotalMaxMana(): Double = (maxMana + armorMana + attribute(AttributeType.INTELLIGENCE) * 2.0).coerceAtLeast(0.0)
    fun getTotalDamage(): Double = armorDamage + attribute(AttributeType.STRENGTH)
    fun getTotalDefense(): Double = (baseDefense + armorDefense + attribute(AttributeType.VITALITY)).coerceAtLeast(0.0)
    fun getTotalHealthRegen(): Double = (baseHealthRegen + armorHealthRegen).coerceAtLeast(0.0)
    fun getTotalManaRegen(): Double = (baseManaRegen + armorManaRegen + attribute(AttributeType.WISDOM) * 0.1).coerceAtLeast(0.0)
    fun attribute(type: AttributeType): Int = attributes[type] ?: 0

    fun resetEquipmentStats() {
        armorHealth = 0.0
        armorMana = 0.0
        armorDamage = 0.0
        armorDefense = 0.0
        armorHealthRegen = 0.0
        armorManaRegen = 0.0
    }

    var currentMana: Double
        get() = _currentMana
        set(value) { _currentMana = value.coerceIn(0.0, getTotalMaxMana()) }

    fun calculateBaseStats() {
        val clazz = rpgClass ?: return
        maxHealth = clazz.getHealthAtLevel(level)
        maxMana = clazz.getManaAtLevel(level)
        baseDefense = clazz.getDefenseAtLevel(level)
        currentMana = currentMana
    }

    fun getLevelUpThreshold(): Int = level.coerceAtLeast(1) * 100

    fun addExperience(amount: Int, levelCap: Int = 100): Int {
        if (amount <= 0 || level >= levelCap) return 0
        exp += amount
        var gainedLevels = 0
        while (level < levelCap && exp >= getLevelUpThreshold()) {
            exp -= getLevelUpThreshold()
            level += 1
            attributePoints += 1
            skillPoints += 1
            calculateBaseStats()
            gainedLevels += 1
        }
        if (level >= levelCap) exp = exp.coerceAtMost(getLevelUpThreshold() - 1)
        if (gainedLevels > 0) currentMana = getTotalMaxMana()
        return gainedLevels
    }
}
