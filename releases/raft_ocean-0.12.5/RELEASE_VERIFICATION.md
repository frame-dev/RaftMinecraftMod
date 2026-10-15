# Release verification — 0.12.5 preview

Model and animation pass verified locally on 2026-10-15 with Minecraft 26.3,
NeoForge 26.3.0.48-beta, Java 25, ModDevGradle 2.0.147 and Gradle 9.2.1.

## Current checks

- Final isolated build and bootstrap server passed and closed cleanly.
- Native parser: 952 models/57,779 elements; 165 fitted, centered GUI models.
- 243 rotor continuity/normalized-detail and 18,797 fill/mount checks passed.
- 75 polygon meshes/13,934 quads validated for materials, vertices and normals.
- Packaged coverage: 156 3D items, 779 blocks and seven entity renderers.
- Surface audit: 1,035 models/215,651 atlas-safe faces and 75 meshes;
  no opaque coplanar overdraw; prior visual envelopes retained.
- Runtime: 2,904 movement, 729 carry, 425 collision, 3,641 construction,
  262,811 crafting, 869 survival and 161 navigation assertions passed.
- Remaining server station, research, crew, trading, salvage, creature/config
  and addon API suites passed. Machine frame regressions cover malformed
  records, duplicate cells, numeric clamping, complete replacement and clearing.
- Packaged recipe/research/localization audits passed: 153 recipes (63 fit
  2x2; 90 require 3x3), 1,419 recipe rows, 30 projects/60 unlocks, 699 matching
  bilingual keys, three sounds and bounded configuration presets.

Evidence: build/release-0.12.5-final.log. The earlier development run
build/release-0.12.5-art.log caught a snapshot compatibility regression;
it was fixed, and the complete final run above passed.

Ocean World (5,119 assertions), story/template audits and addon example build
were last run in 0.12.4 and are carried forward: generation, stories, recipes,
physical collision envelopes and API interfaces did not change in this patch.
Software previews inspect actual geometry; they are not Minecraft screenshots.
The package verifies jar copies, ZIP integrity and SHA-256 checksums.

## Pending

Follow docs/UTILITY_MODELS_AND_MOTION.md: four placement rotations, wet/dry
bricks, biorefiner mixing, stirrer start/stop/pause, water alignment, inventory
and JEI. Test real client lighting, resource packs, walking near machines on a
turning raft and actual co-op latency/large builds. Automated checks cannot
establish a completely bug-free playthrough or final live visual quality.
