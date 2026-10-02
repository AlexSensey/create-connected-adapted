package com.hlysine.create_connected.content.linkedtransmitter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.redstone.analogLever.AnalogLeverBlockEntity;
import com.simibubi.create.content.redstone.analogLever.AnalogLeverRenderer;
import com.simibubi.create.content.redstone.link.LinkBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxRenderer;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class LinkedAnalogLeverRenderer extends AnalogLeverRenderer {
    public LinkedAnalogLeverRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public BlockEntityRenderState createRenderState() {
        // The parent's private state owns the animated handle and indicator.
        return new LinkedLeverRenderState(super.createRenderState());
    }

    @Override
    public void extractRenderState(AnalogLeverBlockEntity be, BlockEntityRenderState state, float partialTicks,
                                   Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        if (!(state instanceof LinkedLeverRenderState linked))
            return;
        super.extractRenderState(be, linked.lever, partialTicks, cameraPos, crumblingOverlay);
        // Reset reused states before any early return, including locking/removal.
        linked.first = ItemStack.EMPTY;
        linked.second = ItemStack.EMPTY;
        if (isInvalid(be))
            return;
        float distance = AllConfigs.client().filterItemRenderDistance.getF();
        if (!be.isVirtual() && cameraPos.distanceToSqr(Vec3.atCenterOf(be.getBlockPos())) > distance * distance)
            return;
        LinkBehaviour link = be.getBehaviour(LinkBehaviour.TYPE);
        if (link == null)
            return;
        var frequencies = link.getNetworkKey();
        for (boolean first : new boolean[] { true, false }) {
            var slot = new LinkedTransmitterFrequencySlot(first);
            if (!slot.shouldRender(be.getLevel(), be.getBlockPos(), be.getBlockState()))
                continue;
            PoseStack transform = new PoseStack();
            slot.transform(be.getLevel(), be.getBlockPos(), be.getBlockState(), transform);
            if (first) {
                linked.first = frequencies.get(true).getStack().copy();
                linked.firstTransform.set(transform.last().pose());
            } else {
                linked.second = frequencies.get(false).getStack().copy();
                linked.secondTransform.set(transform.last().pose());
            }
        }
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack ms, SubmitNodeCollector collector,
                       CameraRenderState cameraRenderState) {
        if (!(state instanceof LinkedLeverRenderState linked))
            return;
        super.submit(linked.lever, ms, collector, cameraRenderState);
        // Use this transmitter's slot transforms; Create's link renderer assumes
        // a RedstoneLinkBlock with a six-direction FACING property.
        submitFrequency(linked.first, linked.firstTransform, state.lightCoords, ms, collector);
        submitFrequency(linked.second, linked.secondTransform, state.lightCoords, ms, collector);
    }

    private static void submitFrequency(ItemStack stack, Matrix4f transform, int light,
                                        PoseStack ms, SubmitNodeCollector collector) {
        if (stack.isEmpty())
            return;
        ms.pushPose();
        ms.mulPose(transform);
        ValueBoxRenderer.submitItemIntoValueBox(stack, ms, collector, light);
        ms.popPose();
    }

    private static class LinkedLeverRenderState extends BlockEntityRenderState {
        final BlockEntityRenderState lever;
        ItemStack first = ItemStack.EMPTY;
        ItemStack second = ItemStack.EMPTY;
        final Matrix4f firstTransform = new Matrix4f();
        final Matrix4f secondTransform = new Matrix4f();

        LinkedLeverRenderState(BlockEntityRenderState lever) {
            this.lever = lever;
        }
    }
}
