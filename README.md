# MagicHeroes

MagicHeroes là plugin RPG/MMO cho Paper 1.21.x, viết bằng Kotlin.

Mục tiêu plugin: item custom data-driven, stat RPG, combat, class/level, skill, quest, party, waypoint, crafting/loot và API cho plugin khác.

> **Trạng thái hiện tại:** source đang phát triển mạnh, chưa có xác nhận build/Paper smoke test cuối cùng. Không dùng production trước khi chạy `./gradlew.bat clean test shadowJar` và test trên server Paper local.

## Plugin hiện có gì

### Item và tooltip

- Đổi tên item, custom lore, enchant, tooltip template.
- Dữ liệu item lưu PDC, không dùng lore làm data source.
- Đọc legacy PDC:
  - `mh_health`, `mh_mana`, `mh_damage`, `mh_defense`
  - `mh_health_regen`, `mh_mana_regen`
  - `mh_name`, `mh_custom_lore`
  - `mh_durability`, `mh_max_durability`
  - `mh_level_req`, `mh_class_req`
- Item template YAML trong `plugins/MagicHeroes/items/**`.
- Item identity PDC:
  - `item_id`
  - `item_type`
  - `item_version`
  - `template_hash`
  - `upgrade_level`
  - `identified`
  - `soulbound_owner`
- Demo template: `firebrand`.
- Item registry validate ID/material/template, giữ snapshot cũ khi item reload lỗi.

### Durability

- Custom durability riêng, không dùng vanilla durability làm dữ liệu thật.
- Infinite durability.
- Broken item:
  - mất stat bonus
  - chặn attack/interact/block break
  - tooltip broken
  - phát `ItemBreakEvent`
- Tooltip refresh sau item mutation qua `ItemUtils.resetAndUpdateTooltip()`.

### Stat và requirement

- Stat PDC typed + legacy fallback.
- Stat snapshot scan:
  - helmet
  - chestplate
  - leggings
  - boots
  - main hand
  - off hand
- Có source tracking cho `/mh stats`.
- Enforce level/class requirement khi attack và interact.
- Attribute cơ bản:
  - Strength
  - Dexterity
  - Intelligence
  - Vitality
  - Wisdom
  - Luck

### Combat và resource

- Bukkit health là health runtime source.
- Damage pipeline nền:
  - pre-damage event
  - attack damage
  - crit
  - armor/magic penetration
  - defense
  - elemental resistance
  - final Bukkit damage
  - post-damage event
- Damage type nền:
  - melee
  - projectile
  - magic
  - fire
  - fall
  - void
  - explosion
  - environmental
  - true damage model
- Resource hiện có:
  - health qua Bukkit
  - mana profile-backed
  - mana regen theo elapsed time
- Custom events:
  - `MagicHeroesPreDamageEvent`
  - `MagicHeroesPostDamageEvent`
  - `MagicHeroesCriticalHitEvent`
  - `MagicHeroesHealEvent`
  - `MagicHeroesKillEvent`
  - `MagicHeroesDeathEvent`

### Class, level và progression

- Class YAML hiện có: warrior, mage, archer.
- Base HP/mana/defense theo class + level.
- EXP multi-level.
- Level cap mặc định 100.
- Level up cấp attribute point + skill point.
- Profile YAML persist:
  - class/level/EXP
  - mana
  - kill/death
  - attributes
  - skill unlock/bind/tree
  - quest progress
  - completed quest
  - discovered waypoint

### Skill và skill tree

- Skill YAML registry.
- Demo skills:
  - `slash`
  - `minor-heal`
- Skill check:
  - unlock
  - class
  - level
  - mana
  - cooldown
  - target/range
- Skill binding slot 1–9.
- Skill tree YAML:
  - parent requirement
  - incompatible node
  - point cost
  - max level
  - unlock skill
- Quit dọn cooldown state.

### Advanced item, crafting và loot

Nền P4 hiện có:

- Tier PDC.
- Upgrade level PDC.
- Socket count + gem ID PDC.
- Soulbound owner check.
- Set registry YAML.
- Recipe registry YAML.
- Loot table YAML:
  - guaranteed entries
  - weighted entry
  - seeded random selection.
- Demo:
  - `recipes/firebrand.yml`
  - `loot-tables/firebrand.yml`

### Quest, party và waypoint

- Quest YAML registry.
- Objective hiện có: kill/collect/mine/reach/interact model.
- Demo quest: `first-blood`.
- Quest start, kill progress, complete, EXP/item reward.
- Party:
  - invite
  - accept
  - leave
  - leader transfer khi leader leave.
- Waypoint service:
  - register
  - discover
  - teleport sau discovery
  - profile persistence.

### API và integration

Public API source-level qua `MagicHeroes.api`:

- đọc item identity
- đọc level
- cấp EXP
- đọc/trừ mana
- custom `DamageContext`
- register/unregister skill

Optional plugin detection:

- PlaceholderAPI
- Vault
- MythicMobs
- Citizens
- ItemsAdder
- Oraxen
- Nexo

Các plugin trên là `softdepend`; không cài không làm MagicHeroes disable.

## Command

| Command | Mục đích |
|---|---|
| `/mh help` | Xem command theo permission |
| `/mh stats` | Xem HP/mana/damage/defense |
| `/mh attributes show\|add\|reset` | Xem và phân điểm attribute |
| `/mh skills` | Xem skill lock/unlock |
| `/mh skill cast\|bind\|unbind\|unlock\|tree\|reset` | Quản lý skill |
| `/mh quests` | Xem quest active |
| `/mh quest start <id>` | Nhận quest |
| `/mh party invite\|accept\|leave` | Party cơ bản |
| `/mh waypoint discover\|teleport\|list` | Waypoint cơ bản |
| `/mh item give <player> <id> [amount]` | Give custom item |
| `/mh item inspect` | Inspect held item |
| `/mh item validate\|reload` | Validate/reload registry |
| `/mh item craft <recipe>` | Craft recipe cơ bản |
| `/mh item loot <table>` | Roll loot table cơ bản |
| `/mh class set\|levelup` | Admin class/level |
| `/mh level addexp <player> <amount>` | Admin EXP |
| `/mh reload` | Reload config/registry |

## Permission

| Permission | Mục đích |
|---|---|
| `magicheroes.user` | Player commands |
| `magicheroes.admin` | Tổng permission admin |
| `magicheroes.reload` | Reload |
| `magicheroes.item.create` | Item template admin |
| `magicheroes.item.give` | Give item |
| `magicheroes.item.edit` | GUI/item editor |
| `magicheroes.item.validate` | Validate item registry |
| `magicheroes.item.reload` | Reload item registry |
| `magicheroes.item.inspect` | Inspect custom item |
| `magicheroes.class.admin` | Quản lý class |
| `magicheroes.level.admin` | Quản lý level/EXP |
| `magicheroes.skill.admin` | Quản lý skill/tree |
| `magicheroes.debug` | Debug |
| `magicheroes.bypass.requirements` | Bypass item requirement |
| `magicheroes.bypass.cooldown` | Bypass cooldown |
| `magicheroes.bypass.soulbound` | Bypass soulbound |

## Cấu trúc data folder

```text
plugins/MagicHeroes/
├── items/
├── skills/
├── skill-trees/
├── recipes/
├── loot-tables/
├── quests/
├── playerdata/
├── classes.yml
├── config.yml
├── lang/
└── tooltips/
```

## Build

Yêu cầu:

- Java 21
- Paper 1.21.x
- Gradle wrapper hoặc Gradle local

Windows:

```powershell
./gradlew.bat clean test shadowJar
./gradlew.bat runServer
```

POSIX:

```bash
./gradlew clean test shadowJar
./gradlew runServer
```

JAR output dự kiến trong `build/libs/`.

## Chưa hoàn tất hoặc chưa xác minh

Các phần dưới đây không được xem là production-ready:

- Build/test/Paper runtime full chưa được xác minh sau toàn bộ thay đổi hiện tại.
- SQLite default, MySQL/MariaDB storage, connection pool.
- Guild, friend/ignore, guild bank/rank/upgrade.
- Economy ledger và Vault economy adapter.
- Quest GUI, bossbar, NPC integration, objective đầy đủ.
- Waypoint config loader, cooldown/cost/world validation hoàn chỉnh.
- Skill GUI, cast time/channel/interrupt, toàn bộ mechanic DSL.
- Set bonus/gem effect/reforge/random rolls/upgrade chance thật sự vào `StatSnapshot`.
- Crafting GUI/station transaction hoàn chỉnh.
- Personal/party loot, loot chest, protection.
- Adapter thật cho PlaceholderAPI/Vault/MythicMobs/Citizens/ItemsAdder/Oraxen/Nexo.
- API custom stat/item type/mechanic/trigger/condition/modifier đầy đủ.

Xem checklist chi tiết:

- `docs/acceptance.md`
- `docs/p1-acceptance.md`
- `docs/p2-acceptance.md`
- `docs/p3-acceptance.md`
- `docs/p4-acceptance.md`
- `docs/p5-acceptance.md`
- `docs/api.md`
