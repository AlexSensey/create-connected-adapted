package com.hlysine.create_connected.content.contraption.menu;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class TrackingContainerLevelAccess implements ContainerLevelAccess {
    private final Level level;
    private final AbstractContraptionEntity entity;
    private final BlockPos localPos;
    private BlockPos lastWorldPos;

    public TrackingContainerLevelAccess(Level level, AbstractContraptionEntity entity, BlockPos localPos) {
        this.level = level;
        this.entity = entity;
        this.localPos = localPos.immutable();
        this.lastWorldPos = this.localPos;
    }

    public boolean stillValid(Player player, Predicate<BlockState> validBlock) {
        if (entity == null || entity.isRemoved() || entity.level() != level || player.level() != level)
            return false;
        var contraption = entity.getContraption();
        if (contraption == null)
            return false;
        var info = contraption.getBlocks().get(localPos);
        return info != null && validBlock.test(info.state())
                && evaluate((world, pos) -> player.isWithinBlockInteractionRange(pos, 4), false);
    }

    @Override
    public <T> @NotNull Optional<T> evaluate(BiFunction<Level, BlockPos, T> provideLevelPos) {
        // Menu removal uses this callback to return input items even after disassembly.
        // Freeze the last position when the entity is gone or has changed levels.
        if (entity != null && !entity.isRemoved() && entity.level() == level)
            lastWorldPos = BlockPos.containing(entity.toGlobalVector(Vec3.atCenterOf(localPos), 1));
        return Optional.of(provideLevelPos.apply(level, lastWorldPos));
    }
}
