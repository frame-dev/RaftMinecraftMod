# Milestone 7 — sailing and engine (0.7.1)

The raft now supports paddles, wind-driven sails, a steering wheel, fuel-powered
engines and anchor winches. Four original raft pieces have shared, server-owned
control screens. A north-up compass HUD shows speed in blocks/meters per second, wind blowing
toward its cyan arrow, hull heading in amber, active drives and anchored state.
An original wind streamer shows that same wind direction in the world. Existing survival machines,
inventory contents, raft ownership and world saves retain their previous data.

## Build and launch

Close the old client/server, then run in the project folder (use matching 0.7.1 clients and server):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Zulu\zulu-25'
.\gradlew.bat build
.\gradlew.bat runClient
```

Verify **Raft Ocean 0.7.1** in Mods. The artifact is
`build/libs/raft_ocean-0.7.1.jar`. Use this version on every connected client and
server: movement protocol `raft-movement-7.1` rejects older clients. Minecraft
26.3, NeoForge 26.3.0.48-beta, Java 25, ModDevGradle 2.0.147 and Gradle 9.2.1
remain pinned. No world regeneration is needed.

## Craft and build

Rows are top to bottom; `-` means an empty slot. P = any planks, S = stick,
B = timber brace, C = cordage/string, F = scrap fastener, M = woven mesh,
I = iron ingot. Metal becomes available through milestone 6's smelter.

| Item | Recipe rows | Mass / durability |
| --- | --- | --- |
| Timber paddle | -S / P- | 192 strokes |
| Wind sail | MMM / BCB / -B- | 8; three blocks tall |
| Steering wheel | -F- / BCB / -B- | 4 |
| Wind streamer | -CM / -S- / -B- | 2; two blocks tall |
| Drive engine | IFI / FBF / III | 12 |
| Anchor winch | -C- / IFI / -B- | 6 |

The four installed controls and passive wind streamer appear in Construction; the paddle appears in Ocean
Salvage. Recipe-book unlocks are generated. Standard raft support, obstruction,
reserved height and buoyancy rules apply. Build more foundations if overloaded.
The sail reserves three levels; leave room above it. Floor mounting works on
upper decks. Crouch + hammer removes a piece and drops its inventory once.

## Controls

| Control | How to use | Result |
| --- | --- | --- |
| Paddle | Stand on the raft, look horizontally, right-click | Short thrust in your look direction; one durability per accepted stroke |
| Sail | Open its screen; Deploy/Fold and -15/+15 | Enable wind drive or adjust trim relative to the physical hull heading |
| Steering wheel | Open its screen; -15/+15 | Choose a hull heading in 15-degree steps; deck and supported players turn gradually |
| Engine | Add fuel in Fuel; Start/Stop | Drive toward the physical hull heading, retaining unused burn reserve when stopped |
| Anchor winch | Lower/Raise in its screen | Any lowered anchor stops horizontal motion and turning; raise all to resume |

Open controls with an empty hand or a non-building item. Holding the paddle always
attempts a stroke; switch hands/items to open a station. There is a 12-tick stroke
cooldown. Looking straight up/down, standing off the raft, or using an anchored
raft refuses the stroke without durability loss. Either hand works.

Headings use Minecraft's horizontal convention: **0 = south, 90 = west,
180 = north, 270 = east**. Wind is displayed as the direction it blows **toward**.
Steer toward wind or trim the sail toward it for maximum drive. Crosswind reduces
drive; a sail pointed directly into the wind adds no thrust. Current still flows
when sails are folded or stalled. Wind direction/strength vary smoothly with
world time; rain increases strength. All rafts in one level use the same wind.

Engines use verified native cooking-fuel components, burn durations and speed
multipliers. Planks and coal work; an emptied lava bucket returns its bucket to
the fuel slot. Only one fuel item starts each burn reserve. Stopped, anchored or
dry engines preserve fuel. Enabled floating engines burn while driving, including
when obstructed by terrain. Take unwanted fuel/remainders normally; right-click
places one and shift-click routes valid fuel. Invalid items stay in your inventory.

Multiple engines/sails contribute, while larger/heavier rafts accelerate and
respond more slowly. Total requested horizontal speed is capped at 0.16 blocks
per tick (3.2 blocks/second). Steering changes motion gradually through existing
acceleration. The hull turns at up to 15 degrees per second (six seconds for a
90-degree turn) while afloat with a wheel installed. It can turn at rest and
stops turning against terrain or the border/unloaded chunks. The anchor
immediately zeros horizontal movement and locks hull rotation. Bobbing and
vertical buoyancy continue while anchored. Removing a lowered anchor releases
its hold; removing an engine/sail removes that drive and closes its menu.

Hull yaw, target heading, sail trim, deployed/enabled states, anchors, fuel stacks and remaining
burn reserve save with the raft. Offline/unloaded rafts do not burn fuel. Paddle
impulses are temporary. Server-controlled forces feed the existing raft movement,
collision, player carry and client interpolation path; clients do not send thrust.

## Test checklist

- [ ] Craft all six items. Place controls on foundations and an upper floor.
      Expected: original models, weight/support rules and readable control screens.
- [ ] Paddle in several directions, then step into the water and try again.
      Expected: movement follows the look direction on deck; no remote steering.
- [ ] Deploy a sail, trim toward the displayed wind, then away, then fold it.
      Expected: stronger aligned drive, no headwind drive, cloth folds/unfolds smoothly, flutters and rotates with trim.
- [ ] Fuel/start an engine and turn the wheel. Expected: the actual deck and its walls turn gradually; feet and view follow it,
      including upper floors. Try walking, crouching, jumping and climbing mid-turn.
- [ ] Lower an anchor with engines/sails enabled. Expected: horizontal movement
      stops, bobbing remains and the fuel gauge stops draining. Raise all anchors;
      expected: propulsion and fuel resume.
- [ ] Stop an engine and reload the world mid-burn. Expected: fuel, heading,
      trim, anchor and enabled states persist; no offline fuel loss.
- [ ] With two clients, use the controls and open the same engine. Expected:
      shared heading/items/fuel, synchronized movement, no duplicate fuel charges.
- [ ] Remove an engine while its screen is open, and remove a lowered anchor.
      Expected: menu closes/cargo drops once, drive disappears/anchor releases.
- [ ] Turn under power near a loaded chunk edge. Expected: collision stays safe;
      debris changes its incoming corridor after its existing settling delay.

- [ ] Place a wind streamer and turn the raft through several headings.
      Expected: streamer and cyan compass point in the same world wind direction;
      the amber hull arrow turns with the raft. The mast stays attached to the deck.
- [ ] While turned, extend foundations, add a floor and open storage/tool stations.
      Expected: pieces attach to local edges; interactions remain correctly aligned.
- [ ] Watch the wheel, enabled fueled engine and anchor winch.
      Expected: turning spokes, rotating crank, animated raise/lower handle.

## Known limits

Validation: 11,908 standalone checks; 725 carry, 272 construction, 48 crafting,
85 existing station, 105 salvage, 128 survival, 50 navigation and 280 Ocean World
runtime assertions pass on isolated dedicated servers. Tests cover local/world
coordinate round trips, interior collider coverage at 72 headings, yaw-aware
packet validation, rotated native building rays, terrain-blocked turns, saved
hull yaw, loaded recipes/fuels and 140 consecutive local-foot checks during an
actual ocean turn. These fake-player tests do not verify live client rendering
or simultaneous multiplayer packet timing.

Rotated collision uses conservative 1/16-block horizontal strips, with exact
boxes at quarter turns, rather than a full oriented-box physics engine. Edges
may feel slightly wider. Cached recent angles limit repeated geometry work;
large builds during turns still need live performance testing. Players blocked
by world terrain can be separated from the moving deck instead of being pushed
through the terrain. Jumping/flying detaches from carry.

Wind streamer cloth is cosmetic; its base/mast collide. Sail cloth animates but
its reserved building volume and stable collider stay unchanged. The winch
handle animates; no chain or seabed anchor simulation yet. Engine crank motion
is visual, not a submerged propeller simulation. No copied sounds or assets.
Wind/drive snapshots update about once per second; folding/winch animation takes
0.6 seconds. Speed/heading use tracked movement and hull yaw. Live visual layout,
client packet timing, ladder carry and two-client stability need the checklist.
Earlier intermittent snap-back and item-loss reports remain pending user retest.

Original resource generator: run `tools/generate_building_assets.py`, then
`tools/generate_navigation_assets.py` after the existing survival generator.
Generated JSON/PNG files are included; normal builds need no Python installation.
Milestone 8 (creature AI) starts after your results or explicit instruction.
