# Raft refinements — 0.9.8

Minecraft 26.3 / NeoForge 26.3.0.48-beta / Java 25.
Jar: `build/libs/raft_ocean-0.9.8.jar`. Update client and server together.
Protocol: `raft-movement-9.8`. Existing saves remain compatible.

## Building

Hold a Collection Net and click a foundation edge. Nets snap beside foundations
at deck height, including while crouching. Their whole mesh surface can be picked.
Extend net → net → foundation: nets now connect separated wood foundations during
placement, reload and shark damage. Removing a required net bridge is refused
until another platform connection exists. Collision, bounds and overload checks
still apply. Nets add connections but no buoyancy.

Foundation buoyancy increases from 20 to 40. Spare equipment capacity after
foundation weight and the safety reserve increases from 6 to 24 per foundation.
The starter raft now has 216 spare capacity instead of 54.

## Engine

The original engine has an animated rear paddle wheel and a blue thrust arrow.
Placement determines its forward direction; hull rotation turns that direction.
Click **Forward / Reverse** in its menu to change direction. The button displays
the current mode. **Thrust** displays the actual bearing. Reversing also reverses
the wheel smoothly. Add supported native cooking fuel and click Start.

Stopping preserves the burn reserve; an active anchor prevents thrust and fuel
consumption. Engines follow their mounted direction, while the steering wheel
rotates the hull gradually. Current, sails and paddles still affect the resulting
course; opposing engines can cancel each other.

`navigation.engineFoundationCapacity = 100` controls foundations powered per
engine at full thrust (range 1–1024). Larger layouts reduce individual engine
power proportionally; additional engines recover propulsion. Global speed limits
and smooth acceleration apply. This is an original simplified load model.

## JEI

JEI 31.9.0.58 is automatically installed for the development client. It remains
optional for release clients and is not needed on dedicated servers.

Custom recipe pages cover Purifier, Rain Collector, Planter, Tree Planter and
Cooking Pot. They display server datapack inputs, output counts, byproducts,
processing time, fuel choices and conditions. NeoForge synchronizes these recipes
on login and reload; the plugin refreshes its active pages. Grill/Smelter items
link to native campfire/smelting recipes, and raft crafting/stonecutting/smithing
stations link to their native categories. Machine ingredient insertion is manual;
automatic transfer into custom reservoirs is not implemented.

## Planting and diving

The existing Raft Planter accepts Sugar Cane and Kelp. Each recipe uses one crop,
one Planting Mix and one fresh-water charge. With light, no roof and default
processing speed, two minutes produces two harvest items plus one replanting
cutting in the second output. Pouring returns the physical empty cup. Both output
slots must have room. Cane has growing segmented stems; kelp has growing fronds
and its own cosmetic shallow water tray. These visuals do not change colliders.

New ordinary and story island templates have underwater shelves with supported
kelp, seagrass, sand, gravel, clay and exposed coal/copper/iron ore. Ordinary
beaches also have sugar cane where water supports it. Crates can supply cane and
kelp to start the loop in older worlds. Native harvesting/tool rules apply.
Island changes require unexplored chunks or a new Ocean World; existing island
blocks are preserved.

## Sharks

Distant raft attacks stage several blocks outside an exposed edge before heading
inward. Their chosen approach direction stays stable in the rotating hull frame.
Sharks lead moving targets, turn gradually, track the edge during windup, bite,
then turn away and retreat before circling. Unreachable approaches time out.
Swimmer attacks require visible windup, facing and line of sight, followed by
retreat and cooldown. Bait, fighting and non-hostile difficulty interrupt attacks.
Existing jaw snap, recoil and tail animations remain server timed.

## Test checklist

1. Place foundation → net → net → foundation, including crouching. Walk on the
   mesh and save/reload. Expect connected platforms that carry you with the hull.
2. Place several heavy stations on the starter raft. Expect increased capacity;
   occupied cells, terrain and player overlaps should still refuse placement.
3. For a clear engine test, disable sails and temporarily set current multiplier
   to zero. Test all four mounting directions and Reverse. Expect motion matching
   the arrow. Turn with the wheel; lower the anchor and check that fuel pauses.
4. Browse crop and pot recipes in JEI, then `/reload` and reconnect. Check machine
   categories, ingredient amounts, conditions and returned containers.
5. Grow cane and kelp. Expect two harvest items, one cutting and one returned cup.
   Harvest both outputs; the crop model should disappear.
6. Visit a newly generated island. Dive for kelp and ores. Expect sharks to line
   up, telegraph bites and retreat.

Build and dedicated-server checks passed: 725 carry, 362 construction, 48 crafting,
85 stations, 108 salvage, 274 survival, 130 navigation, 558 creature/config and
552 fresh Ocean World assertions, plus standalone geometry/codec checks.
Live JEI startup/screens/reload, animation appearance and connected multiplayer
still need client testing. This continues milestone 9; milestones 10–12 are unstarted.
