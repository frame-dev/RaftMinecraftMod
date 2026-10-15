# Larger islands — 0.11.40

Every island type now has a larger terrain footprint. Buildings, counters,
doors, inventories, story puzzles and journal progression retain their scale.
The story remains open-ended and story-island rarity settings are retained.

| Island type | Old width | New width |
|---|---:|---:|
| Ordinary resource islands, ten variants | 17–81 blocks | 33–161 blocks |
| Drift Atoll | 33 | 65 |
| Exploration islands, three variants | 49 | 97 |
| Trading outposts, two variants | 65 | 129 |
| Radio Tower | 81 | 113 |
| Glasswatch, Garden Relay, Echo Vault | 97 | 145 |
| Ember Beacon | 113 | 161 |
| Tideworks Haven, Starfall Archive | 129 | 193 |
| Underwater exploration-site footprint | 33 | 65 |

Resource islands have approximately three to four times their previous exposed
land area, with extra vegetation and harvestable coastal resources. Existing
landmarks and original reefs stay in place relative to the island center.
The underwater sites gain a larger surrounding seabed; their wreck buildings
retain their original size. Cross-pool spacing accounts for the larger bounds.

## Testing

Install the same 0.11.40 jar on all clients and the server. Use a fresh Ocean
World or explore chunks that have never been generated. Existing islands
will not resize. At the boundary of previously generated terrain, old and new
terrain can meet; a fresh world gives the cleanest comparison.

- Compare small sandbars and large wooded resource islands.
- Visit a trading outpost and use its counter.
- Follow an exploration signal and open its original loot chest.
- Visit a story island and check its four side areas and main puzzle.
- Check shores, tree bases, paths and reef resources for obstructions.

Generating the largest islands touches more chunks and can cost more time
than before. Actual client frame rate and multiplayer exploration still need
an in-game check.

## Regenerating assets

Run the base ordinary/exploration/story generators, then
`tools/generate_outposts.py` (which polishes routes and applies the ordinary
island enlargement), and run `tools/verify_larger_islands.py` and the existing
story/route audits. `tools/enlarge_islands.py` is idempotent and can also run
after a base generator. Story terrain sizes live in
`tools/story_island_exploration.py`; ordinary sizes live in
`tools/enlarge_islands.py`.
