package com.hlysine.create_connected.content.fancatalyst;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class FanCatalystRotatingHeadRenderer extends SafeBlockEntityRenderer<FanCatalystRotatingHeadBlockEntity> {
    private final SkullTypes skullType;
    private final SkullModelBase model;

    public FanCatalystRotatingHeadRenderer(SkullTypes skullType, BlockEntityRendererProvider.Context context) {
        this.skullType = skullType;
        this.model = skullType.createModel(context);
    }

    public static FanCatalystRotatingHeadRenderer creeper(BlockEntityRendererProvider.Context context) {
        return new FanCatalystRotatingHeadRenderer(SkullTypes.CREEPER, context);
    }

    public static FanCatalystRotatingHeadRenderer dragon(BlockEntityRendererProvider.Context context) {
        return new FanCatalystRotatingHeadRenderer(SkullTypes.DRAGON, context);
    }

    public RenderType getRenderType() {
        Identifier resourcelocation = SkullBlockRenderer.SKIN_BY_TYPE.get(skullType.getTexture());
        return SkullBlockRenderer.getSkullRenderType(skullType.getTexture(), resourcelocation);
    }

    @Override
    protected void renderSafe(FanCatalystRotatingHeadBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource bufferSource, int light, int overlay) {
        // Rendering uses the 26.2 submission API below.
    }

    @Override
    public BlockEntityRenderState createRenderState() {
        return new HeadRenderState();
    }

    @Override
    public void extractRenderState(FanCatalystRotatingHeadBlockEntity be, BlockEntityRenderState state,
                                   float partialTicks, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderState.extractBase(be, state, overlay);
        HeadRenderState head = (HeadRenderState) state;
        long ticks = be.hasLevel() ? be.getLevel().getGameTime() : 0;
        head.modelState.yRot = (ticks % 360 + partialTicks) % 360;
        head.modelState.xRot = 0;
        head.modelState.animationPos = 0;
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack ms, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        HeadRenderState head = (HeadRenderState) state;
        ms.pushPose();
        skullType.translate(ms);
        skullType.scale(ms);
        collector.submitModel(model, head.modelState, ms, getRenderType(), state.lightCoords,
                OverlayTexture.NO_OVERLAY, -1);
        ms.popPose();
    }

    private static class HeadRenderState extends BlockEntityRenderState {
        final SkullModelBase.State modelState = new SkullModelBase.State();
    }
}
