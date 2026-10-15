# Expeditions and crew — 0.11.32

Minecraft 26.3 / NeoForge 26.3.0.48-beta / Java 25. Install the same
0.11.32 jar on every client and the server. The story remains open-ended.

## Optional expeditions

Seven original, native template structures generate in **new Ocean World chunks**.
Existing world generator settings gain the two new structure sets automatically.
Already generated terrain is not replaced. Normal Minecraft worlds are unaffected.

| Location | Expedition |
| --- | --- |
| Kelp Observatory | Copper-roof survey camp, clay deposits, reef resources and survey journal |
| Basalt Refuge | Rocky basalt coast, abandoned workshop, exposed iron and refuge ledger |
| Orchard Cove | Wooded, sheltered coast, harvestable carrots and orchard letter |
| Broken Keel | Flooded ship hull with an open hold, salvage and iron treasure |
| Coral Grotto | Rock overhang with two wide exits, colourful living coral and reef treasure |
| Drowned Court | Submerged stone courtyard, broken columns and old gold |
| Chart Vault | Flooded archive, illuminated chamber, charts and redstone |

Each chest guarantees one of three original journal items, plus randomized supplies.
Read a journal to record its discovery. Clues connect several routes; reading them
does not advance, reset or complete the six existing story chapters. No final
destination or mandatory ending is added. Resource differences use existing
Minecraft materials rather than introducing another mandatory crafting bottleneck.

Use the **Long-Range Receiver** normally to follow its selected signal.
**Sneak-use** cycles the three islands, four dive sites, then the main story.
The basic receiver retains the main story channel; optional channels require its
existing antenna upgrade. Receiver searches remain bounded and cached. A missing
signal means travel farther and try again; the seven variants are deliberately
spread across the ocean. Optional searches still work after the last main chapter.

Dive treasure sits below sea level. Equip existing breathing gear and flippers,
bring a light, and leave yourself a route back to the surface. Cavities are flooded;
these are swimming expeditions, not dry teleport rooms. New sites avoid nearby
island placement candidates, so flooded templates cannot cut through their terrain.

For testing, use native `/locate structure raft_ocean:broken_keel` (or another
location ID from `Exploration.ROUTES`), then travel to the indicated coordinates.
Existing saves may need a fresh, unexplored region.

## Crew roles and research

Owned Ocean World rafts have three roles:

- **Captain:** the first saved owner; builds, uses storage and controls, assigns roles.
- **Crew:** builds, uses storage and controls, shares research, rescues teammates.
- **Guest:** boards and walks on the raft; cannot change builds, use containers or
  control machinery while crew permissions are enabled. Guests do not share research.

Established co-op members retain crew access. Automatic shared starter entrants
are crew. `/raftcrew join` boards a nearby, loaded, safe raft as a **guest**;
rejoining your current raft preserves your existing role. Join never grants extra
starter items. A raft without saved owners stays usable. Creative players bypass
build/station restrictions, but cannot assign themselves captain through this command.

Commands:

```text
/raftcrew info
/raftcrew join
/raftcrew role <online-player> crew
/raftcrew role <online-player> guest
/raftcrew rescue <online-player>
```

Only the captain may assign roles, and the target must already have that raft as
their saved home. Demotion closes the player's current menu; server reach/access
checks reject subsequent interactions. Native stations, cup transfers, quick stash,
paddles and addon menu handles use the same access check.

Studied samples and learned research projects merge across captain/crew members.
Online members receive new discoveries immediately; reconnecting members receive
saved crew knowledge. Existing recipe-book/JEI research gates update as knowledge
changes. Disabling sharing preserves learned personal recipes. Leaving a crew does
not erase recipes already learned. Administrative research unlocks also propagate
to crew while sharing is enabled. Roles and shared research survive raft recovery.

## Rescue

When a survival player takes lethal damage and a living, online crewmate is
available in the same dimension, they become incapacitated with one health point.
The default rescue window is **60 loaded gameplay seconds**. Further ordinary
damage is blocked during that window; building, using items and attacks are blocked.
They are slowed and weakened, while moving raft support remains active.

To revive: sneak, empty your **main hand**, right-click the incapacitated teammate,
and remain within **three blocks for three seconds**. Keep sneaking with an empty
hand throughout. Moving away, standing up, holding an item, leaving the dimension,
or disconnecting interrupts assistance. The rescue command starts the same channel
and obeys the same physical checks; it cannot remotely revive someone.

A successful rescue restores six health points by default, removes the rescue
effects and gives two seconds of native damage protection. Inventory stays with
the player: rescue does not copy or duplicate items. If the timer expires, normal
Minecraft death and inventory rules apply. Void damage and `/kill` remain lethal.
Solo players retain normal death. The saved timer resumes when the player reconnects.

## Configuration

The synchronized server configuration gains:

```toml
[multiplayer]
sharedStarterRaft = true
sharedResearch = true
crewPermissions = true
playerRescue = true
rescueWindowSeconds = 60
reviveHealth = 6.0
```

`sharedStarterRaft` remains false by default; set it true for automatic co-op entry.
The other five settings have the values shown. Rescue duration range: 10–600 seconds;
revive health: 1–20. Turning permissions off lets visitors operate rafts. Turning
rescue off restores normal death; an already incapacitated player dies on the next
server tick. Existing shark/bird delays and Peaceful behavior are unchanged.

## In-game checklist

1. Explore new chunks or locate each new structure. Check swim entrances, island
   approaches, harvestable resources and chest loot.
2. Read all three journals; main story progress should stay unchanged. Cycle the
   upgraded receiver through all seven expeditions and back to the story.
3. Join a friend's raft, then have its captain promote/demote you. Guests should
   be denied building, storage, cup transfers and steering; crew should regain access.
4. Research a sample/project. Another crew member should see the discovery without
   spending another sample; reconnect to check persistence.
5. In survival, suffer lethal damage with a living crewmate online. Test a successful
   three-second rescue, interruption by walking away, and normal death on timeout.
6. Check walking and rescuing together while the raft moves and turns.

Limits: new structures require new chunks. Native journal models reuse this mod's
existing original book geometry/textures. Rescue currently uses messages and status
effects rather than a carried-body model or a lying-down animation. A real two-client
playtest is still required for live interaction and moving-raft rescue behavior.

## Verification

The build and native server checks passed: 815 ocean-world assertions (including
actual generation and chest loot), 44 crew/rescue assertions, 666 research checks,
and 259,199 crafting checks covering all 1,399 loaded crafting recipes. Existing
movement, carry, collision, navigation, survival and addon suites also passed.
Packaged checks verified 135 item models, 357 blocks, seven entity renderers,
615 matching English/German labels, and surface safety for 564 models/64 meshes.
Logs: `build/expedition-runtime.log` and `build/expedition-final.log`.
