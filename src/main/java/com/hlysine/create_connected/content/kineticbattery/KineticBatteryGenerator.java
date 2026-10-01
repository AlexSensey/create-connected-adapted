package com.hlysine.create_connected.content.kineticbattery;

import com.google.gson.JsonObject;
import com.hlysine.create_connected.CreateConnected;
import com.hlysine.create_connected.datagen.CCSimpleModelGen;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;

/** Native battery block models and all charge/power/facing variants. */
public class KineticBatteryGenerator {
    public void register(Block block) {
        CCSimpleModelGen.registerBlockstate(block, definition());
        for (int level = 0; level <= 5; level++)
            for (boolean powered : new boolean[] { false, true }) {
                String suffix = level + "_" + (powered ? "discharge" : "charge");
                CCSimpleModelGen.registerModel(CreateConnected.asResource("block/kinetic_battery/block_" + suffix),
                        levelModel("block", suffix));
            }
        KineticBatteryOverrides.registerModels(block);
    }

    public static JsonObject definition() {
        JsonObject variants = new JsonObject();
        for (Direction facing : Direction.values())
            for (int level = 0; level <= 5; level++)
                for (int power = 0; power <= 15; power++) {
                    String suffix = level + "_" + (power > 0 ? "discharge" : "charge");
                    JsonObject variant = CCSimpleModelGen.directionalVariant(
                            CreateConnected.asResource("block/kinetic_battery/block_" + suffix), facing);
                    variants.add("facing=" + facing.getSerializedName() + ",level=" + level + ",power=" + power, variant);
                }
        JsonObject json = new JsonObject();
        json.add("variants", variants);
        return json;
    }

    public static JsonObject levelModel(String parent, String suffix) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", "create_connected:block/kinetic_battery/" + parent);
        JsonObject textures = new JsonObject();
        textures.addProperty("level", "create_connected:block/kinetic_battery/level_" + suffix);
        json.add("textures", textures);
        return json;
    }
}
