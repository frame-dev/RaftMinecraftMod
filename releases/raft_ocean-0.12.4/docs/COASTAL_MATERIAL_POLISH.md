# Coastal material polish — 0.10.10

A shared original material kit improves models across the registered item and
block catalog, using warm wood grain, knots and seams, brushed metal, subdued
paint chips, stitched woven cloth, ceramic edges, brick mortar, stone cracks,
soil grains and leaf veins. Deck, damaged deck, bark, foliage, crates and salvage
surfaces get matching materials; the salvage barrel has metal hoops and rivets.
Shared cup, food and paper materials also gain fine surface detail.

Foundations gain plank seams and fasteners. Freestanding walls, doors, railings,
planters and chest parts now have rear joinery and hardware. The existing
branched tree and function-specific machine silhouettes remain. This is a
shared material and detail pass, not a replacement of every object's geometry.

Generate with `python tools/generate_all_models.py`; the final step invokes
`tools/generate_coastal_materials.py` so rerunning the model pipeline retains the
polish. All material patterns are deterministic and original. Journal cover art,
creature facial markings, readable symbols and UI textures are preserved.
The single-atlas journal fix is retained.

Validation: build and packaged model audits, native item GUI fit and machine
fill/mount alignment. Textured software previews: build/coastal-materials-preview.png,
build/handcrafted-models-preview.png and build/all-items-preview.png. Game lighting,
resource packs and actual moving rendering still require a local client test.

Restart after installing build/libs/raft_ocean-0.10.10.jar. For a development run,
close Minecraft, rebuild normally and relaunch; an isolated verification build
does not replace the running client's resources. Inspect deck surfaces, both
sides of walls/chests/planters, station fills, held tools and all six journals.
Look for readable materials without missing textures or animation misalignment.

Physics, collision envelopes, save compatibility, recipes, research and the
open-ended story are unchanged. Earlier movement reports remain pending retest.
