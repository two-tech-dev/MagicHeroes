# Waypoint configuration

Global settings live in `plugins/MagicHeroes/config.yml`.

```yaml
waypoints:
  teleport-cooldown-seconds: 5
```

Waypoint locations live in `plugins/MagicHeroes/waypoints.yml`.

```yaml
waypoints:
  spawn:
    world: world
    x: 0.5
    y: 65.0
    z: 0.5
    yaw: 0.0
    pitch: 0.0
```

## Fields

- `spawn`: waypoint ID used by `/mh waypoint discover spawn` and `/mh waypoint teleport spawn`.
- `world`: Bukkit world name. Must exist when `/mh reload` runs.
- `x`, `y`, `z`: teleport coordinates.
- `yaw`, `pitch`: optional look direction. Defaults to `0.0`.

## Commands

| Command | Use |
|---|---|
| `/mh waypoint discover <id>` | Mark waypoint discovered for player. |
| `/mh waypoint teleport <id>` | Teleport to discovered waypoint. |
| `/mh waypoint list` | List discovered waypoint IDs. |

## Safety

Teleport only starts if destination is safe:

- feet block passable
- head block passable
- block below solid

Teleport uses Paper `teleportAsync()` to avoid sync chunk-load stalls.
