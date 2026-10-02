package com.hlysine.create_connected.content.fluidvessel;

import java.util.List;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.model.CreateStandaloneModels;
import net.createmod.catnip.api.client.render.FluidRenderHelper;
import net.createmod.catnip.api.data.Iterate;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.TypedInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidVesselRenderer extends SafeBlockEntityRenderer<FluidVesselBlockEntity> {
    public FluidVesselRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(FluidVesselBlockEntity be, float partialTicks, PoseStack pose,
                              MultiBufferSource buffer, int light, int overlay) {
        // Minecraft 26.2 renders this block through extractRenderState/submit below.
    }

    @Override
    public BlockEntityRenderState createRenderState() {
        return new VesselRenderState();
    }

    @Override
    public void extractRenderState(FluidVesselBlockEntity be, BlockEntityRenderState renderState,
                                   float partialTicks, Vec3 cameraPos,
                                   ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, renderState, crumblingOverlay);
        if (!(renderState instanceof VesselRenderState state)) return;
        state.controller = be.isController();
        if (!state.controller) return;
        state.window = be.hasWindow();
        state.boilerActive = be.boiler.isActive();
        state.width = be.getWidth();
        state.length = be.getHeight();
        state.axis = be.getAxis();
        var fluidLevel = be.getFluidLevel();
        state.level = fluidLevel == null ? 0 : fluidLevel.getValue(partialTicks);
        state.fluid = be.getTankInventory().getFluid().copy();
        state.lighterThanAir = !state.fluid.isEmpty() && state.fluid.getFluid().getFluidType().isLighterThanAir();
        state.gauge = be.boiler.gauge.getValue(partialTicks);
        state.occluded = be.boiler.occludedDirections.clone();
    }

    @Override
    public void submit(BlockEntityRenderState renderState, PoseStack pose, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        if (!(renderState instanceof VesselRenderState state) || !state.controller) return;
        if (state.window)
            submitFluid(state, pose, collector);
        else if (state.boilerActive)
            submitBoiler(state, pose, collector);
    }

    private static void submitFluid(VesselRenderState state, PoseStack pose, SubmitNodeCollector collector) {
        float cap = 1 / 4f;
        float hull = 1 / 16f + 1 / 128f;
        float puddle = 1 / 16f;
        float totalHeight = state.width - 2 * hull - puddle;
        if (totalHeight <= 0 || state.fluid.isEmpty() || state.level < 1 / (512f * totalHeight)) return;
        float filledHeight = Mth.clamp(state.level * totalHeight, 0, totalHeight);
        float xMin = state.axis == Axis.X ? cap : hull;
        float xMax = state.axis == Axis.X ? state.length - cap : state.width - hull;
        float zMin = state.axis == Axis.Z ? cap : hull;
        float zMax = state.axis == Axis.Z ? state.length - cap : state.width - hull;
        float yMin = hull + puddle + (state.lighterThanAir ? totalHeight - filledHeight : 0);
        FluidRenderHelper.submitFluidBox(collector, (TypedInstance<Fluid>) state.fluid,
            xMin, yMin, zMin, xMax, yMin + filledHeight, zMax, pose, state.lightCoords, false, true);
    }

    private static void submitBoiler(VesselRenderState state, PoseStack pose, SubmitNodeCollector collector) {
        // Resolve every frame through the current manager to respect resource reloads.
        var manager = Minecraft.getInstance().getModelManager();
        var gauge = manager.getStandaloneModel(CreateStandaloneModels.BOILER_GAUGE);
        var dial = manager.getStandaloneModel(CreateStandaloneModels.BOILER_GAUGE_DIAL);
        if (gauge == null || dial == null) return;
        pose.pushPose();
        pose.translate(state.axis == Axis.X ? state.length / 2f : state.width / 2f,
            0.5, state.axis == Axis.Z ? state.length / 2f : state.width / 2f);
        for (Direction direction : Iterate.horizontalDirections) {
            if (direction.getAxis() != state.axis || state.occluded[direction.get2DDataValue()]) continue;
            pose.pushPose();
            pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-direction.toYRot() - 90));
            pose.translate(-.5f, -.5f, -.5f);
            pose.translate(state.width / 2f - 6 / 16f, 0, 0);
            collector.submitBlockModel(pose, RenderTypes.cutoutMovingBlock(), List.of(gauge),
                BlockModelRenderState.EMPTY_TINTS, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            pose.translate(0, 6f / 16, 8f / 16);
            pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-145 * state.gauge + 90));
            pose.translate(0, -6f / 16, -8f / 16);
            collector.submitBlockModel(pose, RenderTypes.cutoutMovingBlock(), List.of(dial),
                BlockModelRenderState.EMPTY_TINTS, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true; // A multiblock's controller may be outside the visible block bounds.
    }

    private static class VesselRenderState extends BlockEntityRenderState {
        boolean controller, window, boilerActive, lighterThanAir;
        int width, length;
        Axis axis = Axis.X;
        float level, gauge;
        FluidStack fluid = FluidStack.EMPTY;
        boolean[] occluded = new boolean[4];
    }
}
