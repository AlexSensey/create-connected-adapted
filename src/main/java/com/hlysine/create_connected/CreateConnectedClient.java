package com.hlysine.create_connected;

import com.hlysine.create_connected.registries.CCPartialModels;
import com.hlysine.create_connected.registries.CCPackets;
import com.hlysine.create_connected.registries.CCPonderPlugin;
import com.hlysine.create_connected.content.contraption.jukebox.PlayContraptionJukeboxPacket;
import net.createmod.ponder.api.client.PonderIndex;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

@Mod(value = CreateConnected.MODID, dist = Dist.CLIENT)
public class CreateConnectedClient {
    public CreateConnectedClient(IEventBus modEventBus) {
        CCPartialModels.register();
        modEventBus.addListener(com.hlysine.create_connected.compat.CatalystFallbackPack::register);
        modEventBus.addListener((RegisterClientPayloadHandlersEvent event) ->
                event.register(CCPackets.PLAY_CONTRAPTION_JUKEBOX.<PlayContraptionJukeboxPacket>getType(),
                        (packet, context) -> packet.handle(context.player())));
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                (net.neoforged.neoforge.client.event.ClientTickEvent.Post tick) ->
                        com.hlysine.create_connected.content.contraption.jukebox.ContraptionMusicManager.tick());
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                (net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut logout) ->
                        com.hlysine.create_connected.content.contraption.jukebox.ContraptionMusicManager.clear());
        modEventBus.addListener(com.hlysine.create_connected.registries.CCBlockEntityRenderers::register);
        modEventBus.addListener((net.neoforged.neoforge.client.event.ModelEvent.RegisterStandalone event) -> {
            com.hlysine.create_connected.compat.SimulatedCompat.invoke(
                    "com.hlysine.create_connected.compat.SimulatedClientRegistration", "registerModels",
                    net.neoforged.neoforge.client.event.ModelEvent.RegisterStandalone.class, event);
        });
        modEventBus.addListener((net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent event) ->
                event.register(com.hlysine.create_connected.content.kineticbattery.KineticBatteryOverrides.ID,
                        com.hlysine.create_connected.content.kineticbattery.KineticBatteryLevelProperty.CODEC));
        modEventBus.addListener(CreateConnectedClient::init);
    }

    public static void init(final FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new CCPonderPlugin());
    }
}
