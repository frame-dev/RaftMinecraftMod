# Creating blocks and machines with Raft Ocean API v3

Target: Raft Ocean **0.13.0**, Minecraft **26.3**, NeoForge **26.3.0.48-beta**,
Java **25**, Gradle **9.2.1**, ModDevGradle **2.0.147**. These are this project's
pinned versions. This guide describes the code shipped here, not other Minecraft releases.

## 1. Start with the complete example

The runnable source project is `examples/raft-addon`. Its complete files include:

| File in the example | Purpose |
| --- | --- |
| `settings.gradle` | Plugin repositories and project name |
| `build.gradle` | Java, NeoForge and Raft Ocean dependency |
| `src/main/resources/META-INF/neoforge.mods.toml` | Mod identity and required versions |
| `src/main/java/example/raftaddon/ExampleAddon.java` | Registers blocks, items and raft definitions |
| `src/main/java/example/raftaddon/KelpDryer.java` | Complete processing and interaction controller |
| `src/main/java/example/raftaddon/ExampleMachineCheck.java` | Optional native server verification of the dryer |
| `src/main/resources/assets/raft_example/blockstates/work_crate.json` | Crate visual state |
| `src/main/resources/assets/raft_example/blockstates/kelp_dryer.json` | Idle machine visual state |
| `src/main/resources/assets/raft_example/blockstates/kelp_dryer_active.json` | Working machine visual state |
| `src/main/resources/assets/raft_example/models/block/` | Original mesh definitions |
| `src/main/resources/assets/raft_example/items/` | Modern inventory item definitions |
| `src/main/resources/assets/raft_example/lang/en_us.json` | English text |
| `src/main/resources/assets/raft_example/lang/de_de.json` | German text |
| `src/main/resources/data/raft_example/recipe/` | Crafting recipes |

From `C:\Users\DHZoc\Desktop\Raft Minecraft Mod`, use:

```powershell
$env:JAVA_HOME='C:\Program Files\Zulu\zulu-25'
.\gradlew.bat -p examples/raft-addon build --no-parallel
```

The main dependency must already exist at `build/libs/raft_ocean-0.13.0.jar`.
The example output is `examples/raft-addon/build/libs/raft-example-addon-1.2.0.jar`.
Put both jars in the client's and dedicated server's `mods` directories. Restart.
Never install the sources jar as a mod. Never bundle Raft Ocean classes inside
your addon: it is an ordinary required dependency.

For a standalone copy, supply your own Gradle 9.2.1 wrapper and replace the file
dependency with the actual path to the Raft Ocean jar. Import `build.gradle` in
your IDE, choose Java 25 for the Gradle JVM and reload the project. The example
intentionally shares the main project's wrapper when built from this workspace.

Use `/give @s raft_example:work_crate` and `/give @s raft_example:kelp_dryer`.
The crate occupies two adjacent foundations and shows a message when used. The
dryer occupies one foundation and opens a native nine-slot inventory. In its
top row, place kelp in the first slot and sticks in the third. The second slot
receives dried kelp. One stick supplies 200 processing ticks; one kelp takes 100
ticks, normally five seconds while the raft is loaded. Six remaining slots are
ordinary storage. Sneak-click with an empty hand pauses/resumes the machine.
The working model changes its metal trim. Pause, blocked output and missing
input preserve fuel and progress. Removing the machine drops its inventory and
refunds one machine item through the normal hammer path.

## 2. Create your own addon identity

Copy the example's source files and resources, excluding `build` and `.gradle`.
Choose a lowercase mod ID such as `coastal_workshop`. Replace `raft_example` in
the annotation/ID constant, metadata, resource directories, recipe outputs,
model references and translation keys. Change the Java package, Gradle group,
project name and display name. A Java package change also requires moving the
Java files into the corresponding directories.

In `neoforge.mods.toml`, retain a required, BOTH-side Raft Ocean dependency with
`ordering="AFTER"`. This example requires `[0.13.0,0.14)`; that range is a
declared compatibility policy, not a promise that every later release has been
tested. Pin a narrower range if you require a specific tested build. Keep the
Minecraft and NeoForge requirements appropriate to this target.

Version 0.13.0 uses protocol
`raft-movement-13.0`. All players and the server need the same main-mod release
and matching addon definitions. Updating only one side is unsupported.

## 3. Register the real Minecraft block and item

The example uses `DeferredRegister.createBlocks(ID)` and
`DeferredRegister.createItems(ID)`. Register these with the mod's event bus in
the mod constructor. A visual block is registered with `registerSimpleBlock`,
and its placeable item with `registerSimpleBlockItem`.

The block registry entry supplies a baked visual model. On a raft this API
creates a raft-owned piece, **not a world block or BlockEntity**. Consequently,
ordinary block tick methods, BlockEntity inventories, neighboring-world-block
signals and vanilla hopper capabilities do not automatically operate on it.
Keep machine behavior in `RaftMachineBehavior`. If the same BlockItem may also
be placed on land, decide separately what that world block should do.

Use `FMLCommonSetupEvent.enqueueWork` to register the raft definition after
Minecraft's item/block registries are ready, on both physical sides. Do not
register only inside client setup or on world login. Register each ID once.
Suppliers defer registry access; do not resolve `.get()` in premature static
initialization. Give each definition its own item: item lookup selects the
first matching definition, so sharing one item creates ambiguous placement.

## 4. Define size, mass, picking and collision

`RaftBuildable` contains:

| Field | Contract |
| --- | --- |
| `id` | Stable namespaced `Identifier`; saved with each owner |
| `item` | Item supplier; one item is consumed/refunded per whole piece |
| `visual` | Block supplier used for the idle baked model |
| `width`, `depth` | Reserved horizontal cells, each 1â€“4 |
| `height` | Reserved vertical cells, 1â€“16 |
| `mass` | Finite 0â€“256; counted once for the whole owner |
| `floorMounted` | Apply floor mounting and require support under every base cell |
| `geometry` | Nonempty immutable list of up to 128 picking/preview boxes |
| `collision` | Immutable list of up to 128 physical boxes; may be empty |
| `interaction` | Server-side interaction callback returning `InteractionResult` |

A box uses **raft-local** coordinates relative to the owner anchor. X and Z
begin at -0.5. X ends at `width - 0.5`, Z at `depth - 0.5`. Y is from zero to
`height`. All extents must be finite and positive. At rotation zero a two-wide
piece extends toward positive X. Placement rotates the footprint and boxes in
quarter turns. Do not rotate them yourself when registering.

For a two-cell bench 0.75 blocks high, the example defines
`new AABB(-.5,0,-.5,1.5,.75,.5)` with width 2, depth 1, height 1.
Use several small boxes for separate legs, posts or steps. Picking can include
decorations that have no physical collision. Avoid a single full bounding box
across a doorway or between legs if players should walk through that space.
The API reserves complete footprint cells even when collision leaves holes.

`floorMounted=true` requires every base cell to have foundation/floor support.
Removing support is subject to normal building rules; damage prunes unsupported
pieces. Mass, buoyancy, placement obstruction, ghost previews, picking and
multiplayer collision use the registered owner and geometry. A wide object is
one owner and one item, not two separate pieces.

## 5. Add models, inventory display, textures and recipes

Use these resource paths, replacing the namespace and name:

```text
src/main/resources/assets/coastal_workshop/blockstates/dryer.json
src/main/resources/assets/coastal_workshop/models/block/dryer.json
src/main/resources/assets/coastal_workshop/items/dryer.json
src/main/resources/assets/coastal_workshop/textures/block/dryer.png
src/main/resources/assets/coastal_workshop/lang/en_us.json
src/main/resources/assets/coastal_workshop/lang/de_de.json
src/main/resources/data/coastal_workshop/recipe/dryer.json
```

Follow the complete example JSON rather than older tutorials: this target uses
`items/<name>.json`, the singular `recipe` directory and `result.id`. Model
textures use namespaced references without `.png`. The provided models reuse
Raft Ocean's original timber/metal textures; new textures must be original or
appropriately licensed. Include English and German block names and every
message key. A BlockItem normally uses `block.<namespace>.<name>` for its name.

The mounted renderer fits the supplied baked model into the merged `geometry`
envelope. Normalize your model to a 16-unit box on every axis; let the definition
provide physical width and height. Modeling a 32-unit mesh *and* declaring a
two-wide definition scales it twice. Keep collision, model silhouette and
picking aligned. The visual envelope may contain gaps: shape those with your
model elements and separate collision boxes.

Define `display.gui` and held-item transforms in the model to make inventory
icons readable. These transforms affect the item view, not the physical raft
footprint. The example is complete and shows the current item-definition format.
Its crafting recipes are data-driven JSON. Datagen may produce identical paths;
include generated resources in your Gradle resource source set. Asset-generation
scripts in the main mod are not required for an addon.

## 6. Turn a definition into a machine

Call `RaftBuildables.register(definition, behavior)` instead of the v1 overload.
The definition's interaction should return `InteractionResult.PASS` to delegate
to `behavior.use`. A consuming v1 interaction intentionally takes precedence.

Implement `RaftMachineBehavior`. One registered controller is shared by all
instances. **Store per-machine data in `context.state()`, never controller
fields.** Immutable suppliers or constants in controller fields are fine.

| Hook | When it runs |
| --- | --- |
| `slots()` | Inventory size; 9, 18, 27, 36, 45 or 54; default 9 |
| `tickInterval()` | Server ticks between calls, 1â€“1200; default 1 |
| `placed(context, player)` | Successful player placement, after one item is consumed |
| `loaded(context)` | After restoring the raft frame and inventory/properties; also upgrades v1 machine pieces with no stored state |
| `tick(context)` | Loaded raft's server tick at the chosen interval |
| `use(context, player, hand)` | Interaction fallback; default opens inventory |
| `removed(context)` | Before inventory drops on explicit removal, destruction or support loss |
| `view(context)` | Builds public visual data; default reads active/progress/duration/variant properties |
| `visual(definition, view)` | Client extraction selects the baked block state |
| `canInsert(context, slot, stack)` | Server native-menu input/shift-click filter; default true |
| `canExtract(context, slot, player)` | Server native-menu pickup/shift-click filter; default true |

Ticks stop when the raft is unloaded. They do not simulate offline processing.
Keep `slots()` constant for the lifetime of a registered type. If you change it
in a future release, migrate saved inventories before reducing their size.
Keep `tickInterval()` within its registration bounds even when reading configs.

A tick interval of 20 means approximately once per second, so advance timers by
20 if your desired units are game ticks. Server lag also extends real-time
duration. Callback exceptions fail loudly; addons must validate their own logic.

The dryer source demonstrates complete initialization, bounded output, exact
input/fuel consumption and pausing. Its single processing rule is deliberately
narrow: kelp plus stick fuel. Expand production machines with an addon-owned
Minecraft `RecipeType`/serializer and JSON, resolving the server recipe manager
inside `tick`; use the existing main-mod `survival/ProcessingRecipe.java` as a
version-matched reference. Validate ingredient, result type/components/count
and output capacity before changing inventory. Do not infer a recipe from JEI
or hardcode every recipe in UI code. Revalidate after a data-pack reload.

## 7. Inventory, persistent properties and migrations

`RaftMachineState` extends native `SimpleContainer`. Use `getItem`, `setItem`,
`removeItem`, `getContainerSize`, `isEmpty` and `setChanged` normally. Directly
shrinking/growing a stack should be followed by `setChanged`. The host saves
the complete inventory with item components, plus named properties, inside the
raft entity's `AddonMachines` save list.

Use `getInt(key)` / `setInt(key,value)` and `getString(key)` /
`setString(key,value)`. Missing values return 0 / an empty string. Integer
properties retain the full Java integer range in saves. Each machine allows
32 integer and 32 string keys. Keys are 1â€“64 characters matching
`[A-Za-z0-9_./:-]`; strings are limited to 256 characters. Do not store large
JSON documents, credentials or unbounded histories here.

Use `hasInt` / `hasString` to distinguish absent values from zero/empty strings.
`integerProperties()` / `stringProperties()` return immutable snapshots for
diagnostics. `removeInt` / `removeString` release old keys during migrations.

Use a saved `schema` integer. In `placed`, set initial defaults. In `loaded`,
migrate only when the saved schema needs it; never reset existing inventory,
fuel, progress or pause choices on each load. The dryer initializes `enabled`
only for schema zero, then writes schema one. Preserve slot numbers when adding
features. Changing inventory size without migrating all occupied old slots can
lose items. Keep IDs stable; implement any rename migration before loading old
structure data. Removing an addon from a world with its pieces is unsupported:
unknown IDs are rejected instead of silently deleting builds.

Removal automatically drops remaining inventory exactly once. Do not also drop
the same items in `removed`, and do not clear them there unless intentionally
consuming them. The callback cannot reopen its owner through machineContext: lookup is closed
during removal/destruction. Use the supplied context.state() for cleanup.
The callback may release addon-owned external links or emit
effects. Chunk unload is not destruction and must not drop contents. The host
does not call a generic unloaded hook; do not hold long-lived context references
or external subscriptions that require such a callback.

## 8. Interactions, moving coordinates and shared inventory

`RaftMachineContext` has `raft`, immutable local `anchor`, `type` and `state`.
`worldPosition()` computes the current piece center through raft translation
and rotation; do not cache a world BlockPos for a moving machine.
`valid(player)` requires a live player in the same level, the same installed
state owner, a live raft and distance at most eight blocks from the machine.
Removal or replacement invalidates old contexts and menus.

`raft.machineContext(anchor)` returns an Optional on the server. Use an owner
anchor from `raft.pieces()`, not an arbitrary secondary occupied cell. Clients
must use `raft.machineView(anchor)`: reading machineContext on a client throws.
For wide machines, `raft.machineContextAt(cell)` resolves the owner from any
reserved footprint cell. The public view has no private inventory or string data.

`RaftMachineMenus.openInventory(context,player)` uses Minecraft's native chest
screen and inventory transactions, with the registered slot filters applied
on the server. Players using the same machine share its one server inventory.
The standard client chest screen cannot show custom slot labels or fuel meters;
the example displays a translated hint before opening. A client may briefly
preview an invalid placement in a slot; the server rejects it and resynchronizes.

These menu filters do not constrain direct `state.setItem` calls. Automation
must explicitly apply your controller's rules and capacity checks. Native world
hoppers do not discover a raft-owned machine automatically.

## 9. Custom menus and screens

Use `RaftMachineMenus.openCustom(context,player,title,factory)` when your addon
owns a `MenuType` and a client screen. The factory receives the native container
ID, player's `Inventory` and the server context. It must create the menu using
that context's inventory. The opening buffer contains exactly:

1. `writeVarInt(context.raft().getId())`
2. `writeBlockPos(context.anchor())`

Your registered NeoForge `IContainerFactory` reads `readVarInt()` then
`readBlockPos()` in that order. On the client, construct a matching inventory
with `new SimpleContainer(slots)` and `SimpleContainerData` for your values;
native slot/data packets fill these. Never call `machineContext` there. Resolve
the client raft entity only if the screen needs `machineView` or positioning.
If it is absent, tolerate missing presentation until tracking catches up.

In your server menu subclass, call `addDataSlots(context.state().data("progress",
"fuel","enabled"))` and override `stillValid(player)` with
`context.valid(player)`. `addDataSlots` is protected, so call it inside the
subclass. On the client use a `SimpleContainerData(3)` in the identical order.
Native data-slot packets carry signed 16-bit values; scale meters to 0â€“1000 or
split larger integers into low/high words. The save system and visual snapshot
do not share this 16-bit restriction.

Use native `Slot` subclasses to filter inputs and prevent placing into output
slots. Implement `quickMoveStack` so shift-click respects those same rules and
retains stack components. A custom menu does not automatically inherit the
standard helper's filters. For buttons, use native menu button messages and
validate IDs and `stillValid` on the server before mutating state. Do not trust
client-provided item counts, progress, raft IDs or anchors.

Version-matched complete implementations already included in the main source:

| Source | Useful pattern |
| --- | --- |
| `src/main/java/dev/raftocean/menu/SurvivalMenu.java` | Filtered inputs, outputs, data slots, shift-click and buttons |
| `src/main/java/dev/raftocean/menu/NavigationMenu.java` | Server-controlled machine settings |
| `src/main/java/dev/raftocean/client/SurvivalScreen.java` | Client-only processing screen |
| `src/main/java/dev/raftocean/client/RaftOceanClient.java` | Client screen registration |

Inspect the actual source files when copying a pattern: a custom menu requires
its own type and matching client constructor, rather than reusing the main
mod's private machine kinds. The supplied dryer uses the fully working native
inventory path and therefore needs no custom client screen.

## 10. Public presentation, models and animation

`RaftMachineView` is immutable: `active`, `progress`, `duration`, `variant`.
Progress/duration are clamped to 0â€“1,000,000, variant to 0â€“4095.
`fraction()` returns normalized progress. The host publishes views about every
five loaded server ticks and immediately after placement/load/removal. It sends
one bounded raft-level tracking snapshot only when changed. Inventories and
arbitrary saved properties are not included in this public snapshot.

Use `visual(definition,view)` to return a registered baked block state. The
example switches between two registered visual blocks. You can instead select
block-state properties for variants. This method can execute on client render
extraction: keep it pure, quick and independent of server state, world mutations
and private inventories. API/common classes must not import client classes.

Animated PNG textures work through Minecraft's normal `.png.mcmeta` animation
metadata. To animate only while working, select a separate active model whose
texture is animated. A model variant can show growth stages or tank levels.
Keep variants within the same registered geometry envelope and collision; view
changes do not resize the physical footprint. The API does not provide arbitrary
bone animation, custom entity renderer registration, physical moving parts or
automatic liquid-fill meshes. Those need addon-owned client rendering hooks and
must remain in client-only classes. Do not promise support by copying an older
Minecraft renderer API without verifying this target's code.

## 11. Recipes, JEI, configs, pipes and progression

Normal crafting JSON is discovered by Minecraft and JEI as usual; the addon
machine's existence does not require JEI. For custom processing recipes, your
optional JEI plugin owns the recipe category, display layout, catalysts and
recipe collection. Keep all JEI references in the optional integration class
with `compileOnly` API dependencies, and test without JEI installed. Register
the dryer's item as a catalyst for your category. The Raft Ocean API does not
automatically register third-party processing categories or transfer handlers.
Use `src/main/java/dev/raftocean/client/jei` for the project's current JEI patterns.

Own config entries belong in your addon's `ModConfigSpec`, registered through
its `ModContainer`. Use server/synced configuration for gameplay and client
configuration for purely visual options. Read rates on the server, validate
nonnegative durations/capacities and translate tick intervals consistently.
`src/main/java/dev/raftocean/config/RaftConfig.java` and the mod constructor are
the pinned implementation reference. Do not change the main mod's global static
config spec from an addon.

API v3 supplies building registration, machine lifecycle/state/menu/view hooks,
explicit utility inputs and item/resource batch processing. See
[API_V3_UTILITIES.md](API_V3_UTILITIES.md) for power, fresh water, biofuel,
readiness demand and opt-in quick output collection. Existing controllers keep
no utility or quick-harvest declarations unless they opt in.

Research registration, shark targeting and entity AI are not public extension
contracts. Prefer normal recipe/tag/advancement resources and your own explicit
integrations; do not add enum constants to `RaftPiece.Kind`. The API v3 example
now includes a complete sediment washer alongside the v1 crate and v2 dryer.

## 12. Test and diagnose your addon

Before publishing:

- Build against the exact jar you ship; restart both sides after updating it.
- Test placement at all four rotations, every occupied cell, upper floors and
  support edges. The preview, interaction target and physical collision must agree.
- Walk around and through intended gaps while the raft drifts and turns.
- Check that one placement consumes one item and one hammer removal refunds one.
- For machines, test missing input/fuel, full and incompatible output, pause,
  shift-click, double-click, empty cursor, quick removal and two simultaneous users.
- Save with partially processed input and remaining fuel; reload and compare exact
  inventory, components and properties. Verify your schema migration too.
- Remove a loaded machine, destroy support and destroy the raft; contents must
  drop once. Unload/reload must preserve them without drops.
- Keep a menu open while sailing; walk away and remove/replace its owner. It must
  stay usable nearby and close when ownership or range changes.
- Test a dedicated server without client classes and a client without optional JEI.
- Check every model/texture/message in English and German and inspect server logs.

| Symptom | First checks |
| --- | --- |
| Item places as a world block instead of a raft piece | Common registration ran, correct supplier/item identity, correct jar |
| Red ghost on a wide object | Full rotated support, reserved neighboring cells, world/player obstruction |
| Oversized/stretched model | Normalize mesh to 16 units; definition scales to geometry envelope |
| Purple/black model | Namespace, path, texture reference and modern item definition |
| Raw translation key | Correct lang file and exact key; BlockItems use block names |
| Use does not open machine | Definition interaction returns PASS; controller registered; player in range |
| Processing shares progress across machines | Mutable data was put in controller fields instead of state |
| Progress resets after loading | loaded hook resets defaults without schema check |
| Menu data wraps above 32767 | Native signed-short data packet; scale or split values |
| Dedicated server crashes importing a screen | Move all client classes/registrations to client-only code |
| Addon ID missing when loading | Install matching addon; preserve registered IDs |
| Machine absent from JEI | Addon category/catalyst/plugin required for custom processing |

The main project's native server checks exercise inventory/property save/load,
tick scheduling, owner separation, output conservation, moving menu validity,
slot filtering and one-time drops. They cannot certify every third-party addon
or replace real multiplayer latency and visual testing.

## 13. Public API reference

All API types live under `dev.raftocean.api`:

| Type/member | Use |
| --- | --- |
| `RaftBuildables.API_VERSION` | 2; old v1 register calls continue working |
| `register(RaftBuildable)` | Pure buildable with interaction callback |
| `register(RaftBuildable, RaftMachineBehavior)` | Buildable with owned machine lifecycle |
| `find(String)` | Optional definition by saved namespaced ID |
| `forItem(ItemStack)` | Optional placeable definition for an item |
| `definitions()` | Immutable registry snapshot |
| `machine(String)` | Optional registered controller |
| `RaftBuildable` | Validated immutable definition and callback contract |
| `RaftMachineBehavior` | Shared stateless lifecycle, slot and visual controller |
| `RaftMachineContext` | Server owner/state, current world center and reach validity |
| `RaftMachineState` | Native inventory plus bounded persisted named data |
| `RaftMachineState.hasInt/hasString`, `removeInt/removeString` | Schema migrations without ambiguity |
| `RaftMachineState.integerProperties/stringProperties` | Immutable diagnostic snapshots |
| `RaftMachineState.data(String...)` | Native menu ContainerData adapter |
| `RaftMachineView` / `IDLE` / `fraction()` | Immutable public presentation |
| `RaftMachineMenus.inventory(...)` | Create native filtered inventory menu |
| `RaftMachineMenus.openInventory(...)` | Open the standard inventory |
| `RaftMachineMenus.openCustom(...)` / `Factory` | Open an addon-owned custom menu |
| `RaftEntity.machineContext(BlockPos)` | Optional server machine handle at owner anchor |
| `RaftEntity.machineContextAt(BlockPos)` | Server owner lookup from any occupied cell |
| `RaftEntity.machineView(BlockPos)` | Latest public presentation, server or client |

`dev.raftocean.api.internal` and `RaftEntity.publishAddonMachines` are the
implementation bridge; addons must not mutate tracking snapshots or instantiate
the host. Save encoding and tracking internals may change between releases.
