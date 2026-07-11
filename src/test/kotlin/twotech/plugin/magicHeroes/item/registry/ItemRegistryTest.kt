package twotech.plugin.magicHeroes.item.registry

import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ItemRegistryTest {
    @Test
    fun `valid template loads and invalid reload keeps previous snapshot`() {
        val dir = createTempDirectory("items")
        dir.resolve("good.yml").writeText(
            """
            id: firebrand
            type: sword
            material: DIAMOND_SWORD
            display-name: Firebrand
            base-stats:
              ATTACK_DAMAGE: 25
            """.trimIndent()
        )
        val registry = ItemRegistry(dir)
        assertTrue(registry.reload().isEmpty())
        assertNotNull(registry.get("FIREBRAND"))
        dir.resolve("bad.yml").writeText("""
            id: bad
            type: sword
            material: NOPE
        """.trimIndent())
        assertTrue(registry.reload().isNotEmpty())
        assertEquals("firebrand", registry.get("firebrand")?.id)
    }
}
