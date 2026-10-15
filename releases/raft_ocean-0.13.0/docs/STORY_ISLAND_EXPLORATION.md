# Larger story islands — 0.11.38

Install `build/libs/raft_ocean-0.11.38.jar` on every client and the server. Targets remain Minecraft 26.3, NeoForge 26.3.0.48-beta and Java 25. Protocol is 11.38; addon API remains 2.

## More land to explore

| Story island | Previous template width | New template width |
| --- | --- | --- |
| Radio Tower | 33 | 81 |
| Glasswatch | 33 | 97 |
| Garden Relay | 33 | 97 |
| Echo Vault | 33 | 97 |
| Ember Beacon | 49 | 113 |
| Tideworks Haven | 65 | 129 |
| Starfall Archive | 49 | 129 |

Widths are template bounds in blocks, including coast and reef, rather than a square of solid land. Terrain now extends around the existing story landmark with uneven coastlines, carved coves, sandy beaches, graded slopes, wooded ridges and tidal pools. Water-only cells at the old template boundary can become terrain to soften that boundary. Original solid blocks, crops, kelp, puzzle components, journal containers and their contents retain their positions relative to the island center.

Each island has four optional side areas: a sheltered supply camp, an abandoned garden, a coastal lookout ruin and an open stone grotto. These add 28 places to visit across the seven islands. Chests and barrels supply food, seeds, string, scrap, coal and occasional copper. They do not grant extra story journals or bypass the bunker keys and switches.

One ordered landing route leads inland. A separate connected trail network reaches the side areas, keeping two blocks of headroom and avoiding two-block steps. Narrow boardwalks cross retained reefs. Trees and groundcover stay clear of these routes and entrances. Outer shallows add harvestable resources, kelp and seagrass.

Other island placement sets avoid the larger story footprints conservatively, so optional sites and ordinary islands are less likely to cut into story terrain. The rare story placement from 0.11.37 remains unchanged. Island variants are deterministic for each landmark; repeated copies of one story island use the same expanded template.

## Existing worlds and story

New terrain receives the expanded templates. Already generated islands remain in place; no regeneration or destructive world migration runs. Explore well beyond previously visited areas, or start a fresh Ocean World, to see complete new islands. Structures at a boundary between old and new chunks can retain parts of the earlier layout.

The seven English/German journals, receiver progression and puzzles retain 0.11.37's behavior. The caller and eastern transmission remain unresolved. There is no new final story chapter or destination in this update.

## Playtest

1. Find a story island in new terrain. Check its beaches, coves, slopes and woodland rather than only visiting the central building.
2. Follow the landing route and side trails. All four side areas should have accessible entrances and optional loot.
3. Walk through the rock grotto and explore the garden; check for blocked paths, unsupported plants or abrupt terrain edges.
4. Collect kelp and resources around the outer reef. The main journal chest, key and puzzle should still work.
5. Sail to another story island and compare its coastline. Confirm that the story still finishes with an unresolved signal.

Live appearance, exploration pacing and cooperative play still need user testing. The generator keeps the original main landmarks, so their architecture has not been wholly rebuilt.

Sources: `tools/story_island_exploration.py`, `data/raft_ocean/story_island_exploration.json`, `data/raft_ocean/island_routes.json` and the seven native NBT templates. Base and expansion generators reapply the terrain upgrade. `tools/verify_story_exploration.py` checks route connectivity, side loot references and preserved non-water core blocks; `StoryExplorationCheck.java` checks actual generated entrances, loot and trail clearance.
