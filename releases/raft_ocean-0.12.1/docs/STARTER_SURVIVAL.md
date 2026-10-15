# Starter survival fixes — 0.11.29

The starter water purifier and first smelter now have crafting paths that do not
require metal fittings, a furnace or previously fired bricks.

## Basic water purifier

Craft in either a 2×2 or 3×3 grid, with no research required:

```text
P L
C P
```

P = any planks; L = drift leaf; C = any bindings-tag item (cordage or string).
Place it on a supported raft foundation. Pour seawater from a saltwater cup,
put planks or another accepted fuel in its fuel slot, and collect the fresh water
with an empty cup. You can also exchange cups through its menu. A cup takes
20 seconds at default processing speed. Pouring returns the empty cup.

The existing purifier is now named Advanced Water Purifier. It retains its ID,
metal-fitting recipe, saved inventory, research requirement and ten-second cup
processing. The electric purifier remains the later powered upgrade.

## Sun-drying bricks

Craft a Sun-Drying Rack without research:

```text
P S
S P
```

P = planks; S = stick. Place the rack outdoors on a raft foundation and insert
Damp Bricks into its input slot. Two clay balls and one sand craft two damp bricks.

One damp brick becomes one Dry Brick after 150 seconds (2½ minutes) of sunlight
at default processing speed. Night, rain, solid overhead blocks and raft roofs
pause drying without consuming the brick or resetting progress. The rack needs
no fuel or water. A blocked output pauses processing; progress and inventory
survive saving/rejoining. A stack dries one brick at a time. Bricks held in your
inventory or dropped as loose item entities do not dry automatically.

Recipes are data-driven: `dry_brick.json` controls the duration. The existing
`survival.processingSpeed` setting also affects drying and purification.

## First smelter

At the research table, study one dry brick and one clay ball, then explicitly
learn Coastal smelter. The existing brick material accepts either dry or fired
bricks; old learned research is preserved.

Craft at a 3×3 table:

```text
D D D
D   D
P D P
```

D = Dry Brick; P = planks. Total: six dry bricks and two planks. No metal fittings
are required. Research consumes one sample brick in addition to the six needed
for crafting. Existing fired-brick recipes remain available for other uses.

## Local test checklist

Install 0.11.29 on every client and the server; no new world is needed. Restart
a running development client after rebuilding.

- Before research, craft and place the basic purifier and drying rack.
- Purify seawater with plank fuel; verify that cups are returned and fresh water
  is collected without losing items.
- Dry a brick in clear daylight. Check that rain, night and a roof pause it.
- Save and rejoin during drying; its progress should remain.
- Study a dry brick and clay, learn the smelter project, and craft the smelter
  from six dry bricks and two planks.
- Check the two new machine categories in JEI and the new placement previews.

Story remains open-ended; milestone 11 continues.

## Verification

The build and native dedicated-server bootstrap passed, including 258,540
combined crafting assertions, 666 research assertions and 726 survival assertions.
Starter regressions cover cup returns, exact default drying time, weather/cover
pauses, saved progress, full output handling and smelter research using dry bricks.
Packaged recipe, research, localization and model audits passed. All 543 model
files and 64 mesh files passed surface checks; existing model envelopes are
preserved. Client visuals, JEI interaction and cooperative play still need the
local checks above.
