"""Run focused migration regressions against the current Prism 26.2 dependency snapshot."""
from pathlib import Path
import json
import os
import subprocess
import sys

root = Path(__file__).resolve().parents[1]
subprocess.run([sys.executable, str(root / 'tools/check_262.py'), '--prepare-only'], check=True)
classpath = json.loads((root / 'build/diagnostics-core-26.2/classpath.json').read_text(encoding='utf-8'))
work = root / 'build/migration-checks'
work.mkdir(parents=True, exist_ok=True)
jdk = Path('C:/Java/jdk-25.0.2/bin')

def run(tool, args, name):
    argfile = work / f'{name}.args'
    argfile.write_text('\n'.join('"' + arg.replace('\\', '/') + '"' for arg in args), encoding='utf-8')
    result = subprocess.run([str(jdk / f'{tool}.exe'), '@' + str(argfile)], cwd=work, capture_output=True)
    (work / f'{name}.log').write_bytes(result.stdout + result.stderr)
    print((result.stdout + result.stderr).decode('utf-8', errors='replace')[-3000:])
    if result.returncode:
        raise SystemExit(result.returncode)

sources = [
    'src/main/java/com/hlysine/create_connected/ConnectedDirections.java',
    'src/main/java/com/hlysine/create_connected/content/fluidvessel/SingleTankTransfer.java',
    'tools/DirectionCompatibilityCheck.java',
    'tools/FluidTransactionCheck.java',
]
run('javac', ['-proc:none', '-encoding', 'UTF-8', '-sourcepath', str(work / 'empty'),
              '-cp', os.pathsep.join(classpath), '-d', str(work), *[str(root / s) for s in sources]], 'compile')
for name in ('DirectionCompatibilityCheck', 'FluidTransactionCheck'):
    run('java', ['-cp', os.pathsep.join([str(work), *classpath]), name], name)
