"""Original compacting press, bound salvage bale and data-driven feedstock tiers."""
import ast, json, math
from pathlib import Path
from model_surface_safety import sanitize
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';A=RES/'assets/raft_ocean'
def write(path,value):
 path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
# Reuse the established silhouette-fitting function without executing its asset generator.
tree=ast.parse((ROOT/'tools/generate_fine_models.py').read_text(encoding='utf-8'))
node=next(n for n in tree.body if isinstance(n,ast.FunctionDef) and n.name=='inventory_display')
exec(compile(ast.Module(body=[node],type_ignores=[]),'<inventory_display>','exec'))
def box(lo,hi,texture):
 return {'from':lo,'to':hi,'faces':{f:{'texture':'#'+texture,'uv':[0,0,16,16]} for f in ['up','down','north','south','east','west']}}
textures={name:f'raft_ocean:block/fine_{name}' for name in ['wood','metal','bronze','teal','navy','rubber','coral','leaves']};textures['particle']=textures['metal']
for active in (False,True):
 name='salvage_compactor'+('_active' if active else '')
 parts=[box([1,1,3],[15,10,15],'teal'),box([2,2,1],[14,8,3],'navy'),box([3,3,.75],[13,7,1],'rubber'),
        box([2,10,3],[3,14,15],'metal'),box([13,10,3],[14,14,15],'metal'),box([3,10,14],[13,14,15],'metal'),
        box([3,10,3],[13,11,4],'bronze'),box([4,10,5],[12,10.4,13],'rubber'),
        box([6,11 if active else 13,6],[10,12 if active else 14,12],'bronze'),box([7,12 if active else 14,8],[9,14,10],'metal'),
        box([14,7,1.5],[15,8,2.5],'coral' if active else 'leaves')]
 for x in (1,13):
  for z in (3,13):parts.append(box([x,0,z],[x+2,1,z+2],'rubber'))
 for x in (4,6,8,10,12):parts.append(box([x,3,.5],[x+.5,7,.75],'metal'))
 for x in (1.2,14.2):
  for y in (2,8.5):parts.append(box([x,y,2.75],[x+.6,y+.6,3],'bronze'))
 model={'parent':'minecraft:block/block','textures':textures,'elements':parts,'display':inventory_display(parts)}
 model,_,_=sanitize(model);write(A/f'models/block/{name}.json',model)
 write(A/f'blockstates/{name}.json',{'variants':{'':{'model':f'raft_ocean:block/{name}'}}})
 write(A/f'items/{name}.json',{'model':{'type':'minecraft:model','model':f'raft_ocean:block/{name}'}})
parts=[box([2,2,3],[14,5,13],'wood'),box([3,5,2],[13,8,14],'teal'),box([2,8,3],[14,12,13],'wood')]
for x in (4,10):
 parts.extend([box([x,1.8,2.8],[x+1,12.2,3],'bronze'),box([x,1.8,13],[x+1,12.2,13.2],'bronze'),box([x,12,3],[x+1,12.2,13],'bronze'),box([x,1.8,3],[x+1,2,13],'bronze')])
parts.extend([box([6,5,1.8],[9,7,2],'metal'),box([12,9,3],[13,11,3.2],'leaves')])
model={'parent':'minecraft:block/block','textures':textures,'elements':parts,'display':inventory_display(parts)};model,_,_=sanitize(model)
write(A/'models/item/salvage_bale.json',model);write(A/'items/salvage_bale.json',{'model':{'type':'minecraft:model','model':'raft_ocean:item/salvage_bale'}})
for tier,values in [('light',['raft_ocean:drift_leaf','minecraft:kelp','minecraft:dried_kelp']),('standard',['#minecraft:planks','raft_ocean:scrap']),('dense',['#raft_ocean:compat/iron_ingots','#raft_ocean:compat/copper_ingots'])]:
 write(RES/f'data/raft_ocean/tags/item/recycling/{tier}.json',{'replace':False,'values':values})
write(RES/'data/raft_ocean/recipe/salvage_compactor.json',{'type':'minecraft:crafting_shaped','category':'equipment','pattern':['PWP','FBF','PPP'],'key':{'P':'raft_ocean:metal_plate','W':'#raft_ocean:compat/copper_wires','F':'raft_ocean:scrap_fasteners','B':'raft_ocean:timber_brace'},'result':{'id':'raft_ocean:salvage_compactor'}})
p=A/'model_coverage.json';coverage=json.loads(p.read_text(encoding='utf-8'))
for group,names in [('items',['salvage_compactor','salvage_bale']),('blocks',['salvage_compactor','salvage_compactor_active']),('item_variants',['salvage_compactor_active'])]:coverage[group]=sorted(set(coverage[group]+names))
write(p,coverage)
print('Generated original compactor assets, exact registry additions, recipe and recycling tags.')
