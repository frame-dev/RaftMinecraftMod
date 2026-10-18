"""Author native harvestable shallows inside each island template's existing footprint."""
from collections import deque
import random


def add_shallows(name, size, blocks):
    rng = random.Random(name + '_underwater')
    tops = {}
    for (x, y, z), (block, _, _) in blocks.items():
        if y <= 13 and block not in ('minecraft:water', 'minecraft:kelp', 'minecraft:kelp_plant', 'minecraft:air'):
            tops[x, z] = max(y, tops.get((x, z), -1))
    distance = {cell: 0 for cell in tops}
    queue = deque(tops)
    while queue:
        x, z = queue.popleft()
        for cell in ((x-1, z), (x+1, z), (x, z-1), (x, z+1)):
            if 0 <= cell[0] < size and 0 <= cell[1] < size and cell not in distance:
                distance[cell] = distance[x, z] + 1
                queue.append(cell)
    candidates = [cell for cell, d in distance.items() if 1 <= d <= 5 and cell not in tops]
    rng.shuffle(candidates)
    if not candidates:
        raise ValueError(f'{name} has no room for underwater shallows')
    resources = ['clay', 'gravel', 'sand', 'copper_ore', 'iron_ore', 'coal_ore']
    kelp_count = 0
    for index, (x, z) in enumerate(candidates):
        top = max(4, 13 - (6 + distance[x, z] // 2 + rng.randrange(2)))
        resource = resources[index] if index < len(resources) else resources[(x//3 + z//3 + sum(map(ord, name))) % len(resources)]
        for y in range(top + 1):
            blocks[x, y, z] = ('minecraft:' + (resource if y == top else 'stone'), {}, None)
        for y in range(top + 1, 14):
            blocks[x, y, z] = ('minecraft:water', {'level': '0'}, None)
        if index < 6 or rng.random() < .23:
            height = rng.randint(2, min(6, 13 - top))
            for y in range(top + 1, top + height):
                blocks[x, y, z] = ('minecraft:kelp_plant', {}, None)
            blocks[x, top + height, z] = ('minecraft:kelp', {'age': '24'}, None)
            kelp_count += 1
        elif rng.random() < .28:
            blocks[x, top + 1, z] = ('minecraft:seagrass', {}, None)
    assert kelp_count >= 6
    beaches=[(x,y,z) for (x,y,z),(block,_,_) in blocks.items() if block=='minecraft:sand' and y==13
             and (x,y+1,z) not in blocks
             and any(blocks.get((x+dx,y,z+dz),('',{},None))[0]=='minecraft:water' for dx,dz in ((1,0),(-1,0),(0,1),(0,-1)))]
    rng.shuffle(beaches)
    for x,y,z in beaches[:max(1,size//16)]:
        for height in range(1,4):
            blocks[x,y+height,z]=('minecraft:sugar_cane',{'age':'0'},None)
    return kelp_count
