package twotech.plugin.magicHeroes.skill

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SkillTreeTest {
    private val tree = SkillTree(
        mapOf(
            "root" to SkillTreeNode("root", skillId = "slash"),
            "child" to SkillTreeNode("child", parent = "root", cost = 2),
            "choice" to SkillTreeNode("choice", incompatible = setOf("root"))
        )
    )

    @Test
    fun `tree enforces parent points and incompatibility`() {
        assertTrue(tree.canUnlock(emptyMap(), "root", 1))
        assertFalse(tree.canUnlock(emptyMap(), "child", 2))
        assertTrue(tree.canUnlock(mapOf("root" to 1), "child", 2))
        assertFalse(tree.canUnlock(mapOf("root" to 1), "choice", 1))
    }
}
