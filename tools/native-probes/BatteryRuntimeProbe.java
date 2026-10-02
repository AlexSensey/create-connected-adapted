package batteryprobe;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.minecraft.core.*;
import net.minecraft.world.level.block.Blocks;
import com.hlysine.create_connected.registries.CCBlocks;
import com.hlysine.create_connected.content.kineticbattery.*;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;

@Mod("create_world_log_probe")
public class BatteryRuntimeProbe {
 int clientTicks,serverTicks;volatile boolean ready,done;BlockPos pos;double before;StringBuilder report=new StringBuilder();
 public BatteryRuntimeProbe(){NeoForge.EVENT_BUS.addListener(this::client);NeoForge.EVENT_BUS.addListener(this::server);}
 void client(ClientTickEvent.Post e){
  var mc=Minecraft.getInstance();
  if(mc.gui.screen() instanceof net.minecraft.client.gui.screens.BackupConfirmScreen screen){try{var f=screen.getClass().getDeclaredField("onProceed");f.setAccessible(true);((net.minecraft.client.gui.screens.BackupConfirmScreen.Listener)f.get(screen)).proceed(true,false);}catch(Exception ex){throw new RuntimeException(ex);}return;}
  if(done){mc.stop();return;}
  if(ready||mc.level==null||mc.player==null||mc.gui.overlay()!=null||++clientTicks<120)return;
  pos=mc.player.blockPosition().offset(8,6,0);ready=true;
 }
 void server(ServerTickEvent.Post e){
  if(!ready||done)return;
  try{
   var level=e.getServer().overworld();
   if(++serverTicks==1){
    level.getChunkAt(pos);
    level.setBlockAndUpdate(pos,CCBlocks.KINETIC_BATTERY.getDefaultState().setValue(KineticBatteryBlock.FACING,Direction.EAST));
    level.setBlockAndUpdate(pos.east(),AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalKineticBlock.FACING,Direction.WEST));
    var b=(KineticBatteryBlockEntity)level.getBlockEntity(pos);b.setBatteryLevel(100000);
    before=b.getBatteryLevel();
   }
   var b=(KineticBatteryBlockEntity)level.getBlockEntity(pos);if(b==null)throw new AssertionError("Missing battery");
   if(serverTicks==80){
    report.append("Unpowered: power=").append(b.getBlockState().getValue(KineticBatteryBlock.POWER)).append(" speed=").append(b.getSpeed()).append(" stressImpact=").append(b.calculateStressApplied()).append(" charge=").append(before).append(" -> ").append(b.getBatteryLevel()).append('\n');
    if(KineticBatteryBlock.isDischarging(b.getBlockState())||b.getBatteryLevel()<=before)throw new AssertionError("Unpowered battery did not charge");
    level.setBlockAndUpdate(pos.east(),Blocks.AIR.defaultBlockState());
    level.setBlockAndUpdate(pos.east(),AllBlocks.MECHANICAL_SAW.getDefaultState());
    level.setBlockAndUpdate(pos.north(),Blocks.REDSTONE_BLOCK.defaultBlockState());before=b.getBatteryLevel();
   }
   if(serverTicks==160){
    report.append("Powered: power=").append(b.getBlockState().getValue(KineticBatteryBlock.POWER)).append(" speed=").append(b.getSpeed()).append(" charge=").append(before).append(" -> ").append(b.getBatteryLevel()).append('\n');
    if(!KineticBatteryBlock.isDischarging(b.getBlockState()))throw new AssertionError("Powered battery did not switch to discharge");
    level.setBlockAndUpdate(pos.north(),Blocks.AIR.defaultBlockState());
    level.setBlockAndUpdate(pos.east(),AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(DirectionalKineticBlock.FACING,Direction.WEST));before=b.getBatteryLevel();
   }
   if(serverTicks==240){
    report.append("Power removed: power=").append(b.getBlockState().getValue(KineticBatteryBlock.POWER)).append(" speed=").append(b.getSpeed()).append(" charge=").append(before).append(" -> ").append(b.getBatteryLevel()).append('\n');
    if(KineticBatteryBlock.isDischarging(b.getBlockState())||b.getBatteryLevel()<=before)throw new AssertionError("Battery did not resume charging");
    finish("BATTERY-PASS.txt",report+"PASS native battery charging and mode switching\n");
   }
  }catch(Throwable ex){ex.printStackTrace();finish("BATTERY-FAIL.txt",report+ex.toString());}
 }
 void finish(String name,String text){try{Files.writeString(Minecraft.getInstance().gameDirectory.toPath().resolve(name),text);}catch(Exception ex){throw new RuntimeException(ex);}done=true;}
}
