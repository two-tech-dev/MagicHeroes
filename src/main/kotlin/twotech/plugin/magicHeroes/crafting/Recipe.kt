package twotech.plugin.magicHeroes.crafting

data class Recipe(
    val id: String,
    val resultItem: String,
    val resultAmount: Int,
    val ingredients: Map<String, Int>,
    val station: String? = null,
    val successChance: Double = 1.0
)
