"""Focused bridge UI checks: production proxy and hit-test, modeled block entities.
No game launch. Uses the installed API's Direction and Vec3 classes.
"""
import json, os, re, subprocess
from pathlib import Path

root = Path(__file__).resolve().parents[1]
src = root / 'src/main/java/com/hlysine/create_connected/content/kineticbridge'
out = root / 'build/api-check-26.2/bridge-settings-test'
out.mkdir(parents=True, exist_ok=True)
proxy = (src / 'KineticBridgeDestinationSettings.java').read_text()
box = (src / 'KineticBridgeValueBox.java').read_text()
def nested(text):
    text = re.sub(r'(?m)^(?:package|import) .*;\n', '', text)
    return text.replace('public class ', 'static class ', 1)

java = '''
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
public class BridgeSettingsCheck {
 static int checks;
 static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 record ValueSettings(int row,int value){}
 static class CompoundTag { boolean written; }
 static class HolderLookup { static class Provider{} }
 static class BlockPos{} static class LevelAccessor{}
 static class BlockState { Direction facing; BlockState(Direction d){facing=d;} }
 static class ConnectedLang {static Object translateDirect(String s){return s;}}
 static class ScrollValueBehaviour {
  int value=40,changes;
  int getValue(){return value;} String formatValue(){return "value="+value;}
  void setValue(int v){value=Math.clamp(v,0,2048);changes++;}
 }
 static class StressImpactScrollValueBehaviour extends ScrollValueBehaviour {
  StressImpactScrollValueBehaviour(Object label,Object be,Object slot){}
  public boolean isActive(){return true;}
  public ValueSettings getValueSettings(){return new ValueSettings(0,value);}
  public void read(CompoundTag n,HolderLookup.Provider p,boolean c){value=999;}
  public void write(CompoundTag n,HolderLookup.Provider p,boolean c){n.written=true;}
  void setValueSettings(ValueSettings s){setValue(s.value());}
 }
 static class KineticBridgeBlockEntity {ScrollValueBehaviour stressMultiplier=new ScrollValueBehaviour();}
 static class KineticBridgeDestinationBlockEntity {
  KineticBridgeBlockEntity source;
  KineticBridgeBlockEntity getSource(){return source;}
 }
 static class KineticBatteryValueBox {
  Direction side;
  KineticBatteryValueBox(double offset){}
  Direction getSide(){return side;}
  boolean isSideActive(BlockState state,Direction d){return state.facing.getAxis()!=d.getAxis();}
  public boolean testHit(LevelAccessor l,BlockPos p,BlockState s,Vec3 hit){return false;}
 }
'''+nested(proxy)+nested(box)+'''
 public static void main(String[] args) {
  KineticBridgeDestinationBlockEntity dest=new KineticBridgeDestinationBlockEntity();
  KineticBridgeDestinationSettings setting=new KineticBridgeDestinationSettings(dest);
  check(!setting.isActive(),"missing source inactive");
  setting.setValue(80);check(setting.getValue()==40,"missing source safe");
  KineticBridgeBlockEntity source=new KineticBridgeBlockEntity();dest.source=source;
  check(setting.isActive(),"valid source active");
  check(setting.getValueSettings().value()==40,"initial UI value from source");
  setting.setValueSettings(new ValueSettings(0,80));
  check(source.stressMultiplier.getValue()==80,"destination edits source");
  check(source.stressMultiplier.changes==1,"source callback path used once");
  check(setting.value==0 || setting.value==40,"no independent value mutation");
  source.stressMultiplier.setValue(120);
  check(setting.getValueSettings().value()==120,"source edits visible on output");
  check(setting.formatValue().equals("value=120"),"output label uses source");
  setting.setValue(9000);check(source.stressMultiplier.getValue()==2048,"source limits preserved");
  CompoundTag n=new CompoundTag();setting.write(n,null,false);
  check(!n.written,"output does not persist a second value");
  setting.read(n,null,false);check(source.stressMultiplier.getValue()==2048,"output NBT cannot overwrite source");
  dest.source=null;setting.setValue(90);
  check(source.stressMultiplier.getValue()==2048,"unpaired output cannot edit old source");
  dest.source=new KineticBridgeBlockEntity();
  check(setting.getValueSettings().value()==40,"replacement source used");
  KineticBridgeValueBox box=new KineticBridgeValueBox();
  for(Direction facing:Direction.values()) for(Direction side:Direction.values()) {
   box.side=side;BlockState state=new BlockState(facing);
   boolean allowed=facing.getAxis()!=side.getAxis();
   for(double a:new double[]{0,0.02,0.5,0.98,1}) for(double b:new double[]{0,0.5,1}) {
    double plane=side.getAxisDirection()==Direction.AxisDirection.POSITIVE?1:0;
    Vec3 hit=switch(side.getAxis()) {
     case X -> new Vec3(plane,a,b);case Y -> new Vec3(a,plane,b);case Z -> new Vec3(a,b,plane);
    };
    check(box.testHit(null,null,state,hit)==allowed,"face hit "+facing+"/"+side+"/"+hit);
   }
   check(!box.testHit(null,null,state,new Vec3(1.1,0.5,0.5)),"outside block rejected");
  }
  System.out.println("PASS: "+checks+" bridge settings checks (modeled entities, native vectors/directions)");
 }
}
'''
path = out / 'BridgeSettingsCheck.java'
path.write_text(java)
cp = os.pathsep.join(json.loads((root / 'build/diagnostics-core-26.2/classpath.json').read_text(encoding='utf-8-sig')))
java_home = Path(os.environ.get('JAVA_HOME', 'C:/Java/jdk-25.0.2'))
for command in ([str(java_home/'bin/javac.exe'), '-cp', cp, '-d', str(out), str(path)],
                [str(java_home/'bin/java.exe'), '-cp', str(out)+os.pathsep+cp, 'BridgeSettingsCheck']):
    result = subprocess.run(command, capture_output=True, text=True)
    if result.returncode:
        print((result.stdout+result.stderr)[-2500:]);raise SystemExit(result.returncode)
    print(result.stdout.strip())
