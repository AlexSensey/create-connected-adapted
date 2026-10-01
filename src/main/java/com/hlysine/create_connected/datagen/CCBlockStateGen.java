package com.hlysine.create_connected.datagen;

import com.google.gson.JsonObject;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

/** Native JSON generation for pulse, gearbox and axis-specific block models. */
public class CCBlockStateGen {
    public static void encasedCrossConnector(Block block, String casing) {
        String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
        CCSimpleModelGen.registerBlockstate(block, encasedConnectorBlockstate(casing));
        registerItemParent(name, "cross_connector/item_" + casing);
    }

    public static JsonObject encasedConnectorBlockstate(String casing) {
        JsonObject json = rotatedAxisBlockstate("cross_connector", false, false);
        for (var entry : json.getAsJsonObject("variants").entrySet()) {
            JsonObject variant = entry.getValue().getAsJsonObject();
            variant.addProperty("model", "create_connected:block/cross_connector/block_" + casing);
            variant.addProperty("uvlock", true);
        }
        return json;
    }

    public static void overstressClutch(Block block) {
        String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
        CCSimpleModelGen.registerBlockstate(block, overstressBlockstate(name));
        registerItemParent(name, name + "/item");
    }

    public static void brassChute(Block block) {
        String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
        CCSimpleModelGen.registerBlockstate(block, chuteBlockstate(name));
        registerItemParent(name, name + "/block");
    }

    public static void chainCogwheel(Block block) {
        String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
        CCSimpleModelGen.registerBlockstate(block, chainBlockstate(name));
        registerItemParent(name, name + "/item");
    }

    public static JsonObject overstressBlockstate(String name) {
        JsonObject variants = new JsonObject();
        for (Direction.Axis axis : Direction.Axis.values())
            for (boolean powered : new boolean[] { false, true })
                for (String state : new String[] { "coupled", "uncoupled", "uncoupling" }) {
                    JsonObject json = variant(name + "/block" + (state.equals("uncoupled") ? "_uncoupled" : "")
                            + (powered ? "_powered" : ""));
                    if (axis != Direction.Axis.Y) {
                        json.addProperty("x", 90);
                        json.addProperty("y", axis == Direction.Axis.X ? 90 : 180);
                    }
                    variants.add("axis=" + axis.getName() + ",powered=" + powered + ",state=" + state, json);
                }
        return blockstate(variants);
    }

    public static JsonObject chuteBlockstate(String name) {
        JsonObject variants = new JsonObject();
        for (Direction facing : new Direction[] { Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST })
            for (String shape : new String[] { "normal", "intersection", "encased", "window" })
                for (boolean waterlogged : new boolean[] { false, true }) {
                    boolean diagonal = facing != Direction.DOWN;
                    String suffix = diagonal ? "_diagonal" : "";
                    if (shape.equals("intersection") || shape.equals("encased"))
                        suffix += diagonal && shape.equals("encased") ? "_encased" : "_intersection";
                    else if (!diagonal && shape.equals("window")) suffix += "_windowed";
                    JsonObject json = variant(name + "/block" + suffix);
                    int y = diagonal ? (int) facing.toYRot() : 0;
                    if (y != 0) json.addProperty("y", y);
                    variants.add("facing=" + facing.getSerializedName() + ",shape=" + shape + ",waterlogged=" + waterlogged, json);
                }
        return blockstate(variants);
    }

    public static JsonObject chainBlockstate(String name) {
        JsonObject variants = new JsonObject();
        for (Direction.Axis axis : Direction.Axis.values())
            for (boolean first : new boolean[] { false, true })
                for (String part : new String[] { "none", "start", "middle", "end" }) {
                    boolean single = part.equals("none");
                    String suffix = single ? "single" : (part.equals("middle") ? "middle" : "end")
                            + (axis == Direction.Axis.Y ? "_vertical" : "_horizontal");
                    JsonObject json = variant(name + "/" + suffix);
                    int x = 0, y = 0;
                    if (single) {
                        x = axis == Direction.Axis.Y ? 90 : 0;
                        y = axis == Direction.Axis.X ? 90 : 0;
                    } else {
                        if (axis == Direction.Axis.X) x = (first ? 90 : 0) + (part.equals("start") ? 180 : 0);
                        if (axis == Direction.Axis.Z) {
                            x = first ? 0 : part.equals("start") ? 270 : 90;
                            y = first && part.equals("end") ? 270 : 90;
                        }
                        if (axis == Direction.Axis.Y) {
                            boolean flip = part.equals("end") && !first || part.equals("start") && first;
                            y = (first ? 90 : 0) + (flip ? 180 : 0);
                        }
                    }
                    if (x != 0) json.addProperty("x", x);
                    if (y != 0) json.addProperty("y", y);
                    variants.add("axis=" + axis.getName() + ",axis_along_first=" + first + ",part=" + part, json);
                }
        return blockstate(variants);
    }

    public static void rotatedAxis(Block block) {
        registerRotatedAxis(block, false, false);
    }

    public static void poweredAxis(Block block) {
        registerRotatedAxis(block, true, false);
    }

    public static void shearPin(Block block) {
        registerRotatedAxis(block, false, true);
    }

    private static void registerRotatedAxis(Block block, boolean powered, boolean bareModel) {
        String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
        CCSimpleModelGen.registerBlockstate(block, rotatedAxisBlockstate(name, powered, bareModel));
        registerItemParent(name, bareModel ? name : name + "/item");
    }

    public static void crankWheel(Block block) {
        String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
        CCSimpleModelGen.registerDirectional(block, state -> id(name + "/block"));
        registerItemParent(name, name + "/item");
    }

    private static void registerItemParent(String name, String parent) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", id(parent).toString());
        CCSimpleModelGen.registerModel(Identifier.fromNamespaceAndPath("create_connected", "item/" + name), json);
    }

    public static JsonObject rotatedAxisBlockstate(String name, boolean powered, boolean bareModel) {
        JsonObject variants = new JsonObject();
        for (Direction.Axis axis : Direction.Axis.values())
            for (int flag = 0; flag < (powered ? 2 : 1); flag++) {
                String key = "axis=" + axis.getName() + (powered ? ",powered=" + (flag != 0) : "");
                JsonObject json = variant(name + (bareModel ? "" : "/block") + (flag != 0 ? "_powered" : ""));
                if (axis != Direction.Axis.Y) {
                    json.addProperty("x", 90);
                    json.addProperty("y", axis == Direction.Axis.X ? 90 : 180);
                }
                variants.add(key, json);
            }
        return blockstate(variants);
    }

    public static void sequencedPulseGenerator(Block block) {
        String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
        CCSimpleModelGen.registerBlockstate(block, pulseBlockstate(name));
        for (int flags = 0; flags < 8; flags++)
            CCSimpleModelGen.registerModel(id(pulseName(name, flags)), pulseModel(name, flags));
    }

    public static void brassGearbox(Block block) {
        String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
        CCSimpleModelGen.registerBlockstate(block, gearboxBlockstate(name));
        for (Direction.Axis axis : Direction.Axis.values())
            for (int flags = 0; flags < 16; flags++)
                CCSimpleModelGen.registerModel(id(gearboxName(name, axis.getName(), flags)), gearboxModel(name, axis.getName(), flags));
    }

    public static void axisBlock(Block block) {
        String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
        CCSimpleModelGen.registerBlockstate(block, axisBlockstate(name));
    }

    public static void dashboard(Block block) {
        var id = BuiltInRegistries.BLOCK.getKey(block);
        CCSimpleModelGen.registerBlockstate(block, dashboardBlockstate(id.getPath()));
        JsonObject item = new JsonObject();
        item.addProperty("parent", "create_connected:block/" + id.getPath() + "/block_open");
        CCSimpleModelGen.registerModel(Identifier.fromNamespaceAndPath(id.getNamespace(), "item/" + id.getPath()), item);
    }

    public static JsonObject dashboardBlockstate(String name) {
        JsonObject variants = new JsonObject();
        for (Direction facing : new Direction[] { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST })
            for (boolean open : new boolean[] { false, true })
                for (boolean waterlogged : new boolean[] { false, true }) {
                    String key = "facing=" + facing.getSerializedName() + ",open=" + open + ",waterlogged=" + waterlogged;
                    JsonObject variant = variant(name + "/block" + (open ? "_open" : ""));
                    int y = ((int) facing.toYRot() + 180) % 360;
                    if (y != 0) variant.addProperty("y", y);
                    variants.add(key, variant);
                }
        return blockstate(variants);
    }

    private static Identifier id(String name) { return Identifier.parse("create_connected:block/" + name); }

    public static String pulseName(String name, int flags) {
        return name + ((flags & 4) != 0 ? "_powered" : "")
                + ((flags & 2) != 0 ? "_powering" : "") + ((flags & 1) != 0 ? "_reset" : "");
    }

    public static JsonObject pulseModel(String name, int flags) {
        JsonObject textures = new JsonObject();
        textures.addProperty("1_top", "create_connected:block/" + name
                + ((flags & 4) != 0 ? "_on" : "_off") + ((flags & 1) != 0 ? "_reset" : ""));
        textures.addProperty("torch", (flags & 2) != 0 ? "minecraft:block/redstone_torch" : "minecraft:block/redstone_torch_off");
        return model("create_connected:block/" + name + "_template", textures);
    }

    public static JsonObject pulseBlockstate(String name) {
        JsonObject variants = new JsonObject();
        for (Direction facing : new Direction[] { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST })
            for (int flags = 0; flags < 8; flags++) {
                String key = "facing=" + facing.getSerializedName() + ",powered=" + ((flags & 4) != 0)
                        + ",powered_side=" + ((flags & 1) != 0) + ",powering=" + ((flags & 2) != 0);
                JsonObject variant = variant(pulseName(name, flags));
                int y = ((int) facing.toYRot() + 180) % 360;
                if (y != 0) variant.addProperty("y", y);
                variants.add(key, variant);
            }
        return blockstate(variants);
    }

    public static String gearboxName(String name, String axis, int flags) {
        String result = name + "_" + axis;
        for (int face = 1; face <= 4; face++)
            if ((flags & (1 << (4 - face))) != 0) result += "_" + face;
        return result;
    }

    public static JsonObject gearboxModel(String name, String axis, int flags) {
        JsonObject textures = new JsonObject();
        for (int face = 1; face <= 4; face++)
            textures.addProperty(Integer.toString(face), "create_connected:block/" + name
                    + ((flags & (1 << (4 - face))) != 0 ? "_top" : "_bottom"));
        return model("create_connected:block/brass_gearbox/block_" + axis, textures);
    }

    public static JsonObject gearboxBlockstate(String name) {
        JsonObject variants = new JsonObject();
        for (Direction.Axis axis : Direction.Axis.values())
            for (int flags = 0; flags < 16; flags++) {
                String key = "axis=" + axis.getName();
                for (int face = 1; face <= 4; face++) key += ",face_" + face + "_flipped=" + ((flags & (1 << (4 - face))) != 0);
                variants.add(key, variant(gearboxName(name, axis.getName(), flags)));
            }
        return blockstate(variants);
    }

    public static JsonObject axisBlockstate(String name) {
        JsonObject variants = new JsonObject();
        for (Direction.Axis axis : Direction.Axis.values())
            variants.add("axis=" + axis.getName(), variant(name + "/block_" + axis.getName()));
        return blockstate(variants);
    }

    private static JsonObject variant(String name) {
        JsonObject json = new JsonObject();
        json.addProperty("model", "create_connected:block/" + name);
        return json;
    }

    private static JsonObject model(String parent, JsonObject textures) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", parent);
        json.add("textures", textures);
        return json;
    }

    private static JsonObject blockstate(JsonObject variants) {
        JsonObject json = new JsonObject();
        json.add("variants", variants);
        return json;
    }
}
