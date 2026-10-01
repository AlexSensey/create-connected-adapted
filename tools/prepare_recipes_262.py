"""Migrate all checked-in Connected recipes to 26.2 ingredient/condition syntax.
Inputs change representation; results, counts, patterns and recipe conditions remain.
"""
import argparse
import json
from pathlib import Path
from prepare_catalyst_recipes_262 import holder_input, migrate

ROOT = Path(__file__).resolve().parents[1]
RECIPES = ROOT / 'src/generated/resources/data/create_connected/recipe'

def conditions(value):
    if isinstance(value, list):
        return [conditions(entry) for entry in value]
    if isinstance(value, dict):
        result = {key: conditions(entry) for key, entry in value.items()}
        if result.get('type') == 'neoforge:false':
            result['type'] = 'neoforge:never'
        elif result.get('type') == 'neoforge:true':
            result['type'] = 'neoforge:always'
        return result
    return value

def convert(recipe):
    result = dict(recipe)
    if 'ingredients' in result:
        result = migrate(result)
    if 'ingredient' in result:
        result['ingredient'] = holder_input(result['ingredient'], 'item')
    if 'key' in result:
        result['key'] = {key: holder_input(value, 'item') for key, value in result['key'].items()}
    if 'sequence' in result:
        result['sequence'] = [convert(entry) for entry in result['sequence']]
    if 'neoforge:conditions' in result:
        result['neoforge:conditions'] = conditions(result['neoforge:conditions'])
    return result

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    pending = []
    paths = sorted(RECIPES.rglob('*.json'))
    for path in paths:
        original = json.loads(path.read_text())
        converted = convert(original)
        assert convert(converted) == converted, path
        for key in ('type', 'result', 'results', 'pattern', 'loops', 'transitional_item', 'keep_held_item'):
            assert converted.get(key) == original.get(key), (path, key)
        if converted != original:
            pending.append((path, converted))
    if not args.check:
        for path, converted in pending:
            path.write_text(json.dumps(converted, indent=2) + '\n', encoding='utf-8')
    print(f'{len(paths)} recipes checked; {len(pending)} ' + ('need migration' if args.check else 'migrated'))
    if args.check and pending:
        raise SystemExit(1)

if __name__ == '__main__':
    main()
