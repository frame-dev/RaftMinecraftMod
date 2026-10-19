# Verification — 0.13.8 preview

Checked2026-10-19: Minecraft26.3, NeoForge26.3.0.48-beta, Java25,
ModDevGradle2.0.147, Gradle9.2.1. API3; handshake13.8.

- Full build/bootstrap pass: build/juicer-0.13.8-final.log. A separate fresh
  Ocean run passes9873assertions in build/juicer-0.13.8-ocean.log, including
  the same127-assertion juicer suite. Isolated world: ocean_world_0_13_8_juicer.
  No gameplay/source changes followed those successful runs; user worlds and
  active client outputs were not modified.
- Native juicer coverage includes all three actual loaded four-food recipes,
  role-slot filters/private liquid access, explicit single starts, foreign/carried
  controls, pause and real raft serialization, empty batteries, disabled config,
  exact four-food completion, idle conservation, ready-liquid reload, missing
  glasses, wrong/full outputs, exact physical-glass serving, duplicate requests,
  speed/charge accounting, output merging, hydration caps, bonus disable, native
  drinking-glass return, recipe-change cancellation, moving reach and detached
  handles. Dismantle checks await active entity tracking and prove four remaining
  food drops with zero new filled drinks. Fixtures clean up their own new drops.
- Existing crafting263891, research784, movement2904, carry729, collision425,
  notebook133, receiver1969, outposts50, compactor30, cooking64, survival968,
  propulsion1625, navigation161 and creature/config593 assertions pass.
  Crafting audit covers159mod plus1266other/vanilla recipes,12006placements,
  16019ingredient alternatives and621recipes fitting2×2. Glass bottles remain
  craftable. Native machine synchronization explicitly verifies all nine types.
- Native model parser accepts966models/58064elements;178GUI silhouettes fitted
  and centered plus7inherited aliases. Packaged172items/787blocks/7renderers,
  atlas-safe surfaces and all prior model envelopes pass. Three world juicer
  variants span exactly the normalized unit cube; separate inventory geometry
  preserves the physical1.5/.875height/width ratio. New models reuse original
  textures; no Raft game assets are copied.
- Packaged912English/German keys/placeholder parity, three original sounds,
  config preset ranges,30research projects/64unlocks and7schematics/8gates pass.
  The native juicing serializer and optional JEI category are packaged. Recipe
  sync works without JEI; live JEI appearance/reload remains a client playtest.
- Initial checks caught a removed JEI CATALYST enum (verified and corrected to
  CRAFTING_STATION in the installed31.9.0.58source), a glass-bottle recipe conflict
  (fixed with a distinct five-glass recipe), an extra unused world-model GUI
  transform (removed after splitting world/inventory models), and an old test
  expecting eight sync types (updated to verify nine including juicing).
  The final full build/bootstrap and fresh Ocean runs pass; no failed check remains.
- Runtime JAR SHA256:
  1C83D0E4157438C58F03865D471E3AE1C41690FB933014D3B5F2E6694F3874B2.
  Runtime and sources JARs copied to build/libs match their isolated build hashes.
  Known host Perflib/JNA hardware-report diagnostics recur without failing suites.

Pending: live juicer-panel/JEI appearance, actual co-op latency, full survival
balance and older user saves. Native portable batteries only for the juicer;
no wired/foreign energy support, bonus-water reserve or invalid-recipe leftovers.
Glasses are intentionally reusable. Unserved liquid spills on dismantle.
No public GitHub release or third-party technology-mod playtest is claimed.

---

# Verification — 0.13.7 preview

Checked2026-10-18: Minecraft26.3, NeoForge26.3.0.48-beta, Java25,
ModDevGradle2.0.147, Gradle9.2.1. API3; handshake13.7.

- Full build, bootstrap and a fresh Ocean World pass in
  build/compactor-0.13.7-verified.log (BUILD SUCCESSFUL). The isolated Ocean level
  is ocean_world_0_13_7_recycling;9746Ocean assertions include the new compactor
  and outpost suites. User worlds and active client outputs were not modified.
- Thirty native compactor assertions cover mixed exact-point batches, component
  protection, native menu slot rules, portable-charge conservation, empty power,
  blocked/four-bale output, configuration disable, saved progress/manual pause,
  actual raft serialization, independent restored state, exact-once completion,
  filtered shift transfer, foreign controls/carried-item rejection, moving reach
  and invalid detached contexts.
- Fifty native outpost assertions cover existing payments, exact bale payments,
  native lamp pickup, wrong/insufficient currency, shared stock, saved uses,
  optional-field legacy migration with retained uses/restock time, idempotence,
  restock, concurrent sessions, reach and disabled trading.
- Existing crafting263560, research781, notebook133, receiver1969,
  movement2904, carry729, collision425, survival952, cooking64,
  propulsion1625, navigation161 and creature/config593 checks pass.
- Native parser accepts958models/57897elements;171GUI silhouettes are centered
  and fitted, with7inherited aliases. Package coverage167items/784blocks/7renderers
  passes. Surface audit finds no opaque coplanar overdraw or UV overflow and
  preserves all prior model envelopes. New models reuse original mod textures.
- Packaged English/German879key/placeholder parity, three original sounds,
  bounded configuration examples,30research projects/63unlocks and7schematics/
  8gates pass. Crafting audit covers all157mod recipes and the existing vanilla
  2×2/3×3 station paths,12000placements and16005ingredient alternatives.
- Final JAR SHA256:
  0CA0834EED88D72E01918744EBA931E595E76CEB7D9BEF51ADA7678F17EF9E16.
  Verified runtime and sources JARs are copied to build/libs; their hashes match
  the isolated build outputs. No failed check remains. Known host Perflib/JNA
  diagnostics recur during hardware reporting and do not fail the mod suites.

Pending: live compactor-screen appearance, actual co-op latency, older user saves,
full economy balance and playthrough. The compactor currently uses native portable
batteries, without wired/foreign energy support or a dedicated JEI recycling
category. Coins, shop reputation and special trading fish are not implemented.
No public GitHub release or new third-party integration test is claimed.

---

# Verification — 0.13.6 preview

Checked2026-10-18: Minecraft26.3, NeoForge26.3.0.48-beta, Java25,
ModDevGradle2.0.147, Gradle9.2.1. API3; handshake13.6.

- New native cooking-batch suite64 assertions passes for all five loaded pot recipes,
  idle fuel/supply conservation, one queued batch, duplicate requests, exact output,
  explicit second batches, native menu ownership/carried-item rejection, pause,
  actual raft save/load, older-tag migration, recipe switching, blocked cup/output,
  physical cup return and automatic compatibility mode.
- Full final build/bootstrap passed: build/cooking-0.13.6-final.log. Fresh Ocean
  World9666 passed in build/cooking-0.13.6-verified.log using an isolated new level.
  The combined earlier run failed its bootstrap configuration-speed fixture, which
  still assumed automatic pot startup; the final fixture explicitly starts cooking
  and the unchanged expected speed assertion passes. That test-only correction
  followed the successful Ocean run; gameplay Java is unchanged between those proofs.
- Existing crafting263374, research778, receiver1969, notebook133, propulsion1625,
  navigation161, movement2904 and other survival/building/AI suites pass.
- Native model parser and geometry/motion checks pass with existing955models and
  57828elements,168fittedGUI transforms plus7inherited transforms. No model, texture,
  recipe or loot files changed. Client code remains in client classes.
- An additional full build/bootstrap with manualCooking=false passed, including
  invalid-recipe feedback in automatic mode. Latest artifact evidence is
  build/cooking-0.13.6-auto-verified.log. Packaged checks pass for858English/German
  keys, sound/config presets,30research projects/62unlocks and7schematics/8gates.

Pending: live Cook-button layout, actual co-op latency, older user saves and a full
playthrough. Four-food pads and collecting a finished dish with a bowl afterward
are not implemented. This delivery keeps the current two-food/water/bowl recipes.
No new third-party integration test or public GitHub release is claimed.

---

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
