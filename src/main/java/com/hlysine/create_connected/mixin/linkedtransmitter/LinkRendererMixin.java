package com.hlysine.create_connected.mixin.linkedtransmitter;

import com.hlysine.create_connected.content.linkedtransmitter.LinkedTransmitterFrequencySlot;
import com.hlysine.create_connected.content.linkedtransmitter.LinkedTransmitterBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.redstone.link.LinkBehaviour;
import com.simibubi.create.content.redstone.link.LinkRenderer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Create 1.20's native link icon renderer assumes RedstoneLinkBlock.FACING. */
@Mixin(value = LinkRenderer.class, remap = false)
public class LinkRendererMixin {
    @Inject(method = "submitOnBlockEntity", at = @At("HEAD"), cancellable = true)
    private static void connectedSlots(SmartBlockEntity be, float partialTicks, PoseStack pose,
                                       SubmitNodeCollector collector, int light, CallbackInfo ci) {
        if (be == null || be.isRemoved() || !be.hasLevel())
            return;
        LinkBehaviour link = be.getBehaviour(LinkBehaviour.TYPE);
        if (link == null || !(be.getBlockState().getBlock() instanceof LinkedTransmitterBlock))
            return;

        ci.cancel();
        for (boolean first : new boolean[]{true, false}) {
            var slot = new LinkedTransmitterFrequencySlot(first);
            var stack = link.getNetworkKey().get(first).getStack();
            if (!slot.shouldRender(be.getLevel(), be.getBlockPos(), be.getBlockState()))
                continue;
            pose.pushPose();
            slot.transform(be.getLevel(), be.getBlockPos(), be.getBlockState(), pose);
            if (Minecraft.getInstance().hitResult instanceof BlockHitResult hit
                    && hit.getBlockPos().equals(be.getBlockPos()) && link.testHit(first, hit.getLocation())) {
                pose.pushPose();
                // The native frame has world-space size; slot.transform already scaled the pose.
                float inverseScale = 1 / slot.getScale();
                pose.scale(inverseScale, inverseScale, inverseScale);
                LinkRendererAccessor.callSubmitValueBoxFrame(pose, collector);
                pose.popPose();
            }
            if (!stack.isEmpty())
                ValueBoxRenderer.submitItemIntoValueBox(stack, pose, collector, light);
            pose.popPose();
        }
    }
}
