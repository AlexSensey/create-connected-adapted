"""Migrate catalyst input JSON to the installed 26.2 item/fluid holder codecs.
Preserve outputs, amounts, conditions and whether the held item is consumed.
"""
import argparse
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RECIPES = ROOT / 'src/generated/resources/data/create_connected/recipe'

def holder_input(value, kind):
    if isinstance(value, str):
        return value
    if isinstance(value, list):
        children = [holder_input(child, kind) for child in value]
    elif set(value) == {kind}:
        return value[kind]
    elif set(value) == {'tag'}:
        return '#' + value['tag']
    elif value.get('type') == 'neoforge:compound' and set(value) == {'type', 'ingredients'}:
        children = [holder_input(child, kind) for child in value['ingredients']]
    else:
        raise ValueError(f'Unsupported {kind} ingredient: {value!r}')
    flattened = [entry for child in children for entry in (child if isinstance(child, list) else [child])]
    if not flattened:
        raise ValueError('Empty catalyst ingredient')
    return flattened[0] if len(flattened) == 1 else flattened

def migrate(recipe):
    result = dict(recipe)
    ingredients = []
    for value in recipe['ingredients']:
        if isinstance(value, dict) and 'amount' in value:
            if 'ingredient' in value:
                ingredients.append(value)
                continue
            fluid = {key: entry for key, entry in value.items() if key != 'amount'}
            if fluid.get('type') in ('neoforge:single', 'neoforge:tag'):
                fluid.pop('type')
            ingredients.append({'ingredient': holder_input(fluid, 'fluid'), 'amount': value['amount']})
        else:
            ingredients.append(holder_input(value, 'item'))
    result['ingredients'] = ingredients
    return result

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--check', action='store_true', help='Fail if packaged source resources need migration')
    args = parser.parse_args()
    changed = 0
    count = 0
    for folder in ('item_application', 'filling'):
        for path in sorted((RECIPES / folder).glob('*.json')):
            recipe = json.loads(path.read_text())
            # All current recipes in these directories fill the empty catalyst.
            if 'create_connected:empty_fan_catalyst' not in json.dumps(recipe['ingredients']):
                continue
            converted = migrate(recipe)
            assert migrate(converted) == converted, path
            assert {k:v for k,v in converted.items() if k != 'ingredients'} == {
                k:v for k,v in recipe.items() if k != 'ingredients'}, path
            count += 1
            if converted != recipe:
                changed += 1
                if not args.check:
                    path.write_text(json.dumps(converted, indent=2) + '\n', encoding='utf-8')
    print(f'{count} catalyst recipes checked; {changed} ' + ('need migration' if args.check else 'migrated'))
    if args.check and changed:
        raise SystemExit(1)

if __name__ == '__main__':
    main()
