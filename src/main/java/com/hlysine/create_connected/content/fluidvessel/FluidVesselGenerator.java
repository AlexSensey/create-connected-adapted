package com.hlysine.create_connected.content.fluidvessel;

import com.google.gson.JsonObject;
import com.hlysine.create_connected.datagen.CCSimpleModelGen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

/** Native vessel model datagen; geometry assets are shared by both variants. */
public class FluidVesselGenerator {
    private final String prefix;
    private static final String[] SHAPES = { "plain", "window", "window_top", "window_middle", "window_bottom",
            "window_single", "window_top_single", "window_middle_single", "window_bottom_single" };

    public FluidVesselGenerator() { this(""); }
    public FluidVesselGenerator(String prefix) { this.prefix = prefix; }

    public void register(Block block) {
        var id = BuiltInRegistries.BLOCK.getKey(block);
        JsonObject blockstate = definition(prefix);
        CCSimpleModelGen.registerBlockstate(block, blockstate);
        if (!prefix.isEmpty()) {
            for (var entry : blockstate.getAsJsonObject("variants").entrySet()) {
                Identifier model = Identifier.parse(entry.getValue().getAsJsonObject().get("model").getAsString());
                String modelName = model.getPath().substring("block/".length() + prefix.length());
                CCSimpleModelGen.registerModel(model, creativeModel(modelName, prefix));
            }
        }
        CCSimpleModelGen.registerModel(Identifier.fromNamespaceAndPath(id.getNamespace(), "item/" + id.getPath()),
                itemModel(!prefix.isEmpty()));
    }

    public static String modelName(String axis, boolean positive, boolean negative, String shape) {
        if (positive && negative && shape.endsWith("_single"))
            shape = shape.substring(0, shape.length() - "_single".length());
        String end = positive && negative ? "single" : positive ? "positive" : negative ? "negative" : "middle";
        return axis + "_" + end + (shape.equals("plain") ? "" : "_" + shape);
    }

    public static JsonObject definition(String prefix) {
        JsonObject variants = new JsonObject();
        for (String axis : new String[] { "x", "z" })
            for (boolean positive : new boolean[] { false, true })
                for (boolean negative : new boolean[] { false, true })
                    for (String shape : SHAPES) {
                        String key = "axis=" + axis + ",negative=" + negative + ",positive=" + positive + ",shape=" + shape;
                        JsonObject model = new JsonObject();
                        String name = modelName(axis, positive, negative, shape);
                        model.addProperty("model", "create_connected:block/" + (prefix.isEmpty() ? "fluid_vessel/block_" : prefix) + name);
                        variants.add(key, model);
                    }
        JsonObject json = new JsonObject();
        json.add("variants", variants);
        return json;
    }

    public static JsonObject creativeModel(String modelName, String prefix) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", "create_connected:block/fluid_vessel/block_" + modelName);
        JsonObject textures = new JsonObject();
        textures.addProperty("0", "create:block/" + prefix + "casing");
        textures.addProperty("1", "create:block/" + prefix + "fluid_tank");
        textures.addProperty("3", "create:block/" + prefix + "fluid_tank_window");
        textures.addProperty("4", "create:block/" + prefix + "casing");
        textures.addProperty("5", "create:block/" + prefix + "fluid_tank_window_single");
        textures.addProperty("6", "create_connected:block/" + prefix + "fluid_container_window");
        textures.addProperty("7", "create_connected:block/" + prefix + "fluid_container_window_single");
        textures.addProperty("particle", "create:block/" + prefix + "fluid_tank");
        json.add("textures", textures);
        return json;
    }

    public static JsonObject itemModel(boolean creative) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", "create_connected:block/fluid_vessel/block_x_single_window");
        if (creative) {
            JsonObject textures = new JsonObject();
            textures.addProperty("0", "create:block/creative_casing");
            textures.addProperty("1", "create:block/creative_fluid_tank");
            textures.addProperty("4", "create:block/creative_casing");
            textures.addProperty("5", "create:block/creative_fluid_tank_window_single");
            textures.addProperty("6", "create_connected:block/fluid_container_window");
            textures.addProperty("7", "create_connected:block/creative_fluid_container_window_single");
            textures.addProperty("particle", "create:block/creative_fluid_tank");
            json.add("textures", textures);
        }
        return json;
    }
}
