package twotech.plugin.magicHeroes.command

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import twotech.plugin.magicHeroes.MagicHeroes
import twotech.plugin.magicHeroes.attribute.AttributeService
import twotech.plugin.magicHeroes.attribute.AttributeType
import twotech.plugin.magicHeroes.calculator.StatCalculator
import twotech.plugin.magicHeroes.data.ResourceService
import twotech.plugin.magicHeroes.handler.DurabilityHandler
import twotech.plugin.magicHeroes.handler.EnchantmentHandler
import twotech.plugin.magicHeroes.handler.GUIHandler
import twotech.plugin.magicHeroes.handler.LoreHandler
import twotech.plugin.magicHeroes.handler.RenameHandler
import twotech.plugin.magicHeroes.handler.SetTooltipHandler
import twotech.plugin.magicHeroes.manager.ClassManager
import twotech.plugin.magicHeroes.manager.HeroPlayerManager
import twotech.plugin.magicHeroes.manager.LanguageManager
import twotech.plugin.magicHeroes.manager.TooltipManager
import twotech.plugin.magicHeroes.party.PartyService
import twotech.plugin.magicHeroes.quest.QuestService
import twotech.plugin.magicHeroes.skill.SkillCastResult
import twotech.plugin.magicHeroes.skill.SkillService
import twotech.plugin.magicHeroes.waypoint.WaypointService

class MagicHeroesCommand(
    private val plugin: MagicHeroes,
    private val resources: ResourceService,
    private val attributes: AttributeService,
    private val skills: SkillService,
    private val quests: QuestService,
    private val parties: PartyService,
    private val waypoints: WaypointService
) : CommandExecutor, TabCompleter {
    private data class CommandSpec(val name: String, val permission: String, val playerOnly: Boolean)

    private val commands = listOf(
        CommandSpec("help", "magicheroes.user", false),
        CommandSpec("language", "magicheroes.user", true),
        CommandSpec("stats", "magicheroes.user", true),
        CommandSpec("attributes", "magicheroes.user", true),
        CommandSpec("skills", "magicheroes.user", true),
        CommandSpec("skill", "magicheroes.user", true),
        CommandSpec("quests", "magicheroes.user", true),
        CommandSpec("quest", "magicheroes.user", true),
        CommandSpec("party", "magicheroes.user", true),
        CommandSpec("waypoint", "magicheroes.user", true),
        CommandSpec("level", "magicheroes.level.admin", false),
        CommandSpec("gui", "magicheroes.item.edit", true),
        CommandSpec("rename", "magicheroes.item.edit", true),
        CommandSpec("setlore", "magicheroes.item.edit", true),
        CommandSpec("addenchant", "magicheroes.item.edit", true),
        CommandSpec("removeenchant", "magicheroes.item.edit", true),
        CommandSpec("durability", "magicheroes.item.edit", true),
        CommandSpec("settooltip", "magicheroes.item.edit", true),
        CommandSpec("item", "magicheroes.user", false),
        CommandSpec("reload", "magicheroes.reload", false),
        CommandSpec("class", "magicheroes.class.admin", false),
        CommandSpec("debug", "magicheroes.debug", false)
    )

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) return sendHelp(sender)
        val spec = commands.firstOrNull { it.name == args[0].lowercase() } ?: run {
            sender.sendMessage(Component.text("Unknown subcommand: ${args[0]}"))
            return sendHelp(sender)
        }
        if (!sender.hasPermission(spec.permission)) return deny(sender, spec.permission)
        val player = sender as? Player
        if (spec.playerOnly && player == null) {
            sender.sendMessage(Component.text("This subcommand requires an in-game player."))
            return true
        }
        when (spec.name) {
            "help" -> sendHelp(sender)
            "language" -> handleLanguage(player!!, args)
            "stats" -> showStats(player!!)
            "attributes" -> handleAttributes(player!!, args)
            "skills" -> showSkills(player!!)
            "skill" -> handleSkill(player!!, args)
            "quests" -> showQuests(player!!)
            "quest" -> handleQuest(player!!, args)
            "party" -> handleParty(player!!, args)
            "waypoint" -> handleWaypoint(player!!, args)
            "level" -> handleLevel(sender, args)
            "item" -> handleItem(sender, player, args)
            "gui" -> GUIHandler.execute(player!!, args)
            "rename" -> RenameHandler.execute(player!!, args)
            "setlore" -> LoreHandler.execute(player!!, args)
            "addenchant" -> EnchantmentHandler.executeAdd(player!!, args)
            "removeenchant" -> EnchantmentHandler.executeRemove(player!!, args)
            "durability" -> DurabilityHandler.execute(player!!, args)
            "settooltip" -> SetTooltipHandler.execute(player!!, args)
            "reload" -> reload(sender)
            "class" -> handleClass(sender, args)
            "debug" -> sender.sendMessage(Component.text("MagicHeroes: ${Bukkit.getOnlinePlayers().size} online player(s)."))
        }
        return true
    }

    private fun handleLanguage(player: Player, args: Array<out String>) {
        val manager = LanguageManager.get() ?: return
        val code = args.getOrNull(1)
        if (code == null) {
            player.sendMessage(Component.text("Languages: ${manager.getAvailableLanguages().joinToString(", ")}"))
            return
        }
        if (!manager.setPlayerLanguage(player, code.lowercase())) player.sendMessage(Component.text("Unknown language: $code"))
        else player.sendMessage(Component.text("Language set to ${code.lowercase()}."))
    }

    private fun handleItem(sender: CommandSender, player: Player?, args: Array<out String>) {
        when (args.getOrNull(1)?.lowercase()) {
            "validate", "reload" -> {
                val permission = if (args[1].equals("validate", true)) "magicheroes.item.validate" else "magicheroes.item.reload"
                if (!sender.hasPermission(permission)) return deny(sender, permission)
                val errors = plugin.itemService.reload()
                sender.sendMessage(Component.text(if (errors.isEmpty()) "Item registry valid." else errors.joinToString(" | ")))
            }
            "give" -> {
                if (!sender.hasPermission("magicheroes.item.give")) return deny(sender, "magicheroes.item.give")
                val target = args.getOrNull(2)?.let(Bukkit::getPlayerExact)
                val id = args.getOrNull(3)
                val amount = args.getOrNull(4)?.toIntOrNull() ?: 1
                if (target == null || id == null || !plugin.itemService.give(target, id, amount)) {
                    sender.sendMessage(Component.text("Usage: /mh item give <online-player> <id> [1-64]"))
                } else sender.sendMessage(Component.text("Gave $amount $id to ${target.name}."))
            }
            "inspect" -> {
                if (!sender.hasPermission("magicheroes.item.inspect")) return deny(sender, "magicheroes.item.inspect")
                if (player == null) sender.sendMessage(Component.text("Item inspection requires an in-game player."))
                else {
                    val identity = plugin.itemService.inspect(player.inventory.itemInMainHand)
                    sender.sendMessage(Component.text(identity?.let { "${it.id} (${it.type}, v${it.templateVersion})" } ?: "Held item has no MagicHeroes identity."))
                }
            }
            "craft" -> {
                val id = args.getOrNull(2)
                if (player == null || id == null || !plugin.itemService.craft(player, id)) sender.sendMessage(Component.text("Usage: /mh item craft <recipe-id>"))
                else sender.sendMessage(Component.text("Craft completed."))
            }
            "loot" -> {
                val id = args.getOrNull(2)
                if (player == null || id == null) sender.sendMessage(Component.text("Usage: /mh item loot <table-id>"))
                else plugin.itemService.rollLoot(id).forEach { (itemId, amount) -> plugin.itemService.give(player, itemId, amount) }
            }
            else -> sender.sendMessage(Component.text("Usage: /mh item <give|inspect|validate|reload|craft|loot> ..."))
        }
    }

    private fun handleClass(sender: CommandSender, args: Array<out String>) {
        when (args.getOrNull(1)?.lowercase()) {
            "set" -> {
                val target = args.getOrNull(2)?.let(Bukkit::getPlayerExact)
                val clazz = args.getOrNull(3)?.let { ClassManager.get()?.getClass(it) }
                val data = target?.let { HeroPlayerManager.get()?.getPlayerData(it.uniqueId) }
                if (target == null || clazz == null || data == null) {
                    sender.sendMessage(Component.text("Usage: /mh class set <online-player> <class-id>"))
                    return
                }
                data.rpgClass = clazz
                data.level = 1
                data.exp = 0
                data.calculateBaseStats()
                refreshStats(target)
                sender.sendMessage(Component.text("Set ${target.name} to ${clazz.id}."))
            }
            "levelup" -> {
                val target = args.getOrNull(2)?.let(Bukkit::getPlayerExact)
                val data = target?.let { HeroPlayerManager.get()?.getPlayerData(it.uniqueId) }
                if (target == null || data == null) sender.sendMessage(Component.text("Usage: /mh class levelup <online-player>"))
                else {
                    data.level += 1
                    data.calculateBaseStats()
                    refreshStats(target)
                    sender.sendMessage(Component.text("${target.name} reached level ${data.level}."))
                }
            }
            else -> sender.sendMessage(Component.text("Usage: /mh class <set|levelup> ..."))
        }
    }

    private fun handleLevel(sender: CommandSender, args: Array<out String>) {
        val target = args.getOrNull(2)?.let(Bukkit::getPlayerExact)
        val amount = args.getOrNull(3)?.toIntOrNull()
        val data = target?.let { HeroPlayerManager.get()?.getPlayerData(it.uniqueId) }
        if (!args.getOrNull(1).equals("addexp", true) || target == null || amount == null || amount <= 0 || data == null) {
            sender.sendMessage(Component.text("Usage: /mh level addexp <online-player> <positive-amount>"))
            return
        }
        val levels = data.addExperience(amount)
        refreshStats(target)
        sender.sendMessage(Component.text("Granted $amount EXP; gained $levels level(s)."))
    }

    private fun showSkills(player: Player) {
        val unlocked = HeroPlayerManager.get()?.getPlayerData(player.uniqueId)?.unlockedSkills.orEmpty()
        player.sendMessage(Component.text("Skills: ${skills.ids().joinToString { if (it in unlocked) "[unlocked] $it" else "[locked] $it" }}"))
    }

    private fun handleSkill(player: Player, args: Array<out String>) {
        when (args.getOrNull(1)?.lowercase()) {
            "cast" -> handleCast(player, args.getOrNull(2))
            "bind" -> {
                val slot = args.getOrNull(2)?.toIntOrNull()
                val id = args.getOrNull(3)
                player.sendMessage(Component.text(if (slot != null && id != null && skills.bind(player, slot, id)) "Bound $id to slot $slot." else "Usage: /mh skill bind <1-9> <id>"))
            }
            "unbind" -> {
                val slot = args.getOrNull(2)?.toIntOrNull()
                player.sendMessage(Component.text(if (slot != null && skills.unbind(player, slot)) "Unbound slot $slot." else "Usage: /mh skill unbind <1-9>"))
            }
            "unlock" -> {
                val id = args.getOrNull(2)
                player.sendMessage(Component.text(if (id != null && skills.unlock(player, id)) "Unlocked $id." else "Unknown skill."))
            }
            "tree" -> player.sendMessage(Component.text(args.getOrNull(2)?.let { skills.unlockTreeNode(player, it) } ?: "Usage: /mh skill tree <node>"))
            "reset" -> { skills.reset(player); player.sendMessage(Component.text("Skills reset.")) }
            else -> player.sendMessage(Component.text("Usage: /mh skill <cast|bind|unbind|unlock|tree|reset> ..."))
        }
    }

    private fun handleCast(player: Player, id: String?) {
        if (id == null) {
            player.sendMessage(Component.text("Usage: /mh skill cast <id>"))
            return
        }
        when (val result = skills.cast(player, id)) {
            is SkillCastResult.Success -> player.sendMessage(Component.text("Cast ${result.skill.id}."))
            is SkillCastResult.Rejected -> player.sendMessage(Component.text(result.reason))
        }
    }

    private fun handleAttributes(player: Player, args: Array<out String>) {
        when (args.getOrNull(1)?.lowercase()) {
            null, "show" -> player.sendMessage(Component.text("Attribute points: ${attributes.points(player)} | ${AttributeType.entries.joinToString { "${it.name.lowercase()}=${HeroPlayerManager.get()?.getPlayerData(player.uniqueId)?.attribute(it) ?: 0}" }}"))
            "add" -> {
                val type = args.getOrNull(2)?.uppercase()?.let { runCatching { AttributeType.valueOf(it) }.getOrNull() }
                val amount = args.getOrNull(3)?.toIntOrNull() ?: 1
                if (type == null) player.sendMessage(Component.text("Usage: /mh attributes add <type> [amount]"))
                else {
                    val result = attributes.spend(player, type, amount)
                    player.sendMessage(Component.text(result.message))
                    if (result.success) refreshStats(player)
                }
            }
            "reset" -> { player.sendMessage(Component.text("Refunded ${attributes.reset(player)} attribute point(s).")); refreshStats(player) }
            else -> player.sendMessage(Component.text("Usage: /mh attributes <show|add|reset>"))
        }
    }

    private fun showQuests(player: Player) {
        val data = HeroPlayerManager.get()?.getPlayerData(player.uniqueId)
        val active = data?.questProgress?.keys?.joinToString().orEmpty()
        player.sendMessage(Component.text("Active quests: ${active.ifBlank { "none" }}"))
    }

    private fun handleQuest(player: Player, args: Array<out String>) {
        when (args.getOrNull(1)?.lowercase()) {
            "start" -> player.sendMessage(Component.text(args.getOrNull(2)?.let { quests.start(player, it).message } ?: "Usage: /mh quest start <id>"))
            else -> player.sendMessage(Component.text("Usage: /mh quest start <id>"))
        }
    }

    private fun handleParty(player: Player, args: Array<out String>) {
        when (args.getOrNull(1)?.lowercase()) {
            "invite" -> {
                val target = args.getOrNull(2)?.let(Bukkit::getPlayerExact)
                player.sendMessage(Component.text(if (target != null && parties.invite(player, target)) "Invited ${target.name}." else "Usage: /mh party invite <online-player>"))
            }
            "accept" -> player.sendMessage(Component.text(if (parties.accept(player)) "Joined party." else "No party invitation."))
            "leave" -> player.sendMessage(Component.text(if (parties.leave(player)) "Left party." else "You are not in a party."))
            else -> player.sendMessage(Component.text("Usage: /mh party <invite|accept|leave> ..."))
        }
    }

    private fun handleWaypoint(player: Player, args: Array<out String>) {
        when (args.getOrNull(1)?.lowercase()) {
            "discover" -> player.sendMessage(Component.text(if (args.getOrNull(2)?.let { waypoints.discover(player, it) } == true) "Waypoint discovered." else "Unknown waypoint."))
            "teleport" -> player.sendMessage(Component.text(if (args.getOrNull(2)?.let { waypoints.teleport(player, it) } == true) "Teleported." else "Waypoint unavailable."))
            "list" -> player.sendMessage(Component.text("Waypoints: ${waypoints.discovered(player).joinToString().ifBlank { "none" }}"))
            else -> player.sendMessage(Component.text("Usage: /mh waypoint <discover|teleport|list> ..."))
        }
    }

    private fun showStats(player: Player) {
        val data = HeroPlayerManager.get()?.getPlayerData(player.uniqueId) ?: return
        val maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)?.value ?: 0.0
        player.sendMessage(Component.text("HP ${"%.0f".format(player.health)}/${"%.0f".format(maxHealth)} | Mana ${"%.0f".format(data.currentMana)}/${"%.0f".format(data.getTotalMaxMana())} | Damage ${"%.0f".format(data.getTotalDamage())} | Defense ${"%.0f".format(data.getTotalDefense())}"))
    }

    private fun refreshStats(player: Player) {
        val data = HeroPlayerManager.get()?.getPlayerData(player.uniqueId) ?: return
        StatCalculator.updateEquipmentStats(player, data)
        resources.applyMaxHealth(player, data.getTotalMaxHealth())
    }

    private fun reload(sender: CommandSender) {
        sender.sendMessage(Component.text(if (plugin.reloadPlugin()) "MagicHeroes reloaded." else "MagicHeroes reload failed."))
    }

    private fun sendHelp(sender: CommandSender): Boolean {
        sender.sendMessage(Component.text("/mh <${commands.filter { sender.hasPermission(it.permission) }.joinToString { it.name }}>"))
        return true
    }

    private fun deny(sender: CommandSender, permission: String): Boolean {
        sender.sendMessage(Component.text("You do not have permission: $permission"))
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (args.size == 1) return commands.filter { sender.hasPermission(it.permission) }.map { it.name }.filter { it.startsWith(args[0], true) }
        return when (args[0].lowercase()) {
            "item" -> complete(args, 1, listOf("give", "inspect", "validate", "reload", "craft", "loot"))
            "skill" -> complete(args, 1, listOf("cast", "bind", "unbind", "unlock", "tree", "reset"))
            "quest" -> complete(args, 1, listOf("start"))
            "party" -> complete(args, 1, listOf("invite", "accept", "leave"))
            "waypoint" -> complete(args, 1, listOf("discover", "teleport", "list"))
            "attributes" -> complete(args, 1, listOf("show", "add", "reset"))
            "level" -> complete(args, 1, listOf("addexp"))
            "class" -> complete(args, 1, listOf("set", "levelup"))
            else -> emptyList()
        }
    }

    private fun complete(args: Array<out String>, index: Int, options: Collection<String>): List<String> =
        if (args.size == index + 1) options.filter { it.startsWith(args[index], true) } else emptyList()
}
