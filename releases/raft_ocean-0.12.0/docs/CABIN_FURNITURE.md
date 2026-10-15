# Cabin furniture — 0.11.34

Update: [0.11.36 tabletop placement](TABLETOP_DECORATIONS.md) removes the dining table's fixed tableware, keeping its runner, so you can place your own ornaments.

Install `build/libs/raft_ocean-0.11.34.jar` on every client and the server. Minecraft 26.3, NeoForge 26.3.0.48-beta and Java 25 remain the targets. Protocol is 11.34; addon API remains 2.

## New furnishings

| Item | Footprint | Details |
| --- | --- | --- |
| Coastal Cabin Bed | 1 × 2 | Turquoise blanket, striped bedding, pillow, wooden frame and brass trim |
| Seafarer Double Bed | 2 × 2 | Coral blanket, two pillows and a broad wooden headboard |
| Brass Floor Lamp | 1 × 1, two levels tall | Brass stem, shaded light and turquoise cap |
| Shaded Table Lamp | 1 × 1 | Short brass stem, fabric shade and coral cap |
| Cushioned Wooden Chair | 1 × 1 | Open wooden back and turquoise seat |
| Coastal Dining Table | 2 × 1 | Planked tabletop, fabric runner and ceramic tableware |
| Ocean Lounge Sofa | 2 × 1 | Blue cushions, turquoise back and contrasting throw pillows |
| Woven Coastal Rug | 2 × 2 | Colored stripes, contrasting border and fringe |
| Decorated Cabin Shelf | 2 × 1 | Wooden shelves, colorful books and ceramic jars |
| Potted Palm | 1 × 1, two levels tall | Angular fronds, segmented trunk and colored ceramic planter |

All pieces have original custom models and English/German names. They are in the construction creative tab and have recipe-book crafting recipes. The original compact raft bed keeps its saved dimensions.

## Building and use

Place furniture using its held item. Placement follows the existing raft rotation controls and ghost preview. Larger pieces reserve every covered cell; provide a foundation or supported floor beneath every bottom cell. One item creates the whole furnishing, one saved owner controls it, and hammer removal frees the whole footprint. Losing a required support removes the unsupported object through the normal raft damage path.

Beds support the existing moving home/respawn and cooperative five-second nighttime rest. Click either bed's visible geometry to use it. Respawn checks full player clearance above the bed; a removed or blocked bed uses the existing fallback. No lying-down animation is added.

Lamps are always lit and illuminate nearby raft-rendered surfaces while drifting or turning. This is visual light on the raft: it does not change terrain light, mob spawning or crop growth. No temporary light blocks are written into the world. Furniture seats, shelves, tableware and the palm are decorative; chairs/sofas do not add sitting interactions and the palm does not produce timber.

Beds and furniture use continuous collision bodies to prevent catching feet between small model parts. Rugs add no physical step. Leave walking space beside large furniture.

## Crafting

Recipes are shapeless. Repeated ingredients require separate occupied grid slots.

| Item | Ingredients | Grid |
| --- | --- | --- |
| Cabin bed | Compact raft bed, timber brace, canvas roll | 2 × 2 |
| Double bed | Two cabin beds, timber brace | 2 × 2 |
| Floor lamp | Torch, salvage scrap, two sticks, canvas roll | 3 × 3 |
| Table lamp | Torch, salvage scrap, stick, canvas roll | 2 × 2 |
| Wooden chair | Two oak planks, stick, canvas roll | 2 × 2 |
| Dining table | Four oak planks, timber brace, cordage | 3 × 3 |
| Sofa | Three oak planks, two canvas rolls, cordage | 3 × 3 |
| Woven rug | Two drift leaves, canvas roll, cordage | 2 × 2 |
| Decorative shelf | Three oak planks, paper, cordage | 3 × 3 |
| Potted palm | Sand, drift leaf, stick, salvage scrap | 2 × 2 |

The compact bed remains subject to existing progression. Buying or crafting decorations does not unlock research projects.

## Asset maintenance

`python tools/generate_furniture.py` generates the shared furniture geometry catalog, custom models, item transforms, recipes and bilingual names. Existing original material textures are reused. The complete model generator runs this step at the end so the detailed furniture designs survive regeneration. Native collision and picking read the same geometry catalog shipped in the jar.

## Playtest checklist

Build and native bootstrap checks passed, including rotated ownership, overlap refusal, complete support requirements, support damage, whole-piece removal and both new bed respawns. Ocean World checks passed. Minecraft accepted the models and GUI transforms; packaged surfaces, bilingual labels and all loaded crafting recipes were audited.

- Place each larger furnishing in all four orientations. Ghost and final geometry should agree; overlap and missing support should be rejected.
- Remove a larger piece with the hammer, then place another object in its former cells.
- Walk around beds, sofa and chair; rugs should not lift or catch the player.
- Use each new bed, move/rotate the raft, and check death/respawn on the moving home.
- At night, compare raft lighting near and far from a lamp while moving.
- Craft every new item in the indicated grid and inspect its inventory icon.

Live client appearance, lamp lighting and two-player bed confirmation remain part of the player playtest.
