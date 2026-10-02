"""Update only runtime dependency metadata in the existing Connected build."""
from pathlib import Path
import hashlib
import tomllib
import zipfile

ROOT = Path(__file__).resolve().parents[1]
source = ROOT/'build/libs/create_connected-1.3.3-adapted-26.2-dev.jar'
target = ROOT/'build/release-minimum-create'/source.name
target.parent.mkdir(parents=True, exist_ok=True)
metadata = 'META-INF/neoforge.mods.toml'
with zipfile.ZipFile(source) as old, zipfile.ZipFile(target, 'w') as new:
    for info in old.infolist():
        data = old.read(info.filename)
        if info.filename == metadata:
            parsed = tomllib.loads(data.decode())
            dependency = next(d for d in parsed['dependencies']['create_connected'] if d['modId'] == 'create')
            assert dependency['versionRange'] == '[6.0.11-adapted-1.20]'
            assert data.count(b'[6.0.11-adapted-1.20]') == 1
            data = data.replace(b'[6.0.11-adapted-1.20]', b'[6.0.11-adapted-1.20,)')
        new.writestr(info, data)
with zipfile.ZipFile(source) as old, zipfile.ZipFile(target) as new:
    assert new.testzip() is None
    assert old.namelist() == new.namelist()
    assert {n for n in old.namelist() if old.read(n) != new.read(n)} == {metadata}
    parsed = tomllib.loads(new.read(metadata).decode())
    dependency = next(d for d in parsed['dependencies']['create_connected'] if d['modId'] == 'create')
    assert dependency['versionRange'] == '[6.0.11-adapted-1.20,)'
digest = hashlib.sha256(target.read_bytes()).hexdigest()
(target.parent/'SHA256SUMS.txt').write_text(digest+'  '+target.name+'\n')
print(target)
print('Verified: only Create dependency range changed; all classes and assets identical.')
