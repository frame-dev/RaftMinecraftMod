# Raft Ocean addon API v3 — 0.13.0

This is the public extension contract for addon utility consumers and batch
processing. Existing v1 buildables and v2 machine controllers still work.
The new behavior methods have opt-out defaults. Existing saves retain their
buildable identifiers, inventories and named properties.

Targets: Minecraft 26.3, NeoForge 26.3.0.48-beta, Java 25,
ModDevGradle 2.0.147 and Gradle 9.2.1. All peers need matching main-mod and addon
versions. `RaftBuildables.API_VERSION` is 3. Check supported versions with a
range, rather than assuming every future API version is compatible.

## Complete working projects

The [example addon](../examples/raft-addon/README.md) includes:

| Example | Demonstrates |
| --- | --- |
| Work crate | A two-cell v1 buildable, placement, original model and interaction |
| Kelp dryer | A v2 controller, inventory, fuel, progress, pause, save/load and view |
| Sediment washer | v3 power/water inputs, demand, item/resource transactions and output collection |

Read the complete [SedimentWasher.java](../examples/raft-addon/src/main/java/example/raftaddon/SedimentWasher.java)
and [ExampleAddon.java](../examples/raft-addon/src/main/java/example/raftaddon/ExampleAddon.java).
Copy the example project when starting an addon, replacing its namespace,
registrations, recipes, models and language entries with your own originals.

From the main project directory, with Java 25 selected:

```powershell
.\gradlew.bat build --offline --no-parallel
.\gradlew.bat -p examples/raft-addon build --offline --no-parallel
```

Offline builds require dependencies to be cached; omit `--offline` for first
setup. The example reads the main version from `gradle.properties` and compiles
against `build/libs/raft_ocean-0.13.0.jar`. A standalone copy must update that
file dependency or use its own dependency repository. No public Maven hosting
is provided by this workspace. Sources jars are for development, not installation.

The example's playable jar is `examples/raft-addon/build/libs/raft-example-addon-1.2.0.jar`.
Its required BOTH-side dependency range is `[0.13.0,0.14)`, ordered AFTER
`raft_ocean`. Compilation alone does not validate this loader metadata. The old
example range excluded current main-mod releases and has been corrected.

## Standalone starter kit

The separately packaged `raft-addon-sdk-0.13.0.zip` contains a self-contained
example project, Gradle wrapper, exact main jar/sources, original example
resources, notices and these guides. Unzip it, select Java 25 and run its own
`gradlew.bat build --no-parallel`. It needs no parent checkout. First dependency
setup needs internet; later builds can use the cache with `--offline`.

The SDK's README takes precedence for its build paths. Its starter source was
built independently from the staged SDK folder. Custom development run profiles
are left to the addon author; install the built addon alongside the main jar in
matching ordinary client/server profiles for local gameplay testing.

## Registration and compatibility

Register your real Minecraft items/blocks through your own DeferredRegisters.
During `FMLCommonSetupEvent.enqueueWork`, register an immutable `RaftBuildable`
and stateless `RaftMachineBehavior` with `RaftBuildables.register(type, behavior)`.
Use your own stable namespaced ID; saved addon identity depends on it.
For geometry, collision, menus, persistence, migration and optional adapters,
see the [full machine guide](MACHINE_ADDON_GUIDE.md).

Keep mutable machine data in `context.state()`, not controller fields. Controller
instances serve all installations, worlds and logical sides. Return stable,
immutable sets from utility/output declaration methods. Registrations validate
slot declarations against the existing 9–54-slot inventory bounds.

Existing registration signatures and old behavior methods remain. Defaults are:

```java
default java.util.Set<RaftUtility> utilityInputs() {
    return java.util.Set.of();
}
default int powerDemand(RaftMachineContext context) {
    return 0;
}
default java.util.Set<Integer> quickHarvestSlots() {
    return java.util.Set.of();
}
```

These are the new interface default implementations, not a standalone controller.
A complete controller is linked above. Old addons may still need their declared
main-mod dependency range updated; unchanged methods do not override loader rules.

## Utility inputs

Declare the input types your machine accepts through `utilityInputs()`:

| Input | Unit | Eligible sources |
| --- | --- | --- |
| `POWER` | FE; one raft energy unit equals one FE | Stored energy in raft biofuel generators |
| `FRESH_WATER` | Whole cups | Water tanks, purifiers and rain collectors |
| `BIOFUEL` | Whole canister items | Fuel tanks and biorefiner output slots |

Use `new RaftUtilityCost(power, freshWater, biofuel)`, or `RaftUtilityCost.NONE`.
Negative costs are rejected. Clean water is distinct from salt water. Consumer
reservoirs, batteries, engine burn reserves and arbitrary chest items are not
sources. This API does not create a world BlockEntity or external FE connection.

Connections use face-adjacent cells around the machine's complete rotated
reserved footprint, including upper and non-anchor cells. Connect the matching
native pipe/cable or place a source directly against that boundary. Different
conduit types do not connect. Cached topology refreshes after structural changes.

A machine can draw from multiple touching components, but does not become a
bridge routing resources between them. A component touching several faces is
counted once. Resources never cross raft ownership boundaries. Paused sources
are excluded. `automation.enabled=false` prevents positive utility draws.

`RaftMachineUtilities.acceptsAt(raft, occupiedCell, input)` is a client-safe,
declaration-only query covering wide/rotated machines. It exposes no inventory.
The main renderer uses it to connect pipe/cable visuals to opted-in machines.

## Preflight and consumption

Call these from the server tick thread with a current installed context:

| Method | Behavior |
| --- | --- |
| `RaftMachineUtilities.canConsume(context, cost)` | Checks all declared inputs without spending |
| `RaftMachineUtilities.consume(context, cost)` | Preflights every resource, then deducts exact units |
| `context.isInstalled()` | Checks that the retained handle still owns this installation/state |

Insufficient power must not spend available water or fuel. Sources are visited
in deterministic local-coordinate order. Inspection is not a reservation: check
again when committing. A stale handle after removal/replacement cannot draw.
Do not call these from rendering, asynchronous workers or packet threads.

Zero cost still requires a live installation, but does not require automation
to be enabled. Addon-owned processing with no utility inputs therefore remains
possible when native utility networks are disabled.

## Generator demand

Generators normally stop when nobody needs their power. Return a positive FE
value from `powerDemand(context)` only when your machine can use power; return
zero when paused, missing inputs/water or blocked by output capacity.

The value is a readiness signal, not a reservation, production-rate override or
promise that a batch can complete immediately. Native generators retain their
normal one-second production steps and capacity/fuel rules. Multiple consumers
share the actual stored supply. Processing may need to wait for energy to build.

The washer's readiness probe checks its item batch plus water with power cost
zero; that lets an empty but fuelled generator start. Its processing commit then
requests the full ten-FE cost. Requiring stored power during the readiness probe
would prevent an empty generator from starting. Do not make `powerDemand`
consume resources: it can be queried separately from machine ticking.

## Item and utility batches

`RaftMachineTransactions.canProcess(context, inputs, outputs, cost)` is read-only.
`RaftMachineTransactions.tryProcess(context, inputs, outputs, cost)` stages the
inventory, checks utilities and commits only when every preflight succeeds.

Inputs and outputs are maps of zero-based inventory slot to `ItemStack`.
Input stacks specify exact item components and required counts. Output stacks
specify products/counts. These helpers do not resolve recipes or tags themselves;
resolve those in your controller first, then pass concrete stacks.

For the washer, the input is slot 0 containing one sand; the output is slot 1
containing one clay ball. The utility cost is ten FE and one fresh-water cup.
The complete code uses those maps for both readiness and commit.

The helper checks slot bounds, positive valid stack counts, exact component
matches, input quantities, output compatibility and stack capacity. One slot
can be both consumed and produced: its output is checked after staged input
consumption. Supplied stack objects are not shrunk or installed by reference.

No input or utility is spent when output is full, incompatible or supplies are
missing. The helper stages only inventory and declared utilities; arbitrary
addon callback side effects are not a rollback system. Keep recipe decisions
and validation before the commit; update progress/view after success.

Use normal Minecraft JSON recipes/tags for crafting. Custom processing recipe
serializers and JEI categories remain addon-owned; these batch helpers do not
silently register recipes or reveal research-locked content.

## Quick collection and native menus

Opt in with `quickHarvestSlots()`, for example `Set.of(1)` for the washer's clay
output. The generic inventory treats these slots as output-only. The shortcut
also calls `canExtract(context, slot, player)`, so temporary output locks remain
effective. Empty declarations preserve existing addon sneak-use interactions.

For an opted-in machine, sneak-use with an empty hand collects those unlocked
slots before custom use callbacks. It respects the server quick-harvest setting,
crew permissions, spectator restrictions and moving reach. Extra items remain
in the machine when inventory fills, including creative mode. Ingredients,
fuel and undeclared slots are never selected. An empty declaration also avoids
claiming the contextual collection hint.

`RaftMachineMenus.quickHarvest(context, player, hand)` exposes the same helper
for custom addon interaction flows. The host calls it automatically for opted-in
controllers. Generic native menus now filter each shift-click destination
before merging; inherited chest merge behavior could otherwise bypass a custom
`canInsert` rule. Locked extraction and stale menu handles are rejected.

## Author checklist

- Test each footprint at all four rotations, including connections at its second
  cell and different floor levels. Confirm pipe/cable visuals agree with routing.
- Test empty, insufficient, paused and disconnected sources and global disablement.
- Test full/differently named outputs, missing inputs and repeated processing.
- Save/reload midway; retain stable IDs and migrate named state properties.
- Test cable removal, machine removal/replacement, locked outputs and co-op pickup.
- Build the example and load its playable jar in a real matching loader profile;
  inspect dependency metadata as well as compiler success.
- Keep client rendering separate and use immutable `RaftMachineView` values;
  never expose private inventory/state properties through public visual snapshots.

The API does not automatically transplant world machines, add arbitrary external
energy networks, register research/AI hooks or supply bone animation/JEI systems.
Those require explicit addon integrations. Test your chosen mod combination and
declare the supported main-mod range rather than promising every future release.
