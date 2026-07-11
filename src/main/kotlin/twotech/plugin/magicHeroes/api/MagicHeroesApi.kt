package twotech.plugin.magicHeroes.api

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import twotech.plugin.magicHeroes.combat.DamageContext
import twotech.plugin.magicHeroes.item.ItemIdentity
import twotech.plugin.magicHeroes.skill.Skill
import java.util.UUID

interface MagicHeroesApi {
    fun itemIdentity(item: ItemStack): ItemIdentity?
    fun playerLevel(player: Player): Int?
    fun grantExperience(player: Player, amount: Int): Int
    fun mana(player: Player): Double?
    fun consumeMana(player: Player, amount: Double): Boolean
    fun damage(context: DamageContext): Boolean
    fun registerSkill(skill: Skill): Boolean
    fun unregisterSkill(id: String): Boolean
    fun registeredOwner(id: String): UUID?
}
