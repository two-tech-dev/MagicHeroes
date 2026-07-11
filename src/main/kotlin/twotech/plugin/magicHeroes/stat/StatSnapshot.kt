package twotech.plugin.magicHeroes.stat

data class StatSource(val source: String, val stat: StatType, val value: Double)

data class StatSnapshot(
    val values: Map<StatType, Double>,
    val sources: List<StatSource>
) {
    fun value(stat: StatType): Double = values[stat] ?: 0.0

    companion object {
        val EMPTY = StatSnapshot(emptyMap(), emptyList())
    }
}
