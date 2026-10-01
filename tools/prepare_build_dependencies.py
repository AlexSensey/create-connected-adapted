"""Stage version-matched compile APIs for a Connected worktree, with hashes."""
from pathlib import Path
import hashlib, json, os, shutil, tomllib, zipfile
ROOT = Path(__file__).resolve().parents[1]
if ROOT.name == 'Create': raise SystemExit('Run the copy in a Connected worktree tools directory.')
props = dict(line.split('=', 1) for line in (ROOT/'gradle.properties').read_text().splitlines() if '=' in line and not line.startswith('#'))
mc, neo = props['minecraft_version'].strip(), props['neoforge_version'].strip()
prism = Path(os.environ['APPDATA'])/'PrismLauncher'
mods = prism/'instances'/mc/'minecraft/mods'
cross = ROOT.parent/'build/cross-version-compat'/mc/'deps'
destination = ROOT/'build'/('dependencies-' + mc)
destination.mkdir(parents=True, exist_ok=True)
entries = []
def stage(role, name, data, runtime=False):
    file = destination/(role+'-'+name)
    if not file.exists() or file.read_bytes() != data: file.write_bytes(data)
    entries.append(dict(role=role, path=file.relative_to(ROOT).as_posix(), sha256=hashlib.sha256(data).hexdigest(), runtime=runtime))
create = next(mods.glob('create-adapted-*.jar'))
stage('create', create.name, create.read_bytes(), True)
with zipfile.ZipFile(create) as jar:
    create_version = tomllib.loads(jar.read('META-INF/neoforge.mods.toml').decode())['mods'][0]['version']
    for role, match in [('catnip','catnip-'), ('flywheel','flywheel'), ('ponder','ponder-'), ('registrate','Registrate-')]:
        candidates = [n for n in jar.namelist() if n.startswith('META-INF/jarjar/') and n.endswith('.jar') and match in Path(n).name]
        assert len(candidates) == 1, (role,candidates)
        stage(role, Path(candidates[0]).name, jar.read(candidates[0]))
for role, pattern in [('jei','jei-*.jar'), ('rei','RoughlyEnoughItems*.jar'), ('architectury','architectury*.jar')]:
    candidates = list(mods.glob(pattern)) or list(cross.glob(pattern))
    if role == 'jei' and mc == '26.3':
        latest = ROOT.parent/'build/neoforge-263-config-fix/deps/jei-26.3-neoforge-31.8.0.53.jar'
        if latest.is_file(): candidates = [latest]
    assert len(candidates) == 1, (role,candidates)
    stage(role, candidates[0].name, candidates[0].read_bytes())
annotation = next((Path.home()/'.gradle/caches/modules-2/files-2.1/org.jetbrains/annotations/26.1.0').rglob('*.jar'))
stage('annotations', annotation.name, annotation.read_bytes())
(destination/'manifest.json').write_text(json.dumps(dict(schema=1, minecraft=mc, neoforge=neo, create_version=create_version, entries=entries, missing_bridge_dependencies=['simulated']), indent=2)+'\n')
print('Staged', len(entries), 'verified APIs for', mc)
