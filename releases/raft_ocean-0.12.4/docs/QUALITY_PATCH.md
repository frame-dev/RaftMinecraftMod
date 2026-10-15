# Quality patch — 0.12.4

This pass reviews existing gameplay and assets and targets specific defects.
It does not expand the story ending or change raft collision envelopes/models.

## Improvements

- Biofuel engine duration now honors automation.biofuelEngineTicks, default
  6000 loaded ticks, before navigation.engineFuelMultiplier. It applies when
  the next canister ignites. Existing burning fuel is not recalculated.
- Navigation packets validate complete records before committing, preserving
  the previous frame on malformed data. Counts now match the existing maximum
  512 structural pieces. No physical movement algorithm was loosened.
- Chest pairing is cached per raft layout revision rather than sorted every
  frame. Position and heading do not invalidate it; structural changes do.
  Cache entries use weak raft keys and immutable maps.
- Anchor release and unattended gaps restart the ten-second debris settling
  delay. Drops already spawned keep their original drift and anchor behavior.
- Machines report a supplied combination with no matching recipe. Empty
  inputs still request supplies. Rejected recipes spend no ingredients, stored
  water or fuel. Recipe-meter and navigation tooltips are bilingual.
- Engine 1.5 m/s, sail 1.0 m/s and paddle 1.8 m/s base targets remain intact.
  Wind, load, drift, fuel, config multipliers and combined propulsion still apply.

## Local playtest

1. Walk around machines, climb stairs and turn the raft under engine power.
   Check for falling through, unexpected sideways pushes or server corrections.
2. Fuel an engine with a fresh biofuel canister. With default settings, it
   starts with five minutes of burn reserve; anchoring or switching it off
   preserves that reserve. Try engine reverse without turning the hull.
3. Lower the anchor, wait, then raise it. Existing debris resumes drifting;
   newly spawned salvage waits for the stream to settle. Changing course still
   starts a new forward corridor.
4. Open two paired chests, remove one, rebuild it and reopen. Contents must
   remain with their original halves and pairing must update immediately.
5. Hover machine progress and steering controls. Try invalid machine inputs
   (including combinations inserted before a recipe datapack reload), then
   a valid recipe. Invalid recipes explain the issue and preserve supplies.
6. Inspect models, fills and inventory/JEI icons, and test with a second player.

Automated asset checks cover references, UVs, geometry, localization and
recipe data. They cannot confirm final Minecraft lighting or every multiplayer
latency scenario. Report any remaining correction with a log and the raft setup.
