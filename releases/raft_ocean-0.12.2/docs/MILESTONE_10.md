# Milestone 10 — research and progression (0.10.0)

This describes the original implementation. For the current material-driven
research and 24 projects, see [the 0.10.2 guide](RESEARCH_AND_FINE_MODELS.md).

Craft and mount a **Raft Research Table**, then right-click it to open the research
screen. The recipe is `PPP / _S_ / PPP`: P is any native planks and S is a stick.
The bench uses the same placement preview, building support and collision system
as other raft stations. Its menu follows the moving raft and closes when you
leave its eight-block reach or remove the table.

The six topics form four tiers: basic wood recipes are available immediately,
followed by scrap workshop knowledge, metal fabrication and advanced navigation
and radio technology. The screen shows each topic's cost and whether it is ready,
missing its sample, awaiting prerequisites or already learned. Hover a study
button for the unlock list and prerequisites. Studying consumes exactly one
sample from your inventory; simply opening or closing the screen consumes nothing.

| Topic | Sample | Prerequisites | Unlocks |
| --- | --- | --- | --- |
| Workshop | 1 scrap | None | Fasteners, hook heads, collection nets, grill and smelter |
| Metal | 1 iron ingot | Workshop | Metal plates, copper wire, reinforced braces, anchor and upgraded hook |
| Cultivation | 1 planting mix | Workshop | Tree planter and cooking pot |
| Textiles | 1 woven mesh | Workshop | Canvas rolls |
| Navigation | 1 canvas roll | Workshop, Metal, Textiles | Sail, steering wheel and engine |
| Radio | 1 copper wire | Workshop, Metal | Receiver, antenna upgrade and long-range receiver |

Foundations, raft expansion, research/crafting tables, basic storage, cups, water
survival, fishing and simple crops remain available before research. Sample
recipes remain reachable through the dependency graph. Existing material recipes
provide the wood → scrap → metal → advanced crafting chain; no material or
station in an existing save is removed or disabled.

Research belongs to each player, persists through saves and is copied on death.
Knowledge is not stored in the table, so breaking/replacing it cannot reset
progress. Each discovery grants an advancement and the recipe-book entries for
its unlocks, including alternative material recipes. Repeated or forged study
actions cannot consume another sample. A crew can share materials, but each
player must learn their own topics.

Tagged outputs are enforced by the server in inventory 2×2 crafting, native 3×3
tables and mounted raft crafting. Locked outputs are empty and cannot be taken
or shift-crafted; recipe ingredients remain intact. Creative players bypass the
restriction. JEI still displays the ordinary recipe ingredients; consult the
research screen for the required topic. There is no dedicated JEI research
category or automatic study transfer in this milestone. Native automated Crafters
have no player identity and do not use the per-player manual crafting gate.

## Configuration and data packs

The gameplay config contains:

```toml
[progression]
researchRequired = true
```

Set this to false for unrestricted crafting, including in an existing world.
Research progress is retained when the setting changes. The server synchronizes
this setting. Existing worlds start with no learned research; their already built
equipment keeps working and placing already owned items remains allowed.

Sample membership and gated outputs use native item tags:
`data/raft_ocean/tags/item/research/samples/<topic>.json` and
`data/raft_ocean/tags/item/research/unlocks/<topic>.json`. Data packs can replace
or extend them. An item present in multiple unlock tags requires all those topics.
Update the corresponding research advancement rewards if adding new recipe-book
entries. Costs are one tagged item per topic; the six-topic prerequisite graph
is defined in `Research.java`. Asset generation is reproducible through
`tools/generate_research_assets.py`.

## Build, story and test checklist

Use matching **0.10.0** clients/server, protocol `raft-movement-10`.
Artifact: `build/libs/raft_ocean-0.10.0.jar`. Target remains Minecraft 26.3,
NeoForge 26.3.0.48-beta, Java 25, Gradle 9.2.1 and ModDevGradle 2.0.147.

The six current story islands retain the **open ending**: an unfamiliar eastern
transmission cannot yet be resolved to another destination. Research adds no
chapter, ending or new story island. Radio research unlocks crafting the receiver;
it does not reset or replace saved journal progress.

- Craft/place the research table and inspect its preview and menu while drifting.
- Try a gated recipe before studying. Expect no output and no ingredient loss.
- Study Workshop, then each remaining topic in prerequisite order. Expect exactly
  one sample consumed, learned status, advancement toast and recipe-book unlocks.
- Try repeated study, missing samples and missing prerequisites. Expect no cost.
- Craft unlocked equipment in both inventory and raft crafting, including shift
  clicks and alternative recipes. Check JEI ingredient browsing.
- Test with two players, save/reload and death/respawn. Knowledge should remain
  separate and survive both persistence paths.
- Remove the table while its screen is open and test a table on an upper floor.
  Expect closure without losing inventory; replacing it should retain knowledge.
- Disable researchRequired and verify locked crafting becomes available.
- Read Starfall and scan with the receiver. The unresolved story lead should remain.

Build and both isolated native server runs pass, including 35 dedicated research
checks, 619 construction checks, 661 Ocean World checks and all earlier systems.
The research checks exercise actual native 2×2/raft crafting, one-sample costs,
the real advancement tracker, ownership, save/reload, drift, reach, removal and
config/Creative bypasses. Packaged models, tags, reward recipe IDs, translations
and the open story ending are verified.

Client visual layout, advancement toasts, death/respawn and connected multiplayer
still require in-game testing before the next milestone.
