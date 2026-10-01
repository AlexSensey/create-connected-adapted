package com.hlysine.create_connected.content.kineticbattery;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.model.CreateStandaloneModels;
import com.simibubi.create.foundation.render.CreateVisualizationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import java.util.List;

public class KineticBatteryRenderer extends KineticBlockEntityRenderer<KineticBatteryBlockEntity> {
    public KineticBatteryRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack pose, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        if (!(state instanceof KineticRenderState kinetic)
                || !(kinetic.blockEntity instanceof KineticBatteryBlockEntity be) || isInvalid(be)) return;
        if (CreateVisualizationManager.supportsVisualization(be.getLevel())) return;

        // Resolve from the current model manager so resource reloads cannot leave stale geometry.
        var shaft = Minecraft.getInstance().getModelManager().getStandaloneModel(CreateStandaloneModels.SHAFT_HALF);
        if (shaft == null) return;
        var parts = List.of(shaft);
        Direction.Axis boxAxis = getRotationAxisOf(be);
        BlockPos pos = be.getBlockPos();
        float time = getRenderTime(be, kinetic.partialTicks);
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() != boxAxis) continue;
            Direction.Axis axis = direction.getAxis();
            float angle = (time * be.getSpeed() * 3f / 10) % 360;
            angle *= be.getRotationSpeedModifier(direction);
            angle += getRotationOffsetForPosition(be, pos, axis);
            pose.pushPose();
            pose.translate(.5, .5, .5);
            pose.rotate(rotation(axis, angle / 180f * (float) Math.PI));
            switch (direction) {
                case NORTH -> pose.rotate(com.mojang.math.Axis.YP.rotationDegrees(180));
                case EAST -> pose.rotate(com.mojang.math.Axis.YP.rotationDegrees(90));
                case WEST -> pose.rotate(com.mojang.math.Axis.YP.rotationDegrees(-90));
                case UP -> pose.rotate(com.mojang.math.Axis.XP.rotationDegrees(-90));
                case DOWN -> pose.rotate(com.mojang.math.Axis.XP.rotationDegrees(90));
                case SOUTH -> {}
            }
            pose.translate(-.5, -.5, -.5);
            collector.submitBlockModel(pose, RenderTypes.cutoutMovingBlock(), parts,
                    BlockModelRenderState.EMPTY_TINTS, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
    }
}
