# Party configuration

Party settings live in `plugins/MagicHeroes/config.yml`. Reload with `/mh reload`.

```yaml
party:
  max-size: 5
```

`max-size` is maximum members per party. Minimum effective value is `2`.

## Commands

| Command | Use |
|---|---|
| `/mh party invite <online-player>` | Leader invites player. |
| `/mh party accept` | Accept pending invitation. |
| `/mh party leave` | Leave current party. Leader transfers automatically. |
| `/mh party kick <online-player>` | Leader removes online member. |
| `/mh party disband` | Leader removes whole party. |
| `/mh party chat <message>` | Send message to online party members. |

## Shared quest progress

Online party members share quest `KILL`, `MINE`, `COLLECT`, `INTERACT`, and `REACH` progress. No range limit yet; avoid using party share for competitive objectives.
