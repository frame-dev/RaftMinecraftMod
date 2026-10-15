# Collision clearance polish — 0.11.1

Recovery previously corrected tiny supporting penetrations upward and otherwise
searched for a sideways escape, checking the path at four positions. A low ledge
could therefore cause a sideways shove, and very thin obstacles could lie between
those path samples.

Recovery now considers supported steps up to 0.6 blocks, with full body/headroom,
world collision and loaded-area checks. It chooses a step when it requires less
movement than the available sideways escape. Otherwise the smallest supported
lateral correction is used. Higher stations and roofs cannot be climbed by this
correction. Clear players receive no recovery movement.

Sideways paths use continuous slab intersection against other raft colliders and
Minecraft's native swept world/entity clipping. Intermediate support is checked
at intervals no greater than 0.05 blocks; final clearance, world border and loaded
chunks remain required. Recovery cannot invent deck space or break through a
sealed room. Existing 1.5-block maximum lateral recovery remains for genuinely
embedded players. Jumping/flying/swimming carry rules and networking are retained.

Native server regressions cover embedded stations/walls at eight non-cardinal
hull angles, repeated rotation carry, low ledge stepping, thin-wall interception,
upper floors, sealed rooms and narrow supported escape. Existing carry, structure,
survival, research and creature checks are retained. Log:
build/collision-clearance-build.log.

Use matching 0.11.1 clients/server. Artifact build/libs/raft_ocean-0.11.1.jar.
Restart installed Minecraft; close, rebuild and relaunch development clients.
Check walking/jumping around chests, engines, stairs, doors and pillars while
anchored, drifting and turning; repeat on upper floors and with two players.
Expect fewer sideways pushes at low ledges, clear exits from overlaps, no passage
through thin walls and no correction when already clear.

Rotated hull colliders still use conservative 1/16-block strips. This is not full
continuous oriented-body physics; corners can still feel slightly wider. Deep
embedding may need a visible bounded correction, and a sealed room without a
clear supported destination cannot be automatically escaped. Live multiplayer
latency and all previously reported glitches still need local verification.
