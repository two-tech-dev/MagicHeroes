package twotech.plugin.magicHeroes.item.template

data class ItemTemplate(
    val id: String,
    val type: String,
    val material: String,
    val displayName: String,
    val lore: List<String>,
    val baseStats: Map<String, Double>,
    val levelRequirement: Int = 0,
    val classRequirement: String? = null,
    val version: Int = 1,
    val maxDurability: Int? = null,
    val infiniteDurability: Boolean = false,
    val tier: String = "COMMON",
    val setId: String? = null,
    val socketCount: Int = 0
)
