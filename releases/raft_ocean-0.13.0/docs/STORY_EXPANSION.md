# Three additional story islands — 0.9.14

Version 0.9.16 makes this route the current stopping point of a larger story.
Starfall ends with an unresolved transmission, not a final resolution: the
receiver reports that no further coordinates can be decoded yet, while the crew
and player voyage continue. Later chapters can extend the route without changing
the existing seven stages or saved progress.

The receiver journey now follows seven locations, beginning with the original
game's Radio Tower:

**Radio Tower → Glasswatch → Garden Relay → Echo Vault → Ember Beacon → Tideworks Haven → Starfall Archive**

| Location | Exploration and rewards |
| --- | --- |
| Radio Tower | A weathered broadcast station with a low operator shack, oxidized steel lattice, stacked service platforms, glass transmitter cabin, mast and first journal. Its signal starts the main receiver route. |
| Glasswatch | A storm-battered lookout with signal flags, chart tables, supply barrels, a glazed upper platform and a repaired line of sight toward the garden relay. |
| Garden Relay | A working seed greenhouse with irrigation channels, composters, rain barrels, planting beds, relay switches and a surviving seed bank. |
| Echo Vault | A reinforced map room with copied charts, shelves, archive tables, mossy approach markers and the sealed route puzzle. |
| Ember Beacon | A 49-block coastal headland with a tall masonry lighthouse, exterior ladder, copper roof, supply cache and keeper's journal in the lantern room. Copper and coal help rebuild the workshop. |
| Tideworks Haven | A 65-block harbor island with a long supported timber pier, abandoned workshop, tools, rain tanks and separate supplies. The harbor journal and relay key lead to the final archive. |
| Starfall Archive | A 49-block island with two joined ridges, a glass observatory, copper telescope sculpture and sealed masonry bunker. Recover its journal, lapis, diamonds and a usable native spyglass. |

All seven islands have irregular shores and native underwater kelp, clay, gravel,
copper, iron and coal. Buildings, item art and lore are original. Journal names,
new lore and receiver destination names have English and German translations.

For Starfall's bunker, face the seal and set **left ON, middle OFF, right ON**.
Use the relay key from Tideworks Haven on either seal block. The key is consumed
once in Survival; the opening remains accessible to the crew. The existing Echo
Vault puzzle retains its behavior.

## Existing worlds

Journals remain reusable, and reading an earlier journal cannot lower progress.
Progress survives save/reload and retains the existing copy-on-death behavior. The receiver keeps
its bounded search and cache, including the antenna upgrade's longer range.

New templates generate only in unexplored Ocean World chunks. Existing islands,
opened containers and raft construction remain saved. The existing story set's
spacing, separation and salt are retained; three weight-one destinations join its
selection. The ten ordinary variants keep their separate placement set.

Use matching **0.9.14** client/server builds (protocol `raft-movement-9.14`).
The JAR is `build/libs/raft_ocean-0.9.14.jar`. The 0.9.13 distant-deck collision
fix remains included. Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25,
ModDevGradle 2.0.147 and Gradle 9.2.1 remain pinned.

## In-game checklist

- Scan after reading the Radio Tower Journal. The next destination should be Glasswatch.
- Scan after reading the Vault Journal. The next destination should be Ember
  Beacon, including on an existing stage-3 player save.
- Climb the lighthouse, open its journal chest, and check that the receiver next
  points to Tideworks Haven. The lighthouse ladder should stay attached.
- Explore the harbor workshop and pier. Recover the Harbor Journal and relay key,
  then scan for Starfall Archive.
- Visit the observatory and unlock the bunker. Wrong switches or a missing key
  should leave the seal closed without consuming an item.
- Read Starfall's journal. The receiver should report the completed seven-stage
  journey; reading older journals should leave it completed.
- Harvest shoreline kelp/resources, save/reload, and repeat with another player.
- Switch language to German and check new item names, journals and island signals.

For direct testing with commands enabled:

```text
/locate structure raft_ocean:ember_beacon
/locate structure raft_ocean:tideworks_haven
/locate structure raft_ocean:starfall_archive
```

Build and both isolated server runs pass, including 632 Ocean World checks (61
new story checks) and all existing movement, station and survival checks.
Native server checks exercise actual chunk generation, template sizes, shoreline
placement, ladders, journal/key loot, both sealed bunkers and stage-3/stage-6 player
serialization. Visual exploration, client screens and connected multiplayer remain
live tests. Receivers can report no signal until you sail farther or upgrade the
antenna; searching remains bounded. Creative crews can obtain the new journals
from the existing survival/exploration tab.

Asset source: `tools/generate_island_assets.py` for Radio Tower and the original
three-stage route; run `tools/generate_story_expansion.py` after the base island
generator when regenerating all assets. Its NBT, JSON, localization and pixel-art
outputs are deterministic; repeat runs do not duplicate structure-set entries.
