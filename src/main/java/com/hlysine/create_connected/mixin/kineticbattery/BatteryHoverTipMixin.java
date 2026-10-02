package com.hlysine.create_connected.mixin.kineticbattery;

import com.hlysine.create_connected.content.kineticbattery.KineticBatteryBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsClient;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.List;

@Mixin(value = ScrollValueRenderer.class, remap = false)
public class BatteryHoverTipMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE",
            target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/ValueSettingsClient;showHoverTip(Ljava/util/List;)V"))
    private static void createConnected$preciseBatteryTip(ValueSettingsClient handler, List<MutableComponent> tip) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.hitResult instanceof BlockHitResult hit
                && mc.level.getBlockEntity(hit.getBlockPos()) instanceof KineticBatteryBlockEntity battery) {
            ScrollValueBehaviour behaviour = battery.getBehaviour(ScrollValueBehaviour.TYPE);
            if (behaviour == null) return;
            if (!behaviour.testHit(hit.getLocation())) return;
        }
        handler.showHoverTip(tip);
    }
}
