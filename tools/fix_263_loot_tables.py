"""Migrate legacy loot JSON in 26.3 sources and repackage existing release JARs.
Resource-only hotfix: compiled code and other ZIP entries remain byte-for-byte unchanged.
"""
from pathlib import Path
import json
import zipfile

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "build/loot-hotfix-26.3"

def migrate(value):
    if isinstance(value, list):
        return [migrate(v) for v in value]
    if not isinstance(value, dict):
        return value
    result = {k: migrate(v) for k, v in value.items()}
    # Legacy predicate/function type discriminators became the standard type key.
    if isinstance(result.get("condition"), str) and "type" not in result:
        result["type"] = result.pop("condition")
    if "function" in result:
        assert "type" not in result
        result["type"] = result.pop("function")
    if "conditions" in result:
        assert "condition" not in result
        conditions = result.pop("conditions")
        if conditions:
            result["condition"] = conditions[0] if len(conditions) == 1 else {"type": "minecraft:all_of", "terms": conditions}
    if "functions" in result and result.get("type") != "minecraft:sequence":
        assert "modifier" not in result
        result["modifier"] = result.pop("functions")
    if result.get("type") == "minecraft:block_state_property":
        result["type"] = "minecraft:match_block"
        result["blocks"] = result.pop("block")
        if "properties" in result:
            result["state"] = result.pop("properties")
    for key in ("rolls", "count"):
        if isinstance(result.get(key), float) and result[key].is_integer():
            result[key] = int(result[key])
    return result

def validate(value):
    if isinstance(value, list):
        for item in value:
            validate(item)
    elif isinstance(value, dict):
        assert "conditions" not in value and "function" not in value
        assert "functions" not in value or value.get("type") == "minecraft:sequence"
        assert value.get("type") != "minecraft:block_state_property"
        if value.get("type") == "minecraft:alternatives":
            assert all(child.get("condition") for child in value["children"][:-1]), "Unreachable entry"
        for item in value.values():
            validate(item)

def convert(raw):
    old = json.loads(raw)
    new = migrate(old)
    validate(new)
    assert migrate(new) == new
    return raw if old == new else (json.dumps(new, ensure_ascii=False, indent=2) + "\n").encode()

def patch_jar(source, target):
    target.parent.mkdir(parents=True, exist_ok=True)
    changed = 0
    with zipfile.ZipFile(source) as src, zipfile.ZipFile(target, "w", zipfile.ZIP_DEFLATED) as dst:
        for info in src.infolist():
            raw = src.read(info.filename)
            data = convert(raw) if "/loot_table/" in info.filename and info.filename.endswith(".json") else raw
            changed += data != raw
            dst.writestr(info, data)
    with zipfile.ZipFile(target) as z:
        assert z.testzip() is None
    print(target.name, "updated loot tables:", changed)

