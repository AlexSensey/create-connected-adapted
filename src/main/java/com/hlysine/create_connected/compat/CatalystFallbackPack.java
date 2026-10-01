package com.hlysine.create_connected.compat;

import com.google.gson.JsonParser;
import com.hlysine.create_connected.CreateConnected;
import com.simibubi.create.foundation.pack.DynamicPack;
import com.simibubi.create.foundation.pack.DynamicPackSource;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.AddPackFindersEvent;

/** Keep saved optional catalysts renderable without supplying another mod's assets. */
public final class CatalystFallbackPack {
    public static void register(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;
        try (var stream = CatalystFallbackPack.class.getResourceAsStream(
                "/assets/create_connected/compat/catalyst_fallbacks.json")) {
            if (stream == null) throw new IllegalStateException("Missing catalyst fallback definitions");
            var definitions = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            var replacements = definitions.getAsJsonObject("replacements");
            var pack = new DynamicPack("create_connected:catalyst_fallbacks", PackType.CLIENT_RESOURCES);
            boolean populated = false;
            for (var entry : definitions.getAsJsonObject("models").entrySet()) {
                var model = entry.getValue().getAsJsonObject().deepCopy();
                var textures = model.getAsJsonObject("textures");
                boolean changed = false;
                for (var texture : textures.entrySet()) {
                    String original = texture.getValue().getAsString();
                    if (replacements.has(original) && !ModList.get().isLoaded(original.substring(0, original.indexOf(':')))) {
                        texture.setValue(replacements.get(original));
                        changed = true;
                    }
                }
                if (changed) {
                    pack.put(CreateConnected.asResource(entry.getKey()), model.toString());
                    populated = true;
                }
            }
            if (populated) event.addRepositorySource(new DynamicPackSource(
                    "create_connected:catalyst_fallbacks", PackType.CLIENT_RESOURCES, Pack.Position.TOP, pack));
        } catch (java.io.IOException error) {
            throw new IllegalStateException("Cannot load catalyst fallbacks", error);
        }
    }
}
