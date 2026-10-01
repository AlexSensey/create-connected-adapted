package com.hlysine.create_connected.content.linkedtransmitter;

import com.hlysine.create_connected.registries.CCItems;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Carries module ownership across the two phases of block removal in 26.2. */
final class LinkedTransmitterRemoval {
    private static final Map<ServerLevel, Map<BlockPos, Boolean>> PENDING = new WeakHashMap<>();

    private LinkedTransmitterRemoval() {}

    static void prepare(BlockEntity entity, boolean containsModule) {
        if (entity.getLevel() instanceof ServerLevel level)
            PENDING.computeIfAbsent(level, ignored -> new HashMap<>())
                    .put(entity.getBlockPos().immutable(), containsModule);
    }

    static void finish(ServerLevel level, BlockPos pos, boolean isMoving) {
        var positions = PENDING.get(level);
        if (positions == null)
            return;
        Boolean containsModule = positions.remove(pos);
        if (positions.isEmpty())
            PENDING.remove(level);
        if (!isMoving && Boolean.TRUE.equals(containsModule))
            Block.popResource(level, pos, new ItemStack(CCItems.LINKED_TRANSMITTER.get()));
    }
}
