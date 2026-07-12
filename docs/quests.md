# Quest configuration

Quest files live in `plugins/MagicHeroes/quests/`. Each `.yml` file defines one quest. Run `/mh reload` after editing files.

## In-game GUI

- `/quest` opens player quest menu.
- `/quest start <id>` starts one quest directly.
- `/quest add`, `/quest remove`, `/quest edit` open admin controls for players with `magicheroes.quest.admin`.
- Admin GUI chat input accepts `cancel` to abort.

Admin input formats:

```text
add:    id|display name|type|target|required|exp
remove: quest-id
edit:   quest-id|new display name
```

Example:

```text
starter-mine|Starter Mining|MINE|COAL_ORE|16|100
```

## Full example

```yaml
id: reach-spawn
display-name: "Reach Spawn"
prerequisites:
  - first-blood
objectives:
  - id: spawn
    type: REACH
    target: world,0,64,0,5
    required: 1
  - id: oak-log
    type: MINE
    target: OAK_LOG
    required: 16
rewards:
  exp: 100
  items:
    firebrand: 1
repeatable: false
```

## Fields

- `id`: unique lowercase quest ID. Keep stable after players start quest.
- `display-name`: player-facing quest name.
- `prerequisites`: optional completed quest IDs.
- `objectives`: one or more objectives. All must complete.
- `rewards.exp`: non-negative EXP reward.
- `rewards.items`: optional `item-id: amount` map. Item ID must exist in item templates.
- `repeatable`: `true` permits quest restart after completion.

## Objective types

### `KILL`

```yaml
- id: zombies
  type: KILL
  target: ZOMBIE
  required: 10
```

`target` is Bukkit entity type name. Progress when player kills matching mob.

### `MINE`

```yaml
- id: diamond-ore
  type: MINE
  target: DIAMOND_ORE
  required: 8
```

`target` is Bukkit material name. Progress when matching block breaks.

### `COLLECT`

```yaml
- id: diamonds
  type: COLLECT
  target: DIAMOND
  required: 16
```

`target` is Bukkit material name. Progress by picked-up item amount.

### `INTERACT`

```yaml
- id: villager
  type: INTERACT
  target: VILLAGER
  required: 1
```

`target` is Bukkit block material or entity type name. Progress on right click. Block interactions only count main hand, preventing off-hand double counts.

### `REACH`

```yaml
- id: spawn
  type: REACH
  target: world,0,64,0,5
  required: 1
```

`target` format is exactly `world,x,y,z,radius`.

- `world`: Bukkit world name, case-insensitive.
- `x`, `y`, `z`: center coordinates. Decimals allowed.
- `radius`: positive spherical radius in blocks.
- `required`: normally `1`. Entering zone completes this objective.

Example Nether portal zone:

```yaml
- id: nether-hub
  type: REACH
  target: world_nether,120.5,64,-30.0,8
  required: 1
```

`REACH` checks only when player crosses into another block, not every movement packet.

## Party behavior

Online party members share `KILL`, `MINE`, `COLLECT`, `INTERACT`, and `REACH` progress. Offline members do not receive progress. No distance limit exists yet; do not use party sharing for competitive quests.
