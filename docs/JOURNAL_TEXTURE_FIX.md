# Journal texture fix — 0.10.9

The client log reports `Multiple atlases used in model` for watch_log,
garden_log, vault_log, beacon_log, haven_log and starfall_log. Each 3D book
combined its original item cover texture with block-atlas paper, cloth and metal.
Minecraft rejected the complete model even though all texture files existed.

The model generator now copies each original cover to textures/block/journal_<id>.png
and references it from the book model. All referenced faces use the block atlas.
No covers, journal text, progression, save data or geometry were removed.

The packaged asset audit now rejects models mixing referenced item/block textures.
It detects this exact defect in the previous 0.10.8 JAR. Build and asset verification
results are in build/journal-atlas-build.log.

Update to build/libs/raft_ocean-0.10.9.jar and restart. In development, close the
client and rebuild normally before launching; isolated verification outputs do not
replace resources used by an already running development client. Check all six
journals in inventory, held and dropped. Their original covers and 3D book bodies
should appear; reading them should continue to work without consuming them.
