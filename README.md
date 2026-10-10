# Raft Ocean

Version 0.11.32 adds [underwater expeditions, branching islands and crew features](docs/EXPEDITIONS_AND_CREW.md): four flooded dive sites, three resource islands, original journals, optional receiver channels, saved crew roles, shared research and timed teammate rescue. New locations generate in unexplored Ocean World chunks; the story remains open-ended. Install 0.11.32 on every client and the server.

Version 0.11.31 adds [optional mod compatibility](docs/MOD_COMPATIBILITY.md): shared crafting/research material tags, exported iron plates/copper wire, rare mod-resource ocean salvage, standard NeoForge energy item charging and lazy optional station adapter registration. Matching mod jars can be added to `compat-mods/` for development runs. Foreign moving machines require dedicated adapters; older Minecraft jars cannot load on 26.3.

Version 0.11.30 adds [a disposable stone anchor and basic food grill](docs/STARTER_ANCHOR_AND_GRILL.md), both available before research. The anchor preserves the requested material cost through a stone-bundle recipe and is consumed when released. Floating crates and barrels now supply occasional cobblestone. Install 0.11.30 on all clients and the server.

Version 0.11.29 [fixes starter survival progression](docs/STARTER_SURVIVAL.md). A basic wooden purifier needs no metal or research. Wet bricks dry on a Sun-Drying Rack in 150 sunny seconds; dry bricks unlock smelter research and craft the smelter without metal fittings. The existing metal purifier is now named Advanced Water Purifier. Install 0.11.29 on all clients and the server.

Version 0.11.28 [fixes stale research crafting results and reviews the project](docs/PROJECT_REVIEW_0_11_28.md). Result pickup now rechecks research, and existing grids refresh when access changes. Build, bootstrap and ocean-world checks passed.

Version 0.11.27 [expands research to 30 projects and adds two weapons and three meals](docs/RESEARCH_EXPANSION.md), with original custom models. Existing research progress is preserved; install 0.11.27 on clients and server.

Version 0.11.26 adds [the research unlock command](docs/RESEARCH_COMMAND.md): `/raft research unlock` for yourself or `/raft research unlock @a` for all online players. It learns every project and reveals recipes while keeping the current game mode.

Version 0.11.25 [ties recipe visibility and crafting to learned research](docs/RESEARCH_RECIPE_DISCOVERY.md). Locked recipes are hidden from the recipe book and JEI until learned; vanilla workstation conversions again require their research project. This supersedes 0.11.24's exemption. Install 0.11.25 on clients and server.

Version 0.11.24 [fixes vanilla chest/barrel/workstation conversions being hidden by research](docs/VANILLA_MOUNTING_FIX.md). Existing vanilla stations can be converted in either grid before research; materials-based recipes retain their locks.

Version 0.11.23 [audits every loaded 2x2/3x3 crafting recipe](docs/CRAFTING_AUDIT_0_11_23.md) and fixes building-hammer/wooden-hoe and paddle/timber-brace conflicts. The hammer and paddle now each require a binding.

Version 0.11.22 includes a [project maintenance review](docs/PROJECT_REVIEW_0_11_22.md): thin-platform carry fixes, consistent clearance choices, continuous sail textures and stronger movement-test completion checks.

Version 0.11.21 sets the [shark raft delay to ten minutes and adds a configurable five-minute crop-pest delay](docs/SHARK_ATTACK_DELAY.md), with saved countdowns and Peaceful support.

Version 0.11.20 adds a [configurable five-minute minimum delay for shark raft attacks](docs/SHARK_ATTACK_DELAY.md), including first attacks and cooperative repelling.

Version 0.11.19 extends the [texture flicker repair](docs/TEXTURE_FLICKER_FIX.md) to curved barrel/tank meshes and adds native polygon validation.

Version 0.11.18 fixes [overlapping model faces, atlas UV overflow and reinforcement edging flicker](docs/TEXTURE_FLICKER_FIX.md).

Version 0.11.17 adds [foundation reinforcement, matching-material quick stash and safer addon destruction](docs/COASTAL_UTILITIES.md).

Version 0.11.16 adds [API v2 and a full machine/addon development guide](docs/MACHINE_ADDON_GUIDE.md), with saved machine inventories, lifecycle hooks, filtered moving menus and synchronized models. A complete working addon is in [examples/raft-addon](examples/raft-addon).



An original Minecraft Java ocean survival mod inspired by raft survival mechanics.

Current scope: milestone 11, [polish, balance and performance](docs/MILESTONE_11.md).

Version 0.11.0 completes English/German label coverage, adds original action sounds and configurable cosmetic effect distance, and provides balance/performance presets.

Version 0.11.9 fixes [idle biofuel burning, unfair utility distribution, sprinkler collision and machine save/UI behavior; adds shared Pause/Resume controls](docs/AUTOMATION_BUG_FIXES.md).

Version 0.11.8 adds [water/fuel pipes, tanks, biofuel refining and power generation, electric purification and sprinkler irrigation, with original models](docs/RAFT_AUTOMATION.md).

Version 0.11.7 adds [wearable diving equipment and craftable first aid with original models and textures](docs/DIVING_AND_FIRST_AID.md).

Version 0.11.6 adds [prepared meal bonuses, quick net pickup and safer cup/crop interactions](docs/MEALS_AND_NET_FIXES.md).

Version 0.11.5 adds [17 more building, survival-machine, fuel, planter, hydration and propulsion settings](docs/EXPANDED_CONFIG.md).

Version 0.11.4 preserves [Peaceful behavior and adds shark timing/counts, optional Peaceful thirst and configurable starter supplies](docs/PEACEFUL_AND_CONFIG.md).

Version 0.11.3 adds [repeated foundation bites, three-hit cooperative shark defense and shared-raft entry/respawn](docs/COOP_AND_SHARK_DEFENSE.md).
Version 0.11.2 aligns [shark foundation bites with the wood edge and gates damage on jaw contact](docs/FOUNDATION_BITE_CONTACT.md).
Version 0.11.1 improves [low-ledge recovery and continuous collision checks for escape paths](docs/COLLISION_CLEARANCE_POLISH.md).
Version 0.11.0 adds complete English/German labels, original action sounds, cosmetic effect distance and balance/performance presets.
Version 0.10.10 refines [shared coastal materials, foundation details and rear joinery](docs/COASTAL_MATERIAL_POLISH.md).

Version 0.10.9 fixes [all six missing journal models caused by mixed texture atlases](docs/JOURNAL_TEXTURE_FIX.md).

Version 0.10.8 adds [branched coastal trees, distinct machine silhouettes and handmade model details](docs/HANDCRAFTED_MODELS.md).

Version 0.10.7 aligns [fills, machine effects, planter roots and rotated animated parts](docs/MACHINE_ALIGNMENT.md).

Version 0.10.6 fixes [small/off-center tool icons, striped round stations and shark geometry](docs/VISUAL_MESH_FIX.md).

Version 0.10.5 enlarges and centers inventory models individually to fit their slots.

Version 0.10.4 fixes [stretched model seams and spikes, with richer materials and colored station details](docs/MODEL_REPAIR.md).

Version 0.10.3 provides [custom models for every registered mod item, block and entity](docs/ALL_MODELS.md).

Version 0.10.2 adds [20 research materials, 24 recipe projects and finer models for every raft piece and moving part](docs/RESEARCH_AND_FINE_MODELS.md).

Version 0.10.1 adds [custom building/station models, coastal scenery and a development rebuild safeguard](docs/CUSTOM_MODELS.md).

Version 0.10.0 adds a moving research table, six personal research topics, tiered recipe unlocks and advancements. The story remains open-ended.

Version 0.9.16 leaves the story open for future chapters, with an unresolved transmission and updated English/German journals.

Version 0.9.15 adds [a connected six-chapter story and richer island architecture](docs/STORY_POLISH.md).

Version 0.9.14 adds [three original story islands and a six-chapter receiver journey](docs/STORY_EXPANSION.md).

Version 0.9.13 fixes [missing deck collisions across chunk boundaries](docs/DECK_COLLISION_FIX.md).

Version 0.9.12 adds [anvils, enchanting tables, Ender Chests, bookshelves and

machine collision fixes](docs/VANILLA_STATIONS.md).

Version 0.9.11 fixes the [Tide Grazer's missing animal attribute crash](docs/GRAZER_CRASH_FIX.md).

Version 0.9.10 lets perpendicular full walls join automatically in one deck cell,

with matching corner previews and the existing save format.

Version 0.9.9 adds [placement ghosts, wall snapping, paired movement frames,

JEI screen links and clearer labels](docs/PLACEMENT_JEI_FIXES.md).

Version 0.9.8 adds [net connectivity fixes, mounted engine thrust/reverse, JEI

machine recipes, cane/kelp planters and underwater resources](docs/RAFT_REFINEMENTS.md).

Version 0.9.7 adds [fine steering, expanded workshop recipes and machine/station

animations](docs/WORKSHOP_UPGRADE.md).

Version 0.9.6 adds [tree planters, edge nets, corner/curved walls, sail

counter-steering and the coastal visual upgrade](docs/COASTAL_UPGRADE.md).

Version 0.9.1 improves [creature and navigation animations](docs/ANIMATIONS.md).

Version 0.9.2 adds original detailed textures, articulated creature details,

shark deck-overlap recovery, and drowned-free Ocean World gameplay.

Version 0.9.3 adds server-timed shark bite anticipation, jaw snap, lunge and recoil.

Version 0.9.4 adds [six ordinary resource islands](docs/ORDINARY_ISLANDS.md),

with template footprints from 17 to 81 blocks across, alongside story islands.

Version 0.9.5 fixes [player overlap around raft objects and gradual shark attack

approaches](docs/MOVEMENT_FIXES.md). Update client and server together.

[Creatures](docs/MILESTONE_8.md) and [configuration](docs/CONFIGURATION.md) remain available.

The [sailing and engine](docs/MILESTONE_7.md) systems provide paddles,

wind sails, steering, fueled engines and anchors. The [survival loop](docs/MILESTONE_6.md)

provides thirst, water, cooking, baited fishing, farming, smelting and moving beds.

[Floating salvage, hooks, and nets](docs/MILESTONE_5.md) supply its materials.

The [Ocean World guide](docs/MILESTONE_4.md) covers the selectable ocean preset,

starter-raft spawning, and default death respawn. [Raft stations](docs/RAFT_STATIONS.md)

provide moving crafting/tool menus and persistent storage. Build and in-game

validation are tracked in PROJECT_STATE.md. Version 0.5.0 adds seven debris kinds,

two hook tiers, original bottle notes, shared net inventories, and bounded spawning.

Version 0.5.1 makes debris approach from ahead and pass by, with a settling delay

after course changes. Version 0.5.2 adds six original intermediate materials,

updated building/net/hook recipes, and direct salvage-based storage crafting.

See the [crafting chains and exact layouts](docs/CRAFTING_CHAINS.md).

Version 0.6.0 adds the functional survival stations, player thirst, and raft rest beds.

Version 0.6.1 adds original processing screens, safe slot routing, reusable cup

transfers, stored water reservoirs, and improved original station materials.

Version 0.7.0 adds original navigation controls and screens, native-fuel engines,

wind-driven sails, supported paddle strokes, anchors and a navigation HUD.

Version 0.7.1 adds physical hull rotation with rotating player carry, a craftable

wind streamer, a north-up speed/wind compass, animated wheel/crank/winch controls,

and fluttering, folding, and trimmed sail cloth. Build placement and station reach

follow the hull's local coordinates. See [the navigation test guide](docs/MILESTONE_7.md).

The [construction guide](docs/MILESTONE_3.md) covers the completed milestone 3 code.



Version 0.8.1 expands raft limits, adds survival modes and double chests, and improves

the thirst HUD. See [the expansion guide](docs/RAFT_EXPANSION.md).



Version 0.9.0 adds island structures, journals, receiver upgrades and a bunker

puzzle, plus foundation-limit configuration and optional JEI development testing.



## Pinned development versions



| Component | Version |

| --- | --- |

| Minecraft Java | 26.3 |

| NeoForge | 26.3.0.48-beta |

| ModDevGradle | 2.0.147 |

| JDK | 25, 64-bit |

| Gradle wrapper | 9.2.1 |

| Mod | Raft Ocean 0.9.0 (`raft_ocean`) |



Verified on 2026-10-04. Minecraft 26.3 is the latest stable game release;

the matching NeoForge loader is currently beta. Versions are pinned for reproducibility.



## Open the project



Open this folder as a Gradle project in your IDE. Use the included Gradle wrapper,

and set both the project JDK and Gradle JVM to Java 25. Java 25 is required to run

the wrapper as well as compile the mod. No global Gradle installation is needed.

The detected local JDK is `C:\Program Files\Zulu\zulu-25` (Zulu 25.0.4.1).

The build does not change your system Java settings or download a replacement JDK.



## Build and test milestone 1



Run these commands in PowerShell from the project folder. The initial build

downloads Gradle, Minecraft, and NeoForge dependencies and can take a while.



```powershell

.\gradlew.bat --version

.\gradlew.bat build

.\gradlew.bat runClient

```



Expected wrapper output: Gradle 9.2.1 and JVM 25. Expected build output:

`BUILD SUCCESSFUL`, with `build/libs/raft_ocean-0.9.0.jar` produced.



In the client, open Mods and verify that **Raft Ocean 0.9.0** is listed. Create a

temporary vanilla world, enter it, save, and return to the title screen. The log

at `run/client/logs/latest.log` should contain:



```text

Raft Ocean: common setup complete.

Raft Ocean: client setup complete.

Raft Ocean: server starting; common entry point loaded.

```



Next, in a second PowerShell terminal:



```powershell

.\gradlew.bat runServer

```



The first server launch normally exits to request acceptance of the Minecraft

EULA. Read the linked EULA in `run/server/eula.txt`. If you agree, change its

`eula=false` to `eula=true`, save it, and run `runServer` again. EULA acceptance

is left to you.



Expected server result: the console reaches `Done`, and

`run/server/logs/latest.log` contains the common setup and server starting

messages. It must not contain the client setup message. Type `stop` in the server

console to shut it down cleanly.



For a multiplayer smoke test, keep the server running, open the development

client, and connect to `localhost:25565`. If the development client's test profile

is rejected by online authentication, stop this temporary local test server and

set `online-mode=false` and `server-ip=127.0.0.1` in

`run/server/server.properties`, then restart. These settings are for a local test

only. Confirm that you can enter the world, walk, disconnect, and reconnect.



Client and server use separate working folders, so their settings and worlds do

not overwrite one another. No existing launcher worlds are used.



## Milestone 1 checklist



- [ ] Wrapper reports Gradle 9.2.1 and Java 25.

- [ ] Build succeeds and produces the mod JAR.

- [ ] Client lists Raft Ocean and opens/saves a temporary world without a crash.

- [ ] Dedicated server reaches `Done` without loading the client subscriber.

- [ ] Development client joins the local server and reconnects successfully.



Milestone 1 client and integrated-server loading was verified from the supplied

log. A dedicated-server log was not supplied. The floating raft prototype has

been confirmed working by the user. Construction code is complete; its final

multiplayer and snap-back checks remain pending. Ocean World supplies the endless sea

and automatic starter spawning. Version 0.5.0 adds bounded floating debris, hooks,

and nets in new and existing raft worlds. Version 0.6.0 adds survival machines

and moving beds. Version 0.7.1 adds sailing, engine controls, physical turning and animated wind indicators. Version 0.8.0 adds sharks, crop pests, friendly grazers, spear/bait recipes, foundation damage/repairs and host-synchronized configuration. Research progression belongs to milestone 10. Paste the full error log if a check fails,

and report which checks passed.



## Roadmap



1. Project setup and client/server validation.

2. Moving, buoyant raft prototype with multiplayer player collision.

3. Raft building, extensions, and structural pieces.

4. Ocean World preset and starter-raft spawn.

5. Floating debris, loot, grappling hook, and collection nets.

6. Thirst, purification, cooking, fishing, farming, smelting, beds, and storage.

7. Wind, sails, paddles, steering, fueled engines, and anchors.

8. Shark combat/raft damage, bird pests, and island fauna.

9. Rare and story islands, structures, lore, receiver/antennas, keys, and puzzles.

10. Research, recipe tiers, tools/armor, diving gear, and advancements.

11. HUD, storms, sounds, configuration, English/German localization, and performance.

12. Release JAR, documentation, and changelog.



Server-authoritative raft movement, client interpolation, and automatically

updated mass/buoyancy/collision are core requirements. Moving-block collision and

custom world generation are the highest-risk areas and require dedicated

multiplayer testing before expanding the feature set. Recipes, loot, structures,

and tags will use data-driven resources and datagen when introduced.



## Sources and licensing



- Game releases: https://piston-meta.mojang.com/mc/game/version_manifest_v2.json

- NeoForge releases: https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml

- Official template: https://github.com/NeoForgeMDKs/MDK-26.3-ModDevGradle

- Template commit used for wrapper files: `eec248ccef70bbc6fd3cf0678c04886667457dc8`

- Wrapper scripts and JAR updated to official Gradle tag `v9.2.1`; JAR checksum

  verified against the Gradle release checksum.

- NeoForge documentation: https://docs.neoforged.net/



The original mod code is All Rights Reserved pending the author's release-license

choice. Upstream template/wrapper files retain their existing notices;

`TEMPLATE_LICENSE.txt` contains the NeoForged template license. The Gradle

license is preserved in `docs/GRADLE_LICENSE.txt`. The Gradle distribution

checksum is pinned in the wrapper configuration. The original project brief is

preserved in `docs/PROJECT_BRIEF.md`.

All future mod textures, models, names, lore, and sounds must be original or

appropriately licensed; no assets or text from Raft will be copied.







