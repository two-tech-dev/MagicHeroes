package twotech.plugin.magicHeroes.storage

import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import twotech.plugin.magicHeroes.data.HeroPlayerSnapshot
import java.util.UUID

class YamlPlayerStorageTest {
    @Test
    fun `profile round trip persists schema and health`() {
        val directory = createTempDirectory("magicheroes-storage-test")
        val playerId = UUID.randomUUID()
        val storage = YamlPlayerStorage(directory)
        val snapshot = HeroPlayerSnapshot(
            playerName = "Hero",
            playerUuid = playerId.toString(),
            classId = "warrior",
            level = 4,
            experience = 22,
            attributePoints = 3,
            skillPoints = 2,
            attributes = mapOf("strength" to 4),
            maxMana = 90.0,
            baseDefense = 12.0,
            baseHealthRegen = 1.0,
            baseManaRegen = 2.0,
            currentHealth = 75.0,
            currentMana = 44.0,
            killCount = 3,
            deathCount = 1
        )
        storage.save(snapshot)
        val loaded = storage.load(playerId, "other")
        assertEquals(snapshot.copy(schemaVersion = 1), loaded)
        assertTrue(directory.resolve("$playerId.yml").toFile().exists())
    }
}
