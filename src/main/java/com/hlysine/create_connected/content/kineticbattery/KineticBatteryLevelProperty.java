package com.hlysine.create_connected.content.kineticbattery;

import com.hlysine.create_connected.registries.CCDataComponents;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Numeric property used by the 26.2 battery item range-dispatch model. */
public record KineticBatteryLevelProperty() implements RangeSelectItemModelProperty {
    public static final MapCodec<KineticBatteryLevelProperty> CODEC = MapCodec.unit(new KineticBatteryLevelProperty());

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        double charge = stack.getOrDefault(CCDataComponents.KINETIC_BATTERY_CHARGE, 0.0);
        return KineticBatteryBlockEntity.getCrudeBatteryLevel(charge, 5);
    }

    @Override
    public MapCodec<KineticBatteryLevelProperty> type() {
        return CODEC;
    }
}
