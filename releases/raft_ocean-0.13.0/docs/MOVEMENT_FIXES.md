# Player clearance and shark approaches in 0.9.5

Update both client and server. The movement protocol is `raft-movement-9.5`;
existing worlds remain save-compatible.

## Raft objects

Rigid carrying previously ignored the owning raft during movement, which prevents
its newly moved colliders from blocking the carry path. However, the destination
was not checked against furniture. A player has an axis-aligned body even when
the raft rotates, so its corners could end up inside a station or wall.

Carry destinations now check actual raft colliders. Already embedded players are
also checked before and after their normal movement. Small corrections choose
the nearest clear supported position, up to 1.5 blocks away, without moving the
feet vertically except for tiny deck penetration. The player's center must stay
over a supporting collider, or on a raft ladder. Candidate paths may escape the
original overlap but cannot enter other raft solids or cross terrain/other rafts.
The server applies the same clearance check to rebased client movement frames.

If no reachable clear space exists, recovery leaves the player in place. Remove
or move an object to make room in a completely packed or enclosed space. Flying,
spectators, passengers, sleeping players, gliding and upward jumps are excluded
from automatic overlap recovery. Live corner/corridor behavior and multiplayer
still need testing.

## Shark turns

Sharks acquire nearby swimmers up to twelve blocks away. Their heading changes
by at most six degrees per server tick and their swimming velocity follows that
heading, creating a curved approach instead of an instant turn. Sharp turns slow
the shark briefly. Foundation attacks also turn toward the exposed target before
the existing 40-tick bite windup starts. Swimmer bites require facing the target
within 25 degrees, so a shark cannot bite sideways while turning around.

Sharks retain the existing damage/cooldown settings, bait response, deck-overlap
recovery and bite animation. Gradual steering can make total approach time longer;
this is expected. Client interpolation renders the server's heading.

## In-game checklist

- Walk close to chests, crafting tables, smelters, pillars, walls and stairs while
  the raft moves and turns. Check both the starter deck and an upper floor.
- Repeat in narrow corridors, near doors and beside an island. Recovery must
  keep you on usable deck space and avoid crossing walls or terrain.
- Watch a shark initially facing away from the raft. It should arc around toward
  its target, align, then wind up and bite. Repeat as a swimmer with room around
  you; the shark should turn from a distance rather than instantly face you.
- With two updated clients, check carrying near furniture and shark attacks.
  Report the object type, floor, and whether the raft was turning if trapping or
  repeated snap-back still occurs.
