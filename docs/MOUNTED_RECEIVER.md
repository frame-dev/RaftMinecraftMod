# Mounted survey receiver — 0.13.3 preview

Build a Survey Receiver Console and three Survey Antennas on your moving raft.
Their recipes and the basic Tidal Battery are in the early Radio receiver research
project. Material research is still required; the seven story schematic gates
remain. Advanced cells, cables and charging keep their power-project gate.

## Build and operate

1. Craft the console in a 3x3 table: copper wires across the top; glass / handheld
   radio receiver / glass in the middle; timber brace / fastener / brace below.
2. Craft each antenna with wire in the top center, iron / wire / iron in the
   middle and a timber brace below. You need three.
3. Build an upper deck, then place the console and antennas. Their items use the
   shared raft placement ghosts, support, rotation and collision system.
4. Insert a charged Tidal Battery or Deepcycle Battery into the console's slot.
   Switch on, then press Scan signal. Switch off to retain remaining charge.

A new basic battery recipe produces1000 charge. A component-free battery stack
created through Creative or `/give` is still empty unless charge data is supplied.
Existing battery stacks keep their charge. Advanced battery crafting retains its
existing empty default. Holders of the already learned old Power project retain
basic-battery crafting access even if they never learned Radio receiver research.

## Antenna setup in Minecraft units

- Console anchor must be at raft-localY4 or above: an actual upper floor, rather
  than the foundation surface. A standard tall-pillar upper deck works.
- Antenna anchors must be within one vertical block of the console anchor.
- Each must be3–15 horizontal blocks from the console.
- A valid set of three must be at least5 horizontal blocks apart pairwise.
- Extra antennas are allowed; the receiver finds any valid triangle on this raft.
  Antennas on other rafts cannot satisfy the setup.

The screen reports height, missing antennas, spacing, battery and world status.
The wiki receiver/antenna loop is the design reference, adapted to this mod's
one-meter construction grid: [Raft receiver reference](https://raft.fandom.com/wiki/Receiver).
Models, UI and existing story content are original.

## Radar and story channels

The radar rotates with the installed console and hull. Up means forward from
that console; it is not a north-up map. The central gold marker is your receiver
and the blue marker is the selected destination. Display range adjusts to fit
that destination, with a minimum512m scale and maximum8192m search range.
The sweep animates while an operational receiver is searching or tracking.

Next clue selects the current personal journey stage. Arrow buttons revisit
known stages; a player cannot scan a future story stage through forged button
IDs or channel selections. The console selection is shared equipment state,
while existing journal progress remains personal. Read/share the existing journals
with your crew to advance each member's journey.

Scans use native structure tags and an explicit server search. The same channel
and nearby origin reuse a60-second cache; all channel changes also obey a10-second
search cooldown. Press Scan again after traveling if you need a new target.
Searches use a radius of six structure-placement regions, and destinations farther
than8192blocks are not shown. This is a single story-signal tracker. A multi-signal
island map, entered numeric codes and a persistent notebook are future features.
After the last current chapter, the unresolved eastern channel remains open.

With a valid setup in Ocean World, the enabled console consumes2charge per loaded
second: a full basic cell lasts500seconds. No charge is spent while switched off,
undercharged, outside Ocean World or missing the antenna setup. GUI refreshes,
button spam and scans do not add extra consumption. This readiness behavior and
explicit scanning are deliberate Minecraft adaptations.

## Save, co-op and old equipment

The API machine inventory saves the battery's native charge components and console
settings with the raft. Removing a console drops its battery with charge retained.
Menus invalidate on removal, lost reach, death or revoked crew permissions.
Guests and spectators cannot operate the equipment. Antenna layout caches update
when pieces change; temporary client display never controls movement or resources.

Existing handheld receivers, upgrades, journey saves and seven story schematics
remain usable. An unplaced console can be crafted back to a handheld receiver;
that recipe returns the receiver only. Install0.13.3 on every peer (handshake13.3).
No new world is needed to craft and mount equipment on an existing raft.

## Verification and playtest

Native checks cover supported upper decks, all four console rotations, hull yaw,
antenna triangles, extra bad antennas, removal/cache invalidation, safe menu data,
owner/reach/button validation, battery conservation, native drops/save/load and
bounded actual Ocean World scans. Crafting/research and existing movement suites
remain covered. New models pass native parsing, centered inventory fitting and
surface/UV audits; seven inherited folio models are now included in coverage.

Please test in a real client: build the console upstairs with three spread antennas,
insert a crafted basic cell, switch on and scan. The next island should appear;
rotate the hull and verify the blip follows the console's forward direction.
Move/remove an antenna, switch off, remove the battery and relog: charge should
remain correct. Check crew permissions and both languages. Actual network latency
and client appearance still need playtesting.
