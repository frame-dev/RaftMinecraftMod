# Unlock all research — 0.11.26

The research unlock command permanently learns all projects and material samples
for the selected players. It reveals their recipes in the recipe book and JEI,
awards research advancements and refreshes crafting outputs already in a grid.
It consumes no items and keeps the player's current game mode.

## Commands

In-game, unlock your own research:

```text
/raft research unlock
```

In-game, unlock another online player or every online player:

```text
/raft research unlock PlayerName
/raft research unlock @a
```

From the server console, specify targets and omit the leading slash:

```text
raft research unlock PlayerName
raft research unlock @a
```

Replace `PlayerName` with the player's actual name. Standard online-player
selectors are supported. The command requires cheats/operator gamemaster
permissions; the server console can run it directly. The no-target form requires
an executing player, so it is intended for in-game use.

The unlock persists after reconnecting, saving and death. It affects only the
selected players and does not change the shared research configuration.
Repeating the command is safe. Players not selected retain their existing
research progress and recipe locks.

## Verification

Build and isolated native server checks passed: all 1,389 loaded crafting
recipes, 256,827 combined crafting assertions and 553 research assertions.
The command regression executes the registered command, verifies all projects,
materials, recipe-book entries and advancements, checks that Survival and
inventory items stay intact, refreshes a previously locked chest conversion,
repeats the command and parses the server-console selector form.
English/German command feedback is included.

Install 0.11.26 on client and server, or rebuild the development client. The
network protocol remains `raft-movement-11.25`; addon API 2 remains unchanged.
No new world is needed. Run the command in Survival, then open the research
table and JEI: expect every research project learned and its recipes visible.
Check after reconnecting that the unlock remains.
