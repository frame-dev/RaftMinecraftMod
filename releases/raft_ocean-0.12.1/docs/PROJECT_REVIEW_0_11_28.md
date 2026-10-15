# Project error review — 0.11.28

## Fixes

A native regression reproduced a stale crafting result: after research was
removed, clicking an already assembled raft chest still produced it and consumed
its vanilla chest ingredient. A similar window existed when leaving Creative
or reenabling the research requirement before the next synchronization tick.

Crafting now revalidates research before result clicks and direct shift transfers.
Reconciliation also recalculates existing inventory and active crafting grids,
so learning a project reveals its output without moving an ingredient. This
covers inventory 2×2, vanilla 3×3 and both raft crafting table variants. Rejected
results preserve inputs and the carried stack. The unlock command uses this same
refresh path.

Standalone archery and battery asset generators could reintroduce older research
assignments after the 30-project expansion. They now restore the canonical catalog:
advanced bows/arrows belong to Reinforced weapons; batteries and the charger
belong to Electrical storage. This was exercised in a separate copy of the assets,
without regenerating the project's existing artwork.

The packaged diving verifier still expected the superseded Tool workshop tag.
It now checks Diving equipment, including the salve. A new research asset audit
checks unique project ownership, all recipe discovery rewards, registered model
coverage, bilingual project labels and ungated starter equipment.

## Checks performed

- Build and both isolated dedicated-server runs passed.
- 257,837 combined crafting assertions across all 1,394 loaded crafting recipes
  (128 mod and 1,266 vanilla/other); 53 mod recipes fit 2×2, 75 need 3×3.
- 666 research assertions, including stale result rejection, immediate unlocks,
  configuration/Creative changes, persistence and the administrative command.
- 756 ocean-world assertions; collision, movement validation, construction,
  carry, machine API, survival, salvage, navigation and creature/config suites passed.
- Packaged recipe/model/research/diving/localization audits passed. Registry
  coverage: 126 custom items, 334 blocks and seven entity renderers.
- 528 model files, 164,215 atlas-safe faces and 60 polygon meshes passed surface
  validation; existing model envelopes were preserved.
- 542 English/German keys passed parity checks. All Python tool syntax and all
  source resource JSON parsed. Both changed standalone generators preserve the
  30 research projects and 60 unique unlocks in an isolated generation test.

Evidence: `build/project-review-reproduction.log` (failure before the fix),
`build/project-review-runtime.log` (successful verification),
[crafting report](crafting-audit-0.11.28.csv).

No new collision, island, texture-reference or processing failures were found
in these automated checks. This does not establish that every live client or
multiplayer issue is resolved.

## Local test checklist

Install 0.11.28 on all clients and the server; no new world is needed. Network
protocol 11.27 and addon API 2 are retained. When testing from the project, close
the running development client before building and launch it again afterward.

1. Use an existing world and verify that research progress remains learned.
2. Keep a locked recipe in the inventory grid, then use `/raft research unlock`.
   Its result should appear without moving the ingredients.
3. On a testing save, prepare a research-locked result in Creative, switch to
   Survival without learning it, then attempt normal and shift pickup. The result
   must be blocked and all ingredients retained. Repeat at the raft table.
4. Check JEI hide/reveal behavior and walking on a moving raft with another player.
   Report any remaining teleporting, clipping or visual/model issue.

Milestone 11 continues. Story remains open-ended; release milestone 12 has not begun.
