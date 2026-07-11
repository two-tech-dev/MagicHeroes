# P4 item systems acceptance

- [ ] Item template tier, set ID, socket count write PDC on creation.
- [ ] Upgrade adds exactly one level on success, never exceeds cap, refreshes tooltip.
- [ ] Socket count caps at six; gem placement cannot exceed sockets.
- [ ] Soulbound owner may use item; other player blocked unless bypass permission.
- [ ] Recipe registry rejects bad amount/chance/duplicate ID and retains old snapshot.
- [ ] Craft verifies every ingredient before removal; failure removes materials once and grants no result.
- [ ] Loot table gives all guaranteed entries plus exactly one weighted entry; seeded RNG deterministic.
- [ ] Empty/invalid weighted table cannot crash or overflow.
- [ ] Set config validates bonus piece counts/stats; equipped set aggregation remains pending stat integration.
- [ ] Reforge/random rolls/item identification modifiers/repair GUI remain pending.
