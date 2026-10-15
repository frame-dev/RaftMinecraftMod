# Complete mod model coverage — 0.10.3

Coverage is checked against an export of Minecraft's live registries, not a
manually guessed item list: **87 mod items, 244 mod blocks and all 7 entity types**.
The block count includes internal materials and articulated-part model blocks.
Every registered mod item resolves to explicit 3D geometry; every registered
mod block resolves to custom element geometry. There are no flat generated-item
models or cube-template fallback models for the registered mod content.

Tools, cups, materials, meals, components, receivers, antennas, keys and journals
now have shaped item models, including first/third-person, inventory and dropped
item transforms. Cups are open vessels with handles; filled cups show water.
Hooks, hammer, spear and paddle have distinct forms. Materials include coils,
mesh strands, rolled cloth, riveted plates and fasteners. Meals use bowls,
garnishes and skewers; original journal cover art appears on dimensional books.
These replace presentation only; recipes, cup behavior and inventories retain
their existing logic.

Creatures now use resource-driven textured polygon meshes rather than scaled
cube skins: rounded bodies, pointed fins/beaks/teeth and tapered left/right
wings. Existing bite, jaw, tail, peck, flight, walking and grazing animations
retain their poses. Floating salvage has shaped logs, barrels, bottles and
beveled planks/crates. The thrown hook has curved tubular geometry; its rope
uses continuous, oriented cylinder segments. Meshes use entity lighting and
support glowing outlines. Resource reloads reload geometry and textures.

The raft keeps its custom moving assembly and fine block/part models. The
story hatch and internal material blocks also have explicit geometry. Original
physics, picking and hitboxes remain conservative and unchanged. All content
is original; native Minecraft content outside this mod is not replaced.

## Reproduce and verify

Run the isolated bootstrap check to export `build/model-registry.json` and
`build/model-layouts.json`, then run `python tools/generate_all_models.py` and
build. Run `python tools/verify_all_models.py` against the resulting JAR.
The all-model generator runs the fine-model generator first. Run it last if
rerunning any older asset generators, which can restore earlier presentations.

The build uses Minecraft's native parser for item and block geometry. The
packaged coverage audit checks model/texture references, exact registry sets,
all renderer registrations, polygon normals and resource-reload registration.
Entity meshes live under `assets/raft_ocean/entity_geometry` and can be edited
through resource packs. Client rendering and visual alignment still need local
testing; server checks cannot validate the graphics pipeline.

## Local test checklist

- Close Minecraft before building, then restart with 0.10.3. Update client and
  server together. Network protocol remains `raft-movement-10.2`.
- Browse every mod item in JEI/creative inventory, hold tools/cups/journals in
  both hands, drink from a filled cup and drop representative items. All should
  have 3D geometry and complete textures.
- Watch shark approaches/bites, bird flight and grazer walking/grazing. Look
  for missing surfaces, incorrect lighting, disconnected limbs or texture seams.
- Throw/reel the hook and collect each salvage type. The hook and rope should
  stay connected while moving and retrieving debris.
- Place/build raft parts, operate machines, open storage and unlock a story
  hatch. Collision and interaction behavior should stay the same.
- Press F3+T and repeat the entity/item checks. Models should remain present.
  Test a large raft and multiplayer for graphics performance and alignment.

Research remains the 20-material, 24-project system from 0.10.2. The six story
destinations and unresolved ending remain intact. No milestone progression is
assumed from successful automated checks; local results come next.
