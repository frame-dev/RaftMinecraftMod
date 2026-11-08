# Building hammer palette — 0.13.9 preview

The hammer now selects and places existing crafted raft pieces without moving
those pieces into your hand. This adds the building-selection/repair portion of
the [Raft hammer reference](https://raft.fandom.com/wiki/Building_Hammer), with
Minecraft controls and the mod's existing recipes, models and placement rules.
It does not yet convert raw wood/rope directly into a placed structure.

## Use it

1. Craft a Building Hammer using its existing recipe. Keep crafted foundations,
   nets, walls, stations or decorations in your hotbar/backpack.
2. Equip the hammer and press the **Hammer Palette** key, default **B**. You can
   also use/right-click the hammer in the air. B opens it while looking at a raft.
3. Choose a category: Structure, Equipment, Cabin or Addons. Page through the
   available pieces. Each choice shows how many ordinary crafted pieces you carry.
4. Select a piece, close the menu, and use/right-click the raft to place it. The
   hammer stays equipped. Successful survival placement consumes exactly one
   selected piece from your hotbar/backpack; a rejected placement consumes none.
5. Use the existing **R** rotation control for machines/conduits. Walls continue
   to follow clicked edges and their existing corner-joining rules. Palette control
   labels follow remapped keys, including the mouse/keyboard language setting.
   The build angle stays in sync through respawn and returning from the End.
   Reconnecting resets it to the default angle.

The selected piece is saved separately on each hammer and survives world reload.
You can select a known piece even at count zero: its ghost turns red with a missing
pieces message. The palette never gives you a preview item or consumes an item
when you click a choice. Creative placement needs no carried piece.

### Repair and dismantle

Choose **Repair** to clear build mode. The existing hammer then repairs foundations
with planks, or turns machines/conduits using the old interaction. Older hammers
start in this mode, so updating does not automatically change their raft action.
Repair still consumes one plank and restores twenty-five foundation health.

**Sneak-use/right-click still dismantles** using the existing support/refund rules,
including when a build piece is selected. The build ghost is hidden while sneaking
so the two actions are visually distinct. Unsneak before attempting to build.
Foundation reinforcement still uses the existing metal brace item directly.

## Inventory, research and cooperation

- The palette draws crafted pieces from hotbar/backpack slots. It does not pull
  from chests, machines, other players, armor or offhand supplies.
- Renamed or otherwise component-modified pieces are preserved. The hammer spends
  only stacks with the selected item's ordinary default components. This avoids
  stripping custom names, data or container contents. Place modified items through
  their normal item interaction if supported by their adapter.
- Unlearned research choices stay hidden and cannot be selected through a forged
  menu click. Previously learned research remains learned. A stale saved selection
  cannot place a newly locked piece through the hammer.
- Server placement reuses the normal support, overlap, player collision, reach,
  foundation limits, footprint, corner joins, rotation and crew checks. It never
  trusts a placement position supplied by the client and never swaps your held
  hammer with a temporary item.
- Guest crew permissions and downed-player restrictions remain effective.
  Changing the held hammer or disabling the palette invalidates its menu.
- Registered addon buildables appear automatically in the Addons category, using
  the same item and machine placement callback as direct placement. A bounded
  menu supports up to512distinct build items. No addon API version change is needed.

Main-hand hammer use is recommended. Offhand selection is supported; Minecraft's
normal main-hand/station interaction priority still applies when clicking a raft.

## Configure

Under `[building]` in `raft_ocean-synced.toml`:

```toml
hammerPalette = true
```

False closes palette access and restores the old hammer's repair/rotate behavior.
Saved build choices remain on the item and become available again when re-enabled.
Direct placement using existing building items remains available either way.

No recipes, models, textures, movement formulas, predator delays or story chapters
are changed in this update. Raw-material construction, a radial wheel, hammer-based
reinforcement and remote storage supply are future work. API3 remains current.
Matching0.13.9clients/server are required (movement handshake13.9).

## Local playtest

- Keep foundations, nets and walls in your backpack; choose each with B, then place
  it while the hammer stays equipped. Expect one piece spent per success.
- Try a full/blocked location and an empty supply stack. Expect red ghosts and no
  spent pieces. Check normal rotation and wall/corner attachment on your raft.
- Choose Repair, damage/repair a foundation and test deliberate sneak dismantling.
- Save/reload while holding a selected hammer, then test drifting/turning placement.
- Repeat with a co-op crew member and guest, and test a registered addon machine.

The automated evidence and outstanding live GUI/co-op checks are recorded in
RELEASE_VERIFICATION.md. Existing user worlds are not opened by development checks.
