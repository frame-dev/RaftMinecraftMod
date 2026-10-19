# Juicer and prepared drinks — 0.13.8 preview

The new Raft Juicer turns four crop/fruit portions into a prepared drink. Its
battery, deliberate Blend action, finished batch and separate glass-serving step
follow the core interaction described by the [Raft juicer reference](https://raft.fandom.com/wiki/Juicer).
Names, recipes, bonuses and all models are original. This mod returns reusable
glasses after drinking; it does not implement Raft's bonus-water meter, consumed
glasses or invalid-recipe leftovers.

## Research and crafting

Learn the existing **Radio receiver** electrical project. Its normal wire, plate
and glass studies now also unlock the juicer. Previously learned electrical
research remains learned; no additional island schematic is required.

Craft the juicer in a 3×3 table:

```text
Glass Wire    Glass
Plate Fitting Plate
Brace Brace   Brace
```

Ingredients: two glass, one copper wire, two metal plates, one scrap fastener
and three timber braces. Glass/wire compatibility tags accept supported mod
alternatives. It occupies one raft cell, requires two cells of vertical clearance
and adds eight units of mass. Place it on a supported foundation or upper floor.

A reusable Drinking Glass has this separate recipe, using five glass blocks for
four drinking glasses:

```text
Glass [empty] Glass
Glass Glass   Glass
```

This leaves Minecraft's three-glass bottle recipe available. Drinking glasses
are available before electrical research and cannot collect ocean water; use the
existing water cups for that.

## Blend and serve

1. Put one recipe portion in each of the four food slots, in any order. Stacks
   can hold spare portions, but each batch consumes exactly one from each slot.
2. Insert a charged Tidal or Deepcycle Battery and press **Blend**.
3. A default recipe takes ninety powered seconds, spending ninety battery charge.
   Pause/Resume and empty batteries retain progress. Ingredients remain present
   until the completed batch consumes all four portions atomically.
4. The finished portion stays inside the juicer without requiring a glass first.
   Insert a Drinking Glass in the separate glass slot and press **Serve**.
5. Serving consumes one glass and one completed portion, placing one filled drink
   in the output. A blocked/full output changes nothing. Drink it to restore
   hydration and receive the empty glass back through native item consumption.

There is no automatic second batch. A finished unserved portion blocks Blend.
Changing the ingredient recipe or changing/removing its data-pack recipe cancels
prior blending progress without consuming the remaining food. Already spent
power is not refunded. Time, portable charge, queue, pause and ready liquid persist
through raft save/reload. Dismantling returns physical inventory, but spills an
unserved portion; serve it before moving the machine. No free filled glass drops.

Hold the cursor over Blend to read the original recipes without JEI. With JEI,
search a drink or the juicer to see the four foods, glass, battery and time. Click
the juicer progress bar to open that category. Recipe data synchronize from the
server and refresh through the existing client recipe cache.

| Drink | Four portions | Temporary bonus |
| --- | --- | --- |
| Melon Breeze | 3 melon slices + 1 apple | Speed I |
| Orchard Cooler | 2 apples + 2 melon slices | Night Vision |
| Shoreberry Tonic | 2 sweet berries + 1 carrot + 1 beetroot | Resistance I |

All three restore twelve hydration by default, capped at twenty. The existing
prepared-meal bonus enable/duration settings also govern drink bonuses; at
defaults they last two minutes. Drinking refreshes a bonus instead of increasing
its level. The ingredient crops use the existing farming and exploration systems.
These recipes provide no ordinary crafting-table shortcut for filled drinks.

## Configuration and data packs

Under `[survival]` in `raft_ocean-synced.toml`:

```toml
juicingEnabled = true
juicerSpeed = 1
juiceHydration = 12
```

`juicerSpeed` supports0.1–4 and multiplies `processingSpeed`. Faster processing
spends charge only for actual running seconds. `juiceHydration` supports1–20.
Disabling juicing preserves placed machines, supplies, queued progress and ready
portions. Existing prepared drinks can still be served and consumed.

Native `raft_ocean:juicing` data-pack recipes have four shapeless ingredients, a
single prepared-juice result and a duration of20–24000ticks. Default recipes are
in `data/raft_ocean/recipe/juicing/`. Only registered prepared-juice items are
accepted as outputs. Add allowed crop items to `raft_ocean:juicer_ingredients`
when extending recipes. Use ordinary crops without container remainders; this
machine does not implement arbitrary container-bearing food byproducts.

The machine uses addon API3 for owned state, persistence, filtered access and
atomic transactions. Portable native batteries are supported; wired power and
foreign energy items are not implemented for the juicer. JEI is optional.

## Local playtest

- Research/craft/place the juicer; inspect its ghost, model and two-cell clearance.
- Blend each recipe; expect one ready portion after ninety powered seconds.
- Pause, remove/replace a battery, and save/reload mid-batch; confirm no food loss.
- Serve with a glass, including a full output; expect one drink per real glass.
- Drink with a full backpack and confirm the reusable glass is retained or dropped
  through the normal consumption remainder handling.
- Repeat on a shared moving raft with a second player. Check the JEI recipe links.

All peers need0.13.8 (movement handshake13.8). Live GUI/JEI appearance, real co-op
latency and older user saves remain local playtests; see RELEASE_VERIFICATION.md.
