package com.hlysine.create_connected.datagen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hlysine.create_connected.registries.CCJukeboxSongs;
import com.hlysine.create_connected.registries.CCPonderPlugin;
import com.hlysine.create_connected.registries.CCSoundEvents;
import com.hlysine.create_connected.CreateConnected;
import com.hlysine.create_connected.datagen.advancements.CCAdvancements;
import com.hlysine.create_connected.datagen.recipes.CreateConnectedProcessingRecipeGen;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.tterrag.registrate.providers.ProviderType;
import net.createmod.ponder.api.client.PonderIndex;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class CCDatagen {
    private static boolean extraDataRegistered;

    public static void gatherClientHighPriority(GatherDataEvent.Client event) {
        registerExtraData(event);
    }

    public static void gatherServerHighPriority(GatherDataEvent.Server event) {
        registerExtraData(event);
    }

    private static void registerExtraData(GatherDataEvent event) {
        if (!event.getModContainer().getModId().equals(CreateConnected.MODID) || extraDataRegistered) return;
        addExtraRegistrateData();
        extraDataRegistered = true;
    }

    public static void gatherClientData(GatherDataEvent.Client event) {
        if (!event.getModContainer().getModId().equals(CreateConnected.MODID)) return;
        event.addProvider(CCSoundEvents.provider(event.getGenerator()));
        event.addProvider(new CCSimpleModelGen(event.getGenerator().getPackOutput()));
    }

    public static void gatherServerData(GatherDataEvent.Server event) {
        if (!event.getModContainer().getModId().equals(CreateConnected.MODID)) return;
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getWorldLookupProvider();
        CCTagGen.addGenerators(event);
        event.addProvider(new CCDataMapGen(output));
        event.addProvider(new CCAdvancements(output, lookupProvider));
        event.addProvider(CCJukeboxSongs.provider(output, lookupProvider));
        CreateConnectedProcessingRecipeGen.registerAllProcessing(event);
    }

    private static void addExtraRegistrateData() {
        CreateConnected.getRegistrate().addDataGenerator(ProviderType.LANG, provider -> {
            BiConsumer<String, String> langConsumer = provider::add;

            provideDefaultLang("interface", langConsumer);
            provideDefaultLang("tooltips", langConsumer);
            CCAdvancements.provideLang(langConsumer);
            CCSoundEvents.provideLang(langConsumer);
            providePonderLang(langConsumer);
        });
    }

    private static void provideDefaultLang(String fileName, BiConsumer<String, String> consumer) {
        String path = "assets/create_connected/lang/default/" + fileName + ".json";
        JsonElement jsonElement = FilesHelper.loadJsonResource(path);
        if (jsonElement == null) {
            throw new IllegalStateException(String.format("Could not find default lang file: %s", path));
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().getAsString();
            consumer.accept(key, value);
        }
    }

    private static void providePonderLang(BiConsumer<String, String> consumer) {
        // Register this since FMLClientSetupEvent does not run during datagen
        PonderIndex.addPlugin(new CCPonderPlugin());

        PonderIndex.getLangAccess().provideLang(CreateConnected.MODID, consumer);
    }
}

