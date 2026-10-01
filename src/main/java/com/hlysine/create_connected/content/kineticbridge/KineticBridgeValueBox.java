package com.hlysine.create_connected.content.kineticbridge;

import com.hlysine.create_connected.content.kineticbattery.KineticBatteryValueBox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Keep the label centered, but allow holding use anywhere on a lateral face. */
public class KineticBridgeValueBox extends KineticBatteryValueBox {
    public KineticBridgeValueBox() {
        super(8);
    }

    @Override
    public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
        Vec3 offset = super.getLocalOffset(level, pos, state);
        Direction facing = state.getValue(KineticBridgeBlock.FACING);
        double panelShift = state.getBlock() instanceof KineticBridgeDestinationBlock ? 3 / 16d : -3 / 16d;
        return offset.add(facing.getStepX() * panelShift, facing.getStepY() * panelShift,
                facing.getStepZ() * panelShift);
    }

    @Override
    public boolean testHit(LevelAccessor level, BlockPos pos, BlockState state, Vec3 localHit) {
        return isSideActive(state, getSide())
                && localHit.x >= -1e-6 && localHit.x <= 1 + 1e-6
                && localHit.y >= -1e-6 && localHit.y <= 1 + 1e-6
                && localHit.z >= -1e-6 && localHit.z <= 1 + 1e-6;
    }
}
