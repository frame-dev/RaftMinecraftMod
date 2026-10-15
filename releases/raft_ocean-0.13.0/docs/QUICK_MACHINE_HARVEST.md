# Quick machine collection — 0.12.6

Sneak-use a machine with an empty interacting hand to collect its finished
outputs. Ordinary use opens the normal machine menu. Holding a cup still uses
existing cup behavior; sneaking with a held item opens the menu.

Collection includes finished food/ingots, crops/seeds, returned cups, timber,
dry bricks and refined biofuel where the machine defines an output slot.
It does not pull ingredients, fuel, battery sockets or stored water. Chargers,
generators and fuel tanks have no completed-output slots and keep their menu
behavior. Collecting a cup from water storage requires an actual filled output
slot; reservoirs still need the normal empty-cup exchange.

Existing compatible stacks are filled first. Empty backpack slots are preferred
before empty hotbar slots, allowing repeated pickups with an empty selected
hand. Armor and offhand are not used as destinations. Custom item components
are preserved. Nothing is dropped or deleted when inventory fills, including
creative mode. Partial/empty/full feedback explains what happened.

Net pickup uses the same fit-only transfer behavior. Quick stash still moves
only matching backpack supplies into chests/barrels. Each shortcut validates
crew access and reach on the server; spectators cannot mutate cargo.

## Settings

- Server/synced: building.quickMachineHarvest = true. Disable to return
  sneak-empty-hand machine use to normal menu behavior.
- Client: interactionHints = true. Disable to hide contextual prompts.

The crosshair hints appear with both hands empty while aiming at a supported
machine, net or stash container. They do not expose another player's inventory.
English/German prompts wrap to fit smaller windows and respect hidden HUDs.

## Playtest

1. Finish food/smelting/crop/drying/refining recipes; sneak-use empty-handed.
   Completed outputs should enter inventory; ingredients and fuel stay put.
2. Leave space for one item and collect a larger batch. Excess must remain
   in the machine/net. Retry with a full inventory: nothing should disappear.
3. Try normal use, held cups, both hands and a machine with no output slots.
   Menus and normal cup exchanges must remain available.
4. Place a new batch in a shared machine and have two crew members collect it.
   Its goods must transfer once. Test a visitor with guest permissions.
5. Toggle both new settings, test smaller windows and hide the HUD.

Recent models and animation refinements are retained. Engine/sail/paddle base
speeds remain 1.5/1.0/1.8 m/s. The story stays open-ended. Live client prompts,
co-op latency, collision behavior and large builds still require local testing.
