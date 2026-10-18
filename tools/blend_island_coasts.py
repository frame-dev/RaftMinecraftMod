"""Stage connected, graded island templates without touching a running mod build.

Run with Python; output is a native Minecraft data pack in build/island-coast-fix.
Buildings, containers, puzzle blocks and authored paths retain their exact cells.
"""
from collections import deque
from pathlib import Path
import heapq
import json
import math
import random
import zipfile
import template_io

PROJECT = Path(__file__).resolve().parents[1]
SOURCE = PROJECT / 'src/main/resources/data/raft_ocean'
OUTPUT = PROJECT / 'build/island-coast-fix'
GROUND = {'minecraft:' + n for n in ('stone', 'sand', 'gravel', 'dirt',
    'grass_block', 'coarse_dirt', 'moss_block', 'podzol', 'andesite',
    'granite', 'diorite', 'clay', 'basalt', 'tuff', 'coal_ore', 'copper_ore', 'iron_ore')}
FLORA = {'minecraft:' + n for n in ('short_grass', 'fern', 'dandelion', 'poppy',
    'azure_bluet', 'cornflower', 'oxeye_daisy', 'sugar_cane', 'seagrass',
    'tall_seagrass', 'kelp', 'kelp_plant')}
PASSIVE = {'minecraft:air', 'minecraft:water'}
WATER = ('minecraft:water', {'level': '0'}, None)
UNDERWATER_RESOURCES = {'minecraft:clay', 'minecraft:gravel'}
SEA_PLANTS = {'minecraft:kelp', 'minecraft:kelp_plant', 'minecraft:seagrass',
    'minecraft:tall_seagrass'}
STORY_ISLANDS = {'radio_tower', 'glasswatch', 'garden_relay', 'echo_vault',
    'ember_beacon', 'tideworks_haven', 'starfall_archive'}


def vegetation(block):
    return block in FLORA or block.endswith(('_leaves', '_log'))


def adjacent(p, size):
    x, z = p
    for q in ((x-1, z), (x+1, z), (x, z-1), (x, z+1)):
        if 0 <= q[0] < size and 0 <= q[1] < size:
            yield q


def components(tops, size):
    remaining = {p for p, y in tops.items() if y >= 14}
    result = []
    while remaining:
        start = min(remaining)
        pending = [start]
        remaining.remove(start)
        found = {start}
        while pending:
            for p in adjacent(pending.pop(), size):
                if p in remaining:
                    remaining.remove(p)
                    found.add(p)
                    pending.append(p)
        result.append(found)
    # Individual shoreline rocks are scenery, not separate explorable land masses.
    return sorted((c for c in result if len(c) >= 9), key=lambda c: (-len(c), min(c)))


def blur(tops, size, locked):
    """Separable low-pass grading removes rectangular preservation seams."""
    current = {p: float(y) for p, y in tops.items()}
    for iteration in range(36):
        axis = iteration % 2
        next_heights = {}
        for (x, z), y in current.items():
            p = x, z
            if p in locked:
                next_heights[p] = y
                continue
            a = (x-1, z) if axis == 0 else (x, z-1)
            b = (x+1, z) if axis == 0 else (x, z+1)
            next_heights[p] = (current.get(a, -1) + 2*y + current.get(b, -1)) / 4
        current = next_heights
    return {p: round(y) for p, y in current.items()}


def height_noise(name, size):
    """Low-amplitude deterministic terrain variation breaks concentric terraces."""
    result = {}
    for spacing, amplitude, salt in ((28, 2.2, 'broad'), (11, 0.8, 'detail')):
        rng = random.Random(f'{name}:{salt}')
        count = (size + spacing - 1) // spacing + 2
        grid = [[rng.uniform(-1, 1) for _ in range(count)] for _ in range(count)]

        def smooth(value):
            return value * value * (3 - 2 * value)

        for x in range(size):
            gx = x / spacing
            ix = int(gx)
            fx = smooth(gx - ix)
            for z in range(size):
                gz = z / spacing
                iz = int(gz)
                fz = smooth(gz - iz)
                a = grid[ix][iz] * (1 - fx) + grid[ix + 1][iz] * fx
                b = grid[ix][iz + 1] * (1 - fx) + grid[ix + 1][iz + 1] * fx
                result[x, z] = result.get((x, z), 0.0) + (a * (1 - fz) + b * fz) * amplitude
    return result


def connect(tops, size, locked):
    """Join separated land by broad shallow saddles following the least deep water."""
    joined = 0
    while len(parts := components(tops, size)) > 1:
        main = parts[0]
        targets = set().union(*parts[1:])
        costs = {p: 0 for p in main}
        previous = {p: None for p in main}
        pending = [(0, p) for p in sorted(main)]
        heapq.heapify(pending)
        end = None
        while pending:
            cost, p = heapq.heappop(pending)
            if cost != costs[p]:
                continue
            if p in targets:
                end = p
                break
            for q in adjacent(p, size):
                if q in locked and tops[q] < 14:
                    continue
                trial = cost + 1 + max(0, 14-tops[q]) * 3
                if trial < costs.get(q, float('inf')):
                    costs[q] = trial
                    previous[q] = p
                    heapq.heappush(pending, (trial, q))
        if end is None:
            raise ValueError(f'Authored structures prevent a safe land connection: {[(len(c), min(c)) for c in parts]}')
        while end is not None:
            x, z = end
            for dx in range(-4, 5):
                for dz in range(-4, 5):
                    q = x+dx, z+dz
                    if q in tops and q not in locked:
                        # Five-block walkable beach, feathered into adjacent shallows.
                        tops[q] = max(tops[q], 14-max(0, math.ceil(math.hypot(dx, dz)-2)))
            end = previous[end]
        joined += 1
    return joined


def distance_from(cells, size):
    distances = {p: 0 for p in cells}
    pending = deque(sorted(cells))
    while pending:
        p = pending.popleft()
        for q in adjacent(p, size):
            if q not in distances:
                distances[q] = distances[p]+1
                pending.append(q)
    return distances


def refine(path):
    root = template_io.read(path)
    size, height, depth = root['size'][1][1]
    assert size == depth
    before = template_io.blocks(root)
    tops = {(x, z): -1 for x in range(size) for z in range(size)}
    structural = {}
    for (x, y, z), state in before.items():
        block = state[0]
        if block in GROUND:
            tops[x, z] = max(tops[x, z], y)
        elif state[2] is not None or (block not in PASSIVE and not vegetation(block)):
            structural[x, y, z] = state
    # Lock foundation and headroom columns around buildings, stairs and trails.
    locked = set()
    for x, y, z in structural:
        for dx in range(-2, 3):
            for dz in range(-2, 3):
                if (x+dx, z+dz) in tops:
                    locked.add((x+dx, z+dz))
    locked.update((x, z) for (x, _, z), state in before.items() if state[0] in SEA_PLANTS)
    story_catalog = None
    story_paths = []
    story_headroom = set()
    if path.stem in STORY_ISLANDS:
        story_catalog = json.loads((SOURCE/'story_island_exploration.json').read_text())[path.stem]
        story_paths = [site['entry'] for site in story_catalog['sites']] + story_catalog['trails']
        locked.update((x, z) for x, _, z in story_paths)
    original_tops = tops.copy()
    # Boardwalks and authored path surfaces are walkable connections even where
    # the natural seabed beneath them is submerged. Do not turn their roofs into hills.
    for (x, y, z), state in structural.items():
        if 14 <= y <= 18 and (state[0].endswith(('_planks', '_stairs')) or state[0] == 'minecraft:dirt_path'):
            tops[x, z] = max(tops[x, z], y)
    original_components = len(components(tops, size))
    tops = blur(tops, size, locked)
    variation = height_noise(path.stem, size)
    for p, top in tops.items():
        if top >= 14 and p not in locked:
            tops[p] = max(14, round(top + variation[p]))
    joined = connect(tops, size, locked) if original_components else 0
    land = {p for p, y in tops.items() if y >= 14}
    if land:
        distances = distance_from(land, size)
        for (x, z), top in tops.items():
            if (x, z) not in locked and top < 14:
                # Sloping reef fades completely into the existing ocean seabed.
                edge = min(x, z, size-1-x, size-1-z)
                tops[x, z] = min(top, max(-1, 13-math.ceil(distances[x, z]*.55)), edge-1)
    else:
        # Underwater sites retain their ruins; square stone aprons become irregular reefs.
        c = (size-1)/2
        phase = sum(map(ord, path.stem)) * .17
        for (x, z), top in tops.items():
            if (x, z) in locked:
                continue
            angle = math.atan2(z-c, x-c)
            radius = c*(.88+.045*math.sin(3*angle+phase)+.025*math.cos(5*angle))
            rim = radius-math.hypot(x-c, (z-c)*1.08)
            tops[x, z] = min(top, max(-1, math.floor(rim*.6)), min(x,z,size-1-x,size-1-z)-1)
    # Grade steep natural transitions, keeping architectural cliffs intact.
    for _ in range(size):
        changed = False
        for p, y in tops.items():
            if p in locked:
                continue
            limit = min((tops[q]+1 for q in adjacent(p, size)), default=y)
            if y > limit:
                tops[p] = limit
                changed = True
        if not changed:
            break
    if land:
        joined += connect(tops, size, locked)
    after = {}
    for (x, z), top in tops.items():
        if (x, z) in locked:
            continue
        old = original_tops[x, z]
        surface = before.get((x, old, z), ('minecraft:sand', {}, None))
        for y in range(max(top, 13)+1):
            if y > top:
                after[x, y, z] = WATER
            else:
                material = 'sand' if top <= 15 else 'grass_block' if y == top else 'dirt' if y >= top-2 else 'stone'
                if top <= 15 and y < top-2:
                    material = 'stone'
                after[x, y, z] = ('minecraft:'+material, {}, None)
                if (y == top and top > 15 and surface[0] in GROUND
                        and not surface[0].endswith('_ore') and surface[0] not in UNDERWATER_RESOURCES):
                    after[x, y, z] = surface
    # Keep every locked column verbatim, including caves and puzzle airspace.
    for (x, y, z), state in before.items():
        if land and (x, z) not in locked and state[0] in UNDERWATER_RESOURCES:
            after.pop((x, y, z), None)
    for (x, y, z), state in before.items():
        if (x, z) in locked:
            after[x, y, z] = state
        elif state[0].endswith('_ore') or state[0] in UNDERWATER_RESOURCES:
            if state[0] in UNDERWATER_RESOURCES and not land:
                after[x, y, z] = state
                continue
            placed = False
            for radius in range(size):
                for dx in range(-radius, radius+1):
                    for dz in range(-radius, radius+1):
                        if max(abs(dx), abs(dz)) != radius:
                            continue
                        q = x+dx, z+dz
                        if q not in tops or q in locked or tops[q] < 0:
                            continue
                        top = tops[q] - (1 if tops[q] >= 14 else 0)
                        for target in range(min(y, top), -1, -1):
                            pos = q[0], target, q[1]
                            current = after.get(pos)
                            if current is None or (not current[0].endswith('_ore') and current[0] not in UNDERWATER_RESOURCES):
                                after[pos] = state
                                placed = True
                                break
                        if placed:
                            break
                    if placed:
                        break
                if placed:
                    break
            assert placed, (path.stem, x, y, z, 'No safe resource position')
        elif state[0] in ('minecraft:kelp', 'minecraft:kelp_plant', 'minecraft:seagrass'):
            target = y + tops[x, z]-original_tops[x, z]
            if tops[x, z] >= 0 and tops[x, z] < target <= 13:
                after[x, target, z] = state
    # New foliage follows the graded ground instead of floating above old plateaus.
    from island_landscaping import landscape
    if land:
        for x, z in locked:
            for y in range(height):
                after[x, y, z] = ('minecraft:structure_void', {}, None)
        landscape(path.stem+'_blended', size, height, after, story=path.stem in STORY_ISLANDS)
        for x, z in locked:
            for y in range(height):
                pos = x, y, z
                if pos in before:
                    after[pos] = before[pos]
                else:
                    after.pop(pos, None)
        if path.stem in STORY_ISLANDS:
            for x, y, z in story_paths:
                for dy in (1, 2):
                    pos = x, y+dy, z
                    story_headroom.add(pos)
                    state = after.get(pos)
                    if state is not None and (
                            state[0] in GROUND or state[0] in FLORA or state[0] in PASSIVE or vegetation(state[0])):
                        after.pop(pos)
    for pos, state in structural.items():
        assert after.get(pos) == state, (path.stem, pos, 'Landmark changed')
    for pos, state in before.items():
        if (pos[0], pos[2]) in locked and pos not in story_headroom:
            assert after.get(pos) == state, (path.stem, pos, 'Protected column changed')
    assert all(0 <= x < size and 0 <= z < size and 0 <= y < height for x,y,z in after)
    assert not land or len(components(tops, size)) == 1, path.stem
    from collections import Counter
    ores_before = Counter(s[0] for s in before.values() if s[0].endswith('_ore'))
    ores_after = Counter(s[0] for s in after.values() if s[0].endswith('_ore'))
    assert ores_after == ores_before, (path.stem, 'Resources lost during grading')
    underwater_before = Counter(s[0] for s in before.values() if s[0] in UNDERWATER_RESOURCES)
    underwater_after = Counter(s[0] for s in after.values() if s[0] in UNDERWATER_RESOURCES)
    assert underwater_after == underwater_before, (
        path.stem, 'Underwater resource counts changed during grading',
        dict(underwater_before), dict(underwater_after))
    root['RaftOceanCoastalBlendVersion'] = (3, 1)
    template_io.replace_blocks(root, after)
    target = OUTPUT / 'data/raft_ocean/structure/islands' / path.name
    template_io.write(target, root)
    # Validate the actual serialized template, including container NBT.
    assert template_io.blocks(template_io.read(target)) == after
    return {'width': size, 'landComponentsBefore': original_components,
            'landComponentsAfter': len(components(tops, size)), 'connections': joined,
            'protectedColumns': len(locked), 'landmarkBlocksPreserved': len(structural),
            'oreBlocksPreserved': sum(ores_before.values()),
            'underwaterResourcesPreserved': sum(underwater_before.values()),
            'changedTerrainColumns': sum(tops[p] != original_tops[p] for p in tops)}


def main():
    OUTPUT.mkdir(parents=True, exist_ok=True)
    report = {}
    for path in sorted((SOURCE / 'structure/islands').glob('*.nbt')):
        report[path.stem] = refine(path)
        print(path.stem, report[path.stem], flush=True)
    version = json.loads((PROJECT/'build/raft-api/minecraft/version.json').read_text())
    data_version = [version['pack_version']['data_major'], version['pack_version']['data_minor']]
    pack = {'pack': {'description': 'Raft Ocean: connected coastlines and graded reefs',
                     'min_format': data_version, 'max_format': data_version}}
    (OUTPUT/'pack.mcmeta').write_text(json.dumps(pack, indent=2)+'\n')
    (OUTPUT/'verification.json').write_text(json.dumps(report, indent=2)+'\n')
    with zipfile.ZipFile(PROJECT/'build/raft-ocean-connected-islands.zip', 'w', zipfile.ZIP_DEFLATED) as archive:
        for path in sorted((OUTPUT/'data').rglob('*.nbt')):
            archive.write(path, path.relative_to(OUTPUT).as_posix())
        archive.write(OUTPUT/'pack.mcmeta', 'pack.mcmeta')
    print('All island templates serialized and checked; source resources untouched.')


if __name__ == '__main__':
    main()
