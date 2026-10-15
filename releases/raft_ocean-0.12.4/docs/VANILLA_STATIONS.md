# More vanilla stations and collision fixes — 0.9.12

Minecraft 26.3 / NeoForge 26.3.0.48-beta / Java 25.
Jar: `build/libs/raft_ocean-0.9.12.jar`. Update both client and server.
Protocol: `raft-movement-9.12`. Existing raft saves remain compatible.

## Added placeables

| Placeable | Behavior |
| --- | --- |
| Raft Anvil | Native repairing, combining and renaming, including XP costs and NeoForge anvil hooks. |
| Raft Enchanting Table | Native enchantments, lapis, XP, seeded offers and enchanting screen. |
| Raft Ender Chest | The current player's native Ender Chest inventory, shared with their other Ender Chests. |
| Raft Bookshelf | A solid bookshelf that supplies enchanting power in raft coordinates. |

Place the vanilla item directly on the raft, or convert it to its raft version
with a one-item shapeless crafting recipe. All four have original textures and
models, English/German names, recipe-book unlocks and placement ghosts. Anvils
also appear as crafting stations in JEI's native anvil category.

Menus follow the station as the raft moves and close when the station is removed
or the player leaves its eight-block reach. Bookshelves have no menu, matching
the ordinary vanilla bookshelf. These are mounted raft pieces; they are not
terrain blocks or arbitrary vanilla block entities.

## Enchanting

Place shelves two cells away from the table, at table height or one cell above,
leaving the intervening cell empty. Up to fifteen valid raft shelves contribute
power, using the native bookshelf offsets. A solid raft piece in that gap blocks
the shelves behind it. Local positions remain valid through translation and
rotation. Shelf changes refresh offers while the menu is open.

Native nearby terrain bookshelves can also contribute through the existing
Minecraft calculation. The added mixin supplies raft power before native costs,
clues and NeoForge enchanting hooks are calculated. It only changes raft menus;
ordinary enchanting tables retain their original behavior.

## Inventory and anvil behavior

Ender storage belongs to the player. Two players opening the same raft chest see
different contents. Removing or destroying that piece does not drop their Ender
items. The vanilla player save stores those items; the raft does not duplicate
them into its cargo inventory. Open/close lid animation follows station usage.

The mounted anvil retains native item transactions and hooks. Its terrain damage
callback is suppressed while taking an output, so it cannot damage or remove a
terrain anvil beneath a moving raft. It plays the native use sound at its moving
position. The raft anvil does not wear out. Closing a tool menu returns unused
input items through the native container behavior.

## Collision fixes

Sails, steering wheels, engines, anchors, smelters and anvils use separate
collision parts rather than one bounding box around the whole model. The broad
base no longer fills the empty space around a thin mast, wheel or chimney above
it. Visible solid parts remain solid. These same shapes feed server/client
movement and overlap recovery, including the existing rotated hull strips.

Crafting tables and other solid workstations retain their continuous colliders;
ladder and railing safety shapes remain continuous. Physics collision strips
still approximate rotated geometry, and visual animations do not move every
physical collider. Report any remaining snag with the object, hull angle and
movement that causes it.

## Verification and user checklist

Build and standalone checks pass, including eighteen new clearance assertions.
Isolated native runs pass: 729 carry, 610 construction, 48 crafting, 119 stations,
108 salvage, 274 survival, 130 navigation, 562 creature/config, and 571 Ocean World
assertions. Station checks exercise actual native repair/enchantment transactions,
Ender privacy, removal/closing behavior, level-30 offers, blocked gaps and moving
menus. Server tests verify the enchanting mixin is applied through native costs.

- Place each vanilla station directly and check its ghost and moving screen.
- Repair and rename a damaged tool; verify XP and materials are consumed once.
- Place fifteen bookshelves with clear gaps, insert an enchantable item and
  lapis, and verify the third offer requires level thirty. Block a gap, then
  remove the obstruction; offers should refresh.
- Open one Ender Chest with two players. Check private contents, then remove
  the chest and reopen another one. Contents should remain intact.
- Walk beside the sail mast, steering wheel and engine while the raft drifts
  and turns. Check upper space beside a smelter chimney and both door states.
- Save/reload, and repeat on a dedicated server with both sides updated.

Live UI, textures, station animation and connected multiplayer remain user tests.
