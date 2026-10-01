package com.hlysine.create_connected.content.linkedtransmitter;

import com.hlysine.create_connected.registries.CCItems;
import com.hlysine.create_connected.compat.SimCompatRegistry;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.simulated_team.simulated.content.blocks.throttle_lever.ThrottleLeverBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LinkedThrottleLeverBlock extends ThrottleLeverBlock implements SpecialBlockItemRequirement, IWrenchable, LinkedTransmitterBlock {
    public static BooleanProperty LOCKED = BlockStateProperties.LOCKED;

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private final NonNullSupplier<ThrottleLeverBlock> baseSupplier;

    public LinkedThrottleLeverBlock(Properties pProperties, NonNullSupplier<ThrottleLeverBlock> baseSupplier) {
        super(pProperties);
        registerDefaultState(defaultBlockState().setValue(POWERED, false).setValue(LOCKED, false));
        this.baseSupplier = baseSupplier;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(POWERED, LOCKED));
    }

    @Override
    public Block getBlock() {
        return this;
    }

    @Override
    public Block getBase() {
        return baseSupplier.get();
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state,
                                        @NotNull BlockGetter level,
                                        @NotNull BlockPos pos,
                                        @NotNull CollisionContext context) {
        return Shapes.or(getTransmitterShape(state), super.getShape(state, level, pos, context));
    }

    @Override
    public @NotNull List<ItemStack> getDrops(@NotNull BlockState state, LootParams.@NotNull Builder builder) {
        return getBase().defaultBlockState().getDrops(builder);
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(@NotNull BlockState state,
                                                     @NotNull Level level,
                                                     @NotNull BlockPos pos,
                                                     @NotNull Player player,
                                                     @NotNull BlockHitResult hitResult) {
        if (player.isSpectator())
            return InteractionResult.PASS;

        if (isHittingBase(state, level, pos, hitResult)) {
            return super.useWithoutItem(state, level, pos, player, hitResult);
        }
        return LinkedTransmitterBlock.super.useTransmitter(state, level, pos, player);
    }

    @Override
    public @NotNull InteractionResult useItemOn(@NotNull ItemStack stack,
                                                    @NotNull BlockState state,
                                                    @NotNull Level level,
                                                    @NotNull BlockPos pos,
                                                    @NotNull Player player,
                                                    @NotNull InteractionHand hand,
                                                    @NotNull BlockHitResult hitResult) {
        return LinkedTransmitterBlock.super.useWax(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean isMoving) {
        LinkedTransmitterRemoval.finish(world, pos, isMoving);
        super.affectNeighborsAfterRemoval(state, world, pos, isMoving);
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        if (context.getPlayer() == null)
            return InteractionResult.PASS;
        if (context.getLevel().isClientSide())
            return InteractionResult.SUCCESS;
        InteractionResult result = onWrenched(state, context);
        if (result != InteractionResult.SUCCESS)
            return result;
        return super.onSneakWrenched(state, context);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null)
            return InteractionResult.PASS;
        if (context.getLevel().isClientSide())
            return InteractionResult.SUCCESS;
        withBlockEntityDo(context.getLevel(), context.getClickedPos(), be -> ((LinkedThrottleLeverBlockEntity) be).containsBase = false);
        replaceWithBase(state, context.getLevel(), context.getClickedPos());
        if (!context.getLevel().getBlockState(context.getClickedPos()).is(getBase())) {
            withBlockEntityDo(context.getLevel(), context.getClickedPos(), be -> ((LinkedThrottleLeverBlockEntity) be).containsBase = true);
            return InteractionResult.FAIL;
        }
        if (!player.isCreative()) {
            player.getInventory().placeItemBackInInventory(new ItemStack(CCItems.LINKED_TRANSMITTER.get()));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void replaceBase(BlockState baseState, Level world, BlockPos pos) {
        if (!replacePreservingData(defaultBlockState()
                .setValue(FACING, baseState.getValue(FACING))
                .setValue(FACE, baseState.getValue(FACE))
                .setValue(ThrottleLeverBlock.INVERTED, baseState.getValue(ThrottleLeverBlock.INVERTED)), world, pos))
            return;
        withBlockEntityDo(world, pos, be -> {
            var linked = (LinkedThrottleLeverBlockEntity) be;
            world.setBlock(pos, world.getBlockState(pos).setValue(POWERED, linked.getState() > 0), Block.UPDATE_ALL);
            linked.transmit();
        });
        AllSoundEvents.CONTROLLER_PUT.playOnServer(world, pos);
    }

    public void replaceWithBase(BlockState state, Level world, BlockPos pos) {
        if (replacePreservingData(getBase().defaultBlockState()
                .setValue(FACING, state.getValue(FACING))
                .setValue(FACE, state.getValue(FACE))
                .setValue(ThrottleLeverBlock.INVERTED, state.getValue(ThrottleLeverBlock.INVERTED)), world, pos))
            AllSoundEvents.CONTROLLER_TAKE.playOnServer(world, pos);
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(@NotNull LevelReader world, @NotNull BlockPos pos,
                                                @NotNull BlockState state, boolean includeData,
                                                @NotNull Player player) {
        HitResult target = player.pick(player.blockInteractionRange(), 1, false);
        if (isHittingBase(state, world, pos, target))
            return getBase().getCloneItemStack(world, pos, state, includeData, player);
        return new ItemStack(CCItems.LINKED_TRANSMITTER.get());
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity be) {
        ArrayList<ItemStack> requiredItems = new ArrayList<>();
        requiredItems.add(new ItemStack(getBase()));
        requiredItems.add(new ItemStack(CCItems.LINKED_TRANSMITTER.get()));
        return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, requiredItems);
    }

    @Override
    public BlockEntityType<? extends LinkedThrottleLeverBlockEntity> getBlockEntityType() {
        return SimCompatRegistry.LINKED_THROTTLE_LEVER_ENTITY.get();
    }
}
