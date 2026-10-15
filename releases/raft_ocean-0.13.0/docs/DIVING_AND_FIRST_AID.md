# Diving equipment and first aid — 0.11.7

This milestone 11 update adds three original craftable items inspired by the diving and healing mechanics in [Raft's official developer updates](https://store.steampowered.com/news/posts/?appids=648800&enddate=1565969599&feed=steam_community_announcements). Their names, recipes, models and texture layers are original.

| Item | Crafting ingredients | Use |
| --- | --- | --- |
| Tidal Diving Hood | 2 leather, glass, cordage, copper wire | Wear in the head slot to preserve air underwater |
| Reef Flippers | 2 leather, canvas roll, binding paste | Wear in the feet slot to improve underwater movement |
| Shoreleaf Salve | Drift leaf, kelp, binding paste | Hold right-click while injured; after the use animation, consume one and heal three hearts |

Recipes are shapeless. JEI discovers them through the native crafting recipe system. Wearables are unlocked by the existing Tools research project when research gating is enabled; salve has no research lock. Existing saves and player research remain compatible. Items are also available in the mod's salvage creative tab.

The hood adds 2 to Minecraft's native oxygen bonus. This reduces the probability of losing air to one-third before other equipment/enchantments; it does not refill air underwater or grant unlimited breathing. The flippers add 0.65 to native water movement efficiency, which is an underwater movement attribute rather than a fixed 65% speed boost. Bonuses require the correct worn slot and disappear when removed. Both wearables have 192 durability and use native equipping, attributes and saving; they provide no extra armor protection.

Salve heals six health points, capped at maximum health. Healthy players cannot start using it and keep every item. Releasing use early gives no healing. Completing use consumes exactly one. It grants no food, hydration, invulnerability or underwater breathing.

All three have custom inventory/held models, fitted to the inventory icon area. The hood and flippers have original teal/navy/bronze equipment texture layers. Equipped rendering uses Minecraft's native head/footwear silhouette; extended fin geometry on a worn player is a future visual refinement. No client-only classes are loaded by the dedicated server.

## Local test checklist

- Learn Tools research, craft hood/flippers, and equip them in the head/feet slots.
- Dive by an island: compare air drain and swimming with and without equipment. Holding it in a hand should give no bonus.
- Unequip and reconnect: no lingering bonus; equipped items and durability survive saving.
- Take damage, then use salve: one item restores up to three hearts after the animation. At full health it consumes nothing.
- Open JEI and the creative tab: all three items show their recipes, names and original models.
- Test with a second crew member: equipment and salve consumption remain independent per player; Peaceful remains passive.

Native server checks cover the real equipment modifier application/removal path, save/load, slot definitions, loaded recipes, research tags, healthy-use rejection, delayed consumption, exact healing and health clamping. Package audits cover item models, original equipment JSON/PNG assets, bilingual names and prior model coverage. GPU equipment rendering, actual swimming feel and connected multiplayer still require a local client check.

The story remains open-ended; no new story ending is introduced.
