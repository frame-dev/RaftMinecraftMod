# Coastal building upgrade — 0.9.6

Update client and server together. The development project and built jar use
Minecraft 26.3, NeoForge 26.3.0.48-beta and Java 25. This continues milestone 9.

## Tree planter

Craft a Tree Planter using `BCB / PLP / BBB`: B timber brace, C binding,
P plank and L planting mix. Place it on a foundation or upper floor with four
clear reserved levels. The planter has mass 8, so a single foundation cannot
support it alone. Right-click to open its processing screen.

Insert one sapling, one planting mix and one fresh-water cup. You can also pour
fresh water directly into its reservoir by right-clicking with a filled cup.
Oak, birch, spruce, jungle, acacia, dark oak, cherry, pale oak and mangrove are
supported. Growth needs light and an uncovered position. At the default
processing speed, a tree takes three minutes of loaded simulation. Its visible
canopy grows in stages; completion provides six matching logs and two saplings.
Take these from the output slots to harvest. Inserted cups return to the empty
cup output; direct pouring returns the cup to your hand.

Progress, water and inventory survive saving. The original stylized tree model
is shared by all species; it does not place vanilla tree blocks on the moving
raft. The planter reserves headroom but only its pot has collision.

## Nets and walls

Hold a collection net and right-click near a foundation's outer edge. The net
extends outward at foundation height and has a complete walkable surface.
Another net can extend from its edge. Existing elevated nets remain compatible;
sneak-placement retains the previous mounting behavior. Foundation placement
cannot overwrite a net. Nets add mass, not buoyancy; foundations provide their
support and flotation. They retain their shared cargo and collection behavior.

Straight walls, half walls and railings snap to the edge nearest the clicked
point. Sneak to choose their facing from your viewing direction instead. Corner
Walls join two perpendicular edges in one cell. Curved Walls form a stepped
quarter-circle in one cell, keeping the inner space clear. Their facing follows
your viewing direction. Both have recipes and recipe-book unlocks:

- Corner Wall: `PP / BC` (planks, timber brace, binding).
- Curved Wall: `PBP / -C-` (planks, timber brace, binding).

Walls, corners, curves and doors now mount flush on thin upper floors. Each
cell still holds one piece; use a corner piece when two wall edges share a cell.
Placement refuses terrain, players, occupied cells, missing supports or excess
mass before consuming an item. Use the building hammer to remove existing
pieces before changing their type.

## Anchors and sails

Lowering a raft's anchor pauses horizontal motion of debris belonging to its
stream and pauses new spawning for that raft. Floating height, collection,
hooks and bounded debris lifetimes still work. Raising the anchor resumes the
original drift without teleporting debris or turning old debris toward you.
Another raft's stream remains independent.

Deploy a sail and use its left/right trim buttons in 15-degree steps. Close and
broad reaches now produce lateral thrust, allowing you to reduce or oppose
current drift. Watch the speed and heading HUD while adjusting trim. With the
default current and typical starting wind, roughly +45 degrees is a useful
counter-steering starting point on a south-facing hull (heading 0). Wind, mass, hull heading
and multiple sails change the best setting. Direct headwind stalls the sail;
tack or paddle when you cannot sail straight toward your destination. Sail trim
changes travel direction; the steering wheel turns the hull.

## Islands and visuals

The ten ordinary templates span 17–81 blocks: Shell Sandbar, Tidal Grove,
Flower Meadow, Twin Cove, Stone Rise, Broadleaf Island, Crescent Reef, Split
Grove, Longshore and Mossy Arch. Coasts now include open bays, crescents, linked
groves and narrow sandbars, with uneven inland heights. Story islands gain
irregular outer shores while their buildings and puzzle locations stay fixed.
New shapes appear in unexplored chunks; existing terrain is preserved.

All 34 building/station types have original 64px materials, including station
tops, soil, crafting grids, charts, metalwork, storage fittings and cloth.
Salvage, tree materials and the bunker seal also have detailed surfaces. Existing
original creature/deck/cloth textures are retained. All 29 original item icons
have larger pixel surfaces and edge highlights. All 117 mod texture surfaces are
64×64. This is a pixel-art upgrade;
the geometry remains Minecraft-style blocks and articulated parts.

Generators: run `generate_island_assets.py` and
`generate_ordinary_islands.py` for terrain; run `generate_visual_assets.py`
followed by `generate_upgrade_assets.py` for the final materials. The upgrade
generator also generates building models, recipes and tree recipes.

## Test checklist

- Grow a tree, reload halfway through, then harvest six logs and two saplings.
  Check every poured/inserted cup returns. Check a roof pauses growth.
- Add edge nets, walk on them while sailing, collect debris and reload cargo.
  Check an overlapping foundation is refused without losing an item.
- Build adjoining straight walls, then corner/curved pieces on both deck and
  upper floors. Walk beside them while translating and turning the raft.
- Lower the anchor while debris is approaching: horizontal drift stops. Raise
  it: debris resumes the same path. Hook collection should work in both states.
- Trim the deployed sail across the wind and watch the actual travel heading
  change. Check folded sails, engines and anchoring retain their normal behavior.
- Explore new chunks, or locate `raft_ocean:crescent_reef`,
  `raft_ocean:split_grove`, `raft_ocean:longshore` and `raft_ocean:mossy_arch`.
  Inspect station tops, materials, icons and growing trees in-game.

Build, standalone geometry checks, native server processing/placement checks and
fresh Ocean World structure checks passed. Live visual quality, two-client
movement and the previously reported intermittent trapping/snap-back still need
user testing before milestone 10.
