# Optional mod compatibility — 0.11.31

Target: Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25. Other mods must target
the same game and loader. This update adds shared materials and standard energy
interoperability without requiring or bundling any technology mod.

## Materials, crafting and research

Raft crafting recipes accept these `raft_ocean:compat/` item tags. Each keeps
its original vanilla/raft material and includes the common tag optionally.

| Raft compatibility tag | Optional common tag |
| --- | --- |
| `iron_ingots` | `c:ingots/iron` |
| `copper_ingots` | `c:ingots/copper` |
| `gold_ingots` | `c:ingots/gold` |
| `iron_nuggets` | `c:nuggets/iron` |
| `redstone` | `c:dusts/redstone` |
| `glass` | `c:glass_blocks/colorless` |
| `string` | `c:strings` |
| `sticks` | `c:rods/wooden` |
| `cobblestone` | `c:cobblestones/normal` |
| `leather` | `c:leathers` |
| `iron_plates` | `c:plates/iron` |
| `copper_wires` | `c:wires/copper` |

Shared materials also satisfy the corresponding research samples. Existing
research locks and item outputs are preserved. Iron plates remain iron plates;
unrelated metals are not automatically substitutes. Mods or packs must populate
the common tag, or extend a raft compatibility tag themselves.

Raft iron plates and copper wire are also exported into `c:plates/iron` and
`c:wires/copper` (and their parent material tags). Other mods using these tags
can therefore accept the raft materials in their own recipes.

Raft smelters use loaded Minecraft smelting recipes, so modded inputs work when
their mod supplies a smelting recipe. Grills similarly use loaded campfire food
recipes. Machine processing systems such as Mekanism chemicals, Thermal custom
processing and Create kinetics require their own adapters.

## Ocean resources

Floating crates and barrels have a separate ten-percent chance to provide one
random item from `raft_ocean:compat/ocean_resources`. The tag optionally includes
`c:raw_materials/osmium`, `tin`, `lead`, `silver`, `nickel`, `zinc`, `aluminum`
and `uranium`. Missing tags are harmless; an empty tag adds no bonus loot.
This gives matching technology mods a resource path in an ocean world without
requiring normal overworld ore generation. Existing container loot remains.

Packs may extend `data/raft_ocean/tags/item/compat/ocean_resources.json`:

```json
{
  "replace": false,
  "values": [
    {"id": "#example:raw_materials", "required": false}
  ]
}
```

Use the matching installed mod's real item/tag IDs. The bonus chance is controlled
by the named `raft_ocean:mod_resources` pool in the barrel/crate loot tables.

## Energy items

Raft batteries expose the Minecraft 26.3 NeoForge transactional
`Capabilities.Energy.ITEM` capability. Other compatible devices can charge or
drain them. Existing saved charge is preserved. One raft charge unit equals one
NeoForge energy unit (FE); battery capacity and charger throughput stay the same.

The raft Battery Charging Station accepts single modded energy items that expose
this capability. It uses connected raft generator energy first, then biofuel.
It spends only the energy actually accepted, supports both sockets, preserves
fuel when an item is full or rejects charging, and respects Pause/Resume.
The percentage display supports large foreign capacities.

`compatibility.energyItems = false` in the common config disables external
charging and exported battery capabilities. Native raft batteries still work.
Other raft appliances continue to accept native raft batteries. Mod energy
providers must implement the current transactional capability; old Forge
`IEnergyStorage` implementations are not an interchangeable 26.3 API.

## Moving foreign blocks and machines

Raft stations belong to a moving entity and have raft-local inventories. A world
block entity from another mod cannot automatically be moved here and retain its
world-coordinate neighbors, menus, multiblocks, ticking and capabilities.
Installing a technology mod alone does not mount all its machines on the raft.

The existing [machine addon API](MACHINE_ADDON_GUIDE.md) supports actual adapters:
register the foreign station's item, original visual, footprint/collision and a
`RaftMachineBehavior` with explicit processing, inventory and menu handling.
API v2 now additionally supplies:

```java
boolean registered = RaftBuildables.registerOptional(
    "example_mod",
    AdapterDefinitions::buildable,
    AdapterDefinitions::behavior
);
```

The two suppliers are resolved only if the named mod is loaded. Invoke this
during common setup through `enqueueWork`, on both sides, and keep foreign class
references inside the adapter/factories. The complete base addon project is in
`examples/raft-addon`; use this helper around its registration for an optional
integration. Both client and server must have identical adapter definitions.

## Current availability and testing

Checked on 2026-10-10 against the projects' own published pages:

- [Mekanism source](https://github.com/mekanism/Mekanism) has a 26.3 development
  branch; a matching released jar was not verified.
- [Thermal Foundation](https://www.curseforge.com/minecraft/mc-mods/thermal-foundation)
  lists its main release for 1.20.1.
- [Create](https://www.curseforge.com/minecraft/mc-mods/create) lists its main
  release for 1.21.1.
- JEI 31.9.0.58 remains this project's existing tested development integration.

The compatibility layer does not make older-version mod jars load on 26.3.
Place matching mod jars and their dependencies in `compat-mods/` to include them
in development runs. For an installed game, use the normal mods folder. Matching
content mods belong on both client and server.

Local checks: craft with an equivalent tagged ingot/plate, study its research
sample, smelt a mod raw material, find bonus metal salvage, and charge a supported
energy item in both sockets. Verify percentage, rejected/full items, saved charge
and fuel conservation. Direct tests with real Mekanism/Thermal/Create jars remain
pending; their mod-specific moving-machine adapters are not included.

## Automated verification

Build and native server bootstrap passed: 259,199 crafting assertions, 666
research assertions and 857 survival assertions, plus movement, collision,
navigation and addon suites. All 1,399 loaded crafting recipes were audited.
New native tests prove capability registration, insertion/extraction, transaction
rollback, saved item charge, fair socket sharing, pause, fuel preservation for
rejecting/full ports, rate-limited acceptance and missing-mod supplier deferral.
Packaged crafting, research, model, surface and bilingual localization audits
passed. These checks do not substitute for tests with actual third-party jars.
