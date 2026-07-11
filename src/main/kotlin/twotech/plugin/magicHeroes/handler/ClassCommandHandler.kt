package twotech.plugin.magicHeroes.handler

import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import twotech.plugin.magicHeroes.manager.ClassManager
import twotech.plugin.magicHeroes.manager.HeroPlayerManager
import twotech.plugin.magicHeroes.manager.LanguageManager

/**
 * Handler for Sub-Commands: /mh class set and /mh class levelup
 */
object ClassCommandHandler {
    
    /**
     * Handle /mh class <subcommand>
     */
    fun execute(player: Player, args: Array<out String>) {
        val langManager = LanguageManager.get()
        
        if (args.size < 2) {
            player.sendMessage("§cUsage: /mh class <set|levelup> ...")
            return
        }
        
        when (args[1].lowercase()) {
            "set" -> handleSetClass(player, args)
            "levelup" -> handleLevelUp(player, args)
            else -> player.sendMessage("§cInvalid subcommand. Use: set, levelup")
        }
    }
    
    /**
     * Handle /mh class set <player> <class_id>
     */
    private fun handleSetClass(player: Player, args: Array<out String>) {
        if (args.size < 4) {
            player.sendMessage("§cUsage: /mh class set <player> <class_id>")
            return
        }
        
        val targetName = args[2]
        val classId = args[3]
        
        val target = Bukkit.getPlayer(targetName)
        if (target == null) {
            player.sendMessage("§cPlayer not found: $targetName")
            return
        }
        
        val classManager = ClassManager.get()
        val playerManager = HeroPlayerManager.get()
        
        if (classManager == null || playerManager == null) {
            player.sendMessage("§cClass system not initialized!")
            return
        }
        
        val rpgClass = classManager.getClass(classId)
        if (rpgClass == null) {
            player.sendMessage("§cClass not found: $classId")
            player.sendMessage("§7Available classes: ${classManager.getAvailableClassIds().joinToString(", ")}")
            return
        }
        
        val data = playerManager.getPlayerData(target.uniqueId)
        if (data == null) {
            player.sendMessage("§cPlayer data not found!")
            return
        }
        
        // Set new class
        data.rpgClass = rpgClass
        data.level = 1
        data.exp = 0
        
        // Recalculate base stats
        data.calculateBaseStats()
        
        // Restore full HP and Mana
        data.currentMana = data.getTotalMaxMana()
        
        player.sendMessage("§aSet class for ${target.name} to: ${rpgClass.displayName}")
        target.sendMessage("§aYour class has been set to: ${rpgClass.displayName}")
        
        // Effects
        target.world.spawnParticle(Particle.ENCHANT, target.location, 50, 0.5, 1.0, 0.5)
        target.playSound(target.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f)
    }
    
    /**
     * Handle /mh class levelup <player>
     */
    private fun handleLevelUp(player: Player, args: Array<out String>) {
        if (args.size < 3) {
            player.sendMessage("§cUsage: /mh class levelup <player>")
            return
        }
        
        val targetName = args[2]
        val target = Bukkit.getPlayer(targetName)
        
        if (target == null) {
            player.sendMessage("§cPlayer not found: $targetName")
            return
        }
        
        val playerManager = HeroPlayerManager.get()
        if (playerManager == null) {
            player.sendMessage("§cPlayer manager not initialized!")
            return
        }
        
        val data = playerManager.getPlayerData(target.uniqueId)
        if (data == null) {
            player.sendMessage("§cPlayer data not found!")
            return
        }
        
        // Level up
        data.level += 1
        data.calculateBaseStats()
        
        // Restore full HP and Mana
        data.currentMana = data.getTotalMaxMana()
        
        player.sendMessage("§a${target.name} leveled up to level ${data.level}!")
        target.sendMessage("§e§l[LEVEL UP!] §r§aYou are now level ${data.level}!")
        
        // Level Up effects
        target.world.spawnParticle(Particle.FIREWORK, target.location, 100, 0.5, 1.0, 0.5, 0.1)
        target.playSound(target.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f)
    }
}
