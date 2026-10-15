# Raft Ocean building and machine API v3 — 0.13.0

Public common-side classes: dev.raftocean.api.RaftBuildables and RaftBuildable.
The mod jar includes these classes; the sources jar includes the complete source.
A separate compiling addon project is in examples/raft-addon. It registers a
new item/block and mounts a two-cell work crate with its own original model,
recipe, English/German labels and server-side interaction.

## Build and test the example

From the main project's directory with Java25 installed:

```powershell
.\gradlew.bat -p examples/raft-addon build
```

This uses Gradle9.2.1, ModDevGradle2.0.147 and NeoForge26.3.0.48-beta. The example
compiles against build/libs/raft_ocean-0.13.0.jar. Its output is
examples/raft-addon/build/libs/raft-example-addon-1.2.0.jar.
Install that jar and the main mod together on the client and server, then restart.
Obtain the crate with /give @s raft_example:work_crate, or craft it with two oak
planks and Scrap Fasteners. Place it over two foundations and right-click it with
an empty hand. Its interaction displays a translated message. Crouch with the
building hammer to remove it and receive one crate item.

## Definition contract

Register in FMLCommonSetupEvent.enqueueWork on both sides before world loading.
Use your own stable namespaced Identifier and your own registered item/block.
Registration rejects duplicate IDs. Suppliers defer item/block access until the
registries are ready. RaftBuildables.definitions returns an immutable snapshot.

RaftBuildable fields:

| Field | Meaning |
| --- | --- |
| id | Saved identity, for example raft_example:work_crate |
| item | Supplier for the item placed and refunded |
| visual | Supplier for a block whose baked model renders the mounted piece |
| width, depth, height | Reserved cells; width/depth1–4, height1–16 |
| mass | Finite mass0–256, counted once per object |
| floorMounted | Apply normal floor mounting; wider objects require support under every base cell |
| geometry | Immutable picking/placement boxes, at most128 |
| collision | Immutable physical boxes, at most128; can differ from visual geometry |
| interaction | Callback for an empty/non-building hand interaction, invoked only on the server |

Coordinates are relative to the anchor's deck surface. X/Z start at-.5 and end
at width-.5/depth-.5; Y starts at0 and ends at height. Positive X is the second
cell of a two-wide definition at rotation0. Geometry must stay inside these
limits and have finite positive extents. Normal raft placement applies quarter
turns, floor offsets, full-cell reservation, player/world obstruction checks,
buoyancy, ghost previews, collision transforms and support pruning.

The visual block model is fitted to the complete geometry envelope. Use a model
normalized to a16-unit box on all three axes; do not apply width/height scaling
a second time in the model. For example, the work crate's model spans Y0–16 and
its definition scales that to a physical height of.75. Model details and holes
should agree with the geometry and collision lists. All render code remains in
the main mod's client classes; API classes have no client imports.

One addon placement creates one owner, consumes one item and refunds one item on
hammer removal. Saves keep the addon ID, rotation and ownership; old six-field
built-in records remain readable. An addon record adds its ID as a seventh field.
Keep the same addon definitions installed on the server and every client. Worlds
containing addon pieces require their addon; unknown IDs are rejected explicitly.

## Machine API v3

For the full setup, registration, resource, machine, menu, multiplayer,
persistence, migration and debugging instructions, see
[MACHINE_ADDON_GUIDE.md](MACHINE_ADDON_GUIDE.md).

Register a machine with `RaftBuildables.register(definition, behavior)`.
`RaftMachineBehavior` provides placed/loaded/removed hooks, server ticks,
input/output slot rules, interaction and immutable visual selection.
Each installed machine owns a `RaftMachineState` inventory and bounded integer/
string properties, automatically saved with its raft. `RaftMachineContext`
provides current moving coordinates and ownership-aware reach checks.
`RaftMachineMenus` opens native filtered inventories or addon custom menus.
`RaftMachineView` synchronizes active/progress/duration/variant presentation
without broadcasting private inventory or arbitrary saved properties.

The example now includes a functioning kelp dryer: first inventory slot kelp,
second output, third sticks. Sneak-click with an empty hand toggles pause.
Fuel/progress survive saves; output blockage or pause conserves supplies.
The active model changes its trim. Native slot transactions enforce input rules.

Existing v1 registrations and addon structure IDs remain compatible.
Both sides must run 0.13.0 and matching addon definitions; network protocol
changes mean mixed main-mod versions are unsupported. The controller is shared,
so mutable per-instance state must live in its context, not controller fields.

API v3 adds opt-in utility inputs (power, fresh water and biofuel), generator
readiness demand, item/resource batch helpers and safe quick output collection.
See [API_V3_UTILITIES.md](API_V3_UTILITIES.md) for the complete contracts and
[the example addon](../examples/raft-addon/README.md) for three complete examples.

Existing v1/v2 registration methods and controllers remain supported through
new default methods. Defaults join no networks and claim no quick-harvest slots,
preserving existing addon sneak-use behavior. Update dependency ranges when
moving an addon to a new main-mod release; all peers need matching mods.

Custom bone rendering, automatic JEI categories, research registration and
arbitrary world BlockEntity transplantation remain outside this API. Addons can
use normal recipe/tag resources and their own explicit client integration.
