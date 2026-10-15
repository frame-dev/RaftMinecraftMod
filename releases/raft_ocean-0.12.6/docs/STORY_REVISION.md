# Story voyage revision — 0.11.37

Install `build/libs/raft_ocean-0.11.37.jar` on every client and the server. Targets remain Minecraft 26.3, NeoForge 26.3.0.48-beta and Java 25. Protocol is 11.37; addon API remains 2.

## Rarer story islands

The story structure pool now uses 48-chunk placement regions and 24-chunk separation, instead of 32/16. Drift Atoll has weight 7 instead of 3; each of the seven story locations retains weight 1. This reduces the theoretical story-island candidate density by about 68%, before terrain/generation effects. It does not guarantee a fixed distance between islands or between copies of one chapter.

Ordinary resource islands, exploration islands, underwater dive sites and trading outposts retain their existing placement settings. New spacing applies to newly generated terrain. Existing islands are not moved or removed. Sail into unexplored water or start a new Ocean World to assess the difference.

The receiver remains bounded to its current search and distance limits: a basic receiver displays signals within 3,072 blocks, while the upgraded receiver supports 8,192 blocks. A specific chapter can be outside that range; sail onward and scan again. The stronger antenna becomes more useful on the longer voyage.

## Seven connected journals

Every journal now has its own matching chapter, in English and German:

| Island | Author / focus |
| --- | --- |
| Radio Tower | Ivo: the first watch, missing crew names and the first answering light |
| Glasswatch | Mara: an abandoned watch, uncertain charts and the decision to continue |
| Garden Relay | Sela: the seed tin, missing supply boat and the vault's switch clue |
| Echo Vault | Ivo: responsibility for the unsafe route and a missing ledger page |
| Ember Beacon | Mara: the rescued passengers, repaired light and strange handwriting |
| Tideworks Haven | Sela: the repaired tanks, an unexplained record and the archive's switch clue |
| Starfall Archive | All three: conflicting evidence and an unfinished eastern transmission |

The cracked cup, seed tin, watch notes and ledger recur across different voices. Later entries answer earlier questions about the supply boat while introducing an unresolved mystery. Each intermediate chapter gives a usable next destination; both bunker clues keep the existing switch combination and key requirement.

The former six-entry text did not match the seven registered journal stages. The revision supplies all seven and fixes their island-to-text correspondence. Journal reading, keys, puzzle logic and saved journey stages are unchanged. Existing journal items show the revised text when read; progression never moves backward. Completed crews receive an open-ended message rather than a fabricated destination.

There is no new playable island after Starfall yet. The caller, ledger and final words remain unresolved for later story expansion. This update adds narrative text, not new quest objectives, voiced dialogue or additional lore-item mechanics.

## Playtest

1. Explore new Ocean World terrain: story landmarks should be less frequent, while resource islands retain their familiar distribution.
2. Read Radio Tower and then Glasswatch journals: the heading and author should match each island, and the receiver should point to the next chapter.
3. Read Garden Relay and Tideworks Haven entries: both should explain `LEFT ON, MIDDLE OFF, RIGHT ON`, followed by the key.
4. Read Starfall Archive: it should display the final unresolved transmission. The receiver should explain that no further story destination is available yet.
5. Change the language to German and reread the journals; paragraphs, clues and progression should remain intact.

Sources: `tools/story_lore.py` is the canonical narrative. `tools/generate_island_assets.py` owns base structure placement; the expansion generator preserves it. `tools/verify_story_assets.py` checks packaged journal alignment, clues, model references, placement and the other island pools.
