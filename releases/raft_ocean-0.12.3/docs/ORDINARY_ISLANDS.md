# Ordinary islands in 0.9.6

Ocean World gains ten original resource-island templates, independent of the
story progression. They have no journals, keys, seals, or quest buildings.

| Variant | Template footprint | Features |
| --- | --- | --- |
| Shell Sandbar | 17 × 17 | Low irregular beach, washed-up logs, clay/gravel patches and sugar cane |
| Tidal Grove | 25 × 25 | Grassy mound, mixed groundcover, two trees and a sandy beach |
| Flower Meadow | 33 × 33 | Four birches, mixed wildflowers, grass and a rolling shore |
| Twin Cove | 49 × 49 | Sheltered lagoon, five trees, shoreline logs and resource beaches |
| Stone Rise | 65 × 65 | Rocky hill, seven spruce trees, scattered boulders and underground ore |
| Broadleaf Island | 81 × 81 | Broad wooded terrain, twelve mixed trees, boulders, beaches and ore |
| Crescent Reef | 49 × 49 | Open crescent bay and five trees |
| Split Grove | 65 × 65 | Linked wooded lobes and seven trees |
| Longshore | 33 × 33 | Narrow winding sandbar and three trees |
| Mossy Arch | 81 × 81 | Deep coastal inlet, two connected lobes and twelve trees |

Actual shoreline shapes are irregular and occupy less than the square template
footprint. Tree canopies, patches of groundcover and habitat-specific details
vary by island. Larger terrain reaches from the seabed to hills above sea level.
Ordinary mining provides wood, sand, stone, clay, gravel and limited coal/copper/
iron deposits. Grass can supply seeds; island ground supports existing fauna.

## Generation

The new `ordinary_islands` structure set uses 20-chunk regions, separation 12,
and a separate deterministic placement salt. Small variants are more common
than large ones, with weights 4/4/3/3/2/1/3/2/3/1 in the table order. Candidate ordinary
islands within six chunks of the existing island set are excluded to protect
story locations. Story placement and receiver targets remain unchanged.

Island centers stay outside the existing 160-block starter radius. Templates
are placed through native chunk structures, including their surrounding chunks;
the generator now centers each island using its actual template dimensions.
Existing Ocean World settings gain the new set automatically. New islands
appear only in unexplored chunks, with world structure generation enabled.

## Test checklist

- Explore fresh ocean chunks in an existing world, or start a new Ocean World.
- Visit small and large islands, walk over their shorelines, harvest trees and
  gather submerged clay/gravel. Larger islands should extend over multiple chunks.
- In a test world with commands enabled, use `/locate structure
  raft_ocean:shell_sandbar` or `/locate structure raft_ocean:broadleaf_island`.
  All ten identifiers match the variant names above in snake_case.
- Check the receiver still finds Glasswatch, Garden Relay and Echo Vault. Normal
  islands should not advance the story or contain story rewards.

Templates are fixed original variants rather than fully procedural terrain.
Native placement varies their locations by seed; each variant repeats its shape.
Live appearance and generation performance still need user testing. Regenerate
the island resources with `python tools/generate_ordinary_islands.py`.
