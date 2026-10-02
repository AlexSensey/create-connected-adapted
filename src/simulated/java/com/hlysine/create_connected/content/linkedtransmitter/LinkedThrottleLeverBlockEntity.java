package com.hlysine.create_connected.content.linkedtransmitter;

import com.simibubi.create.content.redstone.link.LinkBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import dev.simulated_team.simulated.content.blocks.throttle_lever.ThrottleLeverBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class LinkedThrottleLeverBlockEntity extends ThrottleLeverBlockEntity {
    /**
     * set to false if the module item is already returned to player via wrenching
     */
    public boolean containsBase = true;
    private LinkBehaviour link;

    public LinkedThrottleLeverBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        createLink();
        behaviours.add(link);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        LinkedTransmitterRemoval.prepare(this, containsBase);
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    public void initialize() {
        super.initialize();
        transmit();
    }

    protected void createLink() {
        Pair<ValueBoxTransform, ValueBoxTransform> slots =
                ValueBoxTransform.Dual.makeSlots(LinkedTransmitterFrequencySlot::new);
        link = LinkBehaviour.transmitter(this, slots, this::getState);
    }

    public void transmit() {
        if (hasLevel() && !level.isClientSide() && link != null)
            link.notifySignalChange();
    }

    public float getHandleRenderValue(float partialTicks) {
        return clientAngle.getValue(partialTicks);
    }

    public float getButtonRenderValue(float partialTicks) {
        return clientPressedLerp.getValue(partialTicks);
    }

    public VoxelShape getHandleOutline() {
        var block = (LinkedThrottleLeverBlock) getBlockState().getBlock();
        return block.getHandleShape(block.getBase().defaultBlockState());
    }

    @Override
    public void setSignal(int signal) {
        super.setSignal(Math.clamp(signal, 0, 15));
        updateTransmittedSignal();
    }

    @Override
    public void tick() {
        int previousChange = lastChange;
        super.tick();
        if (previousChange > 0 && lastChange == 0)
            updateTransmittedSignal();
    }

    private void updateTransmittedSignal() {
        if (!hasLevel() || level.isClientSide())
            return;
        transmit();
        boolean powered = getState() > 0;
        if (getBlockState().getValue(BlockStateProperties.POWERED) != powered)
            level.setBlock(worldPosition, getBlockState().setValue(BlockStateProperties.POWERED, powered), Block.UPDATE_ALL);
    }
}
