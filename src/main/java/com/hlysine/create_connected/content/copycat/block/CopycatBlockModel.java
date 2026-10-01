package com.hlysine.create_connected.content.copycat.block;

import com.hlysine.create_connected.CreateConnected;
import com.hlysine.create_connected.registries.CCBlocks;
import com.simibubi.create.content.decoration.copycat.CopycatBlockClientExtensions;
import com.simibubi.create.content.decoration.copycat.CopycatBlockEntity;
import com.simibubi.create.content.decoration.copycat.CopycatModelData;
import com.simibubi.create.AllBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;

import java.util.List;

/** Full cubes can forward the material parts without modifying their geometry. */
@EventBusSubscriber(modid = CreateConnected.MODID, value = Dist.CLIENT)
public class CopycatBlockModel extends DelegateBlockStateModel {
    public CopycatBlockModel(BlockStateModel originalModel) {
        super(originalModel);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        event.getBakingResult().blockStateModels().replaceAll((state, model) ->
                state.getBlock() instanceof CopycatBlockBlock && !(model instanceof CopycatBlockModel)
                        ? new CopycatBlockModel(model) : model);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerBlock(new CopycatBlockClientExtensions(), CCBlocks.COPYCAT_BLOCK.get());
    }

    protected static MaterialModel materialModel(BlockAndTintGetter level, BlockPos pos) {
        // Chunk rendering uses a model-data snapshot, which need not expose live block entities.
        BlockState material = level.getModelData(pos).get(CopycatModelData.MATERIAL_PROPERTY);
        if (material == null && level.getBlockEntity(pos) instanceof CopycatBlockEntity copycat)
            material = copycat.getMaterial();
        if (material == null)
            material = AllBlocks.COPYCAT_BASE.getDefaultState();
        if (material.isAir())
            return null;
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(material);
        // Guard malformed saved materials from recursing into another full cube.
        return model == null || model instanceof CopycatBlockModel ? null : new MaterialModel(material, model);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
                             List<BlockStateModelPart> parts) {
        MaterialModel material = materialModel(level, pos);
        if (material == null) {
            super.collectParts(level, pos, state, random, parts);
            return;
        }
        material.model.collectParts(level, pos, material.state, random, parts);
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        MaterialModel material = materialModel(level, pos);
        if (material == null)
            return super.createGeometryKey(level, pos, state, random);
        return new MaterialGeometryKey(state, material.state,
                material.model.createGeometryKey(level, pos, material.state, random));
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        MaterialModel material = materialModel(level, pos);
        return material == null ? super.particleMaterial(level, pos, state)
                : material.model.particleMaterial(level, pos, material.state);
    }

    @Override
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        MaterialModel material = materialModel(level, pos);
        return material == null ? super.materialFlags(level, pos, state)
                : material.model.materialFlags(level, pos, material.state);
    }

    protected record MaterialModel(BlockState state, BlockStateModel model) {}
    private record MaterialGeometryKey(BlockState copycat, BlockState material, Object geometry) {}
}
