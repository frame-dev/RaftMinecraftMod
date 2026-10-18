# Verification — 0.13.5 preview

Checked2026-10-18: Minecraft26.3, NeoForge26.3.0.48-beta, Java25,
ModDevGradle2.0.147, Gradle9.2.1. API3 retained; handshake13.5.

- Native propulsion-load suite1625 assertions passes, including connected fixture
  construction. Covers non-stacking sails on the same hull, opposed crosswind trims,
  upwind limits, no-wind/config-disabled feedback,100/101/110/111foundation boundaries,
  configured capacity, six real fueled engines on1024foundations, net exclusion,
  opposite engine directions, current while stalled, anchors and fuel conservation.
- Existing navigation161 assertions pass for mounting/reverse, exact requested
  base speeds, fuel, controls and save/load. The Ocean suite additionally checks
  powered movement and player carry.
- Full build/bootstrap/fresh Ocean World passed in navigation-0.13.5-checked.log:
  9602ocean-world assertions. Final reloaded-world proof after fixture correction:
  9606assertions in navigation-0.13.5-tracking-check.log, BUILD SUCCESSFUL.
- Existing server movement2904, collision425, carry729, construction3641, building/API83,
  machineAPI69, crafting263374, stations119, research778, crew53, receiver1969 and
  notebook133 assertions pass, alongside survival, salvage, trading and AI suites.
- Native models:955models/57828elements,168fittedGUI transforms plus7inherited
  transforms. Geometry, textures, sounds, recipes and progression were not changed.
- Packaged audit:850English/German keys, matching placeholders, sound/config presets;
  30research projects/62unlocks;7schematics/8gates. All pass.

Corrections made during verification:

- An earlier steering assertion used an upwind trim permitted by the old formula.
  It now checks legal crosswind counter-steering against the same current.
- The receiver removal fixture used an above-height position and queried dropped
  items in a chunk whose saved entities could be loaded but not actively tracked.
  Moving inside valid height and waiting only for entity loading were insufficient.
  The final fixture also waits, with a bounded timeout, for active entity ticking.
  Actual charge conservation assertions remain intact; the repeated-world run passes.
  Earlier failed smoke runs also hit shutdown errors; the final successful runs
  supersede them. No gameplay inventory-drop implementation was changed.

Final packaging/build/bootstrap evidence: build/navigation-0.13.5-package-verified.log.
All runs use isolated build/bootstrap-smoke and build/ocean-smoke worlds; no user
client/server world was tested or modified. Addon API signatures remain unchanged;
no new third-party integration test is claimed. Live UI layout, real co-op latency
and long-session/large-raft behavior still need local testing. Exact original-game
engine speed formulas are deliberately not claimed: the custom base speeds remain.

---

# Verification — 0.13.4 preview

Checked 2026-10-18 against Minecraft26.3, NeoForge26.3.0.48-beta, Java25,
ModDevGradle2.0.147 and Gradle9.2.1. API3 retained; handshake13.4.

- Final build/bootstrap/fresh Ocean World run passed. Notebook133 assertions cover
  all28 native note loot tables, component/count conservation, duplicate records,
  both menu data halves, personal isolation, inventory/cursor recovery, save/load,
  actual death attachment copy, read-only clicks, spectator restrictions, registered
  non-operator command and sneak/normal journal actions. Opening checks capture
  the native menu provider with a test player; they do not simulate rendered UI.
- Receiver1969 checks include notebook-button owner/reach validation and unchanged
  power/charge, alongside the existing antenna, scan and rotation tests.
- Fresh Ocean World7977 assertions pass in build/ocean-smoke/ocean_world_0_13_4_notebook;
  this includes all notebook checks and existing story generation/exploration tests.
- Existing server suites pass:425collision,2904movement,729carry,3641construction,
  83building/API,69machineAPI,263374crafting,119stations,778research,53crew/rescue,
  41outpost,111salvage,970survival,161navigation,593creature/config.
- Native asset checks pass:955models/57828elements,168fittedGUI transforms and
  7validated inherited transforms. No geometry, texture, recipe or balance changes.
- Packaged audits pass:835English/German keys with placeholder parity, sound/config
  checks;30research projects/62unlocks;7stable schematic IDs/8research gates.
- Initial 127-assertion build passed. A first launch stalled inside Gradle filesystem
  watching and was stopped before compilation; disabling watching resolved it.
  Final command: gradlew.bat -I tools/isolated-visual-build.gradle build
  runBootstrapSmoke runOceanSmoke --offline --no-parallel --no-watch-fs -x downloadAssets.
  Evidence: build/notebook-0.13.4-final.log, BUILD SUCCESSFUL.

Pending: live client text layout/paging, actual network synchronization/co-op,
legacy stored-book recovery in the user's save and full playthrough. The native
opening checks do not establish visual correctness. Existing movement/collision
live-testing limits remain. No public GitHub release is created for this change.

---

# Verification — 0.13.3 preview

Checked2026-10-18: Minecraft26.3, NeoForge26.3.0.48-beta, Java25,
ModDevGradle2.0.147, Gradle9.2.1. API3 retained; handshake13.3.

- Final build/bootstrap/Ocean run passed:1965receiver assertions (including support
  fixture construction),778research,53crew/rescue,263374crafting,988survival and existing server
  movement/carry/collision/construction/crafting/navigation/AI/automation suites.
- Fresh Ocean World7840 includes valid/invalid antenna layouts, extra antennas,
  mounted/hull rotations, exact portable drain, off/undercharge conservation,
  real structure scanning/caching, channel-button authorization, menu ownership,
  reach, save/load, antenna cache invalidation and exact charged-battery drops.
- Loaded crafting verifies charged basic-cell output and unchanged advanced-cell
  defaults. Research verifies early ownership and prior power-owner access.
- First compile corrected a client screen method's public visibility. Initial
  coverage/removal checks were corrected to count icons once and verify dropped
  components after native removal. Final successful runs supersede those attempts.
- Final asset-only build after overlap cleanup and inherited-model metadata repair
  passed955models/57828elements,168fittedGUI transforms plus7validated inherited
  folio transforms,243motion and18797fill/mount checks. Runtime Java is unchanged
  from the successful full server run. Alignment checks now derive live piece
  geometry instead of requiring an untracked smoke-export file; final portability
  build passed (build/radio-0.13.3-build-portability.log).
- Packaged exact registry coverage165items/782blocks/7renderers passed; new models
  remain original. Surface audit1045models/215881faces/75meshes passed against0.13.2
  with no unsafe UVs/coplanar opaque overdraw and baseline envelopes retained.
- Packaged research30projects/62unique unlocks, complete recipe rewards; seven
  existing schematic IDs/eight project gates retained.826EN/DE keys, placeholders,
  sounds and configuration presets passed.

Evidence: build/radio-0.13.3-verified.log and build/radio-0.13.3-final-assets.log.
Native world tests used an isolated fresh level. No user worlds or runtime client
output folders were modified. The prior0.13.2 example-addon proof is carried
forward; no additional external addon was installed for this feature.

Pending: client radar/tooltip presentation, visual mounting, battery/slot control
flow and real multiplayer latency. Single-channel explicit scanning is an original
Minecraft adaptation; numeric code entry, persistent notebook and multiple island
blips are not implemented in this preview. Existing handheld locators remain usable.
This build has not been published as a new GitHub release.

## Earlier verification records

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
