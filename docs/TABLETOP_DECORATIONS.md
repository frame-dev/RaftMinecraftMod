# Tabletop decorations — 0.11.36

Install `build/libs/raft_ocean-0.11.36.jar` on every client and the server. Targets remain Minecraft 26.3, NeoForge 26.3.0.48-beta and Java 25. Network protocol is 11.36; addon API remains 2.

## Tables and crafting

Two new original wooden tables have clear tops and brass trim:

| Table | Footprint | Shapeless recipe | Grid |
| --- | --- | --- | --- |
| Cabin Side Table | 1 × 1 | 2 oak planks, 1 stick, 1 cordage | 2 × 2 or 3 × 3 |
| Wide Display Table | 2 × 1 | 3 oak planks, 2 sticks, 1 timber brace | 3 × 3 |

Put each repeated ingredient in a separate occupied slot. The existing dining and coffee tables also support tabletop placement. Their fixed tableware/books have been removed so you can choose your own decorations; their cloth runners remain.

## Decorating a table

Hold a Shaded Table Lamp, Navigator Book Stack, Reef Ceramic Vase, Potted Palm, Decorative Chart Stand or Coastal Flower Box and right-click the table's top. The placement preview snaps the object to the actual tabletop height, including low coffee tables and tables on upper floors. Mounted decorations move and rotate with the raft and survive saving and reloading.

Each table tile supports one decoration. The wide table types have two tiles, so you can place two separate small ornaments. A flower box occupies both tiles and must align with one wide table; it cannot overhang a side table or bridge separate tables. Placement still checks occupied space and clearance.

Remove tabletop decorations before removing their supporting table. Destroying the support prunes its dependent decorations instead of leaving them floating. Older saved decorations retain their previous support behavior.

This supports the six listed native decoration types. It does not add arbitrary vanilla blocks, sitting, or free positioning at any point on the tabletop. Inventory icons and model previews are verified automatically; live client appearance and cooperative play still need an in-game check.

## Playtest checklist

1. Craft both new tables. Place them in several orientations, including on an upper floor.
2. Put a lamp and vase on opposite halves of a wide table. Both should sit on the top; a second object in an occupied tile should be blocked.
3. Put books on a coffee table and a flower box on a wide display table. Neither should hover or sink into the top.
4. Move and turn the raft, then save and reload. Decorations should stay attached.
5. Try removing an occupied table, then remove its ornaments first. Only the empty table should be removable.

Implementation files: `RaftPiece.java`, `RaftStructure.java`, `RaftEntity.java`, `RaftTabletopCheck.java` and `tools/generate_furniture.py`. Shared furniture geometry drives the custom models and placement dimensions.
