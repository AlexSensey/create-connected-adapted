package com.hlysine.create_connected.content.itemsilo;

import com.hlysine.create_connected.registries.CCBlockEntityTypes;
import com.hlysine.create_connected.registries.CCBlocks;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.logistics.vault.ItemVaultBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.item.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceKey;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.neoforge.common.util.DeferredSoundType;

import org.jetbrains.annotations.Nullable;

public class ItemSiloBlock extends Block implements IWrenchable, IBE<ItemSiloBlockEntity> {
    private record RemovedSiloKey(ResourceKey<Level> dimension, BlockPos pos) {}
    private static final Map<RemovedSiloKey, ItemSiloBlockEntity> REMOVED_SILOS = new HashMap<>();

    public static final BooleanProperty LARGE = ItemVaultBlock.LARGE;

    public ItemSiloBlock(Properties p_i48440_1_) {
        super(p_i48440_1_);
        registerDefaultState(defaultBlockState().setValue(LARGE, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
        pBuilder.add(LARGE);
        super.createBlockStateDefinition(pBuilder);
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        if (pOldState.getBlock() == pState.getBlock())
            return;
        if (pIsMoving)
            return;
        withBlockEntityDo(pLevel, pPos, ItemSiloBlockEntity::updateConnectivity);
    }

    static void prepareRemoval(ItemSiloBlockEntity silo) {
        REMOVED_SILOS.put(new RemovedSiloKey(silo.getLevel().dimension(), silo.getBlockPos()), silo);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean isMoving) {
        ItemSiloBlockEntity removed = REMOVED_SILOS.remove(new RemovedSiloKey(world.dimension(), pos));
        if (removed != null)
            ConnectivityHandler.splitMulti(removed);
        super.affectNeighborsAfterRemoval(state, world, pos, isMoving);
    }

    public static boolean isVault(BlockState state) {
        return CCBlocks.ITEM_SILO.has(state);
    }

    @Nullable
    public static Direction.Axis getVaultBlockAxis(BlockState state) {
        if (!isVault(state))
            return null;
        return Direction.Axis.Y;
    }

    public static boolean isLarge(BlockState state) {
        if (!isVault(state))
            return false;
        return state.getValue(LARGE);
    }

    // Vaults are less noisy when placed in batch
    public static final SoundType SILENCED_METAL =
            new DeferredSoundType(0.1F, 1.5F, () -> SoundEvents.NETHERITE_BLOCK_BREAK, () -> SoundEvents.NETHERITE_BLOCK_STEP,
                    () -> SoundEvents.NETHERITE_BLOCK_PLACE, () -> SoundEvents.NETHERITE_BLOCK_HIT,
                    () -> SoundEvents.NETHERITE_BLOCK_FALL);

    @Override
    public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, Entity entity) {
        SoundType soundType = super.getSoundType(state, world, pos, entity);
        if (entity != null && entity.getPersistentData()
                .contains("SilenceVaultSound"))
            return SILENCED_METAL;
        return soundType;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos, net.minecraft.core.Direction side) {
        return ItemHelper.calcRedstoneFromBlockEntity(this, pLevel, pPos);
    }

    @Override
    public BlockEntityType<? extends ItemSiloBlockEntity> getBlockEntityType() {
        return CCBlockEntityTypes.ITEM_SILO.get();
    }

    @Override
    public Class<ItemSiloBlockEntity> getBlockEntityClass() {
        return ItemSiloBlockEntity.class;
    }
}

