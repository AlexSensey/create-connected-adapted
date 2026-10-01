package com.hlysine.create_connected.registries;

import com.hlysine.create_connected.CreateConnected;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import java.util.List;

@EventBusSubscriber(modid = CreateConnected.MODID, value = Dist.CLIENT)
public class CCColorHandlers {
    @SubscribeEvent
    public static void registerBlockTints(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(new BlockTintSource() {
            @Override
            public int color(BlockState state) {
                return -1;
            }

            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                return level != null && pos != null ? BiomeColors.getAverageWaterColor(level, pos) : -1;
            }
        }), CCBlocks.FAN_SPLASHING_CATALYST.get());
    }

    @SubscribeEvent
    public static void registerItemTints(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(CreateConnected.asResource("water"), WaterItemTint.CODEC);
    }

    public record WaterItemTint() implements ItemTintSource {
        public static final MapCodec<WaterItemTint> CODEC = MapCodec.unit(new WaterItemTint());

        @Override
        public int calculate(ItemStack stack, ClientLevel level, LivingEntity entity) {
            // Resolve the current fluid model so resource reloads and other mods'
            // water tint registrations are reflected in the inventory model.
            var water = Fluids.WATER.defaultFluidState();
            return Minecraft.getInstance().getModelManager().getFluidStateModelSet()
                    .get(water).fluidTintSource().color(water);
        }

        @Override
        public MapCodec<? extends ItemTintSource> type() {
            return CODEC;
        }
    }
}
