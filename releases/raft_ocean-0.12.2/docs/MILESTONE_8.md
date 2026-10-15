# Milestone 8 — creatures and configuration (0.8.1)

This version adds original Reef Sharks, Crop Skimmers and Tide Grazers, a Timber
Spear, water-placeable Shark Bait, foundation health and hammer repairs. Gameplay
settings synchronize from the host; see [configuration](CONFIGURATION.md).

See [the 0.8.1 expansion guide](RAFT_EXPANSION.md) for larger decks, survival modes,
double chests and the improved thirst HUD.

## Build and launch

```powershell
$env:JAVA_HOME = 'C:\Program Files\Zulu\zulu-25'
.\gradlew.bat build
.\gradlew.bat runClient
```

Artifact: `build/libs/raft_ocean-0.8.1.jar`. Use matching 0.8.1 clients and server;
movement protocol is `raft-movement-8.1`. Existing worlds retain raft data and
contents. Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25, ModDevGradle 2.0.147
and Gradle 9.2.1 remain pinned. No world regeneration is required.

## Crafting and behavior

Recipes use the raft crafting table. A Timber Spear is a vertical column of
scrap fasteners, timber brace, then a binding (cordage or string). Shark Bait uses
two raw cod/salmon in the outer cells of the top row and a binding below in the
middle. Both have recipe unlocks; their items also appear in Ocean Salvage.
The spear is a melee weapon with native durability and combat handling.

An occupied raft can attract one shark and, when a planter exists, one crop pest.
Sharks circle, approach exposed foundations and wind up before biting. Each
foundation has 100 health; a default Normal-difficulty bite removes 25. Damaged
foundations develop cracks below 60 health. Right-click a damaged foundation
with the raft hammer to spend one plank for 25 health. Sneak-right-click still
removes building pieces. Creative repairs require no materials.

Attacking a shark interrupts its approach and briefly makes it flee. Sharks can
also bite nearby survival players swimming in water, with an attack cooldown.
Right-click exposed source water with bait: a floating chum object distracts
nearby sharks for one minute by default. Failed/overlapping/capped placement
does not consume an item. Bait is physical and does not protect every distant shark.

Crop Skimmers dive at uncovered planters and steal one harvested item, or a seed
if the output is empty. A successful theft resets that planter's growth progress.
Raft roofs above the planter protect it. Tide Grazers use native land movement,
fleeing, wheat temptation, breeding and baby growth; they require nearby suitable
land. New Ocean World island terrain belongs to milestone 9, so use existing
land in a normal world or an operator summon to test grazers now.

Operator test entities: `raft_ocean:reef_shark`, `raft_ocean:crop_skimmer`,
`raft_ocean:tide_grazer`. Natural spawning observes difficulty and loaded-area caps.
Meshes have animated shark tails, bird wings and grazer legs, with original skins.

## Test checklist

- Confirm 0.8.1 in Mods; open its Config screen. Disable thirst, reopen the world,
  and check hydration stays full. Toggle each HUD independently.
- On Normal difficulty, sail an occupied raft and wait for the default 30-second
  creature spawn check. Build an uncovered planter for birds.
- Watch a shark bite, repair the damaged edge with hammer and planks, then place
  bait in nearby exposed water. Expect distraction and exactly one item consumed.
- Leave a crop exposed, then protect another with a roof. Expect one-item theft
  from the exposed planter and no theft from the covered one.
- Fight a shark with the crafted spear; check swimming damage and brief retreat.
- Test grazers near land with wheat, including breeding. Save/reload damaged
  foundations and station contents. Check creature health and bait expiry persist.
- With matching clients on a dedicated server, check turning/carry, shared repairs,
  damage, crop inventories and bait placement. Retest prior snap-back/item-loss reports.

## Limits

Broken foundations keep the largest connected deck component. Detached foundations,
unsupported pieces and pieces shed to restore buoyancy become item drops; stored
contents drop too. They do not become separate moving rafts. Losing the last
foundation destroys the raft. Protect your cargo and repair before repeated bites.

Creature models are original cuboid prototypes. Movement uses native collision
with simple chase/orbit behavior, without sophisticated obstacle detours. Original
creature sounds, advanced gear, islands and research arrive in later milestones.
Automated dedicated-server checks use fake players; live graphics and connected
multiplayer still need your local test. This milestone does not start island generation.
