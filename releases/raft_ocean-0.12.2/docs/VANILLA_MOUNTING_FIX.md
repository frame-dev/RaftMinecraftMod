# Vanilla workstation conversion — 0.11.24

Superseded by [0.11.25 research recipe discovery](RESEARCH_RECIPE_DISCOVERY.md):
conversions again require research and stay hidden until learned.

The chest conversion JSON was present and matched correctly, but the shared
research gate discarded its result until the player learned Sealed storage.
The previous audit accepted that lock. Conversion of an already crafted vanilla
workstation is now available before research; crafting locked items directly
from materials still requires the corresponding project.

Put one vanilla chest into any slot of either the inventory 2x2 grid or a 3x3
crafting table: the result is one raft chest. This also works in the regular and
wide raft workbenches. A stack of chests consumes exactly one per craft.

The same rule covers barrels, crafting tables, stonecutters, smithing tables,
grindstones, looms, cartography tables, anvils, enchanting tables, ender chests and
bookshelves. This is a conversion of the matching vanilla block into its raft
counterpart. Research checks for other recipes remain active.

## Verification

- Build and the complete native server suite passed.
- All 1,389 loaded crafting recipes, including 123 mod recipes, were audited again.
- The combined crafting suite passed 256,579 assertions and the research suite
  passed 267 assertions.
- New regressions test all 12 conversions before research in both grids,
  including right-click on a two-item stack and shift-click on the remaining item.
- A separate native materials-based chest fixture still produces no output
  before research.
- Updated per-recipe research status is recorded in
  [crafting-audit-0.11.24.csv](crafting-audit-0.11.24.csv).

## Local test

Restart/rebuild using 0.11.24 on client and server. No new world is needed.
Place a vanilla chest into your inventory grid: expect one raft chest immediately.
Repeat with a barrel, then try the same in a raft workbench. Shift-click should
consume one input and return one output. JEI and the recipe book should show the
conversion after restart; client recipe transfer still needs local confirmation.
