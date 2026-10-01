"""Production event handler regression with modeled world/player collaborators."""
from pathlib import Path
import re, subprocess
root = Path(__file__).resolve().parents[1]
out = root / 'build/api-check-26.2/catalyst-bucket-test'
out.mkdir(parents=True, exist_ok=True)
source = (root / 'src/main/java/com/hlysine/create_connected/content/fancatalyst/FanCatalystInteraction.java').read_text()
source = re.sub(r'(?m)^(?:package|import) .*;\n', '', source)
source = source.replace('public final class FanCatalystInteraction', 'static final class FanCatalystInteraction')
java = '''
public class CatalystBucketCheck {
 static int checks;
 static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 static class Fluids {static final Object EMPTY=new Object(),WATER=new Object(),LAVA=new Object();}
 static class BucketItem {Object content;BucketItem(Object c){content=c;}}
 static class Items {
  static final Object NETHERRACK=new Object(),SOUL_SAND=new Object();
  static final BucketItem BUCKET=new BucketItem(Fluids.EMPTY),WATER_BUCKET=new BucketItem(Fluids.WATER),
   LAVA_BUCKET=new BucketItem(Fluids.LAVA);
 }
 static class ItemStack {
  Object item;ItemStack(Object i){item=i;}Object getItem(){return item;}boolean is(Object i){return item==i;}
 }
 static class Entry {Object block=new Object();Object get(){return block;}State getDefaultState(){return new State(block);}}
 static class CCBlocks {
  static Entry EMPTY_FAN_CATALYST=new Entry(),FAN_SPLASHING_CATALYST=new Entry(),FAN_BLASTING_CATALYST=new Entry();
 }
 record State(Object block){boolean is(Object b){return block==b;}}
 static class Player {
  boolean spectator,build=true;int writes;Object lastHand;ItemStack lastStack;
  boolean isSpectator(){return spectator;}boolean mayBuild(){return build;}
  void setItemInHand(Object hand,ItemStack stack){writes++;lastHand=hand;lastStack=stack;}
 }
 static class Level {
  State state;boolean client,allowed=true,setSucceeds=true;int updates,sounds,events;
  boolean isClientSide(){return client;}boolean mayInteract(Player p,Object pos){return allowed;}
  State getBlockState(Object pos){return state;}
  boolean setBlock(Object p,State s,int f){if(!setSucceeds)return false;state=s;updates++;return true;}
  void playSound(Object p,Object pos,Object sound,Object source,int a,int b){sounds++;}
  void gameEvent(Player p,Object e,Object pos){events++;}
 }
 static class ItemUtils {
  static int calls;static ItemStack lastInput;
  static ItemStack createFilledResult(ItemStack held,Player p,ItemStack result){calls++;lastInput=held;return result;}
 }
 static class Block{static final int UPDATE_ALL=3;}
 static class SoundEvents{static final Object BUCKET_FILL=new Object(),BUCKET_FILL_LAVA=new Object();}
 static class SoundSource{static final Object BLOCKS=new Object();}
 static class GameEvent{static final Object FLUID_PICKUP=new Object();}
 static class InteractionResult{static final Object SUCCESS=new Object();}
 static class PlayerInteractEvent {
  static class RightClickBlock {
   Level level=new Level();Player player=new Player();ItemStack held;boolean canceled;Object result,hand;
   Level getLevel(){return level;}Player getEntity(){return player;}Object getPos(){return this;}
   boolean isCanceled(){return canceled;}ItemStack getItemStack(){return held;}Object getHand(){return hand;}
   void setCanceled(boolean c){canceled=c;}void setCancellationResult(Object r){result=r;}
  }
 }
 static PlayerInteractEvent.RightClickBlock event(Entry block,Object item,boolean client,Object hand){
  var e=new PlayerInteractEvent.RightClickBlock();e.level.state=block.getDefaultState();
  e.level.client=client;e.held=new ItemStack(item);e.hand=hand;return e;
 }
'''+source+'''
 public static void main(String[] args){
  for(Object material:new Object[]{Items.NETHERRACK,Items.SOUL_SAND}) {
   var client=event(CCBlocks.EMPTY_FAN_CATALYST,material,true,"main");
   FanCatalystInteraction.onRightClickBlock(client);
   check(client.canceled&&client.result==InteractionResult.SUCCESS,"material input suppresses predicted block placement");
   check(client.level.updates==0&&client.player.writes==0,"material transform remains server-owned");
   var server=event(CCBlocks.EMPTY_FAN_CATALYST,material,false,"main");
   FanCatalystInteraction.onRightClickBlock(server);
   check(!server.canceled,"material server recipe is not blocked");
   var ordinary=event(new Entry(),material,true,"main");
   FanCatalystInteraction.onRightClickBlock(ordinary);
   check(!ordinary.canceled,"material placement on other blocks unchanged");
  }
  for(Object bucket:new Object[]{Items.WATER_BUCKET,Items.LAVA_BUCKET,new BucketItem(new Object())}){
   var client=event(CCBlocks.EMPTY_FAN_CATALYST,bucket,true,"main");
   FanCatalystInteraction.onRightClickBlock(client);
   check(client.canceled&&client.result==InteractionResult.SUCCESS,"client prevents ordinary bucket packet");
   check(client.level.updates==0&&client.player.writes==0,"client does not apply recipe");
   var server=event(CCBlocks.EMPTY_FAN_CATALYST,bucket,false,"main");
   FanCatalystInteraction.onRightClickBlock(server);
   check(!server.canceled&&server.level.updates==0,"server leaves filling to Create recipe");
  }
  for(Entry block:new Entry[]{CCBlocks.FAN_SPLASHING_CATALYST,CCBlocks.FAN_BLASTING_CATALYST})
   for(Object hand:new Object[]{"main","off"}){
    for(boolean clientSide:new boolean[]{false,true}){
     var full=event(block,Items.WATER_BUCKET,clientSide,hand);FanCatalystInteraction.onRightClickBlock(full);
     check(full.canceled&&full.level.updates==0,"repeat filled bucket cannot pour on full catalyst");
    }
    var client=event(block,Items.BUCKET,true,hand);FanCatalystInteraction.onRightClickBlock(client);
    check(client.canceled&&client.player.writes==0&&client.level.updates==0,"pickup client consumes only");
    var server=event(block,Items.BUCKET,false,hand);int calls=ItemUtils.calls;
    FanCatalystInteraction.onRightClickBlock(server);
    check(server.canceled&&server.level.state.is(CCBlocks.EMPTY_FAN_CATALYST.get()),"pickup empties catalyst");
    check(server.player.lastHand==hand&&server.player.lastStack.item==
     (block==CCBlocks.FAN_SPLASHING_CATALYST?Items.WATER_BUCKET:Items.LAVA_BUCKET),"correct bucket and hand");
    check(ItemUtils.calls==calls+1&&ItemUtils.lastInput==server.held,"native stack/creative/inventory utility receives held stack");
    FanCatalystInteraction.onRightClickBlock(server);
    check(server.level.updates==1&&server.player.writes==1,"repeat cannot duplicate contents");
   }
  var failed=event(CCBlocks.FAN_SPLASHING_CATALYST,Items.BUCKET,false,"main");failed.level.setSucceeds=false;
  FanCatalystInteraction.onRightClickBlock(failed);
  check(failed.player.writes==0&&failed.level.sounds==0,"failed block update cannot award bucket");
  for(int guard=0;guard<4;guard++){
   var e=event(CCBlocks.FAN_SPLASHING_CATALYST,Items.BUCKET,false,"main");
   switch(guard){case 0:e.canceled=true;break;case 1:e.player.spectator=true;break;
    case 2:e.player.build=false;break;case 3:e.level.allowed=false;break;}
   FanCatalystInteraction.onRightClickBlock(e);check(e.level.updates==0&&e.player.writes==0,"permission/cancel guard");
  }
  var unrelated=event(new Entry(),Items.WATER_BUCKET,true,"main");FanCatalystInteraction.onRightClickBlock(unrelated);
  check(!unrelated.canceled,"ordinary bucket use on other blocks preserved");
  System.out.println("PASS: "+checks+" catalyst bucket checks (production handler; modeled world/player)");
 }
}
'''
path=out/'CatalystBucketCheck.java';path.write_text(java)
for cmd in [['C:/Java/jdk-25.0.2/bin/javac.exe','-d',str(out),str(path)],
            ['C:/Java/jdk-25.0.2/bin/java.exe','-cp',str(out),'CatalystBucketCheck']]:
    result=subprocess.run(cmd,capture_output=True,text=True)
    print((result.stdout+result.stderr)[-2000:])
    if result.returncode:raise SystemExit(result.returncode)
