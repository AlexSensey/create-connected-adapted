package com.hlysine.create_connected.mixin.sequencedgearshift;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Screen.class)
public interface AbstractSimiScreenAccessor {
    @Invoker("removeWidget")
    void callRemoveWidget(GuiEventListener widget);

}
