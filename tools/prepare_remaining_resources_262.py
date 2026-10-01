"""Finish the diagnosed native 26.2 data and pale-oak resource migration."""
from pathlib import Path
import argparse,json
from prepare_recipes_262 import conditions,convert

ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/generated/resources'

def converted(path,data):
 relative=path.relative_to(RES).as_posix()
 result=conditions(data)
 if '/recipe/' in relative:result=convert(result)
 if '/loot_table/' in relative:
  # These blocks have no BlockItem. Linked controls override getDrops to use
  # their base control; bridge output/wrapped geometry has no independent drop.
  if any(entry.get('name')=='minecraft:air' for pool in result.get('pools',[]) for entry in pool.get('entries',[])):
   result=dict(result,pools=[])
  if path.name.startswith('dye_depot_'):
   result=dict(result)
   condition={'type':'neoforge:mod_loaded','modid':'dye_depot'}
   result['neoforge:conditions']=list(result.get('neoforge:conditions',[]))
   if condition not in result['neoforge:conditions']:result['neoforge:conditions'].append(condition)
 return result

def main():
 parser=argparse.ArgumentParser();parser.add_argument('--check',action='store_true');args=parser.parse_args()
 pending=[]
 for path in sorted((RES/'data').rglob('*.json')):
  old=json.loads(path.read_text(encoding='utf-8'));new=converted(path,old)
  assert converted(path,new)==new,path
  if old!=new:pending.append((path,new))
 oak=RES/'assets/create_connected/blockstates/linked_oak_button.json'
 pale=oak.with_name('linked_pale_oak_button.json')
 expected=json.loads(oak.read_text().replace('minecraft:block/oak_button','minecraft:block/pale_oak_button'))
 if not pale.exists() or json.loads(pale.read_text())!=expected:pending.append((pale,expected))
 loot=RES/'data/create_connected/loot_table/blocks/linked_pale_oak_button.json'
 expected_loot={'type':'minecraft:block','pools':[],'random_sequence':'create_connected:blocks/linked_pale_oak_button'}
 if not loot.exists() or json.loads(loot.read_text())!=expected_loot:pending.append((loot,expected_loot))
 if not args.check:
  for path,data in pending:path.write_text(json.dumps(data,indent=2)+'\n',encoding='utf-8')
 print(len(pending),'resource files '+('need migration' if args.check else 'migrated'))
 if args.check and pending:raise SystemExit(1)

if __name__=='__main__':main()
