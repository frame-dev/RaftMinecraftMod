# Changelog

## 0.12.3 � 2026-10-12 � propulsion balance

- Engine base drive is 1.5 m/s; aligned sail base drive is 1.0 m/s at unit wind.
- Paddle peak drive is 1.8 m/s, including repeated strokes; strokes still decay.
- Existing load, wind, fuel, current, configuration multipliers and combined
  propulsion remain active. These are base targets, not fixed HUD speeds.
- Added native speed and repeated-stroke regressions; engine bearing/reverse
  checks now verify the requested engine speed at every tested mounting angle.

## 0.12.2 — 2026-10-12 — survival model refinement

- Refines basic/advanced purifier, rain collector, cooking pot and smelter
  models with original framing, fittings, bands, grips, vents and joinery.
- Matching static/inventory designs, refitted GUI silhouettes and sanitized
  opaque surfaces. Existing texture materials are reused.
- Keeps placement/collision envelopes, machine-fill anchors and save format.
- Retains net feedback, raft-specific placement hints and the open-ended story.

## 0.12.1 — 2026-10-12 — preview refinement

- Net pickup distinguishes an empty net, full inventory and partial pickup.
  Partial pickup reports the exact cargo left for the next crew member.
- Placement HUD hints are tied to their owning raft, preventing a stale hint
  when the crosshair switches between nearby rafts.
- Disconnect/reset clears cached placement hints together with build rotation.
- Adds native cargo-count/stale-location checks while retaining item-preserving
  partial pickup, save format, graded islands and the open-ended story.
- All clients/server should update together to protocol 12.1.

## 0.12.0 — 2026-10-12 — preview release

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

- Enlarged all island types: ordinary islands 33–161 blocks, exploration
  islands 97, trading outposts 129, and story islands 113–193.
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
