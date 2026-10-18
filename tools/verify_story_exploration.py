"""Check expanded native templates, connected routes and exact authored-core preservation."""
from collections import deque
import json
import argparse
from pathlib import Path
import sys
import tempfile
import zipfile
import template_io
from story_island_exploration import SIZES
from blend_island_coasts import GROUND, vegetation

root = Path(__file__).resolve().parents[1]
version = next(s.split('=',1)[1] for s in (root/'gradle.properties').read_text().splitlines() if s.startswith('mod_version='))
parser=argparse.ArgumentParser()
parser.add_argument('jar',nargs='?',type=Path,default=root/f'build/libs/raft_ocean-{version}.jar')
parser.add_argument('--baseline',type=Path,default=None)
arguments=parser.parse_args()
jar=arguments.jar
preserved = trails = sites = 0


def hull_area(points):
    def cross(a,b,c):
        return (b[0]-a[0])*(c[1]-a[1])-(b[1]-a[1])*(c[0]-a[0])
    points=sorted(points)
    lower=[]
    for p in points:
        while len(lower)>=2 and cross(lower[-2],lower[-1],p)<=0:lower.pop()
        lower.append(p)
    upper=[]
    for p in reversed(points):
        while len(upper)>=2 and cross(upper[-2],upper[-1],p)<=0:upper.pop()
        upper.append(p)
    hull=lower[:-1]+upper[:-1]
    return abs(sum(a[0]*b[1]-b[0]*a[1] for a,b in zip(hull,hull[1:]+hull[:1])))/2

with zipfile.ZipFile(jar) as current, tempfile.TemporaryDirectory() as folder:
    report = json.loads(current.read('data/raft_ocean/story_island_exploration.json'))
    shapes = json.loads(current.read('data/raft_ocean/story_island_shapes.json'))
    assert set(report)==set(SIZES)
    assert list(SIZES.values())==sorted(set(SIZES.values())), 'Story stages must strictly increase'
    languages={locale:json.loads(current.read(f'assets/raft_ocean/lang/{locale}.json')) for locale in ('en_us','de_de')}
    for name,size in SIZES.items():
        entry=f'data/raft_ocean/structure/islands/{name}.nbt'
        path=Path(folder)/'island.nbt'
        path.write_bytes(current.read(entry));after_root=template_io.read(path);after=template_io.blocks(after_root)
        if arguments.baseline:
            with zipfile.ZipFile(arguments.baseline) as old:path.write_bytes(old.read(entry))
            before_root=template_io.read(path)
        else:
            before_root=template_io.read(root/'tools/story_cores'/f'{name}.nbt')
        before=template_io.blocks(before_root)
        assert after_root['size'][1][1]==[size,48,size]
        shift=(size-before_root['size'][1][1][0])//2
        graded=after_root.get('RaftOceanCoastalBlendVersion',(3,0))[1]==1
        for (x,y,z),state in before.items():
            if state[0]=='minecraft:water':
                continue
            # The explicit coastal grading revision relocates natural terrain/flora.
            # Authored architecture, containers, puzzle blocks and path cells remain exact.
            if graded and state[2] is None and (state[0] in GROUND or vegetation(state[0])):
                continue
            actual=after.get((x+shift,y,z+shift))
            # Above the ocean surface, an omitted template cell remains air.
            # Underwater cave air still requires an explicit air placement.
            if actual is None and state==('minecraft:air',{},None) and y>=15:
                actual=state
            assert actual==state,(name,x,y,z,'Authored core changed')
            preserved+=1
        for pos in after:
            assert 0<=pos[0]<size and 0<=pos[1]<48 and 0<=pos[2]<size,(name,pos)
        island=report[name]
        route={tuple(p) for p in island['trails']}
        assert len(route)>=100 and island['trees']>=3,name
        assert island['shape']==shapes[name]['label']
        land={(x,z) for (x,y,z),state in after.items() if y>=14 and state[0] in ('minecraft:grass_block','minecraft:sand','minecraft:dirt','minecraft:gravel','minecraft:stone')}
        assert len(land)/hull_area(land)<.90,(name,'Coastline is too close to a convex oval')
        pending=deque([min(route)]);seen={min(route)}
        while pending:
            x,y,z=pending.popleft()
            for dx,dz in [(1,0),(-1,0),(0,1),(0,-1)]:
                for dy in [-1,0,1]:
                    p=x+dx,y+dy,z+dz
                    if p in route and p not in seen:seen.add(p);pending.append(p)
        assert seen==route,(name,'Disconnected trail graph')
        for x,y,z in route:
            assert after.get((x,y,z),('',{},None))[0] not in ('','minecraft:air','minecraft:water'),(name,x,y,z)
            for dy in [1,2]:assert after.get((x,y+dy,z),('minecraft:air',{},None))[0]=='minecraft:air',(name,x,y,z,dy)
            trails+=1
        assert len(island['sites'])==4
        for site in island['sites']:
            x,y,z=site['entry']
            for dy in (1,2):
                assert after.get((x,y+dy,z),('minecraft:air',{},None))[0]=='minecraft:air',(name,site['kind'],'Entrance obstructed')
            assert min((x-tx)**2+(z-tz)**2 for tx,ty,tz in route)<=4,(name,site['kind'])
            px,py,pz=site['pos']
            loot=[state for (bx,by,bz),state in after.items() if abs(bx-px)<=3 and by==py and abs(bz-pz)<=3 and state[2] is not None]
            assert any(state[2].get('LootTable')==(8,site.get('lootTable','raft_ocean:chests/story_side_supplies')) for state in loot),(name,site['kind'])
            key=site['note']
            table=json.loads(current.read('data/raft_ocean/loot_table/'+site['lootTable'].split(':')[1]+'.json'))
            book=table['pools'][1]['entries'][0]
            assert table['pools'][1]['rolls']==1 and book['name']=='minecraft:written_book'
            components=book['modifier'][0]['components']
            assert book['modifier'][0]['type']=='minecraft:set_components'
            pages=components['minecraft:written_book_content']['pages']
            assert pages==[{'raw':{'translate':key+'.page'}},{'raw':{'translate':key+'.context'}}]
            for locale,language in languages.items():
                for suffix in ('.title','.page','.context'):assert language.get(key+suffix),(locale,key+suffix)
            sites+=1
    loot=json.loads(current.read('data/raft_ocean/loot_table/chests/story_side_supplies.json'))
    assert loot['pools'] and all(e['name'] not in ('raft_ocean:relay_key','raft_ocean:starfall_log') for p in loot['pools'] for e in p['entries'])
print(f'Expanded story templates verified: {sites} reachable side sites, {trails} connected clear trail cells, {preserved} protected authored core blocks unchanged.')
