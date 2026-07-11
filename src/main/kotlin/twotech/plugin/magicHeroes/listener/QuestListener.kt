package twotech.plugin.magicHeroes.listener

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import twotech.plugin.magicHeroes.quest.QuestObjectiveType
import twotech.plugin.magicHeroes.quest.QuestService

class QuestListener(private val quests: QuestService) : Listener {
    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val killer = event.entity.killer ?: return
        quests.progress(killer, QuestObjectiveType.KILL, event.entity.type.name)
    }
}
