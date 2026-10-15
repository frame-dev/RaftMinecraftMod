# Interaction polish — 0.12.1 preview

Net pickup now gives specific feedback:

- Empty: the net contains no cargo.
- Full inventory: the cargo remains safely in the net.
- Partial pickup: the message shows both collected items and items left behind.
- Complete pickup: the normal collected-items message appears.

Transfers retain the existing server validation and stack/component rules.
Leftovers stay in the shared net for another crew member; Creative mode does
not destroy cargo when the inventory is full. New English/German labels are included.

Placement hints now belong to a specific raft rather than whichever raft was
rendered most recently. A hint from another raft is hidden immediately, and
disconnect/reset clears the hint and its timestamp with the build rotation.
The owning raft reference is weak, so it does not retain an old client world.

## Playtest

1. Sneak-use an empty collection net with an empty hand: expect the empty message.
2. Fill your inventory, then try a loaded net: expect the full-inventory message
   and unchanged cargo.
3. Leave room for only part of a stack: expect the collected/remaining counts,
   then let another player retrieve the leftovers.
4. Aim between two nearby rafts with different valid/blocked placement targets:
   each hint must describe the raft under the crosshair.
5. Disconnect and rejoin; a placement hint should appear only after targeting
   a raft again. Try this with rotation controls and offhand building items.

The save format, physical collision system and open-ended story remain included.
These focused changes do not establish a bug-free live multiplayer session.
