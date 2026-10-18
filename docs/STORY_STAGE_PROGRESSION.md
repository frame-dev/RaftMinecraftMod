# Story exploration and research balance — 0.13.1 preview

Each of the seven story destinations now has a strictly larger template footprint
than the preceding stage. These are bounding widths, not solid square islands;
the authored irregular coastlines, coves, landmarks and graded terrain remain.

| Stage | Island | Previous width | New width |
| --- | --- | ---: | ---: |
| 1 | Radio Tower | 113 | 129 |
| 2 | Glasswatch | 145 | 153 |
| 3 | Garden Relay | 145 | 177 |
| 4 | Echo Vault | 145 | 201 |
| 5 | Ember Beacon | 161 | 217 |
| 6 | Tideworks Haven | 193 | 233 |
| 7 | Starfall Archive | 193 | 249 |

Four guaranteed field-note books per island are distributed between the shelter,
garden ruin, lookout and rock grotto supply containers. Each contains a distinct
account by Mara, Ivo or Sela, and a second contextual page, localized in English
and German. The main journals still carry the next destination. Side notes do
not grant journey stages, keys or research unlocks. Both existing key/switch
puzzles remain; the final eastern signal remains unresolved and open-ended.
Ordinary islands, spawn frequency and story destination IDs are unchanged.
Exclusion clearance for neighboring island pools accounts for the larger maximum.

## More demanding research

A material study now consumes its complete batch once. The Materials page shows
`xN`, and hovering Study shows the required batch. An incomplete batch disables
Study and consumes nothing. Completed projects and previously studied materials
remain known; recipe learning after material study stays free.

| Samples | Materials |
| --- | --- |
| 2 | Planting mix, woven mesh, canvas, coal, leather |
| 3 | Iron, dry/fired brick, copper, glass, cordage, metal plate, binding paste |
| 4 | Scrap, wire, fiber, planks, stone, clay, fasteners, kelp |

Balanced costs are bundled in `data/raft_ocean/research_sample_costs.json`;
this is a build resource, not a live datapack reload or server configuration.
Client and server must use the matching mod version so displayed costs agree.
Starter purifier/grill, sun-dried brick and one-use anchor paths remain available.
The smelter still accepts sun-dried brick for research without needing a smelter
first. Creative/admin research unlocks and the research-disable option remain.

## Existing saves and testing

Back up worlds before updating. Existing generated chunks retain their old
islands and loot. Use newly generated regions or a fresh Ocean World to see
all larger islands and new notes. No old region files are deleted or regenerated.
Large templates generate more blocks and may take longer to load.

Test a short research batch, then the exact amount; verify one-time consumption
and persistent knowledge. Explore all seven stages in a fresh Ocean World,
check clear landing/side-site paths, collect four different books per island,
read both language variants and complete the existing puzzles. Inspect multiplayer
chunk loading and walking on the larger shores under actual network latency.

Regenerate templates and note tables with:
`python -X utf8 tools/story_stage_upgrade.py`.
The script uses retained original authored cores in `tools/story_cores`, then
applies the existing natural coastline and route grading pipeline. Vanilla
written-book components and loot modifiers follow the pinned 26.3 codecs.
