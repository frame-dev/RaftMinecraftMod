# Changelog



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

