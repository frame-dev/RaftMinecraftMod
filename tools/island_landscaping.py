"""Deterministic coastal scenery that preserves buildings, loot and routes."""
import random


def landscape(name, size, height, blocks, story=False):
    rng = random.Random(name + '_coastal_gardens_v1')
    center = size // 2
    soil = {'minecraft:grass_block', 'minecraft:moss_block', 'minecraft:podzol', 'minecraft:coarse_dirt'}
    plants = {'minecraft:air', 'minecraft:short_grass', 'minecraft:fern', 'minecraft:dandelion',
              'minecraft:poppy', 'minecraft:azure_bluet', 'minecraft:cornflower', 'minecraft:oxeye_daisy'}
    trees = []
    decorated = 0
    surfaces = {}
    for (x, y, z), state in blocks.items():
        if state[0] not in plants and y > surfaces.get((x, z), (-1, ''))[0]:
            surfaces[x, z] = (y, state[0])

    def empty(x, y, z):
        return blocks.get((x, y, z), ('minecraft:air', {}, None))[0] in plants

    def put(x, y, z, block, properties=None):
        if 0 <= x < size and 0 <= z < size and 0 <= y < height:
            blocks[x, y, z] = ('minecraft:' + block, properties or {}, None)

    def add_tree(x, y, z):
        if any((x-tx)**2+(z-tz)**2 <= 36 for tx, tz in trees):
            return False
        trunk = rng.choice([3, 4])
        cells = [(x+dx, ly, z+dz)
                 for ly in range(y+1, y+trunk+3)
                 for dx in range(-2, 3) for dz in range(-2, 3)]
        if not all(empty(*p) for p in cells):
            return False
        species = rng.choice(['oak', 'birch'])
        for ly in range(y+1, y+trunk+1):
            put(x, ly, z, species+'_log', {'axis': 'y'})
        for ly, radius in ((y+trunk-1, 2), (y+trunk, 2), (y+trunk+1, 1), (y+trunk+2, 0)):
            for dx in range(-radius, radius+1):
                for dz in range(-radius, radius+1):
                    if abs(dx)+abs(dz) <= radius+1 and not (dx == dz == 0 and ly <= y+trunk):
                        put(x+dx, ly, z+dz, species+'_leaves', {'persistent': 'true', 'distance': '1', 'waterlogged': 'false'})
        trees.append((x, z))
        return True

    for x in range(3, size-3):
        for z in range(3, size-3):
            # Keep the authored central route, bunker switches and approach docks clear.
            if story and ((abs(x-center) < (7 if size == 33 else 12) and abs(z-center) < (7 if size == 33 else 10))
                          or (abs(x-center) < 4 and z > center-9)):
                continue
            if (x, z) not in surfaces:
                continue
            y, surface = surfaces[x, z]
            if y < 13 or y+1 >= height or not empty(x, y+1, z):
                continue
            if story and surface == 'minecraft:sand' and y >= 15 and rng.random() < .45:
                put(x, y, z, 'moss_block')
                surface = 'minecraft:moss_block'
            if surface in soil:
                if rng.random() < .10:
                    put(x, y+1, z, rng.choice(['fern', 'short_grass', 'oxeye_daisy', 'cornflower', 'azure_bluet']))
                    decorated += 1
                if rng.random() < (.06 if story else .027) and y+6 < height:
                    add_tree(x, y, z)
            elif surface in ('minecraft:sand', 'minecraft:gravel', 'minecraft:stone') and y <= 16 and rng.random() < .014:
                # Rounded, grounded shoreline outcrops. No floating stone ornaments.
                neighbors = [(x+dx, z+dz) for dx, dz in ((0,0), (1,0), (0,1), (-1,0))]
                if all(blocks.get((bx,y,bz), ('',{},None))[0] in ('minecraft:sand','minecraft:gravel','minecraft:stone')
                       and empty(bx,y+1,bz) for bx,bz in neighbors):
                    for bx,bz in neighbors:
                        put(bx, y+1, bz, 'mossy_cobblestone' if rng.random() < .4 else 'andesite')
                    put(x, y+2, z, 'andesite_slab', {'type': 'bottom', 'waterlogged': 'false'})
                    decorated += 1
    if not story and not trees:
        for (x, z), (y, surface) in sorted(surfaces.items()):
            if surface in soil and y >= 13 and y+6 < height and empty(x, y+1, z) and add_tree(x, y, z):
                break
    print(f'{name}: coastal scenery added {len(trees)} trees and {decorated} flower/rock patches')
