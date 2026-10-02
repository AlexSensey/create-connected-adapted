"""Quick source/ASM audit against the existing diagnostic classpath; no game launch.
Names/descriptors/staticness and literal anchors only; not a Mixin execution test.
"""
import argparse
import json
from pathlib import Path
import re
import subprocess


def erased_type(value):
    result, depth = [], 0
    for char in value:
        if char == "<":
            depth += 1
        elif char == ">":
            depth -= 1
        elif depth == 0:
            result.append(char)
    return "".join(result).strip()


def type_descriptor(value, imports):
    value = erased_type(value)
    arrays = 0
    while value.endswith("[]"):
        arrays += 1
        value = value[:-2]
    primitives = dict(void="V", boolean="Z", byte="B", char="C", short="S", int="I", long="J", float="F", double="D")
    if value in primitives:
        descriptor = primitives[value]
    else:
        parts = value.split(".")
        outer = parts[0]
        qualified = imports.get(outer)
        if qualified is None and outer in ("String", "Object"):
            qualified = "java.lang." + outer
        if qualified is None and outer in ("Map", "List", "Vector", "Collection", "Optional", "Set"):
            qualified = "java.util." + outer
        if qualified is None:
            if value.startswith(("java.", "net.", "com.", "dev.")):
                qualified = value
                parts = [value]
            else:
                raise ValueError("Unresolved member type: " + value)
        internal = qualified.replace(".", "/") + "".join("$" + part for part in parts[1:])
        descriptor = "L" + internal + ";"
    return "[" * arrays + descriptor


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--jdk", default="C:/Java/jdk-25.0.2")
    parser.add_argument("--classpath", type=Path, help="Prepared classpath.json for a separate baseline")
    parser.add_argument("--output-dir", type=Path, help="Separate project-local audit output directory")
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    source_root = root / "src/main/java/com/hlysine/create_connected/mixin"
    config = json.loads((root / "src/main/resources/create_connected.mixins.json").read_text())
    out = args.output_dir.resolve() if args.output_dir else root / "build/api-check-26.2/mixin-audit-core-26.2"
    if not out.is_relative_to(root.resolve()):
        raise ValueError("Audit output must remain inside this project")
    out.mkdir(parents=True, exist_ok=True)
    requests, members = [], []
    for group in ("mixins", "client"):
        for name in config[group]:
            source = (source_root / (name.replace(".", "/") + ".java")).read_text()
            imports = {v.rsplit(".", 1)[-1]: v for v in re.findall(r"import ([\w.]+);", source)}
            annotation = re.search(r"@Mixin\((.*?)\)\s", source, re.S)
            if not annotation:
                raise ValueError(f"No supported @Mixin declaration: {name}")
            targets = [imports.get(v, v) for v in re.findall(r"(\w+)\.class", annotation[1])]
            targets += re.findall(r'"([\w.]+)"', annotation[1])
            for match in re.finditer(r"@(Inject|WrapOperation|ModifyExpressionValue|ModifyVariable|ModifyArg|Redirect)\s*\(", source):
                index, depth, quoted, escape = match.end(), 1, False, False
                while depth and index < len(source):
                    char = source[index]
                    if quoted:
                        if escape:
                            escape = False
                        elif char == chr(92):
                            escape = True
                        elif char == '"':
                            quoted = False
                    elif char == '"':
                        quoted = True
                    elif char == "(":
                        depth += 1
                    elif char == ")":
                        depth -= 1
                    index += 1
                text = source[match.start():index]
                handler = re.match(r"\s*(?:private|public|protected)\s+(static\s+)?[^\n(]+\(", source[index:])
                if not handler:
                    raise ValueError(f"Unsupported handler declaration: {name}")
                static = "1" if handler[1] else "0"
                anchors = re.findall(r'\btarget\s*=\s*"(L[^";]+;[^"\n]+)"', text)
                for selector in re.findall(r'\bmethod\s*=\s*"([^"]+)"', text):
                    for target in targets:
                        requests.append(chr(9).join((name, target, selector, "|".join(anchors), static)))
            for match in re.finditer(r'@(Shadow|Accessor|Invoker)(?:\("([^"]+)"\))?\s*(?:@(?:Final|Mutable)\s*)*([^;{]+)[;{]', source):
                kind, explicit, declaration = match.groups()
                static = "1" if re.search(r"\bstatic\b", declaration) else "0"
                is_method = "(" in declaration
                if is_method:
                    member = re.search(r"([\w$]+)\s*\(", declaration)[1]
                    if kind in ("Accessor", "Invoker"):
                        stripped = re.sub(r"^(?:get|set|call|invoke)", "", member)
                        inferred = stripped if len(stripped) > 1 and stripped[:2].isupper() else stripped[:1].lower() + stripped[1:]
                        member = explicit or inferred
                else:
                    member = re.search(r"([\w$]+)\s*$", declaration)[1]
                clean = re.sub(r"\b(public|private|protected|static|abstract|final)\s+", "", declaration).strip()
                if is_method:
                    header, parameters = clean.split("(", 1)
                    return_type = header.rsplit(None, 1)[0]
                    parameters = parameters.rsplit(")", 1)[0].strip()
                    parameter_types = [] if not parameters else [part.strip().rsplit(None, 1)[0] for part in parameters.split(",")]
                    if kind == "Accessor":
                        descriptor = type_descriptor(parameter_types[0] if return_type == "void" else return_type, imports)
                    else:
                        descriptor = "(" + "".join(type_descriptor(t, imports) for t in parameter_types) + ")"
                        descriptor += "V" if member == "<init>" else type_descriptor(return_type, imports)
                else:
                    descriptor = type_descriptor(clean.rsplit(None, 1)[0], imports)
                for target in targets:
                    members.append(chr(9).join((name, target, member, "field" if kind == "Accessor" or not is_method else "method", static, descriptor)))
    (out / "requests.tsv").write_text(chr(10).join(requests), encoding="utf-8")
    (out / "members.tsv").write_text(chr(10).join(members), encoding="utf-8")
    dependencies = json.loads((args.classpath or root / "build/diagnostics-core-26.2/classpath.json").read_text())
    # classpath.json was prepared previously against the installed instance.
    import os
    classpath = os.pathsep.join(dependencies)
    jdk = Path(args.jdk) / "bin"
    suffix = ".exe" if os.name == "nt" else ""
    classes = out / "classes"
    subprocess.run([str(jdk / ("javac" + suffix)), "-proc:none", "-cp", classpath, "-d", str(classes), str(root / "tools/MixinTargetAudit.java")], check=True)
    subprocess.run([str(jdk / ("java" + suffix)), "-cp", str(classes) + os.pathsep + classpath, "MixinTargetAudit", str(out / "requests.tsv"), str(out / "results.tsv"), str(out / "members.tsv")], check=True)
    print("Report:", out / "results.tsv")
    print("Limitations: source subset parser, names/descriptors/staticness and literal anchors; no local-capture, ordinal, remapping or game/mixin validation.")


if __name__ == "__main__":
    main()
