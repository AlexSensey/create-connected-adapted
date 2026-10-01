package com.hlysine.create_connected.compat;

import com.hlysine.create_connected.config.FeatureToggle;
import com.hlysine.create_connected.registries.CCCreativeTabs;
import java.util.Collection;
import java.util.ArrayList;
import me.shedaniel.rei.api.client.entry.filtering.base.BasicFilteringRule;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;

/** Apply the same feature conditions as Connected's creative tab and JEI integration. */
@REIPluginClient
public final class CreateConnectedREI implements REIClientPlugin {
    @Override
    public void registerBasicEntryFiltering(BasicFilteringRule<?> rule) {
        rule.hide(CreateConnectedREI::hiddenItems);
    }

    private static Collection<EntryStack<?>> hiddenItems() {
        Collection<EntryStack<?>> hidden = new ArrayList<>();
        for (var entry : CCCreativeTabs.ITEMS)
            if (!FeatureToggle.isEnabled(entry.getId())) hidden.add(EntryStacks.of(entry.asStack()));
        return hidden;
    }

    public static void refreshItemList() {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        // Config Loading also fires before the Minecraft singleton exists.
        if (minecraft != null && minecraft.level != null)
            minecraft.execute(() -> me.shedaniel.rei.RoughlyEnoughItemsCoreClient.reloadPlugins(null, null));
    }
}
