# Cooperative rafting and repeated shark attacks — 0.11.3

A shark now stays at the selected exposed foundation for repeated contact bites.
Each cycle opens the jaw, checks wood contact, applies damage, then allows twelve
recovery ticks before the next windup. It retreats after ten bites or when that
foundation breaks. New configs default to ten damage per bite: a full-health
foundation takes ten successful Normal-mode bites to break, unless repaired.
Difficulty scales damage; Easy causes less damage and Hard causes more. Existing
custom values are retained, so an older value of 25 breaks a foundation sooner.

During a foundation attack, successful player hits are counted across the crew.
The first two hurt the shark but do not cancel its target. The third makes it
retreat and pauses new attacks. Minecraft's damage cooldown still applies: an
ignored hit does not count. Player-owned projectile hits also count. Bait,
Peaceful, disabled aggression/damage and ordinary swimmer combat retain their
previous rules. Repel count resets for a new attack; dying still kills the shark.

Host synced config:

```toml
[creatures]
foundationBiteDamage = 10
foundationBitesPerAttack = 10
sharkRepelHits = 3

[multiplayer]
sharedStarterRaft = true
```

Edit existing sections rather than appending duplicates. foundationBitesPerAttack
accepts 1–20; sharkRepelHits accepts 1–10. sharedStarterRaft defaults to false so
existing worlds keep their individual homes. Restart the host/world after edits.
A full example is docs/config-examples/presets/coop-synced.toml.

## Same-raft cooperation

Multiplayer raft movement, building, storage and processing already run on the
server. This update adds explicit crew home selection in Ocean World:

- With sharedStarterRaft enabled, first-time players board the designated shared
  raft, including its saved moving location. Existing player homes are retained.
- Without operator permissions, use /raftcrew join while on or within eight blocks
  of a safe raft. It boards that raft and makes it your default moving home.
- /raftcrew info reports its saved crew size, including offline members.
- Default death respawn follows the shared raft. Beds can still set personal
  respawn positions. Joining another raft clears your old moving-bed reference.
- Each first-time player gets starter supplies once. Joining, death and reconnects
  do not duplicate them. Normal reconnects keep the saved player position.
- Shared homes persist through saves. If a crew's raft is gone, recovery updates
  all its recorded members to one replacement, even with automatic sharing off.

There are no crew invitations, private build permissions or locks. Joining a
nearby raft is public. This feature does not add remote teleportation, keep
unoccupied chunks ticking, share personal research or merge already-built rafts.
If a shared raft has no safe boarding space, entry reports failure; enlarge or
clear its deck. It does not split that crew onto separate fallback rafts.

Use matching 0.11.3 clients/server, the Ocean World preset and the same gameplay
config on the host. Friends connect through the ordinary LAN or dedicated-server
multiplayer flow. Artifact build/libs/raft_ocean-0.11.3.jar. Restart installed
Minecraft; close and rebuild normally for development runs.

## Verification and local tests

Native server regressions exercise ten actual damage events, third-hit retreat
with hits split between two players, damage stopping during retreat, shared
first join, reconnect supply protection, save codecs, nearby/far command boarding,
death without supplies and shared recovery after raft loss. Existing foundation
jaw-contact, movement, stations, research, difficulty and bait tests are retained.
Build/model/packaged locale/audio/preset audits also run.
Log build/coop-shark-burst-build.log.

- In Normal mode with ten damage configured, watch ten jaw-close impacts break
  an unprotected full-health foundation. Repair or remove the target mid-attack.
- Hit the attacking shark three times with valid cooldown spacing. Share those
  hits between players. Expect the first two to leave its attack active and the
  third to make it swim away without further foundation damage during retreat.
- Enable shared starter entry; have a new player join. Build, steer, use storage
  and machines together, then test death, reconnect and server restart.
- Use /raftcrew join for an existing player; check /raftcrew info and bed behavior.
- Check both players while drifting/turning and on upper floors under real latency.

Live connected multiplayer, bite visuals under latency, conservative rotated
colliders and previous movement/crash reports still need local verification.
The six-destination story remains open-ended. Milestone 11 continues.
