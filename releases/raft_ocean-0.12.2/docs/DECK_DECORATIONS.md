# Deck decorations — 0.11.35

Update: [0.11.36 tabletop placement](TABLETOP_DECORATIONS.md) removes the coffee table's fixed books and cup, keeping its runner, so you can place your own ornaments.

Install `build/libs/raft_ocean-0.11.35.jar` on every client and the server. Targets remain Minecraft 26.3, NeoForge 26.3.0.48-beta and Java 25. Protocol is 11.35; addon API remains 2.

## Eight new decorations

| Decoration | Footprint | Details |
| --- | --- | --- |
| Driftwood Deck Bench | 2 × 1 | Planked seat, open back, wooden supports and brass armrests |
| Cabin Coffee Table | 2 × 1 | Low wooden table with runner, books and ceramic cup |
| Coastal Flower Box | 2 × 1 | Planked planter with seven flowers and contrasting petals |
| Navigator Book Stack | 1 × 1 | Four colorful bound books and a brass ribbon |
| Reef Ceramic Vase | 1 × 1 | Contrasting ceramic bands and a flower arrangement |
| Decorative Chart Stand | 1 × 1 | Wooden stand, stepped sloping chart and brass retaining rail |
| Nautical Room Divider | 2 × 1, two levels tall | Two framed fabric panels with original maritime motifs |
| Striped Canvas Canopy | 2 × 2, three levels tall | Four wooden posts, striped raised roof and contrasting valance |

All have custom models, fitted inventory icons, English/German names, recipe-book unlocks and construction creative-tab entries. Existing furniture keeps its geometry and recipes.

## Crafting

These recipes are shapeless. Put repeated ingredients in separate occupied slots.

| Decoration | Ingredients | Grid |
| --- | --- | --- |
| Deck bench | Three oak planks, two sticks, cordage | 3 × 3 |
| Coffee table | Two oak planks, stick, paper, salvage scrap | 3 × 3 |
| Flower box | Two oak planks, dirt, drift leaf, poppy | 3 × 3 |
| Book stack | Three paper, cordage | 2 × 2 |
| Ceramic vase | Three clay balls, poppy, drift leaf | 3 × 3 |
| Chart stand | Oak plank, paper, two sticks, scrap fasteners | 3 × 3 |
| Room divider | Three sticks, two canvas rolls, cordage | 3 × 3 |
| Canvas canopy | Four timber braces, two canvas rolls, two cordage | 3 × 3 |

## Placement and limits

Use the held decoration item and the existing raft placement/rotation controls. Ghost previews, picking, persistence and rendering share the same geometry catalog. Wider pieces require support under every bottom tile; a single item places the whole furnishing. Hammer removal frees the full footprint, and support damage removes unsupported objects as one piece.

The canopy uses separate post and roof colliders so players can walk through its center. Other pieces use continuous collision bodies to prevent catching feet on small details. As with the existing building system, the canopy reserves its entire footprint and height: another buildable cannot be placed underneath it.

These are decorations: bench sitting, flower growth, vase storage and chart navigation are not included. Books, cups and flowers shown in models are fixed details. The story remains open-ended; no progression ending is added.

Generate these assets with `python tools/generate_furniture.py`, also called by the full model generator. The same catalog now contains all 18 furnishings.

## Playtest

- Craft each decoration; only the book stack fits the inventory 2 × 2 grid.
- Place wider pieces in all four orientations and compare ghosts with final placement.
- Walk around benches and through the canopy; its center should have clear headroom.
- Save/reload and remove pieces with the hammer; all reserved cells should be freed.
- Inspect models and inventory icons on a moving raft, including multiplayer.

Build and native bootstrap checks passed, including 2136 construction checks, rotated footprints, support loss, removal and canopy headroom. Minecraft accepted the models and GUI transforms. Surface, bilingual-label, research and all 1417 loaded crafting recipe audits passed. Live appearance and multiplayer confirmation remain pending player testing.
