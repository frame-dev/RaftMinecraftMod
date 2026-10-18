# Story schematics — 0.13.2 preview

Advanced technology now needs an island discovery as well as studied materials.
A schematic records its discovery on the next server inventory tick after entering
any player inventory slot. It is not consumed. Store, share or discard the physical
copy afterward; the discovery is saved, copied on death and retained on reload.

## Where to find them

Each main story journal chest has one guaranteed schematic, in addition to its
existing journal, keys and supplies. The 28 side-site field notes are unchanged.

| Story island | Original schematic | Research projects |
| --- | --- | --- |
| Radio Tower | Signal Survey Folio | Signal array / antenna and upgraded receiver |
| Glasswatch | Tide-drive Plans | Steering assembly and marine engine |
| Garden Relay | Garden Fuel Notes | Bioenergy machines and fuel distribution |
| Echo Vault | Waterworks Diagrams | Water automation |
| Ember Beacon | Beacon Power Ledger | Batteries, cables and charging |
| Tideworks Haven | Deepwater Equipment Folio | Diving equipment and advanced salve |
| Starfall Archive | Reef Armament Plans | Advanced weapons and arrows |

Collecting a plan does **not** study its materials or learn its recipes. At the
research table, study complete material batches (2–4 samples) and press Learn
once the project is ready. Missing-plan projects show **Explore**; hover for the
required schematic and its island. Crafted-output research restrictions and JEI
visibility continue to use learned projects, not possession of paper.

The initial receiver, sail, smelter, starter food/water, and basic equipment have
no new story gate. Their existing research/crafting costs remain. Engines retain
the requested1.5m/s nominal base speed, sails1.0 and paddles1.8; delays, terrain,
models, collision geometry and the open-ended ending are unchanged.

## Older saves and co-op

Previously learned recipes stay learned without requiring new paper. Previously
studied materials stay studied. An unlearned advanced project now needs its plan.
Read an existing main island journal to recover that island's schematic discovery,
so a previously emptied chest does not block progress. Journals are not consumed.
Existing JOURNEY/knowledge bit assignments are not changed.

When shared research is enabled, eligible members of the same saved raft crew
pool discoveries, including offline members when they next synchronize. Guests
neither publish nor receive shared research. Switching sharing off leaves personal
progress in place. Leaving a crew does not erase knowledge already learned.
The optional new crew-save field defaults to zero for older crew data.

`/raft research unlock` (or `/raft research unlock @a`) now also discovers every
schematic. Creative crafting and the existing research-disabled configuration
retain their bypass. Individual plans are available through Creative and normal
`/give` item IDs, for example `raft_ocean:schematic_tide_drive`.

All peers need0.13.2; the handshake version is13.2 and addon API remains3.
Back up worlds before updating. Unopened loot containers resolve their current
loot table; containers whose loot was already generated retain their contents.
Use the journal recovery path or explore a new island rather than deleting regions.

## Data and extension boundaries

`data/raft_ocean/story_schematics.json` is the bundled registration catalog:
stable ID, source island, main chest loot table and the existing project IDs.
Its seven ordered discovery bits are append-only; never reorder old entries.
This is startup content, not a live datapack reload API. The item registry and
client catalog must match the server. Existing data-driven research material,
unlock tags and main chest loot tables remain adjustable through their normal
resource systems. No schematic crafting recipes are supplied.

The seven folios reuse this mod's original 3D journal geometry and existing
navy/teal materials, with English/German labels. No external game assets or lore
were copied.

## Client test checklist

- With researched materials but no new plan, engine research shows Explore and
  clicking cannot learn it or consume anything.
- Collect Glasswatch's plan into a backpack slot. A discovery message appears;
  the project becomes Learn only when all its material requirements are met.
- Learn the project, lose/store the paper, relog and verify access remains.
- On an older save, read the Glasswatch journal to recover the plan discovery.
- With shared research enabled, a crew member receives discoveries; a guest does
  not. Confirm disabling sharing prevents new transfers.
- In a fresh Ocean World, inspect all seven main chests, the plan models and
  tooltips in both languages. Continue the existing puzzles and open-ended story.

Server checks cover native inventory ticking, exact research gates, live menu
refusals, player serialization/death copy, old crew codec defaults, sharing and
actual generated island loot. Client presentation and real network latency still
require playtesting.
