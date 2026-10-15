# Milestone 3 — raft construction (0.5.0)

The user confirmed that 0.3.1 carry and foundation building work. This build
adds floors, walls, two-block doors, stairs, ladders, and stepped roofs to the
moving raft. These are owned pieces, not stationary world blocks. The server
validates construction; layout, orientation, and door state synchronize together
and persist. Existing 0.2.x/0.3.1 foundations load without conversion commands.
Version 0.3.4 adds tall pillars, short pillars, railings, and half walls for open
second decks. Existing structural saves remain compatible.
Version 0.3.5 completes construction item access: all 12 items appear in the
Raft Construction Creative tab and all 12 recipes unlock upon acquiring planks.
The implementation is ready for the final user acceptance checks below.

## Build and launch

Close the previous client/server, then run from the project folder:

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
```

Verify **Raft Ocean 0.5.0** in Mods. Artifact:
`build/libs/raft_ocean-0.5.0.jar`. Use this build on all connected clients and servers.
Use an existing raft or spawn one over open water with `/raft spawn`.

```text
/raft board
/raft current 0 0
/give @s raft_ocean:foundation 32
/give @s raft_ocean:building_hammer
/give @s raft_ocean:raft_floor 32
/give @s raft_ocean:raft_wall 32
/give @s raft_ocean:raft_door 4
/give @s raft_ocean:raft_stairs 8
/give @s raft_ocean:raft_ladder 16
/give @s raft_ocean:raft_roof 32
/give @s raft_ocean:raft_pillar 8
/give @s raft_ocean:raft_short_pillar 8
/give @s raft_ocean:raft_railing 16
/give @s raft_ocean:raft_half_wall 16
```

Creative flight helps construct upper levels. Test collision, walking, stairs,
ladders, and carry in survival afterward. Zero current decelerates gradually.

## Intermittent snap-back fix

Version 0.3.3 sends the client's interpolated raft origin with movement updates.
For supported feet or ladder contact, the server translates that position to its
current raft origin before normal movement validation. Server carry also resets
the movement baseline so the next packet does not count raft displacement twice.
Frames must match the accompanying position, refer to a nearby raft, and pass
server-side support checks. Jumping away uses normal vanilla movement.

The reported intermittent snap-back needs a live retest. Stand on a drifting
foundation and upper floor for five minutes, then walk, sprint, jump, climb,
reverse the current, and leave/reboard. You should retain your position relative
to the raft while idle and move freely while walking. Repeat with two clients.
Use 0.5.0 on both sides for the acceptance tests. The required protocol rejects
pre-0.3.4 clients that cannot decode the new structural kinds.

Networking follows the [official NeoForge payload workflow](https://docs.neoforged.net/docs/networking/payload/);
the exact signatures were verified against the local 26.3 dependency sources.

## Controls

- **Foundation:** hold the item, aim near an existing foundation's outer edge,
  and right-click. The base extends horizontally, as in 0.3.1.
- **Structural pieces:** hold the matching item and right-click a piece. Clicking
  a foundation builds above it. Clicking a structural piece uses the hit face to
  attach above, below, or beside it. Crouch + right-click forces placement above.
- **Orientation:** face the direction you want the piece to use before placing.
  Stairs rise forward; wall/door/ladder panels sit at the near edge of their cell.
- **Doors:** use an empty hand and right-click to open/close. A door occupies two
  vertical cells. A player obstructing its destination prevents toggling.
- **Removal:** hold the hammer, crouch, and right-click the piece. Survival refunds
  its item. Remove upper pieces before their supports. Removal cannot disconnect
  foundations, leave floating structures, or overload the remaining foundations.
- **Ladders:** approach the rungs and hold jump to climb; release to descend.
  Crouching holds position. Stack ladders by aiming at a post's top face or using
  crouch placement. Keep the space above the ladder clear for exiting.
- `/raft info` reports foundation/structure counts, mass, buoyancy, and speed.
  `/raft board` now searches for a clear surface with headroom.

All pieces use one-block vertical grid spacing, beginning above the 0.25-block
foundation. Floors are 0.125 blocks thick, walls one block tall, doors two blocks
tall, and stairs climb one block using four quarter-block steps. Roofs use four
stepped strips. Stack two wall pieces before covering a room so it has standing
headroom. For a full-height upper room, use a floor three grid levels above the
foundation; shallow one-block levels can function as platforms or crawlspaces.

Example upper platform: place a wall on a foundation, then crouch-place a floor
above the wall. Add adjacent floor tiles by clicking the floor's side. Place
stairs on a neighboring foundation facing the platform, or build a ladder beside
it. Upper floors stay attached to the raft and should carry players at their own
height, rather than snapping them down to the foundation.

## Open second floor with pillars

1. Set `/raft current 0 0` and give yourself the new pieces above. Step away from
   the foundation's center so your body does not obstruct the pillar.
2. Hold **Tall Raft Pillar** and right-click a foundation. It occupies three
   vertical cells and has a narrow central post with original end caps.
3. Hold **Raft Floor**, aim at the pillar, and crouch + right-click. This snaps
   the floor above the complete pillar, leaving three blocks clear below it.
   Clicking the pillar's top without crouching does the same.
4. Extend the upper deck by clicking the thin side edge of that floor with more
   floor items. Crouch placement always builds above, so release crouch here.
5. Add railings or half walls by clicking an upper floor. Their bases sit directly
   on the thin floor surface. Facing controls their edge orientation. Railings
   have a full-height safety collider despite the visible gaps between rails.
6. Reach the deck with three stacked ladder pieces next to the pillar, or a
   connected staircase using the existing stair pieces. Creative flight is useful
   for initial construction; switch to survival to verify walking and carry.

Short pillars occupy one cell and support low platforms. Tall pillars weigh 6;
short pillars, railings, and half walls weigh 2 each. Pillars reserve their whole
height, so another piece cannot pass through them. A second supporting pillar can
keep the deck supported while removing the first. Remove mounted railings/half
walls before removing the floor under them.

Support still uses the bounded connected-cell graph: there is no realistic beam
span calculation. Pillars support open upper decks; the simulation does not
rotate or tilt their geometry. Floors are the existing floor item, with placement
above tall supports now handled automatically.

## Support and buoyancy

Updated in 0.8.1: limits are 1024 foundations, 512 structural pieces, local X/Z from -32 to +32, sixteen
structural grid levels, five-block interaction reach. Doors reserve their upper
cell. Every structural piece must connect through occupied neighboring cells to
at least one foundation. This is a simple support graph, not real beam engineering.

Foundations contribute 12 mass and 20 buoyancy each. Floor/wall add 4 mass;
door/stairs 6; ladder 2; roof 3. Construction cannot exceed 90% of total buoyancy.
If placement becomes too heavy, extend the foundations. More mass increases draft
and slows acceleration; structural pieces do not add buoyancy. Player/cargo mass
is deferred. Placement also checks terrain, player obstruction, border, occupied
cells, and height/size limits. English feedback explains refused building actions.

## Recipes

Version 0.5.2 replaces the early building recipes with brace/binding/mesh
chains. See [the current recipe layouts](CRAFTING_CHAINS.md) for all ingredients,
outputs, and bootstrap recipes. Existing placed structures remain compatible.
Generated recipe-book unlocks are included; research gates remain milestone 10.

## Test checklist

- [ ] Open the Raft Construction Creative tab. It contains foundation, hammer,
  and all twenty-six structural pieces; items also appear in Creative search.
- [ ] In survival, acquire planks and open a crafting table's recipe book. Construction
  recipes are learned; enable the uncraftable-recipe view to see missing components.
- [ ] Build all six types in several orientations. Shapes/icons render without
  missing textures; wall panels obstruct movement.
- [ ] Build an open second floor on tall pillars; verify headroom, upper-deck
  carry, pillar collision, and railings/half walls mounted without visible gaps.
- [ ] Try placing through the pillar's upper cells. Placement is refused. Try
  removing the only support pillar: refused. Add a second support and retry.
- [ ] Save/reload the new pieces. Their kinds and rotations persist. Craft all
  four new recipes and check survival consumption/removal refunds.
- [ ] Open/close a door and walk through it. Another player in its destination
  should block closing; both clients should see the same state.
- [ ] Walk up stairs, climb/descend ladders, and walk across elevated floors.
  Native movement controls work; crouching holds you on ladders.
- [ ] Restore `/raft current 0.02 0.008`. Stand idle on an upper floor, walk,
  jump, and climb. Carry follows the raft without dropping you to the lower deck.
- [ ] Try removing a wall supporting an otherwise disconnected upper platform.
  Removal is refused; removing the platform first allows removing its wall.
- [ ] Craft the new items, build in survival, and check consumption/refunds.
  Build enough pieces to reach the weight limit; adding foundations permits more.
- [ ] Save/reload with an open door and upper pieces. Layout, rotations, and open
  state persist. Old foundation-only rafts still work.
- [ ] Repeat with two clients on a dedicated server: edits synchronize, both
  players move independently, and no repeated corrections or flying kicks occur.

Build checks cover foundation geometry/carry and structural support/save rules.
An isolated development server also validates resources and runs the production
carry method with a fake player; it does not simulate a connected client or a
registered raft's multiplayer collision. Additional construction checks load all
recipes/unlock rewards, build the registered Creative tab, round-trip a raft with
every structural kind through the actual entity save/load methods, and carry a
fake player on the restored second floor with bobbing and reversed movement.
New pieces still require these live tests.

Version 0.3.5 validation: build and dedicated-server startup succeeded; 86
standalone regression checks, 725 runtime carry assertions, and 170 runtime
construction assertions passed. The development server shut down cleanly.

If something fails, run `/raft support` and `/raftsupport` while on the raft, then
attach `run/client/logs/latest.log` and the server log if using a dedicated server.
These commands report contact, relative position, and requested/applied carry.
The server report also includes a rebased-packet counter; it should increase
while standing on a moving raft. Capture both reports immediately after a snap-back.

## Known limits and next gate

Original wood geometry is deliberately simple. Roofs are stepped; doors open
instantly. Shapes stay axis-aligned; physical rotation/tilt, damage, weather,
redstone, arbitrary attached vanilla blocks, and offline chunk drift are deferred.
Beds/storage and other functional survival pieces arrive in later milestones.

Milestone 3 implementation is finished. Final in-game acceptance and the reported
intermittent snap-back retest remain pending. Wait for the user's test results
before marking gameplay acceptance complete or starting milestone 4.

Raft Crafting Table was added in 0.4.1 to close the survival crafting gap. See
[the raft crafting guide](RAFT_CRAFTING.md) for its inventory-grid recipes, placement,
normal 3x3 screen, removal behavior, and acceptance tests.

Version 0.4.2 adds seven storage/tool station kinds and direct vanilla station
placement. See [the station guide](RAFT_STATIONS.md).
