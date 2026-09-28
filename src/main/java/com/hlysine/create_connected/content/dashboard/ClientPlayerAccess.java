package com.hlysine.create_connected.content.dashboard;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

@OnlyIn(Dist.CLIENT)
public class ClientPlayerAccess {
    public static Player getPlayer() {
        return Minecraft.getInstance().player;
    }
}
