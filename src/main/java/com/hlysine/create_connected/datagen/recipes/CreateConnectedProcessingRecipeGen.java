package com.hlysine.create_connected.datagen.recipes;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public final class CreateConnectedProcessingRecipeGen {
    public static void registerAllProcessing(DataGenerator gen, PackOutput output,
                                             CompletableFuture<HolderLookup.Provider> registries) {
        add(gen, output, registries, "standard", CCStandardRecipes::new);
        add(gen, output, registries, "sequenced assembly", SequencedAssemblyGen::new);
        add(gen, output, registries, "cutting", CuttingRecipeGen::new);
        add(gen, output, registries, "filling", FillingRecipeGen::new);
        add(gen, output, registries, "item application", ItemApplicationRecipeGen::new);
    }

    private static void add(DataGenerator gen, PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
                            String name, BiFunction<HolderLookup.Provider, RecipeOutput, RecipeProvider> factory) {
        gen.addProvider(true, new RecipeProvider.Runner(output, registries) {
            @Override
            protected RecipeProvider createRecipeProvider(HolderLookup.Provider lookup, RecipeOutput recipes) {
                return factory.apply(lookup, recipes);
            }

            @Override
            public String getName() {
                return "Create: Connected " + name + " recipes";
            }
        });
    }
}
