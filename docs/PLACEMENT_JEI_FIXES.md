# Placement, movement and JEI — 0.9.10

Target: Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25.
Install `build/libs/raft_ocean-0.9.10.jar` on client and server together.
Protocol: `raft-movement-9.10`. Existing raft saves remain compatible.

## Placement

Hold any raft building item and aim at the raft within five blocks. A translucent
green model shows the proposed piece; red means placement is refused. The hint
explains obstruction, missing support/capacity or foundation limits. The preview
uses the same picking, shape, support, orientation and obstruction calculation
as the server. It has no collision and cannot consume items.

Walls, half walls, railings, doors and corner/curved walls snap to the clicked
deck edge. Click a wall face to extend along its orientation; click its top to
stack another section. Sneak to use your relative facing instead of automatic
wall edge alignment. Existing saved walls are not rotated or moved.

To reproduce the joined walls from the screenshot, place one full wall, then
aim at the other edge of its supporting deck cell and place a perpendicular full
wall. The green preview becomes a corner wall. The second click consumes one
wall and converts the existing face into the joined corner, preserving its
orientation. All eight perpendicular face combinations work on rotating hulls.
Removing the joined piece returns one reusable Corner Wall item. The joined
piece uses the existing corner-wall save format; existing saves need no migration.

Foundations and collection nets extend along the selected deck edge. Other
stations use the selected mounting cell and player facing. Rotated obstruction
checks use the same narrow hull colliders as player collisions. The bounded
structural grid still allows one structural piece per occupied cell; it does
not allow arbitrary opposite wall faces or several stations to share a cell.
Two perpendicular full walls join automatically, or place a corner wall item
directly. Half walls and railings do not automatically merge. Door animation and
machine animations do not animate ghosts.

An offhand preview is shown when the empty main hand would let that placement
reach the offhand. Stations and doors use the main-hand interaction first.
Another player, raft motion or terrain can change validity between preview and
the server receiving a click; rejected placements leave the item unchanged.

## Movement

The client sends each raft frame immediately before its matching position
packet. The server buffers at most 32 immutable frames and matches coordinates
before consuming one. Rotation-only packets do not consume frames. World/entity
validation stays on the server thread; frames expire after one second.

After rebasing the coordinates to the authoritative hull, the server checks the
whole movement path against raft solids and world collisions. A free endpoint
on the other side of a wall no longer permits crossing that wall. Vanilla
movement, speed and teleport acknowledgement checks remain active. The selected
raft is explicitly included during collision checks across tracking transitions.

These changes address known causes of movement corrections. Multiplayer latency,
large rotating rafts and the reported intermittent snap-back still require live
testing; automated server checks do not prove every connection behaves correctly.

## JEI and labels

JEI 31.9.0.58 remains optional. The development client installs it automatically;
dedicated servers do not need it. Five custom processing categories use the
server's synchronized recipes and refresh on reload. Grill and smelter link to
native cooking/smelting recipes. Click a machine's progress meter on its Process
page to open its matching recipe category. This adds recipe browsing, not an
automatic ingredient transfer button.

JEI conditions wrap on dark contrast panels. Machine titles no longer overlap
the Supplies button, adjacent output labels are staggered, long labels are
bounded, and engine status text wraps beside its controls. Added English/German
ghost hints and German machine, navigation and JEI labels. Remaining German lore
and other unfinished translations use Minecraft's English fallback.

## Test checklist

- Aim each building item at a clear deck: green ghost follows raft position and
  rotation. Right-click builds at the preview. Aim at occupied/unsupported
  space: red ghost; rejected clicks consume nothing.
- Build straight wall rows, corner/curved sections, doors and a second floor.
  Check edge alignment, side continuation, top stacking and sneak orientation.
- Extend foundations through nets; check station placement on rotated hulls.
- Walk, sprint, jump and climb beside walls and equipment while drifting,
  turning, sailing and driving. Repeat with two players on a dedicated server;
  report any snap-back with client/server logs and the action that triggered it.
- Open every machine in English and German. Labels should fit; hover empty
  slots for complete descriptions. Click progress meters and search recipes
  in JEI. Run `/reload` and reconnect: recipes should remain current.

Build, standalone geometry checks and isolated native server checks are run
locally. In-game rendering, JEI interaction/reload and connected multiplayer
remain user validation.
