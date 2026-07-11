# MagicHeroes public API

Obtain API from plugin instance:

```kotlin
val magicHeroes = server.pluginManager.getPlugin("MagicHeroes") as? MagicHeroes
val api = magicHeroes?.api ?: return
```

Active functions:

- `itemIdentity(item)`
- `playerLevel(player)`
- `grantExperience(player, amount)`
- `mana(player)` / `consumeMana(player, amount)`
- `damage(DamageContext)`
- `registerSkill(skill)` / `unregisterSkill(id)`

Thread rule: call API only from Paper main thread. Profile, inventory, entity, event, and world state are main-thread state.

Optional integrations are capability-detected only. PlaceholderAPI, Vault, MythicMobs, Citizens, ItemsAdder, Oraxen, and Nexo may be absent without disabling MagicHeroes.
