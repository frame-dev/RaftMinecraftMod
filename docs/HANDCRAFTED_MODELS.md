# Handmade model detail — 0.10.8

The planter tree now has a tapered jointed trunk, three branches, buttress roots
and layered pointed fronds. Bark grooves and green leaf veins replace the previous
round crown sections. Growth scales the whole tree, rooted at the planter soil;
wind sways it around that root.

The purifier uses an open rectangular metal reservoir with wooden framing, a teal
rim, gauge and copper plumbing. The smelter has an octagonal brick body, square
open chimney, framed fire opening, vents and brass hardware. The rain collector
has supports and metal feet; the cooking pot has side handles and a control badge.
Engine geometry includes fittings and a gauge. The preview sheet shows station
mesh resources; the engine image is its body part, not its complete assembled model.

Across the model families, building panels have plank seams, posts and pegs;
planters and chests have joinery; sails and beds have seams; grills have grate bars;
benches have instrument panels; tools have wrapped grips and material fasteners.
Round shapes remain for objects where they serve a purpose, such as vessels,
barrels and rotating parts. All geometry and textures are original.

Reference: [Raft official listing](https://store.steampowered.com/app/648800/Raft/)
and [crafting station reference images](https://techraptor.net/gaming/guides/raft-cooking-guide).
No game assets, names, lore or sounds were copied.

## Validation and local test

The build validates native model parsing, item GUI fit, material/resource coverage,
normalized polygon bounds and machine fill/mount alignment. The textured software
preview is build/handcrafted-models-preview.png. GPU rendering remains a local test.

Restart the client to load changed renderer classes. Update clients and server
with build/libs/raft_ocean-0.10.8.jar when using installed mods. Check:

- Plant a tree; inspect trunk, roots and pointed fronds through growth and wind.
- Place machines facing different directions and on an upper floor; check liquid,
  fire, handles and moving parts stay aligned with their bodies.
- Inspect wall corners, planters, chests, tools and inventory icons for added detail.

This update changes appearance; existing saves, recipes, research, story,
physics and collision shapes remain compatible. The story is still open-ended.
Conservative collision envelopes and earlier movement reports need local retesting.

If development Minecraft is running, Windows locks normal generated outputs.
Close it for a normal development rebuild. A separate verification/package build is:

```powershell
.\gradlew.bat -I tools/isolated-visual-build.gradle build --no-parallel
python tools/verify_all_models.py build/visual-art-verification/libs/raft_ocean-0.10.8.jar
python tools/verify_custom_models.py build/visual-art-verification/libs/raft_ocean-0.10.8.jar
```

This does not refresh the files used by an already running development client.
