# P3 acceptance

- [ ] `skills/**` validates into immutable registry; invalid YAML keeps previous snapshot.
- [ ] `/mh skills` lists locked/unlocked skills.
- [ ] `/mh skill unlock <id>` unlocks skill; `/mh skill bind <1-9> <id>` persists binding.
- [ ] Skill cast checks level/class/unlock/target/range/cooldown before mana spend.
- [ ] Failed cast spends zero mana and starts no cooldown.
- [ ] Successful cast spends mana exactly once and cooldown rejects repeat.
- [ ] `magicheroes.bypass.cooldown` skips cooldown only.
- [ ] `slash` damages target through Bukkit damage path; no second custom health subtraction.
- [ ] `minor-heal` heals Bukkit health and clamps max.
- [ ] Skill tree node checks parent, incompatible node, max level, and point cost.
- [ ] `/mh skill tree <node>` unlocks node and unlocks attached skill.
- [ ] Quit clears cooldown/cast state; restart keeps skill bindings/unlocks/tree levels.
