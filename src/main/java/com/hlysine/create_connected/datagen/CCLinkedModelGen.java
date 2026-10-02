package com.hlysine.create_connected.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.AttachFace;

/** Multipart geometry for the base control and its transmitter module. */
public final class CCLinkedModelGen {
    public enum Mode { BUTTON, LEVER, ANALOG }
    private CCLinkedModelGen() {}

    public static void register(Block block, Identifier off, Identifier on, Mode mode) {
        CCSimpleModelGen.registerBlockstate(block, definition(off, on, mode));
    }

    public static JsonObject definition(Identifier off, Identifier on, Mode mode) {
        JsonArray parts = new JsonArray();
        for (Direction facing : new Direction[] { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST }) {
            for (AttachFace face : AttachFace.values()) {
                int x = face == AttachFace.FLOOR ? 0 : face == AttachFace.WALL ? 90 : 180;
                int y = (int) (face == AttachFace.CEILING ? facing : facing.getOpposite()).toYRot();
                for (boolean powered : new boolean[] { false, true }) {
                    if (mode != Mode.ANALOG || !powered) {
                        Identifier base = mode == Mode.ANALOG ? off : mode == Mode.LEVER
                                ? (powered ? off : on) : (powered ? on : off);
                        JsonObject when = condition(facing, face);
                        if (mode != Mode.ANALOG) when.addProperty("powered", Boolean.toString(powered));
                        parts.add(part(when, base.toString(), x, y, mode == Mode.BUTTON && face == AttachFace.WALL));
                    }
                    for (boolean locked : new boolean[] { false, true }) {
                        JsonObject when = condition(facing, face);
                        when.addProperty("powered", Boolean.toString(powered));
                        when.addProperty("locked", Boolean.toString(locked));
                        String module = "create_connected:block/linked_transmitter/block"
                                + (powered ? "_powered" : "")
                                + (face == AttachFace.WALL ? "_vertical" : "")
                                + (locked ? "_locked" : "");
                        parts.add(part(when, module, x, y + (face == AttachFace.FLOOR ? 180 : 0), false));
                    }
                }
            }
        }
        JsonObject json = new JsonObject();
        json.add("multipart", parts);
        return json;
    }

    private static JsonObject condition(Direction facing, AttachFace face) {
        JsonObject when = new JsonObject();
        when.addProperty("facing", facing.getSerializedName());
        when.addProperty("face", face.getSerializedName());
        return when;
    }

    private static JsonObject part(JsonObject when, String model, int x, int y, boolean uvLock) {
        JsonObject apply = new JsonObject();
        apply.addProperty("model", model);
        if (x % 360 != 0) apply.addProperty("x", x % 360);
        if (y % 360 != 0) apply.addProperty("y", y % 360);
        if (uvLock) apply.addProperty("uvlock", true);
        JsonObject part = new JsonObject();
        part.add("when", when);
        part.add("apply", apply);
        return part;
    }
}
