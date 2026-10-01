package com.hlysine.create_connected.content.kineticbattery;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hlysine.create_connected.CreateConnected;
import com.hlysine.create_connected.datagen.CCSimpleModelGen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public class KineticBatteryOverrides {
    public static final Identifier ID = CreateConnected.asResource("kinetic_battery_level");

    public static void registerModels(Block block) {
        for (int level = 0; level <= 5; level++)
            CCSimpleModelGen.registerModel(CreateConnected.asResource("item/kinetic_battery_level_" + level),
                    KineticBatteryGenerator.levelModel("item", level + "_discharge"));
        JsonObject base = new JsonObject();
        base.addProperty("parent", "create_connected:item/kinetic_battery_level_0");
        CCSimpleModelGen.registerModel(CreateConnected.asResource("item/kinetic_battery"), base);
        CCSimpleModelGen.registerItemDefinition(block, itemDefinition());
    }

    public static JsonObject itemDefinition() {
        JsonObject dispatch = new JsonObject();
        dispatch.addProperty("type", "minecraft:range_dispatch");
        dispatch.addProperty("property", "create_connected:kinetic_battery_level");
        dispatch.add("fallback", levelReference(0));
        JsonArray entries = new JsonArray();
        for (int level = 0; level <= 5; level++) {
            JsonObject entry = new JsonObject();
            entry.addProperty("threshold", level);
            entry.add("model", levelReference(level));
            entries.add(entry);
        }
        dispatch.add("entries", entries);
        JsonObject json = new JsonObject();
        json.add("model", dispatch);
        return json;
    }

    private static JsonObject levelReference(int level) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:model");
        json.addProperty("model", "create_connected:item/kinetic_battery_level_" + level);
        return json;
    }
}
