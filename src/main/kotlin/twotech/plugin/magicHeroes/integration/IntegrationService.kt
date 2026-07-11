package twotech.plugin.magicHeroes.integration

import org.bukkit.plugin.PluginManager

enum class Integration { PLACEHOLDER_API, VAULT, MYTHIC_MOBS, CITIZENS, ITEMS_ADDER, ORAXEN, NEXO }

class IntegrationService(private val plugins: PluginManager) {
    private val pluginNames = mapOf(
        Integration.PLACEHOLDER_API to "PlaceholderAPI",
        Integration.VAULT to "Vault",
        Integration.MYTHIC_MOBS to "MythicMobs",
        Integration.CITIZENS to "Citizens",
        Integration.ITEMS_ADDER to "ItemsAdder",
        Integration.ORAXEN to "Oraxen",
        Integration.NEXO to "Nexo"
    )

    fun isAvailable(integration: Integration): Boolean = plugins.getPlugin(pluginNames.getValue(integration))?.isEnabled == true
    fun available(): Set<Integration> = Integration.entries.filterTo(mutableSetOf(), ::isAvailable)
}
