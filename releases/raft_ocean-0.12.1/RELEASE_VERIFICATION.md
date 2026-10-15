# Release verification — 0.12.1 preview

Verified locally on 2026-10-12 with Minecraft 26.3, NeoForge 26.3.0.48-beta,
Java 25, ModDevGradle 2.0.147 and Gradle 9.2.1.

Includes added net cargo-count checks, precise pickup feedback and owner-specific placement hints.

## Passed

- Release build plus bootstrap and fresh Ocean World server checks. Both
  isolated servers saved and closed cleanly.
- 425 collision, 2,904 native movement-packet, 729 carry and 3,641 construction
  assertions; building/machine API, coastal utilities and station checks.
- 262,811 crafting assertions, 666 research assertions, crew/rescue, outpost
  trading, salvage, survival, navigation and creature/config checks.
- 5,119 fresh Ocean World assertions.
- Native model parser: 952 models and 45,566 elements; 165 fitted inventory
  silhouettes; 18,797 fill/mount alignment checks; 75 meshes and 13,200 quads.
- Packaged coverage: 156 3D items, 779 custom blocks, seven entity renderers.
  Surface/UV audit: 1,035 models and 75 meshes without opaque coplanar overdraw.
- 695 English/German localization keys, 30 research projects and 60 unlocks.
- 153 mod recipes: 63 fit 2x2, 90 require 3x3; all 1,419 loaded crafting rows
  agree with the native crafting audit.
- All 27 island templates are byte-identical to the preceding 0.11.41 jar.
  Story validation against that build verifies 28 reachable side areas,
  2,003 connected clear trail cells and 11,626 protected authored core blocks.
  This baseline includes the intentionally graded natural terrain.
- API example addon build against the 0.12.1 jar.
- Packaged version/loader metadata and `raft-movement-12.1` protocol.

The packaging tool verifies ZIP integrity, every included SHA-256 checksum,
and equality between the built jar and the archived jar. Runtime worlds,
server properties, private configuration and logs are excluded.

## Pending local playtesting

Live client visuals, actual network delay, large-raft performance and a complete
co-op session have not been signed off. Third-party modpacks have not been
fully tested. This is why 0.12.1 is identified as a preview release.

Project-local evidence: `build/release-0.12.1-final.log` and
`build/release-0.12.1-addon.log`. These runtime logs are not distributed.
