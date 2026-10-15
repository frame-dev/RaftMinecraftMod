# Prepared meals, quick net pickup and interaction fixes — 0.11.6

This milestone 11 update extends the existing original cooking system and improves shared-inventory interactions. Meal bonuses are inspired by the mechanic described in [Raft's official developer announcement](https://store.steampowered.com/news/posts/?appgroupname=Raft&appids=648800&enddate=1655919278&feed=steam_community_announcements). Recipes, names, models and artwork remain this mod's original designs.

## Prepared meal bonuses

Cook these existing recipes in the cooking pot, then eat the output. Nutrition, ingredient costs, physical bowl return, original models and JEI recipe support remain available.

| Meal | Ingredients plus fresh water and a bowl | Bonus |
| --- | --- | --- |
| Garden Broth | Wheat + carrot | Speed I |
| Mushroom Chowder | Brown mushroom + potato | Water Breathing |
| Sea Grain Bowl | Dried kelp + beetroot | Haste I |
| Hearty Fish Pot | Cooked cod + potato | Absorption I |

Bonuses default to 120 seconds, apply on the server and use vanilla effect synchronization. Eating another meal refreshes the effect without stacking its strength. Stronger existing vanilla effects follow Minecraft's normal merging rules. These foods have bilingual bonus tooltips and do not require new model assets or packet formats. Water breathing supports short resource dives; it does not protect against shark damage.

In `[survival]`, `preparedMealBonuses = false` disables the bonuses while retaining normal food nutrition. `mealBonusTicks = 2400` accepts 200–12000 ticks. Tick durations assume 20 ticks per second. Stop the world/server before editing its active synced config and restart. The host's config applies to the whole crew.

## Quick net pickup

Crouch-right-click a collection net with an empty hand to move its stored loot into your inventory. Regular interaction still opens its shared inventory. Only cargo that fits is removed. Matching stacks fill before empty slots; excess loot stays in the net and no items are thrown into the water. Repeated clicks and another crew member's next pickup cannot duplicate already transferred cargo. Full Creative inventories also retain excess cargo.

## Interaction fixes

- Cup transfers reject missing or removed stations, wrong-world players, spectators and players outside the existing eight-block station reach, preserving the held cup and reservoir.
- Fresh-water drinking updates hydration only on the server, avoiding a competing client attachment update.
- The direct crop-theft transaction enforces Peaceful and disabled hostile damage as well as the bird AI. Removed rafts reject theft.

These are targeted interaction fixes. Previously reported movement/collision glitches under live network latency still need reproduction and in-game testing; this update does not claim to eliminate them all.

## Local test checklist

- Cook and eat each meal: regain hunger, receive a bowl and see its listed level-one bonus for two minutes.
- Eat the same meal twice: strength stays at level one. Disable preparedMealBonuses: ordinary food and bowl return remain.
- Crouch-right-click a loaded net with an empty hand: collect what fits. Fill your inventory and repeat: remaining cargo stays in the net.
- Have two crew members take turns collecting the same net: total cargo is conserved.
- Pour/draw cups on a moving raft, including the offhand: exchange one cup. Empty/full reservoirs and stale stations lose no items.
- Test Peaceful: crops remain untouched and sharks remain passive.

Native server regression checks cover consumption, configured duration, non-stacking, disabled bonuses, bowl return, exact/partial/full net transfers, Creative retention, stale/remote cup rejection and direct Peaceful theft protection. Rendering and connected multiplayer behavior still need local testing.
