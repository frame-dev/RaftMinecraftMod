# Machine contents and animation alignment — 0.10.7

Animated contents retained offsets from the old square models after the vessels
were replaced with polygon surfaces. The grill also used its first box (the top
plate) as its base, raising food and effects above the model. A quarter-turned
part changed its bounding-box dimensions without rotating its mesh's own axes.

The renderer now uses the complete piece envelope as the base, restores canonical
part axes before scaling, then rotates the geometry around the existing animation
pivot. This applies to shafts, lids, textured parts, wheels and other articulated
pieces in all four placement directions. Chest latches follow the opening lid.

Water uses the current vessel envelope, its .18 normalized interior floor, .425
inner radius and a smaller .405 fill radius. Its height is clamped below the rim;
small waves cannot pass through the floor or rim. Water renders as a smooth disk.
Bubbles and cooking-pot stirrers follow that surface. Rain drops finish inside
the collector; steam starts above the model; grill portions sit on its actual
top. Smelter embers align with its front opening. Floating external flame blobs
were removed from closed purifier/pot walls.

Planter soil is now on top of the base instead of hidden inside it. Crops and tree
trunks start at that soil surface; aquatic planter water sits just above it.
Off-center machine effects use the station's mounting rotation.

## Verification and local test

The build's native model test checks mounted geometry at all four quarter turns,
including off-center pivots, and checks empty-to-full vessel fills with extreme
wave inputs. Inventory fitting and packaged model/texture/mesh audits also run.
See build/machine-alignment-final-build.log for the build and native results.

Restart with 0.10.7, then test:

- Fill/drain purifiers and collectors; water should remain inside their walls.
- Run cooking pots, grills and smelters; food, stirrers, bubbles and heat/steam
  effects should align with their respective surfaces and openings.
- Place each machine at 0/90/180/270 degrees, including on an upper floor.
- Open rotated chests/barrels and operate the engine and steering wheel; attached
  parts should retain their model orientation and hinge/shaft placement.
- Grow crops and a tree; their roots should touch the planter soil. Reload with
  F3+T and check that geometry and effects still align.

Live visual timing and multiplayer remain user tests. Collision boxes, gameplay,
recipes, inventory fitting and the open-ended story are unchanged.
