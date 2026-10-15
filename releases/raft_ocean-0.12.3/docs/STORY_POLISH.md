# Story and island detail upgrade — 0.9.15

The six journals now tell one original story through Mara the lookout keeper,
Sela the seed keeper and Ivo the chart maker. A disputed evacuation, a mistaken
route and a rescued supply boat connect the locations. Each chapter has distinct
voices, discoveries and explicit directions to the next destination. Both key
chapters retain the exact lever clues. Version 0.9.16 leaves the crew following
an unfamiliar eastern transmission. Its caller, missing words and destination
remain unresolved, leaving room for later chapters. The receiver gives no further
coordinates yet; the six existing locations remain the current playable route.

All six chapters have English and German text, separated into readable paragraphs.
Journals still display in chat, remain reusable and advance the existing saved
journey. Their IDs, order, loot positions and receiver destinations remain stable.

| Location | Added character |
| --- | --- |
| Glasswatch | Dark timber supports, balcony, roof eaves, charts, lantern and planted approach. |
| Garden Relay | Timber greenhouse framing, raised glazed roof with ribs, potting bench, rain cauldron, storage and flower beds. |
| Echo Vault | Weathered entrance pillars, lighting, archive shelves, map desk and overgrown roof. |
| Ember Beacon | Keeper's cottage, copper lantern-room trim, lit approach and connected paths. |
| Tideworks Haven | Workshop gable, exposed corner beams, dock railings and lights, tools, workbenches and planted borders. |
| Starfall Archive | Observatory trail, carved entry pillars, lighting, a constellation in the bunker roof and memorial flowers. |

Outside the building clearings, weathered rock, mixed groundcover, flowers and
ferns soften the shores. Native block textures supply the architectural palette;
all building layouts and lore are original. Buildings retain standard Minecraft
block collision. Chests, switches and required ladders retain their positions.

## Upgrade and test

Update client and server to `build/libs/raft_ocean-0.9.15.jar`, protocol
`raft-movement-9.15`. Saves, six-stage journey progress and the existing raft
collision fix remain compatible. New journal text applies to existing journal
items; redesigned island buildings appear only in newly generated chunks.
No already explored island or opened chest is overwritten.

- Read each chapter, check its next destination against the receiver, and repeat
  in German. The Garden and Harbor journals must give the correct key/switch clues.
- Explore newly generated story locations. Walk the balcony, approach paths,
  greenhouse entrance, harbor pier and observatory route.
- Check lighthouse ladders, access to every main journal chest and both bunker
  entrances. Lighting should remain attached; switches and keys should work.
- Look at the new rooflines, cottage, workshop and gardens from the raft, then
  save/reload and repeat with another player.

Build, standalone checks and the native server checks pass, including 672 Ocean
World checks and 40 additional landmark/lighting assertions. The native checks
verify generated landmark blocks and supported lanterns in all
six locations, plus the existing loot, kelp, ladder, puzzle and journey checks.
Visual quality and connected multiplayer still require in-game testing.

Reproducible sources: `tools/story_details.py`, `tools/story_lore.py` and the two
island generators. Regenerate the base islands first, then the story expansion.
The expansion writes the authoritative six-chapter translations after base assets.
