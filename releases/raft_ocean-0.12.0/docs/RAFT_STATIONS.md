# Raft storage and tool stations — 0.4.2

Supported raft stations: crafting table, chest, barrel, stonecutter, smithing table,
grindstone, loom, and cartography table. They are raft-owned structural pieces,
with original placeholder wood models, synchronized geometry, and normal menus.
Existing worlds can load this update.

## Build and use

Close the old client/server. Run from the project folder:

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
```

Verify **Raft Ocean 0.6.0** in Mods. Use 0.6.0 on every connected client and server.
The required network protocol rejects earlier builds. Artifact:
`build/libs/raft_ocean-0.6.0.jar`.

1. Craft or obtain a normal vanilla station item, such as a chest or barrel.
2. Hold it and right-click a foundation or upper floor. Stand beside the target
   cell so your body does not obstruct placement. Normal structural support,
   buoyancy, placement reach, and obstruction checks apply.
3. Right-click the mounted station with an empty hand to open its normal menu.
   Menus follow its moving position, closing when the station disappears or you
   leave eight-block menu range or change dimension.
4. Crouch + right-click with the building hammer to remove it. This refunds the
   corresponding raft item. Storage contents drop at the station. A mounted
   station must be removed before its supporting floor.

The Raft Construction Creative tab also contains the raft versions. Converting
one vanilla station item alone in the inventory grid gives its raft version;
this conversion is optional. The plank/stick raft crafting-table recipe remains.
As of 0.11.24, conversions of already crafted vanilla workstations do not require
research. Materials-based crafting still follows research requirements; see
[the conversion fix](VANILLA_MOUNTING_FIX.md).
This exemption was replaced in 0.11.25: conversions follow the corresponding
research project and remain hidden until learned; see
[research recipe discovery](RESEARCH_RECIPE_DISCOVERY.md).

Example test items:

```text
/give @s minecraft:chest
/give @s minecraft:barrel
/give @s minecraft:stonecutter
/give @s minecraft:smithing_table
/give @s minecraft:grindstone
/give @s minecraft:loom
/give @s minecraft:cartography_table
```

## Storage and processing

Each chest or barrel has one shared 27-slot inventory. Multiple players opening
the same piece see the same inventory through native server synchronization.
Cargo saves with the raft, including slot numbers, stack counts, custom names,
damage, and other item data. Chunk unloading preserves cargo. Removing storage
or destroying the raft drops stored items once.

Tool stations use normal recipes and processing: stonecutting, smithing upgrades,
grindstone repair/disenchantment, loom patterns, and cartography operations.
Their input grids are temporary and per player. Closing an interface returns
unused inputs through vanilla handling, including drops when inventory is full.
Sounds and experience effects originate at the moving station position.

Station masses: chest/barrel/grindstone 6; stonecutter/smithing table 8;
loom/cartography table 4; crafting table 6. Inventory cargo mass is deferred.
Chests do not combine into double chests, models have no opening animation, and
hopper/redstone automation is unavailable. Arbitrary block entities are not
supported. Furnaces and beds remain future survival work, as selected by the user.

## Test checklist

- [ ] Place normal vanilla items directly on foundations and upper floors.
  Expected: one held item consumed, flush mounting, collision, and movement.
- [ ] Store items in both chest and barrel, including slot 27 and a renamed or
  damaged tool. Save, close, and reload. Expected: exact contents preserved.
- [ ] Shift-click transfers in both directions. With two clients, open the same
  storage. Expected: shared counts update with no duplicated items.
- [ ] Use each tool station while drifting. Expected: native output and ingredient
  consumption; menus remain open while you stay nearby.
- [ ] Leave range or remove a station while another player has its menu open.
  Expected: screen closes; temporary ingredients return once; stored cargo drops.
- [ ] Unload/reload the raft's area. Expected: stored cargo remains. Recheck drift
  and walking for the previously reported intermittent snap-back.

Automated validation: build, dedicated startup, 91 standalone regressions,
725 carry checks, 208 construction checks, 13 crafting checks, 85 station checks,
and 43 Ocean World checks. Station tests exercise actual native menus, recipes,
save/load, transfers, removal/destruction drops, and unload preservation using
fake players. Live client rendering, multiplayer timing, and disconnect handling
still need the checklist above. The user authorized milestone 5; continue the Ocean World and station live checks alongside it.

Version 0.5.2 also has a shared 27-slot collection net; see [milestone 5](MILESTONE_5.md).

Current component recipes and direct storage crafting: [crafting chains](CRAFTING_CHAINS.md).

The current survival stations and moving beds are covered in [milestone 6](MILESTONE_6.md).
