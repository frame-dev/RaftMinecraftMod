"""Original four-pad blender, serving glasses and native data-pack juice recipes."""
import ast,json,math
from pathlib import Path
from model_surface_safety import sanitize
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';A=RES/'assets/raft_ocean'
def write(path,value):
 path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
tree=ast.parse((ROOT/'tools/generate_fine_models.py').read_text(encoding='utf-8'));node=next(n for n in tree.body if isinstance(n,ast.FunctionDef) and n.name=='inventory_display')
exec(compile(ast.Module(body=[node],type_ignores=[]),'<inventory_display>','exec'))
def box(lo,hi,texture):return {'from':lo,'to':hi,'faces':{f:{'texture':'#'+texture,'uv':[0,0,16,16]} for f in ['up','down','north','south','east','west']}}
textures={name:f'raft_ocean:block/fine_{name}' for name in ['wood','metal','bronze','teal','navy','rubber','coral','leaves','ceramic','linen']};textures['particle']=textures['metal']
def model(path,parts,inventory=True):
 value={'parent':'minecraft:block/block','textures':textures,'elements':parts,'display':inventory_display(parts)};value,_,_=sanitize(value);
 if not inventory:value.pop('display')
 write(A/path,value)
for state in ('','_active','_ready'):
 name='raft_juicer'+state
 parts=[box([1,7,1],[15,8,15],'wood'),box([9,8,4],[15,12,14],'teal'),box([10,12,5],[14,13,13],'bronze'),
        box([10,13,6],[10.5,22,12],'metal'),box([13.5,13,6],[14,22,12],'metal'),box([10.5,13,11.5],[13.5,22,12],'navy'),
        box([10,22,6],[14,23,12],'navy'),box([11,23,8],[13,24,10],'bronze'),box([13.9,16,8],[15,17,9],'bronze'),
        box([14.5,17,8],[15,20,9],'bronze'),box([13.9,20,8],[15,21,9],'bronze'),box([11.7,13,8.7],[12.3,17,9.3],'metal'),
        box([10.7,13,6.5],[13.3,13.5,11.5],'rubber'),box([11,9,3.75],[13,10,4],'coral' if state=='_active' else 'leaves')]
 for x in (1,13):
  for z in (1,13):parts.append(box([x,0,z],[x+2,7,z+2],'navy'))
 for x in (2,5.5):
  for z in (2,10):parts.extend([box([x,8,z],[x+2.5,8.25,z+3],'linen'),box([x+.25,8.25,z+.25],[x+2.25,8.45,z+2.75],'bronze')])
 if state:
  parts.append(box([10.6,13.5,6.6],[13.4,20 if state=='_ready' else 17,11.4],'coral' if state=='_ready' else 'teal'))
 blade=box([10.7,17,8.8],[13.3,17.3,9.2],'metal')
 if state=='_active':blade['rotation']={'origin':[12,17,9],'axis':'y','angle':45}
 parts.append(blade)
 # The raft renderer stretches a unit model to the collision envelope. Inventory
 # keeps the authored one-cell-wide, 1.5-cell-tall silhouette independently.
 if not state:model(Path('models/item/raft_juicer.json'),parts)
 import copy
 normalized=copy.deepcopy(parts)
 for element in normalized:
  for key in ('from','to'):
   v=element[key];element[key]=[(v[0]-1)*16/14,v[1]*16/24,(v[2]-1)*16/14]
  if 'rotation' in element:
   v=element['rotation']['origin'];element['rotation']['origin']=[(v[0]-1)*16/14,v[1]*16/24,(v[2]-1)*16/14]
 model(Path(f'models/block/{name}.json'),normalized,bool(state));write(A/f'blockstates/{name}.json',{'variants':{'':{'model':f'raft_ocean:block/{name}'}}});write(A/f'items/{name}.json',{'model':{'type':'minecraft:model','model':f'raft_ocean:block/{name}' if state else 'raft_ocean:item/raft_juicer'}})
for name,liquid in [('drinking_glass',None),('melon_breeze','coral'),('orchard_cooler','bronze'),('shoreberry_tonic','leaves')]:
 parts=[box([4,1,4],[12,2,12],'metal'),box([4,2,4],[4.5,13,12],'ceramic'),box([11.5,2,4],[12,13,12],'ceramic'),box([4.5,2,4],[11.5,13,4.5],'ceramic'),box([4.5,2,11.5],[11.5,13,12],'ceramic')]
 for lo,hi in [([3.8,13,3.8],[12.2,13.4,4.5]),([3.8,13,11.5],[12.2,13.4,12.2]),([3.8,13,4.5],[4.5,13.4,11.5]),([11.5,13,4.5],[12.2,13.4,11.5])]:parts.append(box(lo,hi,'teal'))
 if liquid:parts.extend([box([4.5,2,4.5],[11.5,11.5,11.5],liquid),box([9,11.5,9],[9.5,17,9.5],'linen'),box([3.9,7,4.5],[4.5,10,7],liquid)])
 model(Path(f'models/item/{name}.json'),parts);write(A/f'items/{name}.json',{'model':{'type':'minecraft:model','model':f'raft_ocean:item/{name}'}})
recipes=[('melon_breeze',['melon_slice']*3+['apple']),('orchard_cooler',['apple','apple','melon_slice','melon_slice']),('shoreberry_tonic',['sweet_berries','sweet_berries','carrot','beetroot'])]
for name,foods in recipes:write(RES/f'data/raft_ocean/recipe/juicing/{name}.json',{'type':'raft_ocean:juicing','ingredients':['minecraft:'+f for f in foods],'result':{'id':'raft_ocean:'+name},'duration':1800})
write(RES/'data/raft_ocean/tags/item/juicer_ingredients.json',{'replace':False,'values':['minecraft:'+x for x in sorted(set(f for _,foods in recipes for f in foods))]})
write(RES/'data/raft_ocean/recipe/raft_juicer.json',{'type':'minecraft:crafting_shaped','category':'equipment','pattern':['GWG','PFP','BBB'],'key':{'G':'#raft_ocean:compat/glass','W':'#raft_ocean:compat/copper_wires','P':'raft_ocean:metal_plate','F':'raft_ocean:scrap_fasteners','B':'raft_ocean:timber_brace'},'result':{'id':'raft_ocean:raft_juicer'}})
write(RES/'data/raft_ocean/recipe/drinking_glass.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':['G G','GGG'],'key':{'G':'#raft_ocean:compat/glass'},'result':{'id':'raft_ocean:drinking_glass','count':4}})
p=A/'model_coverage.json';c=json.loads(p.read_text(encoding='utf-8'))
for group,names in [('items',['raft_juicer','drinking_glass','melon_breeze','orchard_cooler','shoreberry_tonic']),('blocks',['raft_juicer','raft_juicer_active','raft_juicer_ready']),('item_variants',['raft_juicer_active','raft_juicer_ready'])]:c[group]=sorted(set(c[group]+names))
write(p,c)
print('Generated original juicer models, drinks, native recipes and exact registry coverage.')
