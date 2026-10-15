# Starter anchor and grill — 0.11.30

Both new stations can be crafted before research and placed on a supported raft
foundation. The existing reusable metal anchor and grill remain available.

## Disposable Stone Anchor

Total cost: four cobblestone, four string, two planks and one salvage scrap.
The total exceeds nine ingredients, so use two ordinary 3×3 crafting recipes.

First craft a Stone Anchor Bundle:

```text
C S C
S   S
C S C
```

C = cobblestone; S = string. Then craft the anchor:

```text
P X P
  B
```

P = any planks; X = salvage scrap; B = Stone Anchor Bundle.
Floating ocean crates and barrels now have a chance to contain cobblestone,
with two to four stones per selected loot entry.

Place the anchor, open its controls and select Lower. This stops the raft and
its associated debris drift. The deployed state survives saving and rejoining.
Select Release & discard to resume sailing: the anchor disappears permanently.
A deployed anchor removed with the building hammer or detached by foundation
damage does not drop a reusable anchor. An unused placed anchor can be recovered.

## Basic Food Grill

Craft in a 3×3 table:

```text
P X P
P S P
  X
```

P = any planks; X = salvage scrap; S = string.
Total: four planks, two salvage scrap and one string.

Insert raw food in the food slot and planks or another accepted fuel in the fuel
slot. Collect cooked food from the result slot. This uses loaded campfire cooking
recipes, needs no water or research, and supports the grill speed configuration.
It cooks food; use the separate smelter for ores. JEI shows its crafting recipe,
food recipes, station catalyst and menu recipe link.

## Local test checklist

- Install 0.11.30 on all clients and the server; no new world is needed.
- Craft both stations before research using the recipes above.
- Lower the anchor while moving: the raft and associated debris should stop.
- Save/rejoin while anchored, then release: the raft moves and the anchor is gone.
- Try the building hammer on a lowered anchor: it must not return an anchor item.
- Collect floating crates/barrels and check for occasional cobblestone loot.
- Cook a fish with plank fuel; check the output, placement preview and JEI links.

Story remains open-ended; milestone 11 continues.

## Verification

Build and native dedicated-server checks passed, including 259,199 crafting,
666 research and 848 survival assertions. New tests cover anchor deployment,
saved state, consumption and stale-menu reuse; unfueled food preservation,
food/ore filtering and successful fish cooking; and cobblestone loot from both
floating container types. All loaded 1,399 crafting recipes were audited.
Packaged model, surface, translation and research audits also passed.
In-game client visuals and cooperative play still need the local tests above.
