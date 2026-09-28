package com.hlysine.create_connected.content.linkedtransmitter;

import com.hlysine.create_connected.registries.CCItems;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Carries module ownership across the two phases of block removal in 26.2. */
final class LinkedTransmitterRemoval {
    private record Key(ResourceKey<Level> dimension, BlockPos pos) {}
    private static final Map<Key, Boolean> PENDING = new HashMap<>();

    private LinkedTransmitterRemoval() {}

    static void prepare(BlockEntity entity, boolean containsModule) {
        if (entity.getLevel() instanceof ServerLevel level)
            PENDING.put(new Key(level.dimension(), entity.getBlockPos()), containsModule);
    }

    static void finish(ServerLevel level, BlockPos pos, boolean isMoving) {
        Boolean containsModule = PENDING.remove(new Key(level.dimension(), pos));
        if (!isMoving && Boolean.TRUE.equals(containsModule))
            Block.popResource(level, pos, new ItemStack(CCItems.LINKED_TRANSMITTER.get()));
    }
}
