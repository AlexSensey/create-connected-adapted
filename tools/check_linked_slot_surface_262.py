"""Check production slot math against the panel surface; native Catnip/PoseStack math.
Block state and Flywheel's rotation facade are modeled (its PoseStack mixin needs FML).
This does not execute the renderer or launch Minecraft.
"""
import json, re, subprocess
from pathlib import Path
root=Path(__file__).resolve().parents[1]
source=(root/'src/main/java/com/hlysine/create_connected/content/linkedtransmitter/LinkedTransmitterFrequencySlot.java').read_text()
out=root/'build/api-check-26.2/linked-slot-surface-test';out.mkdir(parents=True,exist_ok=True)
def method(name):
    m=re.search(r'    public [^\n]*\b'+name+r'\([^\n]*\) \{',source)
    start=m.start();i=source.index('{',start)+1;depth=1
    while depth:
        depth+=(source[i]=='{')-(source[i]=='}');i+=1
    return source[start:i]
model=json.loads((root/'src/main/resources/assets/create_connected/models/block/linked_transmitter/block.json').read_text())
surface=max(e['to'][1] for e in model['elements'] if e.get('name','').startswith('Slot'))/16
java='''
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;
import net.createmod.catnip.api.math.VecHelper;
import net.createmod.catnip.api.math.AngleHelper;
import com.mojang.blaze3d.vertex.PoseStack;
public class LinkedSlotSurfaceCheck {
 record Key<T>(){}
 static class FaceAttachedHorizontalDirectionalBlock {
  static final Key<Direction> FACING=new Key<>();static final Key<AttachFace> FACE=new Key<>();
 }
 static class BlockStateProperties {static final Key<Boolean> LOCKED=new Key<>();}
 static class BlockState {
  Direction facing;AttachFace face;
  @SuppressWarnings("unchecked") <T>T getValue(Key<T> key){
   return (T)(key==FaceAttachedHorizontalDirectionalBlock.FACING?facing:
    key==FaceAttachedHorizontalDirectionalBlock.FACE?face:Boolean.FALSE);
  }
 }
 static class LevelAccessor{}static class BlockPos{}
 static class NativePoseRotations {
  PoseStack pose;NativePoseRotations(PoseStack p){pose=p;}
  NativePoseRotations rotateYDegrees(float degrees){pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(degrees));return this;}
  NativePoseRotations rotateXDegrees(float degrees){pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(degrees));return this;}
 }
 boolean first;boolean isFirst(){return first;}
'''+method('getLocalOffset')+method('rotate').replace('TransformStack.of(ms)', 'new NativePoseRotations(ms)')+'''
 public static void main(String[] args){
  double surface=Double.parseDouble(args[0]);int checks=0;
  var slot=new LinkedSlotSurfaceCheck();
  for(Direction direction:new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST})
   for(AttachFace face:AttachFace.values())for(boolean first:new boolean[]{true,false}){
    slot.first=first;var state=new BlockState();state.facing=direction;state.face=face;
    Vec3 center=slot.getLocalOffset(null,null,state);var pose=new PoseStack();
    pose.translate(center.x,center.y,center.z);slot.rotate(null,null,state,pose);
    // Native submitValueBoxFrame translates local +Z by 1/512 after slot scale is undone.
    var frame=new org.joml.Vector3f(0,0,1/512f).mulPosition(pose.last().pose());
    double coordinate=face==AttachFace.WALL?(direction.getAxis()==Axis.X?frame.x:frame.z):frame.y;
    double distance=Math.min(coordinate,1-coordinate);
    if(distance<=surface)throw new AssertionError("Frame below panel: "+direction+"/"+face+"/"+first+" "+distance);
    checks++;
   }
  System.out.println("PASS: "+checks+" slot/frame positions outside model panels (native math, modeled states)");
 }
}
'''
path=out/'LinkedSlotSurfaceCheck.java';path.write_text(java)
cp=';'.join(json.loads((root/'build/diagnostics-core-26.2/classpath.json').read_text(encoding='utf-8-sig')))
for cmd in [['C:/Java/jdk-25.0.2/bin/javac.exe','-classpath',cp,'-d',str(out),str(path)],
            ['C:/Java/jdk-25.0.2/bin/java.exe','-classpath',str(out)+';'+cp,'LinkedSlotSurfaceCheck',str(surface)]]:
    result=subprocess.run(cmd,capture_output=True,text=True);print((result.stdout+result.stderr)[-2000:])
    if result.returncode:raise SystemExit(result.returncode)
