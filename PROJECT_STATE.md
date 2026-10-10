# PROJECT STATE



- Updated: 2026-10-10.
- Latest: Raft Ocean 0.11.30; artifact build/libs/raft_ocean-0.11.30.jar.
  Targets: Minecraft26.3, NeoForge26.3.0.48-beta, Java25,
  ModDevGradle2.0.147, Gradle9.2.1, JEI31.9.0.58. Protocol11.30/API2.
  Disposable Stone Anchor: exact cost4cobblestone/4string/2planks/1scrap,
  using a stone bundle intermediate to fit native3x3 crafting. No research.
  Lowering stops raft/debris; releasing consumes anchor; deployed state persists.
  Used anchor cannot be reclaimed through hammer removal or foundation damage.
  Basic Food Grill:4planks/2scrap/1string; plank-fueled campfire cooking, no
  research/water/metal fittings. Native food filters, UI, animations and JEI links.
  Crates/barrels offer2–4cobblestone per selected entry; original colored models.
  Build/native server bootstrap passed:259199crafting,666research,848survival,
  971construction assertions plus movement/collision/navigation/gameplay suites.
  Packaged audits:1399recipes(133mod),132items,357blocks,577bilingual labels;
  561models/64meshes surface-safe, old model envelopes preserved.
  Guide docs/STARTER_ANCHOR_AND_GRILL.md;docs/crafting-audit-0.11.30.csv;
  log build/starter-anchor-runtime.log. Finished1–10;current11;story open-ended.
  Pending local new-station/JEI/co-op confirmation and previous movement/collision/
  large-raft reports. Shark600s/bird300s retained. Release12 not started.
  No new world needed. Install0.11.30 on all clients and server.

- Retained: Raft Ocean 0.11.29; artifact build/libs/raft_ocean-0.11.29.jar.
  Targets: Minecraft26.3, NeoForge26.3.0.48-beta, Java25,
  ModDevGradle2.0.147, Gradle9.2.1, JEI31.9.0.58. Protocol11.29/API2.
  Basic wooden purifier and Sun-Drying Rack are craftable before research.
  Rack dries damp bricks in150sunny seconds, pausing at night/rain/under cover;
  saves progress, requires no fuel/water, and preserves blocked output inputs.
  Smelter research accepts dry or fired bricks plus clay; crafting now uses
  six dried bricks and two planks, removing the early metal/furnace dependency.
  Existing metal purifier renamed Advanced Water Purifier; existing saves retained.
  Custom models, placement parts, English/German labels and eight JEI categories.
  Build/native server bootstrap passed:258540combined crafting,666research,
  726survival,953construction assertions and movement/collision/gameplay suites.
  Packaged audits passed:1396recipes(130mod),129items,344blocks,7renderers,
  560bilingual keys;543models/64meshes surface-safe, baseline envelopes preserved.
  Guide docs/STARTER_SURVIVAL.md;docs/crafting-audit-0.11.29.csv;
  log build/starter-survival-runtime.log. Finished1–10;current11;story open-ended.
  Pending local new-machine/JEI/co-op confirmation and previous human movement/
  collision/large-raft reports. Shark600s/bird300s retained. Release12 not started.
  No new world needed. Install0.11.29 everywhere.

- Retained: Raft Ocean 0.11.28; artifact build/libs/raft_ocean-0.11.28.jar.
  Targets: Minecraft26.3, NeoForge26.3.0.48-beta, Java25,
  ModDevGradle2.0.147, Gradle9.2.1, JEI31.9.0.58. Protocol11.27/API2 retained.
  Reproduced and fixed stale crafting-result pickup after research access changes.
  Result click and direct shift transfer revalidate before mutation; reconcile
  refreshes inventory and active crafting grids immediately. All four native
  crafting menus covered, including config toggles and leaving Creative.
  Fixed standalone archery/battery generators restoring obsolete research gates;
  separate-copy generation verified. Corrected diving audit; new research asset
  audit checks30projects/60uniqueunlocks, discovery rewards and starter access.
  Build/bootstrap/oceanSmoke passed:257837crafting,666research,756ocean-world
  assertions plus other gameplay suites. Package recipe/model/diving/localization/
  research/surface audits pass:126items,334blocks,7renderers,542bilingualkeys.
  Guide docs/PROJECT_REVIEW_0_11_28.md;docs/crafting-audit-0.11.28.csv;
  log build/project-review-runtime.log; pre-fix evidence project-review-reproduction.log.
  Finished1–10;current11;story open-ended;release12 not started.
  Pending live JEI/co-op/model confirmation and previous human movement/collision/
  large-raft reports. No additional automated collision or texture failures found.
  Shark600s/bird300s delays retained. No new world needed. Install0.11.28 everywhere.

- Retained: Raft Ocean 0.11.27; artifact build/libs/raft_ocean-0.11.27.jar.
  Targets: Minecraft26.3, NeoForge26.3.0.48-beta, Java25,
  ModDevGradle2.0.147, Gradle9.2.1, JEI31.9.0.58. Protocol11.27/API2.
  Research expanded24→30: biofuel, water distribution, electrical storage,
  diving, reinforced weapons and travel provisions. Stable old project indices;
  previous24-project saves remain learned, new projects require explicit learning.
  Five new items: metal spear, reef machete, reef stew, orchard compote,
  seafarer supper; original custom models, fitted inventory silhouettes,
  native iron-tier weapons/enchantment tags and configurable food bonuses.
  Meals use four-ingredient crafting recipes and return bowls on eating.
  Native research saving, command unlock, discovery and every crafting menu tested.
  Build/bootstrap passed:1394 recipes(128mod+1266vanilla),257837 combined
  crafting assertions plus research/survival/movement/collision/gameplay checks.
  Package audits passed:126items,334blocks,7renderers,542 bilingual labels;
  528 model surfaces and60 meshes safe, prior model envelopes preserved.
  Guide docs/RESEARCH_EXPANSION.md;docs/crafting-audit-0.11.27.csv;
  log build/research-expansion-runtime.log. All clients/server must use0.11.27.
  Finished1–10;current11;story open-ended;release12 not started.
  Pending local JEI/co-op/model confirmation and previous movement/collision/
  large-raft reports. Shark600s/bird300s delays retained. No new world needed.

- Retained: Raft Ocean 0.11.26; artifact build/libs/raft_ocean-0.11.26.jar.
  Targets: Minecraft26.3, NeoForge26.3.0.48-beta, Java25,
  ModDevGradle2.0.147, Gradle9.2.1, JEI31.9.0.58. Protocol11.25/API2 retained.
  New administrative /raft research unlock [targets]: self by default;
  server console must specify online player(s), e.g. @a. Existing raft command
  gamemaster/cheat permission gate applies. Permanent all-project/all-material
  unlock, owner knowledge sync, recipe discovery, research advancements and
  immediate grid-output refresh. No item costs or gamemode/config changes.
  Native command regression covers execution, repeat, all recipe/advancement
  coverage, Survival/sample preservation, open locked grid and console selector.
  Build/bootstrap passed:1389 recipes,256827 combined crafting,553 research
  assertions and other gameplay suites. English/German feedback included.
  Guide docs/RESEARCH_COMMAND.md;log build/research-command-runtime.log.
  Finished1–10;current11;story open-ended;release12 not started.
  Pending local JEI/co-op confirmation and prior movement/collision/large-raft
  reports. Shark600s/bird300s delays retained. No new world needed.

- Retained: Raft Ocean 0.11.25; artifact build/libs/raft_ocean-0.11.25.jar.
  Targets: Minecraft26.3, NeoForge26.3.0.48-beta, Java25,
  ModDevGradle2.0.147, Gradle9.2.1, JEI31.9.0.58. Protocol11.25/API2.
  User reversed the conversion exemption: matching research now gates vanilla
  station conversions again. All research-gated crafting recipes hidden from
  server recipe-book discovery and client JEI until explicitly learned.
  Learning immediately awards associated recipes. Saved books reconciled on login;
  reset/config/Creative changes reconciled. Knowledge attachment owner-only sync.
  All clients/server must use0.11.25 for updated protocol. Existing items usable;
  no save migration. Build/bootstrap pass:1389 recipes,256827 combined crafting,
  450 research assertions, plus all other gameplay suites. Regression coverage
  includes blocked consumption, discovery rewards, stale saved book entries,
  learning, reset, config and gamemode. Packaged audit verifies storage locks.
  Guide docs/RESEARCH_RECIPE_DISCOVERY.md;docs/crafting-audit-0.11.25.csv;
  log build/research-discovery-runtime.log. Finished1–10;current11;story open-ended.
  Release12 not started. Pending local JEI live hide/reveal and co-op confirmation,
  plus previous human movement/collision/large-raft reports. Shark600s/bird300s kept.

- Retained: Raft Ocean 0.11.24; artifact build/libs/raft_ocean-0.11.24.jar.
  Targets: Minecraft26.3, NeoForge26.3.0.48-beta, Java25,
  ModDevGradle2.0.147, Gradle9.2.1, JEI31.9.0.58. Protocol11.17/API2 retained.
  Fixed vanilla chest/barrel/workstation conversions being hidden by output-only
  research gate. Matching one-block vanilla-to-raft conversions now bypass research;
  materials-based recipes and all other projects retain their restrictions.
  New native regressions cover all12 conversions before research in2x2/3x3,
  right-click on two inputs, shift-click remaining input and locked materials chest.
  Build/bootstrap pass:1389 recipes,256579 combined crafting assertions,
  267 research assertions and all other gameplay suites. Updated CSV/report:
  docs/VANILLA_MOUNTING_FIX.md;docs/crafting-audit-0.11.24.csv;
  log build/vanilla-mounting-runtime.log. No world migration required.
  Finished1–10;current11;story open-ended;release12 not started.
  Pending local JEI/recipe-book confirmation and previous human co-op movement,
  collision/large-raft performance reports. Shark600s/bird300s retained.

- Retained: Raft Ocean 0.11.23; artifact build/libs/raft_ocean-0.11.23.jar.
  Targets: Minecraft26.3, NeoForge26.3.0.48-beta, Java25,
  ModDevGradle2.0.147, Gradle9.2.1, JEI31.9.0.58. Protocol11.17/API2 retained.
  Native audit covers all1389 loaded crafting IDs:123 mod and1266 Minecraft/NeoForge.
  Fifty mod recipes fit2x2;73 require3x3. Inventory, raft, wide raft and vanilla
  menus cover extraction/remainders, research gates, offsets/mirrors, tag alternatives.
  Fixed hammer conflict with vanilla wooden hoe and paddle interception of the
  shapeless timber brace recipe: each tool now uses one additional binding.
  Recipe generators and crafting guide updated. Native extended audit256779
  assertions,11662 placements,15602 ingredient alternatives,64 dynamic fixtures;
  combined crafting suite256827. Build and all bootstrap gameplay suites passed.
  Report docs/CRAFTING_AUDIT_0_11_23.md and docs/crafting-audit-0.11.23.csv;
  log build/crafting-audit-runtime.log. No world migration required.
  Shark600s/bird300s delays retained. Finished1–10;current11;story open-ended;
  release12 not started. Pending local JEI transfer and recipe-book confirmation,
  plus prior human co-op latency/collision reports and large-raft FPS.

- Retained: Raft Ocean 0.11.22; artifact build/libs/raft_ocean-0.11.22.jar.
  Targets: Minecraft26.3, NeoForge26.3.0.48-beta, Java25,
  ModDevGradle2.0.147, Gradle9.2.1, JEI31.9.0.58. Protocol11.17/API2 retained.
  Maintenance review of carry/clearance, rendering, menu transactions, recipe/JEI
  handling, creatures, addon ownership and asset/localization coverage.
  Carry chooses highest contact within existing tolerance instead of first lower
  foundation; fixes grounded state on thin platforms. Equal-distance recovery
  candidates have explicit coordinate tie-breakers. Sail renders authored cloth
  once instead of repeating whole texture/model across four animated strips;
  deployment, trim and gentle flutter retained. Colliders/models unchanged.
  Bootstrap completion now also requires native movement-validation marker.
  New native thin-addon regression covers40 consecutive rotating/bobbing carries,
  grounded state, feet height and clear player bounds;42 extra construction checks.
  Build/bootstrap/ocean suites pass:1210 movement,425 collision,729 carry,
  935 construction,756 ocean and all machine/crafting/survival suites.
  Native GUI/fill/mesh checks and all five packaged audits pass:523 models,
  163064 atlas-safe faces,60 meshes,521 bilingual keys;model envelopes unchanged.
  Guide docs/PROJECT_REVIEW_0_11_22.md;log build/project-review-runtime.log.
  Shark600s and bird300s delays retained. Finished1–10;current11;story open-ended;
  release12 not started. Pending local sail appearance, walking near objects,
  human co-op latency corrections, shader/resource-pack behavior and large-raft FPS.
  Rotated collision still uses conservative1/16-block strips; no new test failures.

- Retained: Raft Ocean 0.11.21; artifact build/libs/raft_ocean-0.11.21.jar.
  Targets: Minecraft26.3, NeoForge26.3.0.48-beta, Java25,
  ModDevGradle2.0.147, Gradle9.2.1, JEI31.9.0.58. Protocol11.17 retained.
  Shark default creatures.sharkRaftAttackDelaySeconds now600 (ten minutes).
  New creatures.birdCropTheftDelaySeconds defaults300 (five minutes), range0–3600.
  Birds have independent saved countdowns before first theft and after theft or
  interrupted approaches. Legacy birds receive grace; reload during dive gives
  full wait. Loaded ticking only;0 removes extra wait, old theft interval remains.
  Peaceful and covered planter rules retained. Native checks cover full12000-tick
  shark and6000-tick bird delays, save/reload, theft and scare-away resets.
  Development client/server gameplay configs updated to600/300 with adjacent
  .before-creature-delay-0.11.21.bak backups. Six examples and bilingual labels updated.
  Build/bootstrap/ocean suites pass:756 ocean assertions;521 bilingual keys and
  packaged model coverage pass. Log build/creature-delays-final.log.
  Guide docs/SHARK_ATTACK_DELAY.md. Finished1–10;current11;story open-ended;
  release12 not started. Pending local creature timing, prior visual confirmation,
  reported movement corrections under human latency and large-raft performance.

- Retained: Raft Ocean 0.11.20; artifact build/libs/raft_ocean-0.11.20.jar.
  Targets: Minecraft26.3, NeoForge26.3.0.48-beta, Java25,
  ModDevGradle2.0.147, Gradle9.2.1, JEI31.9.0.58. Protocol11.17 retained.
  New creatures.sharkRaftAttackDelaySeconds defaults300, range0–3600.
  Each shark has an independent saved countdown, before first raft attack and
  after completion, repelling or cancellation; not shortened by aggression/mode.
  Loaded gameplay ticks only. Legacy saves receive grace; reloading an active
  attack gives a full wait. Shorter settings cap existing waits; zero removes
  the extra delay. Old bite interval/repel cooldown and swimming combat remain.
  Peaceful behavior retained. English/German config labels and six examples updated.
  Guide docs/SHARK_ATTACK_DELAY.md. Build/bootstrap/ocean suites pass, including
  749 ocean assertions with delay/persistence/repel regressions;520 bilingual keys
  and packaged asset coverage pass. Log build/shark-delay-final.log.
  Finished1–10;current11;story open-ended;release12 not started.
  Pending: local attack timing/co-op observation, previous visual confirmation,
  reported movement corrections under human latency and large-raft performance.

- Retained: Raft Ocean 0.11.19; artifact build/libs/raft_ocean-0.11.19.jar.
  Targets: Minecraft26.3, NeoForge26.3.0.48-beta, Java25,
  ModDevGradle2.0.147, Gradle9.2.1, JEI31.9.0.58. Protocol11.17 retained.
  Polygon clipping extends the rectangle surface repair to convex coplanar
  polygons with original triangle UV interpolation.44 additional opaque faces
  clipped across7 barrel/tank/biorefiner meshes; opposite-facing surfaces retained.
  Native mesh checks validate60 meshes/11519 quads for finite positions/UVs,
  dimensions, unit normals and nonempty material groups. Exact model envelopes
  match0.11.18 and repeat repair is idempotent. No gameplay/collision changes.
  Build/server suites and packaged audits pass; log build/polygon-polish-runtime.log.
  Finished1–10;current11;story open-ended;release12 not started.
  Pending: user camera/sailing confirmation, shaders/resource packs, human
  co-op movement corrections and large-raft performance. Coplanar checks do
  not resolve arbitrary curved intersections in different planes or external packs.

- Retained: Raft Ocean 0.11.18; artifact build/libs/raft_ocean-0.11.18.jar.
  Targets: Minecraft26.3, NeoForge26.3.0.48-beta, Java25,
  ModDevGradle2.0.147, Gradle9.2.1, JEI31.9.0.58. Protocol11.17 retained.
  User reported texture flickering. Repair trims earlier opaque coplanar faces,
  preserving native UV orientation and model envelopes;10,052 out-of-atlas face
  UVs fixed,7,398 cuboid faces trimmed,79 rectangular polygon faces trimmed
  across15 station meshes. Reinforcement bands separated from wood sides/end joins.
  Physical collisions, machine anchors and animation frames unchanged.
  tools/model_surface_safety.py is the final generator stage; idempotent.
  tools/verify_model_surfaces.py audits source or packaged model surfaces and
  compares envelopes to0.11.17;523 models/163064 atlas-safe faces/60 meshes pass.
  Native build checks now enforce UV bounds;464 models/42657 elements accepted,
  130 GUI fits and6125 fill/mount checks pass. Build/server suites pass, including
  1210 movement,425 collision,729 carry,893 construction,83 building/API,
  29 machine API,17 coastal utility and all previous crafting/survival/creature suites.
  Four packaged audits pass:121 items,334 blocks,7 renderers,519 bilingual keys.
  Log build/texture-flicker-runtime.log. Guide docs/TEXTURE_FLICKER_FIX.md.
  Finished1–10;current11;story open-ended;release12 not started.
  Pending: user visual confirmation while turning/sailing; exact affected surface
  if flicker persists; shader/resource-pack interaction, human co-op latency and
  large-raft performance. Audit covers opaque rectangles, not arbitrary curved
  intersections or third-party packs. Previous addon/API limits retained.

- Retained: Raft Ocean 0.11.17; artifact build/libs/raft_ocean-0.11.17.jar.
  Targets: Minecraft26.3, NeoForge26.3.0.48-beta, Java25,
  ModDevGradle2.0.147, Gradle9.2.1, JEI31.9.0.58. Protocol raft-movement-11.17.
  Foundation reinforcement uses existing Reinforced Braces, consumes one per
  foundation, preserves damage, saves armor and adds original metal edging
  without new colliders. Sharks skip protected edges; hammer removal refunds
  one brace. Disabling building.foundationReinforcement retains armor records.
  Matching-material quick stash supports barrels and both halves of double
  chests; merges stacks then fills empty slots, preserves components, hotbar,
  equipment and excess cargo. Empty-hand sneak-click; building.quickStash toggle.
  Bug fixes: addon removal/destruction/final-support callbacks cannot recreate
  machine ownership; addon loaded hooks run after the saved hull frame and
  other raft state have been restored. Removing owners are skipped by host ticks/views.
  Fully armored rafts throttle unsuccessful shark foundation target scans.
  API v2 retained. Updated example1.1.1 builds against packaged0.11.17;
  archive build/libs/raft-addon-example-0.11.17.zip. Guides docs/COASTAL_UTILITIES.md
  and docs/MACHINE_ADDON_GUIDE.md. Sources build/libs/raft_ocean-0.11.17-sources.jar.
  Validation: build/server with example installed passed;17 coastal utility,
  29 machine API,8 example dryer,1210 native movement,425 collision,729 carry,
  893 construction,83 building/API,417 crafting,119 station,192 research,
  108 salvage,661 survival,130 navigation,593 creature/config assertions.
  All four final packaged audits pass:121 items/334 blocks/7 renderers/519
  bilingual keys;native parser464 models/25561 elements,130 GUI fits,6125
  fill/mount checks. Logs build/coastal-utility-final.log and build/coastal-addon-build.log.
  Finished milestones1–10;current11;story open-ended;release12 not started.
  Pending: visual/local co-op armor/stash tests, remaining reported movement
  corrections under human latency and large-raft performance. No unresolved
  automated failure from this update. Existing API integration limits retained.

- Retained: Raft Ocean 0.11.16; artifact build/libs/raft_ocean-0.11.16.jar.
  Targets: Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25,
  ModDevGradle 2.0.147, Gradle 9.2.1, JEI 31.9.0.58.
  Protocol: raft-movement-11.16; matching main-mod/addons on client and server.
  Public API v2 retains v1 buildable registrations and adds machine lifecycle,
  server ticking, independent saved inventories and bounded named properties,
  schema helpers/immutable property snapshots, filtered native inventory menus,
  custom-menu opening, moving reach/owner validation and synchronized visual states.
  Context lookup supports any occupied cell of a wide machine.
  Shared controllers remain stateless; runtime state belongs to each installed owner.
  Complete guide docs/MACHINE_ADDON_GUIDE.md; reference docs/RAFT_ADDON_API.md.
  Separate example includes work crate and fuel-powered kelp dryer, recipes,
  models, bilingual labels, saved processing/pause and an active visual variant.
  Example source zip build/libs/raft-addon-example-0.11.16.zip; addon jar
  examples/raft-addon/build/libs/raft-example-addon-1.1.0.jar; main sources jar
  build/libs/raft_ocean-0.11.16-sources.jar.
  Validation: build, 23 native machine API assertions, 83 building/API assertions,
  all existing runtime suites and four packaged audits pass. Separate addon build
  and server startup pass with 8 actual dryer assertions. Example JSON/textures
  verified. Logs build/api2-runtime.log, build/api2-example-build.log,
  build/api2-addon-runtime.log. Parser464 models/25561 elements;130 GUI fits;
  6125 fill/mount checks;121 items/334 blocks/7 renderers/514 bilingual keys.
  Finished milestones1–10; current11; story open-ended; release12 not started.
  Pending: restarted-client visual tests, human co-op latency, remaining movement
  reports and large-raft performance. No unresolved API failure in automated checks.
  API limits: baked visual variants/texture animation supported; arbitrary bones,
  automatic addon JEI categories and built-in utility/research network bridges
  require addon-owned integration. Inventory filters govern menus, not raw state writes.

- Retained: Raft Ocean0.11.15; artifact build/libs/raft_ocean-0.11.15.jar.
  Targets: Minecraft26.3, NeoForge26.3.0.48-beta, Java25,
  ModDevGradle2.0.147, Gradle9.2.1, JEI31.9.0.58. Protocol raft-movement-10.5.
  Eight new original buildables: Window Wall, Gable Wall, Open Archway,
  Half-height Stairs, Wide Stairs, Tall Ladder, Wide Floor, Wide Crafting Bench.
  Two-cell pieces reserve rotated footprints as one owner/item/refund; bench
  uses native3x3 crafting and JEI station integration; full base support enforced.
  Wide floor second-cell mounting/removal and damage pruning use full footprints.
  Existing enum ordinals and six-field saves retained; addon IDs use seventh field.
  Public common-side API v1 registers custom items, visual blocks, dimensions,
  geometry, collision, mass and server interactions. No generic machine/tick/
  animation framework yet. Matching addon definitions required on client/server.
  Independent example addon builds against packaged jar; source jar and example zip
  supplied. Guides docs/BUILDING_EXPANSION.md and docs/RAFT_ADDON_API.md.
  Build/bootstrap:83 API/expansion,893 construction,417 crafting,1210 native movement
  assertions plus all previous runtime suites pass. Parser464 models/25561 elements;
  130 fitted GUI silhouettes;6125 fill/mount checks; all four packaged audits pass.
  Coverage121 items (including hidden addon placeholder),334 blocks,7 renderers,
  514 bilingual keys. Log build/buildables-final-runtime.log. Example log
  build/addon-example-build.log. Preview docs/previews/building-expansion-0.11.15.png.
  Finished1–10; current11; story open-ended; release12 not started.
  Pending: restarted-client new building/visual/climbing tests, actual human
  co-op latency, remaining movement reports and large-raft performance.
- Retained: Raft Ocean 0.11.14; artifact build/libs/raft_ocean-0.11.14.jar.
  Targets: Minecraft26.3, NeoForge26.3.0.48-beta, Java25,
  ModDevGradle2.0.147, Gradle9.2.1, JEI31.9.0.58. Protocol raft-movement-10.5 retained.
  Original handmade coastal art pass: separated timber boards, square pillars,
  wooden bench legs with rope joints, angular painted engine casing and vents,
  open red grill grate, timber-framed purifier; warmer wood and weathered paint.
  Existing animation pivots, fill anchors and gameplay collision boxes retained.
  Full model generator now handles archery/battery extensions and bronze texture aliases.
  Build/bootstrap and all four packaged audits passed. Native parser397 models/
  23679 elements;121 fitted GUI silhouettes;4845 fill/mount alignment checks;
  1210 native movement checks and all existing runtime suites pass.
  Coverage112 3D items,276 custom blocks,7 renderers,505 bilingual keys.
  Log build/handmade-models-final.log. Guide docs/HANDMADE_MODELS.md.
  Preview docs/previews/handmade-models-0.11.14.png.
  Finished milestones1–10; current11. Story open-ended; release12 not started.
  Pending: restarted-client art/animation and walking confirmation, real human
  co-op latency, remaining intermittent corrections and large-raft performance.
- Retained: Raft Ocean 0.11.13; artifact build/libs/raft_ocean-0.11.13.jar.
  Targets: Minecraft26.3, NeoForge26.3.0.48-beta, Java25,
  ModDevGradle2.0.147, Gradle9.2.1, JEI31.9.0.58. Protocol raft-movement-10.5 retained.
  Landing correction now records platform movement and resets the native server
  movement baseline. Previously tickPlayer restored firstGood after doTick,
  undoing post-tick deck landing and sometimes putting feet back inside the deck.
  Native regression failed before the fix (landed347.251 -> restored347.270).
  New smoke fixture replaces FakePlayer's no-op connection with the actual native
  packet handler; checks post-tick landings above/below the deck and walking on
  interpolated/drifting/turning hulls near crafting tables, chests, sprinklers,
  battery chargers and stairs at six headings. 1210 movement assertions passed.
  Build/bootstrap passed, all existing runtime suites and four packaged audits passed.
  Log build/movement-fixed.log; guide docs/RAFT_MOVEMENT_FIX.md.
  Finished milestones1–10; current11. Story remains open-ended; release12 not started.
  Pending: user walking/jumping/landing confirmation, real human co-op latency,
  any other intermittent corrections, large-raft performance and client visuals.
- Retained: Raft Ocean 0.11.12; artifact build/libs/raft_ocean-0.11.12.jar.
  Targets: Minecraft26.3, NeoForge26.3.0.48-beta, Java25,
  ModDevGradle2.0.147, Gradle9.2.1, JEI31.9.0.58. Protocol raft-movement-10.5 retained.
  Recipe fix: Crumb Bait was silently gated by Bait preparation despite using only bread.
  Bread ->8 Crumb Bait now works before research in inventory and mounted grids;
  native right-click consumes one bread correctly. Baited Ocean Rod remains researched.
  Recipe-book unlock now uses bread; catalog/survival generators retain the fixes.
  Research.blockedProject identifies unmet requirements; native crafting shows a
  bilingual action-bar message naming the project and item instead of silent failure.
  Audited all115 shaped/shapeless crafting recipes through actual mounted native menus:
  correct result/count, native take and exact ingredient consumption with research unlocked.
  Build/bootstrap passed:425 collision,729 carry,820 construction,393 crafting,
  119 stations,192 research,108 salvage,661 survival,130 navigation,
  593 creature/config assertions. Models and all packaged audits passed.
  112 original 3D items,276 custom blocks,505 bilingual keys; art retained.
  Log build/recipe-fixes-second.log. Guide docs/RECIPE_FIXES.md.
  Finished milestones1–10; current11. Story open-ended; release12 not started.
  Pending: user confirmation for Crumb Bait/other recipes, actual client visuals,
  connected human co-op latency, prior raft teleports and large-raft performance.
- Retained: Raft Ocean 0.11.11; artifact build/libs/raft_ocean-0.11.11.jar.
  Targets: Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25,
  ModDevGradle 2.0.147, Gradle 9.2.1, JEI 31.9.0.58. Protocol raft-movement-10.5.
  Added empty rechargeable Tidal Battery (1000) and Deep-Cycle Battery (4000).
  Charge is a clamped native item CustomData component; nonstacking reusable cells,
  original 3D models, native inventory charge bars and bilingual charge tooltips.
  Appended BATTERY_CHARGER kind preserves existing piece ordinals and saves.
  Two sockets, biofuel input, per-socket UI percentages, pulse animation, pause/resume,
  crafting and recipe-book entries; existing Electrical research tags extended.
  Charger uses generator power first and local biofuel for remaining demand;
  fuel network feeds its fuel slot. Total charge rate configurable 1–200 (default20).
  Each biofuel yields configured generator energy (default2000), retaining remainder.
  Electric Purifier/Sprinkler now accept a cell in slot1 for local portable power;
  wired power preferred, shortfalls atomic, depleted cells never destroyed.
  Three new shapeless recipes appear in standard JEI crafting category.
  Final build/bootstrap: 425 collision, 729 carry, 820 construction, 48 crafting,
  119 stations, 188 research, 108 salvage, 661 survival, 130 navigation,
  593 creature/config assertions. Includes 25 battery conservation/persistence/network
  checks plus new machine-menu interaction coverage. Ocean pass741 in batteries-first.log.
  Native model parser:397 models,121 GUI silhouettes,4845 geometry/fill checks.
  Packaged audits passed:112 original 3D items,276 custom blocks,504 en/de keys.
  Guide docs/RAFT_BATTERIES.md; generator tools/generate_batteries.py;
  final log build/batteries-final.log. Prior archery/models retained.
  Finished milestones1–10; current11. Story open-ended; release12 not started.
  Limits: cells power their own purifier/sprinkler, do not export network energy;
  handheld receiver unchanged; mounted charger processes only loaded rafts.
  Converted charger fuel energy is lost when dismantling, stored batteries drop normally.
  Pending: actual client visuals, human connected co-op latency, prior intermittent
  raft teleports and large-raft performance.
- Retained: Raft Ocean 0.11.10; artifact build/libs/raft_ocean-0.11.10.jar.
  Targets: Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25,
  ModDevGradle 2.0.147, Gradle 9.2.1, JEI 31.9.0.58. Protocol raft-movement-10.4.
  Added three original bows, four damage-tier arrows, bowstring and arrow shaft.
  Sixteen data-driven crafting recipes: staged bow upgrades, renewable arrow batches,
  leaf/feather fletching, bamboo/string alternatives and vanilla arrows from shafts.
  Original colored 3D items, three draw stages per bow and bilingual speed/damage tooltips.
  Native aiming/ammo/enchantment/durability pipeline, bow repair materials, custom
  dispenser behavior, recoverable survival ammo and creative duplication restrictions.
  Existing Tools/Metal research tags extended without changing saved progress.
  All bows use native 20-tick draw; flying arrows retain native projectile appearance.
  Crafting upgrades consume the old bow without transferring enchantments/name/damage.
  Bootstrap passed: 425 collision, 729 carry, 810 construction, 48 crafting,
  119 stations, 188 research, 108 salvage, 619 survival (138 new archery),
  130 navigation and 593 creature/config assertions. Ocean passed 741 assertions.
  Native models/GUI fitting and all packaged audits passed: 109 3D items,
  9 extra bow draw models, 273 custom blocks, 492 English/German keys.
  Log build/archery-third.log; ocean pass build/archery-native.log.
  Guide docs/RAFT_ARCHERY.md; additive generator tools/generate_archery.py.
  Finished milestones 1–10; current 11. Story remains open-ended.
  Pending: actual client bow rendering, connected co-op latency, prior intermittent
  player teleports and large-raft performance. Milestone 12 not started.
- Retained: Raft Ocean 0.11.9; artifact build/libs/raft_ocean-0.11.9.jar.
  Targets: Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25,
  ModDevGradle 2.0.147, Gradle 9.2.1. Protocol raft-movement-10.4.
  Fixed dry purifier/sprinkler fuel demand and stale disabled activity status.
  Rotate eligible fuel/water/power and irrigation recipients to avoid starvation;
  cap each receiver to one automatic cup/canister per network update across sources.
  Sprinkler collider uses base and stem; decorative nozzle does not block walking.
  Every survival machine has shared Pause/Resume controls; automatic work/transfers
  stop without discarding resources. Manual cups/inventory retrieval still work.
  Paused flag and recipe duration now persist, including old-save defaults.
  Native server suites passed: 425 collision, 729 carry, 810 construction,
  48 crafting, 119 stations, 188 research, 108 salvage, 481 survival,
  130 navigation and 593 creature/config assertions. Ocean suite passed 733 checks.
  Model/parser/GUI-fit and packaged coverage/localization/story/audio audits passed:
  100 3D items, 273 custom blocks, 481 English/German keys. Prior models retained.
  Log build/automation-fixes-final2.log; earlier ocean pass in automation-fixes-native.log.
  Guide docs/AUTOMATION_BUG_FIXES.md.
  Finished milestones 1–10; current 11. Story remains open-ended.
  Pending: actual screen/model rendering, live connected co-op latency,
  prior intermittent player teleports and large-raft performance.
  Utility placement limits from 0.11.8 remain. Release milestone 12 not started.
- Retained: Raft Ocean 0.11.8; artifact build/libs/raft_ocean-0.11.8.jar.
  Targets: Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25,
  ModDevGradle 2.0.147, Gradle 9.2.1. Protocol raft-movement-10.3.
  Nine new buildables: water/fuel pipes, power cables, water/fuel tanks,
  biorefiner, biofuel generator, electric purifier and sprinkler; one biofuel item.
  Server-authoritative raft-local connected utility networks, conserved transfers,
  64-cup water tanks, biofuel engine fuel, demand-based generation and irrigation.
  Biorefiner data-pack recipes: organic + honeycomb -> 2 fuel / 600 ticks;
  organic + sugar -> 1 fuel / 900 ticks. Custom processing UI and co-op inventories.
  Seven synced automation configs; Metal/Electrical research tags retain old progress.
  Six JEI processing categories; dedicated server has no JEI dependency.
  100 custom 3D items, 273 registered custom blocks, matching placement previews,
  18 additional detailed station meshes, 477 English/German keys; previous assets retained.
  Native bootstrap passed: 415 survival, 130 navigation, 593 creature/config,
  417 collision, 729 carry, 810 construction, 48 crafting, 119 stations,
  188 research and 108 salvage assertions. Ocean-world suite passed 746 checks.
  Build/native model fitting and packaged assets/localization audits passed.
  Logs: build/automation-final.log, build/automation-package.log,
  build/automation-verified.log (ocean suite passed; obsolete recipe-count assertion fixed).
  Guide docs/RAFT_AUTOMATION.md; generator tools/generate_automation_models.py.
  Finished milestones 1–10; current 11. Story remains open-ended.
  Limits: one piece per cell, separate utility routes, static conduit ports,
  physical biofuel transfers, refinery output moved to tank manually,
  loaded rafts only; removed tanks/generators lose stored water/energy.
  Pending: actual client graphics and connected human co-op latency tests,
  previous movement/crash reports and performance. Release not started.
- Retained: Raft Ocean 0.11.7; artifact build/libs/raft_ocean-0.11.7.jar.
  Targets: Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25,
  ModDevGradle 2.0.147, Gradle 9.2.1. Protocol raft-movement-10.2 retained.
  Added Tidal Diving Hood (head slot, oxygen bonus +2), Reef Flippers
  (feet slot, water movement efficiency +0.65), each 192 durability, and
  Shoreleaf Salve (one completed use heals six health points, capped at max).
  Healthy salve use rejects without consumption. Native equipment attributes
  apply only in worn slots and survive save/load; no separate tick-based buff.
  Three original item models and two original wearable texture layers;
  equipped shape uses native head/footwear mesh, held models are custom.
  Shapeless recipes, existing Tools research tags for wearables and creative
  tab integration; original medical/diving designs inspired by Raft mechanics.
  434 English/German keys; 90 custom 3D item models.
  Native suites passed: 307 survival, 593 creature/config, 733 ocean-world,
  retained 417 collision/729 carry/720 construction assertions.
  Final build/native model fitting and packaged model/equipment/locale/audio
  audits passed. Logs build/diving-salve-build.log and build/diving-model-build.log.
  Guide docs/DIVING_AND_FIRST_AID.md; generator tools/generate_dive_gear.py.
  Finished milestones 1–10; current 11. Story remains open-ended.
  Pending: GPU wearable rendering, actual underwater feel, connected co-op
  latency and prior movement/crash reports/performance. Release not started.
- Retained: Raft Ocean 0.11.6; artifact build/libs/raft_ocean-0.11.6.jar.
  Targets: Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25,
  ModDevGradle 2.0.147, Gradle 9.2.1. Protocol raft-movement-10.2 retained.
  Existing cooked bowl meals gain configurable temporary level-one bonuses:
  Garden Broth speed, Mushroom Chowder water breathing, Sea Grain Bowl haste,
  Hearty Fish Pot absorption. Default 2400 ticks; repeat meals do not stack levels.
  Original recipes/models and JEI retained; bilingual bonus tooltips included.
  Crouch-empty-hand net interaction picks up fitting cargo, leaving excess in
  shared storage; full Creative inventory does not destroy loot.
  Cup transactions reject remote/wrong-world/spectator/stale/removed stations.
  Fresh-water hydration updates only on server. Direct theft enforces Peaceful.
  New settings survival.preparedMealBonuses and survival.mealBonusTicks;
  all presets/default examples updated. 430 bilingual keys.
  Build and both native server suites passed: 293 survival, 593 creature/config,
  741 ocean-world, retained 417 collision/729 carry/720 construction assertions.
  Packaged models/locales/sounds/presets passed. Log build/meal-net-fixes-build.log.
  Guide docs/MEALS_AND_NET_FIXES.md. Finished milestones 1–10; current 11.
  Story remains open-ended. Pending: live rendering, connected co-op inventory
  and latency checks, prior movement/crash reports/performance. Release not started.
- Retained: Raft Ocean 0.11.5; artifact build/libs/raft_ocean-0.11.5.jar.
  Targets: Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25,
  ModDevGradle 2.0.147, Gradle 9.2.1. Protocol raft-movement-10.2 retained.
  Added 17 synchronized settings: piece cap, cup hydration, sprint thirst,
  seven per-machine speed factors, machine/engine fuel efficiency, planter
  minimum light and raft sky-cover requirement, engine/sail/paddle strength.
  Defaults retain previous behavior. Lower caps preserve saved structures;
  new fuel efficiency preserves burning reserves. Zero engine thrust burns no fuel.
  Peaceful and co-op behavior retained. Added faster workshop preset.
  422 English/German keys; guide docs/EXPANDED_CONFIG.md.
  Build and both native server suites passed, including 593 creature/config
  and 733 ocean-world assertions, retained 417 collision/729 carry/720 building
  checks. Packaged model, audio, locale and config-preset audits passed.
  Log build/expanded-config-build.log. Finished milestones 1–10; current 11.
  Story remains open-ended. Pending: local visuals and connected multiplayer
  latency/performance checks, prior reported crashes; milestone 12 not started.
- Retained: Raft Ocean 0.11.4; artifact build/libs/raft_ocean-0.11.4.jar.
  Targets: Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25,
  ModDevGradle 2.0.147, Gradle 9.2.1. Protocol raft-movement-10.2 retained.
  Minecraft Peaceful takes precedence over mod difficulty overrides; both forms
  of Peaceful cancel foundation bursts, prevent swimmer attacks, crop theft and
  dehydration damage. Passive sharks persist; optional Peaceful thirst drain.
  Added 14 bilingual synchronized settings: windup, recovery pause, retreat,
  repel cooldown, per-mode shark counts and first-entry supply toggle/amounts.
  Co-op remains opt-in. Starter kit changes do not backfill existing players.
  Existing configs are preserved; a peaceful-coop preset is provided.
  405 bilingual keys; guide docs/PEACEFUL_AND_CONFIG.md.
  Build, packaged models/locales/audio/presets and both native server suites
  passed: 741 ocean-world and 565 creature/config assertions, with retained
  417 collision, 729 carry and 720 construction checks.
  Log build/peaceful-config-build.log. Finished milestones 1–10; current 11.
  Story remains open-ended. Pending: rendered timing and connected co-op latency
  testing, prior crash reports and live performance; milestone 12 not started.
- Retained: Raft Ocean 0.11.3; artifact build/libs/raft_ocean-0.11.3.jar.
  Targets: Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25,
  ModDevGradle 2.0.147, Gradle 9.2.1. Protocol raft-movement-10.2 retained.
  Foundation attack bursts default to 10 contact bites, 10 damage each in Normal,
  and 12 recovery ticks between windups. Existing configured damage is preserved.
  Three successful player hits across a crew repel an attacking shark; ignored
  damage-cooldown hits do not count. Burst/repel counts configurable.
  Opt-in multiplayer.sharedStarterRaft supplies common first entry in Ocean World;
  public /raftcrew join selects a nearby safe moving home; /raftcrew info counts
  saved crew. Shared home codec is backward-compatible; crew recovery updates all
  members together. Existing reconnect positions, homes and personal research kept.
  391 bilingual keys; original audio, journal textures and collision fixes retained.
  Guide docs/COOP_AND_SHARK_DEFENSE.md; coop config preset provided.
  Build, model, packaged locale/audio/preset checks and both native server suites
  passed, including 728 Ocean World and 562 creature/config assertions.
  Log build/coop-shark-burst-build.log. Finished milestones 1–10; current 11.
  Story remains open-ended. Pending: connected co-op movement/save/respawn and
  repeated-bite visuals under latency, prior crash reports and live performance.
- Retained: Raft Ocean 0.11.2; artifact build/libs/raft_ocean-0.11.2.jar.
  Targets: Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25,
  ModDevGradle 2.0.147, Gradle 9.2.1. Protocol raft-movement-10.2 retained.
  Shark foundation attacks rise from underwater staging to actual deck-side
  height; offshore body clearance and synchronized visual reach align the jaw.
  Shared FoundationBite geometry gates server damage on <=0.15-block jaw contact.
  Foundation pose suppresses bob/pitch/bank, retains jaw/tail motion and fades
  reach during recovery. Bait/swimmer/circling retain ordinary swim depth.
  Added native damage-transition jaw/body checks and 24 rotated-face geometry
  checks. Guide docs/FOUNDATION_BITE_CONTACT.md; log build/foundation-bite-build.log.
  Build/models/assets/localization audits and dedicated-server suites passed,
  including 562 creature/config assertions, retained collision/carry checks, and
  704 Ocean World assertions including actual jaw contact/body clearance.
  Additional log build/foundation-bite-ocean-check.log.
  Finished milestones 1–10; current 11; story remains open-ended.
  Pending: real anchored/drifting/turning shark visuals and multiplayer tests,
  previous movement/crash reports and live performance. Narrow gaps remain limited.
- Retained: Raft Ocean 0.11.1; artifact build/libs/raft_ocean-0.11.1.jar.
  Targets: Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25,
  ModDevGradle 2.0.147, Gradle 9.2.1. Protocol raft-movement-10.2 retained.
  Clearance considers supported low steps up to 0.6 blocks instead of larger
  lateral pushes. Continuous swept raft obstacle tests and native world/entity
  clipping protect escape paths; supported intermediate samples at <=0.05 blocks.
  Final headroom/loaded chunks/border/support rules and bounded recovery retained.
  Added rotated station/wall, no-repeat correction, ledge and thin-wall regressions.
  Build/model/packaged asset and locale/audio audits passed. Dedicated-server
  regressions passed: 720 construction checks, 417 collision and 729 carry checks
  plus crafting/stations/research/salvage/survival/navigation/creature suites.
  Guide docs/COLLISION_CLEARANCE_POLISH.md; log build/collision-clearance-build.log.
  Finished milestones 1–10; current 11 awaiting local tests; story open-ended.
  Pending: drifting/turning/upper-floor/connected multiplayer movement tests,
  prior crash reports and live performance. Conservative rotated strips retained.
- Retained: Raft Ocean 0.11.0; artifact build/libs/raft_ocean-0.11.0.jar.
  Targets: Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25,
  ModDevGradle 2.0.147, Gradle 9.2.1. Protocol raft-movement-10.2 retained.
  383 English/German keys and placeholders match; original cup-fill/drink and
  hook-cast mono Vorbis sounds have bilingual subtitles and server-only emission.
  Client machineEffectDistance 0–128 (default 32) limits cosmetic effects per
  transformed machine; water, crops, food and processing retained. Mesh lookup
  uses reload-prepared names, eliminating repeated resource path allocations.
  Calm, large-raft and low-effect complete config examples included.
  Build, native models/alignment, packaged assets and isolated dedicated-server
  regressions passed. Locale/audio/preset audit passed; 3 sounds decode with ffmpeg.
  Guide docs/MILESTONE_11.md; build/server log build/milestone-11-build.log.
  Finished milestones 1–10; current 11 awaiting local tests; release 12 unstarted.
  Story remains six destinations with open ending. Journal atlas fix retained.
  Pending: live audio/visual/performance and connected multiplayer tests,
  earlier movement/crash reports. No measured FPS improvement claimed.

- Retained: Raft Ocean 0.10.10; artifact build/libs/raft_ocean-0.10.10.jar.

  Targets: Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25,

  ModDevGradle 2.0.147, Gradle 9.2.1. Protocol raft-movement-10.2 retained.

  Deterministic coastal material kit refines 16 shared wood/metal/fabric/paint/

  masonry/foliage surfaces, 10 deck/scenery/salvage variants, 7 item/effect

  materials and riveted barrel surface. Model generator reapplies kit last.

  Foundations gain top seams/fasteners; freestanding building/storage/planter

  families gain rear joinery. All 87 item and 244 block model coverage retained.

  Six journal covers retain the single-atlas fix. Tree/mesh/animation layouts,

  gameplay, colliders, recipes and save/network formats unchanged.

  Guide docs/COASTAL_MATERIAL_POLISH.md; previews build/coastal-materials-preview.png

  and build/handcrafted-models-preview.png. No in-game visual proof claimed.

  Build passed: 331 native models/19388 elements, 87 GUI fits, 4461 machine

  alignment checks, contact/rotation regressions and both packaged audits.

  Log build/coastal-material-polish-build.log.

  Finished milestones 1–9; current 10; six story destinations remain open-ended.

  Pending: in-game appearance/fills/animations, earlier movement/crash reports

  and multiplayer performance.

- Retained: Raft Ocean 0.10.9; artifact build/libs/raft_ocean-0.10.9.jar.

  Targets: Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25,

  ModDevGradle 2.0.147, Gradle 9.2.1. Protocol raft-movement-10.2 retained.

  Client log confirmed all six journal models failed baking because item-atlas

  covers were combined with block-atlas materials. Original covers now copied

  to block/journal_<id> by the generator; journal geometry/materials retained.

  Packaged model audit rejects mixed referenced item/block atlases; confirmed

  it detects the defective 0.10.8 JAR. Guide docs/JOURNAL_TEXTURE_FIX.md.

  Build/native model and alignment checks plus both packaged audits passed;

  log build/journal-atlas-build.log.

  Finished milestones 1–9; current 10; story remains open-ended.

  Pending: in-game journal appearance; previous movement/crash reports,

  visual tests and multiplayer performance. Gameplay/collision shapes unchanged.

- Retained: Raft Ocean 0.10.8; artifact build/libs/raft_ocean-0.10.8.jar.

  Targets: Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25,

  ModDevGradle 2.0.147, Gradle 9.2.1. Protocol raft-movement-10.2 retained.

  Original tapered branched coastal tree with buttress roots, pointed fronds,

  bark/leaf materials, growth and wind sway replaces stacked round crowns.

  Rectangular framed purifier, octagonal masonry smelter with square chimney,

  collector supports, pot handles, engine fittings, plank joints, brass pegs,

  cloth stitching, wrapped tool grips and colored controls enrich model families.

  Existing machine fill and articulated part alignment retained; 13 station meshes.

  87 items, 244 blocks, 7 entity renderers and 27 creature/salvage meshes retained.

  Guide docs/HANDCRAFTED_MODELS.md; software preview build/handcrafted-models-preview.png.

  Build passed: 331 native models/18931 elements, 87 GUI fits, 4461 machine

  alignment checks, contact/rotation regressions and both packaged asset audits.

  Log build/handcrafted-models-isolated-build.log.

  Isolated output build uses tools/isolated-visual-build.gradle while development

  Minecraft locks normal outputs. No running client was stopped.

  Finished milestones 1–9; current 10; story six destinations remains open-ended.

  Pending: local appearance, tree growth/wind, rotated fills/animations, multiplayer

  performance and previous movement/crash reports. Colliders/gameplay unchanged.

- Retained: Raft Ocean 0.10.7; artifact build/libs/raft_ocean-0.10.7.jar.

  Targets: Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25,

  ModDevGradle 2.0.147, Gradle 9.2.1. Protocol raft-movement-10.2 retained.

  MachineVisualLayout derives bases/fills from full envelopes; grill effects no

  longer treat the top plate as the base. Fill heights/radii stay inside vessel

  floors/rims; water uses a smooth disk, bubbles and stirrers track its level.

  Rain endpoints, food, smoke and smelter heat align to actual model anchors.

  Planter soil/roots/aquatic water and tree trunk base align at the soil surface.

  Canonical mesh axes restored before mounting rotation; chest latch moves with

  lid. Twelve station/effect meshes, 27 creature/salvage meshes retained.

  Native quarter-turn/off-center-pivot and empty/full/wave fill alignment checks

  run in modelAssetCheck; logs build/machine-alignment-final-build.log.

  Guide docs/MACHINE_ALIGNMENT.md. 87 item GUI fits and 244 block coverage kept.

  Finished milestones 1â€“9; current 10; story open-ended. Gameplay/colliders unchanged.

  Pending: live fill/animation/upper-floor tests, multiplayer performance and

  previous movement/crash reports.

- Retained: Raft Ocean 0.10.6; artifact build/libs/raft_ocean-0.10.6.jar.

  Targets: Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25,

  ModDevGradle 2.0.147, Gradle 9.2.1. Protocol raft-movement-10.2 retained.

  Fixed GUI-fit loop variable mutation; actual native ItemTransform matrices

  verify all 87 inventory silhouettes. Diagonal tool views, broad material views,

  corrected spear blade orientation and open curved handheld hook head.

  Eleven true polygon station meshes replace striped cuboid-slice walls/rims;

  multi-material client resource meshes support reload and existing animations.

  Continuous tapered shark body, original blue/pale skin, dorsal/pectoral/tail

  meshes and aligned jaw/teeth. 27 creature/salvage meshes; 11 station meshes.

  244 registered block and 87 item models retained. Physics/colliders unchanged.

  Build/native GUI/model and packaged audits: build/visual-mesh-final-build.log.

  Guide docs/VISUAL_MESH_FIX.md; software GUI preview build/all-items-preview.png.

  Finished milestones 1â€“9; current 10; six story destinations remain open-ended.

  Pending: live GUI/station/shark/reload/animation tests, multiplayer performance,

  and earlier movement/crash reports. Native shape validity is not visual proof.

- Retained: Raft Ocean 0.10.5; artifact build/libs/raft_ocean-0.10.5.jar.

  Targets: Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25,

  ModDevGradle 2.0.147, Gradle 9.2.1. Protocol raft-movement-10.2 retained.

  All 87 registered item models have individually fitted/centered GUI transforms;

  silhouette fills a 15-pixel slot area, with a maximum zoom of 1.6.

  Held, dropped, placed appearances, animations and physics retained.

  Build/parser and packaged coverage checks: build/inventory-model-build.log.

  Finished milestones 1â€“9; current 10; story open-ended.

  Pending: local GUI appearance and previous graphics/multiplayer/movement retests.

- Retained: Raft Ocean 0.10.4; artifact build/libs/raft_ocean-0.10.4.jar.

  Targets: Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25,

  ModDevGradle 2.0.147, Gradle 9.2.1. Protocol remains raft-movement-10.2.

  Screenshots exposed distorted rotated planar faces under nonuniform scaling.

  Generated panels, cylinders, open rims and curves now use closed sections;

  positive volume and absence of local rotations audited for all fine models.

  Fixed cylindrical part face permutation; preserved whole-part animation pivots.

  Brighter original grain, brushed metal, brass, ceramic, woven cloth, teal/navy/

  coral paint, soil/stone/rubber; distinct station parts, bands, vents and rims.

  Receiver/meal colors and full journal cover UVs updated. All 87 items,

  244 blocks, seven renderers and 22 polygon meshes retain custom coverage.

  Native parser/build and packaged model audits logged in

  build/model-repair-build.log; software textured previews refreshed.

  Guide: docs/MODEL_REPAIR.md. Physics/collisions, save/network formats,

  20 research samples/24 projects and six open-ended story destinations retained.

  Finished milestones 1â€“9; current 10; 11â€“12 unstarted.

  Pending: client appearance/reload/animation and multiplayer performance retest;

  previous movement/crash reports and conservative collider limitations retained.

- Retained: Raft Ocean 0.10.3; artifact build/libs/raft_ocean-0.10.3.jar.

  Targets: Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25,

  ModDevGradle 2.0.147, Gradle 9.2.1. Protocol remains raft-movement-10.2;

  matching installations recommended; no new registry/network/save formats.

  Native registry export verifies 87 mod items, 244 mod blocks (including internal

  visual/part blocks) and 7 entity types. All items now resolve to original 3D

  models, all blocks to explicit geometry, all entities to custom renderers.

  Cups/handles/water, tools/hooks, materials/coils/mesh/plates, meals/skewers,

  receivers/antennas/keys/journals get shaped inventory/held/dropped models.

  Creature and salvage renderers now submit resource-driven textured polygons:

  rounded bodies, fins, tapered mirrored wings, beaks/teeth, logs/barrels/bottles,

  beveled salvage, curved tubular hook and oriented continuous rope segments.

  Client-only mesh reload listener supports F3+T; entity lighting/outlines and

  existing animation poses retained. Collision/AI/physics/interactions unchanged.

  All-model generator and exact packaged registry/model/mesh audit added.

  Native model parser now checks item geometry as well as block geometry.

  Build and both isolated native server checks pass: 188 research, 417 collision,

  729 carry, 619 construction, 48 crafting, 119 stations, 108 salvage,

  274 survival, 130 navigation, 562 creature/config, 672 Ocean World assertions.

  Final parser/coverage totals recorded in build/all-models-final-build.log.

  Guides docs/ALL_MODELS.md and docs/RESEARCH_AND_FINE_MODELS.md. Research remains

  20 samples/24 projects; story remains six destinations and open-ended.

  Finished milestones 1â€“9; current 10; 11â€“12 unstarted. Client shader appearance,

  first/third-person alignment, reloads, large-world graphics performance,

  connected multiplayer and earlier movement/crash reports await user retest.

  Conservative colliders/placement ghosts remain; automated Crafters and vanilla

  crafting retain their existing research bypass behavior.

- Retained: Raft Ocean 0.10.2; artifact build/libs/raft_ocean-0.10.2.jar.

  Targets: Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25,

  ModDevGradle 2.0.147, Gradle 9.2.1. Protocol raft-movement-10.2;

  matching client/server required; existing saves retained.

  All 39 pieces and 138 articulated parts use original fine model resources:

  round 24-sided vessels/barrels/shafts/tool wheels, beveled panels/foundations,

  smooth curved walls, open bookshelves, bench papers/tools and circular helm rim.

  Eight original material/book textures. Machine activity, storage lids, sail/wind,

  engine/wheel animation frames retained. Conservative original colliders and

  placement bounds retained. Native client model parser runs as a build check;

  no display or user world is started for this check. Geometry preview is a

  software inspection artifact, not a Minecraft screenshot.

  Research now has 20 material samples and 24 recipe projects. Protected temporary

  sample slot: consume one once, reuse knowledge across projects, explicitly Learn

  ready projects for free. Paged Material/Recipe screen; synced small integer

  statuses avoid large-mask menu truncation. Knowledge/materials saved and copied

  on death; old six-topic knowledge migrated once to matching project unlocks.

  Sample returns/drop through native close/removal/death handling. Native manual

  crafting enforces tagged mod output unlocks; vanilla outputs retain native

  behavior and automated Crafters bypass per-player gates. Data-driven material

  tags, output tags, advancement and recipe-book rewards, full English/German.

  Build/native checks pass: 188 research, 417 collision, 729 carry, 619 construction,

  48 crafting, 119 stations, 108 salvage, 274 survival, 130 navigation,

  562 creature/config and 657 Ocean World checks in the saved isolated fixture.

  Exact final model parser/asset totals are in build/fine-models-final-build.log;

  packaged classes/model/texture references and both open endings verified.

  Guide docs/RESEARCH_AND_FINE_MODELS.md. Story remains six destinations/open-ended.

  Finished milestones 1â€“9; current 10; 11â€“12 unstarted. Live client appearance,

  moving-part alignment, large-raft rendering performance, connected multiplayer,

  migration/death-copy UI, previous movement reports and crash recurrence await

  user testing. Development rebuild safeguard from 0.10.1 retained.

- Retained: Raft Ocean 0.10.1; artifact build/libs/raft_ocean-0.10.1.jar.

  Targets: Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25,

  ModDevGradle 2.0.147, Gradle 9.2.1. Protocol raft-movement-10.1;

  update client and server together; existing saves retained.

  All 39 building/station inventory models plus foundation now use original

  multipart 3D geometry. Static raft pieces use native custom block model

  resources; benches have inlaid papers/tools and bookshelves have individual

  shelves/books. Animated machines/navigation/storage/doors retain articulated

  rendering. Geometry export and deterministic asset/preview generators added.

  Shared coastal landscaping adds grounded rocks, flowers, moss shores and

  layered groves to resource/story islands, protecting central approaches/loot.

  Scenery appears in new chunks only. Six destinations and open ending retained.

  Attached crash: NoClassDefFoundError RaftEntity$Placement from live development

  output. Concurrent rebuild/clean suspected, not proven by the report alone.

  Gradle now guards output writers/clean against this project's live client JVM;

  Windows uses native process-table command lines. External IDE compilation is

  outside this guard. Close, rebuild and relaunch before retesting.

  Build and both isolated native server runs pass: 417 collision, 729 carry,

  619 construction, 48 crafting, 119 stations, 35 research, 108 salvage,

  274 survival, 130 navigation, 562 creature/config and 672 Ocean World checks.

  Standalone geometry/codec checks pass. Packaged 78 world/inventory station

  models, 39 item/blockstate references, original texture references, required

  nested runtime classes and exact English/German open-ending chapters verified.

  Live matching-JVM probe confirms the rebuild guard rejects before output writes;

  rebuilding after that probe exits succeeds.

  Guide docs/CUSTOM_MODELS.md. Finished milestones 1â€“9; current 10;

  11â€“12 unstarted. Live client model rendering/resource reloads, connected

  multiplayer, previous movement reports and crash recurrence await user tests.

  Native automated Crafters still bypass per-player research gates.

- Retained: Raft Ocean 0.10.0; artifact build/libs/raft_ocean-0.10.0.jar.

  Protocol raft-movement-10; matching client/server required; prior saves retained.

  User authorized milestone 10; milestone 9 implementation accepted for progression.

  New RESEARCH_TABLE station/preview/geometry and server-authoritative screen.

  Six topics: Workshop, Metal, Cultivation, Textiles, Navigation, Radio. One tagged

  sample each; prerequisites, per-player saved knowledge, copy-on-death, advancement

  and recipe-book rewards. Output tags gate manual native 2x2/3x3/raft crafting

  before result synchronization. Creative bypass; synced progression.researchRequired

  config default true. Existing equipment usable; basic survival/wood recipes free.

  Twelve native item tags and six advancement JSONs; original bench texture, English

  and German labels/tooltips. Dedicated native research transaction checks added.

  Story remains six chapters with unresolved eastern transmission; no finale or

  new story location added. Guide docs/MILESTONE_10.md. Build and both isolated

  server runs pass: 35 research (native 2x2/raft crafting gates, real advancement

  tracker, one-sample costs, ownership, save/reload, drift/reach/removal/config),

  417 collision, 729 carry, 619 construction, 48 crafting, 119 stations, 108 salvage,

  274 survival, 130 navigation, 562 creature/config, 661 Ocean World assertions.

  All standalone checks pass. Packaged table models/textures, tag sets, advancement

  recipe rewards and both language/open-ending resources verified.

  Finished milestones 1â€“9; current 10; 11â€“12 unstarted. Live screen/JEI browsing,

  multiplayer/death-copy and previous movement reports await user retest. Native

  automated Crafters lack player identity and bypass per-player crafting gates.

- Retained 0.9.16 open ending; artifact build/libs/raft_ocean-0.9.16.jar.

  Text-only open-ending update; protocol remains raft-movement-9.15. Harbor points

  to the latest copied entry instead of a finale. Starfall introduces an unfamiliar

  eastern voice and a missing ledger; crew fate/destination remain unresolved.

  Receiver says no further coordinates can be located yet. Full English/German

  updates apply to existing journal items. Six current destinations/stages retained;

  no future islands added. Build and existing standalone checks pass; packaged

  English/German chapters and receiver endpoint message verified. Native generation

  remains as verified in 0.9.15; no Java or world-generation changes in this update.

  Milestones 1â€“8 finished; 9 current; 10â€“12 unstarted. Live story reading, island

  visuals, multiplayer and prior movement/clearance reports remain user tests.

- Retained 0.9.15 polish; artifact build/libs/raft_ocean-0.9.15.jar.

  Protocol raft-movement-9.15; update both sides; saves compatible. All six

  journals rewritten as one original voyage through Mara/Sela/Ivo, with conflict,

  a mistaken route, rescue and departure. Full English/German chapters with

  paragraph breaks, explicit next destinations and both exact seal clues.

  Original journal IDs/stages/loot positions retained. All six story templates

  gain authored lighting, paths and architecture: lookout balcony/eaves, framed

  greenhouse/potting bench, weathered vault/shelves, beacon cottage/copper trim,

  harbor gable/pier rails/workstations, archive trails/constellation/memorial garden.

  Shores gain groundcover, flowers and weathered rock. Native geometry/loot and

  existing underwater resources retained. New geometry affects unexplored chunks;

  new lore applies to existing journals. Guide docs/STORY_POLISH.md.

  Final build passes; native Ocean World: 672 checks (40 new landmark/lighting

  assertions) in isolated ocean_world0915b. Bootstrap checks pass: 417 collision,

  729 carry, 610 construction, 48 crafting, 119 stations, 108 salvage, 274 survival,

  130 navigation, 562 creature/config. All standalone checks pass. Packaged six

  revised templates and both full chapter translations verified. Unsupported

  harbor lantern caught by native checks and corrected with a workbench below.

  Milestones 1â€“8 finished, 9 current,

  10â€“12 unstarted. Visual exploration, connected multiplayer and previous movement

  reports remain live user tests.

- Retained 0.9.14 expansion; artifact build/libs/raft_ocean-0.9.14.jar.

  Protocol raft-movement-9.14; matching client/server required; saves compatible.

  Three original story islands: Ember Beacon (49), Tideworks Haven (65), Starfall

  Archive (49). Native templates include lighthouse/ladder, harbor workshop/pier,

  observatory/bunker, supplies, underwater gathering and original journal art.

  Receiver route extends from three to six destinations. Journey codec accepts

  stages 0â€“6; existing stage-3 completion resumes at Ember Beacon. Earlier journal

  IDs retained; progress cannot regress. Harbor supplies a relay key for Starfall's

  ON/OFF/ON seal; existing Echo Vault behavior retained. English/German new lore,

  names and signals; native loot tables and structure tags. Original story-set

  spacing/salt retained with three new weight-one entries; ordinary set unchanged.

  Guide docs/STORY_EXPANSION.md. Build and both isolated server runs pass.

  Native: 632 Ocean World (61 additional story/template/progress assertions),

  417 collision, 729 carry, 610 construction, 48 crafting, 119 stations, 108

  salvage, 274 survival, 130 navigation, 562 creature/config assertions. All

  standalone checks pass. Ocean generation verified in isolated ocean_world0914.

  Current milestone: 9; finished: 1â€“8; milestones 10â€“12 unstarted. Live visuals,

  connected multiplayer and prior movement/clearance reports remain user tests.

- Retained 0.9.13 collision fix; artifact build/libs/raft_ocean-0.9.13.jar.

  Protocol raft-movement-9.13; update client and server together; saves compatible.

  A large raft could be absent from entity queries at a distant deck cell because

  Minecraft searches only nearby origin sections. Native baseline reproduced a

  fall through an existing foundation at local X=16, yaw=0.

  Weak per-world raft index supplements typed/untyped/existence queries and keeps

  collision, contact and picking consistent across section boundaries. Only live

  tracked entities participate; type/predicate/exclusion/limits and duplicates

  are respected. Actual geometry preserves holes and open water. World unload

  clears the index. Native checks cover eight headings, distant nets/foundations,

  vertical-section upper floors, carry, removal and unsupported deck holes.

  Guide docs/DECK_COLLISION_FIX.md. Build and both isolated native runs pass.

  Native: 417 collision, 729 carry, 610 construction, 48 crafting, 119 stations,

  108 salvage, 274 survival, 130 navigation, 562 creature/config, 571 Ocean World

  assertions. All standalone geometry/coordinate checks also pass. Packaged JAR

  verified with the common spatial index and query mixin included.

  Open reports: intermittent falling/snap-back and station clearance await live

  retest, including connected multiplayer. Milestone 9 remains current.

- Retained 0.9.12 stations and geometry; artifact build/libs/raft_ocean-0.9.12.jar.

  Protocol raft-movement-9.12; update both sides; saves compatible.

  New native raft stations: Anvil, Enchanting Table, Ender Chest; Bookshelf boosts

  enchanting from local hull coordinates (fifteen clear shelves enable level30).

  Native screens, XP/lapis/repair transactions and hooks retained; menus follow

  hull/removal/reach. Ender contents remain per-player and never become raft cargo.

  Anvil output suppresses terrain damage callback; raft anvils do not wear out.

  Four original asset sets, vanilla direct placement/conversions and recipe unlocks.

  JEI native anvil catalyst. Thirty-eight buildables have shared preview planning.

  Sail/wheel/engine/anchor/smelter/anvil colliders retain separate solid parts,

  removing invisible upper volume around masts, wheels and chimneys.

  Guide docs/VANILLA_STATIONS.md. Build and both isolated native runs pass.

  Native: 729 carry, 610 construction, 48 crafting, 119 stations, 108 salvage,

  274 survival, 130 navigation, 562 creature/config, 571 Ocean World assertions.

  Standalone: 16 contact,19 foundation,92 structure,16 codec,11811 rotation checks.

  Live screens, animation and remaining collision/snap-back reports await retests.

  Mixin power injection is verified against target bytecode and native level30

  offers, blocked gaps and rotating/moving menu assertions. Existing worlds retained.

- Retained 0.9.11 fixes and earlier features:

  Tide Grazer uses Animal.createAnimalAttributes instead of Mob attributes,

  registering Minecraft26.3 TEMPT_RANGE=10 required by native wheat temptation.

  Existing animals acquire the fixed supplier when loading; no migration/delete

  needed. Guide docs/GRAZER_CRASH_FIX.md. Native regression invokes TemptGoal,

  ticks full grazer AI, reloads/ticks a saved grazer, and ticks bred offspring.

  Perpendicular full walls can now join in the same supported cell, consuming

  one additional wall and converting the original into a saved CORNER_WALL.

  Preview and server use the same join; support/capacity/obstruction checks still

  apply. Removal returns a reusable Corner Wall item. Opposite faces, half walls

  and railings do not automatically merge. Screenshot's raw ghost hint key is

  included in English/German resources; restart client to load updated resources.

  Shared placement planner drives server interaction and green/red client ghosts

  for every building item. Walls snap to clicked deck edges, continue along an

  existing wall and stack on top; sneaking overrides wall alignment with facing.

  Rotated placement obstruction checks use narrow hull collision strips.

  Movement frames are sent immediately before matching position packets, queued

  on the network thread and consumed by matching coordinates on the server.

  Full rebased movement paths are clipped against solids, including the selected

  raft explicitly. Bounds, expiry and vanilla movement checks remain active.

  Machine progress meters link to JEI categories. Recipe panels gain contrast and

  wrapped conditions; machine labels are bounded/staggered; German machine,

  navigation, ghost and JEI text added. Remaining German lore uses English fallback.

  Guide docs/PLACEMENT_JEI_FIXES.md. Build and standalone checks pass; isolated

  native checks: 729 carry, 574 construction, 48 crafting, 85 stations, 108 salvage,

  274 survival, 130 navigation, 562 creature/config, 571 Ocean World assertions.

  The exact client movement injection target is verified against MC26.3 bytecode.

  Live ghost rendering, JEI screens/reload, and multiplayer snap-back remain user

  tests. One structural piece per occupied cell remains the construction limit;

  use corner walls for joined faces. JEI browsing has no automatic transfer button.

- Retained 0.9.8 features and prior verification:

  Nets select whole meshes, snap flush even while crouching, and bridge foundations

  during placement/reload/removal/damage. Foundation buoyancy 40 increases spare

  equipment capacity fourfold (216 on starter). Engines follow mounted direction,

  have reverse/actual-bearing UI, arrows and reversible animated paddle wheels;

  navigation.engineFoundationCapacity defaults to 100 (range 1â€“1024).

  Optional JEI plugin has five custom processing categories and native station

  links, with NeoForge server recipe synchronization and reload refresh.

  Cane/kelp planter recipes return harvest/cutting/cup and synchronize visuals.

  New ordinary/story coast templates include native underwater kelp/resources;

  ordinary beaches also grow cane, and crates supply cane/kelp for older worlds.

  Sharks stage distant approaches, track hull edges, telegraph swimmer bites,

  retreat after attacks and time out failed approaches. User clarified movement

  problem as wrong raft direction; engine mounting now determines thrust.

  Guide docs/RAFT_REFINEMENTS.md. Build and standalone checks pass. Native: 725

  carry, 362 construction, 48 crafting, 85 stations, 108 salvage, 274 survival,

  130 navigation, 558 creature/config, 552 fresh Ocean World assertions.

  Live JEI startup/screens/reload, visual animations, propulsion and connected

  multiplayer await user tests. Earlier trapping/snap-back/inventory-loss reports

  await live retest. Current milestone 9; 1â€“8 finished; 10â€“12 unstarted.

- Target: Minecraft Java 26.3; NeoForge 26.3.0.48-beta.

- Build tools: ModDevGradle 2.0.147; Gradle 9.2.1; Java 25.

- Installed JDK: Zulu 25.0.4.1 at C:\Program Files\Zulu\zulu-25.

- IDE: another IDE with Java 25 confirmed by user; VS Code files present.

- Mod: Raft Ocean (raft_ocean) 0.9.12; package dev.raftocean.

- Version 0.9.7 continues milestone 9 with Â±1/5/15/45-degree sail/wheel controls

  and reset buttons; 15 original workshop/food items and 29 data-driven recipes,

  including four new pot meals and 14 alternative equipment patterns. Crates

  supply kelp/mushrooms; all chains have available ocean-survival ingredients.

  Bounded server animation snapshots drive water/heat/smoke/stirring, crops,

  smooth tree growth, opening chest/barrel lids and tool-station use animations.

  Cosmetic geometry leaves colliders and inventory transactions unchanged.

  Protocol raft-movement-9.7 requires updated client and server; saves compatible.

  Native validation: 263 survival (73 new workshop/activity checks), 63 navigation

  (11 new fine-control checks), 429 Ocean World, and all earlier native/standalone

  checks pass. Artifact: build/libs/raft_ocean-0.9.7.jar. Guide: docs/WORKSHOP_UPGRADE.md.

  Live animation appearance, multiplayer and past trapping/snap-back/inventory

  reports remain pending user retesting. Milestones 10â€“12 remain unstarted.

- Current work: milestone 9 coastal/building upgrade; milestones 10â€“12 remain unstarted.

- Version 0.9.6 adds a persisted, shared Tree Planter with nine species recipes,

  staged synchronized tree visuals, fresh-water/cup handling and timber/sapling outputs.

  Collection nets extend at foundation height with walkable colliders; straight

  walls snap to clicked edges, new corner/curved wall pieces have original geometry

  and recipes, and walls/doors mount flush to thin upper floors. Anchors pause their

  debris stream and new spawning. Sail close/broad reaches allow counter-steering.

  Ten ordinary island variants have bays, crescents, linked groves, sandbars and

  ridges; story coasts become irregular while buildings remain fixed. All 34

  building/station materials plus salvage/tree/seal surfaces and 29 original item

  icons have detailed pixel textures. See docs/COASTAL_UPGRADE.md for usage/tests.

- Current limits: islands change only in unexplored chunks; authored templates

  repeat. Tree outputs are harvested through their menu, with a shared stylized

  tree model. Nets supply no extra buoyancy. One structural piece per grid cell;

  use corner pieces for two wall edges. German localization of the whole mod

  remains milestone 11; this update includes German names for its new pieces.

- Open user-validation reports: intermittent trapping/snap-back and past inventory

  loss need live retesting; connected two-client movement and visual appearance

  are not independently verified. Current code remains in milestone 9 pending tests.

- Validation for 0.9.6: build passes; standalone checks pass (16 contact, 19

  foundation/carry, 68 structure/geometry, 16 movement codec, 11,811 rotation).

  Native dedicated-server checks pass: 725 carry, 359 construction, 48 crafting,

  85 stations, 108 salvage, 190 survival, 52 navigation, 558 creature/config.

  Fresh Ocean World checks pass with 430 assertions. All 117 mod PNG surfaces

  are 64x64 and model texture references resolve. Protocol raft-movement-9.6;

  update both client and server. Artifact: build/libs/raft_ocean-0.9.6.jar.

- Milestone 1: user confirmed client/integrated-server setup. Assistant isolated

  dedicated-server development bootstrap reaches Done; no connected client test.

- Milestone 2: user now confirms all tested prototype functions work in 0.3.1,

  including carry and foundation contact. This resolves the reported missing

  carry bug. Dedicated-server/two-client behavior is not independently verified.

- Milestone 3: implementation finished; user explicitly requested milestone 4.

  Final connected-client gameplay evidence remains unavailable. Version 0.3.2

  adds floors, walls, two-cell opening doors, stairs, moving ladders, and roofs.

  Structures, facing, and door state synchronize atomically with foundations and

  persist. Collision/render/picking use original per-piece geometry. Upper floors

  carry players at their own height; ladder carry preserves climbing velocity.

- Version 0.3.4 adds original tall (three-cell) pillars, short pillars, railings,

  and half walls. Pillar top placement snaps above all reserved cells; side

  placement uses the clicked level. Railings and half walls mount flush on thin

  floors. Tall piece overlap/save/height validation generalizes door reservations.

  Upper floors reuse the floor item. Removing the last deck support is refused;

  mounted railings/half walls must be removed before their floor.

- Milestone 3 implementation finished in 0.3.5: a registered Raft Construction

  Creative tab exposes all 12 building items and Creative search entries. Twelve

  generated advancement JSON files unlock the basic building recipes on acquiring

  planks. Research-gated progression remains milestone 10. Final gameplay acceptance

  awaits user results; the user authorized starting milestone 4.

- Milestone 4 implementation finished; user explicitly authorized milestone 5. Ocean World (raft_ocean:ocean) is a selectable world preset

  added to the normal world-preset tag without replacing vanilla entries. Its

  registered OceanChunkGenerator extends the verified 26.3 layer generator with

  corrected minY (-64) and sea-level (63) metadata. Data-driven layers: bedrock

  -64, stone -63..45, sand 46..48, water 49..62; ocean biome, no terrain features

  or vanilla structures. Nether and End remain vanilla. Flat seabed is deliberate;

  rare/story island structures belong to milestone 9.

- Construction gap fixed in 0.4.1: Raft Crafting Table is an original 6-mass owned

  piece, flush-mounted on upper floors. Both recipes work in the player 2x2 grid:

  three planks + stick (PP/PS), or convert a vanilla crafting table alone.

  RaftCraftingMenu extends the verified local 26.3 vanilla CraftingMenu with

  moving level access and server-side table-existence/dimension/eight-block reach

  validation. Native CRAFTING menu type uses the vanilla client screen. Normal

  close/removal returns ingredients; each player has an independent temporary grid.

  Table saves/syncs with existing structural data; existing worlds remain compatible.

  Twenty-eight items are available in the Construction Creative tab; the separate

  Ocean Salvage tab exposes hook tiers, salvage materials, notes, and nets.

- Station gap fix in 0.4.2, authorized by user: raft chests, barrels, stonecutters,

  smithing tables, grindstones, looms, and cartography tables. Native vanilla items

  for these and crafting tables place directly; optional one-item conversion

  recipes also exist. Original box models mount flush on upper floors. Native

  menus use moving reach and server validation, with no client-only common code.

  Chest/barrel pieces own shared 27-slot inventories saved with exact item data.

  Removal drops contents and invalidates open menus; destruction drops cargo,

  while chunk unload preserves it. Tools use temporary per-player inputs returned

  by native close handling. Required network protocol raft-movement-4 requires

  This version introduced protocol raft-movement-4. The current 0.5.0 protocol

  supersedes it; earlier worlds remain load-compatible.

  Version 0.6.0 adds original smelter and rest-bed pieces; arbitrary vanilla furnace/bed mounting is still pending. Static storage models

  have no lid animation, adjacent chests do not merge, no hoppers/redstone, and

  cargo mass is not counted yet. Connected-client station acceptance is pending.

- Station validation: 85 dedicated-server runtime assertions cover direct vanilla

  item mapping, loaded conversion recipes, native menus across displacement and

  range/removal, shared storage/shift transfers, save/load names/damage/slot counts,

  exactly-once removal/destruction drops, unload preservation, actual stonecutting,

  netherite smithing, grinding, loom patterns and map copying, ingredient/output

  transfers and repeated closing. These checks use fake players, not live clients.

- Milestone 5: implemented; user explicitly authorized milestone 6. Live acceptance remains incomplete. Seven

  server-owned floating debris kinds: plank/log/barrel/crate/scrap/leaves/bottle.

  They now keep a fixed drift, spawning ahead and passing behind without homing. Seven 26.3 loot

  tables use the verified modifier/type fields, including original named lore

  notes. Loot rolls once, saves with debris, and preserves exact components/counts.

  Natural spawning runs every 100 ticks, four tries per nearby occupied raft,

  exposed entity-ticking water only, forward band 18..30. Caps: 24 incoming, 128

  loaded per dimension; no forced chunks. Five-minute game-clock expiry includes

  elapsed time unloaded; dry/orphan debris cleans up. Native operator summons can

  bypass spawn caps for tests. Rates/config are deferred to milestone 11.

- Stream correction in 0.5.1: removed center attraction. Travel-relative forward

  band, independent fixed .018-block/tick opposite-heading drift per entity, saved

  drift/source UUID, ten-second settling after activation/25-degree cumulative

  turns/large position jumps. Ordinary travel keeps spawning. Passed/old-flow

  pieces do not count toward the incoming cap; all loaded pieces still respect

  the global cap. Source-owned debris cleans up 24 blocks behind its original

  corridor. Existing debris keeps its course when the raft turns. Actual ocean

  tick tests and cardinal/diagonal passage/turn/delay/persistence checks pass.

- Hooks: original items, one active cast per player, main/off-hand supported,

  exclusive live debris claims, manual reel follows moving player, obstacles and

  invalid owners cancel/release. Wooden: 24-block tether, .32 reel speed, 128

  durability. Reinforced: 40/.5/384. Damage per cast; basic 2x2 plank/stick recipe,

  upgrade basic + metal hook head + cordage/string binding. Casting entities do not save; switching

  away from hook, logout/death/dimension changes, or 600 ticks cancels. Full player

  inventories drop overflow via native server-only drop handling.

- Nets: new COLLECTION_NET(4 mass), original thin frame/mesh, shared 27-slot

  storage and native chest screen, same support/floor mounting/removal persistence.

  Half-second local capture checks; complete simulated deposit before commit so

  full nets neither partially collect nor reroll loot. Reach follows raft, hooks

  have exclusive claims, cargo drops on removal/destruction and survives unload.

  Craft with four timber braces, four bindings, and woven mesh. No automatic chest routing/cargo mass yet.

  Client-only salvage/hook/segmented rope renderer, original procedural textures

  and icons. Network protocol raft-movement-5 requires matching 0.5.0 clients/servers.

- Milestone 5 validation: build/startup plus 105 real server salvage assertions pass:

  all loaded loot tables and original notes, stable rolls/save/load, exactly-once

  transfers, spawn cap boundaries/lifetime, full-net rollback, moving net reach and

  persisted cargo, loaded basic/upgrade recipes, native item use, single cast,

  durability/range tiers, exclusive claims, full server reel ticks with a moving

  registered fake player, cancellation, and cache cleanup. Ocean checks now also

  sample exposed water detection in five distant chunks. Live spawn timing,

  connected rendering, and two-client races still need user checks.

- Ocean spawn: each new UUID gets a registered 3x3 raft and one-time hammer,

  8 foundations, 16 oak planks, and 8 bread. Normal reconnect preserves position

  and inventory. Default death respawn boards the remembered moving raft; working

  beds/forced spawn, End returns, and hardcore spectators retain native behavior.

  Missing/unloaded raft loading has a 60-tick grace period, then bounded recovery

  on an empty raft without kit refill. An obstructed built raft is preserved and

  a separate empty recovery raft is assigned.

- Ocean ownership: per-world codec-based SavedData maps player UUID to raft UUID

  and last ticking position, updated once per second. Login/death/logout handlers

  run on the server; pending work clears on shutdown. Only the custom Ocean World

  Overworld enables these handlers. Existing normal worlds are not converted.

- Support: every structure connects by occupied grid cells to a foundation;

  removal cannot leave floating structures or split foundations. Doors reserve

  their upper cell. Construction/removal cannot exceed 90% of foundation buoyancy.

- Limits: 1024 foundations, 512 structural pieces, local X/Z -32..32, sixteen structural

  grid levels, five-block interaction range. Foundation mass 12/buoyancy 20;

  structure mass varies by kind and does not add buoyancy. Draft/acceleration adapt.

- Resources: original wood geometry/icons, twenty-six generated structural models,

  fifty-one recipe resources (including eight processing/smelting recipes) (including foundation/hammer).

  Generation sources: tools/generate_building_assets.py, tools/generate_salvage_assets.py,

  tools/generate_crafting_assets.py, and tools/generate_survival_assets.py. English localization.

- Movement correction in 0.3.3: a required client movement-frame payload supplies

  the interpolated raft origin alongside vanilla position packets. The server

  converts supported positions to its current raft origin before vanilla checks.

  Frames are bounded, single-use, and matched to the accompanying position;

  airborne movement remains vanilla. Server carry refreshes the movement baseline.

  Acceptance tests use 0.7.1 on all connected clients and servers. The required payload protocol

  version also prevents connection from older builds without the new piece kinds.

- Validation: build and isolated dedicated-server startup pass. 16 contact/carry,

  17 foundation/direction, 48 structural support/geometry, and 10 movement-frame/

  codec regressions pass (91 total). 725 runtime assertions pass with an

  unregistered fake player/raft above terrain, covering directions/bobbing/jump

  detach, collision/movement-hook registration, and packet-coordinate conversion.

  268 additional runtime construction assertions pass, verifying loaded recipes/unlock rewards,

  registered Creative-tab contents/search, real entity save/load of all thirty kinds,

  open door/rotation/geometry/current persistence, and restored second-floor carry.

  These checks do not verify connected client timing or live structural gameplay.

- Crafting validation: 48 runtime assertions pass for both 2x2 bootstrap recipes,

  vanilla menu slot layout, validity across 20-block raft displacement, raft/vanilla

  recipes, shift output transfer and ingredient consumption, out-of-range/table

  removal invalidation, and returning leftovers exactly once on close. Component-chain

  checks load the eight new recipe rewards, craft each material through the moving

  native workbench, verify spruce-plank and mixed string/cordage ingredients,

  craft nets/storage/roofs/pillars/hook upgrades, and check exact output counts and

  single consumption on shift transfer. Missing fiber ingredients refuse output.

- Startup check caught an unused stick key in the wall recipe; corrected and

  resource reload now succeeds. Dedicated smoke server shuts down automatically.

- Open bugs: user reports intermittent snap-back. The available client log is

  from 0.3.1 and contains no movement warnings. Version 0.3.3 addresses a found

  interpolation/baseline mismatch; the reported symptom awaits live retesting.

  New building, climbing, upper-floor carry, persistence, and multiplayer

  interaction await user tests. Milestone 4 client world selection, connected

  multiplayer, unloaded-raft timing, beds, and first-spawn packet order also need

  live acceptance. No milestone 4 bug is currently reproduced by automated checks.

  The missing crafting facility is implemented; live opening/rendering and

  connected multiplayer crafting/removal still await user verification.

- Resolved prior startup issue: empty processed metadata caused Missing license;

  resources regenerated and verifyModMetadata now gates packaging and launches.

- Known limits: axis-aligned geometry, stepped roofs, instant doors, simple support

  graph; no physical tilt/rotation, damage, redstone, arbitrary attached vanilla blocks,

  cargo/player mass, or offline drift.

  Matching NeoForge loader is beta.

- Ocean validation: 135 runtime ocean-world assertions pass on an isolated dedicated

  server: five distant actual chunk samples, registered starter/boarding, one-time

  kit, reconnect, ownership codec persistence, moving-raft default respawn, and

  missing-raft recovery. The fake test player records server teleport positions;

  it does not test client acknowledgments or real unloaded-entity timing.

  Additional checks cover actual cup filling, dry/raining/roofed collectors, moved-bed

  respawn/deck fallback and cooperative timed night rest using the real world clock.

  The preset survived server save/reload. Both test servers shut down cleanly.

  Gradle smoke runs require success log markers because the game's server thread

  can catch an exception and return process exit code zero.

- Crafting extension in 0.5.2 (milestone 5): six registered original materials: plant

  fiber, braided cordage, woven mesh, timber brace, scrap fasteners, metal hook head.

  Leaf -> fiber -> cordage -> mesh; planks/sticks -> braces; scrap -> fasteners;

  fasteners/nuggets -> hook head -> reinforced hook. Native string substitutes for

  cordage through the bindings tag. Building recipes consume meaningful components,

  with direct chest/barrel alternatives and retained vanilla conversion support.

  Foundation, hammer, workbench, and basic hook remain bootstrap-accessible.

  Six original procedural icons; eight new recipes/unlocks; total 32 recipes.

  No research gates or new processing machines. Recipes documented in

  docs/CRAFTING_CHAINS.md. Live recipe-book/balance/connected-menu tests pending.

- Milestone 6: implemented in 0.6.1; user explicitly authorized milestone 7.

  Live acceptance of earlier fixes remains pending.

  Seven kinds: purifier, rain collector, grill, cooking pot, planter, smelter, bed.

  Six machines retain persisted 27-slot inventories; original registered processing

  menus label inputs/fuel/extraction-only outputs and synchronize progress, fuel,

  reservoir units and pause reasons. Spare slots appear in Supplies; old item

  indices/data persist. Native mouse transactions use filtered shift-click routing. Native cooking-fuel components

  and speed multipliers are read through a furnace context. Input changes reset work;

  output/byproduct fit is checked before fuel/progress/atomic completion. Remaining

  fuel is retained while idle/blocked. Progress/fuel/work key persist with cargo;

  unload/offline processing pauses. Removal/destruction drops inputs/outputs once.

- Survival items: timber cup, sea-water cup, fresh-water cup, crumb bait, planting

  mix, sea-garden stew, baited ocean rod. Empty cups fill from actual source water;

  all source water is treated as seawater for now. Clean cups use native consumption,

  restore six hydration, and return cups. Player thirst is a serialized owner-synced

  NeoForge attachment (20 max); drain timer is persisted entity data. One point per

  600 ticks, sprint multiplier two. Zero gives nonlethal damage once per 100 ticks

  outside Peaceful; Creative/Spectator exempt. Death resets hydration.

- Survival recipe resources: four crafting supplies/rod recipes, seven station

  crafts, seven custom timed processing recipes, one scrap-smelting recipe. Recipe

  type/serializer wrappers use the verified 26.3 shapeless codec, with duration and

  optional byproducts, and cannot assemble as crafting-table recipes. Purifier/rain

  fill in 200 ticks, planter four crop options in 2400, stew in 400. Grill uses

  campfire recipes; smelter uses furnace recipes plus scrap -> iron in 400 ticks.

  Rain checks vanilla rain/sky and owned overhead geometry. Planter needs light 9

  and clear overhead. Seeds/carrots/potatoes now appear in barrel/crate loot.

  Harvest: two food crops and two replanting seeds/crops. Watering returns cups immediately. Stew restores

  eight hunger and returns bowl. Furnace experience is not awarded by raft machines.

- Baited rod: native FishingRodItem/FishingHook behavior, native loot/bites/open

  water/enchantments, 128 durability. Survival casts require/consume one crumb bait

  in main inventory; reeling does not consume another. Vanilla rods remain native.

- Rest bed: compact original model; in Ocean World, use sets persisted/copy-on-death

  raft UUID + cell attachment and updates existing ownership. Default death respawn

  first tries the moving bed coordinates with collision/headroom checks, then safe

  deck/recovery. Working vanilla beds chosen later retain precedence. Five-second

  cooperative rest within three blocks; uses sleeping percentage and native clock

  wake marker/sleep-finished event/time rules. Hostile mobs refuse initial rest;

  damage, range/removal/logout or thirty-second timeout cancels. No lying animation.

  Night rest supports dimensions that can sleep through night; moving respawn is

  Ocean-World-only. Rest does not clear weather.

- Rendering: seven original procedural station materials now used by placed geometry

  as well as item models; original seven supply/rod icons. Client-only thirst layer

  uses the verified 26.3 GuiGraphicsExtractor/Hud APIs and honors hidden HUD. Network

  current protocol raft-movement-7.1 requires matching 0.7.1 clients and servers.

- Survival validation: build plus 128 real runtime assertions pass, covering thirst

  drain/sprint/clamping/Creative exemption, native drink/cup and stew/bowl remainders,

  player save/load and death attachment copying, native baited cast/reel, six shared

  inventories across movement/removal, full-output rollback, persisted partial

  processing/fuel, native food/scrap cooking, stew byproduct capacity and no crafting

  bypass, and all four renewable harvests. Ocean checks total 135. No connected-client

  HUD, fishing-catch, multiplayer timing or bed-respawn acknowledgments validated.

- Next step: close old client/server, build/run 0.7.1 and test docs/MILESTONE_7.md.

  Existing snap-back and connected multiplayer acceptance remain pending. Wait for

  gameplay results before milestone 8 unless the user explicitly asks to proceed.

- Milestones 8-12 have not started. Universal arbitrary vanilla blocks/station

  expansion is still pending. Crop animation/bed animation, hoppers,

  redstone, automatic supplies and XP are known prototype limits; balance/config

  and German localization remain milestone 11.



- Survival polish 0.6.1: six client-only original wood/navy processing screens,

  per-machine accent colors, role-filtered inputs, output-only ready slots,

  progress and remaining-fuel meters, water gauges, pause reasons, slot help,

  server-controlled Supplies tab with independent per-player page state.

  Short-safe normalized data avoids overflow on long recipes/fuels.

- Reusable cups: source-water filling uses an explicit native transformed-hand

  result; transfers affect exactly one cup in either hand. Sea water pours into

  purifier raw reservoir; empty cup draws fresh water; fresh cup waters planter/

  pot. Returned cups are immediate. Full/empty tanks reject unchanged. Rain works

  without cups. Raw/clean capacity four, progress/fuel/water persist. Inserted cups

  return into dedicated ready slots before processing; blocked returns preserve

  inputs. Existing cup stack capacity stays 16 to preserve old inventories.

- Cosmetic polish: original deterministic 32-pixel side/top station materials,

  riveted metal, wood grain, slatted grill, soil, cloth and purifier coil/funnel.

  No Raft assets/text copied. Models/collision geometry remain compatible.

- Item-loss report: original exact live trigger has not been reproduced. New

  native server regressions pass for right-click deposits/half-stack pickups,

  filtered shift-clicks, protected outputs, old spare storage, either-hand and

  stacked cup transfers, source-water filling, full-inventory hand replacement

  and safe overflow drops. Live client retest is required before closing report.



- Current milestone: 9, islands and exploration in 0.9.0; awaiting live gameplay acceptance.

  Milestones 1-8 implemented; user authorized advancing. Milestones 10-12 not started.

- Navigation: original sail (mass 8, height 3), steering wheel (4), engine (12),

  anchor winch (6), and new wind streamer (2, height 2): 31 total building kinds.

  Paddle has 192 durability. Six data-driven navigation recipes/unlocks. Steering

  wheel recipe: 3 timber braces, 1 binding, 1 scrap fastener. Wind streamer recipe:

  1 timber brace, 1 stick, 1 binding, 1 woven mesh. Original procedural textures.

- Physical turning: wheel adjusts shared target heading in 15-degree steps;

  afloat hull turns at up to .75 degrees/tick, including at rest. Anchors lock

  translation/rotation; shore/border/unloaded chunk obstruction prevents turns.

  Propulsion uses actual hull yaw. Local/world transforms now govern building

  ray selection/edge placement, stations, drops, water sampling, ladders, boarding,

  renderer and player carry. Native tracked hull yaw interpolates on the client.

- Collision: conservative 1/16-block horizontal strips for rotated rectangles;

  exact at quarter turns, four-angle cache invalidated on layout changes. No

  arbitrary OBB rigid-body solver. Slightly wider edges and large-build live

  performance remain limits. Cosmetic parts retain stable colliders; cloth on

  the passive wind streamer is non-solid. Rotation midpoint/destination collision

  tests run before applying the turn; other rafts and terrain still obstruct.

- Player carry preserves local feet and rotates view yaw, while normal walking

  remains native. Own raft temporarily excluded only during external carry clip;

  terrain and other rafts still clip it. Upward ladder movement retains carry;

  jumps/flying detach. Yaw-aware paired movement frame rebases feet and view to

  server origin/yaw; finite checks, raw-packet matching, support, 15-degree angle,

  2-block origin and 1.5-block player displacement limits remain enforced.

  Current protocol raft-movement-9 requires matching 0.9.0 clients/server.

- Visuals: wind streamer points toward wind in world coordinates and flutters;

  trimmed sail flutters and smoothly folds/unfolds. Original open wheel/spokes

  respond to heading error; running engine crank turns; winch handle animates

  raising/lowering. No physical chain/propeller, wind torque, roll/tilt or sounds.

  Client-only north-up compass: cyan wind, amber hull, speed in m/s, meter and

  active drives/anchor status. Wind/drive snapshots refresh about once per second.

- Persistence: actual HullYaw plus target heading, sail trim, enabled/anchor states,

  engine fuel/reserve save with raft. Old worlds default HullYaw=0; storage,

  buoyancy/support limits and existing station processing remain compatible.

  Native fuel reserves burn exactly one tick per active afloat engine tick;

  stopped/dry/anchored engines preserve fuel. Paddle impulses remain transient.

- Validation: build succeeds. 11,908 standalone checks; dedicated assertions:

  725 carry, 272 construction, 48 crafting, 85 station, 105 salvage, 128 survival,

  50 navigation, 280 Ocean World. New checks cover 72-heading collider coverage,

  transform round trips/wire codec, finite/angle frame limits, rotated native

  foundation/floor placement, terrain-blocked turn, actual 140-tick ocean turn

  with preserved off-center local feet, anchor rotation lock, moving menu reach

  and native rotating packet yaw rebasing. These use fake players, not live clients.

- Open reports: prior intermittent snap-back and inventory-loss trigger remain

  pending live user retest; no connected-client visual or multiplayer acceptance.

  Next: run 0.9.0 and test docs/MILESTONE_9.md, report results before milestone 10.



Full roadmap: README.md. Original feature request: docs/PROJECT_BRIEF.md.



- Version 0.8.0: SYNCED host gameplay settings (FML 12.0.8) and CLIENT HUD settings,

  native NeoForge configuration screen, commented TOML defaults and world overrides.

  Thirst, processing, current/wind/speed/turning, salvage caps/rates/lifetime,

  creature caps/rates/aggression/damage/bait and HUD visibility are configurable.

- Creatures: server-authoritative sharks circle and wind up exposed-foundation

  bites, chase swimmers, flee after combat, and follow physical expiring bait.

  Crop Skimmers take one item from uncovered planters; raft roofs protect crops.

  Tide Grazers have native wheat temptation, breeding and land movement.

  Natural spawning respects loaded-area caps, occupied rafts and mob spawning;

  friendly fauna requires existing land, with new islands deferred to milestone 9.

- Foundation health saves/syncs; cracked material below 60/100. Hammer + one plank

  repairs 25 health. Breaks retain the largest deck component, drop detached

  pieces/cargo, and prune unsupported/overloaded pieces. Last-foundation loss

  destroys the raft; detached builds do not become separate raft entities.

- Original textures and animated cuboid creature meshes, native spear melee,

  data-driven spear/bait recipes/unlocks and entity loot. English localization.

  No creature sounds or sophisticated obstacle-routing AI yet.

- Additional validation: 29 native bootstrap creature/config assertions and

  21 native ocean creature assertions, including swimmer damage/cooldown,

  physical bait use/refused placement without consumption, rotated deck bites,

  crop theft, persistence, repair costs, caps and detached cargo drops.

  Live client rendering, config screen and connected multiplayer await user testing.

  See docs/MILESTONE_8.md and docs/CONFIGURATION.md.



- Version 0.8.1 milestone 8 extension: limits raised to 1024 foundations,

  512 structures and 16 grid levels; merged foundation collision rectangles

  preserve holes and reduce full-deck rotated strip counts. Decode, health and

  navigation snapshot limits accommodate larger saved/synchronized builds.

- WORLD/EASY/PEACEFUL/NORMAL/HARD synchronized gameMode. Easy halves attack

  frequency, Normal regular attacks, Hard targets two sharks per occupied raft

  and 1.5x attack frequency; Peaceful sharks remain passive and birds do not steal.

  Existing creatures are not deleted when changing mode. Native world difficulty

  may still protect players. Spawning caps and aggression settings still apply.

- Adjacent same-facing, same-height side-by-side chests form deterministic

  reciprocal pairs. Native CompoundContainer provides shared 54-slot storage,

  stable ordering and exact separate persistence. Pair topology/removal invalidates

  open double menus; contents of surviving halves remain intact. Original wood join.

- Thirst HUD: percentage, ten original drops with half-drop precision,

  amber low-water warning; client-only and respects existing config visibility.

- Validation: additional large-save, pairing/shift transfer/reload/removal,

  mode selection, actual second-shark spawn and Peaceful AI checks. Full-size

  ocean movement/turning and merged collision count checked in isolated server.

  Live HUD appearance and large connected-client performance await user tests.



- Final 0.8.1 verification: build, standalone checks and both dedicated-server

  smoke runs pass. Native assertions: 553 creature/config and 307 Ocean World,

  alongside the existing carry/construction/crafting/station/survival/navigation checks.



- Version 0.9.0: Building foundationLimit 9..1024, default 1024. Native placement

  checks it before consumption. Reduced caps preserve existing saved geometry.

- Optional JEI 31.9.0.58 NeoForge 26.3 installed by installClientJei/runClient

  into run/client/mods. Full distribution embeds required Mezz Config 0.6.6;

  no global mod dependency or JEI inclusion in the Raft Ocean release JAR.

  Native crafting viewing works; custom processing categories/raft transfer pending.

- Milestone 9: custom native IslandStructure/IslandPiece using four generated

  original compressed NBT templates with verified DataVersion 5023 lowercase

  block-state id/properties schema. Native structure set: 32-chunk spacing,

  16 separation, weight 3 atoll and 1 per story type; protected origin radius160.

  OceanChunkGenerator adds the set to old empty structure overrides too, affecting

  only newly generated chunks. Data-driven JSON structure/loot/recipe/tag/unlocks.

- Original locations: Drift Atoll, Glasswatch, Garden Relay, Echo Vault. Three

  original journal items advance serialized per-player journey with copyOnDeath.

  Handheld receiver plus craftable antenna upgrade searches 3/6 placement-region

  rings, caches signals and reports island center coordinates/distance/heading.

  Original key, seal and ON/OFF/ON lever puzzle consume exactly one key on success;

  native masonry may be mined, so this is not an enforced adventure-map boundary.

- Existing water/raft motion, survival, wind/steering, chests and server authority

  retain their implementation. Island flora supports existing native grazer spawning.

- In-game visuals, receiver UX, JEI recipe viewing/transfer and multiplayer remain

  user verification. Prior snap-back/inventory-loss reports still need live retesting.



- Final 0.9.0 validation: build/installClientJei and both native server smoke runs

  pass. Ocean World assertions: 339, including 32 new island/journey/puzzle/cap

  checks. Existing 553 creature/config and all earlier native regressions pass.

  The previous native advancement/schema startup errors are fixed in this build.



- Version 0.9.1: client-only visual animation improvements. Speed-responsive

  shark tail/fin movement, bite jaw, banking, bird segmented wings with smoothed

  glide transitions and pecking, grazer leg/head/ear/tail motion, living hurt/death

  poses, and bait bobbing. Cosmetic state uses weak entity maps and frame-time

  smoothing; entity physics and server authority remain unchanged.

- Navigation: segmented sail ripple, smooth wrap-aware sail/streamer rotation,

  steering-wheel easing, and continuously integrated engine crank acceleration

  and coast-down. Build-piece animation maps discard removed pieces.

- Protocol stays raft-movement-9; no synchronized data or save schema changed.

  See docs/ANIMATIONS.md for expected behavior and in-game checks. Storage lids

  remain static. Live animation quality and multiplayer acceptance are pending.

- Final 0.9.1 build passes after the articulated wing/tail joint adjustment.

  Dedicated bootstrap and Ocean World runs pass all existing native regression

  checks (553 creature/config, 339 ocean-world, and earlier movement/station/

  survival checks). Client compilation passes; live rendering is not verified.



- Version 0.9.2: original 64px deck/cloth/creature textures generated by

  tools/generate_visual_assets.py, revised face-specific shark skin, separate fin/

  belly/feather/eye/tooth/beak/hoof materials, tapered shark mesh and downward

  animated jaw. New visual block registrations require matching mod builds on

  client and server; worlds retain their save schema and raft-movement-9 protocol.

- Shark AI swims 1.08 blocks below the hull/water surface, targets exposed cardinal

  foundation sides rather than radial offsets, and safely recovers existing

  overlap into loaded water below actual rotated/merged raft colliders. Native

  regressions cover overlap recovery, resumed travel, concave bite approach and

  1024-foundation recovery. Physics/carry for players are unchanged.

- Ocean World filters drowned from natural spawning, cancels join/load/conversion,

  and discards any already-loaded drowned without death drops. Ordinary presets

  are unaffected. Native regressions cover spawn tables, join/load, cleanup and

  preservation of normal-world spawning. Live visuals and multiplayer remain

  pending user testing, alongside earlier snap-back/inventory-loss reports.

- Final 0.9.2 verification: build and both dedicated-server checks pass (554

  creature/config and 351 Ocean World assertions, including the additional

  above-deck station recovery case). Packaged JAR: build/libs/raft_ocean-0.9.2.jar.

  Original texture atlas inspected locally. One initial parallel smoke process

  failed in jvm.dll before gameplay; sequential retry and final runs passed.



- Version 0.9.3: server-synchronized shark bite windup and successful-impact

  timestamp; deterministic jaw anticipation/snap, forward thrust, recoil and tail

  kick. Foundation attacks face the attacked cell. Swimmer damage timing and

  cooldown remain unchanged. Render-only thrust does not move hitboxes or players.

- Protocol raft-movement-9.3 requires updated clients and server. Saves remain

  compatible; transient attack animation data resets on load/interruption.

  Native regressions cover bite pose phases, successful foundation/swimmer impact

  publication and interruption. Live bite appearance and multiplayer need testing.

- Final 0.9.3 validation: build plus both isolated dedicated-server runs pass;

  558 creature/config and 354 Ocean World assertions. All earlier movement,

  construction, inventory/station and survival checks pass. Packaged artifact:

  build/libs/raft_ocean-0.9.3.jar. Live rendering is still user verification.



- Version 0.9.4: six original ordinary islands, template footprints 17/25/33/49/

  65/81 blocks. Sandy shoal, small grove, flower meadow, lagoon, rocky hill and

  broad woodland contain native harvestable resources without story loot/puzzles.

  Generated deterministically by tools/generate_ordinary_islands.py, with native

  lowercase id/properties block-state NBT and DataVersion 5023.

- Independent ordinary_islands structure set: spacing20, separation12,

  salt6842193, weights4/4/3/3/2/1, six-chunk exclusion around the original island

  set. Existing story weights/spacing and receiver destinations are unchanged.

  OceanChunkGenerator adds both sets to old settings; IslandStructure centers

  variable template sizes. Only newly generated chunks gain the new islands.

- Protocol remains raft-movement-9.3, saves compatible. Native checks exercise

  all template sizes, absence of story containers/seals, resource trees, native

  structure location, correct seabed placement and cross-chunk shore placement.

  Live island appearance and generation performance remain user testing.

- Final 0.9.4 validation: build and isolated Ocean World run pass, including

  391 native world assertions (37 new ordinary-island checks). All standalone

  movement/geometry checks pass. Packaged JAR: build/libs/raft_ocean-0.9.4.jar.



- Version 0.9.5: destination clearance for rigid carry, pre/post player overlap

  recovery, and clearance validation for rebased movement packets. Candidate

  corrections are bounded to 1.5 blocks, stay supported, check loaded terrain,

  border and other rafts, and cannot cross new raft solids. Tiny deck penetration

  may be lifted to the support surface. Fully enclosed/no-space recovery refuses.

- Shark swimming and attack heading changes are bounded to six degrees/tick,

  with velocity following the turn for a curved approach. Swimmers are acquired

  within twelve blocks; facing within25 degrees is required before bite damage

  or foundation windup. Bait, difficulty and bite damage/cooldowns remain intact.

- Protocol raft-movement-9.5 requires updated client and server; saves compatible.

  Native checks cover embedded object recovery, corner rotation carry, repeated

  turns, enclosed refusal, narrow deck support, shark turn limits and approach

  arcs. Live trapping/snap-back and multiplayer remain pending user retest.

- Final 0.9.5 validation: build and both isolated server runs pass. Native checks:

  335 construction (63 new clearance regressions), 406 Ocean World (15 new shark

  turning/approach assertions), and all earlier carry/crafting/station/survival/

  navigation/creature checks. Earlier exact-carry fixtures were moved away from

  railings/equipment so they test unobstructed carry; actual overlap and turning

  corners now have dedicated regressions. Packaged JAR: build/libs/raft_ocean-0.9.5.jar.

