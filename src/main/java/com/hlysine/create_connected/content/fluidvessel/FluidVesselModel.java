package com.hlysine.create_connected.content.fluidvessel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.hlysine.create_connected.CreateConnected;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;

@EventBusSubscriber(modid = CreateConnected.MODID, value = Dist.CLIENT)
public class FluidVesselModel extends DelegateBlockStateModel {
    public FluidVesselModel(BlockStateModel delegate) {
        super(delegate);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        // Create applies the registered connected textures first; cull their resulting parts.
        event.getBakingResult().blockStateModels().replaceAll((state, model) ->
            state.getBlock() instanceof FluidVesselBlock && !(model instanceof FluidVesselModel)
                ? new FluidVesselModel(model) : model);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
                             List<BlockStateModelPart> parts) {
        List<BlockStateModelPart> originals = new ArrayList<>();
        super.collectParts(level, pos, state, random, originals);
        int mask = connectedFaces(level, pos, state);
        for (BlockStateModelPart part : originals)
            parts.add(new VesselPart(part, mask));
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return Arrays.asList(super.createGeometryKey(level, pos, state, random), connectedFaces(level, pos, state));
    }

    private static int connectedFaces(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        int mask = 0;
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() == state.getValue(FluidVesselBlock.AXIS)) continue;
            if (ConnectivityHandler.isConnected(level, pos, pos.relative(direction)))
                mask |= 1 << direction.get3DDataValue();
        }
        return mask;
    }

    private static final class VesselPart implements BlockStateModelPart {
        private final BlockStateModelPart delegate;
        private final List<BakedQuad> quads;

        VesselPart(BlockStateModelPart delegate, int mask) {
            this.delegate = delegate;
            List<BakedQuad> combined = new ArrayList<>();
            for (Direction direction : Direction.values())
                if ((mask & (1 << direction.get3DDataValue())) == 0)
                    combined.addAll(delegate.getQuads(direction));
            combined.addAll(delegate.getQuads(null));
            quads = List.copyOf(combined);
        }

        @Override public List<BakedQuad> getQuads(Direction side) { return side == null ? quads : List.of(); }
        @Override public boolean useAmbientOcclusion() { return delegate.useAmbientOcclusion(); }
        @Override public Material.Baked particleMaterial() { return delegate.particleMaterial(); }
        @Override public int materialFlags() { return delegate.materialFlags(); }
    }
}