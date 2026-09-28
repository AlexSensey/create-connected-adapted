package com.hlysine.create_connected.registries;

import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer;
import com.hlysine.create_connected.content.brassgearbox.BrassGearboxRenderer;
import com.simibubi.create.content.logistics.chute.ChuteRenderer;
import com.hlysine.create_connected.content.dashboard.DashboardRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogRenderer;
import com.hlysine.create_connected.content.fancatalyst.FanCatalystRotatingHeadRenderer;
import com.hlysine.create_connected.content.fluidvessel.FluidVesselRenderer;
import com.simibubi.create.content.kinetics.crank.HandCrankRenderer;
import com.hlysine.create_connected.content.kineticbattery.KineticBatteryRenderer;
import com.hlysine.create_connected.content.kineticbridge.KineticBridgeRenderer;
import com.hlysine.create_connected.content.linkedtransmitter.LinkedAnalogLeverRenderer;
import com.hlysine.create_connected.content.parallelgearbox.ParallelGearboxRenderer;
import com.hlysine.create_connected.content.sixwaygearbox.SixWayGearboxRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.simibubi.create.content.kinetics.transmission.SplitShaftRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Register the 26.2 renderer providers, including their render-state type. */
public final class CCBlockEntityRenderers {
    private CCBlockEntityRenderers() {}

    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CCBlockEntityTypes.ENCASED_CHAIN_COGWHEEL.get(), EncasedCogRenderer::small);
        event.registerBlockEntityRenderer(CCBlockEntityTypes.CRANK_WHEEL.get(), HandCrankRenderer::new);
        event.registerBlockEntityRenderer(CCBlockEntityTypes.PARALLEL_GEARBOX.get(), ParallelGearboxRenderer::new);
        event.registerBlockEntityRenderer(CCBlockEntityTypes.SIX_WAY_GEARBOX.get(), SixWayGearboxRenderer::new);
        event.registerBlockEntityRenderer(CCBlockEntityTypes.OVERSTRESS_CLUTCH.get(), SplitShaftRenderer::new);
        event.registerBlockEntityRenderer(CCBlockEntityTypes.SHEAR_PIN.get(), BracketedKineticBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(CCBlockEntityTypes.INVERTED_CLUTCH.get(), SplitShaftRenderer::new);
        event.registerBlockEntityRenderer(CCBlockEntityTypes.INVERTED_GEARSHIFT.get(), SplitShaftRenderer::new);
        event.registerBlockEntityRenderer(CCBlockEntityTypes.CENTRIFUGAL_CLUTCH.get(), SplitShaftRenderer::new);
        event.registerBlockEntityRenderer(CCBlockEntityTypes.FREEWHEEL_CLUTCH.get(), SplitShaftRenderer::new);
        event.registerBlockEntityRenderer(CCBlockEntityTypes.KINETIC_BRIDGE.get(), KineticBridgeRenderer::source);
        event.registerBlockEntityRenderer(CCBlockEntityTypes.KINETIC_BRIDGE_DESTINATION.get(), KineticBridgeRenderer::destination);
        event.registerBlockEntityRenderer(CCBlockEntityTypes.BRASS_GEARBOX.get(), BrassGearboxRenderer::new);
        event.registerBlockEntityRenderer(CCBlockEntityTypes.BRAKE.get(), SplitShaftRenderer::new);
        event.registerBlockEntityRenderer(CCBlockEntityTypes.KINETIC_BATTERY.get(), KineticBatteryRenderer::new);
        event.registerBlockEntityRenderer(CCBlockEntityTypes.FLUID_VESSEL.get(), FluidVesselRenderer::new);
        event.registerBlockEntityRenderer(CCBlockEntityTypes.CREATIVE_FLUID_VESSEL.get(), FluidVesselRenderer::new);
        event.registerBlockEntityRenderer(CCBlockEntityTypes.INVENTORY_BRIDGE.get(), SmartBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(CCBlockEntityTypes.LINKED_TRANSMITTER.get(), SmartBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(CCBlockEntityTypes.LINKED_ANALOG_LEVER.get(), LinkedAnalogLeverRenderer::new);
        event.registerBlockEntityRenderer(CCBlockEntityTypes.BRASS_CHUTE.get(), ChuteRenderer::new);
        event.registerBlockEntityRenderer(CCBlockEntityTypes.DASHBOARD.get(), DashboardRenderer::new);
        event.registerBlockEntityRenderer(CCBlockEntityTypes.FAN_ENDING_CATALYST_DRAGON_HEAD.get(), FanCatalystRotatingHeadRenderer::dragon);
        event.registerBlockEntityRenderer(CCBlockEntityTypes.FAN_EXPLODING_CATALYST.get(), FanCatalystRotatingHeadRenderer::creeper);
    }
}
