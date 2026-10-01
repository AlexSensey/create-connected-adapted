package com.hlysine.create_connected.content.inventoryaccessport;

import net.createmod.catnip.api.math.BlockFace;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public final class ConnectedInventoryLookup {
    private ConnectedInventoryLookup() {}

    public static ResourceHandler<ItemResource> find(Level level, BlockFace target) {
        if (level == null || target == null || !level.isLoaded(target.getConnectedPos())) return null;
        ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK,
            target.getConnectedPos(), target.getOppositeFace());
        // Match the old WrappedItemHandler exclusion for direct port/bridge connections.
        return handler instanceof RoutedResourceHandler<?> ? null : handler;
    }
}
