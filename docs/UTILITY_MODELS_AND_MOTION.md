# Utility models and motion — 0.12.5

Three original utility models share timber, teal-painted metal and copper
fittings. Existing material textures are reused; no Raft assets are copied.

- Battery charger: raised docks, timber feet, front meter, contacts, continuous
  wiring and vents. Static docks belong to the charger housing.
- Biorefiner: open chamber, support frame, bands, gauge, outlet and overhead
  drive support. Working contents and a paddle animate inside the opening.
- Drying tray: framed slats, handles and fasteners. Wet clay appears during
  progress; the ready output appears dry. These are cosmetic indicators,
  not exact private inventory counts.

Rotors integrate angular velocity, winding up and coasting down continuously.
Long rendering gaps preserve phase. Incomplete visual updates retain the
previous complete frame; numeric clamping remains compatible. Empty frames
clear removed machines.

Mounted polygon, static and inventory models retain their previous envelopes.
Inventory transforms are refitted. Player collision, placement, recipes, saves
and propulsion remain unchanged. Animation anchors use the mounted raft frame.

Regenerate with `tools/refine_utility_models.py`; the main station generator
also runs this refinement. Software previews of actual resources:
`docs/previews/utility-models-0.12.5.png` and `utility-items-0.12.5.png`.

## In-game checks

1. Place all three at four rotations. Inspect models, inventory/JEI and ghosts.
2. Run and pause the biorefiner; its paddle must stay inside the chamber,
   stop smoothly and resume without snapping backward.
3. Cook food; check stirring, blocked output, pause and water alignment.
4. Dry bricks in sunlight. Pieces must rest on the slats, appear dry when ready
   and disappear after taking the output.
5. Walk past them on a rotating raft and test with a second player. Animated
   details must not add collision or change hull/player movement.

Minecraft lighting, final animation appearance, resource packs and real co-op
latency need local testing. Automated checks cannot guarantee every scenario.
