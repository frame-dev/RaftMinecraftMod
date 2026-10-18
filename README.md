# Raft Ocean — 0.13.5 preview

This update adds [wind-limited sails and foundation-load engine handling](docs/SAILING_AND_ENGINE_LOAD.md).
Extra sails share their drive instead of stacking speed. Engines show capacity,
slow down under heavy load and stall when overloaded; six powered engines support
large rafts. Your original 1.5/1.0/1.8 m/s engine/sail/paddle base speeds are retained.
The expedition notebook, cooperative building and open-ended story remain available.

The [addon API v3](docs/API_V3_UTILITIES.md) supports utility
inputs, preflighted processing, generator demand and opt-in output collection.
A standalone starter SDK and three complete addon examples are available.

An original Minecraft Java ocean-survival mod with a moving, extendable raft,
floating salvage, survival machines, sailing, cooperative exploration and an
open-ended story. Models, materials, lore and sounds are original; no Raft
game assets or text are copied.

## Requirements

| Component | Pinned version |
| --- | --- |
| Minecraft Java | 26.3 |
| NeoForge | 26.3.0.48-beta |
| Java | 25, 64-bit |
| Optional JEI | 31.9.0.58 for 26.3/NeoForge |
| Development Gradle | 9.2.1, included wrapper |
| ModDevGradle | 2.0.147 |

These are the existing project targets. Fabric and older Minecraft versions
are not supported by this build.

## Install and start

Install `raft_ocean-0.13.5.jar` in the matching NeoForge profile's `mods`
folder. Keep exactly one Raft Ocean jar installed. Sources jars are for
developers. Use the same version on every multiplayer client and server.

Create an **Ocean World** to start on a raft. Collect floating debris with
the hook, expand foundations, and build the basic wooden purifier and grill
before advancing to metal machines. Wet bricks dry on a Sun-Drying Rack in
sunlight; research unlocks later recipes. JEI is installed separately.

See [installation and multiplayer](INSTALLATION.md), [changes](CHANGELOG.md)
and [known limitations](KNOWN_ISSUES.md). Existing 0.11.41 worlds retain their
save format. Larger islands appear only in previously ungenerated chunks.

The [Raft gameplay comparison](docs/RAFT_GAMEPLAY_REFERENCE.md) records verified
reference mechanics, remaining gaps and the next implementation priorities.

## Included gameplay

- Foundations, edge nets, upper floors, pillars, stairs, ladders, straight/
  corner/curved walls, doors, roofs and placement previews.
- Moving crafting/tool stations and saved storage, including double chests.
- Thirst, cups, purification, rain collection, cooking, fishing, crops, tree
  planters, sugar cane, kelp, smelting and sun-dried bricks.
- Paddles, wind sails, precise steering, fueled forward/reverse engines,
  disposable/permanent anchors, wind indication and navigation HUD.
- Sharks with delayed foundation attacks and cooperative repelling, crop
  pests, island fauna, difficulty modes and Peaceful behavior.
- Larger islands, underwater resources/dive sites, trading outposts, seven
  story destinations, journals and receiver signals. The story is open-ended.
- Research, tiered crafting, diving/first-aid gear, batteries, charging,
  water/fuel/power distribution and automation machines.
- Cabin furniture, larger beds, lamps, tables and mounted decorations.
- Shared-raft co-op, crew permissions, shared research and teammate rescue.
- English/German localization, original sounds and addon API v3.

## Configuration and commands

Gameplay: `config/raft_ocean-synced.toml` on the host/server.
Cosmetics: `config/raft_ocean-client.toml` on the client. Stop the instance
before editing its files. See [configuration](docs/CONFIGURATION.md) and
[expanded settings](docs/EXPANDED_CONFIG.md).

Operator commands include `/raft info`, `/raft support`, `/raft spawn`,
`/raft board`, and `/raft research unlock` (or `/raft research unlock @a`).
The research command discovers all projects without changing game mode.

## Build from the project checkout

Open this folder as a Gradle project and select Java 25 for the project JDK
and Gradle JVM. Use the wrapper. Close the development client before rebuilding.

```powershell
./gradlew.bat build
./gradlew.bat runClient
```

Output: `build/libs/raft_ocean-0.13.5.jar`. Development client launches install
the pinned JEI build for testing. The first build downloads dependencies;
cached builds can use `--offline`. Server launch: `./gradlew.bat runServer`.
Read `INSTALLATION.md`; Minecraft EULA acceptance remains your choice.

Isolated verification protects a running development client's output folders:

```powershell
./gradlew.bat -I tools/isolated-visual-build.gradle build runBootstrapSmoke runOceanSmoke --no-parallel -x downloadAssets
```

Use a new `level-name` in the isolated ocean test server's properties for a
fresh generation test; never remove a gameplay world.

Build the [example addon](examples/raft-addon/README.md) after the main mod.
See [API v3](docs/API_V3_UTILITIES.md), [machine guide](docs/MACHINE_ADDON_GUIDE.md)
and [compatibility](docs/MOD_COMPATIBILITY.md).

The release ZIP includes addon source examples for reference. Building them
requires the full project checkout; the sources jar alone is not a Gradle project.

## Verification and playtesting

See `RELEASE_VERIFICATION.md` for this package's actual results. Automated
checks cover collision/packets, construction, crafting, machines, survival,
co-op, generated islands, models, inventory fit and localization.

Playtest walking/jumping/stairs during drift and turns, bed/seat surfaces,
machine interactions, engine reversal, save/reload and two-player co-op.
Use a fresh Ocean World to inspect islands and story routes. Live visuals,
real network delay, large builds and third-party packs still need testing.
Passing automated checks does not establish a bug-free release.

Project license: **All Rights Reserved**. See `LICENSE_NOTICE.txt`.
Historical notes: [development archive](docs/DEVELOPMENT_HISTORY.md).
