package twotech.plugin.magicHeroes.combat

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

open class MagicHeroesPreDamageEvent(val context: DamageContext) : Event(), Cancellable {
    private var cancel = false
    override fun isCancelled() = cancel || context.cancelled
    override fun setCancelled(value: Boolean) { cancel = value; if (value) context.cancelled = true }
    override fun getHandlers() = handlerList
    companion object { @JvmStatic val handlerList = HandlerList() }
}

class MagicHeroesPostDamageEvent(val context: DamageContext) : Event() {
    override fun getHandlers() = handlerList
    companion object { @JvmStatic val handlerList = HandlerList() }
}

class MagicHeroesCriticalHitEvent(val context: DamageContext) : Event(), Cancellable {
    private var cancel = false
    override fun isCancelled() = cancel
    override fun setCancelled(value: Boolean) { cancel = value }
    override fun getHandlers() = handlerList
    companion object { @JvmStatic val handlerList = HandlerList() }
}

class MagicHeroesHealEvent(val target: org.bukkit.entity.Player, var amount: Double) : Event(), Cancellable {
    private var cancel = false
    override fun isCancelled() = cancel
    override fun setCancelled(value: Boolean) { cancel = value }
    override fun getHandlers() = handlerList
    companion object { @JvmStatic val handlerList = HandlerList() }
}

class MagicHeroesKillEvent(val context: DamageContext) : Event() {
    override fun getHandlers() = handlerList
    companion object { @JvmStatic val handlerList = HandlerList() }
}

class MagicHeroesDeathEvent(val context: DamageContext) : Event() {
    override fun getHandlers() = handlerList
    companion object { @JvmStatic val handlerList = HandlerList() }
}
