# Raft Ocean 0.11.10 — archery and crafting

Target: Minecraft 26.3 / NeoForge 26.3.0.48-beta / Java 25.
Milestone 11; the story remains open-ended.

## New equipment

| Bow | Durability | Speed compared with vanilla | Anvil repair material |
| --- | ---: | ---: | --- |
| Driftwood Bow | 192 | 85% | Stick |
| Reef Laminated Bow | 384 | 110% | Leather |
| Stormsteel Bow | 640 | 125% | Iron ingot |

All bows use the native 20-tick full draw, aiming, critical shots, durability,
ammo selection and enchantment hooks. Vanilla arrows work in the new bows;
the new arrows are tagged as arrows for vanilla bows and crossbows.
Bow enchantments and durability enchantments recognize all three bows.
Creative uses free Reed Arrows with restricted pickup, preventing duplication.

| Arrow | Base damage factor | Tip material |
| --- | ---: | --- |
| Reed Arrow | 1.5 | Drift leaf |
| Scrap Arrow | 2.0 | Scrap |
| Steel Arrow | 2.5 | Iron ingot |
| Broadhead Arrow | 3.0 | Metal plate |

Vanilla arrow base damage factor is 2.0. Actual damage depends on launch speed,
critical hits, armor and enchantments; these numbers are not fixed heart damage.
Special arrows remain consumable with Infinity; vanilla arrows follow native Infinity rules.
Missed survival arrows retain their item type for pickup and world save/load.
Dispensers fire the custom arrows with the same base damage and pickup identity.
The flying projectile currently uses Minecraft's arrow appearance; held and inventory
items have original 3D models. Bow drawing has three custom animated model stages.

## Sixteen new recipes

All recipes are shapeless and appear in JEI's standard crafting category.
Use the raft crafting table for the recipes requiring more than four inputs.
Counts below describe separate ingredient slots, not multiple items in one slot.

| Result | Ingredients |
| --- | --- |
| 2 Waxed Bowstrings | 2 cordage + binding paste |
| 2 Waxed Bowstrings, alternate | 3 string + binding paste |
| 8 Bound Arrow Shafts | 2 sticks + binding paste |
| 8 Bound Arrow Shafts, alternate | 2 bamboo + binding paste |
| Driftwood Bow | 3 sticks + bowstring + binding paste |
| Reef Laminated Bow | Driftwood Bow + timber brace + bowstring + canvas roll + binding paste |
| Stormsteel Bow | Reef Laminated Bow + 2 metal plates + copper wire + bowstring |
| 4 Reed Arrows | 4 shafts + drift leaf tip + drift leaf fletching |
| 4 Scrap Arrows | 4 shafts + scrap + drift leaf |
| 4 Steel Arrows | 4 shafts + iron ingot + drift leaf |
| 4 Broadhead Arrows | 4 shafts + metal plate + drift leaf |
| Four feather alternatives | Replace the fletching drift leaf with one feather in each of the four arrow recipes |
| 4 vanilla arrows | 4 shafts + flint + feather |

Bow upgrade crafting consumes the previous bow and produces a new unenchanted bow.
It does not transfer existing enchantments, damage or custom names. Enchant the final
bow tier, or repair the same tier with its anvil repair material.

Reef Laminated Bow and Scrap Arrow join the existing Tools research unlock.
Stormsteel Bow, Steel Arrow and Broadhead Arrow join Metal fabrication.
Basic bow, bowstrings, shafts and Reed Arrows are starter recipes.
Existing research progress remains intact; research restrictions apply on raft stations
when the research config is enabled, as before.

## Test locally

1. Restart with the new jar on both client and server. Keep only one Raft Ocean jar.
2. Find all three bows, four arrows, bowstring and shaft in the salvage creative tab.
   Expect colored models filling their slots, English/German names and speed/damage tooltips.
3. Look up the sixteen crafting recipes in JEI. Try leaf and feather alternatives.
4. In Survival, fully draw each bow: one shot consumes one arrow and one durability.
   A click without drawing consumes neither. No ammunition prevents Survival drawing.
5. Fire into a solid block, recover the arrow, and repeat after saving/reloading.
   Expect the original arrow item. Compare the bow tiers' ranges.
6. Try a dispenser, native bow/crossbow and anvil repair/enchantments.
7. Test aiming and shooting while the raft moves in connected co-op.

## Verification

Isolated build and native dedicated-server bootstrap passed. Archery adds 138 native
assertions for real shots, all bow/arrow combinations, speed, consumption, durability,
creative pickup restrictions, arrow persistence, dispenser registration and actual
loaded recipe assembly. Existing survival and automation assertions also passed.
Ocean regression suite passed 741 assertions earlier in this change.
Native model parser and 118 inventory/draw-model silhouette checks passed.
Packaged coverage, localization, original models/story/audio and diving audits passed.

In-game bow positioning/animation and connected co-op latency still need human testing.
The pre-existing intermittent raft movement reports are not considered resolved by archery.
