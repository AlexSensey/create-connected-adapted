package com.hlysine.create_connected.content.linkedtransmitter;

import com.hlysine.create_connected.registries.CCShapes;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public interface LinkedTransmitterBlock {
    Block getBlock();

    Block getBase();

    void replaceBase(BlockState baseState, Level world, BlockPos pos);

    /** Retains a lever's native data while replacing its block/entity type. */
    default boolean replacePreservingData(BlockState replacement, Level world, BlockPos pos) {
        if (world.isClientSide())
            return false;
        var previous = world.getBlockEntity(pos);
        var data = previous == null ? null : previous.saveWithoutMetadata(world.registryAccess());
        if (!world.setBlockAndUpdate(pos, replacement))
            return false;
        var installed = world.getBlockEntity(pos);
        if (data != null && installed != null) {
            try (var problems = new net.minecraft.util.ProblemReporter.ScopedCollector(
                    com.hlysine.create_connected.CreateConnected.LOGGER)) {
                installed.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(
                        problems, world.registryAccess(), data));
            }
            installed.setChanged();
            if (installed instanceof com.simibubi.create.foundation.blockEntity.SmartBlockEntity smart)
                smart.sendData();
            world.updateNeighborsAt(pos, replacement.getBlock());
        }
        return true;
    }

    default VoxelShape getTransmitterShape(BlockState state) {
        Direction facing = state.getValue(FaceAttachedHorizontalDirectionalBlock.FACING);
        return switch (state.getValue(FaceAttachedHorizontalDirectionalBlock.FACE)) {
            case FLOOR -> CCShapes.FLOOR_LINKED_TRANSMITTER.get(facing);
            case WALL -> CCShapes.WALL_LINKED_TRANSMITTER.get(facing);
            case CEILING -> CCShapes.CEILING_LINKED_TRANSMITTER.get(facing);
        };
    }

    default boolean isHittingBase(BlockState state, BlockGetter level, BlockPos pos, HitResult hit) {
        return !getTransmitterShape(state).bounds().inflate(0.01 / 16).move(pos).contains(hit.getLocation());
    }

    default @NotNull InteractionResult useTransmitter(@NotNull BlockState state,
                                                      @NotNull Level level,
                                                      @NotNull BlockPos pos,
                                                      @NotNull Player player) {
        if (state.getValue(BlockStateProperties.LOCKED))
            return InteractionResult.CONSUME;
        return InteractionResult.PASS;
    }

    default InteractionResult useWax(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (player == null || player.isSpectator())
            return InteractionResult.PASS;
        boolean locking = stack.is(Items.HONEYCOMB) && !state.getValue(BlockStateProperties.LOCKED);
        boolean unlocking = stack.is(ItemTags.AXES) && state.getValue(BlockStateProperties.LOCKED);
        if ((locking || unlocking) && (!player.mayBuild()
                || !player.mayUseItemAt(pos, hitResult.getDirection(), stack)))
            return InteractionResult.PASS;
        if (locking) {
            if (level.isClientSide())
                return InteractionResult.SUCCESS;
            BlockState newState = state.setValue(BlockStateProperties.LOCKED, true);
            if (!level.setBlock(pos, newState, Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_ALL))
                return InteractionResult.FAIL;
            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
            }
            if (!player.isCreative())
                stack.shrink(1);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, newState));
            level.levelEvent(player, 3003, pos, 0);
            return InteractionResult.CONSUME;
        }
        if (unlocking) {
            if (level.isClientSide())
                return InteractionResult.SUCCESS;
            BlockState newState = state.setValue(BlockStateProperties.LOCKED, false);
            if (!level.setBlock(pos, newState, Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_ALL))
                return InteractionResult.FAIL;
            level.playSound(player, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.levelEvent(player, 3004, pos, 0);
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer) player, pos, stack);
            }
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, newState));
            if (!player.isCreative()) {
                stack.hurtAndBreak(1, player, hand);
            }

            return InteractionResult.CONSUME;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }
}
