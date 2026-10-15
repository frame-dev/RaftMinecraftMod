# Release verification — 0.12.6 preview

Interaction pass verified locally on 2026-10-15 with Minecraft 26.3,
NeoForge 26.3.0.48-beta, Java 25, ModDevGradle 2.0.147 and Gradle 9.2.1.

## Current checks

- Isolated build and bootstrap server passed; the server saved and closed cleanly.
- 968 survival assertions passed, including quick harvest across every machine,
  preserving input/fuel/battery roles, backpack-first storage, repeated pickups,
  partial/full inventories in survival/creative, config disablement, distance,
  guest permissions and spectator rejection. Existing net pickup tests passed
  using the same shared inventory transfer implementation.
- Runtime suites: 2,904 native movement, 729 carry, 425 collision, 3,641
  construction, 262,811 crafting and 161 navigation assertions passed, plus
  station/research/crew/trading/salvage/creature/config and API checks.
- Native assets: 952 models/57,779 elements, 165 fitted GUI silhouettes,
  243 rotor/detail checks, 18,797 fill/mount checks, 75 meshes/13,934 quads.
- Packaged model coverage and surface audit passed: 156 items, 779 custom
  blocks, seven renderers, 1,035 models/215,651 atlas-safe faces and 75 meshes.
  Previous visual bounds retained; no opaque coplanar polygon overdraw.
- Localization: 708 English/German keys and placeholder parity, three original
  sounds and bounded config presets. Research: 30 projects/60 unique unlocks.
- Packaged crafting audit: 153 recipes, 63 fitting 2x2, 90 requiring 3x3;
  all 1,419 loaded recipe rows present.

Evidence: build/release-0.12.6-interactions.log. The package verifies jar
copies, ZIP integrity and per-file SHA-256 checksums.
Fresh Ocean World (5,119 assertions), story/template audits and addon-example
build are carried forward from 0.12.4. This patch does not change generation,
story, recipes, model geometry, physical collision or existing API interfaces.

## Pending

Follow docs/QUICK_MACHINE_HARVEST.md: finish recipes and collect outputs,
check full/partial inventories and repeated crew pickups, test cups/ordinary
menus, disable each setting, inspect prompts in a small window and hide HUD.
Live client appearance, real multiplayer latency, large builds and earlier
movement/collision reports still need user confirmation. Automated checks do
not establish a fully bug-free playthrough.
