# Research and equipment expansion â€” 0.11.27

Research grows from 24 to 30 projects. Study each material once, then explicitly
learn the project at the raft research table. Learning consumes no additional
samples. Recipes remain hidden in the recipe book and JEI until learned.
Research is individual in multiplayer; the admin command can unlock everyone.

## Added projects

| Project | Study materials | Unlocks |
| --- | --- | --- |
| Biofuel systems | Coal, metal plate, glass | Biofuel, biorefiner, biofuel generator, fuel pipes and fuel tank |
| Water distribution | Planks, metal plate, glass | Water pipes, water tank, sprinkler and electric purifier |
| Electrical storage | Copper, copper wire, glass, metal plate | Both batteries, power cables and battery charger |
| Diving equipment | Leather, kelp, cordage | Diving hood, flippers and shoreleaf salve |
| Reinforced weapons | Iron, leather, cordage | New metal spear and reef machete; existing advanced bows, steel and broadhead arrows |
| Travel provisions | Planks, kelp, plant fiber | Three new meals below |

The utility and diving equipment already existed; this update integrates it into
progression. Basic timber spear, driftwood bow, reed/scrap arrows and crumb bait
remain accessible without these advanced projects.

## New items and recipes

These five items have original custom models and English/German labels.
Weapons use native Minecraft iron-tier durability, repair and enchantment behavior.
The spear swings slower than the machete. The machete is a combat weapon; it does
not introduce special island-clearing mechanics.

Metal spear, 3Ã—3 shaped recipe:

```text
  M
 B 
C  
```

Reef machete, 3Ã—3 shaped recipe:

```text
 MM
 BM
C  
```

`M` = metal plate, `B` = timber brace, `C` = any item in the bindings tag.
Patterns can also be mirrored in normal Minecraft crafting.

| Meal | Shapeless ingredients | Bonus |
| --- | --- | --- |
| Reef stew | Cooked salmon, baked potato, dried kelp, bowl | Resistance I |
| Orchard compote | Apple, sugar, wheat, bowl | Speed I |
| Seafarer supper | Cooked cod, carrot, dried kelp, bowl | Night Vision |

These are crafting-grid preparation recipes using existing food ingredients,
not additional cooking-pot processing recipes. Each fits a 2Ã—2 or 3Ã—3 grid.
Eating returns the bowl. Bonuses last two minutes by default; eating again
refreshes duration without increasing strength. Existing settings
`survival.preparedMealBonuses` and `survival.mealBonusTicks` apply to all seven
prepared bowl meals. Meals retain nutrition when bonuses are disabled.

## Existing worlds and installation

Existing project IDs and their saved bits are unchanged. Previously learned
projects remain learned; the six additions require learning even if every old
project was complete. Existing equipment remains usable. No new world is needed.
Install 0.11.27 on all clients and the server: the research menu has a new protocol.

For testing with cheats, `/raft research unlock` learns all 30 projects for you;
`/raft research unlock @a` does so for every online player. From the dedicated
server console use `raft research unlock @a` without the leading slash.

## Local test checklist

- Open the research table in an existing world. Verify old research remains learned
  and the six new projects appear in the paged list.
- Before learning, check that advanced recipes are hidden in JEI and blocked in
  inventory, normal-table and raft-table crafting; ingredients must remain intact.
- Study the listed materials and learn a new project. Its recipes should appear.
- Craft both weapons at a 3Ã—3 table and each meal in a 2Ã—2 grid. Check inventory
  models, held models, tooltips, food bonuses and the empty bowl after eating.
- Save/rejoin and test with a second player: research remains saved and private.

The story remains open-ended. Live JEI refresh, held-model appearance and co-op
network behavior still require your local client/server confirmation.

## Verification

Build and native isolated dedicated-server bootstrap passed. The crafting audit
covered all 1,394 loaded recipes (128 mod, 1,266 vanilla/other), with 257,837
combined crafting assertions. Of the mod recipes, 53 fit 2×2 and 75 require 3×3.
Research checks cover all 30 projects, recipe discovery, unlock command, saved
24-project progress and player serialization. Native consumption checks cover
all seven meal bonuses, bowl returns and weapon enchantment eligibility.

Packaged audits passed for registry/model coverage, surface safety, recipe data
and 542 English/German labels. Geometry previews were reviewed; actual client
lighting, held transforms, live JEI refresh and network behavior still need
in-game testing. Report: [crafting audit](crafting-audit-0.11.27.csv).
