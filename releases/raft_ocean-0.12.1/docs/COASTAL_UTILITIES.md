# Foundation reinforcement, quick stash and lifecycle fixes — 0.11.17

Targets: Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25,
ModDevGradle 2.0.147, Gradle 9.2.1. Install the same release and addons on
client and server. Network protocol is now `raft-movement-11.17`.
Existing foundations and saved inventories remain readable.

## Reinforce foundations

Craft the existing **Reinforced Brace**: two Timber Braces, one Metal Plate
and one Binding Paste make two braces in any crafting table. Hold a brace
and right-click an exposed foundation to reinforce it. Each foundation uses
one brace in Survival; Creative consumes none. Applying armor again spends
nothing. Ordinary plank repairs with the building hammer still work.

The original deck gains four metal edge strips. They sit within its existing
footprint and do not add collision boxes, steps or raised lips. Reinforcement
preserves existing damage: it protects a 75-health foundation at 75 health
rather than granting a free repair. It persists with foundation health.
Sharks choose exposed unarmored foundations; an armored foundation cannot
receive ordinary shark foundation damage while protection is enabled.
Sharks can still attack swimming players. Peaceful behavior is unchanged.

Sneak-click with the building hammer to remove the foundation normally.
A successful Survival removal refunds one foundation and one brace. Existing
support/connectivity rules can prevent removal. Creative removal produces
neither refund. Lost support due to destruction still follows ordinary raft
pruning; reinforcement is not an exemption from connectivity.

Config `building.foundationReinforcement` defaults to true. Turning it off
prevents new reinforcement and disables protection but preserves saved armor.
The edging remains visible, and re-enabling restores protection for surviving
foundations. Armor does not automatically enable hostile sharks in Peaceful.

## Quick-stash materials

Sneak-right-click a raft chest or barrel **with an empty hand** to deposit
matching materials from backpack slots 10–36. A material must already be in the
storage. The hotbar, armor, offhand and unmatched supplies remain untouched.
A translated message reports the number of items stored.

Partial stacks fill first; then free slots receive the remainder. Double
chests use both halves as one destination, so clicking either half works.
Full storage leaves excess items in your backpack. Items must have matching
components: custom names, damage and other stack data are preserved. The
transfer moves actual stacks even in Creative; it never creates copies or
throws overflow into the ocean. Two players share the server's real inventory.

Use ordinary right-click to open the inventory. Config `building.quickStash`
defaults to true. When disabled, sneak-click opens the ordinary storage screen.
This action does not deposit into furnaces, machines, nets or Ender Chests.

## Bugs fixed

An addon removal callback could ask for a machine context while the intact
raft was being destroyed. Because its footprint still existed, the old host
could create a fresh state owner during cleanup. Destruction now closes owner
lookup before callbacks and guards subsequent ticking/snapshot generation.
Callback code still receives the original context and inventory for cleanup;
it must not reopen a destroyed machine. Normal chunk unload/save remains
separate from destruction and preserves inventory. The same owner guard applies
when losing the final foundation, before the entity itself is discarded.

Machine load callbacks also ran before the stored hull direction and other
raft systems were fully restored. They now run after restoration, so an addon
that uses moving machine coordinates during load receives the correct frame.

This update does not claim to resolve every reported multiplayer movement
correction. Existing collision/carry suites are run again; human latency tests
and large-raft performance remain pending.

## Local test checklist

- Armor a damaged foundation; verify one brace is consumed, metal edges appear
  and a second attempt consumes nothing. Confirm plank repair still works.
- Let a shark approach an armored edge: it should choose an available unarmored
  edge. Save/reload and verify armor and health. Test Peaceful separately.
- Remove a reinforced corner using the hammer: expect one foundation and one
  brace. A supported station may prevent the removal until it is removed first.
- Stash into a barrel and each half of a double chest. Check full storage,
  partial fits, named items, hotbar preservation and repeated clicks.
- Test two players using storage on a moving raft. Check that total item counts
  remain constant. Restart both client and server after updating.

Implementation files:

- `src/main/java/dev/raftocean/entity/RaftEntity.java`
- `src/main/java/dev/raftocean/survival/QuickStash.java`
- `src/main/java/dev/raftocean/survival/CoastalUtilityCheck.java`
- `src/main/java/dev/raftocean/creature/OceanCreature.java`
- `src/main/java/dev/raftocean/client/render/RaftRenderer.java`
- `src/main/java/dev/raftocean/api/internal/RaftMachineHost.java`

Build and native server checks passed, including 17 coastal utility, 29 machine
API and eight example-dryer assertions, plus the existing movement/collision and
survival suites. All four packaged audits passed. Full results are recorded in
`PROJECT_STATE.md`; server log: `build/coastal-utility-final.log`. Visual appearance
and real multiplayer latency require local testing.
