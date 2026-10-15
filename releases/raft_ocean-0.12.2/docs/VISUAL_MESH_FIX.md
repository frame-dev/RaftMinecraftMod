# Visual rendering and item fit fix — 0.10.6

The supplied 23:51 screenshots showed that resource changes were loading, but
rounded stations still had striped surfaces and the hotbar tools remained small.
The previous native parser check established valid JSON, not correct appearance.

The GUI fitting loop mistakenly reused and changed its nested loop variables
while measuring corners. It now measures independent coordinates, applies the
same rotation order as Minecraft and centers the resulting silhouette. Native
ItemTransform matrices check all 87 GUI silhouettes for size, clipping and
centering. Long tools use a readable diagonal front view; flat materials show
their broad face. The spear blade now faces the viewer instead of presenting its
thin edge, and the handheld hook has an open curved head instead of a radial disk.

Moving purifiers, rain collectors, cooking pots, smelters, pillar shafts, barrel
parts and engine cylinders now use true textured polygon surfaces, with separate
materials per surface. Eleven smooth station resources replace visible cuboid
slice bands in those parts. The reusable client mesh renderer supports material
groups and resource reload; whole-part pivots, animations and collisions remain.
Other structures continue using the closed beveled block models.

The shark has a continuous tapered body and rounded snout, a blue upper body and
pale underside, separate dorsal/pectoral/tail shapes, and teeth aligned with the
resting jaw. Existing swim, turn and bite animation calculations are retained.

## Local verification

- Restart using 0.10.6 on client and server. Inspect the hammer, spear, paddle,
  hook and receivers in the hotbar and inventory: they should fill their slots
  and stay centered. Check held items too.
- Inspect the named round stations from above and the side in daylight: their
  outer walls and open rims should be continuous without alternating slice bands.
- Open barrels, run the engine, use the machines and reload with F3+T. Their
  existing animations should still move around their original pivots.
- Inspect the shark from front and side, including a bite: its snout should be
  rounded, body continuous, pectoral fins thin and teeth attached to its jaws.

Build, native matrix/model tests and packaged texture/mesh audits are recorded
in build/visual-mesh-final-build.log. The software inventory preview now uses
the actual GUI rotation, scale and translation. It is not a live game screenshot.
Live rendering, connected multiplayer, night lighting and performance require
user verification. Existing collision limitations and movement reports remain
open; research and the story remain unchanged and open-ended.
