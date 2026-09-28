package com.hlysine.create_connected.compat;

import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;

/*
 * Base class of the two feature refresh events.
 *
 * @see FeatureRefreshEvent.Pre
 * @see FeatureRefreshEvent.Post
 */
public class FeatureRefreshEvent extends Event {
    private final Identifier jeiPluginId;
    private final IIngredientManager ingredientManager;

    protected FeatureRefreshEvent(Identifier jeiPluginId, IIngredientManager ingredientManager) {
        this.jeiPluginId = jeiPluginId;
        this.ingredientManager = ingredientManager;
    }

    public Identifier getJeiPluginId() {
        return jeiPluginId;
    }

    public IIngredientManager getIngredientManager() {
        return ingredientManager;
    }

    /**
     * Fired before Create: Connected updates the JEI item list according to enabled features.
     */
    public static class Pre extends FeatureRefreshEvent {
        public Pre(Identifier jeiPluginId, IIngredientManager ingredientManager) {
            super(jeiPluginId, ingredientManager);
        }
    }

    /**
     * Fired after Create: Connected updates the JEI item list according to enabled features.
     */
    public static class Post extends FeatureRefreshEvent {
        public Post(Identifier jeiPluginId, IIngredientManager ingredientManager) {
            super(jeiPluginId, ingredientManager);
        }
    }
}
