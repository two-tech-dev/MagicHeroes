package twotech.plugin.magicHeroes.data

data class HeroPlayerSnapshot(
    val playerName: String,
    val playerUuid: String,
    val classId: String?,
    val level: Int,
    val experience: Int,
    val attributePoints: Int = 0,
    val skillPoints: Int = 0,
    val attributes: Map<String, Int> = emptyMap(),
    val unlockedSkills: Set<String> = emptySet(),
    val skillBindings: Map<Int, String> = emptyMap(),
    val skillTreeLevels: Map<String, Int> = emptyMap(),
    val maxMana: Double,
    val baseDefense: Double,
    val baseHealthRegen: Double,
    val baseManaRegen: Double,
    val currentHealth: Double?,
    val currentMana: Double,
    val killCount: Int,
    val deathCount: Int,
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
    }
}
