"""Compile all Connected sources against the exact ABE 26.2 binaries.

This is an API migration diagnostic, not a distributable build: access
transformers and mixins still require a NeoForge launch after compilation.
No files in the Prism instance are modified.
"""
from pathlib import Path
import argparse
import hashlib
import json
import os
import re
import shutil
import subprocess
import zipfile
from collections import Counter

ROOT = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--instance', type=Path, default=Path(os.environ['APPDATA']) / 'PrismLauncher/instances/Adventures Beyond Eternity')
parser.add_argument('--jdk', type=Path, default=Path('C:/Java/jdk-25.0.2'))
parser.add_argument('--prepare-only', action='store_true', help='Prepare the exact dependency classpath without invoking javac')
args = parser.parse_args()
prism = args.instance.parent.parent
pack = json.loads((args.instance / 'mmc-pack.json').read_text())
versions = {c['uid']: c['version'] for c in pack['components']}
if versions['net.minecraft'] != '26.2':
    raise SystemExit('This diagnostic requires Minecraft 26.2')
neo = versions['net.neoforged']
mods = args.instance / 'minecraft/mods'
create_candidates = list(mods.glob('create-adapted-26.2-*.jar'))
if len(create_candidates) != 1:
    raise SystemExit(f'Expected one Create: Adapted jar, found {len(create_candidates)}')
create = create_candidates[0]
work = ROOT / 'build/diagnostics-26.2'
deps = work / 'dependencies'
deps.mkdir(parents=True, exist_ok=True)
nested = []
with zipfile.ZipFile(create) as archive:
    for name in archive.namelist():
        if name.startswith('META-INF/jarjar/') and name.endswith('.jar'):
            dest = deps / Path(name).name
            dest.write_bytes(archive.read(name))
            nested.append(dest)
libraries = prism / 'libraries'
mc = libraries / f'net/neoforged/minecraft-client-patched/{neo}/minecraft-client-patched-{neo}.jar'
nf = libraries / f'net/neoforged/neoforge/{neo}/neoforge-{neo}-universal.jar'
for path in (mc, nf, args.jdk / 'bin/javac.exe'):
    if not path.is_file():
        raise SystemExit(f'Missing dependency: {path}')
with zipfile.ZipFile(nf) as archive:
    for name in archive.namelist():
        if name.startswith('META-INF/jarjar/') and name.endswith('.jar'):
            dest = deps / Path(name).name
            dest.write_bytes(archive.read(name))
            nested.append(dest)
extra = []
for component, version in versions.items():
    metadata = prism / 'meta' / component / f'{version}.json'
    if not metadata.exists():
        continue
    for library in json.loads(metadata.read_text()).get('libraries', []):
        coordinate = library['name'].split(':')
        group, artifact, version, *classifier = coordinate
        suffix = '-' + classifier[0] if classifier else ''
        path = libraries / group.replace('.', '/') / artifact / version / f'{artifact}-{version}{suffix}.jar'
        if path.exists() and 'natives-' not in path.name:
            extra.append(path)
extra.extend(sorted(mods.glob('jei-*.jar')))
extra.extend(sorted((Path.home() / '.gradle/caches/modules-2/files-2.1/org.jetbrains/annotations/26.1.0').rglob('*.jar')))
classpath = [mc, nf, create, *nested, *extra]
# Keep javac's ZIP filesystem entirely inside the workspace. Some installed
# launcher libraries have restrictive ACLs even when their bytes are readable.
local_classpath = []
for index, source in enumerate(classpath):
    dest = deps / f'{index:03d}-{source.name}'
    if not dest.exists() or dest.stat().st_size != source.stat().st_size or dest.stat().st_mtime_ns != source.stat().st_mtime_ns:
        shutil.copy2(source, dest)
    local_classpath.append(dest)
sources = sorted((ROOT / 'src/main/java').rglob('*.java'))
classes = work / 'classes'
classes.mkdir(exist_ok=True)
options = ['-proc:none', '-implicit:none', '-encoding', 'UTF-8', '-Xmaxerrs', '10000',
           '-sourcepath', str(work / 'empty-sourcepath'), '-cp', os.pathsep.join(map(str, local_classpath)),
           '-d', str(classes), *map(str, sources)]
argfile = work / 'javac.args'
argfile.write_text('\n'.join('"' + a.replace('\\', '/') + '"' for a in options), encoding='utf-8')
(work / 'environment.json').write_text(json.dumps({
    'instance': str(args.instance), 'versions': versions, 'create': str(create),
    'create_sha256': hashlib.sha256(create.read_bytes()).hexdigest(),
    'source_count': len(sources), 'classpath': list(map(str, classpath)),
}, indent=2), encoding='utf-8')
(work / 'classpath.json').write_text(json.dumps(list(map(str, local_classpath)), indent=2), encoding='utf-8')
if args.prepare_only:
    print(f'Prepared Minecraft 26.2 / NeoForge {neo} dependencies in {deps}')
    raise SystemExit(0)
result = subprocess.run([str(args.jdk / 'bin/javac.exe'), '@' + str(argfile)], capture_output=True)
log = work / 'compile.log'
log.write_bytes(result.stdout + result.stderr)
diagnostics = result.stderr.decode('utf-8', errors='replace')
summary = {
    'javac_exit': result.returncode,
    'source_count': len(sources),
    'error_count': len(re.findall(r'error:', diagnostics)),
    'errors_by_message': dict(Counter(m.strip() for m in re.findall(r'error: (.+)', diagnostics)).most_common()),
    'missing_symbols': dict(Counter(re.findall(r'symbol:\s+([^\r\n]+)', diagnostics)).most_common()),
}
(work / 'summary.json').write_text(json.dumps(summary, indent=2), encoding='utf-8')
print(f'{len(sources)} sources; javac exit {result.returncode}; diagnostics: {log}')
print(diagnostics[-600:])
raise SystemExit(result.returncode)
