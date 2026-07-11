package twotech.plugin.magicHeroes.api

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import twotech.plugin.magicHeroes.combat.DamageContext
import twotech.plugin.magicHeroes.combat.CombatService
import twotech.plugin.magicHeroes.item.ItemIdentity
import twotech.plugin.magicHeroes.item.ItemService
import twotech.plugin.magicHeroes.manager.HeroPlayerManager
import twotech.plugin.magicHeroes.resource.ResourceService
import twotech.plugin.magicHeroes.skill.Skill
import twotech.plugin.magicHeroes.skill.SkillService
import java.nio.charset.StandardCharsets
import java.util.UUID

class MagicHeroesApiImpl(
    private val plugin: JavaPlugin,
    private val items: ItemService,
    private val combat: CombatService,
    private val skills: SkillService,
    private val resources: ResourceService
) : MagicHeroesApi {
    private val owner = UUID.nameUUIDFromBytes("MagicHeroes:${plugin.name}".toByteArray(StandardCharsets.UTF_8))
    private val skillOwners = java.util.concurrent.ConcurrentHashMap<String, UUID>()

    override fun itemIdentity(item: ItemStack): ItemIdentity? = items.inspect(item)
    override fun playerLevel(player: Player): Int? = HeroPlayerManager.get()?.getPlayerData(player.uniqueId)?.level

    override fun grantExperience(player: Player, amount: Int): Int {
        if (amount <= 0) return 0
        return HeroPlayerManager.get()?.getPlayerData(player.uniqueId)?.addExperience(amount) ?: 0
    }

    override fun mana(player: Player): Double? = resources.currentMana(player)
    override fun consumeMana(player: Player, amount: Double): Boolean = resources.spendMana(player, amount)

    override fun damage(context: DamageContext): Boolean = combat.damage(context)

    override fun registerSkill(skill: Skill): Boolean {
        val registered = skills.register(skill)
        if (registered) skillOwners[skill.id.lowercase()] = owner
        return registered
    }

    override fun unregisterSkill(id: String): Boolean {
        val removed = skills.unregister(id)
        if (removed) skillOwners.remove(id.lowercase())
        return removed
    }

    override fun registeredOwner(id: String): UUID? = skillOwners[id.lowercase()]
}
