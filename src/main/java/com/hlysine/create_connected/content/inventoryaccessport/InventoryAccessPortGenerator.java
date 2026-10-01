package com.hlysine.create_connected.content.inventoryaccessport;

import com.google.gson.JsonObject;
import com.hlysine.create_connected.CreateConnected;
import com.hlysine.create_connected.datagen.CCSimpleModelGen;
import net.minecraft.world.level.block.Block;

/** Native model output for inventory access ports and bridges. */
public class InventoryAccessPortGenerator {
    public void register(Block block) {
        CCSimpleModelGen.registerBlockstate(block, portDefinition());
        for (String target : new String[] { "ceiling", "floor", "wall" })
            for (boolean attached : new boolean[] { false, true }) {
                String model = "block/inventory_access_port/block_" + target + (attached ? "_on" : "_off");
                CCSimpleModelGen.registerModel(CreateConnected.asResource(model), indicatorModel(target, attached));
            }
        CCSimpleModelGen.registerModel(CreateConnected.asResource("item/inventory_access_port"),
                itemModel("inventory_access_port/block_wall"));
    }

    public static void registerBridge(Block block) {
        CCSimpleModelGen.registerBlockstate(block, bridgeDefinition());
        CCSimpleModelGen.registerModel(CreateConnected.asResource("item/inventory_bridge"),
                itemModel("inventory_bridge/item"));
    }

    public static JsonObject portDefinition() {
        JsonObject variants = new JsonObject();
        String[] directions = { "north", "east", "south", "west" };
        for (boolean attached : new boolean[] { false, true })
            for (int i = 0; i < directions.length; i++)
                for (String target : new String[] { "ceiling", "floor", "wall" }) {
                    JsonObject variant = new JsonObject();
                    variant.addProperty("model", "create_connected:block/inventory_access_port/block_" + target
                            + (attached ? "_on" : "_off"));
                    if (i != 0) variant.addProperty("y", i * 90);
                    variants.add("attached=" + attached + ",facing=" + directions[i] + ",target=" + target, variant);
                }
        JsonObject json = new JsonObject();
        json.add("variants", variants);
        return json;
    }

    public static JsonObject bridgeDefinition() {
        JsonObject variants = new JsonObject();
        for (boolean negative : new boolean[] { false, true })
            for (boolean positive : new boolean[] { false, true })
                for (String axis : new String[] { "x", "y", "z" }) {
                    String suffix = negative && positive ? "_both" : negative ? "_negative" : positive ? "_positive" : "";
                    JsonObject variant = new JsonObject();
                    variant.addProperty("model", "create_connected:block/inventory_bridge/block" + suffix);
                    if (!axis.equals("y")) {
                        variant.addProperty("x", 90);
                        variant.addProperty("y", axis.equals("x") ? 90 : 180);
                    }
                    variants.add("attached_negative=" + negative + ",attached_positive=" + positive + ",axis=" + axis, variant);
                }
        JsonObject json = new JsonObject();
        json.add("variants", variants);
        return json;
    }

    public static JsonObject indicatorModel(String target, boolean attached) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", "create_connected:block/inventory_access_port/block_" + target);
        JsonObject textures = new JsonObject();
        textures.addProperty("level", "create_connected:block/inventory_access_port_" + (attached ? "on" : "off"));
        json.add("textures", textures);
        return json;
    }

    public static JsonObject itemModel(String model) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", "create_connected:block/" + model);
        return json;
    }
}
