"""Verify research ownership and recipe rewards in source or a packaged jar."""
import ast
import json
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
tree = ast.parse((ROOT / 'tools/generate_research_catalog.py').read_text(encoding='utf-8'))
projects = next(ast.literal_eval(node.value) for node in tree.body
                if isinstance(node, ast.Assign) and any(isinstance(t, ast.Name) and t.id == 'projects' for t in node.targets))
package = zipfile.ZipFile(sys.argv[1]) if len(sys.argv) > 1 else None
base = ROOT / 'src/main/resources'

def read(path):
    return json.loads(package.read(path)) if package else json.loads((base / path).read_text(encoding='utf-8'))

paths = package.namelist() if package else [p.relative_to(base).as_posix() for p in base.rglob('*.json')]
recipes = {}
for path in paths:
    if path.startswith('data/raft_ocean/recipe/') and path.endswith('.json'):
        value = read(path)
        if value.get('type') in ('minecraft:crafting_shaped', 'minecraft:crafting_shapeless'):
            recipes['raft_ocean:' + Path(path).stem] = value['result']['id']

items = set(read('assets/raft_ocean/model_coverage.json')['items'])
ownership = {}
for project, _, outputs, _, _, _ in projects:
    expected = {'raft_ocean:' + name for name in outputs.split()}
    actual = set(read(f'data/raft_ocean/tags/item/research/unlocks/{project}.json')['values'])
    assert actual == expected, ('Incorrect research ownership', project, actual ^ expected)
    for output in actual:
        assert output not in ownership, ('Multiple research gates', output, ownership.get(output), project)
        assert output.split(':', 1)[1] in items, ('Unregistered unlock', output)
        ownership[output] = project
    advancement = read(f'data/raft_ocean/advancement/research/{project}.json')
    rewards = set(advancement['rewards']['recipes'])
    assert rewards == {recipe for recipe, output in recipes.items() if output in actual}, ('Incomplete discovery rewards', project)
    assert advancement['criteria']['learned']['trigger'] == 'minecraft:impossible', ('Automatic research award', project)
    for locale in ('en_us', 'de_de'):
        labels = read(f'assets/raft_ocean/lang/{locale}.json')
        assert labels.get('raft_ocean.research.topic.' + project), (locale, project)
        assert labels.get('raft_ocean.research.description.' + project), (locale, project)

assert ownership.get('raft_ocean:reef_laminated_bow') == 'armaments'
assert ownership.get('raft_ocean:tidal_battery') == 'electrical'
assert ownership.get('raft_ocean:tidal_diving_hood') == 'diving'
for starter in ('timber_spear', 'driftwood_bow', 'reed_arrow', 'scrap_arrow', 'crumb_bait'):
    assert 'raft_ocean:' + starter not in ownership, ('Starter equipment accidentally gated', starter)
if package:
    package.close()
print(f'Research assets verified: {len(projects)} projects, {len(ownership)} unique unlocks; complete discovery rewards and starter access.')
