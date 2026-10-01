"""Apply known source-level 26.3 API migrations to the Connected branch."""
from pathlib import Path
import re
ROOT = Path(__file__).resolve().parents[1]
source = ROOT/'src/main/java/com/hlysine/create_connected'
def append_argument(text, call, argument):
    start = 0
    while (at := text.find(call, start)) >= 0:
        opening = at + len(call)-1
        depth, end = 1, opening+1
        while depth:
            depth += (text[end] == '(')-(text[end] == ')')
            end += 1
        if argument not in text[opening:end]:
            text = text[:end-1]+', '+argument+text[end-1:]
            end += len(argument)+2
        start = end
    return text
for file in source.rglob('*.java'):
    old = file.read_text()
    text = old.replace('DirtPathBlock', 'PathBlock').replace('RedStoneWireBlock', 'RedstoneWireBlock')
    text = text.replace('PushReaction.PUSH_ONLY', 'PushReaction.PUSH')
    text = text.replace('net.minecraft.world.level.storage.loot.providers.number.ConstantValue', 'net.minecraft.world.level.storage.loot.providers.number.floats.ConstantValue')
    text = text.replace('event.getLookupProvider()', 'event.getWorldLookupProvider()')
    if file.name != 'LinkedAnalogLeverRenderer.java': text = text.replace('.mulPose(', '.rotate(')
    text = append_argument(text, '.placeItemBackInInventory(', 'net.minecraft.util.Prediction.PREDICTED')
    text = text.replace('player.drop(leftover, false)', 'player.drop(leftover, false, net.minecraft.util.Prediction.PREDICTED)')
    text = text.replace('SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F)', 'SoundEvents.AXE_WAX_OFF.value(), SoundSource.BLOCKS, 1.0F, 1.0F)')
    text = text.replace('renderType(Font.DisplayMode.NORMAL, true)', 'renderType(Font.DisplayMode.NORMAL)')
    if 'protected boolean updateCustomBlockEntityTag(' in text:
        text = text.replace('extends BlockItem {', 'extends BlockItem implements com.simibubi.create.foundation.item.CustomBlockEntityTagItem {')
        text = text.replace('protected boolean updateCustomBlockEntityTag(', 'public boolean updateCustomBlockEntityTag(')
        text = re.sub(r'super\.updateCustomBlockEntityTag\((\w+), (\w+), (\w+), (\w+), (\w+)\)',
                      lambda m: 'BlockItem.updateCustomBlockEntityTag('+', '.join(m.group(i) for i in [2,3,1,4])+')', text)
    text = text.replace('(HolderLookup.Provider registries, net.minecraft.data.recipes.RecipeOutput output)',
        '(net.minecraft.data.worldgen.BootstrapContext<net.minecraft.world.item.crafting.Recipe<?>> registries, net.minecraft.data.worldgen.BootstrapContext<net.minecraft.advancements.Advancement> output)')
    if file.name in ['DashboardBlock.java', 'SequencedPulseGeneratorBlock.java']:
        text = re.sub(r'    public static final MapCodec<[^\n]+CODEC = simpleCodec\([^\n]+\);\n', '', text)
        text = re.sub(r'    @Override\n    protected @NotNull MapCodec<[^\n]+ codec\(\) \{\n        return CODEC;\n    }\n', '', text)
    if file.name == 'SimpleCCTrigger.java':
        text = text.replace('import net.minecraft.advancements.predicates.ContextAwarePredicate;', 'import net.minecraft.core.Holder;\nimport net.minecraft.world.level.storage.loot.predicates.LootItemCondition;')
        text = text.replace('ContextAwarePredicate', 'Holder<LootItemCondition>').replace('EntityPredicate.ADVANCEMENT_CODEC', 'LootItemCondition.CODEC')
    if old != text: file.write_text(text)
