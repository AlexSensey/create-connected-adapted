package com.hlysine.create_connected.content.fancatalyst;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.SkullBlock;
import org.joml.Vector3f;

public enum SkullTypes{
    DRAGON(ModelLayers.DRAGON_SKULL, SkullBlock.Types.DRAGON, new Vector3f(0.5F, 0.25F, 0.5F), new Vector3f(-0.5F, -0.5F, 0.5F)),
    CREEPER(ModelLayers.CREEPER_HEAD, SkullBlock.Types.CREEPER, new Vector3f(0.5F, 0.25F, 0.5F), new Vector3f(-1F, -1F, 1F)),
    ;


    private final ModelLayerLocation modelLayer;
    private final SkullBlock.Type texture;
    private final Vector3f translation;
    private final Vector3f scale;

    SkullTypes(ModelLayerLocation modelLayer, SkullBlock.Type texture,
               Vector3f translation, Vector3f scale) {


        this.modelLayer = modelLayer;
        this.texture = texture;
        this.translation = translation;
        this.scale = scale;
    }

    public SkullModelBase createModel(BlockEntityRendererProvider.Context context) {
        return SkullBlockRenderer.createModel(context.entityModelSet(), texture);
    }

    public ModelLayerLocation getModelLayer() {
        return modelLayer;
    }

    public SkullBlock.Type getTexture() {
        return this.texture;
    }

    public void translate(PoseStack poseStack) {
        poseStack.translate(this.translation.x, this.translation.y, this.translation.z);
    }

    public void scale(PoseStack poseStack) {
        poseStack.scale(this.scale.x(), this.scale.y, this.scale.z);
    }
}
