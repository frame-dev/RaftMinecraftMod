# Expedition notebook — 0.13.4 preview

The original notebook brings recovered lore together in a personal, persistent
archive. It follows the gameplay role of Raft's [journal](https://raft.fandom.com/wiki/Journal),
using this mod's own writing, artwork and island names.

## Open and read

- Use `/raftnotebook` without operator permissions, anywhere you can issue commands.
- Select **Notebook** in the mounted survey receiver header.
- Sneak-use any of the seven main island journals or three optional-route journals.
  Normal use still reads the journal and advances its existing discovery behavior.
- Select Journey, Field notes or Side routes. Left arrows change entries;
  right arrows turn text pages. All original text is available in English and German.

Journey includes the next known signal and earlier journal texts inferred from
saved journey progress. This keeps older saves readable. Optional-route logs use
their existing three saved discovery bits. Neither view advances progress.

## Field-note collection

There are 28 original field notes, four per authored story island. Acquiring their
written books records their IDs permanently. Ground pickups and using a book record
immediately; books moved from chests are recovered from the player's inventory at
most one gameplay second later. Opening the notebook also checks inventory and the
carried cursor stack. Keep a newly looted book until its archive notification appears
before discarding or storing it. Previously collected books must be retrieved once
if they are currently stored in a chest. Uncollected notes are not revealed.

The notebook stores only recognized authored note IDs, identified by the existing
translated two-page book content. It does not copy arbitrary player-written books.
Books are never consumed, changed or duplicated. A note does not advance the main
journey, unlock recipes, or reveal future story destinations. Notebook progress
survives logout, save/load and death, including death with dropped inventory.

The archive is personal. Crew members can pass books around to archive them;
opening a receiver does not expose another player's personal archive. Spectators
cannot collect notes or open this notebook. No new item or crafting recipe is needed.

## Limits and testing

- This adds an archive, not a numeric frequency-code system or multi-signal radar.
- Earlier journey progress restores earlier journal text; it cannot reconstruct
  historical side-note pickups that were never recorded by an older version.
- The last journey destination remains an unresolved signal. The story stays open-ended.
- The notebook is read-only and has no inventory slots. Native menu synchronization
  sends only the owning player's masks and progress, split into short-safe values.
- Update all multiplayer peers to 0.13.4 (handshake13.4); API3 is unchanged.

Client test: loot a field-note book, wait for the archive message, open the notebook,
read both pages, put the physical book away, then relog and verify it is still readable.
Test long German text, all entry/page buttons and the smallest GUI scale that you use.
In co-op, confirm a crewmate sees their own archive and can record a passed book.
Native server checks cover all 28 real loot tables, item conservation, duplicate
collection, inventory recovery, saved masks, death copying and menu isolation.
