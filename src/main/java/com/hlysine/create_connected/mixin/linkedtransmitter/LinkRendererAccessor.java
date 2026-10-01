package com.hlysine.create_connected.mixin.linkedtransmitter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.redstone.link.LinkRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = LinkRenderer.class, remap = false)
public interface LinkRendererAccessor {
    @Invoker("submitValueBoxFrame")
    static void callSubmitValueBoxFrame(PoseStack pose, SubmitNodeCollector collector) {
        throw new AssertionError("Mixin invoker was not applied");
    }
}
