# Changelog

## 0.13.8 preview — juicer and reusable prepared drinks

- Battery-powered Raft Juicer with four food pads and explicit Blend/Pause/Serve.
- Three original crop drinks: Melon Breeze, Orchard Cooler and Shoreberry Tonic.
- Separate post-blend glass serving; drinking restores hydration and returns a glass.
- Atomic completion/serving, saved queue/pause/ready portions and idle power safety.
- Recipe changes cancel prior work; no automatic repeat or free bottled dismantle drops.
- Native data-pack recipe type, optional JEI category, recipe links and reload support.
- Original idle/blending/ready models, tall inventory model and custom drink models.
- Electrical research recipe, a bottle-safe glass recipe, configurable speed/hydration.
- English/German guidance, API3 retained and handshake13.8 for matching peers.
- Speeds, predator delays, harder research and open-ended story remain unchanged.

## 0.13.7 preview — surplus recycling and outpost purchases

- Original battery-powered Salvage Compactor and bound Salvage Bale models.
- Exact mixed forty-point batches use seven feed slots; native battery in slot8,
  four-bale buffer in slot9. Inputs stay intact until atomic completion.
- Dedicated workshop UI shows feed points, charge, progress and Pause/Resume.
- Saved progress pauses without materials, power, output space or when disabled.
- Data-pack feedstock tiers, configurable enable/time, electrical research recipe.
- Five bale purchases: beetroot seeds, carrots, table lamps, rugs and reef flippers.
- Old outpost stock gains these offers once without resetting uses or restock time.
- Native batch/menu/save/trade regressions; English/German text, API3 and handshake13.7.
- Speeds, predator delays, harder research and the open-ended story retained.

## 0.13.6 preview — deliberate cooking batches

- Cook prepares one cooking-pot batch and stops after completion.
- Saved queue and pause/resume retain progress and fuel across reloads.
- Server preflight validates recipes, water, cup returns and output space.
- Changing recipes cancels previous progress. Existing food/cup/bowl recipes remain.
- Older active pots resume one batch; idle pots wait. Setting manualCooking to false
  restores continuous automatic cooking. Storage indices and addon API3 retained.
- English/German controls and handshake13.6; matching clients/server required.
- Story, research difficulty, custom speeds and predator delays remain unchanged.

## 0.13.5 preview — sailing and engine capacity

- Sails share a wind-limited drive budget, with crosswind steering, opposed-trim
  cancellation and no speed stacking or upwind propulsion. No cargo-mass sail penalty.
- Engines use configurable full/half/stalled foundation-load bands; six fueled
  enabled engines bypass capacity. Shared drive preserves the1.5m/s nominal base.
- Stalled engines preserve fuel. Existing mounting, reverse, anchors and paddles remain.
- Engine capacity/status and sail wind feedback added to bilingual controls.
- engineLoadLimits=false restores earlier engine scaling; prior saves are retained.
- User speeds, difficulty delays, research and open-ended story remain unchanged.
- API3 unchanged; handshake13.5 requires matching peers.


## 0.13.4 preview — expedition notebook

- Archive all 28 collected original field notes permanently without consuming books.
- Read the archive through the receiver, sneak-use journals or `/raftnotebook`.
- Journey overview, recovered main journals and optional-route logs share a paged,
  bilingual screen; existing journey progress remains readable after upgrading.
- Personal server-owned data survives saves and death; no future notes are granted.
- Original story remains open-ended. Numeric code entry is still future work.
- API3 unchanged; handshake13.4 requires matching clients/server.


## 0.13.3 — 2026-10-18 — mounted survey receiver

- Original receiver console and three-antenna hardware use the existing raft API,
  with shared placement/support/collision, saved battery inventory and crew access.
- Upper-deck antenna triangles, portable charge, on/off, known story channels,
  bounded/cached scans and a console-relative animated radar with setup feedback.
- Basic Tidal Battery moves to early signal research and crafts with1000charge;
  previous power-project owners retain access. Advanced power remains story-gated.
- Added console/antenna recipes and a handheld recovery recipe; existing handheld
  receivers and journey saves remain usable. The eastern ending remains unresolved.
- Native models fitted to inventory, coplanar face overlaps removed, and inherited
  schematic folios included in exact registry coverage. EN/DE labels; handshake13.3.


## 0.13.2 — 2026-10-18 — story schematic progression

- Seven original schematic folios in guaranteed main story chests gate eight
  advanced research projects, while starter survival and initial travel remain open.
- Discovery is recorded automatically in player inventory, retained on death/save,
  and separate from studying materials and explicitly learning recipes.
- Research menu shows missing-plan guidance and source island; server rejects
  premature Learn actions. Existing research/JEI output visibility is retained.
- Crew discovery sharing respects the existing toggle and guest roles, persists
  in crew data and loads older crew saves with no new field required.
- Previously learned recipes stay learned; reading existing main island journals
  recovers plan discoveries for older looted islands. Admin unlock includes plans.
- Original folio models and EN/DE labels; protocol13.2 requires matching peers.


## 0.13.1 — 2026-10-18 — staged story exploration

- Seven strictly increasing story island widths, from 129 to 249 blocks, with
  irregular coastlines, preserved main landmarks and connected side-site routes.
- 28 guaranteed English/German field-note books tell additional crew accounts
  across shelters, gardens, lookouts and grottoes. The story remains open-ended.
- Research consumes complete material batches of 2–4 samples once; the research
  screen displays costs and refuses incomplete batches without consumption.
- Existing knowledge, starter survival paths and journal/puzzle identities retained.
- Larger terrain and new loot apply to newly generated chunks.




## 0.13.0 — 2026-10-15 — addon API v3 and starter SDK

- Opt-in POWER, FRESH_WATER and BIOFUEL inputs connect addon machines to native
  raft utility networks, including rotated/wide footprints. No cross-raft draws.
- Added read-only preflight and exact item/component/resource batch commits;
  blocked outputs or missing supplies spend nothing. Stale contexts invalidate.
- Readiness demand wakes native generators at their normal production rate.
  Paused sources and global automation disablement remain respected.
- Addons can opt into output-only quick collection while retaining extraction
  locks. Existing controllers keep their original sneak-use behavior by default.
- Fixed generic addon menu shift-click merges bypassing insertion rules.
- Cable/pipe visuals recognize addon input declarations; connection and utility
  topology lookups are cached by structural revision.
- Added complete utility-powered sediment washer example, corrected example
  loader range, v3 guide and standalone SDK with wrapper/API jar/sources.
- Existing v1/v2 registration/controller methods and save identities retained.
  All peers must update the main mod; addon metadata must declare compatible ranges.

## 0.12.6 — 2026-10-15 — easier machine and storage interactions

- Sneak-use with an empty hand collects role-labelled completed machine outputs.
  Ingredients, fuel and charger sockets stay in place; excess remains stored.
- Machine and net pickups share fit-only inventory transfers, preserving item
  components and preferring backpack space before empty hotbar slots.
- Added contextual machine/net/quick-stash hints with English/German labels.
  Hints wrap on smaller screens and can be disabled in the client configuration.
- Added server setting building.quickMachineHarvest; ordinary use still opens
  menus. Shortcuts honor distance, crew ownership and spectator restrictions.
- Spectator quick stash is explicitly rejected. Localization maintenance source
  repaired so regeneration does not introduce malformed German characters.
- Models, continuous animations, propulsion base speeds and open story retained.

## 0.12.5 — 2026-10-15 — workshop models and motion

- Detailed original charger, biorefiner and drying tray models, with matching
  mounted/static/inventory variants, fitted icons and preserved visual envelopes.
- Continuous cooking stirrer rotation; new biorefiner mixing paddle and contents,
  plus wet/dry pieces on the drying tray. Cosmetic parts do not add collision.
- Atomic machine visual updates retain the last complete frame on malformed
  data. Existing numeric clamping and empty-frame clearing remain supported.
- Engine/sail/paddle base speeds remain 1.5/1.0/1.8 m/s; story stays open-ended.

## 0.12.4 — 2026-10-12 — gameplay quality pass



- Fixed automation.biofuelEngineTicks: newly ignited biofuel canisters now use

  the configured burn duration, modified by engineFuelMultiplier. Coal-equivalent

  engine speed remains; existing burn reserves are preserved.

- Navigation snapshots commit atomically and reject incomplete/non-finite data.

  Large raft engine/sail counts support the existing 512-piece ceiling instead

  of silently displaying a maximum of 128.

- Double-chest pairing reuses an immutable raft-layout cache during motion and

  rendering, and refreshes after building/removal/reload.

- Debris corridors settle again after anchoring or long unattended gaps instead

  of resuming immediately. Existing debris remains governed by its saved drift.

- Machines distinguish an invalid supplied recipe from missing ingredients,

  preserving inputs, cups, water and fuel. Added English/German machine recipe,

  steering and engine reverse hints; longer navigation titles fit the panel.

- Requested engine/sail/paddle base speeds remain 1.5/1.0/1.8 m/s.



## 0.12.3 — 2026-10-12 — propulsion balance



- Engine base drive is 1.5 m/s; aligned sail base drive is 1.0 m/s at unit wind.

- Paddle peak drive is 1.8 m/s, including repeated strokes; strokes still decay.

- Existing load, wind, fuel, current, configuration multipliers and combined

  propulsion remain active. These are base targets, not fixed HUD speeds.

- Added native speed and repeated-stroke regressions; engine bearing/reverse

  checks now verify the requested engine speed at every tested mounting angle.



## 0.12.2 â€” 2026-10-12 â€” survival model refinement



- Refines basic/advanced purifier, rain collector, cooking pot and smelter

  models with original framing, fittings, bands, grips, vents and joinery.

- Matching static/inventory designs, refitted GUI silhouettes and sanitized

  opaque surfaces. Existing texture materials are reused.

- Keeps placement/collision envelopes, machine-fill anchors and save format.

- Retains net feedback, raft-specific placement hints and the open-ended story.



## 0.12.1 â€” 2026-10-12 â€” preview refinement



- Net pickup distinguishes an empty net, full inventory and partial pickup.

  Partial pickup reports the exact cargo left for the next crew member.

- Placement HUD hints are tied to their owning raft, preventing a stale hint

  when the crosshair switches between nearby rafts.

- Disconnect/reset clears cached placement hints together with build rotation.

- Adds native cargo-count/stale-location checks while retaining item-preserving

  partial pickup, save format, graded islands and the open-ended story.

- All clients/server should update together to protocol 12.1.



## 0.12.0 â€” 2026-10-12 â€” preview release



Milestone 12 packages the existing survival mod for local client/server testing.

This release retains the gameplay and save format from 0.11.41.



- Ships the installable mod jar, a separate sources jar, installation guide,

  known limitations, release verification summary and SHA-256 checksums.

- Replaces outdated milestone-1 README instructions with current gameplay,

  installation, multiplayer, configuration and development instructions.

- Updates the Mods-screen description, which still described milestone 3.

- Uses the 0.12.0 movement protocol; update all clients and servers together.

- Includes larger islands, the open-ended seven-destination story, trading,

  research, automation, co-op and the collision/model refinements below.

- Retains the checkout's connected, graded coastlines and automatic conduit

  junction visuals. Story checks account for intentional terrain grading while

  requiring authored architecture, puzzle blocks and loot to remain intact.

- Preserves historical update notes in `docs/DEVELOPMENT_HISTORY.md`.

- Fixes pipe/cable corner, tee and cross meshes extending past their normalized

  bounds; removes overlapping opaque junction faces before packaging.



No additional gameplay chapter or release hosting is introduced. This is a

preview release: live visual, network and modpack checks are still pending.



## 0.11.41



- Separate physical collision profiles for beds, seats, lamps and palms.

- Supported bed respawns at mattress height and collision-geometry caching.

- Correct engine-arrow rotation; refined engine and wood/metal grill models.

- Matching grill inventory models and addon example dependency maintenance.

- Expanded native movement and furniture regression coverage.



## 0.11.40



- Enlarged all island types: ordinary islands 33â€“161 blocks, exploration

  islands 97, trading outposts 129, and story islands 113â€“193.

- Extra vegetation/reefs and footprint-aware cross-pool spacing.

- Kept authored loot, puzzles and landmarks at their original scale.

- Fixed isolated-test shutdown timing and detect server shutdown errors.



## 0.11.39



- Seven distinct concave story coastlines, including hooked, branching,

  forked and jagged shapes; retained all 28 optional side areas.

- Clearer trail/entrance validation and preserved authored story cores.



Earlier development notes: [archived history](docs/DEVELOPMENT_HISTORY.md).

Historical version numbers and instructions in that archive describe older

builds; use the current README for installation.

