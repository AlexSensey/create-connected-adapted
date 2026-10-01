package com.hlysine.create_connected.content.copycat;

import com.hlysine.create_connected.CreateConnected;
import com.hlysine.create_connected.content.copycat.block.CopycatBlockModel;
import com.simibubi.create.content.decoration.copycat.CopycatBlockClientExtensions;
import com.hlysine.create_connected.content.copycat.beam.CopycatBeamModel;
import com.hlysine.create_connected.content.copycat.beam.CopycatBeamBlock;
import com.hlysine.create_connected.content.copycat.board.CopycatBoardModel;
import com.hlysine.create_connected.content.copycat.board.CopycatBoardBlock;
import com.hlysine.create_connected.content.copycat.fence.CopycatFenceModel;
import com.hlysine.create_connected.content.copycat.fence.CopycatFenceBlock;
import com.hlysine.create_connected.content.copycat.fencegate.CopycatFenceGateModel;
import com.hlysine.create_connected.content.copycat.fencegate.CopycatFenceGateBlock;
import com.hlysine.create_connected.content.copycat.slab.CopycatSlabModel;
import com.hlysine.create_connected.content.copycat.slab.CopycatSlabBlock;
import com.hlysine.create_connected.content.copycat.stairs.CopycatStairsModel;
import com.hlysine.create_connected.content.copycat.stairs.CopycatStairsBlock;
import com.hlysine.create_connected.content.copycat.verticalstep.CopycatVerticalStepModel;
import com.hlysine.create_connected.content.copycat.verticalstep.CopycatVerticalStepBlock;
import com.hlysine.create_connected.content.copycat.wall.CopycatWallModel;
import com.hlysine.create_connected.content.copycat.wall.CopycatWallBlock;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = CreateConnected.MODID, value = Dist.CLIENT)
public abstract class ConnectedCopycatModel extends CopycatBlockModel {
    protected ConnectedCopycatModel(BlockStateModel delegate) { super(delegate); }

    protected abstract List<BakedQuad> getCroppedQuads(BlockState state, List<BakedQuad> templateQuads);

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerModels(ModelEvent.ModifyBakingResult event) {
        event.getBakingResult().blockStateModels().replaceAll((state, model) ->
                model instanceof ConnectedCopycatModel ? model : wrap(state.getBlock(), model));
    }

    private static BlockStateModel wrap(Block block, BlockStateModel model) {
        if (block instanceof CopycatBeamBlock) return new CopycatBeamModel(model);
        if (block instanceof CopycatBoardBlock) return new CopycatBoardModel(model);
        if (block instanceof CopycatFenceBlock) return new CopycatFenceModel(model);
        if (block instanceof CopycatFenceGateBlock) return new CopycatFenceGateModel(model);
        if (block instanceof CopycatSlabBlock) return new CopycatSlabModel(model);
        if (block instanceof CopycatStairsBlock) return new CopycatStairsModel(model);
        if (block instanceof CopycatVerticalStepBlock) return new CopycatVerticalStepModel(model);
        if (block instanceof CopycatWallBlock) return new CopycatWallModel(model);
        return model;
    }

    @SubscribeEvent
    public static void registerShapeClientExtensions(RegisterClientExtensionsEvent event) {
        var extensions = new CopycatBlockClientExtensions();
        for (Block block : net.minecraft.core.registries.BuiltInRegistries.BLOCK)
            if (block instanceof ICopycatWithWrappedBlock && !(block instanceof com.hlysine.create_connected.content.copycat.block.CopycatBlockBlock)
                    && block instanceof com.simibubi.create.content.decoration.copycat.CopycatBlock)
                event.registerBlock(extensions, block);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
                             List<BlockStateModelPart> parts) {
        MaterialModel material = materialModel(level, pos);
        if (material == null) {
            delegate.collectParts(level, pos, state, random, parts);
            return;
        }
        List<BlockStateModelPart> originals = new ArrayList<>();
        material.model().collectParts(level, pos, material.state(), random, originals);
        for (BlockStateModelPart original : originals) {
            List<BakedQuad> template = new ArrayList<>();
            for (Direction direction : Direction.values()) template.addAll(original.getQuads(direction));
            template.addAll(original.getQuads(null));
            parts.add(new CroppedPart(original, List.copyOf(getCroppedQuads(state, template))));
        }
    }

    private record CroppedPart(BlockStateModelPart delegate, List<BakedQuad> quads) implements BlockStateModelPart {
        // Cropped faces can lie inside the block; they must not retain the source cube's boundary cull face.
        @Override public List<BakedQuad> getQuads(Direction side) { return side == null ? quads : List.of(); }
        @Override public boolean useAmbientOcclusion() { return delegate.useAmbientOcclusion(); }
        @Override public Material.Baked particleMaterial() { return delegate.particleMaterial(); }
        @Override public int materialFlags() { return delegate.materialFlags(); }
    }
}
