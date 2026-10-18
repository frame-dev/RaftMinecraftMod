"""Expand authored story cores into deterministic natural exploration islands."""
from collections import deque
import json
import math
import random
from pathlib import Path
import template_io
from underwater_resources import add_shallows

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/data/raft_ocean'
SIZES = {'radio_tower': 129, 'glasswatch': 153, 'garden_relay': 177, 'echo_vault': 201,
         'ember_beacon': 217, 'tideworks_haven': 233, 'starfall_archive': 249}
GROUND = {'minecraft:' + n for n in ('grass_block', 'sand', 'stone', 'gravel', 'dirt',
          'coarse_dirt', 'moss_block', 'dirt_path', 'oak_planks', 'spruce_planks',
          'stone_bricks', 'mossy_cobblestone', 'andesite', 'stone_brick_stairs')}


def coastline(x, z, outline):
    """Return polygon membership and distance to the nearest shoreline edge."""
    inside = False
    distance = float('inf')
    for index,(ax,az) in enumerate(outline):
        bx,bz = outline[(index+1)%len(outline)]
        if (az>z)!=(bz>z) and x<(bx-ax)*(z-az)/(bz-az)+ax:
            inside = not inside
        vx,vz = bx-ax,bz-az
        t = max(0,min(1,((x-ax)*vx+(z-az)*vz)/(vx*vx+vz*vz)))
        distance = min(distance,math.hypot(x-ax-t*vx,z-az-t*vz))
    return inside,distance


def landing_route(blocks, trail, center):
    allowed = {'minecraft:dirt_path','minecraft:gravel','minecraft:oak_planks','minecraft:stone_brick_stairs'}
    cells = {tuple(p) for p in trail if blocks[tuple(p)][0] in allowed}
    shores = [p for p in cells if p[1]<=15]
    assert shores, 'Landing route has no coastal entry'
    start = max(shores,key=lambda p:(p[0]-center)**2+(p[2]-center)**2)
    previous = {start:None}
    pending = deque([start])
    while pending:
        x,y,z = pending.popleft()
        for dx,dz in [(1,0),(-1,0),(0,1),(0,-1)]:
            for dy in [-1,0,1]:
                p=x+dx,y+dy,z+dz
                if p in cells and p not in previous:
                    previous[p]=(x,y,z)
                    pending.append(p)
    goal=min(previous,key=lambda p:(p[0]-center)**2+(p[2]-center)**2)
    route=[]
    while goal is not None:
        route.append(list(goal))
        goal=previous[goal]
    return route[::-1]


def expand_templates(names=None):
    shapes = json.loads((ROOT/'story_island_shapes.json').read_text())
    report_path = ROOT / 'story_island_exploration.json'
    report = json.loads(report_path.read_text()) if report_path.exists() else {}
    route_path = ROOT / 'island_routes.json'
    routes = json.loads(route_path.read_text()) if route_path.exists() else {}
    for name in names or SIZES:
        if name not in SIZES:
            continue
        path = ROOT / f'structure/islands/{name}.nbt'
        root = template_io.read(path)
        revision = root.get('RaftOceanExplorationVersion', (3, 0))[1]
        if revision == 2:
            continue
        if revision:
            raise ValueError(f'Regenerate the original {name} template before changing its coastline revision')
        old_size = root['size'][1][1][0]
        size = SIZES[name]
        height = 48
        shift = (size - old_size) // 2
        c = size // 2
        half = old_size // 2
        rng = random.Random(name + '_expanded_coast_v1')
        phase = rng.random() * math.tau
        original = {(x+shift, y, z+shift): state for (x, y, z), state in template_io.blocks(root).items()}
        blocks = {}
        tops = {}

        def core(x, z):
            return shift <= x < shift+old_size and shift <= z < shift+old_size

        def put(x, y, z, block, props=None, nbt=None):
            assert 0 <= x < size and 0 <= z < size and 0 <= y < height, (name, x, y, z)
            blocks[x, y, z] = (block if ':' in block else 'minecraft:'+block, props or {}, nbt)

        def column(x, z, top):
            for y in range(0, top+1):
                surface = 'sand' if top <= 15 else 'gravel' if top >= 24 else 'grass_block'
                put(x, y, z, surface if y == top else 'dirt' if y >= top-2 and top > 15 else 'stone')
            tops[x, z] = top

        for x in range(2, size-2):
            for z in range(2, size-2):
                dx, dz = x-c, z-c
                px,pz = dx/(c-4),dz/(c-4)
                inside,shore = coastline(px+.014*math.sin(pz*31+phase),
                                         pz+.012*math.sin(px*23-phase),shapes[name]['outline'])
                if not inside:
                    continue
                if core(x, z):
                    continue
                rim = min(max(abs(dx)-half, 0), max(abs(dz)-half, 0)) if abs(dx)>half and abs(dz)>half else max(abs(dx)-half, abs(dz)-half)
                top = 12 + round(10*min(.8,shore/.4))
                ridge = 5*math.exp(-((dx+(c*.38))**2+(dz-c*.24)**2)/(c*.27)**2)
                top += round(ridge*min(1, max(0, rim)/8))
                top = min(top, 16+max(0, round(rim/2)))
                column(x, z, top)
        blocks.update(original)
        # Round the old template's square reef boundary into tidal pools.
        # Water may become terrain; every solid landmark/resource and plant
        # keeps its original state, and kelp columns remain open to the sky.
        occupied = {}
        for (x,y,z),state in original.items():
            occupied.setdefault((x,z),[]).append((y,state[0]))
        for x in range(shift,shift+old_size):
            for z in range(shift,shift+old_size):
                dx,dz=x-c,z-c
                radius=half*(.84+.085*math.sin(math.atan2(dz,dx)*3+phase))
                if math.hypot(dx,dz)<radius:
                    continue
                column_states=occupied.get((x,z),[])
                if any(block in ('minecraft:kelp','minecraft:kelp_plant','minecraft:seagrass','minecraft:sugar_cane')
                       or y>13 and block not in ('minecraft:water','minecraft:air') for y,block in column_states):
                    continue
                for y in range(17):
                    pos=x,y,z
                    if pos not in original or original[pos][0]=='minecraft:water':
                        put(x,y,z,'grass_block' if y==16 else 'dirt' if y>=14 else 'stone')
        # Narrow boardwalks cross the retained old reef without replacing its
        # water, resources or kelp, and join the expanded terrain beyond it.
        for axis in (0, 1):
            for sign in (-1, 1):
                for distance in range(max(6, half-7), half+9):
                    for width in (-1, 0, 1):
                        x, z = (c+sign*distance,c+width) if axis==0 else (c+width,c+sign*distance)
                        if (x,16,z) not in original:
                            put(x,16,z,'oak_planks')
                        if not core(x,z):
                            for y in (17,18):
                                blocks.pop((x,y,z),None)
        delta = min(c-13, half+11)
        sites = []
        for kind, ox, oz in [('shelter', delta, 0), ('garden_ruin', 0, -delta),
                             ('lookout', -delta, 0), ('rock_grotto', 0, delta)]:
            x, z = c+ox, c+oz
            # Find nearby land wholly outside the protected landmark footprint.
            candidates = [(bx, bz) for bx, bz in tops if not core(bx, bz)
                          and not any(core(bx+dx, bz+dz) for dx, dz in ((-6,-6), (-6,6), (6,-6), (6,6)))
                          and 8 <= bx < size-8 and 8 <= bz < size-8 and tops[bx, bz] >= 15
                          and all((bx+dx,bz+dz) in tops for dx in range(-4,5) for dz in range(-4,5))]
            x, z = min(candidates, key=lambda p: (p[0]-x)**2+(p[1]-z)**2)
            top = tops[x, z]
            # Grade surrounding terrain into a small, walkable pad rather than a sheer platform.
            for dx in range(-7, 8):
                for dz in range(-7, 8):
                    cell = x+dx, z+dz
                    if cell not in tops or core(*cell):
                        continue
                    previous = tops[cell]
                    distance = max(abs(dx), abs(dz))
                    target = top if distance <= 4 else round(top+(previous-top)*(distance-4)/3)
                    for y in range(max(previous,target,16)+1):
                        blocks.pop((cell[0], y, cell[1]), None)
                    column(*cell, target)
            sites.append({'kind': kind, 'pos': [x, top+1, z], 'entry': [x, top, z-4]})
            if kind == 'shelter':
                for dx, dz in [(-3,-2), (3,-2), (-3,2), (3,2)]:
                    for y in range(top+1, top+4):
                        put(x+dx, y, z+dz, 'stripped_spruce_log', {'axis': 'y'})
                for dx in range(-3, 4):
                    for dz in range(-2, 3):
                        put(x+dx, top+4+(1 if abs(dx)<2 else 0), z+dz, 'white_wool' if (dx+dz)%3 else 'cyan_wool')
                put(x-2, top+1, z, 'crafting_table')
                put(x+2, top+1, z+1, 'barrel', {'facing':'up','open':'false'},
                    {'id': (8, 'minecraft:barrel'), 'LootTable': (8, 'raft_ocean:chests/story_side_supplies')})
            elif kind == 'garden_ruin':
                for dx in (-3, 3):
                    for dz in range(-3, 4):
                        put(x+dx, top+1, z+dz, 'mossy_cobblestone')
                for dx in (-1, 1):
                    for dz in range(-2, 3):
                        put(x+dx, top, z+dz, 'farmland', {'moisture':'7'})
                        put(x+dx, top+1, z+dz, 'carrots' if name in ('garden_relay','tideworks_haven') else 'wheat', {'age':'7'})
                for dz in range(-2, 3):
                    put(x, top, z+dz, 'water', {'level':'0'})
                put(x+2, top+1, z+2, 'composter', {'level':'0'})
                put(x-2, top+1, z+2, 'chest', {'facing':'north','type':'single','waterlogged':'false'},
                    {'id': (8, 'minecraft:chest'), 'LootTable': (8, 'raft_ocean:chests/story_side_supplies')})
            elif kind == 'lookout':
                for dx in range(-3, 4):
                    for dz in range(-2, 3):
                        put(x+dx, top, z+dz, 'gravel' if (dx+dz)%3 else 'mossy_cobblestone')
                        if dz == 2 or abs(dx) == 3:
                            put(x+dx, top+1, z+dz, 'cobblestone_wall', {'east':'low','west':'low','north':'none','south':'none','up':'true','waterlogged':'false'})
                put(x+2, top+1, z+1, 'barrel', {'facing':'up','open':'false'},
                    {'id': (8, 'minecraft:barrel'), 'LootTable': (8, 'raft_ocean:chests/story_side_supplies')})
            else:
                # An open-ended natural stone tunnel; both mouths have ground-level access.
                for dx in range(-3, 4):
                    for dz in range(-3, 4):
                        roof = top+4+(1 if abs(dx)<2 and abs(dz)<2 else 0)
                        for y in range(top+1, roof+1):
                            if abs(dx)>=2 or y>=top+4:
                                put(x+dx, y, z+dz, rng.choice(['stone','andesite','mossy_cobblestone']))
                            else:
                                put(x+dx, y, z+dz, 'air')
                put(x+1, top+1, z+1, 'chest', {'facing':'north','type':'single','waterlogged':'false'},
                    {'id': (8, 'minecraft:chest'), 'LootTable': (8, 'raft_ocean:chests/story_side_supplies')})

        # Use clear, step-sized terrain to connect side sites and a coastal landing.
        surfaces = {}
        for (x, y, z), (block, _, _) in blocks.items():
            if block in GROUND and y >= 13 and y > surfaces.get((x, z), -1):
                surfaces[x, z] = y
        def empty(x, y, z):
            return blocks.get((x,y,z), ('minecraft:air',{},None))[0] in ('minecraft:air','minecraft:short_grass','minecraft:fern')
        nodes = {p:y for p,y in surfaces.items() if empty(p[0],y+1,p[1]) and empty(p[0],y+2,p[1])
                 and (not core(*p) or all(blocks.get((p[0],y+dy,p[1]),('minecraft:air',{},None))[0]=='minecraft:air' for dy in (1,2)))}
        # Use the largest walkable component rather than an isolated tile in
        # a preserved locked building or beneath a raised landmark.
        unseen = set(nodes)
        components = []
        while unseen:
            first = min(unseen)
            component = {first}
            pending = deque([first])
            unseen.remove(first)
            while pending:
                bx,bz = pending.popleft()
                for p in [(bx+1,bz),(bx-1,bz),(bx,bz+1),(bx,bz-1)]:
                    if p in unseen and abs(nodes[p]-nodes[bx,bz])<=1:
                        unseen.remove(p)
                        component.add(p)
                        pending.append(p)
            components.append(component)
        connected = max(components,key=len)
        start = min(connected, key=lambda p:(p[0]-c)**2+(p[1]-c)**2)
        previous = {start: None}
        queue = deque([start])
        while queue:
            x, z = queue.popleft()
            for p in [(x+1,z),(x-1,z),(x,z+1),(x,z-1)]:
                if p in nodes and p not in previous and abs(nodes[p]-nodes[x,z]) <= 1:
                    previous[p] = (x,z)
                    queue.append(p)
        goals = []
        for site in sites:
            ex, ey, ez = site['entry']
            goal = min(previous, key=lambda p:(p[0]-ex)**2+(p[1]-ez)**2)
            assert (goal[0]-ex)**2+(goal[1]-ez)**2 <= 4, (name, site['kind'], 'Side route unreachable')
            goals.append(goal)
        coastal = [p for p in previous if nodes[p] <= 15 and not core(*p)]
        assert coastal, (name, 'No reachable landing')
        goals.append(max(coastal, key=lambda p:(p[0]-c)**2+(p[1]-c)**2))
        trail = set()
        for goal in goals:
            p = goal
            while p is not None:
                trail.add((p[0],nodes[p],p[1]))
                p = previous[p]
        for x,y,z in sorted(trail):
            if not core(x,z):
                put(x,y,z,'gravel' if blocks[x,y,z][0] in ('minecraft:stone','minecraft:andesite') else 'dirt_path')
                put(x,y+1,z,'air')
                put(x,y+2,z,'air')
        reserved = {(x+dx,z+dz) for x,y,z in trail for dx in range(-2,3) for dz in range(-2,3)}
        reserved.update((bx,bz) for site in sites for bx in range(site['pos'][0]-6,site['pos'][0]+7) for bz in range(site['pos'][2]-6,site['pos'][2]+7))
        trees = []
        for (x,z), top in sorted(tops.items()):
            if core(x,z) or (x,z) in reserved or top <= 15 or top >= 25 or not empty(x,top+1,z):
                continue
            if rng.random() < .08 and 4 <= x < size-4 and 4 <= z < size-4 and all((x-tx)**2+(z-tz)**2>36 for tx,tz in trees):
                species = 'birch' if rng.random()<.3 else 'oak'
                length = rng.randint(4,6)
                cells = [(x,ly,z) for ly in range(top+1,top+length+1)]
                cells += [(x+dx,ly,z+dz) for ly in range(top+length-1,top+length+3) for dx in range(-2,3) for dz in range(-2,3)]
                if not all(empty(*p) and not core(p[0],p[2]) and (p[0],p[2]) not in reserved for p in cells):
                    continue
                for ly in range(top+1,top+length+1):
                    put(x,ly,z,species+'_log',{'axis':'y'})
                for ly, radius in [(top+length-1,2),(top+length,2),(top+length+1,1),(top+length+2,0)]:
                    for dx in range(-radius,radius+1):
                        for dz in range(-radius,radius+1):
                            if abs(dx)+abs(dz)<=radius+1 and not(dx==dz==0 and ly<=top+length):
                                put(x+dx,ly,z+dz,species+'_leaves',{'persistent':'true','distance':'1','waterlogged':'false'})
                trees.append((x,z))
            elif rng.random()<.18:
                put(x,top+1,z,rng.choice(['fern','short_grass','azure_bluet','oxeye_daisy']))
        reef = dict(blocks)
        add_shallows(name+'_expanded',size,reef)
        for pos,state in reef.items():
            if not core(pos[0],pos[2]):
                blocks[pos] = state
        for pos,state in original.items():
            if state[0]!='minecraft:water':
                assert blocks[pos] == state, (name,pos,'Authored core changed')
        for x,y,z in trail:
            assert empty(x,y+1,z) and empty(x,y+2,z), (name,x,y,z,'Trail blocked')
        for site in sites:
            x,y,z=site['entry']
            assert all(blocks.get((x,y+dy,z),('minecraft:air',{},None))[0]=='minecraft:air' for dy in (1,2)), (name,site['kind'],'Entrance obstructed')
        root['size'] = (9,(3,[size,height,size]))
        root['RaftOceanExplorationVersion'] = (3,2)
        root['RaftOceanRoutesVersion'] = (3,1)
        main_route = landing_route(blocks, trail, c)
        root['RaftOceanRoute'] = (9,(3,[v for p in main_route for v in p]))
        template_io.replace_blocks(root,blocks)
        temporary = path.with_suffix('.expanded.nbt')
        template_io.write(temporary,root)
        temporary.replace(path)
        routes[name] = main_route
        report[name] = {'size':size,'height':height,'coreSize':old_size,'coreShift':shift,'shape':shapes[name]['label'],
                        'sites':sites,'trails':[list(p) for p in sorted(trail)],'trees':len(trees),'preservedCoreBlocks':sum(state[0]!='minecraft:water' for state in original.values())}
        report_path.write_text(json.dumps(report,indent=2)+'\n')
        route_path.write_text(json.dumps(routes,indent=2)+'\n')
        print(f'Expanded {name}: {old_size} -> {size}, four reachable side sites, {len(trail)} trail cells, {len(trees)} new trees.')
    report_path.write_text(json.dumps(report,indent=2)+'\n')
    route_path.write_text(json.dumps(routes,indent=2)+'\n')
    loot = {'type':'minecraft:chest','pools':[{'rolls':{'type':'minecraft:uniform','min':3,'max':5},
        'entries':[{'type':'minecraft:item','name':item,'weight':weight,'modifier':[{'type':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':count}}]}
        for item,weight,count in [('raft_ocean:scrap',6,4),('minecraft:bread',5,3),('minecraft:string',4,3),
            ('minecraft:coal',4,4),('minecraft:carrot',3,3),('minecraft:wheat_seeds',3,4),('minecraft:copper_ingot',2,2)]]}]}
    (ROOT/'loot_table/chests/story_side_supplies.json').write_text(json.dumps(loot,indent=2)+'\n')


if __name__ == '__main__':
    expand_templates()
