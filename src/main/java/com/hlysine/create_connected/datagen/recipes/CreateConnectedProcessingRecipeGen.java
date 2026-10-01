package com.hlysine.create_connected.datagen.recipes;

import com.hlysine.create_connected.CreateConnected;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.recipes.RecipeProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class CreateConnectedProcessingRecipeGen {
    public static void registerAllProcessing(GatherDataEvent.Server event) {
        RegistrySetBuilder recipes = new RegistrySetBuilder();
        recipes.add(RecipeProvider.asBootstrap(CCStandardRecipes::new));
        recipes.add(RecipeProvider.asBootstrap(SequencedAssemblyGen::new));
        recipes.add(RecipeProvider.asBootstrap(CuttingRecipeGen::new));
        recipes.add(RecipeProvider.asBootstrap(FillingRecipeGen::new));
        recipes.add(RecipeProvider.asBootstrap(ItemApplicationRecipeGen::new));
        event.createReloadableRegistryObjects(recipes, java.util.Set.of(CreateConnected.MODID), "Connected recipes");
    }
}
