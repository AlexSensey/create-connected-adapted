package com.hlysine.create_connected.datagen;

import com.hlysine.create_connected.CreateConnected;
import com.hlysine.create_connected.compat.Mods;
import com.hlysine.create_connected.registries.CCBlocks;
import com.simibubi.create.AllTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public class CCTagGen {
    public static void addGenerators(GatherDataEvent.Server event) {
        PackOutput output = event.getGenerator().getPackOutput();
        event.addProvider(new ConnectedBlockTags(output, event.getLookupProvider()));
        event.addProvider(new ItemTags(output, event.getLookupProvider()));
    }

    private static class ConnectedBlockTags extends net.minecraft.data.tags.KeyTagProvider<Block> {
        ConnectedBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
            super(output, Registries.BLOCK, lookup, CreateConnected.MODID);
        }

        @Override
        protected void addTags(HolderLookup.Provider lookup) {
            tag(BlockTags.create(Mods.DIAGONAL_FENCES.rl("non_diagonal_fences")))
                    .add(CCBlocks.COPYCAT_FENCE.getKey())
                    .add(CCBlocks.WRAPPED_COPYCAT_FENCE.getKey());
            tag(BlockTags.create(Mods.DREAMS_DESIRES.rl("fan_processing_catalysts/freezing")))
                    .add(CCBlocks.FAN_FREEZING_CATALYST.getKey());
            tag(BlockTags.create(Mods.DREAMS_DESIRES.rl("fan_processing_catalysts/seething")))
                    .add(CCBlocks.FAN_SEETHING_CATALYST.getKey());
            tag(BlockTags.create(Mods.DREAMS_DESIRES.rl("fan_processing_catalysts/sanding")))
                    .add(CCBlocks.FAN_SANDING_CATALYST.getKey());
            tag(BlockTags.create(Mods.DREAMS_DESIRES.rl("fan_processing_catalysts_freezing")))
                    .add(CCBlocks.FAN_FREEZING_CATALYST.getKey());
            tag(BlockTags.create(Mods.DREAMS_DESIRES.rl("fan_processing_catalysts_superheating")))
                    .add(CCBlocks.FAN_SEETHING_CATALYST.getKey());
            tag(BlockTags.create(Mods.DREAMS_DESIRES.rl("fan_processing_catalysts_sanding")))
                    .add(CCBlocks.FAN_SANDING_CATALYST.getKey());
            tag(BlockTags.create(Mods.GARNISHED.rl("fan_processing_catalysts/freezing")))
                    .add(CCBlocks.FAN_FREEZING_CATALYST.getKey());
            tag(BlockTags.create(Mods.NUCLEAR.rl("fan_processing_catalysts/enriched")))
                    .add(CCBlocks.FAN_ENRICHED_CATALYST.getKey());
            tag(BlockTags.create(Mods.DRAGONS_PLUS.rl("passive_block_freezers")))
                    .add(CCBlocks.FAN_FREEZING_CATALYST.getKey());
            tag(BlockTags.create(Mods.DRAGONS_PLUS.rl("fan_processing_catalysts/sanding")))
                    .add(CCBlocks.FAN_SANDING_CATALYST.getKey());
            tag(BlockTags.create(Mods.DRAGONS_PLUS.rl("fan_processing_catalysts/ending")))
                    .add(CCBlocks.FAN_ENDING_CATALYST_DRAGONS_BREATH.getKey())
                    .add(CCBlocks.FAN_ENDING_CATALYST_DRAGON_HEAD.getKey());
            tag(BlockTags.create(Mods.MORE_CATALYSTS.rl("fan_catalysts/chocolate_coating")))
                    .add(CCBlocks.FAN_CHOCOLATE_COATING_CATALYST.getKey());
            tag(BlockTags.create(Mods.MORE_CATALYSTS.rl("fan_catalysts/honey_coating")))
                    .add(CCBlocks.FAN_HONEY_COATING_CATALYST.getKey());
            tag(BlockTags.create(Mods.MORE_CATALYSTS.rl("fan_catalysts/exploding")))
                    .add(CCBlocks.FAN_EXPLODING_CATALYST.getKey());
            tag(BlockTags.create(Mods.MORE_CATALYSTS.rl("fan_catalysts/resonance")))
                    .add(CCBlocks.FAN_RESONANCE_CATALYST.getKey());
            tag(BlockTags.create(Mods.MORE_CATALYSTS.rl("fan_catalysts/sculking")))
                    .add(CCBlocks.FAN_SCULKING_CATALYST.getKey());
            tag(BlockTags.create(Mods.MORE_CATALYSTS.rl("fan_catalysts/purifying")))
                    .add(CCBlocks.FAN_PURIFYING_CATALYST.getKey());
            tag(BlockTags.create(Mods.SHIMMER.rl("fan_transmutation_catalysts")))
                    .add(CCBlocks.FAN_TRANSMUTATION_CATALYST.getKey());
            tag(BlockTags.create(Mods.SHIMMER.rl("fan_glooming_catalysts")))
                    .add(CCBlocks.FAN_GLOOMING_CATALYST.getKey());
            tag(BlockTags.create(Mods.NETHER_INDUSTRY.rl("fan_soul_stripping_catalysts")))
                    .add(CCBlocks.FAN_SOUL_STRIPPING_CATALYST.getKey());
            CCBlocks.FAN_DYEING_CATALYSTS.forEach((color, block) -> {
                tag(BlockTags.create(Mods.GARNISHED.rl("fan_processing_catalysts/dye/" + color.getName())))
                        .addOptional(block.getKey());
                tag(BlockTags.create(Identifier.fromNamespaceAndPath("c", "dyes/" + color.getName())))
                        .addOptional(block.getKey());
                tag(BlockTags.create(Identifier.fromNamespaceAndPath("c", "dyes")))
                        .addOptional(block.getKey());
            });
        }
    }

    private static class ItemTags extends net.minecraft.data.tags.KeyTagProvider<Item> {
        ItemTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
            super(output, Registries.ITEM, lookup, CreateConnected.MODID);
        }

        @Override
        protected void addTags(HolderLookup.Provider lookup) {
            tag(AllTags.AllItemTags.CONTRAPTION_CONTROLLED.tag)
                    .add(Items.JUKEBOX.builtInRegistryHolder().key(), Items.NOTE_BLOCK.builtInRegistryHolder().key());
        }
    }
}
