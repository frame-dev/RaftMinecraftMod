# Release verification — 0.12.2 preview

Model refinement verified locally on 2026-10-12 with Minecraft 26.3,
NeoForge 26.3.0.48-beta, Java 25, ModDevGradle 2.0.147 and Gradle 9.2.1.

## Current checks

- Final asset build passed. Native Minecraft parser accepted 952 models and
  52,670 elements; all 165 GUI silhouettes fit and remain centered.
- 18,797 machine-fill and mounted-geometry alignment checks passed.
- 75 polygon meshes and 13,476 quads passed vertex/material/normal validation.
- Packaged model coverage passed: 156 items, 779 custom blocks and seven renderers.
- Surface audit passed for 1,035 models and 75 meshes: UVs stay within their
  atlas sprites, no opaque coplanar overdraw, prior model envelopes retained.
- Localization, research and packaged crafting audits passed: 695 bilingual
  keys, 30 research projects, 153 mod recipes and 1,419 loaded recipe rows.
- The same 0.12.2 gameplay code passed the bootstrap server checks, including
  2,904 native movement assertions and crafting, survival, machine, crew and
  navigation regressions. It saved and closed cleanly.

The final starter-purifier art revision was checked by the final asset build
after that bootstrap run; no gameplay or physical collision code changed.
Fresh Ocean World checks were last run in 0.12.1 (5,119 assertions). This
cosmetic patch retains the same island templates and story data. API v2 and
the existing save format are retained; the addon example was last built in 0.12.1.

Evidence: `build/release-0.12.2-models.log` and
`build/release-0.12.2-assets.log`. Runtime logs are not distributed.
The packager verifies jar copies, ZIP integrity and per-file SHA-256 checksums.

## Pending

Inspect the five models in-game at all four placement rotations. Fill/empty
reservoirs, process food, smelt an item and check icons in inventory/JEI.
Software previews are not Minecraft screenshots. Real lighting/animation,
network latency, large builds and third-party packs still need local testing.
