package reiprobe;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
@Mod("rei_probe")
public final class ReiProbe {
 int ticks,phase,assertions;boolean done,menuOpened;StringBuilder report=new StringBuilder();java.util.List<me.shedaniel.rei.api.common.display.Display> pages=new ArrayList<>();int machines;
 void check(boolean pass,String message){assertions++;report.append(pass?"PASS ":"FAIL ").append(message).append('\n');if(!pass)throw new AssertionError(message);}
 public ReiProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void tick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();
  // This probe runs only in the runner's disposable world copy.
  if(mc.gui.screen() instanceof net.minecraft.client.gui.screens.BackupConfirmScreen screen){
   try{var field=screen.getClass().getDeclaredField("onProceed");field.setAccessible(true);
    ((net.minecraft.client.gui.screens.BackupConfirmScreen.Listener)field.get(screen)).proceed(true,false);
   }catch(Exception error){throw new RuntimeException(error);}return;
  }
  if(done||mc.level==null||mc.player==null||mc.gui.overlay()!=null)return;
  ++ticks;
  if(Boolean.getBoolean("rei.probe.menuOnly")){
   if(!menuOpened&&ticks>=100){menuOpened=true;mc.gui.setScreen(new MenuProbeScreen());}
   return;
  }
  if(phase>0&&ticks==30&&pages.get(phase-1).getCategoryIdentifier().toString().equals("create:sequenced_assembly"))net.minecraft.client.Screenshot.grab(mc,false);
  if(ticks<(phase==0?300:60))return;
  try{
   var registry=DisplayRegistry.getInstance();
   if(phase>0){
    check(mc.gui.screen()!=null&&mc.gui.screen().getClass().getName().contains("DisplayViewing"),"REI page renders "+pages.get(phase-1).getCategoryIdentifier());
    if(Boolean.getBoolean("rei.probe.layoutCheck")){
     var screen=(me.shedaniel.rei.impl.client.gui.screen.DefaultDisplayViewingScreen)mc.gui.screen();
     check(screen.getCurrentCategoryId().equals(pages.get(phase-1).getCategoryIdentifier()),"expected category selected");
     var field=screen.getClass().getDeclaredField("recipeBounds");field.setAccessible(true);
     var recipeBounds=(Map<me.shedaniel.math.Rectangle,?>)field.get(screen);var frame=screen.getBounds();
     check(!recipeBounds.isEmpty(),"recipe rectangles present");
     for(var rect:recipeBounds.keySet()){
      check(rect.x>=frame.x&&rect.getMaxX()<=frame.getMaxX(),"recipe stays between sidebar buttons");
      check(rect.y>=frame.y+32,"recipe below title and page controls");
      check(rect.getMaxY()<=frame.getMaxY(),"recipe bottom fully visible");
     }
    }
    net.minecraft.client.Screenshot.grab(mc,false);
    if(phase<pages.size()){open(pages.get(phase++));ticks=0;return;}
    Files.writeString(Path.of("REI-PASS.txt"),report+"Assertions: "+assertions);done=true;mc.stop();return;
   }
   report.append("Total displays: "+registry.size()+"\n");
   registry.getAll().forEach((id,displays)->report.append(id).append(": ").append(displays.size()).append('\n'));
   for(String namespace:List.of("ad_astra","create","createaddition","create_connected","handcrafted")){
    long count=registry.getAll().values().stream().flatMap(Collection::stream).filter(d->d.getDisplayLocation().map(id->id.getNamespace().equals(namespace)).orElse(false)).count();
    report.append("Recipe IDs ").append(namespace).append(": ").append(count).append('\n');
   }
   Files.writeString(Path.of("REI-REPORT.txt"),report);
   var manualPages=new ArrayList<me.shedaniel.rei.api.common.display.Display>();
   if(Boolean.getBoolean("rei.probe.manualOnly")||Boolean.getBoolean("rei.probe.coreOnly")){
    var application=me.shedaniel.rei.api.common.category.CategoryIdentifier.of("create:item_application");
    var displays=registry.getAll().entrySet().stream().filter(e->e.getKey().equals(application)).map(Map.Entry::getValue).findFirst().orElseThrow();
    for(String target:List.of("andesite_encased_large_cogwheel","brass_encased_large_cogwheel","andesite_encased_cogwheel","brass_encased_cogwheel","andesite_encased_shaft","brass_encased_shaft")){
     var item=net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse("create:"+target));
     check(item!=net.minecraft.world.item.Items.AIR,"target item registered "+target);
     var display=displays.stream().filter(d->d.getOutputEntries().stream().flatMap(Collection::stream).anyMatch(e->e.getValue() instanceof net.minecraft.world.item.ItemStack stack&&stack.is(item))).findFirst().orElseThrow();
     var input=display.getInputEntries().getFirst().stream().findFirst().orElseThrow();
     var search=me.shedaniel.rei.api.client.view.ViewSearchBuilder.builder().filterCategory(application).addRecipesFor(me.shedaniel.rei.api.common.util.EntryStacks.of(new net.minecraft.world.item.ItemStack(item)));
     check(search.streamDisplays().findAny().isPresent(),"R finds manual encasing "+target);
     var usage=me.shedaniel.rei.api.client.view.ViewSearchBuilder.builder().filterCategory(application).addUsagesFor(input);
     check(usage.streamDisplays().findAny().isPresent(),"U finds manual encasing "+target);
     var held=(net.minecraft.world.item.ItemStack)display.getInputEntries().get(1).stream().findFirst().orElseThrow().getValue();
     check(held.is(com.simibubi.create.AllBlocks.BRASS_CASING.asItem())||held.is(com.simibubi.create.AllBlocks.ANDESITE_CASING.asItem())||held.is(com.simibubi.create.AllBlocks.COPPER_CASING.asItem()),"correct casing ingredient "+target);
     check(display.getClass().getField("keepHeldItem").getBoolean(display),"casing remains in hand "+target);
     manualPages.add(display);
    }
    check(!net.minecraft.network.chat.Component.translatable("create.rei.manual_application").getString().equals("create.rei.manual_application"),"manual instruction translated");
    check(mc.font.width(net.minecraft.network.chat.Component.translatable("create.rei.manual_application"))<=169,"manual instruction fits recipe width");
    if(Boolean.getBoolean("rei.probe.manualOnly")){pages.addAll(manualPages);phase=1;ticks=0;open(pages.getFirst());return;}
   }
   if(Boolean.getBoolean("rei.probe.connectedOnly")){
    var visible=me.shedaniel.rei.api.client.registry.entry.EntryRegistry.getInstance().getPreFilteredList();
    int disabled=0,enabled=0;
    var connectedItems=(List<com.tterrag.registrate.util.entry.ItemProviderEntry<?,?>>)Class.forName("com.hlysine.create_connected.registries.CCCreativeTabs").getField("ITEMS").get(null);
    var enabledMethod=Class.forName("com.hlysine.create_connected.config.FeatureToggle").getMethod("isEnabled",net.minecraft.resources.Identifier.class);
    for(var item:connectedItems){
     boolean present=visible.stream().anyMatch(e->e.getValue() instanceof net.minecraft.world.item.ItemStack stack&&stack.is(item.asItem()));
     boolean active=(boolean)enabledMethod.invoke(null,item.getId());
     check(present==active,"REI feature visibility "+item.getId()+" enabled="+active);
     if(active)enabled++;else disabled++;
    }
    check(disabled>0&&enabled>0,"both optional and core items checked");
    try(var input=mc.getResourceManager().getResource(net.minecraft.resources.Identifier.parse("create_connected:compat/catalyst_fallbacks.json")).orElseThrow().open()){
     var definitions=com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(input,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
     for(var model:definitions.getAsJsonObject("models").entrySet()){
      var resource=mc.getResourceManager().getResource(net.minecraft.resources.Identifier.parse("create_connected:"+model.getKey())).orElseThrow();
      try(var reader=resource.openAsReader()){
       var textures=com.google.gson.JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("textures");
       for(var texture:textures.entrySet()){
        var id=net.minecraft.resources.Identifier.parse(texture.getValue().getAsString());
        check(mc.getResourceManager().getResource(id.withPath("textures/"+id.getPath()+".png")).isPresent(),"resolved catalyst texture "+model.getKey()+" "+texture.getKey()+"="+id);
       }
      }
     }
    }
    var all=registry.getAll().values().stream().flatMap(Collection::stream).toList();
    for(String path:List.of("crafting/kinetics/kinetic_bridge","crafting/kinetics/centrifugal_clutch","crafting/kinetics/overstress_clutch","crafting/kinetics/kinetic_battery","crafting/kinetics/brake","sequenced_assembly/control_chip")){
     var display=all.stream().filter(d->d.getDisplayLocation().map(id->id.toString().equals("create_connected:"+path)).orElse(false)).findFirst().orElseThrow();
     var output=display.getOutputEntries().stream().flatMap(Collection::stream).findFirst().orElseThrow();
     var recipe=me.shedaniel.rei.api.client.view.ViewSearchBuilder.builder().filterCategory(display.getCategoryIdentifier()).addRecipesFor(output);
     check(recipe.streamDisplays().findAny().isPresent(),"R finds Connected "+path);
     var input=display.getInputEntries().stream().flatMap(Collection::stream).findFirst().orElseThrow();
     var usage=me.shedaniel.rei.api.client.view.ViewSearchBuilder.builder().filterCategory(display.getCategoryIdentifier()).addUsagesFor(input);
     check(usage.streamDisplays().findAny().isPresent(),"U finds Connected "+path);
     pages.add(display);
    }
    phase=1;ticks=0;open(pages.getFirst());return;
   }
   if(Boolean.getBoolean("rei.probe.gravelOnly")){
    var gravel=registry.getAll().values().stream().flatMap(Collection::stream)
     .filter(d->d.getDisplayLocation().map(id->id.toString().equals("create:splashing/gravel")).orElse(false)).findFirst().orElseThrow();
    check(gravel.getOutputEntries().stream().flatMap(Collection::stream).anyMatch(entry->entry.getValue() instanceof net.minecraft.world.item.ItemStack stack&&stack.is(net.minecraft.world.item.Items.IRON_NUGGET)),"gravel washing has iron nugget as secondary output");
    var nuggets=me.shedaniel.rei.api.client.view.ViewSearchBuilder.builder().filterCategory(gravel.getCategoryIdentifier())
     .addRecipesFor(me.shedaniel.rei.api.common.util.EntryStacks.of(net.minecraft.world.item.Items.IRON_NUGGET));
    var ids=nuggets.streamDisplays().flatMap(d->d.provideInternalDisplayIds().stream()).map(Object::toString).toList();
    check(ids.contains("create:splashing/gravel"),"R on iron nugget finds gravel washing");
    report.append("Iron nugget washing results: "+ids+"\n");
    var usages=me.shedaniel.rei.api.client.view.ViewSearchBuilder.builder().filterCategory(gravel.getCategoryIdentifier())
     .addUsagesFor(me.shedaniel.rei.api.common.util.EntryStacks.of(net.minecraft.world.item.Items.GRAVEL));
    check(usages.streamDisplays().flatMap(d->d.provideInternalDisplayIds().stream()).anyMatch(id->id.toString().equals("create:splashing/gravel")),"U on gravel finds washing");
    check(usages.open(),"open gravel washing through U");pages.add(gravel);phase=1;ticks=0;return;
   }
   if(registry.getAll().containsKey(me.shedaniel.rei.api.common.category.CategoryIdentifier.of("create:crushing"))){
    if(Boolean.getBoolean("rei.probe.embedded"))check(!net.neoforged.fml.ModList.get().isLoaded("create_adapted_rei"),"integration belongs to Create, no separate adapter mod");
    var categories=Boolean.getBoolean("rei.probe.coreOnly")?List.of("create:crushing","create:mixing","create:filling","create:sequenced_assembly","create:mechanical_crafting"):List.of("create:crushing","create:mixing","create:filling","create:sequenced_assembly","create:mechanical_crafting","createaddition:rolling","createaddition:charging","createaddition:liquid_burning","ad_astra:compressing");
    for(String category:categories){
     var displays=registry.getAll().entrySet().stream().filter(entry->entry.getKey().toString().equals(category)).map(Map.Entry::getValue).findFirst().orElse(List.of());
     check(!displays.isEmpty(),"registered "+category);
     pages.add(category.equals("create:sequenced_assembly")?displays.stream().filter(d->d.getDisplayLocation().map(id->id.toString().equals("create:sequenced_assembly/precision_mechanism")).orElse(false)).findFirst().orElseThrow():displays.getFirst());
    }
    var all=registry.getAll().values().stream().flatMap(Collection::stream).toList();
    for(String namespace:Boolean.getBoolean("rei.probe.coreOnly")?List.<String>of():List.of("create_connected","handcrafted")){
     if(!net.neoforged.fml.ModList.get().isLoaded(namespace))continue;
     var display=all.stream().filter(d->d.getDisplayLocation().map(id->id.getNamespace().equals(namespace)).orElse(false))
      .filter(d->!d.getCategoryIdentifier().toString().startsWith("minecraft:plugins/tag"))
      .filter(d->!d.getOutputEntries().isEmpty()).findFirst().orElseThrow();
     check(!display.getOutputEntries().isEmpty(),"searchable recipe output "+namespace);pages.add(display);
    }
    var mechanical=pages.get(4);check(mechanical.getInputEntries().size()>9,"mechanical crafting grid larger than vanilla");
    for(String category:List.of("create:splashing","create:haunting","create:fan_smoking","create:fan_blasting")){
     var displays=registry.getAll().entrySet().stream().filter(entry->entry.getKey().toString().equals(category)).map(Map.Entry::getValue).findFirst().orElseThrow();
     check(!displays.isEmpty(),"fan category "+category);pages.add(displays.getFirst());
     if(category.equals("create:fan_blasting"))check(displays.stream().noneMatch(d->d.getDisplayLocation().map(id->id.toString().equals("minecraft:baked_potato")).orElse(false)),"lava fan excludes smoking food");
    }
    var mixing=registry.getAll().entrySet().stream().filter(entry->entry.getKey().toString().equals("create:mixing")).map(Map.Entry::getValue).findFirst().orElseThrow();
    for(String heat:List.of("HEATED","SUPERHEATED")){
     var heated=mixing.stream().filter(d->{try{
      var recipe=d.getClass().getField("processingRecipe").get(d);
      return recipe!=null&&recipe.getClass().getMethod("getRequiredHeat").invoke(recipe).toString().equals(heat);
     }catch(Exception e){throw new RuntimeException(e);}}).findFirst().orElseThrow();
     check(true,"mixer heat "+heat);pages.add(heated);
    }
    var crushing=registry.getAll().entrySet().stream().filter(entry->entry.getKey().toString().equals("create:crushing")).map(Map.Entry::getValue).findFirst().orElseThrow();
    pages.add(crushing.stream().max(Comparator.comparingInt(d->d.getOutputEntries().size())).orElseThrow());
    pages.addAll(manualPages);
    for(var page:pages){
     var source=page.getOutputEntries().isEmpty()?page.getInputEntries():page.getOutputEntries();
     var stack=source.stream().flatMap(Collection::stream).findFirst().orElseThrow();
     var search=me.shedaniel.rei.api.client.view.ViewSearchBuilder.builder();
     if(page.getOutputEntries().isEmpty())search.addUsagesFor(stack);else search.addRecipesFor(stack);
     check(search.buildMapInternal().keySet().stream().anyMatch(c->c.getCategoryIdentifier().equals(page.getCategoryIdentifier())),"recipe/usage search resolves "+page.getCategoryIdentifier());
     var input=page.getInputEntries().stream().flatMap(Collection::stream).findFirst().orElseThrow();
     var usage=me.shedaniel.rei.api.client.view.ViewSearchBuilder.builder().addUsagesFor(input);
     check(usage.buildMapInternal().keySet().stream().anyMatch(c->c.getCategoryIdentifier().equals(page.getCategoryIdentifier())),"usage search resolves "+page.getCategoryIdentifier());
    }
    phase=1;ticks=0;open(pages.getFirst());return;
   }
   done=true;mc.stop();
  }catch(Throwable t){t.printStackTrace();try{Files.writeString(Path.of("REI-FAIL.txt"),t.toString());}catch(Exception ignored){}done=true;mc.stop();}
 }
 void open(me.shedaniel.rei.api.common.display.Display display){
  var search=me.shedaniel.rei.api.client.view.ViewSearchBuilder.builder();
  if(Boolean.getBoolean("rei.probe.layoutCheck"))search.setPreferredOpenedCategory(display.getCategoryIdentifier());
  else search.filterCategory(display.getCategoryIdentifier());
  if(!display.getOutputEntries().isEmpty())search.addRecipesFor(display.getOutputEntries().stream().flatMap(Collection::stream).findFirst().orElseThrow());
  else search.addCategory(display.getCategoryIdentifier());
  check(search.open(),"open recipe "+display.getDisplayLocation().map(Object::toString).orElse(display.getCategoryIdentifier().toString()));
 }
 final class MenuProbeScreen extends net.minecraft.client.gui.screens.Screen {
  me.shedaniel.rei.impl.client.gui.modules.entries.SubMenuEntry submenu;
  int frames;
  MenuProbeScreen(){
   super(net.minecraft.network.chat.Component.literal("REI submenu regression"));
  }
  protected void init(){
   var text=new me.shedaniel.rei.impl.client.gui.modules.entries.TextMenuEntry(()->net.minecraft.network.chat.Component.literal("Nested entry"));
   var nested=new me.shedaniel.rei.impl.client.gui.modules.entries.SubMenuEntry(net.minecraft.network.chat.Component.literal("Nested submenu"),List.of(text));
   submenu=new me.shedaniel.rei.impl.client.gui.modules.entries.SubMenuEntry(net.minecraft.network.chat.Component.literal("Open submenu"),List.of(nested));
   submenu.setParent(new me.shedaniel.rei.impl.client.gui.modules.Menu(new me.shedaniel.math.Rectangle(80,80,120,16),List.of(submenu),false));
  }
  public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor extractor,int mouseX,int mouseY,float delta){
   if(done)return;
   try{
    var graphics=me.shedaniel.rei.api.client.gui.compat.GuiGraphics.of(extractor);
    // Exercise the exact failing constructor, restoration, and nested calls.
    var field=net.minecraft.client.gui.GuiGraphicsExtractor.class.getDeclaredField("scissorStack");field.setAccessible(true);var original=field.get(graphics);
    graphics.withFreshScissorStack(()->graphics.withFreshScissorStack(()->{}));
    check(field.get(graphics)==original,"nested scissor state restored");
    try{graphics.withFreshScissorStack(()->{throw new IllegalStateException("probe");});}catch(IllegalStateException expected){}
    check(field.get(graphics)==original,"scissor state restored after callback exception");
    submenu.updateInformation(80,80,true,true,true,120);
    submenu.render(graphics,85,85,delta);
    check(!submenu.getChildMenu().children().isEmpty(),"native submenu rendered");
    if(++frames>=10){
     net.minecraft.client.Screenshot.grab(Minecraft.getInstance(),false);
     Files.writeString(Path.of("REI-REPORT.txt"),"Native submenu frames: "+frames);
     Files.writeString(Path.of("REI-PASS.txt"),report+"Assertions: "+assertions);done=true;Minecraft.getInstance().stop();
    }
   }catch(Throwable t){t.printStackTrace();try{Files.writeString(Path.of("REI-FAIL.txt"),t.toString());}catch(Exception ignored){}done=true;Minecraft.getInstance().stop();}
  }
 }
}
