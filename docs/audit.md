# MagicHeroes audit — P0 baseline

## Working

- PDC item rename, custom lore, enchantment mutation, tooltip templates.
- Custom durability reduction on attacks/block breaks and armor hits.
- Three YAML classes, linear level/EXP, base health/mana/defense.
- GUI item editor, durability, enchantment, tooltip selectors.

## Data-only or incomplete gameplay

- `mh_damage` displayed but previously ignored by combat.
- `mh_level_req` and `mh_class_req` displayed but not enforced.
- RPG current health duplicated Bukkit health.
- YAML profile persistence synchronous and language preference not persisted.
- No item ID/template/registry, type, advanced combat, skills, professions, quests, social, loot, API integrations.

## P0 defects found

- Damage listener changed Bukkit damage, subtracted custom health, and set `Player.health`: double application and death lifecycle conflicts.
- Regen used amount-per-run instead of elapsed time.
- Stat scan omitted offhand and damage.
- Broken items lost stats but could still attack/interact.
- Commands rejected console, had no permission declarations, and tab completion advertised `addexp` while backend used `levelup`.
- Player profile load/save used synchronous YAML on join/quit/disable.
- Actionbar task had no lifecycle handle and reload could create duplicate tasks.
- Item mutations were inconsistent about tooltip synchronization.

## P0 acceptance

- Bukkit health and `Attribute.MAX_HEALTH` own runtime health. RPG listener only changes event damage once.
- Resource regen multiplies rate by elapsed monotonic time and clamps.
- Legacy player YAML remains readable; schema version and `.v0.bak` added; saves use async executor and atomic replacement.
- Permission gates exist in `plugin.yml`; console can run admin/help/reload/class/debug; player-only operations report clear error.
- Broken custom item cancels attack/interact; break event fires once; stat recalc removes item stats.
- GUI rejects drag, shift-click, number-key, double-click; input state clears on close/quit.

## Not P0

SQLite, item registry/templates, complete stat registry/source breakdown, requirements, advanced damage pipeline, crit/penetration/elemental, skills, skill trees, crafting, loot, profession, quest, party, guild, waypoint, economy, optional integrations, public extension API.
