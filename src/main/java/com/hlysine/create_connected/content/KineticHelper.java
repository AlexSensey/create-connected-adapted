package com.hlysine.create_connected.content;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class KineticHelper {
    public static void updateKineticBlock(KineticBlockEntity kineticTE) {
        Level level = kineticTE.getLevel();
        if (level == null || level.isClientSide() || kineticTE.isRemoved())
            return;
        if (kineticTE.hasNetwork())
            kineticTE.getOrCreateNetwork().remove(kineticTE);
        kineticTE.detachKinetics();
        kineticTE.removeSource();
        BlockState state = kineticTE.getBlockState();
        BlockPos pos = kineticTE.getBlockPos();
        // Reattach even if an indirect shape notification is suppressed during a state change.
        kineticTE.updateSpeed = true;
        level.markAndNotifyBlock(pos, level.getChunkAt(pos), state, state, 3, 512);
        if (kineticTE instanceof GeneratingKineticBlockEntity generatingBlockEntity) {
            generatingBlockEntity.reActivateSource = true;
        }
    }
}
