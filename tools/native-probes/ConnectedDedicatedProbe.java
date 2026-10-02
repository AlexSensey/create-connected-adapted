package connecteddedicatedprobe;
import java.nio.file.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import com.hlysine.create_connected.registries.CCBlocks;
import net.minecraft.core.BlockPos;

@Mod("connected_dedicated_probe")
public class ConnectedDedicatedProbe {
 public ConnectedDedicatedProbe(){NeoForge.EVENT_BUS.addListener(this::started);}
 void started(ServerStartedEvent event){
  try{
   boolean clientAbsent=false;try{Class.forName("net.minecraft.client.gui.screens.Screen");}catch(ClassNotFoundException expected){clientAbsent=true;}
   if(!clientAbsent)throw new AssertionError("Client Screen unexpectedly present in dedicated test");
   var level=event.getServer().overworld();var pos=new BlockPos(0,100,0);level.getChunkAt(pos);
   level.setBlockAndUpdate(pos.below(),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
   level.setBlockAndUpdate(pos,CCBlocks.SEQUENCED_PULSE_GENERATOR.getDefaultState());
   level.setBlockAndUpdate(pos.east(),CCBlocks.KINETIC_BATTERY.getDefaultState());
   if(level.getBlockEntity(pos)==null||level.getBlockEntity(pos.east())==null)throw new AssertionError("Connected block entities missing");
   Files.writeString(Path.of("DEDICATED-PASS.txt"),"PASS native dedicated server started without client Screen class; Sequenced Pulse Generator and Kinetic Battery block entities created.\n");
  }catch(Throwable error){error.printStackTrace();try{Files.writeString(Path.of("DEDICATED-FAIL.txt"),error.toString());}catch(Exception ignored){}}
  event.getServer().halt(false);
 }
}
