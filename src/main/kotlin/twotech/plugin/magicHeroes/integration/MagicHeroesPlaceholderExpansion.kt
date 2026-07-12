package twotech.plugin.magicHeroes.integration

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import twotech.plugin.magicHeroes.MagicHeroes
import twotech.plugin.magicHeroes.manager.HeroPlayerManager

class MagicHeroesPlaceholderExpansion(private val plugin: MagicHeroes) : PlaceholderExpansion() {
    override fun getIdentifier(): String = "magicheroes"
    override fun getAuthor(): String = plugin.pluginMeta.authors.joinToString().ifBlank { "twotech" }
    override fun getVersion(): String = plugin.pluginMeta.version
    override fun persist(): Boolean = true

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        val playerId = player?.uniqueId ?: return null
        val data = HeroPlayerManager.get()?.getPlayerData(playerId) ?: return null
        return when (params.lowercase()) {
            "class" -> data.rpgClass?.id ?: "none"
            "level" -> data.level.toString()
            "exp" -> data.exp.toString()
            "mana" -> "%.0f".format(data.currentMana)
            "max_mana" -> "%.0f".format(data.getTotalMaxMana())
            "damage" -> "%.0f".format(data.getTotalDamage())
            "defense" -> "%.0f".format(data.getTotalDefense())
            "kills" -> data.killCount.toString()
            "deaths" -> data.deathCount.toString()
            else -> null
        }
    }
}
