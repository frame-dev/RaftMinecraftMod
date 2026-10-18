# Verification — 0.13.2 preview

Checked2026-10-18 with Minecraft26.3, NeoForge26.3.0.48-beta, Java25,
ModDevGradle2.0.147 and Gradle9.2.1. API3 retained; handshake13.2 requires matching
clients and server. No geometry, world-template or propulsion changes.

- Final build/bootstrap/fresh Ocean World run passed. Research768 includes native
  non-hotbar inventory acquisition, all eight premature-study refusals, discovery
  without materials/automatic learning, lost paper, duplicate acquisition, player
  save/reload, actual death attachment copy, live menu actions and journal recovery.
- Crew/rescue53 includes guest exclusion, discovery union, sharing disabled and
  re-enabled, old crew codec defaults, save/reload and replacement-raft retention.
- Fresh Ocean World5824 checks include all seven guaranteed schematic items in
  actual native-generated main journal chests and inventory discovery afterward,
  existing journals/keys/puzzles, all28 field-note books and island/trail checks.
- Existing suites passed: crafting262811, movement2904, carry729, collision425,
  construction3641, machineAPI69, survival971, plus station/navigation/trading/
  salvage/creature/config and structural/building checks. No external addon loaded.
- Native existing model checks952models/57779elements,165GUI silhouettes,
  243motion and18797fill/mount checks passed. New folios alias the already checked
  original journal model; their definitions/parents and labels passed asset audit.
- Packaged schematic audit: seven stable IDs, eight unique project gates,
  guaranteed loot, ungated starter projects, original model references and both
  language labels. Research30projects/60unlocks and recipe discovery rewards passed.
- Localization804English/German keys, placeholder parity, sounds/config passed.

Evidence: build/schematics-0.13.2-verified.log. The earlier successful first build
is superseded by that final run. No user worlds or runtime client outputs changed.

Pending: client folio appearance, Explore/tooltips in both languages, and shared
acquisition under real network latency. Arbitrary mod/addon compatibility remains
unverified. Existing unlearned advanced projects need discovery; older learned
projects remain unlocked and old main journals provide a recovery path.
This is a preview build, not a newly published GitHub release.

## Earlier verification records

# Verification — 0.13.1 preview

Checked on 2026-10-18 with Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25,
ModDevGradle 2.0.147 and Gradle 9.2.1. API3 and movement protocol13.0 retained.

- Build and isolated bootstrap server passed. Runtime research: 686 assertions,
  including all 20 short-batch refusals, exact batch consumption, one-time study,
  existing research persistence and starter sun-dried-brick smelter access.
- Existing movement2,904/carry729/collision425/construction3,641, crafting262,811,
  machineAPI69, survival959 and remaining server suites passed without an addon.
- Packaged templates: 28 reachable side sites, 2,218 connected clear trail cells,
  7,996 protected authored core blocks preserved; irregular coastline checks pass.
- All 28 guaranteed field-note tables have native 26.3 component modifiers and
  bilingual title/pages. Localization: 793 matching English/German keys,
  placeholder parity, sound files and bounded configuration presets passed.
- Research assets: 30 projects, 60 unique unlocks, complete discovery rewards;
  starter access retained. Packaged JAR integrity and 20 batch costs verified.
- Fresh Ocean World: 5,810 runtime assertions passed, including generated
  story shorelines, clear trails, all 28 real loot-container field-note books,
  native journal progression, puzzles, persistence and neighboring island pools.

Evidence: build/story-0.13.1-build.log, build/story-0.13.1-assets.log,
build/story-0.13.1-ocean-verified.log.
The first ocean check stopped on an obsolete dimension assertion; its expectations
were corrected before rerunning in a new isolated world. No user worlds changed.
Live client book layout, larger-island exploration and actual co-op latency still
need playtesting. Existing generated chunks retain their earlier terrain/loot.

## Previous release evidence

# Release verification — 0.13.0 preview

Addon API v3 verified locally on 2026-10-15 with Minecraft26.3,
NeoForge26.3.0.48-beta, Java25, ModDevGradle2.0.147 and Gradle9.2.1.

## Current checks

- Final isolated build and server boot passed with example addon 1.2.0 loaded;
  both mods saved/closed cleanly. Loader metadata accepted the current main mod.
- 69 native machine API assertions passed: old lifecycle/menu/save behavior,
  multi-resource preflight/commit, exact component batches, blocked outputs,
  same-slot processing, insertion/extraction locks, generator readiness, paused
  sources, global disablement, stale handles, cable removal and all four rotated
  non-anchor footprint connections. A network touching two faces is counted once.
- Loaded example passed 13 native assertions, including the actual utility washer
  generating power and consuming water/sand, idle/blocked conservation and pause.
- Example addon compiled. Standalone SDK starter compiled independently with
  local bundled API dependency and wrapper; no parent project is required.
  The final SDK ZIP was also freshly extracted and built with its own wrapper.
- Final runtime suites: 2,904 movement, 729 carry, 425 collision, 3,641 construction,
  263,322 crafting, 978 survival and 161 navigation assertions plus remaining
  station/research/crew/trading/salvage/creature/config suites passed.
- Native model/motion checks passed unchanged: 952 models/57,779 elements,
  165 fitted GUI silhouettes, 243 rotor/detail, 18,797 fill/mount checks,
  75 meshes/13,934 quads. Packaged coverage/surface bounds and UV audits passed.
- Packaged crafting: 153 main recipes (63 fit 2x2; 90 require 3x3), 1,422 loaded
  rows including three example recipes. Research: 30 projects/60 unlocks.
  Localization: 708 English/German keys, placeholder parity, sounds/config passed.

Evidence: build/release-0.13.0-verified.log,
build/release-0.13.0-addon-build.log, build/release-0.13.0-sdk-build.log
and build/release-0.13.0-sdk-extracted.log.
Earlier API development runs are superseded by the verified run above.
The example jar was installed only into the isolated test server's mods folder
and is removed there after testing. It is not installed in the user's client.

Packages verify jar copies, ZIP integrity and SHA-256 checksums. SDK archives
exclude build caches, worlds, run directories and runtime logs.
Fresh Ocean World/story template audits are carried forward from 0.12.4;
world generation, story content, collision envelopes and model resources were
not altered by the API expansion. The open-ended story and requested propulsion
speeds are retained.

## Pending

Install matching main/addon jars on a local client and server. Inspect washer
models, pipe/cable joins at four rotations, upper-floor/wide connections, resource
shortages, output locks, saves and shared pickup under actual network latency.
Test your own addons/mod combinations; API contracts do not guarantee arbitrary
world-machine transplantation or automatic research/JEI integration.
