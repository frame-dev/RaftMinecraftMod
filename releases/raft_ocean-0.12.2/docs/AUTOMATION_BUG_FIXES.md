# Automation and machine fixes — 0.11.9

This keeps milestone 11 and the open-ended story. Models, recipes, research and
world generation remain available from 0.11.8.

Fixed behavior:

- A connected purifier above dry land no longer creates generator fuel demand.
  A sprinkler with an empty water reservoir also cannot trigger fuel burning.
- Fuel, water and power rotate priority between eligible consumers. A low supply
  no longer always favors the station with the first raft coordinates. Sprinklers
  also rotate watering between nearby thirsty planters.
- Multiple sources can deliver at most one cup or one fuel canister to each
  recipient per network update. Full or paused receivers are skipped.
- Sprinkler collision follows its foot and narrow stem. The thin nozzle is cosmetic
  and no longer blocks shoulder space like a solid machine cube.
- Disabling automation clears its activity status, stopping stale working animations.
- Entity saving now retains processing duration as well as progress. A restored
  machine's progress bar has its correct denominator before its next processing tick.

## Pause and resume

Every survival machine, including tanks, generators and the biorefiner, has a
**Pause / Resume** button below the inputs. Pausing stops processing, automatic
cup exchange and automatic utility transfers for that machine, preserving its
inventories, progress, stored water, stored energy and remaining fuel. Other machines
can continue using the same connected pipes; pause does not disconnect the network.

Manual inventory retrieval and hand-held cup transfers still work. A paused
sprinkler cannot water crops; paused planters do not receive automatic irrigation.
A paused generator cannot contribute its energy to connected consumers.

Co-op viewers control the same pause state. Each player retains their own Supplies
page. Carried stacks, unknown controls, spectators and invalid station reach cannot
toggle the control. The pause state survives saving; old saves default to running.

Use 0.11.9 on every client and server. The menu protocol is now
`raft-movement-10.4`, since the screen needs an additional synchronized pause value.

## Verification

Build and native model fitting passed. Native server suites passed 425 collision,
729 carry, 810 construction, 48 crafting, 119 station, 188 research, 108 salvage,
481 survival, 130 navigation and 593 creature/config assertions. Ocean-world checks
also passed during this change. Packaged coverage remains 100 custom 3D item models
and 273 custom blocks. All 481 English/German keys and both open story endings pass
the packaged resource audits.

New regressions exercise actual scarce water/fuel/power allocation, dry consumer
fuel preservation, multiple water sources, saved pause/duration, resumed refinery
progress and shared native menu controls. Collision tests walk in small steps past
a sprinkler at eight hull angles. They complement existing holes, upper decks,
carry, removal and movement checks.

Logs: `build/automation-fixes-final2.log` and `build/automation-fixes-native.log`
(the latter's ocean suite passed; its initial collision test was subsequently
corrected to use normal walking steps).

## Test locally

1. Keep a fueled generator connected to a dry purifier or dry sprinkler. Expect no
   canister to burn. Restore water; expect processing to resume.
2. Connect two engines to a tank and add fuel one canister at a time. Expect both
   engines to receive supplies. Try the equivalent with two thirsty planters.
3. Pause a machine mid-batch, save and reload. Expect the same supplies and progress.
   Resume it; the batch should continue. Test the same control with two co-op viewers.
4. Walk past a sprinkler while sailing and turning. Its empty shoulder space should
   be clear; the small stand still has collision.

Known limits: utilities still occupy separate building cells and have static ports.
Actual OpenGL screen/model rendering and connected human co-op latency need local
verification. This does not establish that earlier intermittent player teleport
reports are resolved; no recent reproduction was supplied.
