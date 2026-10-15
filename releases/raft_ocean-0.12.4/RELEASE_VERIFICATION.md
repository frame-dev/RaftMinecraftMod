# Release verification — 0.12.4 preview

Quality pass verified locally on 2026-10-12 with Minecraft 26.3,
NeoForge 26.3.0.48-beta, Java 25, ModDevGradle 2.0.147 and Gradle 9.2.1.

## Current checks

- Isolated build and bootstrap server passed; the server saved and closed cleanly.
- Navigation: 161 assertions, including malformed-frame atomicity, large counts,
  requested speeds, mounted engine directions/reverse, anchoring and fuel.
- Movement: 2,904 native validation, 729 carry and 425 collision assertions.
- Construction: 3,641 assertions; 83 building/API and 29 machine/API assertions.
- Survival: 866 assertions, including chest cache invalidation and invalid recipe
  transactions. Salvage: 111 assertions, including stream pause/gap settling.
- Crafting: 262,811 runtime assertions. Packaged audit: 153 mod recipes,
  63 fitting 2x2, 90 requiring 3x3 and 1,419 loaded recipe rows.
- Research: 666 runtime assertions and 30 packaged projects/60 unique unlocks.
- Crew, trading, stations and creature/config suites passed.
- Native asset build: 18,797 fill/mount assertions; 75 meshes/13,476 quads.
- Packaged coverage: 156 3D items, 779 custom blocks and seven renderers.
- Surface audit: 1,035 models/207,356 atlas-safe faces and 75 meshes;
  no opaque coplanar overdraw and previous envelopes retained.
- Localization: 699 matching English/German keys and placeholder parity;
  all three original sounds and config presets validated.
- Story audit: seven bilingual journals, open ending, rarity, 28 reachable side
  sites, 2,003 clear trail cells and 11,626 protected authored core blocks.
- Fresh Ocean World passed 5,119 assertions, including floating engine drive,
  turning/carry, generation, island exploration and spawn behavior; clean shutdown.
- Addon example built against 0.12.4. API v2/save formats are preserved.

Evidence: build/release-0.12.4-quality.log, build/release-0.12.4-ocean.log
and build/release-0.12.4-addon.log.
Runtime logs are not distributed. The packager verifies jar copies,
ZIP integrity and per-file SHA-256 checksums.

## Pending

Follow docs/QUALITY_PATCH.md for the local playtest: deck movement, engine
reverse and biofuel duration, anchors and debris delay, double-chest rebuilding,
invalid recipe feedback and bilingual tooltips. Test real co-op latency and large
builds; inspect models/fills/JEI in Minecraft. Automated geometry validation
cannot establish live appearance or guarantee an entirely bug-free playthrough.
