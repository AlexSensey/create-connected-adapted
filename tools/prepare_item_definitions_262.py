"""Prepare missing 26.2 item definitions from the checked-in legacy item models.

Does not launch Minecraft or run datagen. Hand-authored special definitions are
preserved; battery level models are model variants, not registered items.
"""
import argparse
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = Path('assets/create_connected')
VARIANTS = {f'kinetic_battery_level_{level}' for level in range(6)}


def prepare(check):
    generated = ROOT / 'src/generated/resources' / ASSETS
    authored = ROOT / 'src/main/resources' / ASSETS
    pending = []
    count = 0
    for model in sorted((generated / 'models/item').glob('*.json')):
        if model.stem in VARIANTS:
            continue
        definition = generated / 'items' / model.name
        special = authored / 'items' / model.name
        source = json.loads(model.read_text(encoding='utf-8'))
        if special.is_file():
            json.loads(special.read_text(encoding='utf-8'))
            continue
        if 'overrides' in source:
            raise SystemExit(f'Legacy overrides need an explicit modern definition: {model.name}')
        expected = {'model': {'type': 'minecraft:model',
                              'model': f'create_connected:item/{model.stem}'}}
        count += 1
        if definition.is_file() and json.loads(definition.read_text(encoding='utf-8')) == expected:
            continue
        pending.append((definition, expected))
    if check and pending:
        raise SystemExit(f'{len(pending)} missing or mismatched item definitions. Run this tool without --check.')
    for definition, expected in pending:
        definition.parent.mkdir(parents=True, exist_ok=True)
        definition.write_text(json.dumps(expected, indent=2) + '\n', encoding='utf-8')
    print(f'{count} ordinary item definitions ready; {len(pending)} written. Special definitions and model variants preserved.')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true', help='Validate without changing files')
    prepare(parser.parse_args().check)
