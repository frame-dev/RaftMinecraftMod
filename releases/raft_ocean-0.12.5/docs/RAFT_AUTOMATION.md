# Raft automation — 0.11.8

Nine new raft buildables and a biofuel item have original colored models, fittings,
ports and inventory silhouettes. Water uses teal, fuel uses coral, and power uses
brass. The moving water tank has an animated level gauge; working sprinklers show
water jets and the generator has a pulsing indicator. These are original designs.

This expands milestone 11. The existing story remains open-ended.

## Start using it

Use the same 0.11.8 jar on the server and every client. This release changes the
movement/menu protocol to `raft-movement-10.3`; older mod clients cannot join.

Research **Metal fabrication** for tanks, pipes and the biorefiner; research
**Radio receiver** (the existing electrical research project) for the generator, cable, electric purifier and sprinkler.
The existing research projects unlock the added recipes without resetting progress.
JEI shows all building recipes, a sixth processing category for the biorefiner, and
machine usage notes. Automation works without JEI on clients or dedicated servers.

Place the machines on foundations or supported floors. Put conduits in the raft
cells between them. Connections use the six face-adjacent cells, so pipes and cables
can go around corners or climb to another deck. Matching machines can also connect
directly when adjacent. Diagonal connections do not work. Water, fuel and power stay
separate even where different kinds of pipe are adjacent.

**Fuel setup:** put raw potatoes, carrots, beetroots, kelp, apples, cod or salmon in
the biorefiner's Organic slot. One honeycomb in Sweetener produces two biofuel
canisters in 30 seconds; one sugar produces one in 45 seconds. Processing speed
configuration also applies. No heat or water is required. Full output pauses the
batch without consuming its ingredients. Put the biofuel in a fuel tank's input
slot, then connect fuel pipes to engines and/or generators. Pipes transfer one
canister per recipient per second and preserve any incompatible fuel already
inside an engine. An engine burns one biofuel for 6000 ticks by default, modified by
the existing engine fuel setting. Existing wood fuel remains usable.

**Water setup:** connect a biofuel generator to an electric purifier with power
cables. The purifier requires loaded surface water beneath its column and takes
five seconds per cup by default. It stores four fresh-water cups. Connect water
pipes to a fresh-water tank, which stores 64 cups, then to planters, tree planters,
a cooking pot or sprinklers. Ordinary purifiers and rain collectors can feed this
network too. Water moves as conserved cup units, one per eligible recipient per
second. Existing machine reservoirs remain four cups.

**Irrigation:** a sprinkler needs fresh water and connected generator power. It
waters one nearby planter or tree planter per second, on the same deck and within
two cells horizontally in each direction by default. It stops when nearby
reservoirs are full. Crops still require the normal seeds/mix/light conditions.
Connected water pipes can also refill a planter directly, without a sprinkler.

The generator burns a canister only when its power network has demand. One canister
provides 2000 energy by default, generated at 20 energy per second. The internal
energy bank stores 2000. The purifier spends eight energy per processing second;
a sprinkler spends four per delivered irrigation cup. Insufficient total power
pauses the consumer without spending partial energy or water. The generator screen
shows stored energy, remaining canister fuel, progress and network status.

Empty cups draw fresh water from tanks, electric purifiers and sprinklers; fresh
cups pour into them. Sneak-use opens the screen instead. The cup input slot also
exchanges cups into the filled/empty output slots. Full cup outputs prevent the
exchange; no cup is deleted. Supplies tabs retain the normal 27-slot storage.

## Crafting patterns

Rows are read from top to bottom. JEI and the native recipe book show these too.

| Buildable | Pattern | Materials | Yield |
|---|---|---|---|
| Water pipe | ` M ` / `MMM` / ` M ` | M copper ingot | 4 |
| Fuel pipe | ` M ` / `MGM` / ` M ` | M iron ingot; G binding paste | 4 |
| Power cable | ` W ` / `WLW` / ` W ` | W copper wire; L cordage | 4 |
| Water tank | `MMM` / `MBM` / `MMM` | M copper ingot; B timber brace | 1 |
| Fuel tank | `MMM` / `MBM` / `MMM` | M metal plate; B binding paste | 1 |
| Biorefiner | `CMC` / `MBM` / `FFF` | C copper; M metal plate; B barrel; F scrap fasteners | 1 |
| Biofuel generator | `MWM` / `IEI` / `FFF` | M metal plate; W copper wire; I iron; E raft engine; F scrap fasteners | 1 |
| Electric purifier | `CMC` / `WPW` / `FFF` | C copper; M metal plate; W copper wire; P raft purifier; F scrap fasteners | 1 |
| Sprinkler | `MCM` / ` W ` / `FBF` | M copper; C water pipe; W copper wire; F scrap fasteners; B timber brace | 1 |

## Configuration

Add or change these values in the existing synced raft configuration. Registrations
and recipes remain present when automation is disabled.

```toml
[automation]
enabled = true
biofuelEngineTicks = 6000
energyPerBiofuel = 2000
purifierEnergy = 8
electricPurifyTicks = 100
sprinklerRadius = 2
sprinklerEnergy = 4
```

`enabled` controls networks, electric purification and sprinklers. The unpowered
biorefiner still processes its data-pack recipes. Energy and water capacities are
fixed save-safe limits in this version. Existing processing and engine fuel
multipliers remain available.

## Validation and local test checklist

Native server regression tests cover both refinery recipes, delayed input commit,
full outputs, conservation of water/fuel/power, incompatible engine fuel, network
breaks and wrong conduit types, actual surface-water purification, biofuel engine
burn time, sprinklers, disabled networks and entity save/load. Existing native
inventory tests now cover all new machine screens, right-click and shift-click,
separate co-op viewers and invalidated station reach. Original model assets are
parsed by the pinned Minecraft model parser and checked for inventory fitting,
coverage and texture references. OpenGL rendering and connected human co-op latency
still need local testing.

1. Craft/research the nine buildables and inspect their models, ghosts and JEI links.
2. Refine sugar + potato and honeycomb + kelp; expect one and two canisters.
3. Connect a fuel tank through a fuel pipe to a running engine; expect its fuel to
   increase without replacing any wood already in the input slot.
4. Run generator → power cable → electric purifier above water; connect the purifier
   through water pipes to a tank. Expect gradual filling up to 64 cups.
5. Connect water and power to a sprinkler beside a seeded/mixed planter. Expect water
   jets while irrigating, and no water consumption once all nearby reservoirs fill.
6. Disconnect a pipe/cable and fill outputs. Expect paused work and preserved inputs.
7. Save/reload and sail/rotate the raft. Expect stored fluids/fuel/energy and the same
   local connections. Have two co-op players open a machine and check shared contents.

## Current limits

Each conduit occupies one raft building cell; different utilities need separate
routes and cannot share a station cell. Ports are static four-way models and do not
hide unused connections. Fuel pipes transport actual biofuel items, not a separate
liquid simulation; the biorefiner's output must be moved into a tank manually.
Connections do not span separate rafts. Networks update once per second only on
loaded rafts, without offline production or chunk tickets. Removing a tank loses
its stored water; removing a generator loses stored energy. Ordinary item contents
follow existing station drop behavior. The sprinkler refills planter reservoirs;
it does not hydrate terrain farmland. Electrical grill/smelter upgrades and battery
charging are not included in this set.

The fuel/water automation mechanics are inspired by the developer's public
[Raft Chapter 2 announcement](https://store.steampowered.com/news/posts/?appgroupname=Raft&appids=648800&enddate=1604069783&feed=steam_community_announcements).
No game assets, models, textures or story text are copied.


Version 0.11.9 adds [bug fixes and shared Pause/Resume controls](AUTOMATION_BUG_FIXES.md). Use the newer jar on all clients and the server.
