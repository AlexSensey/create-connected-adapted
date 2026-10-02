package com.hlysine.create_connected.content.shearpin;

import com.hlysine.create_connected.CreateConnected;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;

import java.util.List;

/** The renderer owns both the rotating pin and the stationary bracket. */
@EventBusSubscriber(modid = CreateConnected.MODID, value = Dist.CLIENT)
public class ShearPinModel extends DelegateBlockStateModel {
    public ShearPinModel(BlockStateModel delegate) {
        super(delegate);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        event.getBakingResult().blockStateModels().replaceAll((state, model) ->
                state.getBlock() instanceof ShearPinBlock && !(model instanceof ShearPinModel)
                        ? new ShearPinModel(model) : model);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
                             List<BlockStateModelPart> parts) {
        // Suppress the stationary world mesh. The inherited context-free
        // collectParts still exposes the pin geometry to the kinetic renderer.
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return this;
    }
}
