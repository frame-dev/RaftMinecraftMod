# Milestone 2 — floating raft prototype

## Scope

One `raft_ocean:raft` entity owns a fixed map of nine deck cells. Its 3x3 deck is
0.25 blocks thick, with an original 16x16 wood-and-fastener placeholder texture.
The server simulates damped buoyancy, subtle vertical bobbing, and current drift.
The default current is X=0.020 and Z=0.008 blocks/tick, about 0.43 blocks/second.

The client interpolates tracked positions using the verified Minecraft 26.3
interpolation handler. Immediately after the raft moves or interpolates, supported
players receive platform translation on the server and local client while retaining
normal walking/jumping. Remote players
use normal Minecraft tracking. There are no passenger seats or recurring player
teleports. `/raft board` is a one-time test action.

The fixed map is reconstructed from deck version 1 on both sides. Current, wave
phase, and normal entity position/motion persist. Editable layouts and their
synchronization belong to milestone 3. The raft collides with world blocks and
other rafts, and stops at unavailable chunks or the world border.

## Build and launch

Close the old game client/server before relaunching. From the project folder:

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
```

Expected artifact: `build/libs/raft_ocean-0.2.2.jar`; Mods menu: Raft Ocean 0.2.2.
Compilation and the full Gradle build passed locally. The packaged resources and
metadata were checked; gameplay and absence of desync are unverified.

### Contact correction in 0.2.1

The user reported falling through the deck and not being carried in 0.2.0.
Support now uses both pre-movement carry and a post-movement swept landing check.
It restores clearance above the deck, clears downward velocity on contact, and
sets ground/collision flags. Jumping and leaving the footprint detach the player.
Ten standalone contact regressions pass as part of `build`; they check geometry,
not full Minecraft simulation or multiplayer behavior. Re-test the existing raft
with the updated client/server before marking the issue resolved.

### Carry timing correction in 0.2.2

The user confirmed that 0.2.1 prevents fall-through but reported that players
still do not follow the raft. Horizontal carry now runs immediately after each
raft's server movement/client interpolation, instead of inside player-tick callbacks.
The installed server source shows that ServerGamePacketListenerImpl.tickPlayer
restores the player's old position after ServerPlayer.doTick, undoing position
changes made there. Position snapshots in player support no longer control carry.
Pre/post-player callbacks only capture landing frames and resolve contact.

Sixteen geometry/contact/carry regressions now pass, including 1,200 ticks of drift
and bobbing, reverse movement, and refusing carry for jumping/swimming/off-deck
players or large network teleports. These tests are not a live game or network
simulation. Restart the client/server and re-test idle carry in-game.

### Startup recovery: Missing license

The subsequent startup log reported `InvalidModFileException: Missing license`.
The processed metadata file in `build/resources/main/META-INF/neoforge.mods.toml`
was empty even though the source template and generated file were valid. Resources
were regenerated, and Gradle now checks the processed license, mod ID, and version
before client/server launch or packaging. If the error recurs, run:

```powershell
.\gradlew.bat processResources --rerun-tasks
.\gradlew.bat runClient
```

Use the Gradle launch for this retest, so resource preparation and validation run.
If you use an IDE launch afterward, reload its Gradle project first.

## Spawn a raft

Use a temporary world with cheats enabled. Fly over open water at least 6x6 blocks
wide, hovering within eight blocks of the surface. If needed, use
`/locate biome minecraft:ocean` and teleport near the reported location.

```text
/raft spawn
/raft board
/raft current 0 0
/gamemode survival
```

Spawning searches your X/Z column for exposed water up to eight blocks above and
sixteen below you. It refuses an obstructed deck and does not replace terrain.
All commands require game-master permissions. Commands other than spawning select
the nearest loaded raft within sixteen blocks in your dimension.

| Command | Purpose |
| --- | --- |
| `/raft spawn` | Create a raft at a nearby water surface under your X/Z position. |
| `/raft board` | Move to its center after checking headroom. |
| `/raft current 0 0` | Gradually stop horizontal drift; buoyancy continues. |
| `/raft current 0.02 0.008` | Restore default current. |
| `/raft current -0.02 0` | Test the opposite X direction. |
| `/raft info` | Report cell count, position, deck height, and actual speed. |
| `/raft remove` | Remove the nearest raft; supported players will fall. |

Current components are limited to +/-0.05 blocks/tick and combined speed is
capped at 0.05. This is a prototype test control, not sailing or anchoring.

## Single-player checklist

- [ ] **Render:** nine textured cells appear, without missing textures or misplaced
  collision. You can stand at all four corners.
- [ ] **Float:** with zero current, stand still for 60 seconds. The raft settles
  at the water surface with subtle bobbing, keeping your feet on the deck.
- [ ] **Carry:** restore the default current and stand still for 60 seconds.
  The raft carries you diagonally without sliding you off or snapping the camera.
  `/raft info` eventually reports about 0.43 blocks/second.
- [ ] **Walk:** cross the deck, sprint short distances, and crouch at each edge.
  Normal movement controls and deck collision work.
- [ ] **Jump:** jump repeatedly at the center while drifting. You detach naturally
  and land if still above the deck. The raft continues moving while you are airborne.
- [ ] **Detach:** walk off into water. You stop being carried. Swim back or board.
- [ ] **Current:** reverse direction, then stop drift. Acceleration/deceleration
  is gradual; zero current eventually gives zero horizontal speed.
- [ ] **Save:** set a distinctive current, record info, save and quit, then reopen.
  The raft and its current persist; it does not drift while unloaded.
- [ ] **Obstacles:** approach a shore or temporary solid block. The raft stops
  against the obstacle instead of crossing it.

## Dedicated server and multiplayer checklist

Run in a second terminal:

```powershell
.\gradlew.bat runServer
```

Follow README EULA/local authentication instructions. The server must reach
`Done` without a client-class loading error. Connect the updated client and obtain
operator permissions if needed using `op Dev` in the server console.

- [ ] Repeat stationary and drifting walking/jumping checks in survival mode.
- [ ] Stand idle on the drifting raft for at least two minutes. No flying kick,
  repeated correction, fall-through, or `moved wrongly` warnings should occur.
- [ ] With two clients using distinct player names, stand on the same raft and
  walk/jump independently while observing one another. Neither player should
  slide off while idle, push the raft, or repeatedly snap position.
- [ ] Disconnect/reconnect. Stale support state must not carry players who left.
- [ ] Stop with `stop`, restart, and verify raft/current persistence.

A single-player log does not verify dedicated-server or two-client behavior.
Report which checks passed. For failures, include client/server `latest.log`,
whether the raft was drifting, and the action that triggered the issue.

## Known limits

- Flat, axis-aligned rectangular collision only. Physical tilt and rotation are
  deferred until flat-platform movement passes multiplayer testing. Visual tilt
  is also omitted so feet and deck remain aligned.
- Fixed layout: no holes, extensions, walls, placing/breaking pieces, or carrying
  placed blocks yet. Those are milestone 3 work.
- Buoyancy approximates calm water. No player mass, storms, damage, or deep-water
  recovery guarantee. Vertical bobbing amplitude is deliberately small.
- Rafts only tick in loaded, ticking chunks. No chunk tickets or offline drift.
- Multiplayer support is implemented but unverified in-game, particularly with
  latency or third-party movement mods. Milestone 2 remains unfinished until the
  dedicated-server movement checks pass.

## API verification

Code was checked against installed Minecraft 26.3 / NeoForge 26.3.0.48-beta sources:
Entity/EntityGetter collision, LinearInterpolationHandler, FallingBlockRenderer,
MovingBlockRenderState, PlayerTickEvent, EntityType.Builder, command permissions,
and ValueInput/ValueOutput persistence. Older-version signatures were not assumed.
