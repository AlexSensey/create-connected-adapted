package com.hlysine.create_connected.content;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.transmission.SplitShaftBlockEntity;
import com.simibubi.create.content.kinetics.transmission.SplitShaftRenderer;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueLabelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

/** Split shafts with the scroll labels required by Connected's adjustable clutches. */
public class ClutchRenderer extends SplitShaftRenderer {
    public ClutchRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack pose, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        super.submit(state, pose, collector, camera);
        if (!(state instanceof KineticRenderState kinetic)
                || !(kinetic.blockEntity instanceof SplitShaftBlockEntity be) || isInvalid(be)) return;

        // Labels must also be submitted when Flywheel owns the shaft geometry.
        for (var behaviour : be.getAllBehaviours()) {
            if (!(behaviour instanceof ScrollValueBehaviour scroll) || !scroll.isActive()) continue;
            if (scroll instanceof ScrollOptionBehaviour<?> option)
                ScrollValueLabelRenderer.submitWallAttachedScrollOption(option, state, pose, collector, camera);
            else
                ScrollValueLabelRenderer.submitEjector(scroll, state, pose, collector, camera);
        }
    }
}
