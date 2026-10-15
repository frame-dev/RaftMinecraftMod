# Model geometry and color repair — 0.10.4

The supplied night screenshots showed pointed deck/wall corners, seams around
stairs and flat, nearly uniform machine colors. The old model generator scaled
rotated zero-depth faces before Minecraft applied their rotations. Nonuniform
scaling therefore changed where the faces met. A model can pass Minecraft's
JSON parser and still have this geometric defect.

All generated raft panels and cylinders now use closed, positive-volume strips.
They remain closed when scaled into a thin wall, broad deck, tread or machine
part. Beveled panels use nine sections instead of the old separate side and cap
planes; curved vessels use fine sections, including inner walls for open cups
and collectors. Articulated model geometry has no local element rotations.
Existing whole-part rotations, animation pivots and collision boxes are retained.
The cylindrical machine-part texture permutation also applies to every element.

Original material textures now have brighter wood, visible grain and plank seams,
brushed metal, warm brass, cream ceramics, woven linen, teal paint, navy paint,
coral paint, dark soil and contrasting stone/rubber. Machines and storage have
separate material assignments: painted engine casing and vents, brass chest
straps and latches, barrel hoops, contrasting vessel rims, painted table legs,
dark planter soil and a coral smelter with a dark fire opening. Handheld receivers
and bowls also have painted accents. Journal fronts use their full original cover.

## Verification

The normal build runs Minecraft's native model parser. The packaged coverage
audits still cover 87 registered items, 244 registered blocks, all seven entity
renderers and 22 polygon meshes. The fine-model audit additionally rejects
zero-volume elements and local rotations in every display, inventory and
articulated raft-piece model. This catches the original scaling defect.

`build/custom-models-preview.png` and `build/all-items-preview.png` are textured
software previews, not Minecraft screenshots. Lighting, texture filtering and
actual animation appearance require a local client test.

## Test locally

1. Close the old client, build/relaunch or install 0.10.4. Use the same jar on a
   connected server. Existing pieces receive the models without replacement.
2. Inspect foundations, straight/corner/curved walls and stairs from both sides
   and from below. There should be no pointed overhangs or open cap seams.
3. Inspect chest lids, barrel hoops, engine casing, collectors, smelter, planters
   and tables in daylight. Materials and accent colors should be distinct.
4. Open storage; run the engine and machines; deploy the sail and turn the helm.
   Existing animations should keep their previous pivots and motion.
5. Check cups, bowls, receivers and journals in inventory and hands, then use
   F3+T to confirm resource reload retains the models.

Curves are finely segmented native cuboid geometry; they are not mathematically
smooth surfaces. Conservative collision/placement bounds, world generation,
research and the open-ended story are unchanged. Live graphics and multiplayer
performance still need user verification.
