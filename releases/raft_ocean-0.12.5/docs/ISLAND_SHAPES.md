# Distinct story island shapes — 0.11.39

Install `build/libs/raft_ocean-0.11.39.jar` on every client and the server. Minecraft 26.3, NeoForge 26.3.0.48-beta and Java 25 remain the targets. Protocol is 11.39; addon API remains 2.

Story coastlines now use seven authored concave outlines, rather than a perturbed circle or ellipse:

| Island | Coastline |
| --- | --- |
| Radio Tower | Crooked headland with offset points and deep inlets |
| Glasswatch | Hooked peninsula with a sheltered inner bay |
| Garden Relay | Branching coast with several fingers and channels |
| Echo Vault | Split cliff lobes connected around the main landmark |
| Ember Beacon | Long broken ridge with uneven side promontories |
| Tideworks Haven | Forked harbor headlands and an open southern inlet |
| Starfall Archive | Jagged stepped promontory with recessed coves |

Small shoreline perturbations soften the outline edges. Beach and slope heights follow distance from the actual coastline, so narrow necks, outer points and coves receive their own shore transitions. Vegetation and optional sites adapt to the available land; exposed headlands can have fewer trees than broad wooded lobes.

All seven islands keep the larger 81–129-block bounds from 0.11.38, the four optional exploration areas, connected trails and resource shallows. Main landmarks, crops, journal loot and puzzle blocks retain their center-relative positions. Rare story placement, receiver progression and the unresolved ending remain unchanged.

This update changes the seven story templates. Ordinary resource islands retain their existing variants. Repeated copies of a given story island still share its authored shape. New coastlines appear in newly generated chunks; already generated islands are not rewritten.

## Playtest

1. Find story islands in a fresh Ocean World or unexplored terrain. Compare their silhouettes from the raft and from a high vantage point.
2. Explore the new bays, fingers, narrow necks and split lobes. Look for abrupt edges or cut-off paths.
3. Follow trails to all four side areas, collect their loot, then reach the main journal and puzzle.
4. Check underwater gathering near the reshaped coastline. Report any inaccessible entrance or terrain seam, ideally with the island name and coordinates.

The generated map preview is `build/story-exploration-preview.png`; it shows template geometry, not an in-game screenshot. Live terrain appearance and cooperative exploration still need testing.

The outlines are data-driven in `data/raft_ocean/story_island_shapes.json`. `tools/story_island_exploration.py` generates the templates, while `tools/verify_story_exploration.py` checks concavity, connected trails, optional loot and preserved non-water core blocks. To regenerate after editing an outline, run the base and expanded story asset generators to rebuild their original cores first; the exploration revision marker prevents accidentally expanding an already expanded template.
