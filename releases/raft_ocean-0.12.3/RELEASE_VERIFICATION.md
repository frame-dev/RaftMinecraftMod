# Release verification — 0.12.3 preview

Propulsion balance verified locally on 2026-10-12 with Minecraft 26.3,
NeoForge 26.3.0.48-beta, Java 25, ModDevGradle 2.0.147 and Gradle 9.2.1.

## Current checks

- Build and bootstrap server passed; the server saved and shut down cleanly.
- All 154 navigation assertions passed, including exact isolated base speed,
  paddle stacking/decay, mounted engine bearings/reverse, anchor and fuel checks.
- 2,904 native movement, 729 carry and 425 runtime collision assertions passed.
- 3,641 construction and 262,811 crafting assertions passed, alongside
  survival, crew, station, research, creature/config and machine/API regressions.
- Asset validation retained the same models as 0.12.2: 18,797 fill/mount
  assertions and 75 polygon meshes with 13,476 quads passed validation.

Evidence: build/release-0.12.3-speeds.log. No new geometry, island templates,
recipes or API interfaces were introduced. Fresh Ocean World and addon-example
checks were last run in 0.12.1; visual surface audits were last run in 0.12.2.
These are carried forward, not represented as newly executed checks.
The packager verifies jar copies, ZIP integrity and per-file SHA-256 checksums.

## Pending

Test individual engines, sails and paddle strokes in-game, including engine
reverse, anchor and walking on a moving deck. Targets are base contributions:
wind, mass, fuel speed, current, custom config and multiple sources affect
actual HUD speed. Paddle movement fades and the hull accelerates smoothly.
Live lighting/animation, network latency, large builds and third-party packs
still need local testing. No claim of a bug-free complete playthrough is made.
