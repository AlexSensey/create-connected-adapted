"""Migrate Connected's shipped data, preserving criteria and loot semantics."""
import json
from pathlib import Path
from fix_263_loot_tables import convert, migrate

root = Path(__file__).resolve().parents[1] / 'src'
changed = 0
for path in root.rglob('*.json'):
    raw = path.read_bytes()
    if 'loot_table' in path.parts:
        new = convert(raw)
    elif 'advancement' in path.parts:
        data = json.loads(raw)
        for criterion in data.get('criteria', {}).values():
            conditions = criterion.get('conditions', {})
            if criterion.get('trigger') == 'minecraft:recipe_unlocked' and 'recipe' in conditions:
                conditions['recipes'] = conditions.pop('recipe')
            if criterion.get('trigger') == 'minecraft:placed_block' and isinstance(conditions.get('location'), list):
                predicates = migrate(conditions['location'])
                conditions['location'] = predicates[0] if len(predicates) == 1 else {'type': 'minecraft:all_of', 'terms': predicates}
        new = (json.dumps(data, ensure_ascii=False, indent=2) + '\n').encode()
    else:
        continue
    if raw != new:
        path.write_bytes(new)
        changed += 1
print('Migrated resources:', changed)
