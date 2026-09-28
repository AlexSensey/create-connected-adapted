package com.hlysine.create_connected;

import com.hlysine.create_connected.registries.CCPartialModels;
import com.hlysine.create_connected.registries.CCPonderPlugin;
import net.createmod.ponder.api.client.PonderIndex;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = CreateConnected.MODID, dist = Dist.CLIENT)
public class CreateConnectedClient {
    public CreateConnectedClient(IEventBus modEventBus) {
        CCPartialModels.register();
        modEventBus.addListener(com.hlysine.create_connected.registries.CCBlockEntityRenderers::register);
        modEventBus.addListener((net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent event) ->
                event.register(com.hlysine.create_connected.content.kineticbattery.KineticBatteryOverrides.ID,
                        com.hlysine.create_connected.content.kineticbattery.KineticBatteryLevelProperty.CODEC));
        modEventBus.addListener(CreateConnectedClient::init);
    }

    public static void init(final FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new CCPonderPlugin());
    }
}
