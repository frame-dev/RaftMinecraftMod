# Milestone 4 — Ocean World and starter spawning (0.5.0)

Ocean World is a selectable world preset backed by an original registered chunk
generator. Layer definitions are data-driven JSON; its base generation uses the
verified 26.3 layer generator with corrected Overworld height and sea-level
metadata. The surface is Y=63 throughout the Overworld, within the world border.
Bedrock begins at -64, sand ends at 48, and source water fills 49 through 62.
The biome is ocean. Terrain features and vanilla structures are disabled.
Nether and End generation remain vanilla.

This milestone deliberately uses a flat seabed. Rare island structures, story
islands, loot, and lore arrive in milestone 9. The current empty structure list
can later be replaced with the mod's island structure sets. No land is generated
yet. Debris/hook/nets and the survival loop remain milestones 5 and 6.

## Build and create a new world

Close the previous client/server, then run in the project folder:

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
```

Verify **Raft Ocean 0.5.0** in Mods. Artifact:
`build/libs/raft_ocean-0.5.0.jar`.

1. Choose Singleplayer → Create New World.
2. In the World tab, cycle World Type to **Ocean World**.
3. Create a new survival test world. Cheats are optional; spawning needs no command.
4. After entry, you should appear on a moving 3x3 raft with a building hammer,
   8 foundations, 16 oak planks, and 8 bread.

Existing worlds retain their saved generators. Selecting this preset requires a
new world; loading a normal world does not activate ocean spawning. The layer
layout is part of the saved generator settings. Do not use a Superflat customization
screen to replace the custom generator with a different world type.

## Dedicated server

Use 0.5.0 on both server and clients. Stop the server and edit
`run/server/server.properties` for a new test world:

```properties
level-type=raft_ocean:ocean
level-name=ocean_world_test
```

Choose an unused level-name to preserve existing worlds. Changing level-type does
not convert an existing saved world. Keep the other server settings, then run:

```powershell
.\gradlew.bat runServer
```

Follow the existing milestone 1 EULA/setup instructions if this is your first
server launch. Each new player gets a separate raft, normally spaced 16 blocks
apart. First entry works without operator privileges.

## Spawn and persistence rules

- First entry creates and remembers one raft for that player's UUID. Starter
  supplies are given once.
- Normal reconnect keeps the player's saved position and inventory. It does not
  pull swimming/exploring players back to the raft or duplicate starter supplies.
- Default death respawn returns to the player's remembered raft at its current
  location. A working bed or forced respawn keeps its normal behavior; returning
  from the End is not redirected. Hardcore spectators are not given new rafts.
- Ownership lives in world SavedData, separate from player death data. Moving
  rafts refresh their saved reference position every second while ticking.
- If the remembered raft is unloaded, the player is moved near its last saved
  position so normal chunk loading can recover it. A three-second grace period
  precedes recovery of a missing raft.
- If the raft is gone, an empty recovery raft is created nearby without refilling
  the kit. If the built raft has no clear standing surface, it is preserved and
  the player is assigned a separate empty recovery raft.
- Placement searches a bounded water area and checks terrain, border, and other
  rafts. Failure reports an English message instead of overwriting blocks.

## In-game acceptance checklist

- [ ] Ocean World appears in world creation. A fresh survival world starts you
  standing on a 3x3 raft at the water surface, without commands or drowning.
- [ ] Look around and travel in several directions: ocean continues, with no
  vanilla mainland, villages, or terrain-generated islands. F3 surface is near 63.
- [ ] Verify the one-time hammer, 8 foundations, 16 planks, and 8 bread. Acquire
  the planks to see the building recipes in the recipe book.
- [ ] Extend the raft and build an upper floor. Walk/jump while drifting; contact
  and carry should work as before. Retest the earlier intermittent snap-back.
- [ ] Swim away, save/quit, and reconnect. Position and inventory should persist;
  no second starter raft or replacement supplies should appear.
- [ ] Let the raft drift, then die. Default respawn should board that same raft
  with its buildings intact; no starter kit is refilled.
- [ ] Leave/reload the world and repeat death respawn. The reference should survive
  reload and resolve an unloaded moving raft.
- [ ] In a disposable test world, remove your raft with `/raft remove`, then die.
  A new empty recovery raft should appear after the loading grace period.
- [ ] Repeat with two clients: each first spawn has a separate raft; building,
  movement, respawn, and reconnect do not alter the other player's ownership.
- [ ] Open an existing normal world: its generator and spawn behavior stay normal.

If a check fails, report the action and expected/actual behavior and attach
`run/client/logs/latest.log` plus the dedicated-server log when applicable.
For movement failures, also capture `/raft support` and `/raftsupport`.

## Automated validation and limits

The isolated development Ocean World server checks actual generated blocks and
biomes at five widely separated coordinates, starter entity registration and
boarding, one-time kit grants, reconnect behavior, ownership codec round-trip,
moving-raft death respawn, and missing-raft recovery. Its fake test player records
teleports; it does not simulate a connected client's loading/teleport acknowledgments
or unloaded-entity timing. Those paths require the live checks above.

The previous carry/construction regression suites continue to run separately.
No user worlds are used by the isolated test servers.
Build and isolated startup/save/reload pass: 91 standalone regression checks,
725 runtime carry assertions, 208 runtime construction assertions, and 43 runtime
Ocean World assertions. Client and connected multiplayer acceptance remain pending.

There are no islands or renewable survival resources yet. Rafts pause when their
chunks stop ticking; offline drift remains deferred. Physical raft rotation/tilt
and the prior snap-back report still require their own implementation or live
verification. Wait for milestone 4 test results before starting milestone 5.

The 0.4.2 storage/tool station gap fix is covered in [RAFT_STATIONS.md](RAFT_STATIONS.md).
Run that checklist as well before accepting milestone 4.
