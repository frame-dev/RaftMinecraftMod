# Material research and fine models — 0.10.2

Open the raft research table and put a material in its sample slot. Press
**Research** to consume exactly one sample and permanently study that material.
Studying it again cannot consume anything. The **Materials** pages show 20
materials; the **Recipes** pages show 24 projects. Hover each project's Learn
button for its required materials. Once they are all studied, press **Learn**:
learning is free, and studying materials alone does not unlock crafting.

Projects include nets, metal fittings/fabrication, grill, smelter, tree nursery,
canvas, sails, anchor, reinforced hook, cooking pot, wheel, engine, radio,
antenna, purifier, rain gathering, bed, storage, planter, fishing bait, shelter,
tool workshop and mapping. Original names and recipes are retained. Recipe
output tags and material tags remain adjustable by data packs. Basic plank,
rope, mesh, soil and research-table recipes remain available. Vanilla crafting
and automatic Crafters retain their existing Minecraft behavior; per-player
research restrictions apply to tagged mod outputs in manual crafting.

One studied sample serves every project that uses it. Close the menu to return
an unused sample to your inventory (or drop it if no space remains). Leaving
the table's reach, removing it or dying also uses the native return/drop path.
Research is personal, saved and copied on death. Previous six-topic saves migrate
once to corresponding projects and materials, without charging samples again.
Creative crafting and `progression.researchRequired=false` retain their bypass.

All 39 building/station models now use original fine geometry, including the
animated equipment: 24-sided vessels and barrels, round shafts and tool wheels,
beveled furniture/panels, smooth faceted curved walls, open shelves with books,
bench papers/tools and a circular steering rim. There are 138 separate part
models; existing lids, fabric, wheel, engine and machine activity animations
still follow their original pivots. Eight original wood/metal/bronze/ceramic/linen
and book-cover textures give the parts consistent materials. Foundation edges
are beveled too. Minecraft still uses polygons;
these are deliberately faceted models, with the existing conservative collision
shapes. Placement ghosts continue to show those collision/placement bounds.

Reproduction: run the bootstrap check for a geometry export, then
`python tools/generate_custom_models.py`, `python tools/generate_fine_models.py`
and `python tools/generate_research_catalog.py`, and build. The fine generator
must follow the earlier model generator. The research catalog generator must
follow the earlier research asset generator if that generator is rerun.

## Client checklist

- Close Minecraft before building, then update both client and server to 0.10.2.
- Inspect all station/building items in JEI and place them in four directions.
  Round forms, material textures and moving parts should render without missing
  textures. Test active machines, open storage, deployed sails and steering.
- Shift-click a sample into the research slot, study it once, and try again.
  Only the first study consumes one item. Close the menu with a leftover sample
  and confirm it returns. Opening the table alone never consumes materials.
- Study the ingredients listed for a project, then Learn it. The corresponding
  mod crafting output should appear; learning consumes no extra items.
- Check an existing researched character and a second multiplayer character:
  previous unlocks should migrate; personal progress should remain separate.

The build parses the models with the pinned native Minecraft model parser and
server checks exercise research transactions, crafting gates and saved data.
Actual client appearance, connected multiplayer and rendering performance are
still local tests. The six-island story remains open-ended.
