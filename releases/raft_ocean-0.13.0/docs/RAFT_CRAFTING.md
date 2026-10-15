# Raft-mounted crafting fix — 0.4.2

The Raft Crafting Table is an owned structural piece with an original four-leg
wooden model. It moves, collides, contributes 6 mass, synchronizes, and saves with
the raft. It mounts flush on foundations and upper floors. Existing 0.4.0 worlds
can load the update without creating a new world.

## Build and launch

Close the old client/server, then run:

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
```

Verify **Raft Ocean 0.6.0** in Mods. Use this version on every connected client and
server; the required protocol rejects older builds that cannot decode the table
piece. Artifact: `build/libs/raft_ocean-0.6.0.jar`.

## Craft without a stationary block

A vanilla crafting table can now be placed directly on the raft. If you prefer
the raft item, either recipe works in the inventory's 2x2 crafting grid:

1. Craft a vanilla crafting table from four planks. Put that item alone into the
   crafting grid to convert it to a **Raft Crafting Table**.
2. Or use three planks and one stick, with this pattern (P = any plank; S = stick):

```text
P P
P S
```

Two planks placed vertically in the inventory grid make sticks. The recipe book
learns the new recipes when you acquire planks. The item also appears in the Raft
Construction Creative tab, or can be obtained for testing with:

```text
/give @s raft_ocean:raft_crafting_table
```

## Place and use

- Hold the raft table and right-click a foundation or floor. Step away from the
  target cell first so your body does not obstruct placement. The usual support,
  weight, reach, and obstruction checks apply.
- Right-click the placed table with an empty hand or a non-building item to open
  the normal 3x3 crafting screen, including recipe-book and shift-click crafting.
- The server checks reach against the table's current raft position. Drifting
  together keeps the screen open; leaving eight-block range, removing the table,
  removing the raft, or changing dimension invalidates it.
- Crouch + right-click with the building hammer removes/refunds the table. A
  mounted table must be removed before its supporting thin floor.
- Closing the screen returns remaining grid items to the player's inventory, or
  drops them using vanilla behavior when they cannot be returned. Crafting grids
  are per player and temporary, just like a vanilla crafting table.

Supported vanilla station items now mount as raft-owned pieces; see
[storage and tool stations](RAFT_STATIONS.md). The crafting table does not store
items between uses. Other arbitrary world blocks cannot be mounted.

## Test checklist

- [ ] Craft either recipe in survival using only the player inventory. Obtain a
  Raft Crafting Table without placing a stationary world block.
- [ ] Place it on a foundation and an upper floor. It should sit flush, show its
  original model, collide, and move with the raft. Save/reload should preserve it.
- [ ] Open it while drifting. Craft a foundation and a vanilla recipe. Output and
  ingredient consumption should match normal crafting; shift-click should work.
- [ ] Put leftover ingredients in the grid and close the menu. They should return
  once, with no lost or duplicated items.
- [ ] Walk away, or have another player remove the table while your screen is
  open. The screen should close and return ingredients.
- [ ] With two clients, open the same table simultaneously. Each player has an
  independent grid and inventory; closing one screen should not change the other.

Automated validation passes: 91 standalone geometry/movement regressions, 725
runtime carry assertions, 208 construction assertions (including table save/load),
13 crafting assertions, and 43 Ocean World assertions. Crafting checks run the
production menu on an unregistered raft/fake player and verify recipe resolution,
shift crafting, ingredient consumption/return, movement-relative reach, and
table removal. Connected-client screen opening/rendering still needs the checks
above. Both isolated test servers shut down cleanly.

Current component recipes and direct storage crafting: [crafting chains](CRAFTING_CHAINS.md).

The current survival stations and moving beds are covered in [milestone 6](MILESTONE_6.md).
