package twotech.plugin.magicHeroes.command

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import twotech.plugin.magicHeroes.MagicHeroes
import twotech.plugin.magicHeroes.calculator.StatCalculator
import twotech.plugin.magicHeroes.data.ResourceService
import twotech.plugin.magicHeroes.handler.DurabilityHandler
import twotech.plugin.magicHeroes.handler.EnchantmentHandler
import twotech.plugin.magicHeroes.handler.GUIHandler
import twotech.plugin.magicHeroes.handler.LanguageHandler
import twotech.plugin.magicHeroes.handler.LoreHandler
import twotech.plugin.magicHeroes.handler.RenameHandler
import twotech.plugin.magicHeroes.handler.SetTooltipHandler
import twotech.plugin.magicHeroes.manager.ClassManager
import twotech.plugin.magicHeroes.manager.HeroPlayerManager
import twotech.plugin.magicHeroes.manager.LanguageManager
import twotech.plugin.magicHeroes.manager.TooltipManager
import twotech.plugin.magicHeroes.attribute.AttributeService
import twotech.plugin.magicHeroes.attribute.AttributeType
import twotech.plugin.magicHeroes.skill.SkillCastResult
import twotech.plugin.magicHeroes.skill.SkillService

class MagicHeroesCommand(
    private val plugin: MagicHeroes,
    private val resources: ResourceService,
    private val attributes: AttributeService,
    private val skills: SkillService
) : CommandExecutor, TabCompleter {
    private data class CommandSpec(val name: String, val permission: String, val playerOnly: Boolean)

    private val commands = listOf(
        CommandSpec("help", "magicheroes.user", false),
        CommandSpec("language", "magicheroes.user", true),
        CommandSpec("stats", "magicheroes.user", true),
        CommandSpec("attributes", "magicheroes.user", true),
        CommandSpec("skills", "magicheroes.user", true),
        CommandSpec("skill", "magicheroes.user", true),
        CommandSpec("level", "magicheroes.level.admin", false),
        CommandSpec("gui", "magicheroes.item.edit", true),
        CommandSpec("rename", "magicheroes.item.edit", true),
        CommandSpec("setlore", "magicheroes.item.edit", true),
        CommandSpec("addenchant", "magicheroes.item.edit", true),
        CommandSpec("removeenchant", "magicheroes.item.edit", true),
        CommandSpec("durability", "magicheroes.item.edit", true),
        CommandSpec("settooltip", "magicheroes.item.edit", true),
        CommandSpec("item", "magicheroes.item.inspect", false),
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
        if (!sender.hasPermission(spec.permission)) {
            sender.sendMessage(Component.text("You do not have permission: ${spec.permission}"))
            return true
        }
        val player = sender as? Player
        if (spec.playerOnly && player == null) {
            sender.sendMessage(Component.text("This subcommand requires an in-game player."))
            return true
        }
        when (spec.name) {
            "help" -> sendHelp(sender)
            "language" -> LanguageHandler.execute(player!!, args)
            "stats" -> showStats(player!!)
            "attributes" -> handleAttributes(player!!, args)
            "skills" -> showSkills(player!!)
            "skill" -> handleSkill(player!!, args)
            "level" -> handleLevel(sender, args)
            "gui" -> GUIHandler.execute(player!!, args)
            "rename" -> RenameHandler.execute(player!!, args)
            "setlore" -> LoreHandler.execute(player!!, args)
            "addenchant" -> EnchantmentHandler.executeAdd(player!!, args)
            "removeenchant" -> EnchantmentHandler.executeRemove(player!!, args)
            "durability" -> DurabilityHandler.execute(player!!, args)
            "settooltip" -> SetTooltipHandler.execute(player!!, args)
            "item" -> handleItem(sender, player, args)
            "reload" -> reload(sender)
            "class" -> handleClass(sender, args)
            "debug" -> sender.sendMessage(Component.text("MagicHeroes: ${Bukkit.getOnlinePlayers().size} online player(s)."))
        }
        return true
    }

    private fun handleItem(sender: CommandSender, player: Player?, args: Array<out String>) {
        val service = plugin.itemService
        when (args.getOrNull(1)?.lowercase()) {
            "validate" -> {
                if (!sender.hasPermission("magicheroes.item.validate")) return noPermission(sender, "magicheroes.item.validate")
                val errors = service.reload()
                sender.sendMessage(Component.text(if (errors.isEmpty()) "Item registry valid." else errors.joinToString(" | ")))
            }
            "reload" -> {
                if (!sender.hasPermission("magicheroes.item.reload")) return noPermission(sender, "magicheroes.item.reload")
                val errors = service.reload()
                sender.sendMessage(Component.text(if (errors.isEmpty()) "Item registry reloaded." else errors.joinToString(" | ")))
            }
            "give" -> {
                if (!sender.hasPermission("magicheroes.item.give")) return noPermission(sender, "magicheroes.item.give")
                val target = args.getOrNull(2)?.let(Bukkit::getPlayerExact)
                val id = args.getOrNull(3)
                val amount = args.getOrNull(4)?.toIntOrNull() ?: 1
                if (target == null || id == null || amount !in 1..64 || !service.give(target, id, amount)) {
                    sender.sendMessage(Component.text("Usage: /mh item give <online-player> <id> [amount]"))
                    return
                }
                sender.sendMessage(Component.text("Gave $amount $id to ${target.name}."))
            }
            "inspect" -> {
                if (player == null) {
                    sender.sendMessage(Component.text("Item inspection requires an in-game player."))
                    return
                }
                if (!sender.hasPermission("magicheroes.item.inspect")) return noPermission(sender, "magicheroes.item.inspect")
                val identity = service.inspect(player.inventory.itemInMainHand)
                sender.sendMessage(Component.text(identity?.let { "${it.id} (${it.type}, v${it.templateVersion})" } ?: "Held item has no MagicHeroes identity."))
            }
            else -> sender.sendMessage(Component.text("Usage: /mh item <give|inspect|validate|reload> ..."))
        }
    }

    private fun noPermission(sender: CommandSender, permission: String) {
        sender.sendMessage(Component.text("You do not have permission: $permission"))
    }

    private fun reload(sender: CommandSender) {
        if (plugin.reloadPlugin()) sender.sendMessage(Component.text("MagicHeroes reloaded."))
        else sender.sendMessage(Component.text("MagicHeroes reload failed; active state remains unchanged."))
    }

    private fun handleClass(sender: CommandSender, args: Array<out String>) {
        when (args.getOrNull(1)?.lowercase()) {
            "set" -> {
                val target = args.getOrNull(2)?.let(Bukkit::getPlayerExact)
                val classId = args.getOrNull(3)
                val rpgClass = classId?.let { ClassManager.get()?.getClass(it) }
                val data = target?.let { HeroPlayerManager.get()?.getPlayerData(it.uniqueId) }
                if (target == null || rpgClass == null || data == null) {
                    sender.sendMessage(Component.text("Usage: /mh class set <online-player> <class-id>"))
                    return
                }
                data.rpgClass = rpgClass
                data.level = 1
                data.exp = 0
                data.calculateBaseStats()
                data.currentMana = data.getTotalMaxMana()
                StatCalculator.updateEquipmentStats(target, data)
                resources.applyMaxHealth(target, data.getTotalMaxHealth())
                resources.restoreHealth(target)
                sender.sendMessage(Component.text("Set ${target.name} to ${rpgClass.id}."))
            }
            "levelup" -> {
                val target = args.getOrNull(2)?.let(Bukkit::getPlayerExact)
                val data = target?.let { HeroPlayerManager.get()?.getPlayerData(it.uniqueId) }
                if (target == null || data == null) {
                    sender.sendMessage(Component.text("Usage: /mh class levelup <online-player>"))
                    return
                }
                data.level += 1
                data.calculateBaseStats()
                data.currentMana = data.getTotalMaxMana()
                StatCalculator.updateEquipmentStats(target, data)
                resources.applyMaxHealth(target, data.getTotalMaxHealth())
                resources.restoreHealth(target)
                sender.sendMessage(Component.text("${target.name} reached level ${data.level}."))
            }
            else -> sender.sendMessage(Component.text("Usage: /mh class <set|levelup> ..."))
        }
    }

    private fun showSkills(player: Player) {
        val unlocked = HeroPlayerManager.get()?.getPlayerData(player.uniqueId)?.unlockedSkills.orEmpty()
        player.sendMessage(Component.text("Skills: ${skills.ids().joinToString { id -> if (id in unlocked) "[unlocked] $id" else "[locked] $id" }}"))
    }

    private fun handleSkill(player: Player, args: Array<out String>) {
        when (args.getOrNull(1)?.lowercase()) {
            "cast" -> {
                val id = args.getOrNull(2)
                if (id == null) { player.sendMessage(Component.text("Usage: /mh skill cast <id>")); return }
                when (val result = skills.cast(player, id)) {
                    is SkillCastResult.Success -> player.sendMessage(Component.text("Cast ${result.skill.id}."))
                    is SkillCastResult.Rejected -> player.sendMessage(Component.text(result.reason))
                }
            }
            "bind" -> {
                val slot = args.getOrNull(2)?.toIntOrNull()
                val id = args.getOrNull(3)
                if (slot == null || id == null || !skills.bind(player, slot, id)) player.sendMessage(Component.text("Usage: /mh skill bind <slot 1-9> <id>"))
                else player.sendMessage(Component.text("Bound $id to slot $slot."))
            }
            "unbind" -> {
                val slot = args.getOrNull(2)?.toIntOrNull()
                if (slot == null || !skills.unbind(player, slot)) player.sendMessage(Component.text("Usage: /mh skill unbind <slot 1-9>"))
                else player.sendMessage(Component.text("Unbound slot $slot."))
            }
            "unlock" -> {
                val id = args.getOrNull(2)
                if (id == null || !skills.unlock(player, id)) player.sendMessage(Component.text("Unknown skill: ${id ?: ""}"))
                else player.sendMessage(Component.text("Unlocked $id."))
            }
            "tree" -> {
                val node = args.getOrNull(2)
                if (node == null) player.sendMessage(Component.text("Usage: /mh skill tree <node>"))
                else player.sendMessage(Component.text(skills.unlockTreeNode(player, node)))
            }
            "reset" -> { skills.reset(player); player.sendMessage(Component.text("Skills reset.")) }
            else -> player.sendMessage(Component.text("Usage: /mh skill <cast|bind|unbind|unlock|tree|reset> ..."))
        }
    }

    private fun handleAttributes(player: Player, args: Array<out String>) {

        when (args.getOrNull(1)?.lowercase()) {
            null, "show" -> player.sendMessage(Component.text("Attribute points: ${attributes.points(player)} | ${AttributeType.entries.joinToString { "${it.name.lowercase()}=${HeroPlayerManager.get()?.getPlayerData(player.uniqueId)?.attribute(it) ?: 0}" }}"))
            "add" -> {
                val type = args.getOrNull(2)?.uppercase()?.let { runCatching { AttributeType.valueOf(it) }.getOrNull() }
                val amount = args.getOrNull(3)?.toIntOrNull() ?: 1
                if (type == null) {
                    player.sendMessage(Component.text("Usage: /mh attributes add <type> [amount]"))
                    return
                }
                val result = attributes.spend(player, type, amount)
                player.sendMessage(Component.text(result.message))
                if (result.success) refreshPlayerStats(player)
            }
            "reset" -> {
                val refunded = attributes.reset(player)
                player.sendMessage(Component.text("Refunded $refunded attribute point(s)."))
                refreshPlayerStats(player)
            }
            else -> player.sendMessage(Component.text("Usage: /mh attributes <show|add|reset>"))
        }
    }

    private fun handleLevel(sender: CommandSender, args: Array<out String>) {
        if (!args.getOrNull(1).equals("addexp", true)) {
            sender.sendMessage(Component.text("Usage: /mh level addexp <online-player> <amount>"))
            return
        }
        val target = args.getOrNull(2)?.let(Bukkit::getPlayerExact)
        val amount = args.getOrNull(3)?.toIntOrNull()
        val data = target?.let { HeroPlayerManager.get()?.getPlayerData(it.uniqueId) }
        if (target == null || amount == null || amount <= 0 || data == null) {
            sender.sendMessage(Component.text("Usage: /mh level addexp <online-player> <positive-amount>"))
            return
        }
        val levels = data.addExperience(amount)
        refreshPlayerStats(target)
        sender.sendMessage(Component.text("Granted $amount EXP to ${target.name}; gained $levels level(s)."))
    }

    private fun refreshPlayerStats(player: Player) {
        val data = HeroPlayerManager.get()?.getPlayerData(player.uniqueId) ?: return
        StatCalculator.updateEquipmentStats(player, data)
        resources.applyMaxHealth(player, data.getTotalMaxHealth())
    }

    private fun showStats(player: Player) {
        val data = HeroPlayerManager.get()?.getPlayerData(player.uniqueId) ?: run {
            player.sendMessage(Component.text("Profile not loaded."))
            return
        }
        val snapshot = plugin.statCalculationService.snapshot(player)
        val maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)?.value ?: 0.0
        val sources = snapshot.sources.joinToString(", ") { "+${"%.0f".format(it.value)} ${it.stat.name.lowercase()} from ${it.source}" }
        player.sendMessage(Component.text("HP ${"%.0f".format(player.health)}/${"%.0f".format(maxHealth)} | Mana ${"%.0f".format(data.currentMana)}/${"%.0f".format(data.getTotalMaxMana())} | Damage ${"%.0f".format(data.getTotalDamage())} | Defense ${"%.0f".format(data.getTotalDefense())}"))
        if (sources.isNotBlank()) player.sendMessage(Component.text(sources))
    }

    private fun sendHelp(sender: CommandSender): Boolean {
        val visible = commands.filter(sender::hasPermission).joinToString(", ") { it.name }
        sender.sendMessage(Component.text("/mh <${visible.ifBlank { "help" }}>"))
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (args.size == 1) return commands.filter { sender.hasPermission(it.permission) }.map { it.name }.filter { it.startsWith(args[0], true) }
        if (args[0].equals("item", true)) return when (args.size) {
            2 -> complete(args, 1, itemActions(sender))
            3 -> if (args[1].equals("give", true) && sender.hasPermission("magicheroes.item.give")) complete(args, 2, Bukkit.getOnlinePlayers().map(Player::getName)) else emptyList()
            4 -> if (args[1].equals("give", true) && sender.hasPermission("magicheroes.item.give")) complete(args, 3, plugin.itemService.templates().map { it.id }) else emptyList()
            else -> emptyList()
        }
        return when (args[0].lowercase()) {
            "durability" -> complete(args, 1, listOf("set", "infinite", "check", "reset"))
            "skill" -> when (args.size) {
                2 -> complete(args, 1, listOf("cast", "bind", "unbind", "unlock", "tree", "reset"))
                3 -> when (args[1].lowercase()) {
                    "cast", "unlock" -> complete(args, 2, skills.ids())
                    "bind", "unbind" -> complete(args, 2, (1..9).map(Int::toString))
                    else -> emptyList()
                }
                4 -> if (args[1].equals("bind", true)) complete(args, 3, skills.ids()) else emptyList()
                else -> emptyList()
            }
            "attributes" -> when (args.size) {
                2 -> complete(args, 1, listOf("show", "add", "reset"))
                3 -> if (args[1].equals("add", true)) complete(args, 2, AttributeType.entries.map { it.name.lowercase() }) else emptyList()
                else -> emptyList()
            }
            "level" -> if (args.size == 2) complete(args, 1, listOf("addexp")) else if (args.size == 3) complete(args, 2, Bukkit.getOnlinePlayers().map(Player::getName)) else emptyList()
            "class" -> when (args.size) {
                2 -> complete(args, 1, listOf("set", "levelup"))
                3 -> complete(args, 2, Bukkit.getOnlinePlayers().map(Player::getName))
                4 -> if (args[1].equals("set", true)) complete(args, 3, ClassManager.get()?.getAvailableClassIds()?.toList().orEmpty()) else emptyList()
                else -> emptyList()
            }
            "language" -> complete(args, 1, LanguageManager.get()?.getAvailableLanguages().orEmpty())
            "settooltip" -> complete(args, 1, TooltipManager.get()?.getAvailableTooltips().orEmpty())
            "addenchant", "removeenchant" -> complete(args, 1, org.bukkit.enchantments.Enchantment.values().map { it.key.key })
            else -> emptyList()
        }
    }

    private fun itemActions(sender: CommandSender): List<String> = buildList {
        if (sender.hasPermission("magicheroes.item.give")) add("give")
        if (sender.hasPermission("magicheroes.item.inspect")) add("inspect")
        if (sender.hasPermission("magicheroes.item.validate")) add("validate")
        if (sender.hasPermission("magicheroes.item.reload")) add("reload")
    }

    private fun complete(args: Array<out String>, index: Int, options: Collection<String>): List<String> =
        if (args.size == index + 1) options.filter { it.startsWith(args[index], true) } else emptyList()
}
