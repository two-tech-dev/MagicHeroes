package twotech.plugin.magicHeroes.loot

data class LootEntry(val itemId: String, val weight: Int, val amount: Int = 1)

data class LootTable(val id: String, val guaranteed: List<LootEntry>, val weighted: List<LootEntry>) {
    fun choose(random: java.util.Random): LootEntry? {
        val total = weighted.sumOf(LootEntry::weight)
        if (total <= 0) return null
        var roll = random.nextInt(total)
        for (entry in weighted) {
            roll -= entry.weight
            if (roll < 0) return entry
        }
        return null
    }
}
