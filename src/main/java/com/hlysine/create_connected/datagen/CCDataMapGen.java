package com.hlysine.create_connected.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hlysine.create_connected.compat.DyeDepotCompat;
import com.hlysine.create_connected.compat.Mods;
import com.hlysine.create_connected.registries.CCBlocks;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.conditions.ConditionalOps;

import java.util.concurrent.CompletableFuture;

/** Generates the optional coloring map without loading Dragons Plus classes. */
public class CCDataMapGen implements DataProvider {
    private final PackOutput output;

    public CCDataMapGen(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        JsonObject values = new JsonObject();
        CCBlocks.FAN_DYEING_CATALYSTS.forEach((color, block) -> values.add(block.getId().toString(),
                coloringEntry(DyeDepotCompat.getColorNamespace(color), color.getSerializedName())));
        JsonObject map = new JsonObject();
        map.add("values", values);
        var path = output.createPathProvider(PackOutput.Target.DATA_PACK, "data_maps/block")
                .json(Identifier.fromNamespaceAndPath(Mods.DRAGONS_PLUS.id(), "fan_processing_catalysts/coloring"));
        return DataProvider.saveStable(cache, map, path);
    }

    static JsonObject coloringEntry(String namespace, String color) {
        JsonObject condition = modLoaded(Mods.DRAGONS_PLUS.id());
        if (!namespace.equals(Identifier.DEFAULT_NAMESPACE)) {
            JsonArray requiredMods = new JsonArray();
            requiredMods.add(condition);
            requiredMods.add(modLoaded(Mods.DYE_DEPOT.id()));
            condition = new JsonObject();
            condition.addProperty("type", "neoforge:and");
            condition.add("values", requiredMods);
        }
        JsonArray conditions = new JsonArray();
        conditions.add(condition);
        JsonObject entry = new JsonObject();
        entry.add(ConditionalOps.DEFAULT_CONDITIONS_KEY, conditions);
        entry.addProperty(ConditionalOps.CONDITIONAL_VALUE_KEY, namespace + ":" + color);
        return entry;
    }

    private static JsonObject modLoaded(String modId) {
        JsonObject condition = new JsonObject();
        condition.addProperty("type", "neoforge:mod_loaded");
        condition.addProperty("modid", modId);
        return condition;
    }

    @Override
    public String getName() {
        return "Create Connected coloring data map";
    }
}
