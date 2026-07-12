# Integrations

MagicHeroes soft-depends on optional plugins. Missing integrations do not disable MagicHeroes.

## PlaceholderAPI

If PlaceholderAPI is installed, MagicHeroes registers internal expansion `magicheroes`.

Placeholders:

| Placeholder | Value |
|---|---|
| `%magicheroes_class%` | Current class ID or `none`. |
| `%magicheroes_level%` | Player level. |
| `%magicheroes_exp%` | Current EXP toward next level. |
| `%magicheroes_mana%` | Current mana rounded. |
| `%magicheroes_max_mana%` | Max mana rounded. |
| `%magicheroes_damage%` | Total RPG damage rounded. |
| `%magicheroes_defense%` | Total RPG defense rounded. |
| `%magicheroes_kills%` | Stored kill count. |
| `%magicheroes_deaths%` | Stored death count. |

## Planned adapters

- Vault: economy costs for waypoint, crafting, item services.
- MythicMobs: custom mob kill targets and loot drops.
- Citizens: quest start/complete NPCs.
- ItemsAdder/Oraxen/Nexo: custom model/material item templates.
