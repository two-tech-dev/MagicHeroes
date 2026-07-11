package twotech.plugin.magicHeroes.quest

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class QuestProgressTest {
    @Test
    fun `quest progress accepts positive increments only`() {
        val objective = QuestObjective("zombie", QuestObjectiveType.KILL, "ZOMBIE", 2)
        val progress = QuestProgress("first-blood")
        assertFalse(progress.advance(objective, 0))
        assertTrue(progress.advance(objective, 1))
    }
}
