package connectedponderprobe;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.createmod.ponder.api.client.PonderIndex;
import net.createmod.ponder.impl.client.gui.PonderUI;

@Mod("create_world_log_probe")
public class ConnectedPonderProbe {
 int ticks,phase; final StringBuilder report=new StringBuilder();
 public ConnectedPonderProbe(net.neoforged.bus.api.IEventBus bus){
  NeoForge.EVENT_BUS.addListener(this::tick);
 }
 static int renderCalls;
 static boolean hasHoldMessage(net.minecraft.network.chat.Component text){
  if(text.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents translated&&translated.getKey().equals("ponder.ui.hold_to_ponder"))return true;
  return text.getSiblings().stream().anyMatch(ConnectedPonderProbe::hasHoldMessage);
 }
 void checkPaletteFormats() throws Exception {
  for(boolean legacy:new boolean[]{true,false})for(boolean multiple:new boolean[]{true,false}){
   var root=new net.minecraft.nbt.CompoundTag();var size=new net.minecraft.nbt.ListTag();for(int i=0;i<3;i++)size.add(net.minecraft.nbt.IntTag.valueOf(1));root.put("size",size);
   var state=new net.minecraft.nbt.CompoundTag();state.putString(legacy?"Name":"id","minecraft:oak_log");var properties=new net.minecraft.nbt.CompoundTag();properties.putString("axis","x");state.put(legacy?"Properties":"properties",properties);
   var palette=new net.minecraft.nbt.ListTag();palette.add(state);
   if(multiple){var palettes=new net.minecraft.nbt.ListTag();palettes.add(palette);root.put("palettes",palettes);}else root.put("palette",palette);
   var block=new net.minecraft.nbt.CompoundTag();block.putInt("state",0);var pos=new net.minecraft.nbt.ListTag();for(int i=0;i<3;i++)pos.add(net.minecraft.nbt.IntTag.valueOf(0));block.put("pos",pos);var blocks=new net.minecraft.nbt.ListTag();blocks.add(block);root.put("blocks",blocks);
   var data=new java.io.ByteArrayOutputStream();net.minecraft.nbt.NbtIo.writeCompressed(root,data);
   var template=net.createmod.ponder.impl.client.registration.PonderSceneRegistry.loadSchematic(new java.io.ByteArrayInputStream(data.toByteArray()));
   var loaded=template.filterBlocks(net.minecraft.core.BlockPos.ZERO,new net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings(),net.minecraft.world.level.block.Blocks.OAK_LOG);
   if(loaded.size()!=1||loaded.getFirst().state().getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS)!=net.minecraft.core.Direction.Axis.X)throw new AssertionError("Palette format/orientation: "+legacy+"/"+multiple);
   report.append("PASS palette legacy=").append(legacy).append(" multiple=").append(multiple).append(" preserves oak_log axis=x\n");
  }
 }
 public static class DiagnosticRenderer extends net.createmod.ponder.impl.client.gui.PonderSceneRenderer {
  public DiagnosticRenderer(){super(net.createmod.catnip.impl.client.render.MultiBufferSource.immediateWithBuffers(Map.of(),new com.mojang.blaze3d.vertex.ByteBufferBuilder(256)));}
  @Override protected void renderToTexture(net.createmod.ponder.impl.client.gui.PonderSceneRenderState state,com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.client.renderer.SubmitNodeCollector collector){
   if(renderCalls++==0){
    int blocks=0;for(int x=-2;x<=10;x++)for(int y=-2;y<=10;y++)for(int z=-2;z<=10;z++)if(!state.scene().getWorld().getBlockState(new net.minecraft.core.BlockPos(x,y,z)).isAir())blocks++;
    System.out.println("PONDER_RENDER state="+state.width()+"x"+state.height()+" bounds="+state.bounds()+" scale="+state.window().guiScale+" blocks="+blocks);
   }
   super.renderToTexture(state,pose,collector);
  }
 }
 void tick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();
  if(mc.gui.screen() instanceof net.minecraft.client.gui.screens.BackupConfirmScreen screen){
   try{var f=screen.getClass().getDeclaredField("onProceed");f.setAccessible(true);((net.minecraft.client.gui.screens.BackupConfirmScreen.Listener)f.get(screen)).proceed(true,false);}catch(Exception e){throw new RuntimeException(e);}return;
  }
  if(mc.level==null||mc.player==null||mc.gui.overlay()!=null||++ticks<120)return;
  try{
   if(phase==0){
    checkPaletteFormats();
    phase=1;report.append("Plugins: ").append(PonderIndex.streamPlugins().map(p->p.getModId()).toList()).append('\n');
    var access=PonderIndex.getSceneAccess();
    var entries=access.getRegisteredEntries().stream().filter(e->e.getKey().getNamespace().equals("create_connected")).toList();
    report.append("Connected storyboards: ").append(entries.size()).append('\n');
    Files.writeString(mc.gameDirectory.toPath().resolve("PONDER-DIAGNOSTICS.txt"),report.toString());
    if(entries.isEmpty())throw new AssertionError("No Connected Ponder scenes registered");
    var keys=new LinkedHashSet<Identifier>();for(var entry:entries)keys.add(entry.getKey());
    for(var key:keys){
     var item=BuiltInRegistries.ITEM.getValue(key);
     var tooltip=new net.minecraft.world.item.ItemStack(item).getTooltipLines(net.minecraft.world.item.Item.TooltipContext.of(mc.level),mc.player,net.minecraft.world.item.TooltipFlag.NORMAL);
     if(tooltip.stream().noneMatch(ConnectedPonderProbe::hasHoldMessage))throw new AssertionError("Ponder hold tooltip missing: "+key);
     report.append("Tooltip ").append(key).append(": ").append(tooltip.stream().map(t->t.getString()).toList()).append('\n');
     var scenes=access.compile(key);
     if(scenes.isEmpty())throw new AssertionError("Empty scenes: "+key);
     for(var scene:scenes){int blocks=0;for(int x=-2;x<=20;x++)for(int y=-2;y<=12;y++)for(int z=-2;z<=20;z++)if(!scene.getWorld().getBlockState(new net.minecraft.core.BlockPos(x,y,z)).isAir())blocks++;
      report.append("Scene block count ").append(key).append(": ").append(blocks).append('\n');
      if(blocks==0)throw new AssertionError("No visible blocks loaded for "+key);
      for(int x=-2;x<=20;x++)for(int y=-2;y<=12;y++)for(int z=-2;z<=20;z++){
       var p=new net.minecraft.core.BlockPos(x,y,z);
       if(scene.getWorld().getBlockEntity(p) instanceof com.simibubi.create.content.kinetics.belt.BeltBlockEntity belt)
        report.append("BELT ").append(key).append(" pos=").append(p).append(" length=").append(belt.beltLength).append(" controller=").append(belt.getController()).append(" isController=").append(belt.isController()).append('\n');
      }
     }
     report.append("PASS compile ").append(key).append(" scenes=").append(scenes.size()).append('\n');
     Files.writeString(mc.gameDirectory.toPath().resolve("PONDER-DIAGNOSTICS.txt"),report.toString());
    }
    var subject=Identifier.parse(System.getProperty("ponder.probe.subject","create_connected:kinetic_battery"));
    var battery=BuiltInRegistries.ITEM.getValue(subject);
    report.append("Rendered subject: ").append(subject).append('\n');
    if(Boolean.getBoolean("ponder.probe.renderer")){
     var gf=mc.gameRenderer.getClass().getDeclaredField("guiRenderer");gf.setAccessible(true);var gui=gf.get(mc.gameRenderer);
     var pf=gui.getClass().getDeclaredField("pictureInPictureRendererPools");pf.setAccessible(true);
     var pools=new HashMap<>((Map)pf.get(gui));
     System.out.println("PONDER_POOLS "+pools.keySet());
     var registration=new net.neoforged.neoforge.client.gui.PictureInPictureRendererRegistration<>(net.createmod.ponder.impl.client.gui.PonderSceneRenderState.class,DiagnosticRenderer::new);
     pools.put(net.createmod.ponder.impl.client.gui.PonderSceneRenderState.class,new net.neoforged.neoforge.client.gui.PictureInPictureRendererPool<>(registration));pf.set(gui,pools);
    }
    var ui=PonderUI.of(battery);
    if(Boolean.getBoolean("ponder.probe.batteryBelt")){
     var field=PonderUI.class.getDeclaredField("scenes");field.setAccessible(true);
     var scene=((java.util.List<net.createmod.ponder.api.client.scene.PonderScene>)field.get(ui)).stream().filter(s->s.getSceneId().getPath().equals("kinetic_battery")).findFirst().orElseThrow();
     for(int t=0;t<1000;t++)scene.tick();
     var start=new net.minecraft.core.BlockPos(1,1,2);var end=start.north();
     var controller=(com.simibubi.create.content.kinetics.belt.BeltBlockEntity)scene.getWorld().getBlockEntity(start);
     var follower=(com.simibubi.create.content.kinetics.belt.BeltBlockEntity)scene.getWorld().getBlockEntity(end);
     if(controller==null||follower==null||!controller.isController()||controller.beltLength!=2||!follower.getController().equals(start)||follower.index!=1)throw new AssertionError("Battery belt chain missing");
     report.append("PASS dynamically created battery belt: controller, length=2, follower index=1\n");
    }
    net.createmod.catnip.api.client.gui.ScreenOpener.transitionTo(ui);ticks=0;return;
   }
   if(ticks==150)net.minecraft.client.Screenshot.grab(mc,false);
   if(ticks>=170){
    if(!(mc.gui.screen() instanceof PonderUI))throw new AssertionError("Ponder screen missing");
    Files.writeString(mc.gameDirectory.toPath().resolve("PONDER-PASS.txt"),report+"PASS Ponder screen rendered\n");mc.stop();
   }
  }catch(Throwable e){e.printStackTrace();try{Files.writeString(mc.gameDirectory.toPath().resolve("PONDER-FAIL.txt"),report+e.toString());}catch(Exception ignored){}mc.stop();}
 }
}
