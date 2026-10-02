package com.hlysine.create_connected.content.sequencedpulsegenerator;

import net.createmod.catnip.api.client.gui.ScreenOpener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

/** Loaded only when the client-only platform callback opens the editor. */
public final class SequencedPulseGeneratorClient {
    private SequencedPulseGeneratorClient() {}

    public static void open(SequencedPulseGeneratorBlockEntity blockEntity, Player player) {
        if (player instanceof LocalPlayer)
            ScreenOpener.open(new SequencedPulseGeneratorScreen(blockEntity));
    }
}
