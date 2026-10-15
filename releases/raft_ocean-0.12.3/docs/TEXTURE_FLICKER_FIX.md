# Texture flicker repair — 0.11.19

## Polygon polish in 0.11.19

The repair now handles convex coplanar polygons, including angled barrel and
tank faces missed by the earlier rectangle pass. It trimmed 44 additional
overlapping opaque faces across seven meshes. Visible fragments retain the
original triangle UV interpolation, winding and normals; opposite-facing
surfaces such as two-sided leaves remain intact. Model envelopes match 0.11.18
exactly, and a second repair pass makes no changes.

Native asset checks now validate all 60 polygon meshes and 11,519 quads for
nonempty material groups, vertex/UV dimensions, finite values and unit normals.
Build and dedicated-server suites passed; packaged model audits also passed.
Log: `build/polygon-polish-runtime.log`.

Test barrel lids, water/fuel tanks and the biorefiner from shallow angles while
turning and sailing. Check that their texture patches remain stable and their
fills stay aligned. Visual confirmation is still pending; intersecting curved
surfaces in different planes and external resource-pack models remain outside
the coplanar clipping check.

## Original repair in 0.11.18

The asset audit found overlapping faces occupying the same plane in generated
cuboid models and axis-aligned portions of custom machine meshes. Drawing both
surfaces can produce z-fighting: patches alternate as the camera moves.
Some generated face UVs also extended outside their 0–16 atlas sprite area,
which could sample nearby textures instead of the intended material.

## Changes

- Earlier faces are trimmed behind later opaque coplanar faces. Visible
  rectangles retain interpolated UVs and their original material. This avoids
  pushing thousands of surfaces outward or changing collision geometry.
- Atlas UV ranges are translated into their sprite, or rescaled if the span is
  larger than the sprite. Face direction and cropping follow the pinned native
  Minecraft 26.3 FaceBakery orientation.
- The same rectangle repair handles the flat portions of 15 polygon machine
  meshes, including purification, smelting, generators, pipes and engine parts.
  Curved/rotated geometry and transparent texture layering are retained.
- Reinforcement edging sits just outside the wood sides and its four bands
  no longer overlap at their ends. No colliders are added or changed.
- The complete model generator runs the surface repair as its final stage.
  Native model checks now reject nonfinite or out-of-range atlas UVs.

The repair corrected 10,052 face UV ranges and clipped 7,398 overlapping cuboid
faces plus 79 rectangular machine-mesh faces. Model envelopes were compared
against 0.11.17 and preserved. Applying the repair again makes no changes.
The audit covers opaque coplanar rectangles; arbitrary overlapping curved
polygons and external resource-pack models are outside that check.

## Install and test

Install `build/libs/raft_ocean-0.11.19.jar` and restart Minecraft. For development
runs, close this project's running client before rebuilding with Java 25, then
launch it again. Existing saves remain compatible; no world reset is needed.
The gameplay/network protocol remains `raft-movement-11.17`.

1. Look at the deck, reinforcement edges, walls, doors, and machine frames from
   shallow angles while moving the camera and sailing. Patches should remain stable.
2. Inspect inventory icons, cups, planters and working machines at several angles.
   Original shape, fills, animation positions and collision should remain intact.
3. If flickering remains, note the exact object and surface and provide a fresh
   screenshot. Include whether shaders or external resource packs are enabled.
   Those details help distinguish remaining geometry from shader/depth effects.

Developer audit: `python tools/verify_model_surfaces.py build/libs/raft_ocean-0.11.18.jar build/libs/raft_ocean-0.11.19.jar`.
Repair stage: `tools/model_surface_safety.py`. Build, server suites, native model/UV checks and four packaged audits passed.
The packaged surface audit verified 523 models and 60 meshes against 0.11.17
with unchanged envelopes. Log: `build/texture-flicker-runtime.log`.
In-game flicker confirmation and real multiplayer performance remain pending.
