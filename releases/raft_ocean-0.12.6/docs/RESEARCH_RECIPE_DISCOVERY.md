# Research recipe discovery — 0.11.25

Version 0.11.26 adds an administrative [unlock-all command](RESEARCH_COMMAND.md).

This update supersedes 0.11.24's research-free vanilla workstation conversions.
Conversion recipes now follow the same research requirement as the corresponding
materials-based recipes. A vanilla chest or barrel produces no crafting output
until Sealed storage is learned.

Locked crafting recipes are also hidden from the normal recipe book and JEI.
Collecting their ingredients cannot discover them early. Learning a project
reveals all its associated crafting recipes immediately, including workstation
conversions and alternative material recipes. Studying material samples alone
does not learn a project: use its Learn button at the research table.

For a raft chest, study the required planks and fasteners, then learn Sealed
storage. Its conversion becomes visible; put a vanilla chest alone in either
the 2x2 inventory grid or a 3x3 crafting grid to get one raft chest.
Starter recipes without a research project remain available normally.

## Multiplayer and existing saves

Each player's learned projects are synchronized privately to that player.
Another player's research does not reveal your recipes. Learned projects still
persist through saving, reconnecting and death. Login removes stale locked
entries from existing recipe books and restores researched entries.
Research resets and configuration/gamemode changes update discovery too.
Creative mode and disabling research reveal recipes and bypass crafting locks.
Returning to Survival with research enabled hides unfinished projects again.

Use 0.11.25 on both server and clients: the connection protocol is now
`raft-movement-11.25` because research knowledge now synchronizes to clients.
The addon API remains version 2. No new world is needed; existing items remain
usable. This controls crafting/discovery, not ownership of previously obtained
items or their placement.

## Validation

Build and the complete isolated native server suites passed, including all 1,389
loaded crafting recipes, 256,827 combined crafting assertions and 450 research
assertions. Regressions verify locked conversions cannot consume ingredients,
learning reveals recipes, ingredient rewards cannot bypass discovery, stale
saved entries are hidden, and reset/config/Creative changes update the book.
The updated per-recipe lock report is
[crafting-audit-0.11.25.csv](crafting-audit-0.11.25.csv).

JEI integration compiles against the pinned 31.9.0.58 API. Its visible screens
and live reveal behavior require a local client check.

## Local checklist

1. In Survival with research enabled and Sealed storage unlearned, search for the
   raft-chest crafting recipe in JEI and the recipe book: expect it hidden.
   Putting a vanilla chest in either grid should produce no output and consume
   nothing.
2. Learn Sealed storage: expect the recipe to appear immediately and one chest
   to convert into one raft chest, including right-click and shift-click.
3. Reconnect: expect learned recipes to remain visible. Test with another player
   who has not learned storage: their recipe should remain hidden/locked.
