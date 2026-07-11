# P1 item engine

Item templates live under `plugins/MagicHeroes/items/**`. Registry loads all YAML files atomically: any invalid file keeps prior registry snapshot.

## Template

```yaml
id: firebrand
type: sword
material: DIAMOND_SWORD
display-name: "&cFirebrand"
lore:
  - "&7A starter flame blade."
base-stats:
  ATTACK_DAMAGE: 25.0
  FIRE_DAMAGE: 5.0
requirements:
  level: 1
  class: warrior
durability:
  max: 100
version: 1
```

## Active fields

- Identity PDC: `item_id`, `item_type`, `item_version`, `template_hash`, `upgrade_level`, `identified`, `soulbound_owner`.
- Legacy PDC stat keys remain writable/readable: health, mana, damage, defense, health regen, mana regen.
- Typed stat PDC keys: `stat_<STAT_TYPE>`.
- P1 stats: all declared `StatType` values render in item data; legacy totals use health/mana/damage/defense/regen now.
- Requirement enforcement: level and class on interact. Equip restrictions, profession/attribute, random rolls, sockets, sets, upgrades, reforge, soulbound enforcement: later phases.

## Commands

- `/mh item give <online-player> <id> [1..64]`
- `/mh item inspect` — held item
- `/mh item validate`
- `/mh item reload`
- `/mh stats`

Missing P1 commands: create/edit/clone/delete persistent template authoring. YAML is current admin authoring surface.
