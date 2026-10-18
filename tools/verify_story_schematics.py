"""Validate packaged schematic ordering, guaranteed loot, UI labels and safe starter gates."""
import json
import re
import sys
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
version=next(s.split('=',1)[1] for s in (ROOT/'gradle.properties').read_text().splitlines() if s.startswith('mod_version='))
artifact=Path(sys.argv[1]) if len(sys.argv)>1 else ROOT/f'build/libs/raft_ocean-{version}.jar'
with zipfile.ZipFile(artifact) as package:
    def read(name):return json.loads(package.read(name))
    plans=read('data/raft_ocean/story_schematics.json')
    assert [p['id'] for p in plans]==['signal_array','tide_drive','bioenergy','waterworks','power_storage','deepwater','reef_armaments']
    catalog=(ROOT/'src/main/java/dev/raftocean/progression/ResearchCatalog.java').read_text(encoding='utf-8')
    projects=set(re.findall(r'new Project\("([a-z_]+)"',catalog))
    gated=[project for plan in plans for project in plan['projects']]
    assert len(gated)==len(set(gated))==8 and set(gated)<=projects
    assert not set(gated)&{'electrical','navigation','smelter','grill','purifier','fasteners','hook','bait','metal'}
    labels={locale:read(f'assets/raft_ocean/lang/{locale}.json') for locale in ('en_us','de_de')}
    for plan in plans:
        name='schematic_'+plan['id']
        loot=read('data/raft_ocean/loot_table/chests/'+plan['loot']+'.json')
        guaranteed=[pool for pool in loot['pools'] if pool.get('rolls')==1 and len(pool['entries'])==1 and pool['entries'][0].get('name')=='raft_ocean:'+name]
        assert len(guaranteed)==1,plan['id']
        assert not any(path.startswith('data/raft_ocean/recipe/') and path.endswith('/'+name+'.json') for path in package.namelist())
        item=read('assets/raft_ocean/items/'+name+'.json')
        assert item['model']['model']=='raft_ocean:item/'+name
        model=read('assets/raft_ocean/models/item/'+name+'.json')
        assert model['parent']=='raft_ocean:item/radio_log'
        for locale,lang in labels.items():
            assert lang.get('item.raft_ocean.'+name) and lang.get('raft_ocean.island.'+plan['island']),(locale,name)
            for key in ('discovered','help','locked','requirement'):assert lang.get('raft_ocean.schematic.'+key)
    assert 'raft-movement-'+version.split('.',1)[1] in (ROOT/'src/main/java/dev/raftocean/network/RaftMovementSync.java').read_text(encoding='utf-8')
print('Story schematics verified: 7 stable plans, 8 gated projects, guaranteed loot, original models and bilingual guidance; starter projects ungated.')
