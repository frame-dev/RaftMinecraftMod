"""Original material-driven research projects with stable IDs."""
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources'
materials=[('scrap','raft_ocean:scrap','Scrap','Schrott'),('iron','minecraft:iron_ingot','Iron','Eisen'),
 ('soil','raft_ocean:planting_mix','Planting mix','Pflanzmischung'),('mesh','raft_ocean:woven_mesh','Woven mesh','Gewebtes Netz'),
 ('canvas','raft_ocean:canvas_roll','Canvas','Segeltuch'),('wire','raft_ocean:copper_wire','Copper wire','Kupferdraht'),
 ('fiber','raft_ocean:plant_fiber','Plant fiber','Pflanzenfaser'),('wood','#minecraft:planks','Planks','Bretter'),
 ('stone','minecraft:cobblestone','Stone','Stein'),('clay','minecraft:clay_ball','Clay','Ton'),
 ('brick','raft_ocean:fired_brick','Dry or fired brick','Trockener oder gebrannter Ziegel'),('copper','minecraft:copper_ingot','Copper','Kupfer'),
 ('glass','minecraft:glass','Glass','Glas'),('fuel','#minecraft:coals','Coal','Kohle'),
 ('rope','raft_ocean:cordage','Cordage','Tauwerk'),('fasteners','raft_ocean:scrap_fasteners','Fasteners','Befestigungen'),
 ('plate','raft_ocean:metal_plate','Metal plate','Metallplatte'),('kelp','minecraft:kelp','Kelp','Seetang'),
 ('leather','minecraft:leather','Leather','Leder'),('paste','raft_ocean:binding_paste','Binding paste','Bindepaste')]
projects=[('workshop','wood rope scrap','raft_collection_net','Collection nets','Sammelnetze',0),
 ('metal','iron scrap','metal_plate reinforced_brace copper_wire','Metal fabrication','Metallverarbeitung',1),
 ('farming','soil wood rope','raft_tree_planter','Tree nursery','Baumschule',2),
 ('textiles','mesh fiber','canvas_roll','Canvas weaving','Segeltuch weben',3),
 ('navigation','canvas wood rope','raft_sail','Sailing rig','Segelanlage',4),
 ('electrical','wire plate glass','radio_receiver tidal_battery survey_receiver survey_antenna salvage_compactor raft_juicer','Radio receiver','Funkempfänger',5),
 ('fasteners','scrap stone','scrap_fasteners metal_hook_head','Metal fittings','Metallbeschläge',0),
 ('grill','scrap stone','raft_grill','Cooking grill','Kochgrill',0),
 ('smelter','brick clay','raft_smelter','Coastal smelter','Küstenschmelzer',0),
 ('anchor','iron rope plate','raft_anchor','Anchor winch','Ankerwinde',1),
 ('hook','iron rope fasteners','reinforced_hook','Reinforced hook','Verstärkter Haken',1),
 ('pot','clay plate soil','raft_cooking_pot','Cooking kettle','Kochkessel',2),
 ('wheel','wood rope fasteners','raft_steering_wheel','Steering assembly','Steueranlage',4),
 ('engine','plate copper fasteners','raft_engine','Marine engine','Schiffsmotor',4),
 ('antenna','wire iron','antenna_upgrade long_range_receiver','Signal array','Signalanlage',5),
 ('purifier','brick glass clay','raft_purifier','Distillation vessel','Destillationsgefäß',-1),
 ('rain','glass wood mesh','raft_rain_collector','Rain gathering','Regen sammeln',-1),
 ('bed','canvas wood rope','raft_bed','Canvas cot','Segeltuchbett',-1),
 ('storage','wood fasteners','raft_chest raft_barrel','Sealed storage','Geschütztes Lager',-1),
 ('planter','soil wood','raft_planter','Crop bed','Pflanzbeet',-1),
 ('bait','kelp fiber','baited_fishing_rod','Bait preparation','Köder herstellen',-1),
 ('shelter','canvas wood paste','raft_roof raft_curved_wall raft_corner_wall','Weatherproof shelter','Wetterfester Schutz',-1),
 ('tools','iron wood leather','raft_smithing_table raft_grindstone raft_stonecutter','Tool workshop','Werkzeugwerkstatt',-1),
 ('mapping','wood glass fiber','raft_cartography_table raft_loom raft_bookshelf','Navigation workshop','Navigationswerkstatt',-1),
 ('bioenergy','fuel plate glass','biofuel raft_biorefiner raft_biofuel_generator raft_fuel_pipe raft_fuel_tank','Biofuel systems','Biokraftstoffsysteme',-1),
 ('waterworks','wood plate glass','raft_water_pipe raft_water_tank raft_sprinkler raft_electric_purifier','Water distribution','Wasserversorgung',-1),
 ('power','copper wire glass plate','deepcycle_battery raft_power_cable raft_battery_charger','Electrical storage','Elektrische Speicher',-1),
 ('diving','leather kelp rope','tidal_diving_hood reef_flippers shoreleaf_salve','Diving equipment','Tauchausrüstung',-1),
 ('armaments','iron leather rope','metal_spear reef_machete reef_laminated_bow stormsteel_bow steel_arrow broadhead_arrow','Reinforced weapons','Verstärkte Waffen',-1),
 ('provisions','wood kelp fiber','reef_stew orchard_compote seafarer_supper','Travel provisions','Reiseproviant',-1)]

# Append only: persisted project bits must retain their original meanings.
assert len(projects)<=30 and len(materials)<=30, 'Expand attachment storage before adding more bitmask entries'
assert len({p[0] for p in projects})==len(projects)
outputs=[output for project in projects for output in project[2].split()]
assert len(outputs)==len(set(outputs)), 'A recipe output must belong to exactly one project'
def write(path,value):
 p=RES/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
lines=['package dev.raftocean.progression;','/** Generated catalog. Keep IDs stable. */','public final class ResearchCatalog {',
 'private ResearchCatalog() {}','public record Material(String id,String item) {}','public record Project(String id,int materials,int legacy) {}',
 'public static final Material[] MATERIALS = {']
for id,item,en,de in materials:
 lines.append(f'new Material("{id}","{item}"),')
 write(f'data/raft_ocean/tags/item/research/materials/{id}.json',{'replace':False,'values':[item,'raft_ocean:dried_brick'] if id=='brick' else [item]})
lines+=['};','public static final Project[] PROJECTS = {']
recipes=[]
for path in (RES/'data/raft_ocean/recipe').glob('*.json'):
 value=json.loads(path.read_text());result=value.get('result',{})
 if value.get('type','').startswith('minecraft:crafting_') and isinstance(result,dict):recipes.append((path.stem,result.get('id')))
for id,required,outputs,en,de,legacy in projects:
 mask=sum(1<<next(i for i,m in enumerate(materials) if m[0]==r) for r in required.split())
 lines.append(f'new Project("{id}",{mask},{legacy}),')
 values=['raft_ocean:'+v for v in outputs.split()]
 write(f'data/raft_ocean/tags/item/research/unlocks/{id}.json',{'replace':False,'values':values})
 write(f'data/raft_ocean/advancement/research/{id}.json',{'display':{'icon':{'id':values[0]},
 'title':{'translate':'raft_ocean.research.topic.'+id},'description':{'translate':'raft_ocean.research.description.'+id},
 'frame':'task','show_toast':True,'announce_to_chat':False,**({'background':'minecraft:block/oak_planks'} if id=='workshop' else {})},
 **({'parent':'raft_ocean:research/workshop'} if id!='workshop' else {}),
 'criteria':{'learned':{'trigger':'minecraft:impossible'}},'requirements':[['learned']],
 'rewards':{'recipes':['raft_ocean:'+name for name,out in recipes if out in values]}})
lines+=['};','}']
(ROOT/'src/main/java/dev/raftocean/progression/ResearchCatalog.java').write_text('\n'.join(lines)+'\n',encoding='utf-8')
for locale in ('en_us','de_de'):
 lang=json.loads((RES/f'assets/raft_ocean/lang/{locale}.json').read_text(encoding='utf-8'));english=locale=='en_us'
 for id,item,en,de in materials:lang['raft_ocean.research.material.'+id]=en if english else de
 for id,required,outputs,en,de,legacy in projects:
  lang['raft_ocean.research.topic.'+id]=en if english else de
  lang['raft_ocean.research.description.'+id]=('Materials: ' if english else 'Materialien: ')+', '.join(next(m[2 if english else 3] for m in materials if m[0]==r) for r in required.split())
 lang.update({'raft_ocean.research.study':'Research' if english else 'Erforschen','raft_ocean.research.learn':'Learn' if english else 'Lernen',
 'raft_ocean.research.materials':'Materials' if english else 'Materialien','raft_ocean.research.recipes':'Recipes' if english else 'Rezepte',
 'raft_ocean.research.help':'Study full batches once; learn recipes for free.' if english else 'Materialpakete einmal prüfen; Rezepte kostenlos lernen.',
 'raft_ocean.research.sample':'Needs study' if english else 'Erforschen','raft_ocean.research.input':'Sample' if english else 'Material'})
 write(f'assets/raft_ocean/lang/{locale}.json',lang)
print(f'Generated {len(materials)} materials and {len(projects)} recipe projects.')

import runpy
runpy.run_path(str(ROOT/"tools/generate_mod_compatibility.py"))
