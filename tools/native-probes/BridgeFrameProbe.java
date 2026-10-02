package bridgeframeprobe;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import com.hlysine.create_connected.registries.CCBlocks;
import com.hlysine.create_connected.content.kineticbridge.*;

@Mod("create_world_log_probe")
public class BridgeFrameProbe {
 int ticks, checks; boolean prepared; final BlockPos source=new BlockPos(425,65,-210);
 public BridgeFrameProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void check(boolean pass){checks++;if(!pass)throw new AssertionError("Bridge panel alignment "+checks);}
 void tick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();
  if(mc.gui.screen() instanceof net.minecraft.client.gui.screens.BackupConfirmScreen screen){
   try{var f=screen.getClass().getDeclaredField("onProceed");f.setAccessible(true);((net.minecraft.client.gui.screens.BackupConfirmScreen.Listener)f.get(screen)).proceed(true,false);}catch(Exception e){throw new RuntimeException(e);}return;
  }
  if(mc.level==null||mc.player==null||mc.gui.overlay()!=null)return;
  try{
   if(!prepared){
    prepared=true;
    for(var facing:Direction.values())for(var side:Direction.values()){
     if(facing.getAxis()==side.getAxis())continue;
     for(boolean destination:new boolean[]{false,true}){
      var state=(destination?CCBlocks.KINETIC_BRIDGE_DESTINATION.getDefaultState():CCBlocks.KINETIC_BRIDGE.getDefaultState()).setValue(KineticBridgeBlock.FACING,facing);
      var box=new KineticBridgeValueBox();box.fromSide(side);
      var offset=box.getLocalOffset(mc.level,source,state);
      double longitudinal=(offset.x-.5)*facing.getStepX()+(offset.y-.5)*facing.getStepY()+(offset.z-.5)*facing.getStepZ();
      check(Math.abs(longitudinal-(destination?3/16d:-3/16d))<1e-7);
      check(box.testHit(mc.level,source,state,new Vec3(.5,.5,.5)));
     }
    }
    mc.getSingleplayerServer().execute(()->{
     var world=mc.getSingleplayerServer().overworld();
     for(int x=-1;x<=2;x++)for(int y=0;y<=2;y++)for(int z=-1;z<=4;z++)world.setBlockAndUpdate(source.offset(x,y,z),Blocks.AIR.defaultBlockState());
     world.setBlockAndUpdate(source,CCBlocks.KINETIC_BRIDGE.getDefaultState().setValue(KineticBridgeBlock.FACING,Direction.EAST));
     world.setBlockAndUpdate(source.east(),CCBlocks.KINETIC_BRIDGE_DESTINATION.getDefaultState().setValue(KineticBridgeBlock.FACING,Direction.EAST));
     var player=world.getPlayerByUUID(mc.player.getUUID());
     if(player!=null)player.setPos(425.5,65,-206);
    });
   }
   mc.player.setPos(425.5,65,-206);mc.player.setYRot(180);mc.player.setXRot(16);
   var target=ticks<130?source:source.east();
   mc.hitResult=new BlockHitResult(new Vec3(target.getX()+.3125,65.5,-209),Direction.SOUTH,target,false);
   ticks++;
   if(ticks==120||ticks==180)net.minecraft.client.Screenshot.grab(mc,false);
   if(ticks>=200){Files.writeString(mc.gameDirectory.toPath().resolve("BRIDGE-PASS.txt"),"PASS native panel alignment and side hit testing: "+checks+" assertions; both bridge halves rendered and captured.\n");mc.stop();}
  }catch(Throwable e){e.printStackTrace();try{Files.writeString(mc.gameDirectory.toPath().resolve("BRIDGE-FAIL.txt"),e.toString());}catch(Exception ignored){}mc.stop();}
 }
}
