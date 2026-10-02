package com.hlysine.create_connected.compat;

import com.hlysine.create_connected.content.linkedtransmitter.LinkedThrottleLeverRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

/** Client-only bridge methods, reached from the core's client listeners. */
public final class SimulatedClientRegistration {
    private SimulatedClientRegistration() {}

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(SimCompatRegistry.LINKED_THROTTLE_LEVER_ENTITY.get(),
                LinkedThrottleLeverRenderer::new);
    }

    public static void registerModels(ModelEvent.RegisterStandalone event) {
        LinkedThrottleLeverRenderer.registerModels(event);
    }
}
