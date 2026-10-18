"""Shared optional material tags and modded ocean salvage; no hard mod dependencies."""
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1];R=ROOT/'src/main/resources'
def write(path,value):
 p=R/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
materials={
 'iron_ingots':('minecraft:iron_ingot','c:ingots/iron'),
 'copper_ingots':('minecraft:copper_ingot','c:ingots/copper'),
 'gold_ingots':('minecraft:gold_ingot','c:ingots/gold'),
 'iron_nuggets':('minecraft:iron_nugget','c:nuggets/iron'),
 'redstone':('minecraft:redstone','c:dusts/redstone'),
 'glass':('minecraft:glass','c:glass_blocks/colorless'),
 'string':('minecraft:string','c:strings'),
 'sticks':('minecraft:stick','c:rods/wooden'),
 'cobblestone':('minecraft:cobblestone','c:cobblestones/normal'),
 'leather':('minecraft:leather','c:leathers'),
 'iron_plates':('raft_ocean:metal_plate','c:plates/iron'),
 'copper_wires':('raft_ocean:copper_wire','c:wires/copper')}
replace={item:'#raft_ocean:compat/'+name for name,(item,tag) in materials.items()}
for name,(item,tag) in materials.items():write('data/raft_ocean/tags/item/compat/'+name+'.json',{'replace':False,'values':[item,{'id':'#'+tag,'required':False}]})
for name,item in [('plates/iron','raft_ocean:metal_plate'),('wires/copper','raft_ocean:copper_wire')]:
 write('data/c/tags/item/'+name+'.json',{'replace':False,'values':[item]})
for name in ['plates','wires']:
 write('data/c/tags/item/'+name+'.json',{'replace':False,'values':['#c:'+name+'/iron'] if name=='plates' else ['#c:'+name+'/copper']})
def convert(value):
 if isinstance(value,str):return replace.get(value,value)
 if isinstance(value,list):return [convert(v) for v in value]
 if isinstance(value,dict):return {k:convert(v) for k,v in value.items()}
 return value
changed=0
for p in (R/'data/raft_ocean/recipe').glob('*.json'):
 data=json.loads(p.read_text(encoding='utf-8'));old=json.dumps(data)
 if data['type'].startswith('minecraft:crafting_'):
  for key in ['key','ingredients']:
   if key in data:data[key]=convert(data[key])
 if json.dumps(data)!=old:changed+=1
 write(p.relative_to(R),data)
for p in (R/'data/raft_ocean/tags/item/research/materials').glob('*.json'):
 data=json.loads(p.read_text(encoding='utf-8'));data['values']=convert(data['values']);write(p.relative_to(R),data)
# Modpack makers may extend this tag; missing mod resources are explicitly optional.
write('data/raft_ocean/tags/item/compat/ocean_resources.json',{'replace':False,'values':[{'id':'#c:raw_materials/'+metal,'required':False} for metal in ['osmium','tin','lead','silver','nickel','zinc','aluminum','uranium']]})
for name in ['crate','barrel']:
 path='data/raft_ocean/loot_table/salvage/'+name+'.json';data=json.loads((R/path).read_text(encoding='utf-8'))
 data['pools']=[pool for pool in data['pools'] if pool.get('name')!='raft_ocean:mod_resources']
 data['pools'].append({'name':'raft_ocean:mod_resources','rolls':1,'condition':{'type':'minecraft:random_chance','chance':0.1},'entries':[{'type':'minecraft:tag','items':'#raft_ocean:compat/ocean_resources','expand':True,'weight':1}]})
 write(path,data)
for locale,label in [('en_us','Mod energy item support'),('de_de','Energiegegenstände anderer Mods')]:
 p=R/f'assets/raft_ocean/lang/{locale}.json';data=json.loads(p.read_text(encoding='utf-8'));data['raft_ocean.config.compatibility.energyItems']=label;write(p.relative_to(R),data)
print(f'Generated12shared material tags, optional mod-resource salvage and updated{changed}crafting recipes.')
