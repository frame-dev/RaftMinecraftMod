"""Explicit release whitelist: no saves, private configuration or runtime logs."""
import argparse
import datetime
import subprocess
import hashlib
import json
import shutil
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
VERSION=next(s.split('=',1)[1] for s in (ROOT/'gradle.properties').read_text().splitlines() if s.startswith('mod_version='))
NAME='raft_ocean-'+VERSION

def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()

def package(release_date):
    target=ROOT/'build/release-staging'/NAME
    target.mkdir(parents=True,exist_ok=True)
    docs=('README.md','CHANGELOG.md','INSTALLATION.md','KNOWN_ISSUES.md','LICENSE_NOTICE.txt',
          'TEMPLATE_LICENSE.txt','RELEASE_VERIFICATION.md')
    for name in ('README.md','INSTALLATION.md','KNOWN_ISSUES.md','RELEASE_VERIFICATION.md'):
        assert VERSION in (ROOT/name).read_text().splitlines()[0],f'Stale release guide version: {name}'
    allowed=set(docs)|{NAME+'.jar','development','docs','examples','SHA256SUMS.txt','release.json'}
    assert all(p.name in allowed for p in target.iterdir()),'Unexpected release-directory files'
    artifacts=ROOT/'build/visual-art-verification/libs'
    with zipfile.ZipFile(artifacts/(NAME+'.jar')) as built:
        assert f'version="{VERSION}"' in built.read('META-INF/neoforge.mods.toml').decode(), 'Stale mod metadata'
    for name in (NAME+'.jar',NAME+'-sources.jar'):
        source=artifacts/name
        assert source.is_file(),f'Missing verified artifact: {source}'
        shutil.copy2(source,ROOT/'build/libs'/name)
        destination=target/name if name==NAME+'.jar' else target/'development'/name
        destination.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(source,destination)
        assert sha(source)==sha(destination)==sha(ROOT/'build/libs'/name)
    for name in docs:shutil.copy2(ROOT/name,target/name)
    for path in (ROOT/'docs').rglob('*'):
        if path.is_file() and path.suffix.lower() in ('.md','.txt','.png','.csv','.toml'):
            destination=target/path.relative_to(ROOT)
            destination.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(path,destination)
    example=ROOT/'examples/raft-addon'
    for path in example.rglob('*'):
        relative=path.relative_to(example)
        if path.is_file() and (relative.parts[0]=='src' or relative.as_posix() in
                ('README.md','LICENSE','build.gradle','settings.gradle','gradle.properties')):
            destination=target/path.relative_to(ROOT)
            destination.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(path,destination)
    files=[p for p in sorted(target.rglob('*')) if p.is_file() and p.name not in ('release.json','SHA256SUMS.txt')]
    manifest={'version':VERSION,'stage':'preview','date':release_date.isoformat(),'sourceCommit':subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT,text=True).strip(),'minecraft':'26.3',
        'neoforge':'26.3.0.48-beta','java':25,'api':3,'story':'open-ended',
        'installableJar':NAME+'.jar','sourcesJar':'development/'+NAME+'-sources.jar',
        'files':{p.relative_to(target).as_posix():sha(p) for p in files}}
    (target/'release.json').write_text(json.dumps(manifest,indent=2)+'\n')
    files=[p for p in sorted(target.rglob('*')) if p.is_file() and p.name!='SHA256SUMS.txt']
    (target/'SHA256SUMS.txt').write_text(''.join(f'{sha(p)}  {p.relative_to(target).as_posix()}\n' for p in files))
    archive=ROOT/'releases'/(NAME+'.zip')
    archive.parent.mkdir(parents=True,exist_ok=True)
    with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED,compresslevel=9) as output:
        for p in sorted(target.rglob('*')):
            if p.is_file():output.write(p,p.relative_to(target.parent).as_posix())
    with zipfile.ZipFile(archive) as output:
        assert output.testzip() is None
        assert output.read(NAME+'/'+NAME+'.jar')==(artifacts/(NAME+'.jar')).read_bytes()
        assert not any('/run/' in n or '/.gradle/' in n or n.endswith('server.properties') for n in output.namelist())
        for row in (target/'SHA256SUMS.txt').read_text().splitlines():
            expected,path=row.split('  ',1)
            assert hashlib.sha256(output.read(NAME+'/'+path)).hexdigest()==expected,path
    archive.with_suffix('.zip.sha256').write_text(f'{sha(archive)}  {archive.name}\n')
    print(f'Packaged and verified {archive}: {len(files)+1} files, {archive.stat().st_size} bytes.')

if __name__=='__main__':
    parser=argparse.ArgumentParser()
    parser.add_argument('--date',type=datetime.date.fromisoformat,required=True,help='Explicit publication date in the user timezone (YYYY-MM-DD)')
    package(parser.parse_args().date)
