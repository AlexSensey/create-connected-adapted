package connectedresourceprobe;

import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import com.hlysine.create_connected.registries.CCBlocks;
import com.hlysine.create_connected.content.linkedtransmitter.LinkedTransmitterBlock;

@Mod("create_world_log_probe")
public final class ConnectedResourceProbe {
 int ticks;volatile boolean launched,done;final List<String> report=new ArrayList<>();int checks;
 public ConnectedResourceProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void check(boolean condition,String message){if(!condition)throw new AssertionError(message);checks++;report.add("PASS "+message);}
 static Identifier id(String path){return Identifier.fromNamespaceAndPath("create_connected",path);}
 void tick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();
  // This probe runs only in the runner's disposable world copy.
  if(mc.gui.screen() instanceof net.minecraft.client.gui.screens.BackupConfirmScreen screen){
   try{var field=screen.getClass().getDeclaredField("onProceed");field.setAccessible(true);
    ((net.minecraft.client.gui.screens.BackupConfirmScreen.Listener)field.get(screen)).proceed(true,false);
   }catch(Exception error){throw new RuntimeException(error);}return;
  }
  if(done){mc.stop();return;}
  if(launched||mc.level==null||mc.player==null||mc.gui.overlay()!=null||++ticks<120)return;
  launched=true;
  mc.getSingleplayerServer().execute(()->{
   try{
    var server=mc.getSingleplayerServer();var level=server.overworld();
    var manager=server.getRecipeManager();
    long count=manager.getRecipes().stream().filter(r->r.id().identifier().getNamespace().equals("create_connected")).count();
    check(count>0,"native Connected recipes loaded: "+count);
    for(String recipe:List.of("crafting/kinetics/kinetic_bridge","crafting/kinetics/centrifugal_clutch","crafting/kinetics/overstress_clutch","crafting/kinetics/kinetic_battery","crafting/kinetics/brake","sequenced_assembly/control_chip")){
     check(manager.byKey(ResourceKey.create(Registries.RECIPE,id(recipe))).isPresent(),"recipe "+recipe);
    }
    check(manager.byKey(ResourceKey.create(Registries.RECIPE,Identifier.fromNamespaceAndPath("create","cutting/shaft"))).isPresent(),"shaft cutting recipe decoded");
    check(CCBlocks.LINKED_BUTTONS.size()>=14,"all vanilla linked buttons registered");
    for(var entry:CCBlocks.LINKED_BUTTONS.values()){
     var block=entry.get();var base=((LinkedTransmitterBlock)block).getBase();
     check(block.asItem()==net.minecraft.world.item.Items.AIR,"linked button has no independent BlockItem: "+entry.getId());
     var drops=Block.getDrops(block.defaultBlockState(),level,new BlockPos(0,80,0),null,null,ItemStack.EMPTY);
     check(drops.size()==1 && drops.getFirst().is(base.asItem()) && drops.getFirst().getCount()==1,"native linked button returns base item: "+entry.getId());
    }
    for(var entry:List.of(CCBlocks.LINKED_LEVER,CCBlocks.LINKED_ANALOG_LEVER)){
     var block=entry.get();var base=((LinkedTransmitterBlock)block).getBase();
     var drops=Block.getDrops(block.defaultBlockState(),level,new BlockPos(0,80,0),null,null,ItemStack.EMPTY);
     check(drops.size()==1 && drops.getFirst().is(base.asItem()),"native linked lever returns base item: "+entry.getId());
    }
    var pale=BuiltInRegistries.BLOCK.getValue(id("linked_pale_oak_button"));
    check(pale!=Blocks.AIR,"pale oak linked button registered");
    check(pale.getStateDefinition().getPossibleStates().size()==48,"all 48 pale oak button states preserved");
    check(mc.getResourceManager().getResource(id("blockstates/linked_pale_oak_button.json")).isPresent(),"pale oak model resource loaded");
    report.add("Assertions: "+checks);
    Files.writeString(Path.of("CONNECTED-PASS.txt"),String.join("\n",report)+"\n");
    Files.writeString(Path.of("WORLD-LOADED.txt"),"PASS original world copy with Connected "+net.neoforged.fml.ModList.get().getModContainerById("create_connected").orElseThrow().getModInfo().getVersion()+"\n");
   }catch(Throwable error){
    try{Files.writeString(Path.of("CONNECTED-FAIL.txt"),String.join("\n",report)+"\n"+error);}catch(Exception ignored){}
    error.printStackTrace();
   }finally{done=true;}
  });
 }
}
