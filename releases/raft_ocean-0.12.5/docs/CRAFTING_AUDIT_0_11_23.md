# Crafting audit — 0.11.23

Follow-up: [0.11.24 makes vanilla workstation conversions research-free](VANILLA_MOUNTING_FIX.md).
The original audit below accepted those conversions being research-locked.

All 1,389 loaded crafting recipe IDs passed native server tests: 123 Raft Ocean
recipes and 1,266 Minecraft/NeoForge recipes. This covers the player's 2x2
inventory grid where the recipe fits, the regular raft workbench, the wide raft
workbench and the vanilla crafting table. Of the mod recipes, 50 fit 2x2 and 73
require 3x3. Across all namespaces, 607 recipes have a 2x2 fixture.

## Actual recipe fixes

- The building hammer previously matched the mirrored wooden hoe recipe. It now
  needs one extra binding so both tools craft reliably.
- The paddle previously intercepted the shapeless timber-brace recipe when its
  plank and stick were diagonal. It also now needs one binding; braces remain
  shapeless and work in either grid.
- Both recipe generators produce the corrected data. Existing items keep working.

P = any plank, S = stick, C = braided cordage or string, `-` = empty slot:

| Result | Rows | Grid |
| --- | --- | --- |
| Building hammer | `PP- / CS- / -S-` | 3x3 |
| Timber paddle | `CS / P-` | 2x2 or 3x3 |
| 2 timber braces | 1 plank + 1 stick, any two separate slots | 2x2 or 3x3 |
| 8 crumb bait | 1 bread, any one slot | 2x2 or 3x3 |

## Coverage

The extended audit completed 256,779 assertions, including 11,662 placement cases
and 15,602 ingredient-alternative cases. The combined crafting suite completed
256,827 assertions. Tests check native recipe selection and conflicting outputs,
shaped offsets and mirrors, shapeless rearrangements, enabled tag members,
left/right/shift result extraction, output components and quantities, ingredient
consumption and crafting remainders. Research is enabled: every recipe is checked
with all research learned and with no research learned. Locked outputs cannot
consume ingredients. Every declared mod crafting recipe must actually load.

The 64 dynamic/special fixtures include colored equipment, banner duplication,
shield decoration, written books, fireworks, repaired tools, decorated pots,
imbued items and map expansion. The tests respect Minecraft's bundle secondary
click and map post-processing behavior. Dynamic fixtures use representative
component inputs; arbitrary enchantment, dye-mixture and custom datapack
combinations are not exhaustively enumerated.

The full per-recipe results, including 2x2 eligibility and research-lock status,
are in [crafting-audit-0.11.23.csv](crafting-audit-0.11.23.csv).
The 28 other mod recipe files are cooking/smelting recipes and are outside this
2x2/3x3 audit. Dedicated survival and station suites also passed.

## Local checks

Restart with 0.11.23 on client and server, or rebuild the development client.
No new world is required. JEI reads the updated recipes after restart.

1. Craft crumb bait from bread in the inventory and raft table: expect 8 bait per
   bread, including right-click and shift-click.
2. Place a plank and stick in different arrangements: expect 2 timber braces.
3. Make both the paddle and hammer using the new layouts above. Craft a vanilla
   wooden hoe too: it should still produce a hoe.
4. Check several learned advanced recipes in both raft workbenches. Before
   research, those same locked recipes must give no output or ingredient loss.

Automated tests use server menus. JEI recipe transfer and a real client's recipe
book still need local confirmation, especially with additional mods/datapacks.

## Reproduce

```powershell
./gradlew.bat -I tools/isolated-visual-build.gradle build runBootstrapSmoke --no-parallel
python tools/verify_crafting_audit.py build/visual-art-verification/libs/raft_ocean-0.11.23.jar
```

Native reports are written to `build/bootstrap-smoke/crafting-audit.csv` and
`build/bootstrap-smoke/crafting-audit-summary.txt`. The audit runs in the isolated
test world and does not change the user's survival world or live client outputs.
