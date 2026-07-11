# P0 acceptance checklist

Run `gradlew.bat clean test shadowJar`.

- [ ] One Bukkit damage event changes final damage once; no RPG health subtraction.
- [ ] Death and respawn use Bukkit lifecycle; respawn restores health and mana once.
- [ ] Regen uses elapsed seconds; changing actionbar interval does not change per-second rate.
- [ ] Legacy `playerdata/*.yml` loads; schema version and `.v0.bak` appear once; unknown keys survive migration.
- [ ] Profile writes run through profile I/O executor and atomic temp replacement.
- [ ] `/mh help`, `/mh reload`, `/mh class`, `/mh debug` work from console with permissions.
- [ ] Player-only item/GUI commands reject console; regular players cannot run admin nodes.
- [ ] Tab completion shows only permitted, implemented nodes; class uses `levelup`, not `addexp`.
- [ ] Broken custom item cancels attack/interact and emits one `ItemBreakEvent`.
- [ ] Broken item stats disappear; tooltip refreshes after every item mutation.
- [ ] GUI blocks drag, shift-click, number-key, double-click, and lower-inventory insertion.
- [ ] Chat input state clears on cancel, close, quit, and completed operation.
- [ ] Reload restarts one ticker, does not duplicate listeners/tasks, and keeps runtime alive on failure.
