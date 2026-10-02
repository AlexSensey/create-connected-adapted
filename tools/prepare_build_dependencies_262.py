"""Stage the diagnosed 26.2 dependencies for Gradle without changing Prism or diagnostics.

Optionally provide a real 26.2 Simulated JAR with --simulated-jar. No dependency
is downloaded, no source is excluded, and no stub classes are generated.
"""
import argparse
import hashlib
import io
import json
import os
import re
from pathlib import Path
import tomllib
import zipfile

ROOT = Path(__file__).resolve().parents[1]
CREATE_SHA256 = 'c49f26a6fc3cfa29746ba41d9506b4c38888f6081ee240de086ecd082f79c77a'
CREATE_VERSION = '6.0.11-adapted-1.20'
NESTED = {
    'catnip': 'META-INF/jarjar/catnip-neoforge-1.0.100046-adapted-fork-early+mc26.2.jar',
    'flywheel': 'META-INF/jarjar/create_flywheel-neoforge-26.2-1.0.6.jar',
    'ponder': 'META-INF/jarjar/net.createmod.ponder.ponder-neoforge-1.0.100046-adapted-fork-early+mc26.2.jar',
    'registrate': 'META-INF/jarjar/Registrate-MC1.21-1.3.0+67.jar',
}


def require(condition, message):
    if not condition:
        raise SystemExit(message)


def read_metadata(data):
    with zipfile.ZipFile(io.BytesIO(data)) as archive:
        return tomllib.loads(archive.read('META-INF/neoforge.mods.toml').decode('utf-8'))


def prepare(simulated, rei=None, architectury=None):
    snapshot = ROOT / 'build/diagnostics-core-26.2/classpath.json'
    require(snapshot.is_file(), 'Missing cached diagnostic classpath. Prepare the exact 26.2 snapshot first.')
    paths = [Path(p) for p in json.loads(snapshot.read_text(encoding='utf-8'))]
    creates = [p for p in paths if 'create-adapted-26.2-1.20-' in p.name]
    # Runtime diagnostics may have moved to a newer compatible Create release.
    # Keep the existing verified compile baseline rather than repinning it.
    if not creates:
        previous = ROOT / 'build/dependencies-26.2/manifest.json'
        if previous.is_file():
            creates = [ROOT / e['path'] for e in json.loads(previous.read_text())['entries']
                       if e['role'] == 'create' and e['sha256'] == CREATE_SHA256]
    require(len(creates) == 1, 'Expected exactly one diagnosed Create 1.20 JAR.')
    create = creates[0].read_bytes()
    require(hashlib.sha256(create).hexdigest() == CREATE_SHA256, 'Create binary differs from the diagnosed baseline.')
    meta = read_metadata(create)
    require(any(m['modId'] == 'create' and m['version'] == CREATE_VERSION for m in meta['mods']), 'Unexpected Create identity/version.')
    create_name = creates[0].name[creates[0].name.index('create-adapted-'):]
    pending = [('create', create_name, create, True)]
    with zipfile.ZipFile(io.BytesIO(create)) as archive:
        for role, name in NESTED.items():
            pending.append((role, Path(name).name, archive.read(name), False))
    for role, pattern in [('jei', 'jei-26.2-neoforge-'), ('annotations', 'annotations-26.1.0.jar')]:
        candidates = [p for p in paths if pattern in p.name]
        require(len(candidates) == 1, f'Expected exactly one cached {role} dependency.')
        name = re.search(r'jei-26\.2-neoforge-[^/]+\.jar$', candidates[0].name)[0] if role == 'jei' else 'annotations-26.1.0.jar'
        pending.append((role, name, candidates[0].read_bytes(), False))
    mods = Path(os.environ['APPDATA']) / 'PrismLauncher/instances/26.2/minecraft/mods'
    for role, modid, pattern, supplied in [('rei', 'roughlyenoughitems', 'RoughlyEnoughItems*.jar', rei),
                                          ('architectury', 'architectury', 'architectury*.jar', architectury)]:
        candidates = [supplied] if supplied else list(mods.glob(pattern))
        require(len(candidates) == 1, f'Supply exactly one 26.2 {role} API JAR.')
        data = candidates[0].read_bytes()
        meta = read_metadata(data)
        require(any(m['modId'] == modid for m in meta['mods']), f'Unexpected {role} API identity.')
        pending.append((role, candidates[0].name, data, False))
    if simulated:
        data = simulated.read_bytes()
        meta = read_metadata(data)
        require(any(m['modId'] == 'simulated' for m in meta['mods']), 'The supplied JAR is not Simulated.')
        minecraft = [d for d in meta.get('dependencies', {}).get('simulated', []) if d['modId'] == 'minecraft']
        require(any(d.get('versionRange') in ('[26.2]', '[26.2,26.3)') for d in minecraft),
                'Simulated must explicitly target Minecraft [26.2] or [26.2,26.3); legacy JARs are not accepted.')
        with zipfile.ZipFile(io.BytesIO(data)) as archive:
            for name in ('index/SimBlocks', 'content/blocks/throttle_lever/ThrottleLeverBlock',
                         'content/blocks/throttle_lever/ThrottleLeverBlockEntity'):
                code = archive.read(f'dev/simulated_team/simulated/{name}.class')
                require(code[:4] == b'\xca\xfe\xba\xbe' and int.from_bytes(code[6:8]) <= 69,
                        f'Unsupported class file: {name}')
        pending.append(('simulated', simulated.name, data, False))
    # All inputs are validated before writing the new manifest.
    destination = ROOT / 'build/dependencies-26.2'
    destination.mkdir(parents=True, exist_ok=True)
    entries = []
    for role, name, data, runtime in pending:
        target = destination / f'{role}-{name}'
        if not target.is_file() or target.read_bytes() != data:
            target.write_bytes(data)
        entries.append({'role': role, 'path': target.relative_to(ROOT).as_posix(),
                        'sha256': hashlib.sha256(data).hexdigest(), 'runtime': runtime})
    manifest = {'schema': 1, 'minecraft': '26.2', 'neoforge': '26.2.0.88',
                'create_version': CREATE_VERSION, 'entries': entries,
                'missing_bridge_dependencies': [] if simulated else ['simulated']}
    (destination / 'manifest.json').write_text(json.dumps(manifest, indent=2) + '\n', encoding='utf-8')
    print(f'Staged {len(entries)} dependencies from the unchanged cached snapshot.')
    if not simulated:
        print('Simulated 26.2 is missing: core dependencies are ready; the optional bridge cannot compile yet.')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--simulated-jar', type=Path)
    parser.add_argument('--rei-jar', type=Path)
    parser.add_argument('--architectury-jar', type=Path)
    args = parser.parse_args()
    prepare(args.simulated_jar, args.rei_jar, args.architectury_jar)
