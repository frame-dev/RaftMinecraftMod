# Milestone 6 — survival loop (0.6.1)

Thirst, clean water, cooking, baited fishing, four crops, smelting, and moving
raft beds are implemented. All processing runs on the server. Stations, shared
contents, progress, and remaining fuel save with the raft. Existing worlds work;
the starter kit is unchanged. Use 0.6.1 on every connected client and server.

## Build and launch

Close previous clients/servers, then run from the project folder:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Zulu\zulu-25'
.\gradlew.bat build
.\gradlew.bat runClient
```

Verify **Raft Ocean 0.6.1** in Mods. Artifact: `build/libs/raft_ocean-0.6.1.jar`.
The required movement protocol is now `raft-movement-6.1`; earlier clients cannot
decode the new station kinds. Saved rafts and cargo from earlier versions load.

## First water and food

1. Make a workbench and wooden salvage hook using the existing bootstrap recipes.
2. Collect leaves and scrap, then make cordage, braces, and fasteners. See
   [the component guide](CRAFTING_CHAINS.md).
3. Make two timber cups from two diagonal planks in the inventory grid: `P- / -P`.
4. Hold a cup and right-click ocean water. The water stays in the ocean; your cup
   becomes a Sea Water Cup. In this prototype, the cup treats all source water as
   sea water. It cannot be drunk until purified.
5. Build a purifier. Right-click it with sea water to pour one cup into its
   reservoir. Your empty cup returns immediately. Sneak-right-click to open its
   screen and add planks to Fuel. After about ten seconds, use the empty cup on
   the purifier to collect stored fresh water.
6. Hold right-click to drink. It restores six thirst points and returns your cup.
7. Turn one bread into eight crumb bait, craft a Baited Ocean Rod, and fish.
   Cook caught cod/salmon on a grill using planks as fuel.

The thirst bar appears above the hunger area in Survival/Adventure. Full thirst
is 20 points. One point drains every 30 seconds, or every 15 seconds while
sprinting. Full hydration therefore lasts ten minutes at the base rate. At zero,
dehydration deals one damage every five seconds, stopping at one health point;
Peaceful skips this damage. Creative and Spectator are exempt. Thirst and its
partial drain timer persist on reconnect; death resets thirst to full.

## Craft the stations

Rows read top to bottom; `-` is an empty slot. P = any planks, B = timber brace,
C = cordage or string, F = scrap fastener, M = woven mesh, I = iron ingot,
L = planting mix. Use the moving raft workbench for three-wide/tall recipes.

| Station | Rows | Mass |
| --- | --- | --- |
| Water purifier | PFP / BCB | 4 |
| Rain collector | CMC / BFB | 4 |
| Grill | FFF / BCB | 4 |
| Cooking pot | IFI / FFF / -B- | 6 |
| Planter | PLP / BMB | 4 |
| Smelter | FFF / FPF / BBB | 8 |
| Rest bed | MMM / BCB | 4 |

Each recipe produces one item. Stations place with the usual raft placement,
support, reach, and buoyancy rules. Extend foundations if the weight limit blocks
placement. They mount flush on upper floors. Crouch + hammer removes/refunds a
piece and drops its contents once. The Construction Creative tab includes all
26 structural kinds; the Ocean Salvage tab includes the new supplies and rod.

Planting mix: four drift leaves in separate slots, shapeless, produce four mix.
Bait: one bread, shapeless, produces eight crumb bait.
Rod: `--S / -SC / S-C`, where S = stick and C = cordage or string.

## Processing screens and water reservoirs

All six machines have an original wood-framed screen with colored role labels,
input slots, extraction-only ready slots, a progress meter, an orange fuel meter,
and a reason for paused work. Water machines show stored cup units (0–4);
purifiers have separate sea-water and fresh-water reservoirs. Every player sees
the same server-owned items and process state. Screens keep working as the raft
moves and close when their station is removed or the player leaves reach.

| Machine | Inputs | Ready slots / stored result | Work |
| --- | --- | --- | --- |
| Purifier | Pour sea water; Fuel; optional Cup slot | Stored fresh water; filled/returned cups | 10 seconds per unit |
| Rain collector | Rain and open sky; optional empty Cup | Stored fresh water; filled cup | 10 seconds per unit |
| Grill | Food, Fuel | Cooked food | Loaded campfire recipes |
| Smelter | Ore/material, Fuel | Smelted material | Loaded furnace recipes |
| Planter | Seed/crop, Mix; pour fresh water or insert Water cup | Crop, Seed, returned Cup | 120 seconds with light and clear overhead |
| Cooking pot | Fish, Veg, Bowl, Fuel; pour fresh water or insert Water cup | Stew, returned Cup | 20 seconds |

**Cup controls:** right-click source water with an empty cup to fill it with sea
water. Use sea water on a purifier to pour; use an empty cup on a purifier or rain
collector to collect fresh water. Use fresh water on a planter or cooking pot to
water its reservoir. Each transfer affects exactly one cup and returns its other
state immediately. Hold right-click away from stations to drink fresh water and
receive the empty cup. Sneak-right-click a machine with a cup to open its screen
instead of transferring. An empty or full reservoir leaves the held cup untouched.

Rain collectors work without a cup inside and stop at four units. Purifiers also
stop when their clean-water reservoir is full, preserving remaining fuel and sea
water. Planting/cooking consume one stored water unit at completion. An inserted
fresh/sea-water cup is emptied into storage immediately and returned to its Cup
slot; if that slot is full, the input cup stays unchanged. An inserted empty cup
in a purifier/collector takes available stored water into Full. Taking water from
a purifier's tank does not duplicate old filled cups retained in its inventory.

Left-click and right-click retain Minecraft's normal pickup/splitting behavior.
Shift-click routes matching supplies to appropriate inputs, and ready items back
to the player. It cannot insert into output slots. Hover empty slots for help.
The **Supplies** tab provides spare storage; processing never draws from it.
Put down a carried cursor item before switching tabs. Existing 27-slot machine
inventories retain all indices, counts and item data: old inactive slots appear
under Supplies. Old cup stacks keep their 16-item capacity; new interactions still
exchange one cup at a time. A transformed cup uses the hand when it was the last
cup, otherwise inventory space; with a full inventory it drops safely nearby.

Ordinary furnace fuel, fuel burn time, and cooking-speed components are used.
One fuel item starts a burn reserve; blocked/idle machines keep remaining fuel.
Ingredients and all recipe outputs are checked before recipe completion. Supplied
water and fuel remain visible in their gauges while ingredients are processing.
Unloaded/offline machines pause; cargo, water, work progress and fuel save together.
A roof/upper floor blocks rain, and planters need light level 9 and clear overhead.

## Grow, cook, and smelt

Barrels/crates can now supply wheat seeds, beetroot seeds, carrots, and potatoes.
Put one seed or planting crop into the planter with one mix and one clean cup.
The empty cup returns immediately when water is poured. After growth, it yields
two food crops and two replanting seeds/crops. Move returned seeds back into Seed
manually; each crop uses one mix and one stored water unit.
Wheat can make bread through the normal recipe; carrots and beetroot are edible,
and potatoes can be grilled. The original planter model uses a static soil
texture; growing and harvesting happen through its inventory in this prototype.

The cooking pot makes **Sea Garden Stew** from fish, a vegetable, clean water,
and a bowl. Eating it restores eight hunger points (four icons) and returns the
bowl. Pouring water returns the empty cup immediately. The custom machine recipe cannot
be assembled instantly at a workbench.

The smelter accepts native furnace recipes, plus **one scrap → one iron ingot**
in 20 seconds. Iron makes the cooking pot and existing metal components. Station
processing does not currently award furnace experience. Native world furnaces
can also use the data-pack scrap-smelting recipe.

## Baited fishing

The Baited Ocean Rod uses Minecraft's real bobber, fishing loot, bite timing,
open-water checks, enchantments, and retrieve controls. Carry crumb bait anywhere
in the main inventory. Every survival cast consumes one bait, including unsuccessful
casts; reeling does not consume another. Creative does not need bait.

The rod has 128 durability. Keep holding a fishing rod while waiting for the
bite, then right-click to reel. A drifting raft can move you too far from a
world-space bobber; reel before the native distance limit is reached. Ordinary
vanilla rods retain their native behavior and do not require the mod's bait.

## Moving beds and night rest

Right-click a Raft Rest Bed to make that raft and bed cell your moving respawn
home **in Ocean World**. It replaces your earlier chosen home there. On death,
the existing raft loading/recovery flow locates the raft and boards that bed's
current coordinates. If the bed is removed or obstructed, a clear deck position
is used. A missing raft uses the existing bounded recovery without refilling the
starter kit. A working vanilla bed chosen later still takes precedence.

At night, right-click the raft bed and stay within three blocks for five seconds.
Other players must rest too, according to the world's players-sleeping percentage;
native sleeping players also count. Nearby hostile monsters refuse initial rest,
and moving away, taking damage, removal, logout, or a 30-second timeout cancels it.
When enough players finish resting, the current world clock advances to the native
wake-up marker. Time-progression rules and NeoForge's sleep-finished event apply.
Rest also resets the resting player's time-since-rest statistic.

This is cooperative resting while standing near the compact bed, without native
lying-down animation. Night rest works in dimensions supporting night sleep;
moving respawn homes currently apply only to Ocean World. Rain is not cleared by
this rest flow. Beds have no inventory.

## Test checklist

- [ ] Right-click an input with a carried stack, then retrieve half a ready stack.
      Expected: exactly one is placed or half picked up; all remaining items stay.
- [ ] Shift-click supplies and ready items. Expected: valid input/fuel routing;
      output slots never accept inserted stacks, and invalid items remain owned.
- [ ] Switch to Supplies. Expected: your previously stored spare items are present.
- [ ] Pour and collect with both hands and stacked cups. Expected: one cup changes
      state each time, with no lost containers. Full/empty tanks preserve the cup.
- [ ] Open all six machines while drifting. Expected: readable themed screens,
      current progress/fuel/water levels and pause reasons. Two players see shared
      contents and gauges; their tab choices are independent.

- [ ] In Survival, wait 30 seconds. Expected: thirst drops by one and the bar
      updates. Drink purified water; expected: +6 and an empty cup returned.
- [ ] Purify several cups while the raft drifts. Expected: progress continues,
      ingredients/fuel consume correctly, and shared contents stay synchronized.
- [ ] Fill the output slot or cup-return slot. Expected: processing pauses without
      partial outputs. Free space; expected: it resumes.
- [ ] Save/reload during processing and reconnect. Expected: machine progress,
      fuel, cargo, thirst, and the chosen moving bed home persist.
- [ ] Test a collector in rain, then put a roof or upper floor above it. Expected:
      open collector stores fresh water without cups; covered collector pauses.
- [ ] Cast without bait, then with bait. Expected: first is refused; second uses
      one bait. Catch and grill a fish; expected: cooked food in the output slot.
- [ ] Grow all four crops. Expected: the cup returns on watering; harvest and planting supplies
      appear after two minutes in sufficient light. Make bread and stew.
- [ ] Smelt scrap; expected: iron after 20 seconds with normal fuel. Eat stew;
      expected: four hunger icons restored and the bowl returned.
- [ ] Set an Ocean World bed home, let the raft travel, then die. Expected: respawn
      follows that bed. Remove it and repeat; expected: safe deck fallback.
- [ ] With two clients, rest at night. Expected: one resting player alone cannot
      skip night under the default 100% rule; both finishing rest advances morning.
- [ ] Remove an occupied machine while another player has its screen open.
      Expected: the menu closes and supplies drop once.

## Validation and limits

Build and isolated servers pass: 91 standalone checks, 725 carry, 246 construction,
48 crafting, 85 existing station, 105 salvage, 128 survival, and 70 Ocean World
runtime assertions. New checks use actual loaded recipes/fuels, native bobber
creation, item consumption/remainders, machine full-output transactions and
save/load, all four harvests, player hydration persistence/death copying, actual
ocean cup filling/rain/roof checks, moved-bed respawn/deck fallback, and cooperative
world-clock advancement. These use fake players and do not prove client timing.

Connected HUD updates, station rendering, natural fishing catches, live bed
respawn timing, and simultaneous multiplayer interactions need your checklist.
Screens are implemented, but client visual layout still needs your in-game
check. Crop-stage/bed animation, hoppers, redstone, automated supply routing and
furnace XP remain prototype limits. Balance/configuration and German localization remain milestone 11. Universal
attachment of arbitrary vanilla blocks is still pending. Milestone 7 starts after
your gameplay results or explicit instruction to proceed.

Asset generation: run the building, salvage, crafting, then survival generators
under `tools/`. Generated JSON/PNG resources are saved with the project, so normal
builds do not require Python/Pillow. Processing recipes use `raft_ocean:purifier`,
`rain_collector`, `planter`, and `cooking_pot` types with ingredients, result,
duration, and optional byproducts; datapacks can change them without Java edits.
