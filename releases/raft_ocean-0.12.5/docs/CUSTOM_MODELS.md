# Custom models and coastal scenery — 0.10.1

All 39 raft building and station items now use original multipart 3D inventory
models. The foundation item also uses a 3D deck model. Static raft pieces use
native block model resources, including corners, curved walls, ladders, stairs,
collection nets and benches. Workbenches have raised papers and tool panels;
bookshelves have open shelves and individual book shapes. Machines, doors,
chests, sails, steering wheels and other moving parts retain their articulated
raft rendering and animations. This is not a replacement of every animated
world model. Existing picking and collision geometry stays in place.

The model generator reads a native geometry snapshot exported by the isolated
bootstrap server. After changing piece shapes, run the bootstrap check, then
`python tools/generate_custom_models.py`, then build the mod. Model bounds are
normalized for the existing raft renderer; inventory models use uniform scaling
to retain the proportions of tall pieces.

Story and resource islands gain deterministic coastal flower patches, grounded
rock outcrops, moss shores and layered oak/birch groves where there is room.
Building pads, loot and central story approaches are protected. Island sizes,
receiver destinations, bunker puzzles and the unresolved eastern transmission
remain the same. Scenery changes appear in newly generated chunks; existing
islands are not rewritten. Use a separate new Ocean World for visual comparison.

## Reported placement-preview crash

The supplied report fails to load `RaftEntity$Placement` from the shared
development class directory. The class exists in the current compiled output.
A rebuild or clean while the development client was running is the suspected
cause; the report alone cannot prove which operation removed the file.

Close the project's Minecraft client, rebuild, then relaunch it. The build now
checks live Java process arguments and refuses compilation, resource updates or
cleaning while this project's Gradle client is running. An external IDE compiler
can still update its own outputs, so disable background compilation for the
running game. Installed clients use the packaged JAR, with this nested class
included. Install the same 0.10.1 version on client and server.

## Local test checklist

- Browse raft items in the inventory and JEI: models should be 3D, with no purple
  missing textures. Reload resources once with F3+T.
- Place benches, bookshelves, nets, walls/corners, stairs and ladders in all four
  orientations. Compare their previews and collision boundaries.
- Open chests, run machines and operate a sail/engine: existing animations should
  still work while the raft turns and moves.
- Explore newly generated story/resource islands. Trees and rocks should stand
  on the ground; journal loot, seal puzzles and the open ending should still work.
- Report a fresh crash log if the placement-preview crash recurs after a full
  close, build and restart.

Native server checks do not validate client lighting, resource reloads, visual
placement or connected multiplayer. Those remain user tests.
