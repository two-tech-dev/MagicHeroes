# P1 acceptance checklist

- [ ] `items/weapons/firebrand.yml` loads into immutable registry.
- [ ] Invalid item material/ID/stat rejects reload and retains prior registry snapshot.
- [ ] `/mh item give <player> firebrand 1` creates a diamond sword with item identity PDC.
- [ ] `/mh item inspect` reports `firebrand`, `sword`, version.
- [ ] `/mh item validate` and `/mh item reload` require separate permissions.
- [ ] `/mh item give` rejects amount outside 1..64 and unknown IDs.
- [ ] `StatCalculationService` includes armor, main hand, off hand; broken items excluded.
- [ ] Stat source breakdown identifies slot and stat value.
- [ ] Legacy `mh_*` item stats still contribute after typed stat keys are absent.
- [ ] Level/class requirements block interaction and player attacks.
- [ ] `magicheroes.bypass.requirements` bypasses requirement checks.
- [ ] Reload with invalid item file reports error and keeps previous valid template.
