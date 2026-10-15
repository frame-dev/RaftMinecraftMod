# Milestone 9 — islands and exploration (0.9.5)

Current expansion: [0.9.14 adds Ember Beacon, Tideworks Haven and Starfall Archive](STORY_EXPANSION.md).
The original three chapters now lead into a six-location receiver journey.
Use the expansion guide for the current artifact, route and test checklist.

This build adds four original island templates through native structure generation,
three story locations, journals, a receiver/antenna crafting chain and a relay-seal
puzzle. It also adds a configurable foundation limit and JEI for the development client.
Version 0.9.4 adds [six ordinary islands of different sizes](ORDINARY_ISLANDS.md)
through a separate structure set. The original story-island weights remain intact.

## Build and launch

Close the previous client/server and run:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Zulu\zulu-25'
.\gradlew.bat build
.\gradlew.bat runClient
```

The artifact is `build/libs/raft_ocean-0.9.5.jar`. Use matching 0.9.5 clients/server;
the movement protocol is `raft-movement-9.5`. Minecraft 26.3, NeoForge 26.3.0.48-beta,
Java 25, ModDevGradle 2.0.147 and Gradle 9.2.1 remain pinned.

## Foundation limit

Open **Mods → Raft Ocean → Config → Building → Foundation limit**, or edit the
gameplay TOML while the world/server is stopped:

```toml
[building]
foundationLimit = 256
```

Valid values are **9–1024**, default 1024. Edit the existing section rather than
adding a duplicate. This limits new foundations on each raft. Setting 64 recreates
the earlier small-deck cap. Lowering the setting preserves existing large rafts and
their saved contents; remove foundations to get below the cap before extending again.
The 1024 save/network ceiling, ±32 coordinate bounds, 512 building pieces and
16 grid-height levels still apply. See [configuration](CONFIGURATION.md).

## JEI testing

`runClient` first runs `installClientJei`, which resolves the pinned NeoForge 26.3
JEI **31.9.0.58** and copies its full distribution into `run/client/mods`.
It includes the required Mezz Config dependency in its embedded JAR metadata.
No JEI classes are required by Raft Ocean; the release JAR and dedicated-server
development runs do not include it. You can install it immediately with
`gradlew.bat installClientJei` without starting Minecraft.

In-game, open inventory and look for JEI's searchable item list. Hover an item
and press **R** for recipes or **U** for uses. Ordinary crafting recipes, including
the receiver chain, use JEI's native recipe support. Custom survival-processing
categories and automatic transfer into raft station menus are not integrated yet;
use the machine screens for their current processing controls.

Sources: [official JEI development setup](https://github.com/mezz/JustEnoughItems/wiki/Getting-Started-%5BMinecraft-26.1.2-and-26.2%5D)
and [published 26.3 NeoForge versions](https://maven.blamejared.com/mezz/jei/jei-26.3-neoforge/maven-metadata.xml).

## Islands

Ocean World now selects one potential island per 32×32-chunk region, with a
16-chunk separation setting. Drift Atolls have weight 3 and each story island
weight 1. The actual pattern depends on seed; islands are not evenly spaced.
A 160-block radius around the world origin remains protected for starter spawning.
Generation requires Minecraft's world structure-generation option.

| Location | What to explore |
| --- | --- |
| Drift Atoll | A low beach, trees and supplies for planting and building. |
| Glasswatch | A timber lookout with a ladder, elevated glass shelter, supplies and Watch Log. |
| Garden Relay | A greenhouse with mature crops, Garden Log and relay key. |
| Echo Vault | A masonry archive, three switches, relay seal, Vault Log and rewards. |

The terrain joins the sand seabed, with irregular sloping shores and sparse
wildflower groundcover away from story buildings. Drift Atolls retain their
level story clearing and supplies. Structure pieces respect native chunk bounds,
persistence and `/locate structure raft_ocean:<name>`.
There is no retroactive placement in finished chunks. Existing Ocean World saves
gain the structure set even if their older generator settings had an empty override
list, but you must sail into unexplored areas. No regeneration of your raft is needed.
New island generation is restricted to Ocean World; other dimensions retain their
previous generation. Suitable island land also lets the milestone 8 grazers spawn.

## Receiver and journey

These are handheld navigation tools, usable aboard a moving raft. The recipe rows
below run top to bottom. F = scrap fasteners, I = iron ingot, W = woven mesh,
B = timber brace; `-` is empty.

| Item | Recipe |
| --- | --- |
| Radio Receiver | FFF / IWI / BBB |
| Antenna Upgrade | -I- / -I- / FWF |
| Long Range Receiver | Receiver + Antenna Upgrade, shapeless |

Right-click the receiver to scan for the next story island. Its chat readout gives
the name, X/Z coordinates, distance and hull-compatible heading: 0 south, 90 west,
180 north, 270 east. The basic receiver searches three placement-region rings,
the upgraded receiver six. Scans cache for 60 seconds unless you move more than
128 blocks, change dimension or advance the journey. A first scan may briefly
pause while native structure starts load; searches remain bounded.

The order is **Glasswatch → Garden Relay → Echo Vault**. Right-click each journal
to read its original lore in chat and advance your saved journey. Journals are
not consumed. Progress belongs to each player and persists through saves and
death. Exploring a later island directly can advance the journey without the
earlier journals; cooperative crews can share journal items.

At the vault, face the seal and set **left ON, middle OFF, right ON**. Right-click
either half of the seal with the relay key. The two-block opening stays open, and
one key is consumed in Survival. A wrong pattern or wrong item consumes nothing.
The Garden Log contains the clue. The seal itself resists mining; ordinary native
masonry can eventually be mined, so this is a simple exploration puzzle rather
than a protected adventure map. Opening the archive and reading its journal
completes this short original story.

## Test checklist and limits

- Change the foundation cap to your preset, restart the world, extend to that cap
  and try one more placement. Expect a limit message and no item consumed.
  Lower it below an existing raft's size, save/reload, and confirm it stays intact.
- Confirm JEI in Mods and its inventory list. Check receiver/antenna crafting
  recipes with R and uses with U.
- Use a new Ocean World or sail into unexplored chunks of an existing one.
  Scan for Glasswatch, follow the readout and collect/read its journal. Test the
  elevated ladder and chest, then repeat at Garden Relay.
- Take the relay key to Echo Vault. Try a wrong pattern first, then the clue's
  pattern. Expect no lost key on failure and one consumed on successful opening.
- Save/reload the journals, journey, opened seal, island chests and raft storage.
  With matching clients, test separate player progress and shared island loot.
- Dock near an island, check raft/terrain collision, building and grazer spawning.
  Retest prior carry snap-back and inventory-loss reports.

Island shapes and buildings are original template prototypes with fixed orientation;
they do not add a full biome system, bosses or bespoke puzzle GUI. Native Minecraft
blocks supply the terrain and buildings; the mod's seal, icons and lore are original.
Live visuals, JEI interaction and connected multiplayer still need your test.
Milestone 10 research and equipment progression has not started.
