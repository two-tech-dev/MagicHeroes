# P2 combat and progression acceptance

- [ ] One Bukkit `EntityDamageEvent` enters `CombatService` once and receives only final `event.damage`.
- [ ] Melee/projectile include typed `ATTACK_DAMAGE`; environmental damage does not.
- [ ] Crit uses seedable random test fixture; cancelled critical event removes crit.
- [ ] Armor/magic penetration reduces effective defense but never below zero.
- [ ] Fire/ice/lightning/poison resistance clamps to -100% through 95%.
- [ ] True damage bypasses defense; all paths retain Bukkit death/respawn ownership.
- [ ] Pre/post/crit/heal/kill/death events fire on Paper main thread.
- [ ] `FIREBRAND` attack applies item damage once; broken/requirement failure cannot attack.
- [ ] `/mh level addexp <player> <amount>` grants levels, attribute points, skill points; caps level at 100.
- [ ] `/mh attributes show|add|reset` changes persistent attributes and recalculates health/mana/defense/damage.
- [ ] `playerdata/*.yml` persists progression points and attribute map.
- [ ] `/mh stats` reports cached totals and equipped stat sources.
