"""Short kinetic regressions using production method bodies and modeled network/world collaborators.
No Minecraft launch; the native 26.2 NBT round-trip uses the actual dependency jars.
"""
from pathlib import Path
import json,os,re,subprocess
ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/'src/main/java/com/hlysine/create_connected'
OUT=ROOT/'build/api-check-26.2/kinetic-logic-test'
OUT.mkdir(parents=True,exist_ok=True)
def method(path,name):
 s=(SRC/path).read_text();m=re.search(r'(?m)^    (?:public|private|protected)[^\n]*\b'+name+r'\([^\n]*\)\s*\{',s)
 if not m: raise ValueError((path,name))
 start=m.start();opening=s.index('{',m.start());depth=1;i=opening+1
 while depth:
  depth+=(s[i]=='{')-(s[i]=='}');i+=1
 return s[start:i]
def be(folder): return f'content/{folder}/{folder[0].upper()+folder[1:]}BlockEntity.java'
# Class names in upstream use several internal capitals.
cent='content/centrifugalclutch/CentrifugalClutchBlockEntity.java'
over='content/overstressclutch/OverstressClutchBlockEntity.java'
bat='content/kineticbattery/KineticBatteryBlock.java'
java=r'''
import java.util.*;
import java.lang.ref.WeakReference;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
public class KineticLogicCheck {
 static int checks;
 static void check(boolean ok,String msg){checks++;if(!ok)throw new AssertionError(msg);}
 record Key<T>(String name){}
 static final Key<Boolean> UNCOUPLED=new Key<>("uncoupled"),POWERED=new Key<>("powered");
 static final Key<Integer> POWER=new Key<>("power"),LEVEL=new Key<>("level");
 static final Key<Direction> FACING=new Key<>("facing");
 static final Key<ClutchState> STATE=new Key<>("state");
 enum ClutchState{COUPLED,UNCOUPLING,UNCOUPLED}
 enum TickPriority{EXTREMELY_HIGH}
 static class BlockState{
  Map<Key<?>,Object> values=new HashMap<>();
  Object block;
  Object getBlock(){return block;}boolean is(Object b){return block==b;}
  <T> Optional<T> getOptionalValue(Key<T> k){return Optional.ofNullable(getValue(k));}
  @SuppressWarnings("unchecked") <T>T getValue(Key<T> k){return (T)values.get(k);}
  <T> BlockState set(Key<T> k,T v){values.put(k,v);return this;}
  BlockState cycle(Key<Boolean> k){return set(k,!getValue(k));}
 }
 record BlockPos(int x){BlockPos relative(Direction d){return new BlockPos(x+switch(d){case NORTH->-1;case SOUTH->1;case EAST->2;case WEST->-2;case UP->3;case DOWN->-3;});}}
 interface BlockGetter{BlockState getBlockState(BlockPos p);}
 static class BlockEntity{}
 static class Level implements BlockGetter{
  Map<BlockPos,BlockState> states=new HashMap<>();Map<BlockPos,BlockEntity> entities=new HashMap<>();
  public BlockState getBlockState(BlockPos p){return states.getOrDefault(p,new BlockState());}
  BlockEntity getBlockEntity(BlockPos p){return entities.get(p);}
  boolean client;int notifications,changes,scheduled;
  boolean isClientSide(){return client;}
  Object getChunkAt(BlockPos p){return null;}
  void markAndNotifyBlock(BlockPos p,Object chunk,BlockState old,BlockState state,int flags,int limit){notifications++;}
  void setBlockAndUpdate(BlockPos p,BlockState s){changes++;}
  void scheduleTick(BlockPos p,Object block,int delay,TickPriority priority){scheduled++;}
 }
 interface KineticNetworkAccessor{float getUnloadedStress();}
 static class KineticNetwork{Map<KineticBlockEntity,Float> sources=new HashMap<>();float unloadedStress;float getActualCapacityOf(KineticBlockEntity b){return sources.get(b);}}
 static class Network extends KineticNetwork implements KineticNetworkAccessor{int removes;public float getUnloadedStress(){return unloadedStress;}void remove(KineticBlockEntity b){removes++;}}
 static class KineticBlockEntity extends BlockEntity{
  Level level;BlockState state=new BlockState();Network network=new Network();
  boolean removed,hasNetwork=true,updateSpeed,overstressed,hasSource=true;int detach,removeSource;
  float speed,stressImpact=3;Direction sourceFacing=Direction.NORTH;
  BlockPos pos=new BlockPos(0);
  Level getLevel(){return level;}BlockState getBlockState(){return state;}BlockPos getBlockPos(){return pos;}
  boolean isRemoved(){return removed;}boolean hasNetwork(){return hasNetwork;}Network getOrCreateNetwork(){return network;}
  void detachKinetics(){detach++;}void removeSource(){removeSource++;}boolean hasSource(){return hasSource;}
  Direction getSourceFacing(){return sourceFacing;}float getSpeed(){return speed;}boolean isOverStressed(){return overstressed;}
  float calculateStressApplied(){return stressImpact;}
 }
 static class GeneratingKineticBlockEntity extends KineticBlockEntity{boolean reActivateSource;int generatedUpdates;void initialize(){}void updateGeneratedRotation(){generatedUpdates++;}}
 static class KineticBridgeBlock{static final Key<Direction> FACING=KineticLogicCheck.FACING;}
 static class KineticBridgeDestinationBlock{
  BRIDGE_POS
  BRIDGE_VALID
 }
 static class KineticBridgeBlockEntity extends KineticBlockEntity{}
 static class BridgeDestination extends GeneratingKineticBlockEntity{
  java.lang.ref.WeakReference<KineticBridgeBlockEntity> sourceBE=new java.lang.ref.WeakReference<>(null);
  BRIDGE_SOURCE
  BRIDGE_INIT
 }
 static class KineticHelper{HELPER}
 static class Value{int value;int getValue(){return value;}}
 static class Mth{static float abs(float v){return Math.abs(v);}static int abs(int v){return Math.abs(v);}}
 static class Entry{Object get(){return this;}}
 static class CCBlocks{static Entry CENTRIFUGAL_CLUTCH=new Entry();}
 static class Centrifugal extends KineticBlockEntity{
  Value speedThreshold=new Value();boolean reattachNextTick;
  CENT_UPDATE
  CENT_MOD
 }
 static class Overstress extends KineticBlockEntity{OVER_MOD}
 static class Config{ValueFloat brakeActiveStress=new ValueFloat();}
 static class ValueFloat{float value=200;float getF(){return value;}}
 static class CCConfigs{static Config cfg=new Config();static Config server(){return cfg;}}
 static class Brake extends KineticBlockEntity{float lastStressApplied;BRAKE_STRESS}
 static class Battery extends KineticBlockEntity{
  float stress,consumedStress=-1;int syncs;
  void updateMinStress(){}void sendDataImmediately(){syncs++;}
  BAT_CONSUMPTION
  BAT_DISCHARGE
  BAT_COMPLETE
  BAT_MOD
 }
 public static void main(String[] args){
  for(Direction facing:Direction.values()){
   BridgeDestination dest=new BridgeDestination();dest.level=new Level();dest.state.block=new KineticBridgeDestinationBlock();dest.state.set(FACING,facing);
   BlockPos sourcePos=KineticBridgeDestinationBlock.getSource(dest.pos,dest.state);
   KineticBridgeBlockEntity source=new KineticBridgeBlockEntity();source.level=dest.level;source.pos=sourcePos;source.state.block=new KineticBridgeBlock();source.state.set(FACING,facing);
   dest.level.states.put(sourcePos,source.state);dest.level.entities.put(sourcePos,source);
   check(dest.getSource()==source,"bridge aligned source "+facing);
   check(dest.getSource()==source,"bridge cached source "+facing);
   source.state.set(FACING,facing.getOpposite());check(dest.getSource()==null,"bridge rejects backwards input "+facing);
   check(dest.sourceBE.get()==null,"bridge clears invalid cache");
   source.state.set(FACING,facing);dest.level.entities.clear();check(dest.getSource()==null,"bridge missing entity");
  }
  BridgeDestination dest=new BridgeDestination();dest.level=new Level();dest.initialize();check(dest.generatedUpdates==1,"bridge refreshes generation at initialization");
  dest.level.client=true;dest.initialize();check(dest.generatedUpdates==1,"bridge cannot initialize client network");
  for(int mode=0;mode<3;mode++){
   KineticBlockEntity b=new KineticBlockEntity();b.level=mode==0?null:new Level();
   if(mode==1)b.level.client=true;if(mode==2)b.removed=true;
   KineticHelper.updateKineticBlock(b);check(b.detach==0&&b.network.removes==0,"do not mutate null/client/removed networks");
  }
  for(boolean generator:new boolean[]{false,true}){
   KineticBlockEntity b=generator?new GeneratingKineticBlockEntity():new KineticBlockEntity();b.level=new Level();
   KineticHelper.updateKineticBlock(b);
   check(b.detach==1&&b.removeSource==1&&b.network.removes==1,"detach once");
   check(b.updateSpeed&&b.level.notifications==1,"schedule reconnection and neighbors");
   if(generator)check(((GeneratingKineticBlockEntity)b).reActivateSource,"reactivate generator");
  }
  for(int threshold:new int[]{64,-64,0})for(float speed:new float[]{0,32,64,128,-32,-64,-128}){
   Centrifugal c=new Centrifugal();c.level=new Level();c.state.set(UNCOUPLED,true);c.speed=speed;c.speedThreshold.value=threshold;
   c.onKineticUpdate();
   boolean expected=speed!=0&&(threshold<0?Math.abs(speed)<=64:Math.abs(speed)>=threshold);
   check(!c.state.getValue(UNCOUPLED)==expected,"centrifugal threshold "+threshold+" at "+speed);
  }
  Centrifugal client=new Centrifugal();client.level=new Level();client.level.client=true;client.state.set(UNCOUPLED,true);client.speed=128;client.speedThreshold.value=64;
  client.onKineticUpdate();check(client.level.changes==0,"client cannot toggle clutch");
  Centrifugal c=new Centrifugal();c.state.set(UNCOUPLED,true).set(FACING,Direction.EAST);
  for(Direction d:Direction.values())check(c.getRotationSpeedModifier(d)==(d==Direction.EAST?0:1),"centrifugal output face");
  Overstress o=new Overstress();o.state.set(STATE,ClutchState.UNCOUPLED);
  for(Direction d:Direction.values())check(o.getRotationSpeedModifier(d)==(d==Direction.NORTH?1:0),"overstress keeps input attached");
  o.state.set(STATE,ClutchState.UNCOUPLING);check(o.getRotationSpeedModifier(Direction.SOUTH)==1,"delayed disengagement transmits");
  Brake brake=new Brake();brake.state.set(POWERED,false);check(brake.calculateStressApplied()==3,"unpowered base impact");
  brake.state.set(POWERED,true);check(brake.calculateStressApplied()==200&&brake.lastStressApplied==200,"powered brake stress");
  for(Direction facing:Direction.values())for(int power:new int[]{0,15})for(int charge:new int[]{0,2,5}){
   Battery b=new Battery();b.state.set(FACING,facing).set(POWER,power).set(LEVEL,charge);
   boolean complete=power>0?charge==0:charge==5;
   for(Direction face:Direction.values()){
    float expected=face.getAxis()!=facing.getAxis()?0:(face!=facing&&!complete?0:1);
    check(b.getRotationSpeedModifier(face)==expected,"battery mode/charge/direction");
   }
  }
  double charge=123456789.12345679;
  CompoundTag tag=new CompoundTag();tag.putDouble("batteryLevel",charge);
  check(tag.getDoubleOr("batteryLevel",0.0)==charge,"native double precision round-trip");
  Battery b=new Battery();b.level=new Level();b.stress=100;b.updateConsumedStress();check(b.consumedStress==0,"transient zero-battery network is finite");
  b.network.sources.put(b,32f);b.updateConsumedStress();check(b.consumedStress==100,"single battery covers demand");
  b.network.sources.put(new Battery(),32f);b.updateConsumedStress();check(b.consumedStress==50,"two batteries share demand");
  b.network.sources.put(new KineticBlockEntity(),40f);b.network.unloadedStress=10;b.updateConsumedStress();check(b.consumedStress==25,"exclude other generation and unloaded stress");
  b.stress=20;b.updateConsumedStress();check(b.consumedStress==0,"no discharge under surplus generation");
  System.out.println("Passed "+checks+" kinetic logic checks; modeled world/network, native NBT.");
 }
}
'''
replacements={
 'HELPER':method('content/KineticHelper.java','updateKineticBlock'),
 'BRIDGE_POS':method('content/kineticbridge/KineticBridgeDestinationBlock.java','getSource'),
 'BRIDGE_VALID':method('content/kineticbridge/KineticBridgeDestinationBlock.java','stillValid'),
 'BRIDGE_SOURCE':method('content/kineticbridge/KineticBridgeDestinationBlockEntity.java','getSource'),
 'BRIDGE_INIT':method('content/kineticbridge/KineticBridgeDestinationBlockEntity.java','initialize'),
 'CENT_UPDATE':method(cent,'onKineticUpdate'),'CENT_MOD':method(cent,'getRotationSpeedModifier'),
 'OVER_MOD':method(over,'getRotationSpeedModifier'),
 'BRAKE_STRESS':method('content/brake/BrakeBlockEntity.java','calculateStressApplied'),
 'BAT_DISCHARGE':method(bat,'isDischarging'),'BAT_COMPLETE':method(bat,'isCurrentStageComplete'),
 'BAT_MOD':method('content/kineticbattery/KineticBatteryBlockEntity.java','getRotationSpeedModifier')}
replacements['BAT_CONSUMPTION']=method('content/kineticbattery/KineticBatteryBlockEntity.java','updateConsumedStress')
for marker,body in replacements.items():java=java.replace(marker,body)
java=re.sub(r'\bBattery\b','KineticBatteryBlockEntity',java)
(OUT/'KineticLogicCheck.java').write_text(java,encoding='utf-8')
cp=os.pathsep.join(json.loads((ROOT/'build/diagnostics-core-26.2/classpath.json').read_text()))
jdk=Path('C:/Java/jdk-25.0.2/bin')
for tool,args in [('javac',['-proc:none','-encoding','UTF-8','-cp',cp,'-d',str(OUT),str(OUT/'KineticLogicCheck.java')]),('java',['-cp',str(OUT)+os.pathsep+cp,'KineticLogicCheck'])]:
 r=subprocess.run([str(jdk/(tool+'.exe')),*args],capture_output=True,text=True)
 (OUT/(tool+'.log')).write_text(r.stdout+r.stderr,encoding='utf-8')
 print((r.stdout+r.stderr)[-2500:])
 if r.returncode: raise SystemExit(r.returncode)
