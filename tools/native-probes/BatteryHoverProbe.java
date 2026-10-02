package batteryhoverprobe;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.world.phys.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import com.hlysine.create_connected.registries.CCBlocks;
import com.hlysine.create_connected.content.kineticbattery.*;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.*;

@Mod("create_world_log_probe")
public class BatteryHoverProbe {
 int ticks,checks; final BlockPos pos=new BlockPos(425,70,-210);boolean placed;
 public BatteryHoverProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void check(boolean value,String detail){checks++;if(!value)throw new AssertionError(detail);}
 void tick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();
  if(mc.gui.screen() instanceof net.minecraft.client.gui.screens.BackupConfirmScreen screen){try{var f=screen.getClass().getDeclaredField("onProceed");f.setAccessible(true);((net.minecraft.client.gui.screens.BackupConfirmScreen.Listener)f.get(screen)).proceed(true,false);}catch(Exception e){throw new RuntimeException(e);}return;}
  if(mc.level==null||mc.player==null||mc.gui.overlay()!=null||++ticks<120)return;
  try{
   if(!placed){placed=true;mc.getSingleplayerServer().execute(()->{var level=mc.getSingleplayerServer().overworld();level.setBlockAndUpdate(pos,CCBlocks.KINETIC_BATTERY.getDefaultState());var player=level.getPlayerByUUID(mc.player.getUUID());if(player!=null)player.setPos(425.5,70,-206);});return;}
   mc.player.setPos(425.5,70,-206);
   if(!(mc.level.getBlockEntity(pos) instanceof KineticBatteryBlockEntity battery))return;
   var behaviour=battery.getBehaviour(ScrollValueBehaviour.TYPE);var handler=CreateClient.VALUE_SETTINGS_HANDLER;
   for(var facing:Direction.values())for(var side:Direction.values()){
    var state=CCBlocks.KINETIC_BATTERY.getDefaultState().setValue(KineticBatteryBlock.FACING,facing);
    mc.level.setBlock(pos,state,2);
    battery=(KineticBatteryBlockEntity)mc.level.getBlockEntity(pos);behaviour=battery.getBehaviour(ScrollValueBehaviour.TYPE);
    var slot=new KineticBatteryValueBox(3);((com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform.Sided)slot).fromSide(side);
    var center=slot.getLocalOffset(mc.level,pos,state);
    handler.hoverWarmup=0;handler.hoverTicks=0;
    mc.hitResult=new BlockHitResult(Vec3.atLowerCornerOf(pos).add(new Vec3(.5+side.getStepX()*.5,.5+side.getStepY()*.5,.5+side.getStepZ()*.5)),side,pos,false);
    ScrollValueRenderer.tick();check(handler.hoverWarmup==0,"Body triggered hint: "+facing+"/"+side);
    handler.hoverWarmup=0;handler.hoverTicks=0;
    mc.hitResult=new BlockHitResult(Vec3.atLowerCornerOf(pos).add(center),side,pos,false);
    ScrollValueRenderer.tick();check((handler.hoverWarmup>0)==(facing.getAxis()!=side.getAxis()),"Settings hint mismatch: "+facing+"/"+side);
   }
   Files.writeString(mc.gameDirectory.toPath().resolve("BATTERY-HOVER-PASS.txt"),"PASS battery direction hint only over settings area: "+checks+" native assertions, all six facings and six hit faces.\n");mc.stop();
  }catch(Throwable e){e.printStackTrace();try{Files.writeString(mc.gameDirectory.toPath().resolve("BATTERY-HOVER-FAIL.txt"),e.toString());}catch(Exception ignored){}mc.stop();}
 }
}
