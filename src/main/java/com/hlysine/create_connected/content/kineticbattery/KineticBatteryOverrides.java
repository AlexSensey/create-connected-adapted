package com.hlysine.create_connected.content.kineticbattery;

import com.hlysine.create_connected.CreateConnected;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public class KineticBatteryOverrides {
    public static final Identifier ID = CreateConnected.asResource("kinetic_battery_level");

    public static void addOverrideModels(DataGenContext<Item, KineticBatteryBlockItem> context,
                                         RegistrateItemModelProvider provider) {
        // The item definition selects these models using the 26.2 numeric property.
        for (int i = 0; i <= 5; i++) {
            provider.withExistingParent("kinetic_battery_level_" + i, "create_connected:block/kinetic_battery/item")
                    .texture("level", CreateConnected.asResource("block/kinetic_battery/level_" + i + "_discharge"));
        }
        provider.withExistingParent(context.getName(), "create_connected:item/kinetic_battery_level_0");
    }
}
