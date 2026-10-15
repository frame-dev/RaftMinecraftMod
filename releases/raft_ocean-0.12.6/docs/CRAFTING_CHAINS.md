# Salvage crafting chains — 0.5.2

Six original components turn collected materials into usable building supplies.
All recipes use the normal Minecraft recipe book and the moving raft workbench.
No new world is required. Existing placed pieces, storage, and items keep working.
New recipe costs apply to future crafting; there are no research locks yet.

## Get started

Make sticks from two vertical planks. In your inventory grid, craft the raft
workbench with `PP / PS` (three planks and a stick), and the wooden salvage hook
with `PP / -S` (two planks and a stick). Place the workbench on your raft.
Collect leaves, scrap, string, and iron nuggets from passing salvage.

## Components

These components fit in the inventory's 2x2 grid except woven mesh, which needs a workbench:

| Ingredients | Result | Used for |
| --- | --- | --- |
| 1 drift leaf, shapeless | 2 plant fiber | Cordage |
| 2 plant fiber in separate slots, shapeless | 1 braided cordage | Binding parts and weaving mesh |
| 1 plank + 1 stick, shapeless | 2 timber braces | Structures, nets, storage |
| 1 salvaged scrap, shapeless | 4 scrap fasteners | Doors, storage, metal hook heads |
| 2 fasteners + 2 iron nuggets, shapeless | 1 metal hook head | Reinforced hook |
| 9 bindings in a 3x3 square | 1 woven mesh | Nets, roofs, barrels |

A **binding** can be braided cordage or vanilla string, mixed in any combination.
One leaf makes enough fiber for one cordage. Mesh therefore costs nine leaves
when made entirely from plant material. Components are consumed, not reusable tools.
Iron nuggets come from barrel/crate loot; a smelter is not needed for this chain.

## Building layouts

Read rows from top to bottom. `-` means an empty slot. P = any planks, S = stick,
B = timber brace, C = binding, F = scrap fastener, M = woven mesh.
Three-wide or three-tall layouts require the raft workbench.

| Item | Rows | Output |
| --- | --- | --- |
| Foundation | PPP / -S- | 2 |
| Building hammer | PP- / CS- / -S- | 1 |
| Floor | PPP / -B- | 2 |
| Wall | PPP / BCB | 2 |
| Door | PP / BF / PP | 1 |
| Stairs | P-- / PP- / BBP | 1 |
| Ladder | S-S / SCS / S-S | 2 |
| Roof | PPP / CMC | 2 |
| Tall pillar | B / B / C | 1 |
| Short pillar | B / C | 2 |
| Railing | SSS / BCB | 2 |
| Half wall | PPP / -C- | 2 |
| Collection net | BCB / CMC / BCB | 1 |
| Raft chest, directly from materials | PPP / BFB / PPP | 1 |
| Raft barrel, directly from materials | BFB / PMP / BCB | 1 |

The chest and barrel each hold 27 slots. You can also craft and mount the
corresponding vanilla blocks or convert them with the existing one-item recipes.
Other tool stations retain their vanilla crafting and conversion recipes.

A net costs four braces, four bindings, and one mesh. Starting with raw materials,
that is two planks, two sticks, and eight leaves (or eight string, or a mixture).
The reinforced hook uses **one wooden hook + one metal hook head + one binding**,
shapeless, in the player inventory. A used wooden hook can be upgraded; the result
is a fresh reinforced hook with its normal 384 durability. Ingredient names and
enchantments are not transferred by this ordinary crafting recipe.

## Recipe book and assets

Acquiring planks or relevant salvage/components learns the new recipes. Turn off
the recipe book's craftable-only filter to view missing ingredients. Recipe groups
keep the two chest/barrel alternatives together. The six icons are original
procedural pixel art in the Ocean Salvage Creative tab and Creative search.

Resource generation is reproducible with:

```powershell
python tools/generate_building_assets.py
python tools/generate_salvage_assets.py
python tools/generate_crafting_assets.py
```

Python/Pillow is needed only to regenerate assets; generated resources are saved
with the project, and normal builds do not require Python. Recipe JSON files are
under `src/main/resources/data/raft_ocean/recipe/`; the shared ingredient tag is
`data/raft_ocean/tags/item/bindings.json`.

## Build and test

Close old clients/servers and run `./gradlew.bat build`, then `./gradlew.bat runClient`.
Verify Raft Ocean 0.6.0. Install the same build on all connected clients and servers.

- [ ] Make the workbench and basic hook from starting planks/sticks.
- [ ] Collect a leaf and scrap; craft fiber, cordage, braces, and fasteners.
      Expected: counts match the component table and ingredients are consumed once.
- [ ] Open the recipe book, show uncraftable recipes, and inspect the new entries.
- [ ] Make mesh with mixed cordage/string, then a net. Expected: it places normally
      and collects passing debris into its shared inventory.
- [ ] Craft a pillar and floor on the drifting raft; build a second deck.
- [ ] Craft and open a chest/barrel from materials; save/reload with cargo.
- [ ] Make a metal hook head, upgrade a used wooden hook, and cast/reel.
      Expected: 384 durability initially, longer tether, faster retrieval.
- [ ] Shift-craft and close/reopen the moving workbench. Expected: no lost or
      duplicated ingredients. Repeat shared-storage access with a second client.

Limits: recipes are available without research; costs still need gameplay balance.
Survival processing machines and research tiers remain milestones 6 and 10.

The current survival stations and moving beds are covered in [milestone 6](MILESTONE_6.md).

## Navigation additions (0.7.1)

See [sailing recipes and controls](MILESTONE_7.md) for all exact patterns.
The steering wheel uses three timber braces, one binding (cordage or string),
and one scrap fastener. The wind streamer uses one timber brace, one stick,
one binding, and one woven mesh. Both are crafted in a 3x3 crafting station.
