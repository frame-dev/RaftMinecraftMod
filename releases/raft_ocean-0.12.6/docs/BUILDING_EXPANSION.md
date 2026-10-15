# Building expansion — 0.11.15

Added Window Wall, Gable Wall, Open Archway, Half-height Stairs, Wide Stairs,
Tall Ladder, Wide Floor and Wide Crafting Bench. All have original custom models,
crafting recipes, recipe-book unlocks and English/German names.

The wide bench, floor and stairs are single objects spanning two cells. They
consume/refund one item and reserve both cells at every quarter-turn rotation.
The wide crafting bench opens the native3x3 crafting menu and is registered as
a JEI crafting station. It requires support under both base cells. Wide floors
also support floor-mounted objects on their second cell; dependent equipment
prevents removing the floor. Footprint support damage is pruned as one object.
The two-high ladder climbs through its full height; the archway has an open
walkable center rather than a solid bounding-box wall. Existing pieces and save
ordinals remain stable. Addon developers can register buildables via API v1;
see RAFT_ADDON_API.md and examples/raft-addon.

## Test locally

1. Restart the client with build/libs/raft_ocean-0.11.15.jar.
2. Find the new pieces in the construction tab and JEI; craft each recipe.
3. Place the wide bench over two supported tiles and try all four orientations.
4. Its ghost should cover both tiles; another piece must not occupy its second tile.
5. Craft in the bench, then crouch with the hammer to remove the whole object.
6. Place new walls, stairs and tall ladders; walk through the archway and climb.
7. Save/reload and repeat on a drifting/turning raft and in co-op.

Expected: one item per wide object, accurate ghost footprint, functioning crafting
menu, correct support/removal rules, and no new walking snap-back. Human client
visuals, co-op latency and large-raft performance remain pending.

## Verification

Build and bootstrap pass. Native tests:83 building/API,893 construction,
417 crafting,1210 movement assertions; all existing runtime suites pass.
Minecraft's parser accepts464 models /25561 elements;130 inventory silhouettes
fit;6125 machine fill/mounted geometry checks pass. Four packaged asset audits
pass. The example addon builds as a separate project.
Logs: build/buildables-final-runtime.log, build/buildables-final.log,
build/addon-example-build.log. Preview: previews/building-expansion-0.11.15.png.
