# Workshop and animation upgrade — 0.9.7

Update client and server together. Existing raft saves remain compatible.
This continues milestone 9; research unlocks remain milestone 10.

## Fine steering

The sail and wheel screens provide −1°, +1°, −5°, +5°, −15°, +15°, −45°
and +45° buttons. Sail trim wraps precisely across −180°/180°; the wheel target
wraps across 0°/360°. Centre sets sail trim to zero. On the wheel, Centre sets
the target to the hull's present heading, cancelling an unfinished turn.

These controls keep the normal anchor, terrain and turn-speed rules. The engine
follows the hull heading; the paddle already follows your freely chosen look
direction. Fine controls do not change engine fuel use or speed limits.

## Original material chains

The expansion has 15 original items and 29 recipes: eight component/skewer
crafts, three firing/cooking recipes, four pot meals and 14 alternative equipment
recipes. Existing recipes still work. These are original survival crafting
chains inspired by the mechanics of raft survival games.

| Ingredients | Result |
| --- | --- |
| 2 clay balls + sand/red sand | 2 damp coastal bricks |
| Fire a damp brick in the raft smelter | 1 fired coastal brick |
| 2 iron ingots + scrap fasteners | 2 pressed metal plates |
| 1 copper ingot + plant fiber | 4 copper wire |
| 2 wheat + a drift leaf | 2 binding paste |
| Woven mesh + 3 plant fiber | 1 canvas roll |
| 2 timber braces + metal plate + binding paste | 2 reinforced braces |
| Carrot + potato + stick | 1 raw root skewer |
| Raw cod/salmon + stick | 1 raw fish skewer |
| Grill a raw root skewer | Roasted root skewer |
| Grill a raw fish skewer | Charred fish skewer |

All ingredient entries above occupy separate crafting slots. Raw skewers need
grilling before eating. Cooked skewers return their stick. Bricks require fuel
in the smelter; skewers require fuel in the grill.

Clay, sand and copper can be mined on resource islands. Crates now occasionally
contain kelp and brown mushrooms for the new meals. Dry kelp in the smelter.
Binding paste uses farmed wheat and salvage leaves, so its chain does not depend
on finding kelp. New crafting/firing/skewer recipes have recipe-book unlocks.
Pot meals use the custom processing system and are listed below.

## Cooking pot meals

The pot's first slot is now labelled Base. Its accepted ingredients include raw
fish, wheat, brown mushrooms, dried kelp and cooked cod. Each meal also needs
one fresh-water unit, one bowl, and normal cooking fuel. Insert a filled cup or
pour it directly into the reservoir. Inserted cups return to the empty-cup
output; direct pouring returns the cup to your hand. Each eaten meal returns a
bowl. Durations below use the default processing speed.

| Base slot | Vegetable slot | Meal | Time | Hunger points |
| --- | --- | --- | --- | --- |
| Wheat | Carrot | Garden Broth | 20 seconds | 7 |
| Brown mushroom | Potato | Mushroom Chowder | 25 seconds | 9 |
| Dried kelp | Beetroot | Sea Grain Bowl | 20 seconds | 8 |
| Cooked cod | Potato | Hearty Fish Pot | 30 seconds | 10 |
| Raw cod/salmon | Potato/carrot/beetroot | Existing Sea Garden Stew | 20 seconds | 8 |

## Alternative equipment recipes

Patterns are rows from top to bottom; `-` is an empty slot. P plank, B timber
brace, R reinforced brace, C binding, M metal plate, W copper wire, F scrap
fasteners, T canvas roll, D fired coastal brick, N woven mesh, L planting mix.
These produce the existing equipment; they provide more uses for intermediate
materials and do not grant hidden strength or performance upgrades.

| Equipment | Pattern | Output |
| --- | --- | --- |
| Foundation | `RR / PP` | 2 |
| Sail | `TTT / BCB / -B-` | 1 |
| Engine | `MWM / FRF / MMM` | 1 |
| Anchor | `-C- / MRM / -B-` | 1 |
| Steering wheel | `-F- / RCR / -B-` | 1 |
| Purifier | `DWD / MCM / BBB` | 1 |
| Smelter | `DDD / DMD / BBB` | 1 |
| Grill | `MFM / BCB` | 1 |
| Cooking pot | `M-M / MMM / -B-` | 1 |
| Tree planter | `RCR / PLP / BBB` | 1 |
| Bed | `TTT / BCB` | 1 |
| Collection net | `RCR / CNC / RCR` | 1 |
| Chest | `PPP / RFR / PPP` | 1 |
| Roof | `-T- / TCT / BBB` | 2 |

## Visible activity

- Purifier: water level, bubbles, heat and rising steam while processing.
- Rain collector: stored water and falling water droplets while collecting rain.
- Grill: food on its surface, flickering embers and rising smoke while cooking.
- Cooking pot: liquid, bubbles, steam and a moving stirrer while cooking.
- Smelter: flickering heat and chimney smoke while firing.
- Planter: growing stalks/leaves, wheat grain heads and wind sway. Harvesting
  crops clears the mature plants; remaining seeds stay in their output slot.
- Tree planter: smooth growth between server updates and swaying leaf layers.
- Chests, including both halves of double chests: opening and closing lids.
- Barrels: lifted/tilting lids while their inventory is open.
- Stonecutter and grindstone: wheels spin and coast while a player uses them.
- Loom: moving fabric while in use.
- Crafting, smithing and cartography tables: a small animated work tool while
  their menus are open.

Existing sail, wheel, engine, anchor and wind-streamer animations remain active.
Server snapshots transmit only bounded visual state, not inventory contents.
Machine activity follows successful processing; blocked or idle machines fade
to a stop. Tool-table activity follows an open, valid menu. Visual updates occur
once per second and smooth over several client frames. Animation parts do not
change colliders, consume items or add ticking particle entities.

## Test checklist

- Adjust sail/wheel by one degree in both directions, test all larger steps,
  cross their wrap boundaries, and try Centre. Test with the anchor raised/down.
- Craft the new materials and cook both skewers plus a pot meal. Check fuel,
  ingredients, food output and the returned cup/bowl/stick.
- Run each processing machine, then remove supplies or block its output. Its
  working animation should stop. Check crop/tree growth and harvesting.
- Open and close a chest/barrel, including a double chest. Open each tool table.
  Check the animation starts, then stops after closing or leaving reach.
- Walk beside animated equipment while translating and turning the raft.
  Check the same activity is visible to a second player.

Build and both isolated server checks pass. Native tests cover the new recipes,
food/container returns, fine steering, menu activity and bounded animation
snapshots. Final animation appearance and connected-client behavior require
user testing. Regenerate this update with `python tools/generate_workshop_assets.py`.
