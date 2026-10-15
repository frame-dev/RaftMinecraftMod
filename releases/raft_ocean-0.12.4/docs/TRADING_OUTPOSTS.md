# Trading outposts and island exploration — 0.11.33

Install `build/libs/raft_ocean-0.11.33.jar` on every client and the server. Targets remain Minecraft 26.3, NeoForge 26.3.0.48-beta and Java 25. Network protocol is 11.33; addon API remains 2.

## Find and trade

Merchant Harbor and Lantern Market generate in unexplored Ocean World chunks. Each has a coastal boarding walk, a covered market terrace, an original wooden counter, colorful canopy, storage props, lanterns and underwater shallows. Right-click the trading counter to open Minecraft's normal barter interface. These are unattended counters; trader NPCs are not included.

The Long-Range Receiver now cycles through nine optional destinations when sneaking, then returns to the main story signal. Merchant Harbor and Lantern Market are the last two optional destinations. Normal use follows the selected signal. For testing with commands enabled:

```text
/locate structure raft_ocean:merchant_harbor
/locate structure raft_ocean:lantern_market
```

There are 20 offers:

| Payment | Goods |
| --- | --- |
| Cod or salmon | Wheat, pumpkin and melon seeds |
| Fish plus salvage scrap | Sugar cane |
| Pufferfish or tropical fish | Kelp or bamboo |
| Salvage scrap plus gold nuggets | Railings, window walls, archways and curved walls |
| Salvage scrap plus fish or gold ingots | Reef flippers or metal spear |
| Gold ingots | Three optional expedition journals |
| Cod plus drift leaves | Shoreleaf salve |

Exact costs and remaining stock appear in the trade screen. Wrong or incomplete payments produce no output. Normal pickup and shift-click consume the displayed payment. A counter admits one customer at a time, shares limited stock across players and saves purchase counts. Stock refreshes when next opened after the configured interval. Research remains required for crafting locked recipes; buying goods does not unlock their recipes.

## Exploration improvements

Twenty-one land-island templates now have marked routes from a coastal landing toward the interior, stairs on climbs, wider clear sections and lights beside the route. Existing loot containers, mod landmarks, tree trunks and underwater resources are preserved. New outposts have irregular coastal terrain and decorated market areas. Routes guide exploration without revealing every secret. The four flooded expedition sites retain their underwater layouts.

Existing generated chunks keep their old appearance. Explore new chunks or create a fresh Ocean World to see the updated islands. The story remains open-ended: trading and optional journals do not introduce a final ending.

## Configuration and data packs

In `config/raft_ocean-synced.toml`:

```toml
[trading]
enabled = true
restockSeconds = 1200
```

The interval accepts 60–86400 seconds of world game time; the default is 20 minutes. Disabling trading closes access to existing menus. Islands remain visible.

Data packs can replace `data/raft_ocean/trading/outpost.json` using the native MerchantOffers codec (`Recipes`, `buy`, optional `buyB`, `sell`, `uses`, `maxUses`). Both outposts use this catalog. Existing stock adopts changed offers at the next restock. Keep 1–64 valid offers with positive stock and legal item stack sizes.

Generate assets with `python tools/generate_outposts.py`. After regenerating older island templates, run this generator last to restore routes. The complete model generator also runs it. `python tools/verify_outpost_islands.py` checks route clearance and protected cells against the retained 0.11.32 jar.

## Validation and playtest

Build, bootstrap and native Ocean World checks passed. Server tests cover actual payments, shift-click, exhausted stock, stock serialization/restock, competing customers, range and config enforcement. Model, localization, research and every loaded crafting recipe were audited.

- Locate each outpost in new chunks and walk from the shore to the counter.
- Trade normally and with shift-click; incomplete payments should give no goods.
- Exhaust an offer, save/reload and confirm stock remains exhausted until restock.
- With two players, confirm the busy counter message and shared stock.
- Explore regenerated story/resource islands and check stairs, route clearance and lighting.

Live client visuals and two-client trading still need player confirmation. Prior moving-raft collision/co-op and third-party mod jar testing remain pending; this update does not establish that those issues are resolved.
