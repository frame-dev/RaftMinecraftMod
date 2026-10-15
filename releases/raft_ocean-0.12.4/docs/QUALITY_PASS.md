# Raft Ocean 0.11.41 — collision and model refinement

This continues milestone 11. Existing survival, crafting, machinery, research,
co-op, story and enlarged-island content remain part of the project.

## Changes

- Cabin beds and double beds use the mattress height rather than a single
  collision box stretched up to the headboard. Moving bed homes choose a
  supported surface directly beneath the respawning player.
- Chairs, sofas and deck benches have a low continuous seat body and separate
  back/arm parts. Players can stand on the seat without colliding with a solid
  invisible box filling the space above it.
- Lamps retain their narrow stems and separate shades. Potted palms collide
  with the pot and trunk; decorative foliage is visual rather than a solid wall.
- Current world collision geometry is reused until the raft moves, turns or
  its structure changes. The cache never supplies old positions after a move,
  rotation, construction, door toggle or removal.
- The engine direction indicator rotates as geometry around its mounting
  point. Diagonal directions retain their shape rather than becoming wider
  axis-aligned bounding boxes.
- The engine has a chamfered salvage-metal hood, service controls, vent trim,
  a fuel cap and cooling pipe. Grills have open roasting trays, grate bars,
  charcoal and distinct wood/metal supports. Grill inventory and static models
  share that design and keep their fitted inventory silhouettes. Materials
  and models are original.
- The API example builds against the current main-project jar instead of a
  fixed old version. API v2 compatibility is retained.

## Verification

The build, both isolated servers and the addon example build passed. Server
checks include 2,904 real packet movement assertions across twelve piece kinds,
3,638 construction assertions, 262,811 crafting assertions and 5,111 fresh
Ocean World assertions. Both servers saved and closed cleanly.

The native model parser accepted 952 models and 45,566 elements. All 165 GUI
silhouettes, 18,797 fill/mount checks and 66 polygon meshes passed. Packaged
surface, texture-reference, localization, research, recipe, island-route and
story-preservation audits passed. These checks complement the local playtest
below; they do not replace it.

## Playtest

Use the same 0.11.41 jar on every client and the server.

1. Step onto a cabin bed mattress and a chair seat. Walk away and jump off.
2. Stand on those surfaces while the raft drifts and turns. Movement should
   follow the raft without repeated side pushes or sinking into furniture.
3. Walk near lamps and palms, and check decorations on raised tables.
4. Remove furniture and open/close doors; old collision shapes should disappear.
5. Test engine forward/reverse at different raft headings. Its arrow should
   keep its shape and indicate the requested propulsion direction.
6. Check model lighting, inventory icons, grill food and machine animations.
7. Repeat with a second player on a larger raft, then save and reload.

Existing saves do not need to be reset for these collision/model changes.
The enlarged islands from 0.11.40 still appear only in newly generated chunks.
Automated checks cannot prove there are no remaining bugs. Live client visuals,
real network delay, multiplayer play and third-party mods need local testing.

## Asset regeneration

Furniture collision profiles live alongside visual geometry in
`data/raft_ocean/furniture.json`. `tools/furniture_collisions.py` applies the
profiles, and the furniture generator invokes it automatically. Refined
machine meshes are produced by `tools/refine_station_models.py` using existing
material textures; the station mesh generator reapplies those refinements.
