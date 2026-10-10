# Handmade coastal models — 0.11.14

Original designs inspired by the handmade survival equipment seen in Raft's
screenshots and official press kit: https://www.raft-game.com/presskit.html.
No game textures, models, logos or text were imported.

## Changes

- Timber board geometry for walls, doors, floors, roofs, stairs, chests, planters
  and wooden bench tops, with small visual seams and contrasting boards.
- Square timber pillars and wooden bench legs with rope bindings.
- A painted angular engine casing with vents, fasteners and a gauge; its mounted
  mesh matches that style. Existing fuel panel and exhaust retain their positions.
- Open red-rimmed grill grate, with a dark inset tray beneath the cooking surface.
- Purifier timber corner posts, dark rim and bronze fittings, with an open reservoir.
- Warmer wood, brighter coastal paint, chipped panels and restrained material wear
  shared across equipment, held items, deck, scenery and salvage.
- Existing articulated part envelopes, pivots, machine fill anchors and gameplay
  collision boxes retained. Decoration moves with its original part.
- Full model generator now delegates newer archery and battery assets to their
  generators rather than failing on arrow_shaft. Fixed a missing bronze alias
  in the base model texture dictionary. Preview tool accepts custom block selections.

## Verification

Build and native bootstrap passed in build/handmade-models-final.log.
397 models / 23679 elements accepted by Minecraft's actual model parser;
121 inventory silhouettes fit their slots; 4845 mounted geometry/fill checks pass.
1210 native movement checks and all existing runtime suites pass.
All four packaged model, localization, polish and diving asset audits pass.
Coverage: 112 original 3D item definitions, 276 custom block definitions,
7 entity renderers and 505 English/German localization keys.

## Test locally

Fully restart the development client, or replace the old jar in a matching
instance with build/libs/raft_ocean-0.11.14.jar. Both co-op players need the update.
Inspect the purifier, grill, engine, chest, planter, benches, pillars and building
pieces at all four placement rotations. Open chests; run the cooking and water
machines; turn the engine and sail; compare their ghosts and inventory icons.
Walk around stations while anchored and while steering the raft.
Expected: readable materials, fitted inventory icons, correctly aligned fills and
articulated parts, with the previous walking fix retained. In-game lighting,
connected co-op latency and large-raft frame rate still need human testing.

Preview: docs/previews/handmade-models-0.11.14.png. The preview is an offline model
render, not a screenshot of Minecraft's lighting.
