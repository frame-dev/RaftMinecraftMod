# Distant deck collision fix — 0.9.13

Minecraft's entity index stores a raft in the section containing its origin.
Nearby searches only visit sections within a small margin of the search box.
Consequently, a large raft can overlap a player while its origin lies outside the
sections being searched. The existing collision and player-support hooks then
receive no raft at all. The isolated regression reproduced a downward fall at
local X=16 even though a foundation was present underneath.

A weak per-world raft index now supplements typed, untyped and existence searches
with intersecting hulls. It checks the world's live entity lookup before returning
a raft, respects type/predicate/exclusion/result limits, and avoids duplicates.
Canceled spawns, unloaded entities and removed rafts cannot become ghost platforms.
World unload clears the index. The same common implementation runs on both sides.
Actual collision still uses individual deck and structure shapes; bounding boxes
do not fill interior holes or the water around the raft.

Update client and server together to `raft_ocean-0.9.13.jar`; the movement protocol
is `raft-movement-9.13`. Existing saves and construction data remain compatible.

## Check in-game

- Walk across an extended raft, especially more than sixteen blocks from its
  original center. Foundations and collection nets should support you throughout.
- Repeat while drifting and turning, then on upper floors and after save/reload.
- Walk around stations and through open doors; their actual solid parts should
  block movement while clear passages remain usable.
- Step deliberately off the deck or into an actual missing foundation. You should
  still fall into water; this fix does not add an invisible safety platform.
- Repeat with another player on a dedicated server. If falling or snap-back
  persists, record whether it happens beside a station, at a seam, or during a
  turn, and capture `/raftsupport` on the client and `/raft info` on the server.

Build and both isolated server runs pass, including 417 native collision checks
and the existing carry, construction, station, survival, navigation, creature and
Ocean World checks. Native regressions cover distant foundations/nets, eight hull headings, upper
floors across vertical sections, player carry, query filtering, real holes and
removed entities. Live client interpolation and connected multiplayer still
require in-game testing.
