# Raft Ocean 0.11.12 — recipe fixes

Crumb Bait used a valid one-bread recipe but was silently blocked by the Bait preparation
research project. Basic crumbs now require no research: **one bread produces eight bait**
in the native 2x2 inventory grid, ordinary crafting table and mounted raft crafting table.
The Baited Ocean Rod remains part of Bait preparation research (kelp and plant fiber).
Recipe-book discovery for Crumb Bait now triggers when you acquire bread.

Other recipes retain their progression requirements. When ingredients match a recipe
whose research is missing, the crafting result remains locked and a message now names
the research project and intended item. Study its materials and learn the project at the
Research Table, then arrange the ingredients again. Existing research progress is preserved.
Crafting upgrades, machines and ammunition added in 0.11.10/0.11.11 remain available.

All115 mod crafting recipes passed actual mounted native menu checks with research
unlocked: correct output/count, taking the result and consuming one complete ingredient
batch. New tests also verified Crumb Bait before research in the inventory and raft grids,
right-click consumption, and the advanced rod's retained lock. Existing server, model and
packaged asset/localization checks passed. Log: build/recipe-fixes-second.log.

## Test locally

1. Restart client/server with 0.11.12, or rebuild the project before starting the client.
2. Put one bread into any inventory crafting slot. Expect eight Crumb Bait without studying.
3. Try the same recipe on the raft crafting table; right-click the result and confirm one
   bread is consumed per batch. Shift-click should craft available batches normally.
4. Try an unlearned advanced recipe. Expect a message naming the required research.
5. Learn that project, rearrange the ingredients and retry. Expect the normal craft result.

If a different recipe still fails, report its item name, ingredients, crafting station and
any displayed research message. JEI also contains cooking/processing recipes that belong
to their specified machine rather than a crafting grid.

Targets: Minecraft26.3, NeoForge26.3.0.48-beta, Java25, ModDevGradle2.0.147,
Gradle9.2.1, JEI31.9.0.58. Protocol raft-movement-10.5.
Milestones1–10 complete; milestone11 active; story remains open-ended.
Client visuals, connected co-op latency and prior movement reports still need user testing.
