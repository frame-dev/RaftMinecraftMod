# Milestone 5 — floating salvage, hooks, and nets (0.5.2)

Seven floating salvage kinds are implemented: planks, logs, barrels, crates,
scrap, leaves, and bottles with original notes. They float on exposed water in a stream ahead of the raft. Each piece keeps a
fixed drift direction, approaching and then passing behind uncollected; it does
not chase the raft or gather at its center. Rafts near active
players receive salvage in any dimension with suitable water, including an
existing Ocean World. No new world is required.

## Build and launch

Close previous clients/servers, then run from the project folder:

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
```

Verify **Raft Ocean 0.6.0** in Mods. Use this version on every connected client and
server: `build/libs/raft_ocean-0.6.0.jar`. The required protocol rejects older
clients without the net piece kind. Existing raft construction and inventories
remain load-compatible. The broader request for all vanilla blocks/stations is
pending; the user explicitly chose to proceed with this milestone.

## Collect by hand or hook

Right-click nearby floating salvage to collect it. Collection is server-controlled
and limited to four-block distance. This also works while swimming. Barrel/crate
contents come from data-driven loot tables; bottles contain a named note whose
two lore lines are readable in the inventory tooltip. Rolled contents save with
the debris, so a full net or world reload cannot reroll its loot.

Craft a **Wooden Salvage Hook** in the inventory 2x2 grid (P = any planks; S = stick):

```text
P P
  S
```

Hold the hook and right-click open water to cast. Aim toward visible debris.
Right-click again to reel back. The hook catches one floating debris entity,
then pulls it to your current position, even while your raft moves. Empty casts
also reel back. The hook is a salvage tool; it does not pull players or mobs.
One active cast is allowed per player, including across the two tiers/hands.

The wooden hook has a 24-block tether limit, 0.32-block/tick reel speed, and
128 durability. Craft one hook + one metal hook head + one binding to obtain
a **Reinforced Salvage Hook**: 40-block tether, 0.5-block/tick reel speed, and
384 durability. Each cast costs one durability in survival. Actual flight distance
depends on aim and water contact; exceeding the tether limit starts reeling.
The upgrade is shapeless and fits in the inventory grid.

Keep the casting hook in its original hand. Switching away, dying, changing
dimension, disconnecting, obstruction, or the 30-second cast timeout cancels the
cast and releases attached debris. Casts are temporary and not saved. Inventory
overflow from a successful hand/hook collection drops at the player through
vanilla handling. Claiming prevents other hooks or nets taking that same debris.

## Collection nets

Use the raft crafting table to craft a **Raft Collection Net**:

```text
B C B
C M C
B C B
```

B = timber brace, C = cordage or string, M = woven mesh. See the
[crafting chains guide](CRAFTING_CHAINS.md) for intermediate materials and all
updated building recipes. String, leaves, scrap, and iron nuggets
can be salvaged from the debris/containers. Place a net on a foundation, ideally
along the raft's outer edge; stand beside its target cell. It adds 4 structural
mass and uses normal raft support, buoyancy, collision, save, and sync handling.

Each net owns a shared 27-slot inventory. Every half-second it checks passing
unclaimed debris within roughly one block horizontally and near its height.
The entire rolled payload must fit before anything is transferred. Full nets
leave debris floating. Empty-hand right-click opens the inventory; shift-click
transfers and multiplayer access use the native storage menu. Crouch + hammer
removes/refunds the net and drops its contents once. Storage survives reload and
chunk unload. Upper-floor nets mount flush but cannot reach distant water below.

## Spawn and performance limits

The spawner runs once every five seconds and tries up to four water positions per
nearby raft, 18–30 blocks ahead of its origin. It requires loaded, entity-ticking
chunks and never requests chunk tickets. Limits: 24 incoming pieces in the current forward corridor, and 128 loaded
debris per dimension. Passed pieces and old streams flowing the opposite way
no longer block the incoming cap; all loaded pieces still count toward the global
cap, including hooked debris. Multiple nearby players share each raft's spawning pass.

Newly activated streams wait 200 ticks (10 seconds) before spawning. A course
change of at least about 25 degrees, including gradual turns accumulating that
angle, or a displacement over eight blocks between one-second observations
restarts that delay. Ordinary forward travel does not. The five-second spawn
schedule and available entity-ticking water can add time before the next batch.
Existing debris keeps its original drift while the new route settles; it never
rotates to chase the changed raft. Naturally spawned pieces clean up after
passing more than 24 blocks behind their source raft along their original stream.
Older 0.5.0 debris chooses one fixed drift on its first water tick in this update.
Fixed drift and source raft ID save with the debris.

Debris expires after 6,000 game ticks (five minutes). Elapsed game time while its
chunk was unloaded counts toward expiration on reload. It also cleans up after
ten seconds without exposed water, or periodic checks finding no player within
128 blocks. Offline wall-clock time does not advance the game clock. Spawn rates
and limits are constants now; user configuration belongs to milestone 11.

## Test checklist

- [ ] Join Ocean World, stand on the raft, and wait 5–30 seconds. Expected: a
  varied stream appears ahead, approaches, and passes behind if uncollected.
- [ ] Leave one piece uncollected. Expected: it passes your raft without circling
  or turning back. Change course with `/raft current -0.02 0` or another direction;
  expected: new spawning pauses, then appears ahead of the new course after the
  settling delay and next spawn cycle. Existing debris continues on its old route.
- [ ] Craft the basic hook using only the inventory grid. Cast toward a plank,
  then reel. Expected: one loot pickup, hook durability decreases once per cast,
  and the hook/placeholder rope appear on connected clients.
- [ ] Reel while walking/drifting; try an empty cast and swapping away from the
  hook. Expected: retrieval follows you; cancellation leaves debris intact.
- [ ] Collect barrels, crates, and bottles. Expected: randomized supplies, original
  readable bottle notes, and no duplicate pickup from repeated interactions.
- [ ] Craft/test the reinforced hook. Expected: longer tether and faster reeling.
- [ ] Place a net near the outer edge; steer its route with the existing test
  current command if desired. Expected: passing debris goes into its shared menu.
- [ ] Fill a net completely, then pass a container over it. Expected: no partial
  transfer; free space and collect the same complete payload. Save/reload should
  preserve net cargo and any already rolled debris contents.
- [ ] With two clients, target the same debris and open the same net. Expected:
  one owner/pickup and synchronized shared cargo. Remove an occupied net while
  its menu is open; expected: menu closes and cargo drops once.

For a controlled Creative test, use these commands at the water surface:

```text
/give @s raft_ocean:grappling_hook
/give @s raft_ocean:reinforced_hook
/give @s raft_ocean:raft_collection_net
/summon raft_ocean:debris ~4 ~-1 ~ {Kind:"BARREL"}
/summon raft_ocean:debris ~4 ~-1 ~ {Kind:"BOTTLE"}
```

Summoning is an operator action and bypasses natural spawning caps. Valid Kind
values are PLANK, LOG, BARREL, CRATE, SCRAP, LEAVES, and BOTTLE.

## Validation and remaining limits

The build, dedicated startup, 91 standalone regressions, 725 carry checks,
212 construction checks, 48 crafting checks, 85 station checks, 105 salvage checks,
and 50 Ocean World checks pass. Salvage checks use loaded loot/recipes and real
entity save/load, complete-deposit rollback, moving net reach, exclusive claims,
both native item-use paths, durability, cancellation, and full hook reel ticks
with a registered fake player moving during retrieval. Stream checks cover cardinal/diagonal forward spawning, passage without lateral
homing, travel versus turning/teleport delays, old-flow cap exclusion, and drift
persistence. Ocean checks sample five actual distant water surfaces and run
actual debris ticks before/after passage and raft course changes. Smoke runs require success markers in their logs.

Connected-client rendering, natural spawn timing under live simulation distance,
and multiplayer race/timing behavior still require the checklist. The rope is a
segmented placeholder and models/textures are original procedural placeholders.
No hook charging, magnets, automatic chest routing, shark damage, or survival
machines are included here. Milestone 6 is now implemented; see its linked guide for the current test gate.

The current survival stations and moving beds are covered in [milestone 6](MILESTONE_6.md).
