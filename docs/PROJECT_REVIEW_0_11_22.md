# Project review — 0.11.22

This review covers moving-player support and collision recovery, rendered machine
and sail geometry, inventory/menu transactions, recipe/JEI cache handling, creature
timers, addon ownership, asset references and localization. It is a maintenance
pass in milestone 11; the story remains open-ended.

## Fixes

- Moving carry selects the highest supporting surface within the existing foot
  contact tolerance. Previously, a thin platform could be mistaken for its lower
  foundation because the foundation collider was listed first. The subsequent
  overlap correction could leave grounded state unset, causing repeated recovery.
- Equal-distance recovery candidates now have explicit coordinate tie-breakers.
  Symmetric obstacle recovery no longer depends on hash-set iteration order.
- Sail cloth renders once with its original complete texture and border. The
  previous four-strip animation scaled the entire model into every strip,
  repeating its trim and texture and opening seams between animated segments.
  Deployment, wind flutter and sail trim remain animated; colliders are unchanged.
- The bootstrap test gate now requires the native movement-validation completion
  marker as well as the collision and other gameplay completion markers.

The regression fixture uses a real registered thin addon platform. It verifies
40 consecutive rotating carries with bobbing, checking foot height, grounded
state and absence of overlap while both the foundation and platform are within
the contact tolerance.

## Local verification

1. Walk beside machines, turn the raft and climb between floors. Look for repeat
   shoves, falling through the deck or becoming stuck; ordinary jumping should
   still detach from carry.
2. Inspect deployed sails from both sides while trimming, furling and sailing.
   Texture borders should stay continuous with gentle whole-panel flutter.
3. Open machines/chests, shift-click supplies and craft recipes through normal
   tables, raft tables and JEI. Check that inventories survive reconnecting.
4. Confirm the shark ten-minute and bird five-minute delays, and test Peaceful.

Rendering changes need confirmation in a running client. Shader/resource-pack
interactions, real co-op latency, large-raft frame rate and the previously
reported movement corrections remain subject to local testing. Rotated collision
still uses conservative 1/16-block strips; this pass does not replace the physics
system or promise that all possible placement/latency combinations are glitch-free.

Validation log: `build/project-review-runtime.log`.

## Automated results

Build, bootstrap and ocean suites passed, including 1,210 native movement,
425 collision, 729 carry, 935 construction and 756 ocean assertions, plus
machine/API, crafting, stations, research, salvage, survival and navigation suites.
Native checks passed for 130 inventory silhouettes, 6,125 fill/mount alignments
and all 60 polygon meshes (11,519 quads).

All packaged audits passed: models and renderers, custom block/item references,
diving gear, open story endings, 521 English/German keys and model surface safety.
The surface audit verified 523 models and 163,064 atlas-safe faces; model envelopes
match 0.11.21 and all 60 meshes pass the coplanar overdraw check. Referenced label
coverage also passed for all 58 buildables.
